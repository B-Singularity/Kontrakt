package execution.architecture

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.domain.JavaModifier
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TypeInsnNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.AnalyzerException
import org.objectweb.asm.tree.analysis.BasicInterpreter
import org.objectweb.asm.tree.analysis.BasicValue
import java.util.ArrayDeque
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.fail

/**
 * [The Kontrakt Core Constitution]
 * Physically enforces the Absolute Protocols.
 * Features OS-Agnostic Multimodule Classpath Defense, Re-entrant Callback Blocking via Reachability,
 * Absolute Impl This-Escape Bans, Transitive Taint Propagation, and Thread-Safe Validation.
 */
class CompilerCoreArchTest {

    companion object {
        @Volatile
        private lateinit var coreClasses: JavaClasses
        @Volatile
        private lateinit var classNodes: Map<String, ClassNode>

        // Dynamically syncs the general scan boundary for classloading
        @Volatile
        private lateinit var activeScanRoots: Set<String>

        // Strict boundary for Fail-Closed enforcement (Core Domain)
        @Volatile
        private lateinit var trustedDomainRoots: Set<String>

        private val implementersCache = ConcurrentHashMap<String, List<ClassNode>>()
        private val methodOwnerCache = ConcurrentHashMap<String, String>()

        fun sig(owner: String, name: String, desc: String) = "$owner.$name.$desc"

        val prims = listOf("[Z", "[C", "[B", "[S", "[I", "[F", "[J", "[D")

        val primitiveCopyMethods = buildSet {
            for (p in prims) {
                add(sig("java/util/Arrays", "copyOf", "(${p}I)${p}"))
                add(sig("java/util/Arrays", "copyOfRange", "(${p}II)${p}"))
            }
        }

        val readOnlyArrayMethods = buildSet {
            for (p in prims) {
                add(sig("java/util/Arrays", "hashCode", "($p)I"))
                add(sig("java/util/Arrays", "equals", "($p$p)Z"))
                add(sig("java/util/Arrays", "toString", "($p)Ljava/lang/String;"))
            }
            add(sig("java/security/MessageDigest", "update", "([B)V"))
        }

        @JvmStatic
        @BeforeAll
        fun setup() {
            implementersCache.clear()
            methodOwnerCache.clear()

            val defaultRoots = setOf(
                "execution", "linking", "metamodel", "infrastructure",
                "discovery", "reporting", "common", "exception", "ir"
            )

            val scanRootsProp = System.getProperty("kontrakt.scan.packages")

            if (scanRootsProp != null && scanRootsProp.contains("/")) {
                fail("CRITICAL: 'kontrakt.scan.packages' must use dot notation (e.g., com.example), not slashes.")
            }

            val scanRoots = if (!scanRootsProp.isNullOrBlank()) {
                System.err.println("⚠️ [WARNING] Additional scan roots injected. Merging with default boundaries to prevent scope replacement.")
                defaultRoots + scanRootsProp.split(",").map { it.trim() }.filter { it.isNotBlank() }
            } else {
                defaultRoots
            }

            activeScanRoots = scanRoots.map { it.replace('.', '/') }.toSet()
            trustedDomainRoots = setOf("execution/", "linking/", "metamodel/")

            coreClasses = ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(*scanRoots.toTypedArray())

            if (!coreClasses.iterator().hasNext()) {
                fail("CRITICAL: No classes loaded by ArchUnit from packages: $scanRoots")
            }

            val requiredAnchors = listOf("execution.domain.", "linking.", "metamodel.")
            for (anchor in requiredAnchors) {
                if (!coreClasses.any { it.name.startsWith(anchor) }) {
                    fail("CRITICAL: Anchor package '$anchor' not found. Scan scope may have been hijacked or misconfigured.")
                }
            }

            val localNodes = HashMap<String, ClassNode>(coreClasses.size)

            val threadLoader = Thread.currentThread().contextClassLoader
            val testLoader = CompilerCoreArchTest::class.java.classLoader
            val systemLoader = ClassLoader.getSystemClassLoader()

            val workspaceProp = System.getProperty("kontrakt.workspace.root") ?: System.getProperty("user.dir")
            val workspace = workspaceProp.replace('\\', '/').trimEnd('/')

            val ws1 = "file:$workspace/"
            val ws2 = "file:/$workspace/"
            val ws3 = "file:///$workspace/"
            val jar1 = "jar:file:$workspace/"
            val jar2 = "jar:file:/$workspace/"
            val jar3 = "jar:file:///$workspace/"

            for (javaClass in coreClasses) {
                val sourceUriOpt = runCatching { javaClass.source.orElse(null)?.uri?.toString() }.getOrNull()
                val rawSourceUri = sourceUriOpt?.replace('\\', '/') ?: ""
                val sourceUri = rawSourceUri.replace("%20", " ")

                val internalName = javaClass.name.replace('.', '/')
                val inWorkspace = sourceUri.startsWith(ws1) || sourceUri.startsWith(ws2) || sourceUri.startsWith(ws3) ||
                        sourceUri.startsWith(jar1) || sourceUri.startsWith(jar2) || sourceUri.startsWith(jar3)

                if (sourceUri.isBlank()) {
                    if (trustedDomainRoots.any { internalName.startsWith(it) }) {
                        fail("CRITICAL: Unresolvable CodeSource for Core Domain Class! Class ${javaClass.name} has no valid URI. Failsafe activated to prevent stealth pollution.")
                    } else {
                        System.err.println("⚠️ [INFO] Skipping stealth class outside core domain roots: ${javaClass.name}")
                        continue
                    }
                } else if (!inWorkspace) {
                    fail("CRITICAL: Classpath pollution detected! Class ${javaClass.name} originated from outside trusted workspace boundaries: $sourceUri")
                }

                val resourceName = internalName + ".class"

                var stream = threadLoader?.getResourceAsStream(resourceName)
                    ?: testLoader?.getResourceAsStream(resourceName)
                    ?: systemLoader.getResourceAsStream(resourceName)

                var reflectLoader: ClassLoader? = null

                if (stream == null) {
                    val targetClass = runCatching { javaClass.reflect() }.getOrNull()
                    reflectLoader = targetClass?.classLoader
                    stream = reflectLoader?.getResourceAsStream(resourceName)
                }

                if (stream == null) {
                    fail(
                        """
                        CRITICAL: Bytecode stream not found in classpath (Fail-Closed).
                        Class Name : ${javaClass.name}
                        Resource   : $resourceName
                        Tried Loaders:
                        - ThreadContextLoader: $threadLoader
                        - TestClassLoader   : $testLoader
                        - SystemLoader      : $systemLoader
                        - ReflectionLoader  : $reflectLoader (Fallback)
                        """.trimIndent()
                    )
                }

                stream.use { s ->
                    val reader = ClassReader(s)
                    val node = ClassNode()
                    reader.accept(node, ClassReader.SKIP_DEBUG)
                    localNodes[node.name] = node
                }
            }

            if (localNodes.isEmpty()) fail("CRITICAL: No ASM ClassNodes parsed.")
            classNodes = localNodes.toMap()
        }

        fun implementsTransitively(className: String, targetInterface: String): Boolean {
            val visited = mutableSetOf<String>()
            val queue = ArrayDeque<String>()
            queue.add(className)

            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                if (current == targetInterface) return true
                if (!visited.add(current)) continue

                val node = classNodes[current]
                if (node == null) {
                    // Synchronized Fail-Closed boundary with trustedDomainRoots
                    if (trustedDomainRoots.any { current.startsWith(it) }) {
                        fail("CRITICAL: Core Domain Class '$current' not found in scan scope. Coverage incomplete (Fail-Closed).")
                    }
                    continue
                }

                if (node.superName != null) queue.add(node.superName)
                queue.addAll(node.interfaces ?: emptyList())
            }
            return false
        }

        fun getImplementers(targetInterface: String): List<ClassNode> {
            return implementersCache.computeIfAbsent(targetInterface) { key ->
                classNodes.values.filter { implementsTransitively(it.name, key) }
            }
        }

        fun resolveMethodOwner(startClass: String, mName: String, mDesc: String): String {
            val cacheKey = sig(startClass, mName, mDesc)
            return methodOwnerCache.computeIfAbsent(cacheKey) {
                var curr: String? = startClass
                while (curr != null && curr != "java/lang/Object") {
                    val node = classNodes[curr] ?: break
                    if (node.methods.any { (it as MethodNode).name == mName && it.desc == mDesc }) {
                        return@computeIfAbsent curr
                    }
                    curr = node.superName
                }
                startClass
            }
        }
    }

    // --- Rule 1: Precision JVM Backdoor & Concurrency Hack Ban ---
    @Test
    fun `Domain Core MUST NOT import reflection, KSP, or precision-targeted memory backdoors`() {
        noClasses().that().resideInAPackage("execution.domain..")
            .should().dependOnClassesThat().haveNameMatching("sun\\.misc\\.Unsafe")
            .orShould().dependOnClassesThat().haveNameMatching("jdk\\.internal\\.misc\\.Unsafe")
            .orShould().dependOnClassesThat().haveNameMatching("java\\.lang\\.invoke\\.VarHandle")
            .orShould().dependOnClassesThat().haveNameMatching("java\\.lang\\.invoke\\.MethodHandles")
            .orShould().dependOnClassesThat().haveNameMatching("java\\.util\\.concurrent\\.atomic\\..*FieldUpdater")
            .orShould().dependOnClassesThat().haveNameMatching("java\\.util\\.concurrent\\.locks\\.LockSupport")
            .orShould().dependOnClassesThat()
            .resideInAnyPackage("kotlin.reflect..", "java.lang.reflect..", "sun.reflect..", "com.google.devtools.ksp..")
            .because("Domain Core must not bypass safe publication or utilize reflection.")
            .check(coreClasses)
    }

    // --- Rule 2: Deep Immutability, Exact Taint Origin & Absolute Side-Effect Ban ---
    @Test
    fun `Canonical implementations MUST be final and PROVE Deep Immutability structurally`() {
        val canonicalTypeName = "execution.domain.vo.plan.CanonicalPlanNode"
        val canonicalInterface = coreClasses.firstOrNull { it.name == canonicalTypeName }
            ?: fail("CRITICAL: $canonicalTypeName type not found in loaded classes.")

        val implClasses = coreClasses.filter { it.isAssignableTo(canonicalInterface.name) && !it.isInterface }
        if (implClasses.isEmpty()) fail("CRITICAL: No CanonicalPlanNode implementations found.")

        for (impl in implClasses) {
            if (!impl.name.startsWith("execution.domain.vo.plan.impl.")) fail("CRITICAL: ${impl.name} is not physically isolated in 'impl' package.")
            if (!impl.modifiers.contains(JavaModifier.FINAL)) fail("CRITICAL: ${impl.name} MUST be final.")
        }

        val strictImmutablePrefixes = listOf(
            "Lkotlinx/collections/immutable/Persistent",
            "Ljava/lang/String;",
            "Ljava/time/",
            "Ljava/util/UUID;",
            "Lexecution/domain/vo/plan/InternedPlanNode;",
            "Z", "C", "B", "S", "I", "F", "J", "D"
        )

        val verifiedVOMemo = mutableSetOf<String>()

        class ArrayEscapeException(val insn: AbstractInsnNode, message: String) : AnalyzerException(insn, message)

        class TaintValue(type: Type?, val isParam: Boolean, val isSafeOrigin: Boolean, val isField: Boolean) :
            BasicValue(type) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is TaintValue) return false
                return super.equals(other) && isParam == other.isParam && isSafeOrigin == other.isSafeOrigin && isField == other.isField
            }

            override fun hashCode(): Int =
                super.hashCode() * 31 + isParam.hashCode() + isSafeOrigin.hashCode() + isField.hashCode()
        }

        class TaintInterpreter(val ownerClass: String) : BasicInterpreter(Opcodes.ASM9) {

            override fun newParameterValue(isInstanceMethod: Boolean, local: Int, type: Type): BasicValue {
                val base = super.newParameterValue(isInstanceMethod, local, type)
                if (isInstanceMethod && local == 0) return base

                if (type.sort == Type.ARRAY || type.sort == Type.OBJECT) {
                    return TaintValue(base.type, isParam = true, isSafeOrigin = false, isField = false)
                }
                return base
            }

            override fun newOperation(insn: AbstractInsnNode): BasicValue {
                if (insn.opcode == Opcodes.GETSTATIC) {
                    val fInsn = insn as FieldInsnNode
                    val t = Type.getType(fInsn.desc)
                    if (fInsn.owner == ownerClass && t.sort == Type.ARRAY) {
                        return TaintValue(t, isParam = false, isSafeOrigin = false, isField = true)
                    }
                    if (fInsn.owner != ownerClass && (t.sort == Type.ARRAY || t.sort == Type.OBJECT)) {
                        return TaintValue(t, isParam = true, isSafeOrigin = false, isField = false)
                    }
                }
                if (insn.opcode == Opcodes.NEWARRAY || insn.opcode == Opcodes.ANEWARRAY) {
                    return TaintValue(
                        super.newOperation(insn).type,
                        isParam = false,
                        isSafeOrigin = true,
                        isField = false
                    )
                }
                return super.newOperation(insn)
            }

            override fun copyOperation(insn: AbstractInsnNode, value: BasicValue?): BasicValue {
                if (value is TaintValue) return value
                return super.copyOperation(insn, value)
            }

            override fun unaryOperation(insn: AbstractInsnNode, value: BasicValue?): BasicValue {
                if (insn.opcode == Opcodes.CHECKCAST && value is TaintValue) {
                    val newType = Type.getObjectType((insn as TypeInsnNode).desc)
                    return TaintValue(newType, value.isParam, value.isSafeOrigin, value.isField)
                }
                if (insn.opcode == Opcodes.GETFIELD) {
                    val fInsn = insn as FieldInsnNode
                    val base = super.unaryOperation(insn, value)
                    val t = Type.getType(fInsn.desc)

                    if (fInsn.owner == ownerClass && t.sort == Type.ARRAY) {
                        return TaintValue(base.type, isParam = false, isSafeOrigin = false, isField = true)
                    }
                    if (value is TaintValue && value.isParam && (t.sort == Type.ARRAY || t.sort == Type.OBJECT)) {
                        return TaintValue(base.type, isParam = true, isSafeOrigin = false, isField = false)
                    }
                    return base
                }
                return super.unaryOperation(insn, value)
            }

            override fun naryOperation(insn: AbstractInsnNode, values: MutableList<out BasicValue>?): BasicValue {
                val res = super.naryOperation(insn, values)
                if (insn.opcode == Opcodes.MULTIANEWARRAY) {
                    return TaintValue(res.type, isParam = false, isSafeOrigin = true, isField = false)
                }
                if (insn is MethodInsnNode) {
                    val methodSig = sig(insn.owner, insn.name, insn.desc)
                    val isArrayClone = insn.name == "clone" && (values?.firstOrNull()?.type?.sort == Type.ARRAY)
                    val isPrimitiveCopy = methodSig in primitiveCopyMethods

                    if (isArrayClone || isPrimitiveCopy) {
                        return TaintValue(res.type, isParam = false, isSafeOrigin = true, isField = false)
                    }

                    val usesTaintedArray = values?.any {
                        it is TaintValue && it.type?.sort == Type.ARRAY && (it.isField || it.isParam)
                    } == true

                    if (usesTaintedArray) {
                        val nonLeaking = (methodSig in readOnlyArrayMethods) || isPrimitiveCopy || isArrayClone
                        if (!nonLeaking) {
                            throw ArrayEscapeException(insn, "TAINTED_ARRAY_CALL:$methodSig")
                        }
                    }
                }
                return res
            }

            override fun merge(v: BasicValue, w: BasicValue): BasicValue {
                val merged = super.merge(v, w)
                if (v is TaintValue || w is TaintValue) {
                    val tv = v as? TaintValue;
                    val tw = w as? TaintValue
                    val p = (tv?.isParam == true) || (tw?.isParam == true)
                    val s = (tv?.isSafeOrigin == true) && (tw?.isSafeOrigin == true)
                    val f = (tv?.isField == true) || (tw?.isField == true)
                    return TaintValue(merged.type, p, s, f)
                }
                return merged
            }
        }

        fun verifyCustomVOImmutability(voClassName: String) {
            if (!verifiedVOMemo.add(voClassName)) return
            val voNode =
                classNodes[voClassName] ?: fail("CRITICAL: Custom VO class $voClassName not found in scan scope.")

            val isSingletonLike =
                voNode.fields.any { (it as FieldNode).name == "INSTANCE" && (it.access and Opcodes.ACC_STATIC) != 0 } ||
                        voClassName.endsWith("\$Companion")

            for (field in voNode.fields) {
                val fNode = field as FieldNode
                if (fNode.desc.startsWith("[")) {
                    if (fNode.desc !in prims) {
                        fail("CRITICAL: Only 1D primitive arrays are allowed in VO to guarantee deep immutability. Found: ${fNode.desc} in $voClassName.${fNode.name}.")
                    }
                    if ((fNode.access and Opcodes.ACC_STATIC) != 0 || isSingletonLike) {
                        fail("CRITICAL: Global-array forbidden (static/singleton/companion) in $voClassName: ${fNode.name}.")
                    }
                    if ((fNode.access and Opcodes.ACC_PRIVATE) == 0) {
                        fail("CRITICAL: Array '${fNode.name}' in VO $voClassName MUST be private.")
                    }
                } else if (!strictImmutablePrefixes.any { fNode.desc.startsWith(it) }) {
                    if (fNode.desc.startsWith("Lexecution/domain/")) {
                        verifyCustomVOImmutability(fNode.desc.removePrefix("L").removeSuffix(";"))
                    } else {
                        fail("CRITICAL: Untracked/Mutable field '${fNode.desc}' found in VO $voClassName.")
                    }
                }
            }

            for (method in voNode.methods) {
                val mNode = method as MethodNode
                val methodType = Type.getMethodType(mNode.desc)

                if (methodType.returnType.sort == Type.ARRAY) {
                    fail("CRITICAL: Array Return Leak! Method ${mNode.name} in VO $voClassName returns an array.")
                }

                for (arg in methodType.argumentTypes) {
                    if (arg.sort == Type.ARRAY && arg.descriptor !in prims) {
                        fail("CRITICAL: Forbidden array type in method signature: ${arg.descriptor} in $voClassName.${mNode.name}")
                    }
                }

                val analyzer = Analyzer(TaintInterpreter(voClassName))
                val frames = try {
                    analyzer.analyze(voClassName, mNode)
                } catch (e: ArrayEscapeException) {
                    fail(
                        "CRITICAL: Tainted Array Escape Breach! VO $voClassName leaks array to ${
                            e.message?.substringAfter(
                                ":"
                            )
                        } in method ${mNode.name}."
                    )
                } catch (e: Exception) {
                    fail("CRITICAL: Analyzer failed on $voClassName.${mNode.name}: ${e.message}")
                }

                for ((index, insn) in mNode.instructions.withIndex()) {
                    if (insn.opcode == Opcodes.ANEWARRAY || insn.opcode == Opcodes.MULTIANEWARRAY) {
                        fail("CRITICAL: Object/Multi-dimensional array allocation forbidden in VO: $voClassName.${mNode.name}")
                    }

                    if (insn.opcode == Opcodes.PUTFIELD) {
                        val fieldInsn = insn as FieldInsnNode

                        if (mNode.name == "<init>" && fieldInsn.owner != voClassName) {
                            fail("CRITICAL: Side-effect breach! VO $voClassName writes foreign field ${fieldInsn.owner}.${fieldInsn.name} in <init>.")
                        }
                        if (mNode.name != "<init>") {
                            fail("CRITICAL: Deep Immutability breach! VO $voClassName mutates reference state in ${mNode.name}.")
                        }

                        val targetField = voNode.fields.find { (it as FieldNode).name == fieldInsn.name } as? FieldNode
                        if (targetField?.desc?.startsWith("[") == true) {
                            val frame = frames[index] ?: continue
                            val valueToStore = frame.getStack(frame.stackSize - 1) as? TaintValue

                            if (valueToStore == null || !valueToStore.isSafeOrigin) {
                                fail("CRITICAL: Aliasing Leak (Fail-Closed)! VO $voClassName assigns an unsafe array reference to '${targetField.name}'. Null arrays are BANNED. Only NEWARRAY, clone(), or Arrays.copyOf() are permitted.")
                            }
                        }
                    }

                    if (mNode.name == "<init>" && insn is MethodInsnNode) {
                        val s = sig(insn.owner, insn.name, insn.desc)

                        val isThisCtor = insn.owner == voClassName && insn.name == "<init>"
                        val isSuperCtor = insn.owner == voNode.superName && insn.name == "<init>"
                        val isCtorChain = isThisCtor || isSuperCtor

                        val isExceptionInit = insn.name == "<init>" && insn.owner.endsWith("Exception")
                        val isKotlinIntrinsic = insn.owner.startsWith("kotlin/jvm/internal/Intrinsics")
                        val isAllowedBase = s == sig("java/lang/Object", "<init>", "()V")
                        val isArrayCloneCall =
                            insn.name == "clone" && insn.desc == "()Ljava/lang/Object;" && insn.owner.startsWith("[")

                        val isSelfOrSuperNonCtor =
                            (insn.owner == voClassName || insn.owner == voNode.superName) && insn.name != "<init>"
                        if (isSelfOrSuperNonCtor) {
                            fail("CRITICAL: Side-effect breach! <init> invokes self/super method (non-ctor): $s")
                        }

                        if (!isCtorChain && !isExceptionInit && !isKotlinIntrinsic && !isArrayCloneCall && s !in primitiveCopyMethods && !isAllowedBase) {
                            fail("CRITICAL: Side-effect breach! VO $voClassName invokes unverified foreign method in <init>: $s")
                        }
                    }

                    if (insn.opcode == Opcodes.ARETURN) {
                        val frame = frames[index] ?: continue
                        val retVal = frame.getStack(frame.stackSize - 1) as? TaintValue
                        if (retVal?.isField == true) {
                            fail("CRITICAL: Array Return Leak (Dataflow)! VO $voClassName returns an internal array reference in ${mNode.name}.")
                        }
                    }

                    if (insn.opcode in Opcodes.IASTORE..Opcodes.SASTORE) {
                        val frame = frames[index] ?: continue

                        val arrayRefSlot = when (insn.opcode) {
                            Opcodes.LASTORE, Opcodes.DASTORE -> frame.stackSize - 4
                            else -> frame.stackSize - 3
                        }

                        if (arrayRefSlot >= 0 && frame.stackSize > 0) {
                            val arrayRef = frame.getStack(arrayRefSlot) as? TaintValue
                            if (arrayRef?.isParam == true || arrayRef?.isField == true) {
                                fail("CRITICAL: Side-effect breach! VO $voClassName mutates external array (param-derived/foreign field) via xASTORE in ${mNode.name}.")
                            }
                        }

                        if (mNode.name != "<init>") {
                            fail("CRITICAL: Array Mutation (xASTORE) breach! VO $voClassName modifies array contents directly in ${mNode.name}.")
                        }
                    }

                    if (insn.opcode == Opcodes.PUTSTATIC && mNode.name != "<clinit>") {
                        fail("CRITICAL: Global state mutation breach! VO $voClassName modifies static state in ${mNode.name}.")
                    }
                }
            }
        }

        val implNames = implClasses.map { it.name.replace(".", "/") }.toSet()

        for (node in classNodes.values.filter { it.name in implNames }) {
            for (field in node.fields) {
                val fNode = field as FieldNode
                if (fNode.desc.startsWith("[")) {
                    fail("CRITICAL: Raw array '${fNode.desc}' found directly in ${node.name}.${fNode.name}. Arrays MUST be wrapped in a defensive VO.")
                }

                val isStdlibImmutable = strictImmutablePrefixes.any { fNode.desc.startsWith(it) }
                if (!isStdlibImmutable && fNode.desc.startsWith("Lexecution/domain/")) {
                    verifyCustomVOImmutability(fNode.desc.removePrefix("L").removeSuffix(";"))
                } else if (!isStdlibImmutable) {
                    fail("CRITICAL: Mutable field type '${fNode.desc}' in ${node.name}.${fNode.name}.")
                }

                if (fNode.signature != null && (fNode.signature.contains("Ljava/util/") || fNode.signature.contains("scala/"))) {
                    fail("CRITICAL: Generic Signature Pollution! Field ${node.name}.${fNode.name} contains a mutable collection.")
                }
            }

            for (method in node.methods) {
                val mNode = method as MethodNode
                for (insn in mNode.instructions) {
                    // This-Escape block: Disallow storing 'this' into any foreign object's field
                    if (insn.opcode == Opcodes.PUTFIELD) {
                        val fieldInsn = insn as FieldInsnNode
                        if (mNode.name == "<init>" && fieldInsn.owner != node.name) {
                            fail("CRITICAL: This-escape breach! Impl ${node.name} writes to foreign field ${fieldInsn.owner}.${fieldInsn.name} in <init>.")
                        }
                        if (mNode.name != "<init>") fail("CRITICAL: PUTFIELD in ${node.name}.${mNode.name}.")
                    }

                    if (insn.opcode == Opcodes.PUTSTATIC && mNode.name != "<clinit>") {
                        fail("CRITICAL: PUTSTATIC in ${node.name}.${mNode.name}.")
                    }

                    // Smart Whitelist for Impl constructors to allow safe data structure initialization
                    if (mNode.name == "<init>" && insn is MethodInsnNode) {
                        val s = sig(insn.owner, insn.name, insn.desc)
                        val isThisCtor = insn.owner == node.name && insn.name == "<init>"
                        val isSuperCtor = insn.owner == node.superName && insn.name == "<init>"
                        val isCtorChain = isThisCtor || isSuperCtor

                        val isExceptionInit = insn.name == "<init>" && insn.owner.endsWith("Exception")
                        val isKotlinIntrinsic = insn.owner.startsWith("kotlin/jvm/internal/Intrinsics")
                        val isAllowedBase = s == sig("java/lang/Object", "<init>", "()V")
                        val isArrayCloneCall =
                            insn.name == "clone" && insn.desc == "()Ljava/lang/Object;" && insn.owner.startsWith("[")

                        val isSafeCollectionOrUtil =
                            insn.owner.startsWith("kotlinx/collections/immutable/") ||
                                    insn.owner == "java/util/UUID" ||
                                    insn.owner == "kotlin/PreconditionsKt"

                        val isSelfOrSuperNonCtor =
                            (insn.owner == node.name || insn.owner == node.superName) && insn.name != "<init>"
                        if (isSelfOrSuperNonCtor) {
                            fail("CRITICAL: Side-effect breach! Impl <init> invokes self/super method (non-ctor): $s in ${node.name}")
                        }

                        if (!isCtorChain && !isExceptionInit && !isKotlinIntrinsic && !isArrayCloneCall && !isSafeCollectionOrUtil && s !in primitiveCopyMethods && !isAllowedBase) {
                            fail("CRITICAL: This-escape / Side-effect breach! Impl ${node.name} invokes unverified foreign method in <init>: $s")
                        }
                    }
                }
            }
        }
    }

    // --- Rule 3: Persistent Pipeline Integrity ---
    @Test
    fun `RawPayloadNode MUST NOT implement CanonicalPlanNode`() {
        noClasses().that().resideInAPackage("..payload..")
            .should().implement("execution.domain.vo.plan.CanonicalPlanNode")
            .check(coreClasses)
    }

    // --- Rule 4: Centralized Fuel Gateway ---
    @Test
    fun `finalizeSteps MUST ONLY be mutated by strict allowed methods`() {
        val sessionClassName = "execution/domain/service/planner/session/PlannerSession"
        val sessionNode = classNodes[sessionClassName]
            ?: fail("CRITICAL: PlannerSession class not found. Centralized Fuel Gateway cannot be verified.")

        val allowedMethods = setOf("<init>", "step", "resetToCleanState")
        var mutated = false

        for (method in sessionNode.methods) {
            val mNode = method as MethodNode
            val isStrictSynthetic =
                mNode.name.startsWith("access\$setFinalizeSteps") && (mNode.access and Opcodes.ACC_SYNTHETIC) != 0

            for (insn in mNode.instructions) {
                if (insn.opcode == Opcodes.PUTFIELD) {
                    val fieldInsn = insn as FieldInsnNode
                    if (fieldInsn.name == "finalizeSteps" && fieldInsn.owner == sessionClassName) {
                        mutated = true
                        if (mNode.name !in allowedMethods && !isStrictSynthetic) {
                            fail("CRITICAL: 'finalizeSteps' mutated in unauthorized method '${mNode.name}'.")
                        }
                    }
                }
            }
        }
        if (!mutated) fail("CRITICAL: No mutation to 'finalizeSteps' found.")
    }

    // --- Rule 5: Hot-path Closed-World Dispatch & Pruned Reachable SCC Detection ---
    @Test
    fun `Planner package MUST NOT allocate lambdas, suspend, and Reachable Subgraph MUST NOT contain cycles`() {
        noClasses().that().resideInAPackage("execution.domain.service.planner..")
            .should().dependOnClassesThat().resideInAPackage("kotlinx.coroutines..")
            .check(coreClasses)

        val plannerPrefix = "execution/domain/service/planner/"
        val callGraph = mutableMapOf<String, MutableSet<String>>()

        // ---- helpers ----
        data class MethodId(val owner: String, val name: String, val desc: String)

        fun parseCallerId(id: String): MethodId {
            val parts = id.split('.', limit = 3)
            if (parts.size != 3) fail("CRITICAL: Invalid method id format: '$id' (expected owner.name.desc)")
            return MethodId(parts[0], parts[1], parts[2])
        }

        fun isTrustedDomainInternal(internalName: String): Boolean =
            trustedDomainRoots.any { internalName.startsWith(it) }

        fun requireInGraphIfTrusted(owner: String) {
            if (!classNodes.containsKey(owner) && isTrustedDomainInternal(owner)) {
                fail("CRITICAL: Fail-Closed breach! Trusted domain target '$owner' missing from ASM graph. Coverage incomplete.")
            }
        }

        // --- callback ban ---
        fun isCallbackType(t: Type): Boolean {
            return when (t.sort) {
                Type.OBJECT -> {
                    val n = t.internalName
                    n.startsWith("java/util/function/") ||
                            n == "java/util/Comparator" ||
                            n == "java/lang/Runnable" ||
                            n == "java/util/concurrent/Callable" ||
                            n.startsWith("kotlin/jvm/functions/")
                }

                Type.ARRAY -> isCallbackType(t.elementType)
                else -> false
            }
        }

        // --- hidden boundary: Object / Object[] / collection interfaces (NOT blanket java/util) ---
        val hiddenBoundaryIfaces = setOf(
            "java/lang/Iterable",
            "java/util/Iterator",
            "java/util/Spliterator",
            "java/util/Collection",
            "java/util/List",
            "java/util/Set",
            "java/util/Map"
        )

        fun isHiddenBoundaryType(t: Type): Boolean {
            return when (t.sort) {
                Type.OBJECT -> {
                    val n = t.internalName
                    n == "java/lang/Object" ||
                            n in hiddenBoundaryIfaces ||
                            n.startsWith("java/util/stream/")
                }

                Type.ARRAY -> t.elementType.sort == Type.OBJECT // Object[] (and higher dims)
                else -> false
            }
        }

        // ---- (1) stdlib allowlist: owner-level narrow whitelist (stronger than prefix-open world) ----
        fun isSafeStdLibOwner(owner: String): Boolean {
            // Kotlin/JDK “wide open” 금지. 필요한 것만 최소 오픈.
            if (owner.startsWith("kotlinx/collections/immutable/")) return true
            if (owner.startsWith("kotlin/jvm/internal/")) return true
            if (owner == "kotlin/PreconditionsKt" || owner.startsWith("kotlin/PreconditionsKt__")) return true
            if (owner.startsWith("kotlin/collections/")) return true
            if (owner.startsWith("kotlin/")) return true // Kotlin stdlib (단, callback/hidden은 별도 차단)

            // JDK: java/lang & time은 허용 (순수 연산/값 객체 중심)
            if (owner.startsWith("java/lang/")) return true
            if (owner.startsWith("java/time/")) return true

            // java/util 전체 허용 금지: 필요한 클래스만 점개방
            if (owner == "java/util/Arrays") return true
            if (owner == "java/util/UUID") return true
            if (owner == "java/util/Objects") return true

            // crypto util (예: MessageDigest.update([B))
            if (owner == "java/security/MessageDigest") return true

            // 그 외는 닫음
            return false
        }

        // ---- (2) hidden-boundary 예외: 인트린식/불변 컬렉션 빌더는 제네릭 erased Object를 허용 ----
        fun isHiddenBoundaryExemptOwner(owner: String): Boolean {
            return owner.startsWith("kotlin/jvm/internal/Intrinsics") ||
                    owner.startsWith("kotlin/jvm/internal/TypeIntrinsics") ||
                    owner == "kotlin/PreconditionsKt" ||
                    owner.startsWith("kotlin/PreconditionsKt__") ||
                    owner == "java/util/Objects" ||
                    owner.startsWith("kotlinx/collections/immutable/")
        }

        // ---- (3) invokedynamic general ban: allow StringConcatFactory only ----
        fun isAllowedInvokeDynamic(insn: InvokeDynamicInsnNode): Boolean {
            val bsmOwner = insn.bsm.owner
            return bsmOwner == "java/lang/invoke/StringConcatFactory" // 현실 충돌 방지 예외
        }

        // ---- Build global call graph (fail-closed for trusted domain gaps) ----
        for (node in classNodes.values) {
            for (method in node.methods) {
                val mNode = method as MethodNode
                val callerId = sig(node.name, mNode.name, mNode.desc)
                val edges = callGraph.computeIfAbsent(callerId) { mutableSetOf() }

                for (insn in mNode.instructions) {
                    if (insn is MethodInsnNode) {
                        val owner = insn.owner
                        requireInGraphIfTrusted(owner)

                        if (classNodes.containsKey(owner)) {
                            edges.add(sig(owner, insn.name, insn.desc))
                            if (insn.opcode == Opcodes.INVOKEINTERFACE || insn.opcode == Opcodes.INVOKEVIRTUAL) {
                                val implementers = getImplementers(owner)
                                for (impl in implementers) {
                                    val actualOwner = resolveMethodOwner(impl.name, insn.name, insn.desc)
                                    edges.add(sig(actualOwner, insn.name, insn.desc))
                                }
                            }
                        }
                    }
                }
            }
        }

        // ---- Reachability from planner ----
        val reachableFromPlanner = mutableSetOf<String>()
        val bfsQueue = ArrayDeque<String>()

        callGraph.keys.filter { it.startsWith(plannerPrefix) }.forEach {
            reachableFromPlanner.add(it)
            bfsQueue.add(it)
        }

        while (bfsQueue.isNotEmpty()) {
            val curr = bfsQueue.removeFirst()
            for (adj in callGraph[curr] ?: emptySet()) {
                if (reachableFromPlanner.add(adj)) bfsQueue.add(adj)
            }
        }

        // ---- Cycle detection over reachable subgraph ----
        val visited = mutableSetOf<String>()
        val recursionStack = mutableSetOf<String>()

        fun detectCycle(nodeId: String, path: List<String>) {
            if (recursionStack.contains(nodeId)) {
                fail("CRITICAL: Recursion Cycle inside Hot-Path Subgraph! Path: ${path.joinToString(" -> ")} -> $nodeId.")
            }
            if (!visited.add(nodeId)) return

            recursionStack.add(nodeId)
            for (target in callGraph[nodeId] ?: emptySet()) {
                if (target in reachableFromPlanner) detectCycle(target, path + nodeId)
            }
            recursionStack.remove(nodeId)
        }

        for (nodeId in reachableFromPlanner) {
            if (!visited.contains(nodeId)) detectCycle(nodeId, emptyList())
        }

        // ---- Closed-world enforcement over entire reachable subgraph ----
        val reachableClassNames = reachableFromPlanner.map { parseCallerId(it).owner }.toSet()

        // Class-level bans: lambda classes / java functional iface impl
        for (className in reachableClassNames) {
            val node = classNodes[className]
                ?: fail("CRITICAL: Reachable class '$className' missing from ASM graph (Fail-Closed).")

            if (node.interfaces.any {
                    it.startsWith("java/util/function/") ||
                            it == "java/util/Comparator" ||
                            it == "java/lang/Runnable" ||
                            it == "java/util/concurrent/Callable"
                }
            ) {
                fail("CRITICAL: Java functional interface implementation found in hot-path reachable class: ${node.name}. Re-entrant callback breach.")
            }

            if (node.superName == "kotlin/jvm/internal/Lambda" ||
                node.interfaces.any { it.startsWith("kotlin/jvm/functions/Function") }
            ) {
                fail("CRITICAL: Lambda class allocation '${node.name}' found in hot-path reachable class.")
            }
        }

        // Method-level bans: suspend, invokedynamic, external calls, callback transfer, hidden boundary (args+return)
        for (callerId in reachableFromPlanner) {
            val id = parseCallerId(callerId)
            val ownerClass = id.owner
            val mName = id.name
            val mDesc = id.desc

            val node = classNodes[ownerClass]
                ?: fail("CRITICAL: Reachable class '$ownerClass' missing from ASM graph (Fail-Closed).")

            val mNode = node.methods.firstOrNull {
                (it as MethodNode).name == mName && (it as MethodNode).desc == mDesc
            } as? MethodNode ?: fail("CRITICAL: Reachable method not found: $callerId (Fail-Closed).")

            if (mNode.desc.contains("Lkotlin/coroutines/Continuation;")) {
                fail("CRITICAL: Method $callerId uses suspend/Continuation.")
            }

            for (insn in mNode.instructions) {
                if (insn is InvokeDynamicInsnNode) {
                    if (insn.bsm.owner == "java/lang/invoke/LambdaMetafactory") {
                        fail("CRITICAL: invokedynamic (LambdaMetafactory) found in $callerId.")
                    }
                    if (!isAllowedInvokeDynamic(insn)) {
                        fail("CRITICAL: invokedynamic found in $callerId with bootstrap '${insn.bsm.owner}'. Hot-path must avoid indy to prevent hidden dispatch.")
                    }
                }

                if (insn is MethodInsnNode) {
                    // 182..185 (virtual/special/static/interface) 모두 포함
                    val isInvoke = insn.opcode in Opcodes.INVOKEVIRTUAL..Opcodes.INVOKEINTERFACE
                    if (!isInvoke) continue

                    val calleeOwner = insn.owner
                    requireInGraphIfTrusted(calleeOwner)

                    val calleeInGraph = classNodes.containsKey(calleeOwner)
                    if (!calleeInGraph) {
                        // External boundary
                        if (!isSafeStdLibOwner(calleeOwner)) {
                            fail("CRITICAL: Hot-path external call breach! Method $callerId invokes foreign method ${calleeOwner}.${insn.name}. Hot-path MUST be fully closed-world.")
                        }

                        val methodType = Type.getMethodType(insn.desc)
                        val argTypes = methodType.argumentTypes
                        val retType = methodType.returnType

                        // (1) Callback transfer ban: no exceptions
                        if (argTypes.any { isCallbackType(it) }) {
                            fail("CRITICAL: Re-entrant callback breach! Method $callerId passes a functional interface to ${calleeOwner}.${insn.name}.")
                        }

                        // (2) Hidden boundary ban (args + return): except intrinsic/immutable-safe owners
                        if (!isHiddenBoundaryExemptOwner(calleeOwner)) {
                            if (argTypes.any { isHiddenBoundaryType(it) }) {
                                fail(
                                    "CRITICAL: Hidden boundary breach! Method $callerId invokes ${calleeOwner}.${insn.name} crossing boundary with Object/Object[]/Collection-like parameter. " +
                                            "This enables implicit virtual dispatch / side-effects via erased generics or foreign collections."
                                )
                            }
                            if (isHiddenBoundaryType(retType)) {
                                fail(
                                    "CRITICAL: Hidden boundary breach! Method $callerId invokes ${calleeOwner}.${insn.name} returning Object/Object[]/Collection-like type. " +
                                            "This enables opaque polymorphism and side-effects post-return."
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Rule 6: Type-State Dual Blockade ---
    @Test
    fun `CandidatePlanNode MUST NEVER be exposed, nor fraudulently cast to InternedPlanNode`() {
        val candidateType = "execution/domain/vo/plan/CandidatePlanNode"
        val internedType = "execution/domain/vo/plan/InternedPlanNode"
        val repoIface = "execution/domain/port/outgoing/PlanInternRepository"
        val factoryType = "execution/domain/vo/plan/impl/NodeFactory"

        val candidateImplementers =
            classNodes.values.filter { implementsTransitively(it.name, candidateType) }.map { it.name }.toSet()
        val internedImplementers =
            classNodes.values.filter { implementsTransitively(it.name, internedType) }.map { it.name }.toSet()
        val repoImplementers =
            classNodes.values.filter { implementsTransitively(it.name, repoIface) }.map { it.name }.toSet()

        val forbiddenExposureTypes = candidateImplementers + candidateType
        val forbiddenCastTargets = internedImplementers + internedType

        fun isAllowedClass(className: String): Boolean {
            if (className == repoIface || className == factoryType) return true
            if (repoImplementers.contains(className)) return true
            if (className.startsWith("$factoryType\$")) return true
            return false
        }

        fun isForbiddenDesc(desc: String?, signature: String?): Boolean {
            if (desc == null && signature == null) return false
            return forbiddenExposureTypes.any { forbidden ->
                val targetL = "L$forbidden;"
                (desc != null && desc.contains(targetL)) || (signature != null && signature.contains(targetL))
            }
        }

        fun isLegalCastContext(node: ClassNode, m: MethodNode): Boolean {
            val isRepoImpl = repoImplementers.contains(node.name)
            return isRepoImpl && (m.name == "intern" || m.name.startsWith("intern\$") || m.name.contains("\$intern"))
        }

        for (node in classNodes.values) {
            val allowedToHandleCandidate = isAllowedClass(node.name)

            if (!allowedToHandleCandidate) {
                for (field in node.fields) {
                    val fNode = field as FieldNode
                    if (isForbiddenDesc(
                            fNode.desc,
                            fNode.signature
                        )
                    ) fail("CRITICAL: CandidatePlanNode leaked into API field ${node.name}.${fNode.name}.")
                }
            }

            for (method in node.methods) {
                val mNode = method as MethodNode
                if (!allowedToHandleCandidate && isForbiddenDesc(mNode.desc, mNode.signature)) {
                    fail("CRITICAL: CandidatePlanNode leaked into method signature ${node.name}.${mNode.name}.")
                }

                for (insn in mNode.instructions) {
                    if (insn.opcode == Opcodes.CHECKCAST || insn.opcode == Opcodes.INSTANCEOF) {
                        val typeInsn = insn as TypeInsnNode
                        if (forbiddenCastTargets.contains(typeInsn.desc) && !isLegalCastContext(node, mNode)) {
                            fail("CRITICAL: Fraudulent Type-State upgrade! Illegal cast to ${typeInsn.desc} found in ${node.name}.${mNode.name}.")
                        }
                    }
                }
            }
        }
    }

    // --- Rule 7: Strict Monomorphic Primitive Port Enforcement ---
    @Test
    fun `Hot-path Outbound Ports MUST strictly utilize specific primitive signatures for zero-allocation performance`() {
        val targetPorts = setOf(
            "execution/domain/port/outgoing/NodeIdIndexer",
            "execution/domain/port/outgoing/CanonicalEdgeKeyProvider"
        )

        val allowedObjectTypes = setOf("execution/domain/vo/plan/CanonicalSignature")
        val allowedPrimitiveSorts = setOf(Type.INT, Type.LONG, Type.BOOLEAN, Type.VOID)

        for (portName in targetPorts) {
            val portNode = classNodes[portName]
                ?: fail("CRITICAL: Hot-path port $portName not found. Cannot verify monomorphic signatures.")

            var validMethodsChecked = 0

            for (method in portNode.methods) {
                val mNode = method as MethodNode

                if (mNode.name == "<init>" || mNode.name == "<clinit>") continue
                if ((mNode.access and Opcodes.ACC_SYNTHETIC) != 0 || (mNode.access and Opcodes.ACC_BRIDGE) != 0) continue

                validMethodsChecked++
                val methodType = Type.getMethodType(mNode.desc)
                val typesToCheck = methodType.argumentTypes.toMutableList()
                typesToCheck.add(methodType.returnType)

                for (type in typesToCheck) {
                    if (type.sort == Type.ARRAY) {
                        fail("CRITICAL: Array allocation breach in hot-path port $portName.${mNode.name}. Descriptor: ${mNode.desc}")
                    } else if (type.sort == Type.OBJECT) {
                        if (type.internalName !in allowedObjectTypes) {
                            fail("CRITICAL: Monomorphic breach! Method ${mNode.name} in $portName uses boxed/forbidden object type '${type.internalName}'.")
                        }
                    } else if (type.sort !in allowedPrimitiveSorts) {
                        fail("CRITICAL: Primitive strictness breach! Method ${mNode.name} in $portName uses sub-optimal primitive sort ${type.sort}. Only I, J, Z are permitted.")
                    }
                }
            }

            if (validMethodsChecked == 0) {
                fail("CRITICAL: No concrete contract methods found in port $portName. Vacuous truth prevented.")
            }
        }
    }
}
package execution.adapter.mockito

import execution.domain.service.generation.FixtureGenerator
import execution.domain.vo.context.generation.GenerationRequest
import execution.domain.vo.trace.ExecutionTrace
import execution.port.outgoing.MockingContext
import execution.port.outgoing.ScenarioTrace
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.invocation.InvocationOnMock
import java.util.Optional

class MockitoEngineAdapterTest {

    private val generator = mockk<FixtureGenerator>(relaxed = true)
    private val trace = mockk<ScenarioTrace>(relaxed = true)
    private val context = mockk<MockingContext>()
    private val adapter = MockitoEngineAdapter()

    @BeforeEach
    fun setup() {
        every { context.generator } returns generator
        every { context.trace } returns trace
    }

    data class TestEntity(val id: String?, val name: String)
    data class NoIdEntity(val name: String)

    open class ConcreteRepository {
        open fun doSomething() {}
    }

    interface TestService {
        fun getData(): String
        fun doSomething(arg: Int)
        fun saveItem(item: String): String
        fun insertItem(item: String): String
        fun updateItem(item: String): String
        fun deleteItem(item: String): String
        fun removeItem(item: String): String
        fun storeData(data: String): String
    }

    interface TestRepository {
        fun save(entity: TestEntity): TestEntity
        fun findById(id: String): TestEntity?
        fun findOptionalById(id: String): Optional<TestEntity>
        fun findAll(): List<TestEntity>
        fun delete(entity: TestEntity)
        fun count(): Long
        fun customQuery(name: String): String
        fun createEntity(entity: TestEntity): TestEntity
        fun registerUser(entity: TestEntity): TestEntity
        fun getById(id: String): TestEntity?
        fun getAccountById(id: String): TestEntity?
        fun listItems(): List<TestEntity>
        fun getAllUsers(): List<TestEntity>
        fun removeUser(entity: TestEntity)
        fun deleteAccount(entity: TestEntity)
        fun countUsers(): Long
        fun findingNemo(name: String): String
        fun saviour(name: String): String
        fun calculator(): Int
    }

    interface EdgeCaseRepository {
        fun findById(): TestEntity?
        fun findById(id: String, v: Int): TestEntity?
        fun save(): TestEntity?
        fun delete()
        fun nonCrudAction()
        fun saveGeneric(obj: Any): Any
        fun deleteGeneric(obj: Any)
        fun findGeneric(id: String): Any?
        fun count(): Long
    }

    @Test
    fun `Stateless - delegates value generation to FixtureGenerator`() {
        val expectedValue = "Generated String"
        val requestSlot = slot<GenerationRequest>()

        every { generator.generate(capture(requestSlot)) } returns expectedValue

        val mock = adapter.createMock(TestService::class, context)
        val result = mock.getData()

        assertThat(result).isEqualTo(expectedValue)
        assertThat(requestSlot.captured.type.classifier).isEqualTo(String::class)

        verify(exactly = 1) { trace.add(any<ExecutionTrace>()) }
    }

    @Test
    fun `Stateless - returns null for Unit methods`() {
        val mock = adapter.createMock(TestService::class, context)

        mock.doSomething(99)

        verify(exactly = 0) { generator.generate(any<GenerationRequest>()) }
        verify(exactly = 1) { trace.add(any<ExecutionTrace>()) }
    }

    @Test
    fun `Stateless - logs warning and proceeds for ALL suspicious keywords`() {
        val mock = adapter.createMock(TestService::class, context)
        every { generator.generate(any<GenerationRequest>()) } returns "OK"

        mock.saveItem("A")
        mock.insertItem("B")
        mock.updateItem("C")
        mock.deleteItem("D")
        mock.removeItem("E")
        mock.storeData("F")

        verify(exactly = 6) { generator.generate(any<GenerationRequest>()) }
    }

    @Test
    fun `Stateless - handles trace failure gracefully`() {
        val mock = adapter.createMock(TestService::class, context)

        every { trace.add(any<ExecutionTrace>()) } throws RuntimeException("Trace DB Full")
        every { generator.generate(any<GenerationRequest>()) } returns "Recovered"

        val result = mock.getData()

        assertThat(result).isEqualTo("Recovered")
        verify(exactly = 1) { trace.add(any<ExecutionTrace>()) }
    }

    @Test
    fun `Stateful - handles Object methods`() {
        val fake = adapter.createFake(TestRepository::class, context)

        assertThat(fake.toString()).startsWith("StatefulFake$")
        assertThat(fake.hashCode()).isNotZero()
        assertThat(fake.equals(fake)).isTrue()
        assertThat(fake.equals(null)).isFalse()
        assertThat(fake.equals(Any())).isFalse()

        verify(exactly = 0) { generator.generate(any<GenerationRequest>()) }
    }

    @Test
    fun `Stateful - handles standard Save, FindById, FindAll, Delete`() {
        val fake = adapter.createFake(TestRepository::class, context)
        val entity = TestEntity(id = "user-1", name = "Alice")

        fake.save(entity)
        val result = fake.findById("user-1")
        val all = fake.findAll()
        fake.delete(entity)

        assertThat(result).isEqualTo(entity)
        assertThat(all).contains(entity)
        assertThat(fake.count()).isEqualTo(0)
    }

    @Test
    fun `Stateful - handles auto-generation of ID for missing ID field`() {
        val fake = adapter.createFake(EdgeCaseRepository::class, context)
        val noIdEntity = NoIdEntity(name = "AutoID")

        fake.saveGeneric(noIdEntity)
        assertThat(fake.count()).isEqualTo(1)
    }

    @Test
    fun `Stateful - handles Optional return type`() {
        val fake = adapter.createFake(TestRepository::class, context)
        val entity = TestEntity(id = "opt-1", name = "Optional")

        fake.save(entity)

        assertThat(fake.findOptionalById("opt-1")).isPresent
        assertThat(fake.findOptionalById("404")).isEmpty
    }

    @Test
    fun `Stateful - falls back to Generator for non-CRUD methods`() {
        val fake = adapter.createFake(TestRepository::class, context)
        every { generator.generate(any<GenerationRequest>()) } returns "Custom Result"

        val result = fake.customQuery("param")

        assertThat(result).isEqualTo("Custom Result")
    }

    @Test
    fun `Matchers - isSave matches create and register`() {
        val fake = adapter.createFake(TestRepository::class, context)
        fake.createEntity(TestEntity("1", "Create"))
        fake.registerUser(TestEntity("2", "Register"))
        assertThat(fake.countUsers()).isEqualTo(2)
    }

    @Test
    fun `Matchers - handleSave falls back to generator if args are empty`() {
        val fake = adapter.createFake(EdgeCaseRepository::class, context)
        every { generator.generate(any<GenerationRequest>()) } returns null
        fake.save()
        verify(exactly = 1) { generator.generate(any<GenerationRequest>()) }
    }

    @Test
    fun `Matchers - isFindById matches getById patterns`() {
        val fake = adapter.createFake(TestRepository::class, context)
        val entity = TestEntity("find-1", "Target")
        fake.save(entity)
        assertThat(fake.getById("find-1")).isEqualTo(entity)
        assertThat(fake.getAccountById("find-1")).isEqualTo(entity)
    }

    @Test
    fun `Matchers - isFindById ignores wrong argument counts`() {
        val fake = adapter.createFake(EdgeCaseRepository::class, context)
        every { generator.generate(any<GenerationRequest>()) } returns null
        fake.findById()
        fake.findById("1", 99)
        verify(exactly = 2) { generator.generate(any<GenerationRequest>()) }
    }

    @Test
    fun `Matchers - isFindAll matches list and getAll`() {
        val fake = adapter.createFake(TestRepository::class, context)
        fake.save(TestEntity("1", "A"))
        assertThat(fake.listItems()).hasSize(1)
        assertThat(fake.getAllUsers()).hasSize(1)
    }

    @Test
    fun `Matchers - isDelete matches delete and remove`() {
        val fake = adapter.createFake(TestRepository::class, context)
        val e1 = TestEntity("d-1", "DeleteMe")
        val e2 = TestEntity("r-1", "RemoveMe")
        fake.save(e1); fake.save(e2)
        fake.deleteAccount(e1); fake.removeUser(e2)
        assertThat(fake.count()).isEqualTo(0)
    }

    @Test
    fun `Matchers - handleDelete accepts empty args`() {
        val fake = adapter.createFake(EdgeCaseRepository::class, context)
        fake.delete()
        verify(exactly = 0) { generator.generate(any<GenerationRequest>()) }
    }

    @Test
    fun `Matchers - handleDelete handles objects without id property`() {
        val fake = adapter.createFake(EdgeCaseRepository::class, context)
        val entity = TestEntity(id = "manual-id", name = "Test")
        fake.saveGeneric(entity)
        fake.deleteGeneric("manual-id")
        assertThat(fake.count()).isEqualTo(0)
    }

    @Test
    fun `Fallback - strictly checks names and sends mismatches to Generator`() {
        val fake = adapter.createFake(TestRepository::class, context)
        
        every { generator.generate(any<GenerationRequest>()) } returnsMany listOf("Fallback", "Fallback", 42)

        fake.findingNemo("Dory")
        fake.saviour("World")
        fake.calculator()

        verify(exactly = 3) { trace.add(any<ExecutionTrace>()) }
    }

    @Test
    fun `Fallback - optimizations skip generator for Unit return types`() {
        val fake = adapter.createFake(EdgeCaseRepository::class, context)
        fake.nonCrudAction()

        verify(exactly = 0) { generator.generate(any<GenerationRequest>()) }
        verify(exactly = 1) { trace.add(any<ExecutionTrace>()) }
    }

    @Test
    fun `EdgeCase - handles concrete class mocking`() {
        val concreteFake = adapter.createFake(ConcreteRepository::class, context)
        concreteFake.doSomething()
        verify {
            trace.add(match<ExecutionTrace> {
                it.methodSignature.contains("Mock") || it.methodSignature.contains("ConcreteRepository")
            })
        }
    }

    @Test
    fun `EdgeCase - handles entities throwing exceptions during property access`() {
        val fake = adapter.createFake(EdgeCaseRepository::class, context)
        val poisonEntity = object {
            @Suppress("unused")
            val id: String
                get() = throw IllegalStateException("Explosive Property")
        }
        fake.saveGeneric(poisonEntity)
        assertThat(fake.count()).isEqualTo(1)
    }

    @Test
    fun `EdgeCase - handleFindById ignores entities with inaccessible IDs`() {
        val fake = adapter.createFake(EdgeCaseRepository::class, context)
        every { generator.generate(any<GenerationRequest>()) } returns null

        val poisonEntity = object {
            @Suppress("unused")
            val id: String
                get() = throw RuntimeException("Inaccessible")
        }

        fake.saveGeneric(poisonEntity)
        val result = fake.findGeneric("target-id")
        assertThat(result).isNull()
    }

    @Test
    fun `Direct - GenerativeAnswer handles Unit return type explicitly`() {
        val answerInstance = createPrivateInnerClassInstance(
            "GenerativeAnswer",
            generator,
            TestService::class,
            io.github.oshai.kotlinlogging.KotlinLogging.logger {},
            trace
        ) as org.mockito.stubbing.Answer<*>

        val realMethod = TestService::class.java.methods.find { it.name == "doSomething" }!!

        val invocation = mockk<InvocationOnMock>()
        every { invocation.method } returns realMethod
        every { invocation.arguments } returns arrayOf(1)
        every { invocation.mock } returns mockk<TestService>()

        val result = answerInstance.answer(invocation)

        assertThat(result).isNull()

        verify { trace.add(any<ExecutionTrace>()) }
        verify(exactly = 0) { generator.generate(any<GenerationRequest>()) }
    }

    @Test
    fun `Direct - StatefulAnswer falls back to Mock name when interfaces are empty`() {
        val answerInstance = createPrivateInnerClassInstance(
            "StatefulOrGenerativeAnswer",
            generator,
            trace
        ) as org.mockito.stubbing.Answer<*>

        val rawObject = Any()

        val invocation = mockk<InvocationOnMock>()
        every { invocation.method } returns TestRepository::class.java.methods.find { it.name == "count" }!!
        every { invocation.arguments } returns emptyArray()
        every { invocation.mock } returns rawObject

        answerInstance.answer(invocation)

        val slot = slot<ExecutionTrace>()
        verify { trace.add(capture(slot)) }

        assertThat(slot.captured.methodSignature).matches {
            it.startsWith("Mock.count") || it.startsWith("Any.count")
        }
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `Direct - handleFindById handles entities completely missing an ID property`() {
        val answerInstance = createPrivateInnerClassInstance(
            "StatefulOrGenerativeAnswer",
            generator,
            trace
        ) as org.mockito.stubbing.Answer<*>

        val storeField = answerInstance::class.java.getDeclaredField("store")
        storeField.isAccessible = true
        val store = storeField.get(answerInstance) as MutableMap<Any, Any>

        val noIdObj = NoIdEntity("Ghost")
        store["some-key"] = noIdObj

        val invocation = mockk<InvocationOnMock>()
        every { invocation.method } returns TestRepository::class.java.methods.find { it.name == "findById" }!!
        every { invocation.arguments } returns arrayOf("target-id")
        every { invocation.mock } returns mockk<TestRepository>()

        val result = answerInstance.answer(invocation)

        assertThat(result).isNull()
    }

    @Test
    fun `Direct - GenerativeAnswer handles null arguments array`() {
        val answerInstance = createPrivateInnerClassInstance(
            "GenerativeAnswer",
            generator,
            TestService::class,
            io.github.oshai.kotlinlogging.KotlinLogging.logger {},
            trace
        ) as org.mockito.stubbing.Answer<*>

        val invocation = mockk<InvocationOnMock>()
        every { invocation.arguments } returns null
        every { invocation.method } returns TestService::class.java.methods.first { it.name == "getData" }
        every { invocation.mock } returns mockk<TestService>()

        every { generator.generate(any<GenerationRequest>()) } returns "Safe"

        val result = answerInstance.answer(invocation)

        assertThat(result).isEqualTo("Safe")
    }

    private fun createPrivateInnerClassInstance(className: String, vararg args: Any?): Any {
        val clazz = MockitoEngineAdapter::class.java.declaredClasses
            .firstOrNull { it.simpleName == className }
            ?: throw IllegalArgumentException("Inner class $className not found in MockitoEngineAdapter")

        val ctor = clazz.declaredConstructors.first()
        ctor.isAccessible = true
        return ctor.newInstance(*args)
    }
}
package execution.domain.strategy.generation

import execution.domain.vo.context.generation.GenerationContext
import kotlin.math.max
import kotlin.math.min

/**
 * Standard Generator for String values.
 *
 * ### ⚠️ Boundary Philosophy: Logical vs Physical
 * 1. **Logical Boundary**: [min, max] range defined by contract.
 * 2. **Physical Boundary**: [PHYSICAL_LIMIT] (10,000) imposed by engine.
 */
class StringGenerator(
    min: Int = 0,
    max: Int = Int.MAX_VALUE
) : Generator<String> {
    private val min: Int
    private val max: Int
    private val PHYSICAL_LIMIT = 10_000
    private val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

    init {
        val safeMin = max(0, min)
        val safeMax = max(0, max)
        require(safeMin <= safeMax) { "Invalid String range: $min > $max" }
        this.min = safeMin
        this.max = safeMax
    }

    override fun generate(context: GenerationContext): String {
        val random = context.seededRandom
        val logicalLen = if (random.nextDouble() < 0.1) {
            if (random.nextBoolean()) min else max
        } else {
            GeneratorUtils.nextIntInclusive(random, min, max)
        }
        val safeLen = GeneratorUtils.clampLength(logicalLen, PHYSICAL_LIMIT)
        return (1..safeLen).map { charset[random.nextInt(charset.length)] }.joinToString("")
    }

    override fun generateEdgeCases(context: GenerationContext): List<String> = buildList {
        // 1. Min Boundary & Neighbor (Off-by-one)
        if (min <= PHYSICAL_LIMIT) {
            add("a".repeat(min))
            // min + 1 (To catch < vs <= errors)
            if (min < Int.MAX_VALUE && (min + 1) <= max && (min + 1) <= PHYSICAL_LIMIT) {
                add("a".repeat(min + 1))
            }
        }

        // 2. Max Boundary & Neighbor (Off-by-one)
        if (max <= PHYSICAL_LIMIT) {
            if (max != min) add("a".repeat(max))
            // max - 1 (To catch > vs >= errors)
            if (max > Int.MIN_VALUE && (max - 1) >= min) {
                add("a".repeat(max - 1))
            }
        }

        // 3. Empty (Structural Edge)
        if (min == 0) add("")

        // 4. Physical Limit (Engine Boundary)
        if (PHYSICAL_LIMIT in min..max && PHYSICAL_LIMIT != min && PHYSICAL_LIMIT != max) {
            add("a".repeat(PHYSICAL_LIMIT))
        }
    }.distinct()

    override fun generateInvalid(context: GenerationContext): List<Any?> = buildList {
        if (min > 0) add("a".repeat(min - 1))
        if (max < PHYSICAL_LIMIT) add("a".repeat(max + 1))

        // Semantic Invalid (Often associated with @NotBlank)
        // Strictly speaking, this is not a length violation, but a common semantic expectation.
        if (min > 0) add("   ")
    }
}

/**
 * Generator for URL strings.
 *
 * ### ⚠️ RFC Non-Compliant (Structural Mimicry)
 * This generator produces strings that mimic URL structure but are **NOT** RFC 3986 compliant.
 * Use strictly for structural fuzzing.
 */
class URLGenerator(
    private val protocols: List<String> = listOf("http", "https"),
    private val host: String = "example.com",
    private val fixedPort: Int = -1
) : Generator<String> {

    init {
        require(protocols.isNotEmpty()) { "At least one protocol must be allowed." }
        if (fixedPort != -1) require(fixedPort in 0..65535) { "Port out of range: $fixedPort" }
    }

    override fun generate(context: GenerationContext): String {
        val random = context.seededRandom
        val protocol = protocols.random(random)
        val portStr = when {
            fixedPort != -1 -> ":$fixedPort"
            random.nextDouble() < 0.8 -> ""
            else -> ":${random.nextInt(1024, 65536)}"
        }
        val pathDepth = random.nextInt(0, 4)
        val path = if (pathDepth == 0) "" else "/" + (1..pathDepth).joinToString("/") { genSegment(random) }
        return "$protocol://$host$portStr$path"
    }

    override fun generateEdgeCases(context: GenerationContext): List<String> = buildList {
        val proto = protocols.first()
        add("$proto://$host")
        add("$proto://$host/")
        add("$proto://$host//foo")
        add("$proto://$host/foo/bar?query=1")
        if (fixedPort == -1) {
            add("$proto://$host:80")
            add("$proto://$host:65535")
        }
    }.distinct()

    override fun generateInvalid(context: GenerationContext): List<Any?> = buildList {
        val proto = protocols.first()
        val known = listOf("http", "https", "ftp", "file", "ws", "wss")
        val forbidden = known.firstOrNull { it !in protocols } ?: "1nv4l1d-proto"

        add("$forbidden://$host") // Protocol Violation
        add("://$host") // Missing Scheme
        add("$proto://") // Missing Host

        if (fixedPort == -1) {
            add("$proto://$host:65536")
            add("$proto://$host:-1")
        }
    }

    private fun genSegment(r: kotlin.random.Random): String {
        val chars = "abc123-_";
        val len = r.nextInt(1, 8)
        return (1..len).map { chars[r.nextInt(chars.length)] }.joinToString("")
    }
}

/**
 * Generator for Email addresses.
 *
 * ### ⚠️ Heuristic Implementation
 * Generates **Structurally Diverse** email addresses to test parsing robustness.
 * Follows an **Application-oriented** approach (omits obscure RFC valid cases like IP domains).
 */
class EmailGenerator(
    private val allowedDomains: List<String> = emptyList(),
    private val blockedDomains: List<String> = emptyList(),
    private val maxTotalLength: Int = Int.MAX_VALUE
) : Generator<String> {

    private val MAX_LOCAL_PART = 64

    override fun generate(context: GenerationContext): String {
        val random = context.seededRandom
        val domainCandidates = if (allowedDomains.isNotEmpty()) allowedDomains else listOf("com", "net", "org", "io")
        var domain = "test." + domainCandidates.random(random)

        if (random.nextDouble() < 0.1) domain = "sub.$domain"
        if (blockedDomains.any { domain.endsWith(it) }) domain = "example.com"

        val remainingTotal = maxTotalLength - 1 - domain.length
        if (remainingTotal < 1) return "a@b.c"

        val maxLocal = min(remainingTotal, MAX_LOCAL_PART)
        val strategyRoll = random.nextDouble()
        val account = when {
            strategyRoll < 0.6 -> generateSimpleAccount(random, maxLocal)
            strategyRoll < 0.8 -> generateDottedAccount(random, maxLocal)
            else -> generatePlusAccount(random, maxLocal)
        }
        return "$account@$domain"
    }

    override fun generateEdgeCases(context: GenerationContext): List<String> = buildList {
        add("a@b.c")

        // Max Local-Part Boundary (64 chars)
        if (maxTotalLength >= 70) {
            val maxLocal = "a".repeat(MAX_LOCAL_PART)
            add("$maxLocal@example.com")
        }

        // Plus Addressing & Dotted
        add("user+tag@example.com")
        add("first.last@example.com")

        // Note: IP Domain Literal (user@[127.0.0.1]) excluded (Application-oriented decision)
    }.distinct()

    override fun generateInvalid(context: GenerationContext): List<Any?> = listOf(
        "plain-string", "user@", "@domain.com", "user name@domain.com",
        "user@domain..com", ".user@domain.com", ""
    )

    private fun generateSimpleAccount(r: kotlin.random.Random, maxLen: Int): String {
        val len = r.nextInt(1, min(10, maxLen) + 1)
        return (1..len).map { "abcdefghijklmnopqrstuvwxyz"[r.nextInt(26)] }.joinToString("")
    }

    private fun generateDottedAccount(r: kotlin.random.Random, maxLen: Int): String {
        if (maxLen < 3) return "a.b"
        val part1 = generateSimpleAccount(r, maxLen / 2)
        val part2 = generateSimpleAccount(r, maxLen - part1.length - 1)
        return "$part1.$part2"
    }

    private fun generatePlusAccount(r: kotlin.random.Random, maxLen: Int): String {
        if (maxLen < 3) return "a+b"
        val part1 = generateSimpleAccount(r, maxLen / 2)
        val part2 = generateSimpleAccount(r, maxLen - part1.length - 1)
        return "$part1+$part2"
    }
}

class UUIDGenerator : Generator<java.util.UUID> {
    override fun generate(context: GenerationContext) =
        java.util.UUID(context.seededRandom.nextLong(), context.seededRandom.nextLong())
}

/**
 * [Stub] Simple heuristic regex generator for MVP.
 */
class PatternGenerator(private val regex: String) : Generator<String> {
    override fun generate(context: GenerationContext): String {
        val r = context.seededRandom
        return when {
            regex.contains("\\d") -> r.nextInt(0, 1000).toString()
            regex.contains("\\w") -> "PatternMatch"
            else -> "FixedPattern"
        }
    }
}
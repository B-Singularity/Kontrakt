package execution.domain.generator

import discovery.api.Email
import discovery.api.NotBlank
import discovery.api.Pattern
import discovery.api.StringLength
import discovery.api.Url
import discovery.api.Uuid
import java.util.UUID
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import kotlin.reflect.KClass

class StringTypeGenerator : TerminalGenerator {

    override fun supports(request: GenerationRequest): Boolean {
        val type = request.type.classifier as? KClass<*>
        return type == String::class
    }

    override fun generateValidBoundaries(
        request: GenerationRequest,
        context: GenerationContext
    ): List<Any?> = buildList {
        val (minLen, maxLen) = calculateEffectiveLength(request)

        if (minLen > 0) add("a".repeat(minLen))
        if (maxLen < 1000) add("a".repeat(maxLen))

        if (request.has<NotBlank>() && minLen <= 1 && !contains("a")) {
            add("a")
        }
        if (request.has<Email>()) add("test@example.com")
        if (request.has<Uuid>()) add(generateDeterministicUuid(context.seededRandom))
    }

    override fun generate(
        request: GenerationRequest,
        context: GenerationContext
    ): Any {
        val random = context.seededRandom
        val length = request.find<StringLength>()
        val maxLen = length?.max ?: Int.MAX_VALUE
        val urlAnno = request.find<Url>()

        return when {
            request.has<Email>() -> generateComplexEmail(maxLen, request.find<Email>()!!, random)
            request.has<Uuid>() -> generateDeterministicUuid(random)
            urlAnno != null -> generateComplexUrl(maxLen, urlAnno, random)
            request.has<Pattern>() -> generateFromRegex(request.find<Pattern>()!!.regexp, random)
            else -> generateStringConstraint(request, random)
        }
    }

    override fun generateInvalid(
        request: GenerationRequest,
        context: GenerationContext
    ): List<Any?> = buildList {
        val (min, max) = calculateEffectiveLength(request)

        if (min > 0) add("x".repeat(min - 1))
        if (max < Int.MAX_VALUE - 100) add("x".repeat(max + 1))

        if (request.has<NotBlank>()) {
            add("")
            add("   ")
        }
        if (request.has<Email>()) {
            add("not-an-email")
            add("@domain.com")
        }
    }

    private fun calculateEffectiveLength(request: GenerationRequest): Pair<Int, Int> {
        var minLen = 0
        var maxLen = Int.MAX_VALUE

        request.find<StringLength>()?.let {
            minLen = max(minLen, it.min)
            maxLen = min(maxLen, it.max)
        }
        if (request.has<NotBlank>()) {
            minLen = max(minLen, 1)
        }
        if (maxLen == Int.MAX_VALUE) maxLen = minLen + GeneratorUtils.DEFAULT_STRING_BUFFER

        if (minLen > maxLen) return minLen to minLen

        return minLen to maxLen
    }

    private fun generateStringConstraint(request: GenerationRequest, random: Random): String {
        val length = request.find<StringLength>()
        if (length != null) {
            val effectiveMax =
                if (length.max == Int.MAX_VALUE) length.min + GeneratorUtils.DEFAULT_STRING_BUFFER else length.max
            return GeneratorUtils.generateRandomString(length.min, effectiveMax, random)
        }
        if (request.has<NotBlank>()) {
            return GeneratorUtils.generateRandomString(1, GeneratorUtils.NOT_BLANK_MAX_LENGTH, random)
        }
        return GeneratorUtils.generateRandomString(
            GeneratorUtils.DEFAULT_STRING_MIN_LENGTH,
            GeneratorUtils.DEFAULT_STRING_MAX_LENGTH,
            random
        )
    }


    private fun generateFromRegex(regex: String, random: Random): String =
        when (regex) {
            "\\d+", "[0-9]+" -> GeneratorUtils.generateRandomNumericString(5, random)
            "\\w+", "[a-zA-Z]+" -> GeneratorUtils.generateRandomString(5, 10, random)
            "^[A-Z]+$" -> GeneratorUtils.generateRandomStringFromCharRange('A'..'Z', random)
            "^[a-z]+$" -> GeneratorUtils.generateRandomStringFromCharRange('a'..'z', random)
            else -> "Pattern_Placeholder_for_$regex"
        }

    private fun generateComplexEmail(limit: Int, rule: Email, random: Random): String {
        val availableDomains =
            if (rule.allow.isNotEmpty()) rule.allow.toList() else listOf("com", "net", "org", "io", "co.kr", "gov")
        val suffix = availableDomains.random(random)
        val domainPart =
            if (suffix.contains(".")) suffix else "${GeneratorUtils.generateRandomString(3, 5, random)}.$suffix"

        val overhead = 1 + domainPart.length
        val maxAccountLen = limit - overhead
        if (maxAccountLen < 1) return "a@$domainPart"

        val effectiveMax = maxAccountLen.coerceAtMost(20)
        val accountLen = if (effectiveMax <= 1) 1 else random.nextInt(1, effectiveMax + 1)
        val account = GeneratorUtils.generateRandomString(accountLen, accountLen, random)
        return "$account@$domainPart"
    }

    private fun generateComplexUrl(limit: Int, rule: Url, random: Random): String {
        val scheme = (if (rule.protocol.isNotEmpty()) rule.protocol.random(random) else "http") + "://"
        val overhead = scheme.length
        val remaining = limit - overhead

        if (remaining < 5) return "${scheme}a.co"

        val host = if (rule.hostAllow.isNotEmpty()) rule.hostAllow.random(random) else generateHost(rule, random)
        val effectiveHost = if (host.length > remaining) {
            if (remaining >= 6) "a.com" else host.take(remaining)
        } else {
            host
        }

        var currentUrl = "$scheme$effectiveHost"

        if (currentUrl.length + 3 < limit) {
            val path = generatePath(random)
            if (currentUrl.length + path.length < limit) {
                currentUrl += path
            }
        }

        if (currentUrl.length + 5 < limit) {
            val query = generateQueryParam(random)
            if (query.isNotEmpty() && currentUrl.length + query.length <= limit) {
                currentUrl += query
            }
        }

        return currentUrl
    }

    private fun generateHost(rule: Url, random: Random): String {
        val subDomains = listOf("www", "api", "")
        val sub = subDomains.random(random).let { if (it.isBlank()) "" else "$it." }
        val domain = GeneratorUtils.generateRandomString(3, 10, random).lowercase()
        val suffix = listOf("com", "net").random(random)
        val gen = "$sub$domain.$suffix"
        return if (rule.hostBlock.any { gen.contains(it) }) "example.com" else gen
    }

    private fun generatePath(random: Random): String {
        val depth = random.nextInt(3)
        if (depth == 0) return ""
        return (1..depth).joinToString(prefix = "/", separator = "/") {
            GeneratorUtils.generateRandomString(3, 6, random).lowercase()
        }
    }

    private fun generateQueryParam(random: Random): String {
        if (random.nextBoolean()) return ""
        return "?key=" + GeneratorUtils.generateRandomString(3, 5, random)
    }

    private fun generateDeterministicUuid(random: Random): String {
        return UUID(random.nextLong(), random.nextLong()).toString()
    }

}
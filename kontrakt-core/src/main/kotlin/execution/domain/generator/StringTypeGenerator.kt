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
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

class StringTypeGenerator : TypeGenerator {

    override fun supports(param: KParameter): Boolean {
        val type = param.type.classifier as? KClass<*>
        return type == String::class
    }

    override fun generateValidBoundaries(param: KParameter): List<Any?> = buildList {
        val (minLen, maxLen) = calculateEffectiveLength(param)

        if (minLen > 0) add("a".repeat(minLen))
        if (maxLen < 1000) add("a".repeat(maxLen))

        if (param.has<NotBlank>()) add("a")
        if (param.has<Email>()) add("test@example.com")
        if (param.has<Uuid>()) add(UUID.randomUUID().toString())
    }

    override fun generate(param: KParameter): Any? {
        val length = param.find<StringLength>()
        val maxLen = length?.max ?: Int.MAX_VALUE
        val urlAnno = param.find<Url>()

        return when {
            param.has<Email>() -> generateComplexEmail(maxLen, param.find<Email>()!!)
            param.has<Uuid>() -> UUID.randomUUID().toString()
            urlAnno != null -> generateComplexUrl(maxLen, urlAnno)
            param.has<Pattern>() -> generateFromRegex(param.find<Pattern>()!!.regexp)
            else -> generateStringConstraint(param)
        }
    }

    override fun generateInvalid(param: KParameter): List<Any?> = buildList {
        val (min, max) = calculateEffectiveLength(param)

        if (min > 0) add("x".repeat(min - 1))
        if (max < Int.MAX_VALUE - 100) add("x".repeat(max + 1))

        if (param.has<NotBlank>()) {
            add("")
            add("   ")
        }
        if (param.has<Email>()) {
            add("not-an-email")
            add("@domain.com")
        }
    }

    private fun calculateEffectiveLength(param: KParameter): Pair<Int, Int> {
        var minLen = 0
        var maxLen = Int.MAX_VALUE

        param.find<StringLength>()?.let {
            minLen = max(minLen, it.min)
            maxLen = min(maxLen, it.max)
        }
        if (param.has<NotBlank>()) {
            minLen = max(minLen, 1)
        }
        if (maxLen == Int.MAX_VALUE) maxLen = minLen + GeneratorUtils.DEFAULT_STRING_BUFFER
        return minLen to maxLen
    }

    private fun generateStringConstraint(param: KParameter): String? {
        val length = param.find<StringLength>()
        if (length != null) {
            val effectiveMax =
                if (length.max == Int.MAX_VALUE) length.min + GeneratorUtils.DEFAULT_STRING_BUFFER else length.max
            return GeneratorUtils.generateRandomString(length.min, effectiveMax)
        }
        if (param.has<NotBlank>()) {
            return GeneratorUtils.generateRandomString(1, GeneratorUtils.NOT_BLANK_MAX_LENGTH)
        }
        return GeneratorUtils.generateRandomString(
            GeneratorUtils.DEFAULT_STRING_MIN_LENGTH,
            GeneratorUtils.DEFAULT_STRING_MAX_LENGTH
        )
    }


    private fun generateFromRegex(regex: String): String =
        when {
            regex == "\\d+" || regex == "[0-9]+" -> GeneratorUtils.generateRandomNumericString(5)
            regex == "\\w+" || regex == "[a-zA-Z]+" -> GeneratorUtils.generateRandomString(5, 10)
            regex == "^[A-Z]+$" -> GeneratorUtils.generateRandomString(5, 10).uppercase()
            regex == "^[a-z]+$" -> GeneratorUtils.generateRandomString(5, 10).lowercase()
            else -> "Pattern_Placeholder_for_$regex"
        }

    private fun generateComplexEmail(limit: Int, rule: Email): String {
        val availableDomains =
            if (rule.allow.isNotEmpty()) rule.allow.toList() else listOf("com", "net", "org", "io", "co.kr", "gov")
        val suffix = availableDomains.random()
        val domainPart = if (suffix.contains(".")) suffix else "${GeneratorUtils.generateRandomString(3, 5)}.$suffix"

        val overhead = 1 + domainPart.length
        val maxAccountLen = limit - overhead
        if (maxAccountLen < 1) return "a@$domainPart"

        val effectiveMax = maxAccountLen.coerceAtMost(20)
        val accountLen = if (effectiveMax <= 1) 1 else Random.nextInt(1, effectiveMax + 1)
        val account = GeneratorUtils.generateRandomString(accountLen, accountLen)
        return "$account@$domainPart"
    }

    private fun generateComplexUrl(limit: Int, rule: Url): String {
        val scheme = rule.protocol.random() + "://"
        val host = if (rule.hostAllow.isNotEmpty()) rule.hostAllow.random() else generateHost(rule)
        val path = generatePath()
        val query = generateQueryParam()
        return "$scheme$host$path$query" // Simplified integration
    }

    private fun generateHost(rule: Url): String {
        val subDomains = listOf("www", "api", "")
        val sub = subDomains.random().let { if (it.isBlank()) "" else "$it." }
        val domain = GeneratorUtils.generateRandomString(3, 10).lowercase()
        val suffix = listOf("com", "net").random()
        val gen = "$sub$domain.$suffix"
        return if (rule.hostBlock.any { gen.contains(it) }) "example.com" else gen
    }

    private fun generatePath(): String {
        val depth = Random.nextInt(3)
        if (depth == 0) return ""
        return (1..depth).joinToString(prefix = "/", separator = "/") {
            GeneratorUtils.generateRandomString(3, 6).lowercase()
        }
    }

    private fun generateQueryParam(): String {
        if (Random.nextBoolean()) return ""
        return "?key=" + GeneratorUtils.generateRandomString(3, 5)
    }

    private inline fun <reified T : Annotation> KParameter.has(): Boolean = findAnnotation<T>() != null
    private inline fun <reified T : Annotation> KParameter.find(): T? = findAnnotation<T>()
}
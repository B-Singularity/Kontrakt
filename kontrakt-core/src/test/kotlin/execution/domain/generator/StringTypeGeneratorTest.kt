package execution.domain.generator

import discovery.api.Email
import discovery.api.NotBlank
import discovery.api.Pattern
import discovery.api.StringLength
import discovery.api.Url
import discovery.api.Uuid
import java.util.UUID
import kotlin.reflect.KParameter
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StringTypeGeneratorTest {

    private val generator = StringTypeGenerator()

    // ==================================================================================
    // [Mega Stub] Contains parameters for Logic, Coverage, and Overlapping scenarios
    // ==================================================================================
    @Suppress("UNUSED_PARAMETER")
    fun annotationStub(
        // [Group 1: Basic Types & Constraints]
        plain: String,
        @StringLength(min = 5, max = 10) lengthConstrained: String,
        @NotBlank notBlank: String,
        intParam: Int, // Unsupported type

        // [Group 2: Email Logic]
        @Email emailDefault: String,
        @Email(allow = ["corp.com", "biz.net"]) emailRestricted: String,
        // Edge Case: Length allows only 5 chars, but domain 'abc.com' is 7 chars.
        // Tests the 'maxAccountLen < 1' safety fallback branch.
        @Email(allow = ["abc.com"]) @StringLength(max = 5) emailShort: String,

        // [Group 3: URL Logic]
        @Url urlDefault: String,
        @Url(protocol = ["https"], hostAllow = ["safe-site.com"]) urlRestricted: String,

        // [Group 4: UUID Logic]
        @Uuid uuid: String,

        // [Group 5: Regex Pattern Coverage]
        @Pattern(regexp = "\\d+") patternNumeric: String,
        @Pattern(regexp = "\\w+") patternWord: String,
        @Pattern(regexp = "^[A-Z]+$") patternUpper: String,
        @Pattern(regexp = "^[a-z]+$") patternLower: String,
        @Pattern(regexp = "unknown-regex") patternUnknown: String,

        // [Group 6: Overlap & Conflict Logic (Key Business Logic)]

        // Overlap: Valid Email AND max length 15
        @Email @StringLength(max = 15) emailWithLength: String,

        // Overlap: Valid URL AND max length 20
        @Url @StringLength(max = 20) urlWithLength: String,

        // Conflict: NotBlank implies min=1, effectively overriding min=0
        @NotBlank @StringLength(min = 0, max = 10) notBlankMinConflict: String,

        // Overlap: NotBlank implies min=1, but explicit min=2 is stronger
        @NotBlank @StringLength(min = 2) notBlankWithMin: String
    ) {
    }

    private fun getParameter(name: String): KParameter {
        return ::annotationStub.parameters.find { it.name == name }
            ?: throw IllegalArgumentException("Parameter '$name' not found in stub function")
    }

    // ==================================================================================
    // 1. Support Logic (Branch Coverage)
    // ==================================================================================

    @Test
    fun `supports - should return true for String and false for others`() {
        assertTrue(generator.supports(getParameter("plain")))
        assertFalse(generator.supports(getParameter("intParam")))
    }

    // ==================================================================================
    // 2. Boundary Logic & Conflicts (Valid Boundaries)
    // ==================================================================================

    @Test
    fun `generateValidBoundaries - should generate min and max length strings`() {
        val param = getParameter("lengthConstrained") // min=5, max=10
        val boundaries = generator.generateValidBoundaries(param)

        // Should contain string of length 5 and 10
        assertTrue(boundaries.any { (it as String).length == 5 }, "Should contain min length string")
        assertTrue(boundaries.any { (it as String).length == 10 }, "Should contain max length string")
    }

    @Test
    fun `generateValidBoundaries - should handle NotBlank constraint`() {
        val param = getParameter("notBlank")
        val boundaries = generator.generateValidBoundaries(param)

        // @NotBlank adds "a" (length 1)
        assertTrue(boundaries.contains("a"), "Should contain single char for NotBlank")
    }

    @Test
    fun `generateValidBoundaries - NotBlank should override StringLength min 0`() {
        // Overlap: @NotBlank + @StringLength(min=0)
        // Logic: NotBlank implies min length is 1. Effective min should be 1.
        val param = getParameter("notBlankMinConflict")
        val boundaries = generator.generateValidBoundaries(param)

        // Should NOT contain empty string
        assertFalse(boundaries.contains(""), "Valid boundaries should NOT contain empty string due to @NotBlank")
        // Should contain length 1 string ("a")
        assertTrue(boundaries.contains("a"), "Should contain length 1 string as min boundary")
    }

    @Test
    fun `generateValidBoundaries - should respect NotBlank with existing Min greater than 1`() {
        // Overlap: @NotBlank + @StringLength(min=2)
        // Logic: max(min=2, notBlank=1) -> Effective min is 2.
        val param = getParameter("notBlankWithMin")
        val boundaries = generator.generateValidBoundaries(param)

        // Should contain "aa" (len 2)
        assertTrue(boundaries.contains("aa"), "Should contain min length string (2 chars)")

        // Should NOT contain "a" (len 1) or ""
        assertFalse(boundaries.contains("a"), "Should not contain length 1 string as min is 2")
        assertFalse(boundaries.contains(""), "Should not contain empty string")
    }

    @Test
    fun `generateValidBoundaries - should include specific formats for special annotations`() {
        val emailParam = getParameter("emailDefault")
        assertTrue(generator.generateValidBoundaries(emailParam).contains("test@example.com"))

        val uuidParam = getParameter("uuid")
        val uuidBoundaries = generator.generateValidBoundaries(uuidParam)
        assertTrue(uuidBoundaries.any {
            try {
                UUID.fromString(it as String); true
            } catch (e: Exception) {
                false
            }
        })
    }

    // ==================================================================================
    // 3. Generation Logic - Priority & Overlap (Happy Path)
    // ==================================================================================

    @Test
    fun `generate - should prioritize Email over generic String`() {
        val param = getParameter("emailDefault")
        repeat(10) {
            val result = generator.generate(param) as String
            assertTrue(result.contains("@"), "Generated value should resemble email")
            assertTrue(result.contains("."), "Email should contain domain part")
        }
    }

    @Test
    fun `generate - should handle complex Email generation with Allowed Domains`() {
        val param = getParameter("emailRestricted")
        repeat(10) {
            val result = generator.generate(param) as String
            assertTrue(
                result.endsWith("corp.com") || result.endsWith("biz.net"),
                "Email domain must be one of the allowed list"
            )
        }
    }

    @Test
    fun `generate - should respect both Email and StringLength overlap`() {
        // Overlap: @Email + @StringLength(max=15)
        val param = getParameter("emailWithLength")
        repeat(20) {
            val result = generator.generate(param) as String

            assertTrue(result.contains("@"), "Should be an email")
            assertTrue(result.length <= 15, "Email length should be <= 15. Actual: ${result.length}")
        }
    }

    @Test
    fun `generate - should fallback safely when Email length limit is too short (Branch Coverage)`() {
        // Targets the 'if (maxAccountLen < 1)' branch in generateComplexEmail
        // Domain 'abc.com' is 7 chars, max length is 5. Account part cannot exist.
        val param = getParameter("emailShort")

        val result = generator.generate(param) as String

        // It should fallback to a minimal email like "a@abc.com" even if it exceeds length slightly,
        // or handle it gracefully. The current logic returns "a@domain".
        assertTrue(result.startsWith("a@"), "Should fallback to minimal account name")
    }

    @Test
    fun `generate - should respect both Url and StringLength overlap`() {
        // Overlap: @Url + @StringLength(max=20)
        val param = getParameter("urlWithLength")
        repeat(20) {
            val result = generator.generate(param) as String

            assertTrue(result.contains("://"), "Should be a URL")
            assertTrue(result.length <= 20, "URL length should be <= 20. Actual: ${result.length}")
        }
    }

    @Test
    fun `generate - should generate URL with allowed hosts`() {
        val param = getParameter("urlRestricted")
        repeat(10) {
            val result = generator.generate(param) as String
            assertTrue(result.startsWith("https://"), "Should use allowed protocol")
            assertTrue(result.contains("safe-site.com"), "Should use allowed host")
        }
    }

    @Test
    fun `generate - should generate URL with random parts (Branch Coverage)`() {
        // Hits random branches for Path depth and Query param generation
        val param = getParameter("urlDefault")
        var hasQuery = false
        var hasPath = false

        repeat(50) {
            val result = generator.generate(param) as String
            if (result.contains("?key=")) hasQuery = true
            if (result.count { it == '/' } > 2) hasPath = true
        }

        assertTrue(hasQuery, "Should eventually generate a query parameter")
        assertTrue(hasPath, "Should eventually generate a path")
    }

    @Test
    fun `generate - should generate UUID`() {
        val param = getParameter("uuid")
        repeat(5) {
            val result = generator.generate(param) as String
            assertTrue(
                runCatching { UUID.fromString(result) }.isSuccess,
                "Generated string '$result' should be a valid UUID"
            )
        }
    }

    @Test
    fun `generate - should handle Regex Patterns (Switch Case Coverage)`() {
        // 1. Numeric
        assertTrue((generator.generate(getParameter("patternNumeric")) as String).all { it.isDigit() })
        // 2. Word
        val word = generator.generate(getParameter("patternWord")) as String
        assertTrue(word.all { it.isLetter() || it.isDigit() })
        // 3. Upper
        assertTrue((generator.generate(getParameter("patternUpper")) as String).all { it.isUpperCase() })
        // 4. Lower
        assertTrue((generator.generate(getParameter("patternLower")) as String).all { it.isLowerCase() })
        // 5. Unknown Fallback
        assertTrue((generator.generate(getParameter("patternUnknown")) as String).startsWith("Pattern_Placeholder"))
    }

    // ==================================================================================
    // 4. Invalid Logic (Negative Testing)
    // ==================================================================================

    @Test
    fun `generateInvalid - should generate boundary violations`() {
        val param = getParameter("lengthConstrained") // min=5, max=10
        val invalids = generator.generateInvalid(param)

        assertTrue(invalids.contains("x".repeat(4)), "Should contain min-1 length")
        assertTrue(invalids.contains("x".repeat(11)), "Should contain max+1 length")
    }

    @Test
    fun `generateInvalid - should generate NotBlank violations`() {
        val param = getParameter("notBlank")
        val invalids = generator.generateInvalid(param)

        assertTrue(invalids.contains(""), "Should contain empty string")
        assertTrue(invalids.contains("   "), "Should contain blank string")
    }

    @Test
    fun `generateInvalid - should generate Email violations`() {
        val param = getParameter("emailDefault")
        val invalids = generator.generateInvalid(param)

        assertTrue(invalids.contains("not-an-email"))
        assertTrue(invalids.contains("@domain.com"))
    }

    @Test
    fun `generateInvalid - should generate violations for overlapping NotBlank and Min=0`() {
        // Overlap: @NotBlank + @StringLength(min=0)
        // Even if length=0 is technically allowed by StringLength, NotBlank forbids it.
        val param = getParameter("notBlankMinConflict")
        val invalids = generator.generateInvalid(param)

        assertTrue(invalids.contains(""), "Should generate empty string as invalid due to NotBlank")
        assertTrue(invalids.contains("   "), "Should generate blank string as invalid due to NotBlank")
    }
}
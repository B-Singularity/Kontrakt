package execution.domain.vo

import execution.domain.AssertionStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AssertionRecordTest {
    private val statusPass = AssertionStatus.PASSED
    private val statusFail = AssertionStatus.FAILED

    @Test
    fun `should retain values provided in constructor`() {
        val message = "Expected 5 but got 3"
        val expected = 5
        val actual = 3

        val record =
            AssertionRecord(
                status = statusFail,
                message = message,
                expected = expected,
                actual = actual,
            )

        assertEquals(statusFail, record.status)
        assertEquals(message, record.message)
        assertEquals(expected, record.expected)
        assertEquals(actual, record.actual)
    }

    @Test
    fun `should handle null values for expected and actual`() {
        val record =
            AssertionRecord(
                status = statusPass,
                message = "Both are null",
                expected = null,
                actual = null,
            )

        assertEquals(null, record.expected)
        assertEquals(null, record.actual)
    }

    @Test
    fun `equals should return true for records with same data`() {
        val record1 = AssertionRecord(statusPass, "Msg", 1, 1)
        val record2 = AssertionRecord(statusPass, "Msg", 1, 1)

        assertEquals(record1, record2)
        assertEquals(record1.hashCode(), record2.hashCode())
    }

    @Test
    fun `equals should return false for records with different data`() {
        val record1 = AssertionRecord(statusPass, "Msg", 1, 1)
        val record2 = AssertionRecord(statusFail, "Msg", 1, 1) // Status different
        val record3 = AssertionRecord(statusPass, "Diff", 1, 1) // Message different

        assertNotEquals(record1, record2)
        assertNotEquals(record1, record3)
    }

    @Test
    fun `copy should create new instance with modified values`() {
        val original = AssertionRecord(statusPass, "Original", 10, 10)
        val copied = original.copy(message = "Modified", actual = 11)

        // Modified fields
        assertEquals("Modified", copied.message)
        assertEquals(11, copied.actual)

        // Retained fields
        assertEquals(original.status, copied.status)
        assertEquals(original.expected, copied.expected)

        // Structure check
        assertNotEquals(original, copied)
    }

    @Test
    fun `toString contains all field values`() {
        val record = AssertionRecord(statusPass, "TestMessage", "A", "B")
        val stringRep = record.toString()

        assertTrue(stringRep.contains("TestMessage"))
        assertTrue(stringRep.contains("A"))
        assertTrue(stringRep.contains("B"))
    }
}

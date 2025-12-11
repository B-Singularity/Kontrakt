package execution.spi

import discovery.api.KontraktConfigurationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

abstract class MockingEngineTest {

    protected abstract val engine: MockingEngine
    protected abstract val control: ScenarioControl

    interface StatelessService {
        fun getString(): String
        fun getInt(): Int
        fun getList(): List<String>
        fun doSomething()
    }

    interface StatefulRepository {
        fun save(entity: User): User
        fun findById(id: Long): User?
        fun findAll(): List<User>
        fun delete(id: Long)
        fun count(): Long
    }

    interface HeuristicRepository {
        // Standard methods for verification
        fun findById(id: Long): User?
        fun count(): Long

        // 1. Save Patterns
        fun createAccount(user: User): User
        fun registerMember(user: User): User

        // 2. Find Patterns
        fun getById(id: Long): User?
        fun findUser(id: Long): User?

        // 3. Delete Patterns
        fun removeAccount(id: Long)

        // 4. Fallback Patterns (Complex Queries -> Should be Generated)
        fun findByName(name: String): List<User>
        fun searchUsers(query: String): List<User>
    }

    data class User(val id: Long, val name: String)


    @Test
    fun `createMock - should generate non-null values for return types`() {

        val mock = engine.createMock(StatelessService::class)

        assertNotNull(mock.getString(), "should generate a string")
        assertNotNull(mock.getInt(), "should generate an int")
        assertTrue(mock.getList().isNotEmpty(), "Should generate a non-empty list (Aggressive Default)")

        mock.doSomething()
    }

    @Test
    fun `createFake - should support basic CRUD operations`() {
        // Given
        val fakeRepo = engine.createFake(StatefulRepository::class)
        val user = User(1L, "TestUser")

        // 1. Save
        val saved = fakeRepo.save(user)
        assertEquals(user, saved, "Save should return the entity")

        // 2. FindById
        val found = fakeRepo.findById(1L)
        assertEquals(user, found, "Should find the saved entity")

        // 3. Count
        assertEquals(1L, fakeRepo.count(), "Count should be 1")

        // 4. FindAll
        val all = fakeRepo.findAll()
        assertEquals(1, all.size)
        assertEquals(user, all.first())

        // 5. Delete
        fakeRepo.delete(1L)
        val deleted = fakeRepo.findById(1L)
        assertEquals(null, deleted, "Should return null after deletion")
        assertEquals(0L, fakeRepo.count(), "Count should be 0 after delete")
    }

    @Test
    fun `createFake - should recognize various naming patterns (Heuristics)`() {

        val repo = engine.createFake(HeuristicRepository::class)
        val user1 = User(10L, "Alice")
        val user2 = User(20L, "Bob")

        // 1. Save Heuristics (create*, register*)
        repo.createAccount(user1)
        repo.registerMember(user2)

        assertEquals(2L, repo.count(), "create* and register* should act as save")
        assertEquals(user1, repo.findById(10L), "Data should be retrievable")

        // 2. Find Heuristics (getById, findUser)
        assertEquals(user1, repo.getById(10L), "getById should work like findById")
        assertEquals(user2, repo.findUser(20L), "findUser should work like findById")

        // 3. Delete Heuristics (remove*)
        repo.removeAccount(10L)
        assertEquals(1L, repo.count(), "remove* should work like delete")
        assertNull(repo.findById(10L), "Removed data should be gone")
    }

    @Test
    fun `createFake - should fallback to generative mode for complex queries`() {

        val repo = engine.createFake(HeuristicRepository::class)

        // The engine should NOT return null or crash, but fallback to FixtureGenerator.
        val resultByName = repo.findByName("UnknownName")
        val resultBySearch = repo.searchUsers("Query")

        assertNotNull(resultByName)
        assertTrue(resultByName.isNotEmpty(), "Fallback should generate a valid list for findByName")

        assertNotNull(resultBySearch)
        assertTrue(resultBySearch.isNotEmpty(), "Fallback should generate a valid list for searchUsers")
    }

    @Test
    fun `ScenarioContext - should override default mock behavior`() {

        val mock = engine.createMock(StatelessService::class)
        val context = control.createScenarioContext()

        context every { mock.getString() } returns "Overridden Value"

        assertEquals("Overridden Value", mock.getString(), "Should return the overridden value")
    }

    @Test
    fun `ScenarioContext - should throw exception when configured`() {

        val mock = engine.createMock(StatelessService::class)
        val context = control.createScenarioContext()
        val expectedException = RuntimeException("Boom!")

        context every { mock.doSomething() } throws expectedException

        val exception = assertFailsWith<RuntimeException> {
            mock.doSomething()
        }
        assertEquals("Boom!", exception.message)
    }

    @Test
    fun `ScenarioContext - should throw ConfigurationException for invalid stubbing`() {
        val context = control.createScenarioContext()
        val realObject = "I am not a mock"

        assertFailsWith<KontraktConfigurationException> {
            context every { realObject.length } returns 5
        }
    }
}
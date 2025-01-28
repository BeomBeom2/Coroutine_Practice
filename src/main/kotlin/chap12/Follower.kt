package org.example.chap12

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

sealed class Follower(
    private val id: String,
    private val name: String
) {
    data class OfficialAccount(
        private val id: String,
        val name: String
    ) : Follower(id, name)

    data class PersonAccount(
        private val id: String,
        val name: String
    ) : Follower(id, name)
}

interface OfficialAccountRepository{
    suspend fun searchByName(name:String) : Array<Follower.OfficialAccount>
}
interface PersonAccountRepository {
    suspend fun searchByName(name: String) : Array<Follower.PersonAccount>
}

class FollowerSearcher(
    private val officialAccountRepository: OfficialAccountRepository,
    private val personAccountRepository: PersonAccountRepository
) {
    suspend fun searchByName(name : String) : List<Follower> = coroutineScope {
        val officialAccountDeferred = async {
            officialAccountRepository.searchByName(name)
        }
        val personAccountDeferred = async {
            personAccountRepository.searchByName(name)
        }

        return@coroutineScope listOf(
            *officialAccountDeferred.await(),
            *personAccountDeferred.await()
        )
    }
}

class StubOfficialAccountRepository(
    private val users: List<Follower.OfficialAccount>
    ): OfficialAccountRepository{
    override suspend fun searchByName(name: String) : Array<Follower.OfficialAccount> {
        delay(1000L)
        return users.filter {user ->
            user.name.contains(name)
        }.toTypedArray()
    }
}

class StubPersonAccountRepository(
    private val users: List<Follower.PersonAccount>
): PersonAccountRepository{
    override suspend fun searchByName(name: String) : Array<Follower.PersonAccount> {
        delay(1000L)
        return users.filter {user ->
            user.name.contains(name)
        }.toTypedArray()
    }
}

class FollowerSearcherTest {
    private lateinit var followerSearcher : FollowerSearcher

    @BeforeEach
    fun setUp() {
        followerSearcher = FollowerSearcher(
            officialAccountRepository = stubOfficialAccountRepository,
            personAccountRepository = stubPersonAccountRepository
        )
    }

    companion object {
        private val companyA = Follower.OfficialAccount(id = "0x0000", name = "CompanyA")
        private val companyB = Follower.OfficialAccount(id = "0x0001", name = "CompanyB")
        private val companyC = Follower.OfficialAccount(id = "0x0002", name = "CompanyC")

        private val stubOfficialAccountRepository =
            StubOfficialAccountRepository(
                users = listOf(companyA, companyB, companyC)
            )

        private val personA = Follower.PersonAccount(id = "0x1000", name = "PersonA")
        private val personB = Follower.PersonAccount(id = "0x1001", name = "PersonB")
        private val personC = Follower.PersonAccount(id = "0x1002", name = "PersonC")

        private val stubPersonAccountRepository = StubPersonAccountRepository(
            users = listOf(personA, personB, personC)
        )
    }

    @Test
    fun `공식 계정과 개인 계정이 합쳐져 반환되는지 테스트`() = runTest {
        // Given
        val searchName = "A"
        val expectedResults = listOf(companyA, personA)

        // When
        val results = followerSearcher.searchByName(searchName)

        // Then
        Assertions.assertEquals(
            expectedResults,
            results
        )
    }

    @Test
    fun `빈 배열이 반환되는지 테스트`() = runTest {
        // Given
        val searchName = "Empty"
        val expectedResults = emptyList<Follower>()

        // When
        val results = followerSearcher.searchByName(searchName)

        // Then
        Assertions.assertEquals(
            expectedResults,
            results
        )
    }
}
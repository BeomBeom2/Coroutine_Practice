package org.example.chap12

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

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
package org.example.chap12

import kotlinx.coroutines.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AddUseCase {
    fun add(vararg args: Int) : Int {
        return args.sum()
    }
}

class AddUseCaseTest {
    @Test
    fun `1 더하기 2는 3이다`() {
        val addUseCase: AddUseCase = AddUseCase()
        val result = addUseCase.add(1, 2)
        assertEquals(3, result)
    }
}

class AddUseCaseTestBeforeEach{
    lateinit var addUseCase : AddUseCase

    @BeforeEach
    fun setUp() {
        addUseCase = AddUseCase()
    }

    @Test
    fun `one plus two is three`() {
        val res = addUseCase.add(1, 2)
        println(res)
        assertEquals(3, res)
    }

    @Test
    fun `minus one plus three is two`() {
        val res = addUseCase.add(-1, 3)
        println(res)
        assertEquals(2, res)
    }
}

interface UserNameRepository {
    fun saveUserName(id: String, name: String)
    fun getNameByUserId(id : String) : String
}

interface UserPhoneNumberRepository{
    fun saveUserPhoneNumber(id: String, phoneNumber: String)
    fun getPhoneNumberByUserId(id: String) : String
}

data class UserProfile(val id : String, val name : String, val phoneNumber : String)

//다른 객체와의 의존성을 가진 객체를 테스트하기 위해서는 테스트 더블이 필요하다.
//테스트 더블 : 객체에 대한 대체물, 객체의 행동을 모방하는 객체를 만드는 데 사용한다.
//스텁 : 미리 정의된 데이터를 반호나하는 모방 객체로 반환값이 없는 동작은 구현하지 않으며, 반환값이 있는 동작만 미리 정의된 데이터를 반환하도록 구현.
class StubUserNameRepository : UserNameRepository {
    private val userNameMap = mapOf<String, String> (
        "0x1111" to "홍길동",
        "0x2222" to "조세영",
    )

    override fun saveUserName(id: String, name: String) {

    }

    override fun getNameByUserId(id: String): String {
        TODO("Not yet implemented")
    }
}

//userNameMap을 주입받도록하는 조금 더 유연한 StubRepo
class StubUserNameRepository1(
    private val userNameMap: Map<String, String>
) : UserNameRepository {
    override fun saveUserName(id: String, name: String) {
        TODO("Not yet implemented")
    }

    override fun getNameByUserId(id: String): String {
        return userNameMap[id] ?: ""
    }
}

//페이크 : 실제 객체와 비슷하게 동작하도록 구현된 모방 객체이다.
class FakeUserPhoneNumberRepository : UserPhoneNumberRepository {
    private val userPhoneNumberMap = mutableMapOf<String, String>()
    override fun saveUserPhoneNumber(id: String, phoneNumber: String) {
       userPhoneNumberMap[id] = phoneNumber
    }

    override fun getPhoneNumberByUserId(id: String): String {
        return userPhoneNumberMap[id] ?: ""
    }
}

class UserProfileFetcher(
    private val userNameRepository : UserNameRepository,
    private val userPhoneNumberRepository : UserPhoneNumberRepository
) {
    fun getUserProfileById(id : String) : UserProfile {
        val userName = userNameRepository.getNameByUserId(id)
        val userPhoneNumber = userPhoneNumberRepository.getPhoneNumberByUserId(id)
        return UserProfile(
            id = id,
            name = userName,
            phoneNumber = userPhoneNumber
        )
    }
}

class UserProfileFetcherTest {
    @Test
    fun `UserNameRepository가 반환하는 이름이 홍길동이면 UserProfileFetcher에서 UserProfile를 가져왔을 때 이름이 홍길동이어야 한다`() {
        // Given
        val userProfileFetcher = UserProfileFetcher(
            userNameRepository = StubUserNameRepository1(
                userNameMap = mapOf<String, String>(
                    "0x1111" to "홍길동",
                    "0x2222" to "조세영",
                )
            ),
            userPhoneNumberRepository = FakeUserPhoneNumberRepository()
        )

        // When
        val userProfile = userProfileFetcher.getUserProfileById("0x1111")

        // Then
        assertEquals("홍길동", userProfile.name)
    }

    @Test
    fun `UserPhoneNumberRepository에 휴대폰 번호가 저장돼 있으면, UserProfile를 가져왔을 때 해당 휴대폰 번호가 반환돼야 한다`() {
        // Given
        val userProfileFetcher = UserProfileFetcher(
            userNameRepository = StubUserNameRepository1(
                userNameMap = mapOf<String, String>(
                    "0x1111" to "홍길동",
                    "0x2222" to "조세영",
                )
            ),
            userPhoneNumberRepository = FakeUserPhoneNumberRepository().apply {
                this.saveUserPhoneNumber("0x1111", "010-xxxx-xxxx")
            }
        )

        // When
        val userProfile = userProfileFetcher.getUserProfileById("0x1111")

        // Then
        assertEquals("010-xxxx-xxxx", userProfile.phoneNumber)
    }

    //테스트를 위해 매번 인터페이스를 만들지 않고 쉽게 사용할 수 있는 Mokito나 MockK같은 라이브러리들이 있다.
}

class RepeatAddUseCase {
    suspend fun add(repeatTime: Int) : Int = withContext(Dispatchers.Default) {
        var result = 0
        repeat(repeatTime) {
            result += 1
        }
        return@withContext result
    }
}

class RepeatAddUseCaseTest {
    @Test
    fun `100번 더하면 100이 반환된다`() = runBlocking<Unit> {
        // Given
        val repeatAddUseCase = RepeatAddUseCase()

        // When
        val result =
            repeatAddUseCase.add(100)


        // Then
        assertEquals(100, result)
    }
}

class RepeatAddWithDelayUseCase {
    suspend fun add(repeatTime : Int) : Int {
        var result = 0
        repeat(repeatTime) {
            delay(100L)
            result += 1
        }
        return result
    }
}

class RepeatAddWithDelayUseCaseTest {
    @Test
    fun `100번 더하면 100이 반환된다`() {
        val testCoroutineScheduler = TestCoroutineScheduler()
        val testDispatcher = StandardTestDispatcher(testCoroutineScheduler)

        val repeatAddUseCase = RepeatAddWithDelayUseCase()

        var result = 0
        CoroutineScope(testDispatcher).launch {
            result = repeatAddUseCase.add(100)
            assertEquals(100, result)
        }

        testCoroutineScheduler.advanceUntilIdle()
        assertEquals(100, result)
    }
}

class TestCoroutineScheduler {
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `가상 시간 조절 테스트`(): Unit {
        // 테스트 환경 설정
        val testCoroutineScheduler = TestCoroutineScheduler()

        testCoroutineScheduler.advanceTimeBy(5000L) //가상 시간에서 5초
        assertEquals(5000L, testCoroutineScheduler.currentTime) //현재 시간이 5초임을 확인

        testCoroutineScheduler.advanceTimeBy(6000L)
        assertEquals(11000L, testCoroutineScheduler.currentTime)
    }
    //TestCoroutineScheduler 객체를 사용해 직접 가장 시간을 컨드롤 하지는 않지만,
    //이후 다룰 코루틴 테스트 라이브러리의 기능을 이해하는 데 도움이 된다.
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `가상 시간 위에서 테스트 진행`() {
        // 테스트 환경 설정
        val testCoroutineScheduler : TestCoroutineScheduler =
            TestCoroutineScheduler()
        val testDispatcher : TestDispatcher = StandardTestDispatcher(scheduler = testCoroutineScheduler)
        val testCoroutineScope : CoroutineScope = CoroutineScope(context = testDispatcher)

        // Given
        var result = 0

        // When
        testCoroutineScope.launch {
            delay(10000L)
            result = 1
            delay(10000L)
            result = 2
            println(Thread.currentThread().name)
        }

        // Then
        assertEquals(0, result)
        testCoroutineScheduler.advanceTimeBy(5000L)
        assertEquals(0, result)
        testCoroutineScheduler.advanceTimeBy(6000L)
        assertEquals(1, result)
        testCoroutineScheduler.advanceTimeBy(11000L)
        assertEquals(2, result)

    }

    @Test
    fun `advanceUntilIdle의 동작 살펴보기`() {
        // 테스트 환경 설정
        val testCoroutineScheduler : TestCoroutineScheduler = TestCoroutineScheduler()
        val testDispatcher : TestDispatcher = StandardTestDispatcher(scheduler = testCoroutineScheduler)
        val testCoroutineScope = CoroutineScope(context = testDispatcher)

        // Given
        var result = 0

        // When
        testCoroutineScope.launch {
            delay(10000L)
            result = 1
            delay(10_000L)
            result = 2
        }

        // testCoroutineScheduler 객체를 사용한 모든 코루틴 작업이 실행 완료될 때까지 가상 시간이 흐르게 된다.
        testCoroutineScheduler.advanceUntilIdle()


        // Then
        assertEquals(2, result)
    }
}
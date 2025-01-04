package org.example.chap12

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

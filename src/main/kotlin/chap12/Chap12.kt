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
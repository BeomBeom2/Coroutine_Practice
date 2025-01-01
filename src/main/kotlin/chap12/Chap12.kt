package org.example.chap12

import org.junit.jupiter.api.Assertions.assertEquals
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
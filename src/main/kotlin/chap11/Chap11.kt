package org.example.chap11

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

//1. 메모리 가시성, 스레드가 변수를 변경시킬 때 메인 메모리가 아닌 CPU 캐시를 사용할 경우 CPU 캐시의 값이 메인 메모리에 전파되는 데 약간의 시간이 결려
// CPU 캐시와 메인 메모리 간에 데이터 불일치 문제가 생긴다.

//2. 2개의 스레드가 동시에 count 변수를 읽고 업데이트 하면 count 변수가 1000에서 1001으로 되는 연산이 두 번 일어난다. 즉, 하나의 연산은 손실된다.

//여전히 하나의 변수에 스레드가 동시에 접근할 수 있어서 의도한? 결과나 나오지 않는다.
@Volatile
var count = 0

fun Ex_11_11_2() = runBlocking<Unit> {
    withContext(Dispatchers.Default) {
        repeat(10000) {
            launch {
                count += 1
            }
        }
    }
    println("count = $count")

    //count = 9355
}

val mutex = Mutex()
fun Ex_11_11_3() = runBlocking<Unit> {
    withContext(Dispatchers.Default) {
        repeat(1000) {
            launch {
                mutex.lock()
                count += 1
                mutex.unlock()
            }
        }
    }
    println("count = $count")

    //count = 1000
}


//위에 있는 mutex의 lock(), unlock() 쌍을 처리해주는 메소드인 withLock이다.
//코드가 복잡해질수록 실수할 확률이 높기 때문에, withLock을 사용해서 처리하면 편리하다.
fun Ex_11_11_4() = runBlocking<Unit> {
    withContext(Dispatchers.Default) {
        repeat(1000) {
            launch {
                mutex.withLock {
                    count += 1
                }
            }
        }
    }
    println("count = $count")

    //count = 1000
}

//Mutex 객체에 락이 걸려 있으면 코루틴은 기존의 락이 해제될 때 까지 스레드를 양보하고 일시 중단한다.
//ReentrantLock은 코루틴은 락이 해제될 때까지 lock을 호출한 스레드를 블로킹하고 기다린다.
//즉, 락이 해제될 때까지 lock을 호출한 스레드를 다른 코루틴이 사용할 수 없다.
//이런 특성 때문에 코루틴에서는 ReentrantLock대신 Mutex 객체를 권장한다.


//원자성 있는 데이터 구조를 사용해 경쟁 상태 문제를 해결하기
var count1 = AtomicInteger(0)

fun Ex_11_11_7() = runBlocking<Unit> {
    withContext(Dispatchers.Default) {
        repeat(10000) {
            launch {
                count1.getAndUpdate {
                    //다른 스레드가 연산을 실행 중이면 코루틴은 스레드를 블로킹시키고 대기한다.
                    it + 1
                }
            }
        }
    }
    println("count = $count1")

    //count = 10000
}

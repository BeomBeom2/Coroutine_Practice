package org.example.chap11

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.coroutines.resume

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

//AtomicReference 를 사용해 객체 참조에 원자성 부여.
//원자성 있는 객체를 사용 시, 한계점은 원자성 있는 객체가 스레드를 블로킹 시킬 수 있다는 점이다.
data class Counter(val name : String, val count : Int)
val atomicCounter = AtomicReference(Counter("MyCounter", 0))

fun Ex_11_11_8() = runBlocking<Unit> {
    withContext(Dispatchers.Default) {
        repeat(10000) {
            launch {
                atomicCounter.getAndUpdate {
                    it.copy(count = it.count +1)
                }
            }
        }
    }
    println(atomicCounter.get())
}

//다음은 실수할 수 있는 코드이다. count1에 대한 get 함수를 실행해 count 값을 가져와 count에 대해
//set 함수를 실행해 get을 통해 가져온 값에 1을 더하고 있다. 이때 get 함수가 실행되고 나서 set 함수 실행 전에 다른 스레드에서
// 읽기 또는 쓰기 연산을 실행할 수 있으므로 경쟁 상태 문제가 생긴다.
fun Ex_11_11_10() = runBlocking<Unit> {
    withContext(Dispatchers.Default) {
        repeat(10000) {
            launch {
                val currentCount = count1.get()

                count1.set(currentCount + 1)
            }
        }
    }
    println("count = $count1")
    //count = 9094
}

//Continuation 객체는 코루틴의 일시 중단 시점에 코루틴의 실행 상태를 저장하며, 여기에는 다음에 실행해야 할 작업에 대한 정보가 포함된다.
//따라서 Continuation 객체를 사용하면 코루틴 재개 시 코루틴의 상태를 복원하고 이어서 작업을 진행할 수 있다.
//우리가 이전까지 다뤘던 코루틴 API는 고수준 API 여서 Continuation 객체를 직접 사용하지는 않았다.

fun Ex_11_11_24() = runBlocking<Unit> {
    println("runBlocking 코루틴 일시 중단 호출")
    suspendCancellableCoroutine<Unit> { continuation:
        CancellableContinuation<Unit> ->
            println("일시 중단 시점의 runBlocking 코루틴 실행 정보 : ${continuation.context}")
            continuation.resume(Unit)
    }
    println("일시 중단된 코루틴이 재개되지 않아 실행되지 않는 코드")

    //runBlocking 코루틴 일시 중단 호출
    //일시 중단 시점의 runBlocking 코루틴 실행 정보 : [CoroutineId(2), "coroutine#2":BlockingCoroutine{Active}@66048bfd, BlockingEventLoop@61443d8f]
    //일시 중단된 코루틴이 재개되지 않아 실행되지 않는 코드
}

fun Ex_11_11_25() = runBlocking<Unit> {
    val result = suspendCancellableCoroutine<String> { continuation:
        CancellableContinuation<String> -> //runBlocking 코루틴 일시 중단 시작
            thread {
                Thread.sleep(1000L)
                continuation.resume("실행 결과") //runBlocking 코루틴 재개
            }
    }
    println(result)
}
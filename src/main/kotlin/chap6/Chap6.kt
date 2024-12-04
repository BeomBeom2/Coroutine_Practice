package org.example.chap6

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
fun Ex_6_1() = runBlocking<Unit> {
    //CoroutineContext 객체는 키-값 쌍으로 구성 요소를 관리하지만, 키에 값을 직접 지정하지 않고 '+' 연산자를 사용한다.
    // 수정을 원할 경우 '+' 연산자를 사용해 마지막에 들어온 하나의 값을 취하도록 하면 된다.
    val coroutineContext : CoroutineContext = newSingleThreadContext("My Thread") + CoroutineName("MyCoroutine")

    launch(context = coroutineContext) {
        println("[${Thread.currentThread().name}] 실행")
    }

    /*
     * [My Thread @MyCoroutine#3] 실행
     */
}

fun Ex_6_5() {
    //아래와 같이 Job 객체를 직접 생성해 추가하면 코루틴의 구조화가 때질 수도 있어 주의가 필요하다.
    val myJob = Job()
    val coroutineContext : CoroutineContext = Dispatchers.IO + myJob

    val key1 = Dispatchers.IO.key
    val key2 = Dispatchers.Default.key

    println("key1 = $key1, key2 = $key2")
    println("key1 과 key2는 ${key1 === key2} 하다.")
    /*
     * key1 과 key2는 true 하다.
     */

    val coroutineContext1 = CoroutineName("MyCoroutine") + Dispatchers.IO
    val nameFromContext = coroutineContext1[CoroutineName.Key]

    //컴파일러의 타입 추론에 의해서 CoroutineName.Key 를 자동 추론해 "MyCoroutine" 값을 반환
    val nameFromContext1 = coroutineContext1[CoroutineName]
    println(nameFromContext1)
    /*
     * CoroutineName(MyCoroutine)
     */
}

@OptIn(ExperimentalStdlibApi::class)
fun Ex_6_8() {
    val coroutineName : CoroutineName = CoroutineName("MyCoroutine")
    val dispatcher : CoroutineDispatcher = Dispatchers.IO
    val coroutineContext = coroutineName + dispatcher
 
    println(coroutineContext[coroutineName.key])
    CoroutineName("MyCoroutine")
    println(coroutineContext[dispatcher.key])

    /*
     * CoroutineName(MyCoroutine)
     * Dispatchers.IO
     */
}

@OptIn(ExperimentalStdlibApi::class)
fun Ex_6_11() = runBlocking<Unit> {
    val coroutineName = CoroutineName("MyCoroutine")
    val dispatcher = Dispatchers.IO
    val job = Job()
    val coroutineContext : CoroutineContext = coroutineName + dispatcher + job

    //minusKey 함수는 minusKey를 호출한 CoroutineContext 객체는 그대로 유지되고,
    //구성 요소가 제거된 새로운 CoroutineContext 객체가 반환된다.
    val deletedCoroutineContext = coroutineContext.minusKey(CoroutineName)

    println(coroutineContext[CoroutineName])
    println(coroutineContext[CoroutineDispatcher])
    println(coroutineContext[Job])

    println(deletedCoroutineContext[CoroutineName])
    println(deletedCoroutineContext[CoroutineDispatcher])
    println(deletedCoroutineContext[Job])

    /*
     * CoroutineName(MyCoroutine)
     * Dispatchers.IO
     * JobImpl{Active}@270421f5
     *
     * null
     * Dispatchers.IO
     * JobImpl{Active}@270421f5
     */
}
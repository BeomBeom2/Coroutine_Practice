package org.example

import kotlinx.coroutines.*
import org.example.chap4.*
import org.example.chap5.*
import org.example.chap6.Ex_6_1
import org.example.chap6.Ex_6_11
import org.example.chap6.Ex_6_5
import org.example.chap6.Ex_6_8

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
fun main() = runBlocking<Unit>(context = CoroutineName("Main")){
    Ex_Basic()

    //Ex_4_1_2()

    //Ex_4_3_1()

    //Ex_4_4_1()

    //Ex_4_4_17()

    //Ex_4_4_20()

    //Ex_4_4_21()

    //Ex_5_1_1()

    //Ex_5_5_1()

    //Ex_5_5_9()

    //Ex_5_5_10()

    //Ex_5_5_11()

    //Ex_6_1()

    //Ex_6_5()

    //Ex_6_8()

    //Ex_6_11()
}


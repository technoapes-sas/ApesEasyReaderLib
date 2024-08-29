package com.apes.capuchin.rfidcorelib.utils

import java.util.concurrent.Executor

class InitTask(private val executor: Executor) {
    fun execute(runnable: Runnable) {
        executor.execute(runnable)
    }
}
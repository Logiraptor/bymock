package io.poyarzun.bymock

import kotlin.IllegalStateException
import kotlin.test.Test

class MockTest {

    interface TestInterface {
        fun getValue(): Int
    }

    @Test(expected = IllegalStateException::class)
    fun dummyObjectThrowsIllegalStateExceptions() {
        val dummyTestInterface: TestInterface = dummy()
        dummyTestInterface.getValue()
    }
}
package io.poyarzun.bymock

import java.lang.reflect.Proxy

/**
 * Returns a test double (a dummy) of type [T].
 *
 * Calls to any method on the dummy will throw an [IllegalStateException]
 * with the method name and arguments in the message
 */
inline fun <reified T> dummy(): T {
    return Proxy.newProxyInstance(Thread.currentThread().contextClassLoader, arrayOf(T::class.java)) { _, method, args ->
        throw IllegalStateException("Method ${method.name} was inappropriately called on test double with arguments $args")
    } as T
}

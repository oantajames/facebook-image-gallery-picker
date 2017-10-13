package com.imagepicker.facebook

/**
 * @author james on 10/13/17.
 *
 * cheers to :
 * https://medium.com/@BladeCoder/kotlin-singletons-with-argument-194ef06edd9e
 *
 * Trust the Kotlin authors on this:
 * this code is actually borrowed directly from the implementation of the lazy() function in the Kotlin standard library
 *
 */
open class SingletonHolder<out T, in A>(creator: (A) -> T) {

    private var creator: ((A) -> T)? = creator

    @Volatile private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}
package utils

import Constants

/**
 * 这是一个用于函数记忆化的工具包，它的主要目的是缓存函数的输出以避免重复计算。具体的工作原理是，对于已经计算过的输入，直接从缓存中获取其对应的输出，而不是重新计算。这可以提高性能，特别是对于计算成本高的函数。
 * * memoize: 为无参、单参、双参和三参的函数提供记忆化功能。函数的结果被存储在一个映射中，这样再次调用时可以直接从映射中获取结果。
 * * @CacheValueProtection 是一个注解，其目的是提醒用户在使用 cacheValue 时确保所传入的函数不依赖于任何外部变量，或函数返回的值始终是常数。这是为了确保缓存的值始终是正确的。
 * * cacheValue 是一个辅助函数，它简化了函数记忆化的使用。当用户想要缓存某个函数的结果时，只需要使用 cacheValue 而不是直接使用 memoize。
 */
@Suppress(Constants.UNCHECKED_CAST)
fun <R> memoize(function: () -> R): () -> R {
    var cache: R? = null
    return {
        if (cache == null) {
            cache = function()
        }
        cache as R
    }
}

fun <P, R> memoize(function: (P) -> R): (P) -> R {
    val cache = mutableMapOf<P, R>()
    return { param ->
        cache.getOrPut(param) { function(param) }
    }
}

fun <P1, P2, R> memoize(function: (P1, P2) -> R): (P1, P2) -> R {
    val cache = mutableMapOf<Pair<P1, P2>, R>()
    return { p1, p2 ->
        cache.getOrPut(p1 to p2) { function(p1, p2) }
    }
}

fun <P1, P2, P3, R> memoize(function: (P1, P2, P3) -> R): (P1, P2, P3) -> R {
    val cache = mutableMapOf<Triple<P1, P2, P3>, R>()
    return { p1, p2, p3 ->
        cache.getOrPut(Triple(p1, p2, p3)) { function(p1, p2, p3) }
    }
}

@JvmName("memoize0")
fun <R> (() -> R).memoize() = memoize(this)

@JvmName("memoize1")
fun <P1, R> ((P1) -> R).memoize() = memoize(this)

@JvmName("memoize2")
fun <P1, P2, R> ((P1, P2) -> R).memoize() = memoize(this)

@JvmName("memoize3")
fun <P1, P2, P3, R> ((P1, P2, P3) -> R).memoize() = memoize(this)

@RequiresOptIn(message = "使用 CacheValue 时，请确保传入的函数中不依赖于任何外部变量，或者函数返回值总为常量")
annotation class CacheValueProtection

@CacheValueProtection
fun <R> cacheValue(supplier: () -> R): R = supplier.memoize().invoke()

@CacheValueProtection
fun <P, R> cacheValue(p: P, supplier: (P) -> R): R = supplier.memoize().invoke(p)
package utils

interface Decision {
    /**
     * 该方法将立刻结束决策，并立刻作出 Accept
     */
    @DecisionDsl
    fun accept(block: () -> Unit = {}): Nothing

    /**
     * 该方法将立刻结束决策，并立刻作出 Reject
     */
    @DecisionDsl
    fun reject(block: () -> Unit = {}): Nothing

    /**
     * 该方法将待决策值设置为 Accept
     */
    @DecisionDsl
    fun setAccept()

    /**
     * 该方法将待决策值设置为 Reject
     */
    @DecisionDsl
    fun setReject()

    /**
     * 该函数将立刻以待决策值作出决策
     */
    @DecisionDsl
    fun decide(): Nothing
}

@DslMarker
annotation class DecisionDsl

class DecisionUnmake : Exception()

enum class DecisionEnum {
    Accept, Reject, Undefined
}

private class DecisionDslImpl<R : Any>(
    private val onAccept: () -> R, private val onReject: () -> R, private val making: Decision.() -> Nothing
) : Decision {
    private class DecisionMade : Exception()

    private fun finish(): Nothing = throw DecisionMade()

    private var status: DecisionEnum = DecisionEnum.Undefined

    override fun setAccept() {
        status = DecisionEnum.Accept
    }

    override fun setReject() {
        status = DecisionEnum.Reject
    }

    @DecisionDsl
    override fun accept(block: () -> Unit): Nothing {
        status = DecisionEnum.Accept
        block()
        finish()
    }

    @DecisionDsl
    override fun reject(block: () -> Unit): Nothing {
        status = DecisionEnum.Reject
        block()
        finish()
    }

    override fun decide(): Nothing = finish()

    @Suppress("UNREACHABLE_CODE")
    fun result(): R {
        try {
            making()
        } catch (e: DecisionMade) {
            return when (status) {
                DecisionEnum.Accept -> onAccept()
                DecisionEnum.Reject -> onReject()
                DecisionEnum.Undefined -> throw DecisionUnmake()
            }
        }
        return throw DecisionUnmake()
    }
}

/**
 * makeDecision 允许你结构化，模块化处理复杂的逻辑判断功能
 * * 如果你在决策函数中没有使用 [DecisionDslImpl.decide] 函数，那么[makeDecision] 总能做出有效决定[DecisionEnum.Accept],[DecisionEnum.Reject]，不会抛出异常
 * * 如果你在决策函数中使用了[DecisionDslImpl.decide] 函数，那么如果之前没有执行任何的[DecisionDslImpl.setAccept],[DecisionDslImpl.setReject]更改待决策值，那么此时会抛出[DecisionUnmake]异常
 */
@Throws(DecisionUnmake::class)
fun <R : Any> makeDecision(onAccept: () -> R, onReject: () -> R, making: Decision.() -> Nothing) =
    DecisionDslImpl(onAccept, onReject, making).result()

/**
 * 这是一个 Boolean 类型的 makeDecision 的实现
 *  * 在 onAccept 返回时 true
 *  * 在 onReject 返回 false
 * #### makeDecision 允许你结构化，模块化处理复杂的逻辑判断功能
 * * 如果你在决策函数中没有使用 [DecisionDslImpl.decide] 函数，那么[makeDecision] 总能做出有效决定[DecisionEnum.Accept],[DecisionEnum.Reject]，不会抛出异常
 * * 如果你在决策函数中使用了[DecisionDslImpl.decide] 函数，那么如果之前没有执行任何的[DecisionDslImpl.setAccept],[DecisionDslImpl.setReject]更改待决策值，那么此时会抛出[DecisionUnmake]异常
 */
@Throws(DecisionUnmake::class)
fun makeDecision(making: Decision.() -> Nothing): Boolean =
    DecisionDslImpl(onAccept = { true }, onReject = { false }, making
    ).result()

fun <T> Sequence<T>.filterDecision(making: Decision.(T) -> Nothing): Sequence<T> = filter {
    makeDecision { making(it) }
}
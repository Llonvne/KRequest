package utils

data class OnRejectDsl(var rejection: (() -> Unit)? = null) {
    fun onReject(block: () -> Unit) {
        rejection = block
    }
}

data class OnAcceptDsl(var acception: (() -> Unit)? = null) {
    fun onAccept(block: () -> Unit) {
        acception = block
    }
}

interface Decision {
    /**
     * 如果 [predicate] 为真作出 Accept 决策
     * 否则不做任何事
     */
    @DecisionDsl
    fun acceptIf(predicate: OnAcceptDsl.() -> Boolean) {
        val acceptDsl = OnAcceptDsl()
        if (predicate(acceptDsl)) {
            reject {
                acceptDsl.acception?.invoke()
            }
        } else { /* do Nothing*/
        }
    }

    /**
     * 如果 [predicate] 为真作出 Reject 决策
     * 否则不做任何事
     */
    @DecisionDsl
    fun rejectIf(predicate: OnRejectDsl.() -> Boolean) {
        val rejectDsl = OnRejectDsl()
        if (predicate(rejectDsl)) {
            reject {
                rejectDsl.rejection?.invoke()
            }
        } else { /* do Nothing*/
        }
    }

    /**
     * 如果 [predicate] 为真作出 Accept 决策
     * 否则作出 Reject 决策
     */
    @DecisionDsl
    fun decideOn(predicate: context(OnAcceptDsl, OnRejectDsl) () -> Boolean): Nothing {
        val onAc = OnAcceptDsl()
        val onRe = OnRejectDsl()
        if (predicate(onAc, onRe)) {
            accept { onAc.acception?.invoke() }
        } else {
            reject { onRe.rejection?.invoke() }
        }
    }

    /**
     * 该方法将立刻结束决策，并立刻作出 Accept
     */
    @DecisionDsl
    fun accept(next: () -> Unit = {}): Nothing

    /**
     * 该方法将立刻结束决策，并立刻作出 Reject
     */
    @DecisionDsl
    fun reject(next: () -> Unit = {}): Nothing

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

class DecisionUnmake : Exception()

@DslMarker
private annotation class DecisionDsl

private class DecisionDslImpl<R : Any>(
    private val onAccept: () -> R, private val onReject: () -> R, private val making: Decision.() -> Nothing
) : Decision {
    private enum class DecisionEnum {
        Accept, Reject, Undefined
    }

    private class DecisionMade : Exception()

    private fun finish(): Nothing = throw DecisionMade()

    private var status: DecisionEnum = DecisionEnum.Undefined

    @DecisionDsl
    override fun setAccept() {
        status = DecisionEnum.Accept
    }

    @DecisionDsl
    override fun setReject() {
        status = DecisionEnum.Reject
    }

    @DecisionDsl
    override fun accept(next: () -> Unit): Nothing {
        status = DecisionEnum.Accept
        next()
        finish()
    }

    @DecisionDsl
    override fun reject(next: () -> Unit): Nothing {
        status = DecisionEnum.Reject
        next()
        finish()
    }

    @DecisionDsl
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

fun <T> Sequence<T>.filterDecision(making: Decision.(T) -> Nothing): Sequence<T> =
    filter { makeDecision { making(it) } }
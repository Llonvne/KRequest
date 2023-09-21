package context

import Apis
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

/**
 * [ApiBuildContext] 上下文是为了保存一下两个对象的引用
 * * [func] 构建依赖于 [Apis] 的拓展函数
 * * [cls] 构建 Api 实现类
 */
data class ApiBuildContext(
    val func: FunSpec.Builder,
    val cls: TypeSpec.Builder
) {
    fun scoped(block: context(ApiBuildContext) () -> Unit) = block(this)
}
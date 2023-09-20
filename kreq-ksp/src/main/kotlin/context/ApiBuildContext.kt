package context

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

data class ApiBuildContext(
    val func: FunSpec.Builder,
    val cls: TypeSpec.Builder
) {
    fun scoped(block: context(ApiBuildContext) () -> Unit) = block(this)
}
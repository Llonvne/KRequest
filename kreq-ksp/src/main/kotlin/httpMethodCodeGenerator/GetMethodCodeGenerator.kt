package httpMethodCodeGenerator

import com.squareup.kotlinpoet.FunSpec.Builder
import context.HttpMethodBuildContext
import context.SymbolProcessorContext

context (SymbolProcessorContext, context.ApiBuildContext, Builder)
class GetMethodCodeGenerator(private val httpMethodBuildContext: HttpMethodBuildContext) {
    fun resolve() {
        getMethod()
        resolveUrl(httpMethodBuildContext)
    }

    private fun getMethod() = addStatement(".get()")
}

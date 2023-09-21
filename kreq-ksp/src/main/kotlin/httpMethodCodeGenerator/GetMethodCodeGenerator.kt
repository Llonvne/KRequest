package httpMethodCodeGenerator

import com.squareup.kotlinpoet.FunSpec.Builder
import context.HttpMethodBuildContext
import context.SymbolProcessorContext

class GetMethodCodeGenerator(private val httpMethodBuildContext: HttpMethodBuildContext) : HttpMethodCodeGenerator {
    context (SymbolProcessorContext, context.ApiBuildContext, Builder)
    override fun resolve() {
        getMethod()
        resolveUrl(httpMethodBuildContext)
    }

    override fun supportMethod(): HttpMethods {
        return HttpMethods.GET
    }

    private fun Builder.getMethod() = addStatement(".get()")
}

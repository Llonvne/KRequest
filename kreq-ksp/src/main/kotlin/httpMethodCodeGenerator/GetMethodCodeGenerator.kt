package httpMethodCodeGenerator

import com.squareup.kotlinpoet.FunSpec.Builder
import context.HttpMethodBuildContext
import context.SymbolProcessorContext
import url.DefaultUrlResolver

class GetMethodCodeGenerator(
    private val httpCtx: HttpMethodBuildContext,
    private val urlResolver: DefaultUrlResolver = DefaultUrlResolver()
) : HttpMethodCodeGenerator {

    context (SymbolProcessorContext, context.ApiBuildContext, Builder)
    override fun resolve() {
        getMethod()
        urlResolver.resolve(httpCtx)
    }

    override fun supportMethod(): HttpMethods {
        return HttpMethods.GET
    }

    private fun Builder.getMethod() = addStatement(".get()")
}

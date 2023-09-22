package httpMethodCodeGenerator

import com.squareup.kotlinpoet.FunSpec
import context.ApiBuildContext
import context.HttpMethodBuildContext
import context.SymbolProcessorContext
import url.DefaultUrlResolver

class DeleteMethodCodeGenerator(
    ctx: HttpMethodBuildContext,
    private val urlResolver: DefaultUrlResolver = DefaultUrlResolver()
) : HttpMethodCodeGenerator {
    context(SymbolProcessorContext, ApiBuildContext, FunSpec.Builder) override fun resolve() {
        throw NotImplementedError()
    }

    override fun supportMethod(): HttpMethods = HttpMethods.DELETE
}

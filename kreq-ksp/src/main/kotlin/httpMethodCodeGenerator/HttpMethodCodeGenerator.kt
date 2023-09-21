package httpMethodCodeGenerator

import com.squareup.kotlinpoet.FunSpec

context (context.SymbolProcessorContext, context.ApiBuildContext, FunSpec.Builder)
interface HttpMethodCodeGenerator {
    fun resolve()

    fun supportMethod(): HttpMethods
}
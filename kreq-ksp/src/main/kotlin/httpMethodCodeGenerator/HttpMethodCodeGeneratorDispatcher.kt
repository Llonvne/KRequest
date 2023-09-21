package httpMethodCodeGenerator

import com.squareup.kotlinpoet.FunSpec
import context.ApiBuildContext
import context.HttpMethodBuildContext
import context.SymbolProcessorContext

class HttpMethodCodeGeneratorDispatcher {
    context (SymbolProcessorContext, ApiBuildContext, FunSpec.Builder)
    fun dispatch(httpCtx: HttpMethodBuildContext) = when (httpCtx.httpMethod) {
        HttpMethods.GET -> {
            GetMethodCodeGenerator(httpCtx).resolve()
        }

        HttpMethods.POST -> {
            PostMethodCodeGenerator(httpCtx).resolve()
        }
    }
}
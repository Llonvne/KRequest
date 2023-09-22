package httpMethodCodeGenerator

import context.HttpMethodBuildContext
import utils.simpleName

enum class HttpMethods(val methodGenerator: (HttpMethodBuildContext) -> HttpMethodCodeGenerator) {
    GET({ ctx -> GetMethodCodeGenerator(ctx) }), POST({ ctx -> PostMethodCodeGenerator(ctx) })
    ,DELETE({ctx -> DeleteMethodCodeGenerator(ctx)})
}

val HttpMethodBuildContext.httpMethod get() = HttpMethods.valueOf(httpMethodAnnotation.simpleName)
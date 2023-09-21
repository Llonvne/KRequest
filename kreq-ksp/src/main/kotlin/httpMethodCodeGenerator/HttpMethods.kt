package httpMethodCodeGenerator

import context.HttpMethodBuildContext
import utils.simpleName

enum class HttpMethods {
    GET, POST
}

val HttpMethodBuildContext.httpMethod get() = HttpMethods.valueOf(httpMethodAnnotation.simpleName)
package context

import Constants
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import utils.uri
import utils.useVar

/**
 * 该上下文是为了保存构建对应 Http 请求内容的
 * * [declaration] Api 接口的声明函数
 * * [functionAnnotation] 在该[declaration]上面的注解
 * * [httpMethodAnnotation] 该注解指示了HttpMethod
 * * [parameters] 该参数是 [declaration] 的参数
 */
data class HttpMethodBuildContext(
    val declaration: KSFunctionDeclaration,
    val functionAnnotation: List<KSAnnotation>,
    val httpMethodAnnotation: KSAnnotation,
    val parameters: List<KSValueParameter>
) {
    operator fun invoke(block: context(HttpMethodBuildContext) () -> Unit) = block(this)
}

val HttpMethodBuildContext.buildUrl get() = useVar(Constants.BASE_URL_VAR) + httpMethodAnnotation.arguments.uri

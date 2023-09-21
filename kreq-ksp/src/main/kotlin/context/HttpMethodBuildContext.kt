package context

import Constants
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import utils.uri
import utils.useVar

data class HttpMethodBuildContext(
    val declaration: KSFunctionDeclaration,
    val functionAnnotation: List<KSAnnotation>,
    val httpMethodAnnotation: KSAnnotation,
    val parameters: List<KSValueParameter>
) {
    fun scoped(block: context(HttpMethodBuildContext) () -> Unit) = block(this)
}

val HttpMethodBuildContext.buildUrl get() = useVar(Constants.BASE_URL_VAR) + httpMethodAnnotation.arguments.uri

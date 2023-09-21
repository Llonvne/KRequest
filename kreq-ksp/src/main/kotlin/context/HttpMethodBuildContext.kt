package context

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter

data class HttpMethodBuildContext(
    val declaration: KSFunctionDeclaration,
    val functionAnnotation: List<KSAnnotation>,
    val httpMethodAnnotation: KSAnnotation,
    val parameters: List<KSValueParameter>
) {
    fun scoped(block: context(HttpMethodBuildContext) () -> Unit) = block(this)
}
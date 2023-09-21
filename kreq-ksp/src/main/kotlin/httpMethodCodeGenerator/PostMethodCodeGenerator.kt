package httpMethodCodeGenerator

import PostBody
import PostBodyMoreThanOneException
import PostBodyNeededException
import com.google.devtools.ksp.symbol.KSValueParameter
import context.HttpMethodBuildContext
import context.SymbolProcessorContext
import utils.getClassDeclarationByNameOrException

context (SymbolProcessorContext, context.ApiBuildContext, com.squareup.kotlinpoet.FunSpec.Builder)
open class PostMethodCodeGenerator(private val methodCtx: HttpMethodBuildContext) {
    fun resolve() = methodCtx.scoped {
        postMethod()
        resolveUrl(methodCtx)
    }

    context (HttpMethodBuildContext)
    private fun postMethod() = addStatement(".post(%L)", resolvePostBodyVarName())

    private val requestBodyDecl =
        resolver.getClassDeclarationByNameOrException("okhttp3.RequestBody").asStarProjectedType()

    private fun KSValueParameter.hasPostBodyAnnotation() =
        annotations.filter { anno -> anno.isSameWith<PostBody>() }.toList().isNotEmpty()

    private fun KSValueParameter.typeIsRequestBody() = type.resolve().isAssignableFrom(requestBodyDecl)
    context (HttpMethodBuildContext)
    private fun resolvePostBodyVarName(): String {
        val matchedParameters = methodCtx.parameters
            .filter { it.hasPostBodyAnnotation() && it.typeIsRequestBody() }
        return when (matchedParameters.size) {
            1 -> matchedParameters[0].name?.getShortName()!!
            else -> invalidPostBody(matchedParameters)
        }
    }

    /**
     * 该方法允许你重写但遇到多个或者零个 PostBody 的情形
     * 默认情况下，找不到时将抛出 [PostBodyNeededException]
     * 超过一个的时候抛出 [PostBodyMoreThanOneException]
     */
    context (HttpMethodBuildContext)
    protected open fun invalidPostBody(hasPostBodyParameters: List<KSValueParameter>): Nothing =
        when (val size = hasPostBodyParameters.size) {
            0 -> throw PostBodyNeededException(declaration)
            else -> throw PostBodyMoreThanOneException(size, declaration)
        }
}




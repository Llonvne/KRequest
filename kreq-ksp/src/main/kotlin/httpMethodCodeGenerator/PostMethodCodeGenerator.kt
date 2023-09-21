package httpMethodCodeGenerator

import PostBodyMoreThanOneException
import PostBodyNeededException
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.FunSpec
import context.HttpMethodBuildContext
import context.SymbolProcessorContext
import utils.hasPostBodyAnnotation
import utils.typeIsRequestBody

open class PostMethodCodeGenerator(private val methodCtx: HttpMethodBuildContext) : HttpMethodCodeGenerator {
    context (SymbolProcessorContext, context.ApiBuildContext, FunSpec.Builder)
    override fun resolve() = methodCtx {
        postMethod()
        resolveUrl(methodCtx)
    }

    override fun supportMethod() = HttpMethods.POST

    context (SymbolProcessorContext, HttpMethodBuildContext)
    private fun FunSpec.Builder.postMethod() = addStatement(".post(%L)", resolvePostBodyVarName())

    context (SymbolProcessorContext, HttpMethodBuildContext)
    private fun resolvePostBodyVarName(): String {
        val matchedParameters = methodCtx.parameters
            .filter { it.hasPostBodyAnnotation() && it.typeIsRequestBody(resolver) }
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




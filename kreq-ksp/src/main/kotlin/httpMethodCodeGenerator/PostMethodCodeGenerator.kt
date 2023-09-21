package httpMethodCodeGenerator

import PostBody
import com.google.devtools.ksp.getClassDeclarationByName
import context.AnnotationContext
import context.SymbolProcessorContext

context (SymbolProcessorContext, context.ApiBuildContext, com.squareup.kotlinpoet.FunSpec.Builder)
class PostMethodCodeGenerator(private val annotationContext: AnnotationContext) {
    fun resolve() {
        addStatement(".post(%L)", resolvePostBodyVarName())
        annotationContext.resolveUrl()
    }

    private val requestBodyDecl = resolver.getClassDeclarationByName("okhttp3.RequestBody")?.asStarProjectedType()!!

    private fun resolvePostBodyVarName(): String {
        val matchedParameters = annotationContext.parameters
            .filter {
                it.annotations.filter {
                    it.isSameWith<PostBody>()
                }.toList().isNotEmpty()
                        &&
                        it.type.resolve().isAssignableFrom(requestBodyDecl)
            }
        return when (matchedParameters.size) {
            1 -> matchedParameters[0].name?.getShortName()!!
            else -> throw IllegalArgumentException("PostBody 注解数量为 ${matchedParameters.size} 需要为 1")
        }
    }
}
package httpMethodCodeGenerator

import com.squareup.kotlinpoet.FunSpec.Builder
import context.AnnotationContext
import context.SymbolProcessorContext

context (SymbolProcessorContext, context.ApiBuildContext, Builder)
class GetMethodCodeGenerator(private val annotationContext: AnnotationContext) {
    fun resolve() {
        getMethod()
        annotationContext.resolveUrl()
    }

    private fun getMethod() = addStatement(".get()")
}

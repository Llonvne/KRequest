package httpMethodCodeGenerator

import com.squareup.kotlinpoet.FunSpec.Builder
import context.AnnotationContext

context (context.LlonvneSymbolProcessorContext, context.ApiBuildContext, Builder)
class GetMethodCodeGenerator(private val annotationContext: AnnotationContext) {
    fun resolve() {
        addStatement(".get()")
        annotationContext.resolveUrl()
    }
}

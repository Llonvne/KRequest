package context

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSValueParameter

data class AnnotationContext(
    val functionAnnotation: List<KSAnnotation>,
    val httpMethodAnnotation: KSAnnotation,
    val parameters: List<KSValueParameter>
)
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import context.AnnotationContext
import httpMethodCodeGenerator.GetMethodCodeGenerator
import httpMethodCodeGenerator.PostMethodCodeGenerator

context (context.LlonvneSymbolProcessorContext, context.ApiBuildContext)
class ApiFuncResolver(
    private val api: KSClassDeclaration,
    private val decl: KSFunctionDeclaration
) {
    fun resolve() {
        assertAnnotatedWithHttpMethodImpl()
        val functionAnnotations = apiFunctionLevelAnnotations()
        val httpMethodAnnotation = extractHttpMethod()
        val annotationContext = AnnotationContext(functionAnnotations, httpMethodAnnotation, decl.parameters)
        val impl = FunSpec.builder(decl.simpleName.asString())
            .returnSameAs(decl)
            .setOverride()
            .importParameters(decl)
            .buildRequest(annotationContext)

        cls.addFunction(impl.build())
    }

    private val okHttpRequestBuilderFqName = "okhttp3.Request.Builder"
    private fun FunSpec.Builder.buildRequest(annotationContext: AnnotationContext) = apply {
        initializeRequestObject()
        dispatchOnHttpMethod(annotationContext)
    }

    private fun FunSpec.Builder.initializeRequestObject() {
        addStatement("%T()", resolver.getClassDeclarationByName(okHttpRequestBuilderFqName)?.toClassName()!!)
    }

    private fun FunSpec.Builder.dispatchOnHttpMethod(annotationContext: AnnotationContext) {
        when (annotationContext.httpMethodStrRepresent) {
            "get" -> {
                GetMethodCodeGenerator(annotationContext).resolve()
            }

            "post" -> {
                PostMethodCodeGenerator(annotationContext).resolve()
            }
        }
    }

    private val AnnotationContext.httpMethodStrRepresent: String
        get() = httpMethodAnnotation.annotationDeclaration().simpleName.asString().lowercase()


    private fun extractHttpMethod(): KSAnnotation = decl.annotations
        .toList().first { httpMethodQualifiedNameSet.contains(it.annotationDeclaration().qualifiedName?.asString()) }

    private val httpMethodQualifiedNameSet: Set<String> = listOf(
        GET::class,
        POST::class
    ).mapNotNull { it.qualifiedName }.toSet()

    private fun apiFunctionLevelAnnotations() = decl.annotations.toList()

    @Suppress("unchecked_cast")
    private fun assertAnnotatedWithHttpMethodImpl() {
        val annotations = apiFunctionLevelAnnotations()
            .flatMap { ksAnnotation ->
                ksAnnotation.annotationDeclaration().annotations.filter {
                    it.annotationDeclaration().qualifiedName?.asString() == qualifiedName<HttpMethod>()
                }
            }.toList().ifEmpty {
                logger.exception(ApiMemberFunctionMustAnnotatedWithHttpMethodAnnotation(api, decl))
            } as List<KSAnnotation>
        if (annotations.size > 1) {
            throw HttpMethodShouldBeUniqueOnOneMethod(annotations, decl)
        }
    }

    private fun KSAnnotation.annotationDeclaration() = annotationType.resolve().declaration

    private inline fun <reified Type> qualifiedName(): String? = Type::class.qualifiedName
}

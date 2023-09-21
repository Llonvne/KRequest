import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import context.HttpMethodBuildContext
import httpMethodCodeGenerator.GetMethodCodeGenerator
import httpMethodCodeGenerator.PostMethodCodeGenerator
import httpMethodCodeGenerator.finishRequestBuild
import utils.*

context (context.SymbolProcessorContext, context.ApiBuildContext)
class ApiFuncResolver(
    private val api: KSClassDeclaration,
    private val decl: KSFunctionDeclaration
) {
    fun resolve() {
        assertAnnotatedWithHttpMethodImpl()
        val functionAnnotations = apiFunctionLevelAnnotations()
        val httpMethodAnnotation = extractHttpMethod()
        val httpMethodBuildContext =
            HttpMethodBuildContext(decl, functionAnnotations, httpMethodAnnotation, decl.parameters)

        val impl = FunSpec.builder(decl.simpleName.asString())
            .returnSameAs(decl)
            .setOverride()
            .importParameters(decl)
            .buildRequest(httpMethodBuildContext)
            .finishRequestBuild()
            .buildNewCall()
            .executeCall()
            .buildResponse()
        cls.addFunction(impl.build())
    }

    private fun FunSpec.Builder.buildResponse() = apply {
        if (decl.returnType?.resolve()?.declaration?.qualifiedName?.asString() == "okhttp3.Response") {
            addStatement("return $respVar")
        }
    }

    private fun FunSpec.Builder.executeCall() = addStatement(".execute()")

    private val respVar = "resp"

    private fun FunSpec.Builder.buildNewCall() =
        addStatement("val $respVar = ${Constants.OK_HTTP_CLIENT_VAR}.newCall(request)")

    private fun FunSpec.Builder.buildRequest(httpMethodBuildContext: HttpMethodBuildContext) = apply {
        initializeRequestObject()
        dispatchOnHttpMethod(httpMethodBuildContext)
    }

    private fun FunSpec.Builder.initializeRequestObject() {
        addStatement(
            "val request = %T()",
            resolver.getClassDeclarationByNameOrException(okHttpRequestBuilderFqName).toClassName()
        )
    }

    private fun FunSpec.Builder.dispatchOnHttpMethod(httpMethodBuildContext: HttpMethodBuildContext) {
        when (httpMethodBuildContext.httpMethodStrRepresent) {
            "get" -> {
                GetMethodCodeGenerator(httpMethodBuildContext).resolve()
            }

            "post" -> {
                PostMethodCodeGenerator(httpMethodBuildContext).resolve()
            }
        }
    }

    private val HttpMethodBuildContext.httpMethodStrRepresent: String get() = httpMethodAnnotation.simpleName.lowercase()


    private fun extractHttpMethod(): KSAnnotation = decl.annotations
        .toList().first { httpMethodQualifiedNameSet.contains(it.qualifiedName) }

    @Suppress(Constants.UNCHECKED_CAST)
    private fun assertAnnotatedWithHttpMethodImpl() {
        val annotations = apiFunctionLevelAnnotations()
            .flatMap { ksAnnotation ->
                ksAnnotation.annotations.filter {
                    it.qualifiedName == HttpMethod::class.qualifiedName
                }
            }.toList().ifEmpty {
                logger.exception(ApiMemberFunctionMustAnnotatedWithHttpMethodAnnotation(api, decl))
            } as List<KSAnnotation>
        if (annotations.size > 1) {
            throw HttpMethodShouldBeUniqueOnOneMethod(annotations, decl)
        }
    }


    private val httpMethodQualifiedNameSet: Set<String> = listOf(
        GET::class,
        POST::class
    ).mapNotNull { it.qualifiedName }.toSet()

    private fun apiFunctionLevelAnnotations() = decl.annotations.toList()

    private val okHttpRequestBuilderFqName = "okhttp3.Request.Builder"
}

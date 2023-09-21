import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import context.HttpMethodBuildContext
import httpMethodCodeGenerator.HttpMethodCodeGeneratorDispatcher
import httpMethodCodeGenerator.finishRequestBuild
import utils.*

context (context.SymbolProcessorContext, context.ApiBuildContext)
class ApiFuncResolver(
    private val api: KSClassDeclaration,
    private val decl: KSFunctionDeclaration
) {
    private val httpMethodCodeGeneratorDispatcher = HttpMethodCodeGeneratorDispatcher()

    fun resolve() {
        assertAnnotatedWithHttpMethodImpl()
        val functionAnnotations = apiFunctionLevelAnnotations()
        val httpMethodAnnotation = extractHttpMethod()
        val httpMethodBuildContext =
            HttpMethodBuildContext(decl, functionAnnotations, httpMethodAnnotation, decl.parameters)

        val impl = FunSpec.builder(decl.simpleName.asString()).apply {
            returnSameAs(decl)
            setOverride()
            importParameters(decl)
            buildRequest(httpMethodBuildContext)
            finishRequestBuild()
            buildNewCall(respVar, requestVar)
            executeCall()
            buildResponse()
        }
        cls.addFunction(impl.build())
    }

    private fun FunSpec.Builder.buildResponse() = apply {
        if (decl.isReturnTypeQualifiedNameEquals("okhttp3.Response")) {
            buildReturnStatement(respVar)
        }
    }

    private val respVar = "resp"
    private val requestVar = "request"
    private val okHttpRequestBuilderFqName = "okhttp3.Request.Builder"
    private val okHttpRequestBuilderDecl
        get() = resolver.getClassDeclarationByNameOrException(
            okHttpRequestBuilderFqName
        )


    private fun FunSpec.Builder.buildRequest(httpCtx: HttpMethodBuildContext) = apply {
        addCreateNewInstanceStatement(requestVar, okHttpRequestBuilderDecl.toClassName())
        httpMethodCodeGeneratorDispatcher.dispatch(httpCtx)
    }

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
                logger.exception(ApiMemberFunctionMustAnnotatedWithHttpMethod(api, decl))
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


}

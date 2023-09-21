import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import context.ApiBuildContext
import context.HttpMethodBuildContext
import context.SymbolProcessorContext
import httpMethodCodeGenerator.finishRequestBuild
import httpMethodCodeGenerator.httpMethod
import utils.*

context (SymbolProcessorContext, ApiBuildContext)
class ApiFuncResolver(
    private val api: KSClassDeclaration,
    private val decl: KSFunctionDeclaration
) {
    fun resolve() {
        assertAnnotatedWithHttpMethodImpl()
        val httpMethodCtx = buildHttpCtx()
        val impl = FunSpec.builder(decl.simpleName.asString()) {
            returnSameAs(decl)
            setOverride()
            importParameters(decl)
            buildRequest(httpMethodCtx)
            finishRequestBuild()
            buildNewCall(respVar, requestVar)
            executeCall()
            buildResponse()
        }
        cls.addFunction(impl.build())
    }

    private fun FunSpec.Builder.buildResponse() = apply {
        if (decl.isReturnTypeQualifiedNameEquals(okHttpResponseFqName)) {
            buildReturnStatement(respVar)
        }
    }

    private val respVar = "resp"
    private val requestVar = "request"
    private val okHttpRequestBuilderFqName = "okhttp3.Request.Builder"
    private val okHttpResponseFqName = "okhttp3.Response"
    private val okHttpRequestBuilderDecl
        get() = resolver.getClassDeclarationByNameOrException(okHttpRequestBuilderFqName)

    private fun buildHttpCtx(): HttpMethodBuildContext {
        fun extractHttpMethod(): KSAnnotation = decl.annotations
            .toList().first { httpMethodQualifiedNameSet.contains(it.qualifiedName) }
        return HttpMethodBuildContext(decl, decl.annotations.toList(), extractHttpMethod(), decl.parameters)
    }

    context (SymbolProcessorContext, ApiBuildContext, FunSpec.Builder)
    private fun FunSpec.Builder.buildRequest(httpCtx: HttpMethodBuildContext) = apply {
        addCreateNewInstanceStatement(requestVar, okHttpRequestBuilderDecl.toClassName())
        httpCtx.httpMethod.methodGenerator(httpCtx).resolve()
    }


    @Suppress(Constants.UNCHECKED_CAST)
    private fun assertAnnotatedWithHttpMethodImpl() {
        val annotations = decl.annotations
            .flatMap { anno -> anno.annotations.filterAnnotationType<HttpMethod>() }.toList()
            .ifEmpty { logger.exception(ApiMemberFunctionMustAnnotatedWithHttpMethod(api, decl)) } as List<KSAnnotation>

        if (annotations.size > 1) {
            throw HttpMethodShouldBeUniqueOnOneMethod(annotations, decl)
        }
    }

    private val httpMethodQualifiedNameSet: Set<String> = listOf(
        GET::class,
        POST::class
    ).mapNotNull { it.qualifiedName }.toSet()
}

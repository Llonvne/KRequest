import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import context.ApiBuildContext
import context.HttpMethodBuildContext
import context.SymbolProcessorContext
import httpMethodCodeGenerator.httpMethod
import utils.*

context (SymbolProcessorContext, ApiBuildContext)
open class ApiFuncResolver(
    private val api: KSClassDeclaration, private val decl: KSFunctionDeclaration
) {
    fun resolve() {
        assertAnnotatedWithHttpMethodImpl()
        val httpMethodCtx = buildHttpCtx()
        val impl = FunSpec.builder(decl.simpleName.asString()) {
            func()
            call()
        }
        cls.addFunction(impl.build())
    }

    /**
     * 该函数控制 ApiFunc 修饰符等信息，重写该函数以改变此默认行为
     */
    protected open fun FunSpec.Builder.func() {
        returnSameAs(decl)
        setOverride()
        importParameters(decl)
    }

    /**
     * 该函数控制 request 的建立和执行，重写该函数以改变此默认行为
     */
    protected open fun FunSpec.Builder.request(httpMethodCtx: HttpMethodBuildContext) {
        buildRequest(httpMethodCtx)
        finishRequestBuild()
    }

    /**
     * 该函数控制 Call 的建立和执行，重写该函数以改变此默认行为
     */
    protected open fun FunSpec.Builder.call() {
        buildNewCall(respVar, requestVar)
        executeCall()
        buildResponse()
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
        fun extractHttpMethod(): KSAnnotation =
            decl.annotations.toList().first { httpMethodQualifiedNameSet.contains(it.qualifiedName) }
        return HttpMethodBuildContext(decl, decl.annotations.toList(), extractHttpMethod(), decl.parameters)
    }

    context (SymbolProcessorContext, ApiBuildContext, FunSpec.Builder)
    private fun FunSpec.Builder.buildRequest(httpCtx: HttpMethodBuildContext) = apply {
        addCreateNewInstanceStatement(requestVar, okHttpRequestBuilderDecl.toClassName())
        httpCtx.httpMethod.methodGenerator(httpCtx).resolve()
    }


    @Suppress(Constants.UNCHECKED_CAST)
    private fun assertAnnotatedWithHttpMethodImpl() {
        val annotations = decl.annotations.flatMap { anno -> anno.annotations.filterType<HttpMethod>() }.toList()
            .ifEmpty { logger.exception(ApiMemberFunctionMustAnnotatedWithHttpMethod(api, decl)) } as List<KSAnnotation>

        if (annotations.size > 1) {
            throw HttpMethodShouldBeUniqueOnOneMethod(annotations, decl)
        }
    }

    private val httpMethodQualifiedNameSet: Set<String> = listOf(
        GET::class, POST::class
    ).mapNotNull { it.qualifiedName }.toSet()
}

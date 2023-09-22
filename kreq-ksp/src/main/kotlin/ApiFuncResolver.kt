import Constants.requestVar
import Constants.respVar
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import context.ApiBuildContext
import context.HttpMethodBuildContext
import context.SymbolProcessorContext
import context.buildHttpCtx
import httpMethodCodeGenerator.httpMethod
import utils.*

context (SymbolProcessorContext, ApiBuildContext)
open class ApiFuncResolver(
    private val api: KSClassDeclaration, private val decl: KSFunctionDeclaration
) {
    fun resolve() {
        decl.assertAnnotatedWithHttpMethodImpl(api)
        val httpMethodCtx = buildHttpCtx(decl)
        val impl = buildFun(decl.simpleName.asString()) {
            buildApiFunc()
            buildRequest(httpMethodCtx)
            buildCall()
        }
        cls.addFunction(impl.build())
    }

    /**
     * 该函数控制 ApiFunc 修饰符等信息，重写该函数以改变此默认行为
     */
    protected open fun FunBuilder.buildApiFunc() {
        if (decl.isSuspend()) {
            setSuspend()
        }
        buildFuncReturn()
        setOverride()
        importParameters(decl)
    }

    private fun FunBuilder.buildFuncReturn() {
        if (decl.returnTypeIsResponse) {
            returnSame(decl)
        } else {
            decl.assertCustomizeReturnTypeShouldBeNullable()
            returnSame(decl, true)
        }
    }


    /**
     * 该函数控制 request 的建立和执行，重写该函数以改变此默认行为
     */
    protected open fun FunBuilder.buildRequest(httpCtx: HttpMethodBuildContext) {
        buildRequest(httpCtx)
        finishRequestBuild()
    }

    /**
     * 该函数控制 Call 的建立和执行，重写该函数以改变此默认行为
     */
    protected open fun FunBuilder.buildCall() {
        buildNewCall(respVar, requestVar)
        executeCall()
        buildResponse()
    }

    private fun FunBuilder.buildResponse() {
        handleSuspend {
            if (decl.returnTypeIsResponse) {
                buildReturnStatement(respVar, withReturnKeyword = it)
            } else {
                buildReturnStatement("converter($respVar)", withReturnKeyword = it)
            }
        }
    }

    private fun FunSpec.Builder.handleSuspend(block: FunSpec.Builder.(needReturnKeyWord: Boolean) -> Unit) {
        val isSuspend = decl.isSuspend()
        if (isSuspend) {
            addStatement("return suspended{")
        }
        block(!isSuspend)
        if (isSuspend) {
            addStatement("}")
        }
    }

    context (SymbolProcessorContext, ApiBuildContext, FunSpec.Builder)
    private fun FunSpec.Builder.buildRequest(httpCtx: HttpMethodBuildContext) = apply {
        addCreateNewInstanceStatement(requestVar, okHttpRequestBuilderDecl.toClassName())
        httpCtx.httpMethod.methodGenerator(httpCtx).resolve()
    }
}

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import context.ApiBuildContext
import context.SymbolProcessorContext
import exception.AlreadyRegisterFunctionInCacheException
import utils.*
import java.util.function.Supplier

open class KotlinPoetResolver(
    private val environment: SymbolProcessorEnvironment, filename: String = "ApisExtensions"
) {
    private val file = FileSpec.builder("", filename)
    private val apiCache = mutableMapOf<KSClassDeclaration, ApiBuildContext>()

    /**
     * 该函数将会将给定的 [api] 在生成的文件中生成一个拓展函数和一个实现类
     * * 拓展函数 [Apis] 定义拓展函数，名字与 [Api] 类名相同，具有 baseUrl 和 okHttpClient 参数,将会调用实现类的主构造器，并返回[api]类型
     * * 实现类 类名 Api的类型+"Impl" 主构造函数函数为 baseUrl 和 okHttpClient
     */
    context (SymbolProcessorContext)
    fun registerApi(api: KSClassDeclaration, scoped: context(ApiBuildContext) () -> Unit) {
        assertIsApiAnnotated(api)
        assertNotRegistered(api)

        val okHttpClientDecl = resolver.getClassDeclarationByNameOrException("okhttp3.OkHttpClient")

        val func = FunSpec.builder(api.toClassName().simpleName) {
            useApisAsReceiver()
            addBaseUrlParameter()
            addOkHttpClientParameter(okHttpClientDecl)
            addStatementReturnApiImplWithBaseUrl(api)
            returnApiType(api)
        }

        val type = TypeSpec.classBuilder(api.apiImplClassName()) {
            setApiSuperInterface(api)
            setPrivateModifier()
            addPrimaryConstructorWithBaseUrlAndOkHttpClientParameter(okHttpClientDecl)
            addGeneratedAnnotation()
        }

        createCachedApiContext(api) { ApiBuildContext(func, type) }.scoped(scoped)
    }

    private fun createCachedApiContext(
        api: KSClassDeclaration, ctxSupplier: Supplier<ApiBuildContext>
    ): ApiBuildContext {
        val ctx = ctxSupplier.get()
        apiCache[api] = ctx
        return ctx
    }

    /**
     * 在文件中写入预定义的函数和类型
     */
    fun generate() {
        apiCache.values.forEach {
            file.addFunction(it.func.build())
            file.addType(it.cls.build())
        }
        return file.build().writeTo(environment.codeGenerator, Dependencies(false))
    }

    /**
     * 返回 api 实现类默认的名字
     * 默认实现是 api类名 + "impl"
     * 重写该方法来覆盖此行为
     */
    context (SymbolProcessorContext)
    protected open fun KSClassDeclaration.apiImplClassName(): String {
        assertIsApiAnnotated(this)
        return "${simpleName.asString()}Impl"
    }

    /**
     * 在拓展函数返回实现类实体
     * 模式为: return 实现类类名(baseUrl,okHttpClient)
     */
    context (SymbolProcessorContext)
    private fun FunSpec.Builder.addStatementReturnApiImplWithBaseUrl(api: KSClassDeclaration) =
        addStatement("return ${api.apiImplClassName()}(${Constants.BASE_URL_VAR},${Constants.OK_HTTP_CLIENT_VAR})")

    /**
     * 构造实现类主构造器
     */
    private fun TypeSpec.Builder.addPrimaryConstructorWithBaseUrlAndOkHttpClientParameter(okHttpClientDecl: KSClassDeclaration) {
        buildPrimaryConstruct {
            addBaseUrlParameter()
            addOkHttpClientParameter(okHttpClientDecl)
        }

        addBaseUrlPropertyWithInitializer()
        addOkHttpClientPropertyWithInitializer(okHttpClientDecl)
    }

    /**
     * 该函数用于确保该 [api] 没有在 apiCache 注册，否则将抛出 [AlreadyRegisterFunctionInCacheException]
     * @throws AlreadyRegisterFunctionInCacheException
     */
    private fun assertNotRegistered(api: KSClassDeclaration) {
        if (apiCache.contains(api)) {
            throw AlreadyRegisterFunctionInCacheException(api)
        }
    }
}



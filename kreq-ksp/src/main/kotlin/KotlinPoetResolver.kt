import com.google.devtools.ksp.getClassDeclarationByName
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
import java.util.function.Supplier

class KotlinPoetResolver(
    private val environment: SymbolProcessorEnvironment, filename: String = "ApisExtensions"
) {
    private val file = FileSpec.builder("", filename)
    private val apiCache = mutableMapOf<KSClassDeclaration, ApiBuildContext>()

    context (SymbolProcessorContext)
    fun registerApi(
        api: KSClassDeclaration, scoped: context(ApiBuildContext) () -> Unit
    ) {
        assertIsApiAnnotated(api)
        assertNotRegistered(api)

        val okHttpClientDecl =
            resolver.getClassDeclarationByName("okhttp3.OkHttpClient") ?: throw OkHttpClientNotFoundException()

        val func = FunSpec.builder(api.toClassName().simpleName)

        func.useApisAsReceiver()
            .addBaseUrlParameter()
            .addOkHttpClientParameter(okHttpClientDecl)
            .addStatementReturnApiImplWithBaseUrl(api)
            .returnApiType(api)

        val type = TypeSpec.classBuilder(api.apiImplClassName())
            .setApiSuperInterface(api)
            .setPrivateType()
            .addPrimaryConstructorWithBaseUrlAndOkHttpClientParameter(okHttpClientDecl)


        createCachedApiContext(api) { ApiBuildContext(func, type) }
            .scoped(scoped)
    }

    private fun createCachedApiContext(
        api: KSClassDeclaration,
        ctxSupplier: Supplier<ApiBuildContext>
    ): ApiBuildContext {
        val ctx = ctxSupplier.get()
        apiCache[api] = ctx
        return ctx
    }

    fun generate() {
        apiCache.values.forEach {
            file.addFunction(it.func.build())
            file.addType(it.cls.build())
        }
        return file.build().writeTo(environment.codeGenerator, Dependencies(false))
    }

    context (SymbolProcessorContext)
    private fun KSClassDeclaration.apiImplClassName(): String {
        assertIsApiAnnotated(this)
        return "${simpleName.asString()}Impl"
    }

    context (SymbolProcessorContext)
    private fun FunSpec.Builder.addStatementReturnApiImplWithBaseUrl(api: KSClassDeclaration) =
        addStatement("return ${api.apiImplClassName()}(${Constants.BASE_URL_VAR},${Constants.OK_HTTP_CLIENT_VAR})")

    private fun TypeSpec.Builder.addPrimaryConstructorWithBaseUrlAndOkHttpClientParameter(okHttpClientDecl: KSClassDeclaration) =
        apply {
            primaryConstructor(
                FunSpec.constructorBuilder().addBaseUrlParameter().addOkHttpClientParameter(okHttpClientDecl).build()
            ).addBaseUrlPropertyWithInitializer().addOkHttpClientPropertyWithInitializer(okHttpClientDecl)
        }

    private fun FunSpec.Builder.addOkHttpClientParameter(okHttpClientDecl: KSClassDeclaration) =
        addParameter(Constants.OK_HTTP_CLIENT_VAR, okHttpClientDecl.toClassName())

    private fun assertNotRegistered(api: KSClassDeclaration) {
        if (apiCache.contains(api)) {
            throw AlreadyRegisterFunctionInCacheException(api)
        }
    }
}



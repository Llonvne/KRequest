import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import context.SymbolProcessorContext
import utils.getSymbolsWithAnnotation
import utils.isInterface

open class KRequestProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    private val poetResolver = KotlinPoetResolver(env)

    /**
     * 该方法在 [process] 内部用于创建 [SymbolProcessorContext]
     * 由于 [logger],[env],[postResolver]不变，每次只需要一个[resolver] 即可创建新的环境
     */
    private fun createProcessorContext(resolver: Resolver, block: SymbolProcessorContext.() -> List<KSAnnotated>) =
        SymbolProcessorContext(resolver, poetResolver, env, env.logger).block()

    /**
     * 该方法创建新的[SymbolProcessorContext]环境，并使用[extractValidApiInterfaces]提取有效的Api接口，
     * 建立[ApiResolver]调用[ApiResolver.resolve] 方法处理
     */
    override fun process(resolver: Resolver) = createProcessorContext(resolver) {
        resolver.getSymbolsWithAnnotation<Api>()
            .filterInterface()
            .map { ApiResolver(it) }
            .onEach(ApiResolver::resolve)
            .toList()
        emptyList()
    }

    /**
     * 在所有符号处理完毕后，调用[poetResolver]完成代码生成
     */
    override fun finish() {
        poetResolver.generate()
    }

    context(SymbolProcessorContext)
    @Suppress(Constants.UNCHECKED_CAST)
    private fun Sequence<KSAnnotated>.filterInterface(): Sequence<KSClassDeclaration> {
        return filter { annotated ->
            if (annotated is KSClassDeclaration && annotated.isInterface) {
                true
            } else {
                processUnValidApiAnnotatedValue(annotated)
                false
            }
        } as Sequence<KSClassDeclaration>
    }

    /**
     * 重写该方法允许你重写当遇到非标准元素的行为
     * 默认行为是抛出[InvalidApiDeclarationException]异常
     */
    context(SymbolProcessorContext)
    protected open fun processUnValidApiAnnotatedValue(annotated: KSAnnotated) {
        logger.exception(InvalidApiDeclarationException(annotated))
    }
}
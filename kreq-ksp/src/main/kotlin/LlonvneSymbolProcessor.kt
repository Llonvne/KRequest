import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import context.LlonvneSymbolProcessorContext

class LlonvneSymbolProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val logger = environment.logger
    private val apisExtensionsFileResolver = ApisExtensionsFileResolver(environment)


    override fun process(resolver: Resolver): List<KSAnnotated> =
        LlonvneSymbolProcessorContext.scope(resolver, apisExtensionsFileResolver, environment) {
            resolver
                .extractValidApiInterfaces()
                .map { ApiResolver(it) }
                .onEach(ApiResolver::resolve)
                .toList()
            emptyList()
        }

    override fun finish() {
        apisExtensionsFileResolver.generate()
    }

    private fun Resolver.extractValidApiInterfaces(): Sequence<KSClassDeclaration> =
        getSymbolsWithAnnotation<Api>().filterIsInterface()

    @Suppress("unchecked_cast")
    private fun Sequence<KSAnnotated>.filterIsInterface(): Sequence<KSClassDeclaration> {
        return filter {
            if (it is KSClassDeclaration && it.classKind == ClassKind.INTERFACE) {
                true
            } else {
                processUnValidApiAnnotatedValue(it)
                false
            }
        } as Sequence<KSClassDeclaration>
    }

    private fun processUnValidApiAnnotatedValue(value: KSAnnotated) {
        logger.exception(InvalidApiDeclarationException(value))
    }

    private inline fun <reified T> Resolver.getSymbolsWithAnnotation() =
        getSymbolsWithAnnotation(T::class.java.name)
}
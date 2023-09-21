import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import context.SymbolProcessorContext
import context.scoped

class LlonvneSymbolProcessor(private val env: SymbolProcessorEnvironment) : SymbolProcessor {
    private val logger = env.logger
    private val poetResolver = KotlinPoetResolver(env)


    override fun process(resolver: Resolver): List<KSAnnotated> {
        SymbolProcessorContext(resolver, poetResolver, env, env.logger).scoped {
            resolver
                .extractValidApiInterfaces()
                .map { ApiResolver(it) }
                .onEach(ApiResolver::resolve)
                .toList()
        }
        return emptyList()
    }

    override fun finish() {
        poetResolver.generate()
    }

    private fun Resolver.extractValidApiInterfaces(): Sequence<KSClassDeclaration> {

        fun processUnValidApiAnnotatedValue(value: KSAnnotated) {
            logger.exception(InvalidApiDeclarationException(value))
        }

        @Suppress(Constants.UNCHECKED_CAST)
        fun Sequence<KSAnnotated>.filterInterface(): Sequence<KSClassDeclaration> {
            return filter {
                if (it is KSClassDeclaration && it.classKind == ClassKind.INTERFACE) {
                    true
                } else {
                    processUnValidApiAnnotatedValue(it)
                    false
                }
            } as Sequence<KSClassDeclaration>
        }

        return getSymbolsWithAnnotation<Api>().filterInterface()
    }
}
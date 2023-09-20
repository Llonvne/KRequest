package context

import ApisExtensionsFileResolver
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

data class LlonvneSymbolProcessorContext(
    val resolver: Resolver,
    val fileResolver: ApisExtensionsFileResolver,
    val environment: SymbolProcessorEnvironment,
    val logger: KSPLogger
) {
    companion object {
        fun <R> scope(
            resolver: Resolver,
            fileResolver: ApisExtensionsFileResolver,
            environment: SymbolProcessorEnvironment,
            scoped: LlonvneSymbolProcessorContext.() -> R
        ) = LlonvneSymbolProcessorContext(resolver, fileResolver, environment, environment.logger).scoped()
    }
}



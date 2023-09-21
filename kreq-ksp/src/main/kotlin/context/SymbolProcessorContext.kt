package context

import KotlinPoetResolver
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

data class SymbolProcessorContext(
    val resolver: Resolver,
    val fileResolver: KotlinPoetResolver,
    val environment: SymbolProcessorEnvironment,
    val logger: KSPLogger
)

fun SymbolProcessorContext.scoped(scoped: SymbolProcessorContext.() -> Unit) = scoped()



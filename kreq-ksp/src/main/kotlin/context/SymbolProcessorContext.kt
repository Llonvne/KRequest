package context

import KotlinPoetResolver
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

/**
 * 该上下文保存了有关于符号处理的对象引用
 * * [resolver] KSP符号处理工具
 * * [poetResolver] 用于处理KotlinPoet的工具
 * * [environment] KSP符号处理的环境变量
 * * [logger] 接口对象
 */
data class SymbolProcessorContext(
    val resolver: Resolver,
    val poetResolver: KotlinPoetResolver,
    val environment: SymbolProcessorEnvironment,
    val logger: KSPLogger
)


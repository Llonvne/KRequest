package exception

import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import context.SymbolProcessorContext

context (SymbolProcessorContext)
fun notKSFunctionDeclaration(value: KSDeclaration) {
    logger.exception(NotFunctionDeclarationException(value))
}

context (SymbolProcessorContext)
fun notAbstractFunctionDeclaration(value: KSFunctionDeclaration) {
    logger.exception(NotAbstractFunctionDeclarationException(value))
}
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

context (context.LlonvneSymbolProcessorContext)
class ApiResolver(private val api: KSClassDeclaration) {
    fun resolve() {
        assertApiClassWithoutTypeParameter()
        fileResolver.registerApi(api) {
            api.extractApiAbstractFunctionCandidates()
                .map { func -> ApiFuncResolver(api, func) }
                .forEach(ApiFuncResolver::resolve)
        }
    }

    private fun KSClassDeclaration.extractApiAbstractFunctionCandidates(): Sequence<KSFunctionDeclaration> =
        declarations.filterAbstractFunction()

    @Suppress("unchecked_cast")
    private fun Sequence<KSDeclaration>.filterAbstractFunction(): Sequence<KSFunctionDeclaration> = filter {
        if (it is KSFunctionDeclaration && it.isAbstract) {
            true
        } else {
            processOnNotKSFunctionDeclarationOrAbstract(it)
            false
        }
    } as Sequence<KSFunctionDeclaration>

    private fun processOnNotKSFunctionDeclarationOrAbstract(value: KSDeclaration) {
        logger.exception(InvalidApiAbstractFunctionDeclarationException(value))
    }

    private fun assertApiClassWithoutTypeParameter() {
        if (api.typeParameters.isNotEmpty()) {
            throw ApiAnnotatedInterfaceNotSupportTypeParameter(api)
        }
    }
}

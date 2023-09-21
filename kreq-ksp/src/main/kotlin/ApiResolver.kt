import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

context (context.SymbolProcessorContext)
class ApiResolver(private val api: KSClassDeclaration) {
    fun resolve() {

        fun assertApiClassWithoutTypeParameter() {
            if (api.typeParameters.isNotEmpty()) {
                throw ApiAnnotatedInterfaceNotSupportTypeParameter(api)
            }
        }

        fun KSClassDeclaration.extractApiAbstractFunctionCandidates() = declarations.filterAbstractFunction()

        assertApiClassWithoutTypeParameter()
        fileResolver.registerApi(api) {
            api.extractApiAbstractFunctionCandidates()
                .map { func -> ApiFuncResolver(api, func) }
                .forEach(ApiFuncResolver::resolve)
        }
    }


    @Suppress(Constants.UNCHECKED_CAST)
    private fun Sequence<KSDeclaration>.filterAbstractFunction(): Sequence<KSFunctionDeclaration> = filter {

        fun processOnNotKSFunctionDeclarationOrAbstract(value: KSDeclaration) {
            logger.exception(InvalidApiAbstractFunctionDeclarationException(value))
        }

        if (it is KSFunctionDeclaration && it.isAbstract) {
            true
        } else {
            processOnNotKSFunctionDeclarationOrAbstract(it)
            false
        }
    } as Sequence<KSFunctionDeclaration>
}

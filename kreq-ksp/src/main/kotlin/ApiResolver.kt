import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

context (context.SymbolProcessorContext)
class ApiResolver(private val api: KSClassDeclaration) {
    fun resolve() {
        fun assertApiClassWithoutTypeParameter() {
            if (api.typeParameters.isNotEmpty()) {
                logger.exception(ApiAnnotatedInterfaceNotSupportTypeParameter(api))
            }
        }
        assertApiClassWithoutTypeParameter()
        fileResolver.registerApi(api) {
            api.declarations.filterAbstractFunction()
                .map { func -> ApiFuncResolver(api, func) }
                .forEach(ApiFuncResolver::resolve)
        }
    }


    @Suppress(Constants.UNCHECKED_CAST)
    private fun Sequence<KSDeclaration>.filterAbstractFunction() = filter { decl ->

        fun processOnNotKSFunctionDeclarationOrAbstract(value: KSDeclaration) {
            logger.exception(InvalidApiAbstractFunctionDeclarationException(value))
        }

        if (decl is KSFunctionDeclaration && decl.isAbstract) {
            true
        } else {
            processOnNotKSFunctionDeclarationOrAbstract(decl)
            false
        }
    } as Sequence<KSFunctionDeclaration>
}

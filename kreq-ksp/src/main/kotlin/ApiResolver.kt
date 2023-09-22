import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import exception.ApiAnnotatedInterfaceNotSupportTypeParameter
import exception.notAbstractFunctionDeclaration
import exception.notKSFunctionDeclaration
import utils.Decision
import utils.filterDecision
import utils.filterType
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

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
            api.declarations.filterAbstractFunction().map { func -> ApiFuncResolver(api, func) }
                .forEach(ApiFuncResolver::resolve)
        }
    }


    @Suppress(Constants.UNCHECKED_CAST)
    private fun Sequence<KSDeclaration>.filterAbstractFunction() = filterDecision { decl ->
        rejectIfNotKSFunctionDecl(decl)
        acceptIf { decl.isAbstract }
        acceptIf { annotatedWithIgnored(decl) }
        reject { notAbstractFunctionDeclaration(decl) }
    } as Sequence<KSFunctionDeclaration>

    @OptIn(ExperimentalContracts::class)
    private fun Decision.rejectIfNotKSFunctionDecl(decl: KSDeclaration) {
        contract {
            returns() implies (decl is KSFunctionDeclaration)
        }
        if (decl !is KSFunctionDeclaration) {
            reject { notKSFunctionDeclaration(decl) }
        }
    }

    private fun annotatedWithIgnored(decl: KSFunctionDeclaration) =
        decl.annotations.filterType<Ignored>().toList().isNotEmpty()
}



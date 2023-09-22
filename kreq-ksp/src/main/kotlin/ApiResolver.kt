import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import exception.ApiAnnotatedInterfaceNotSupportTypeParameter
import exception.notAbstractFunctionDeclaration
import utils.Decision
import utils.filterDecision
import utils.filterType
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

context (context.SymbolProcessorContext)
class ApiResolver(private val api: KSClassDeclaration) {
    fun resolve() {
        assertApiClassWithoutTypeParameter()
        poetResolver.registerApi(api) {
            api.filterAbstractFunction()
                .forEach { func -> ApiFuncResolver(api, func).resolve() }
        }
    }

    @Suppress(Constants.UNCHECKED_CAST)
    private fun KSClassDeclaration.filterAbstractFunction() = declarations.toList().filterDecision { decl ->
        rejectIfNotKSFunctionDecl(decl)
        acceptIf { decl.isAbstract }
        acceptIf { annotatedWithIgnored(decl) }
        reject { notAbstractFunctionDeclaration(decl) }
    } as List<KSFunctionDeclaration>

    @OptIn(ExperimentalContracts::class)
    private fun Decision.rejectIfNotKSFunctionDecl(decl: KSDeclaration) {
        contract {
            returns() implies (decl is KSFunctionDeclaration)
        }
        rejectIf {
            onReject { exception.notKSFunctionDeclaration(decl) }
            decl !is KSFunctionDeclaration
        }
    }

    private fun annotatedWithIgnored(decl: KSFunctionDeclaration) =
        decl.annotations.filterType<Ignored>().toList().isNotEmpty()

    private fun assertApiClassWithoutTypeParameter() {
        if (api.typeParameters.isNotEmpty()) {
            logger.exception(ApiAnnotatedInterfaceNotSupportTypeParameter(api))
        }
    }
}



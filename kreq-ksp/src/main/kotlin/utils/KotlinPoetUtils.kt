package utils

import Api
import Apis
import Constants
import NotAValidApiForReturnValue
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import context.SymbolProcessorContext

typealias FunBuilder = FunSpec.Builder
typealias TypeBuilder = TypeSpec.Builder

fun FunBuilder.addNotImplementedError() = addStatement("throw NotImplementedError()")

fun FunBuilder.addBaseUrlParameter() = addParameter(Constants.BASE_URL_VAR, String::class)

fun TypeBuilder.addBaseUrlPropertyWithInitializer() =
    addProperty(
        PropertySpec
            .builder(Constants.BASE_URL_VAR, String::class, KModifier.PRIVATE)
            .initializer(Constants.BASE_URL_VAR)
            .build()
    )

fun TypeBuilder.addOkHttpClientPropertyWithInitializer(okHttpKSClassDeclaration: KSClassDeclaration) =
    addProperty(
        PropertySpec
            .builder(Constants.OK_HTTP_CLIENT_VAR, okHttpKSClassDeclaration.toClassName(), KModifier.PRIVATE)
            .initializer(Constants.OK_HTTP_CLIENT_VAR)
            .build()
    )

context (SymbolProcessorContext)
fun FunBuilder.returnApiType(api: KSClassDeclaration) {
    assertIsApiAnnotated(api)
    returns(api.asStarProjectedType().toTypeName())
}

fun FunBuilder.useApisAsReceiver() = receiver(Apis::class)
context (SymbolProcessorContext)
fun TypeBuilder.setApiSuperInterface(api: KSClassDeclaration) = run {
    assertIsApiAnnotated(api)
    addSuperinterface(api.asStarProjectedType().toTypeName())
}

fun TypeBuilder.setPrivateType() = apply { modifiers.add(KModifier.PRIVATE) }

fun FunBuilder.setOverride() = apply { modifiers.add(KModifier.OVERRIDE) }

fun FunBuilder.returnSameAs(func: KSFunctionDeclaration) = returns(func.returnType?.toTypeName()!!)

fun FunBuilder.importParameters(declaration: KSFunctionDeclaration) = apply {
    declaration.parameters.toList()
        .forEach { addParameter(it.name?.asString()!!, it.type.toTypeName()) }
}

/**
 * 检查给定 Api 是否由 [Api] 标注
 * 如果没有，抛出 [NotAValidApiForReturnValue] 异常
 * @throws NotAValidApiForReturnValue
 */
context (SymbolProcessorContext)
fun assertIsApiAnnotated(api: KSClassDeclaration) {
    api.getAnnotationsByType(Api::class)
        .toList()
        .ifEmpty {
            throw NotAValidApiForReturnValue(api)
        }
}

fun useVar(name: String) = "\"+$name+\""

fun varToString(name: String) = "\"$name\""

fun FunSpec.Builder.buildReturnStatement(varName: String) {
    addStatement("return $varName")
}

fun FunSpec.Builder.executeCall() = addStatement(".execute()")

fun FunSpec.Builder.buildNewCall(responseVarName: String, requestVarName: String) =
    addStatement("val $responseVarName = ${Constants.OK_HTTP_CLIENT_VAR}.newCall($requestVarName)")

context (SymbolProcessorContext)
fun FunSpec.Builder.addCreateNewInstanceStatement(instanceVarName: String, className: ClassName) =
    addStatement("val $instanceVarName = %T()", className)
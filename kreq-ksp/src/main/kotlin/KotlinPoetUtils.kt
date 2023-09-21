import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

fun FunSpec.Builder.addNotImplementedError() = addStatement("throw NotImplementedError()")

fun FunSpec.Builder.addBaseUrlParameter() = addParameter(Constants.BASE_URL_VAR, String::class)

fun TypeSpec.Builder.addBaseUrlPropertyWithInitializer() =
    addProperty(
        PropertySpec
            .builder(Constants.BASE_URL_VAR, String::class, KModifier.PRIVATE)
            .initializer(Constants.BASE_URL_VAR)
            .build()
    )

fun TypeSpec.Builder.addOkHttpClientPropertyWithInitializer(okHttpKSClassDeclaration: KSClassDeclaration) =
    addProperty(
        PropertySpec
            .builder(Constants.OK_HTTP_CLIENT_VAR, okHttpKSClassDeclaration.toClassName(), KModifier.PRIVATE)
            .initializer(Constants.OK_HTTP_CLIENT_VAR)
            .build()
    )

fun FunSpec.Builder.returnApiType(api: KSClassDeclaration) {
    assertIsApiAnnotated(api)
    returns(api.asStarProjectedType().toTypeName())
}

fun FunSpec.Builder.useApisAsReceiver() = receiver(Apis::class)

fun TypeSpec.Builder.setApiSuperInterface(api: KSClassDeclaration): TypeSpec.Builder {
    assertIsApiAnnotated(api)
    return addSuperinterface(api.asStarProjectedType().toTypeName())
}

fun TypeSpec.Builder.setPrivateType(): TypeSpec.Builder {
    modifiers.add(KModifier.PRIVATE)
    return this
}

fun FunSpec.Builder.setOverride(): FunSpec.Builder {
    modifiers.add(KModifier.OVERRIDE)
    return this
}

fun FunSpec.Builder.returnSameAs(func: KSFunctionDeclaration) = returns(func.returnType?.toTypeName()!!)

fun FunSpec.Builder.importParameters(declaration: KSFunctionDeclaration): FunSpec.Builder {
    declaration.parameters.toList()
        .forEach { addParameter(it.name?.asString()!!, it.type.toTypeName()) }
    return this
}

/**
 * 检查给定 Api 是否由 [Api] 标注
 * 如果没有，抛出 [NotAValidApiForReturnValue] 异常
 * @throws NotAValidApiForReturnValue
 */
fun assertIsApiAnnotated(api: KSClassDeclaration) {
    api.getAnnotationsByType(Api::class)
        .toList()
        .ifEmpty {
            throw NotAValidApiForReturnValue(api)
        }
}


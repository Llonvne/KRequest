package utils

import Api
import Apis
import Constants
import GeneratedCode
import NotAValidApiDecl
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import context.SymbolProcessorContext

/**
 * 将 [FunSpec.Builder] 简称为 [FunSpec]
 */
typealias FunBuilder = FunSpec.Builder
/**
 * 将 [TypeSpec.Builder] 简称为 [TypeBuilder]
 */
typealias TypeBuilder = TypeSpec.Builder

/**
 * 在生成的函数中抛出 [NotImplementedError] 异常
 */
fun FunBuilder.addNotImplementedError() = addStatement("throw NotImplementedError()")

/**
 * 尝试在函数中添加一个类型为[String]名字为[Constants.BASE_URL_VAR]的参数
 */
fun FunBuilder.addBaseUrlParameter() = addParameter(Constants.BASE_URL_VAR, String::class)

/**
 * 尝试在函数中添加一个类型为 [okHttpClientDecl] 名为 [Constants.OK_HTTP_CLIENT_VAR] 的参数
 */
fun FunSpec.Builder.addOkHttpClientParameter(okHttpClientDecl: KSClassDeclaration) =
    addParameter(Constants.OK_HTTP_CLIENT_VAR, okHttpClientDecl.toClassName())

/**
 * 简化 Property 的配置形式，在 [TypeBuilder.AddProperty] 的时候隐式调用[PropertySpec.builder]并允许通过lambda配置[PropertySpec]
 */
inline fun <reified T> TypeBuilder.addProperty(
    name: String, vararg modifiers: KModifier, configuration: PropertySpec.Builder.() -> Unit = {}
) = addProperty(PropertySpec.builder(name, T::class, *modifiers).apply(configuration).build())

/**
 * 简化 Property 的配置形式，在 [TypeBuilder.AddProperty] 的时候隐式调用[PropertySpec.builder]并允许通过lambda配置[PropertySpec]
 */
fun TypeBuilder.addProperty(
    name: String, className: ClassName, vararg modifiers: KModifier, configuration: PropertySpec.Builder.() -> Unit = {}
) = addProperty(PropertySpec.builder(name, className, *modifiers).apply(configuration).build())

/**
 * 允许添加一个变量，并直接在构造器初始化
 */
inline fun <reified T> TypeBuilder.addPropertyAndInitializer(
    name: String, vararg modifiers: KModifier, configuration: PropertySpec.Builder.() -> Unit = {}
) = addProperty<T>(name, *modifiers) {
    configuration()
    initializer(name)
}

/**
 * 允许添加一个变量，并直接在构造器初始化
 */
fun TypeBuilder.addPropertyAndInitializer(
    name: String, className: ClassName, vararg modifiers: KModifier, configuration: PropertySpec.Builder.() -> Unit = {}
) = addProperty(name, className, *modifiers) {
    configuration()
    initializer(name)
}

/**
 * 像类中添加一个 baseUrl 属性，并在构造器中初始化,标识符为 Private
 */
fun TypeBuilder.addBaseUrlPropertyWithInitializer() =
    addPropertyAndInitializer<String>(Constants.BASE_URL_VAR, KModifier.PRIVATE)

/**
 * 像类中添加一个 okHttpClient 属性，并在构造器中初始化,标识符为 Private
 */
fun TypeBuilder.addOkHttpClientPropertyWithInitializer(okHttpKSClassDeclaration: KSClassDeclaration) =
    addPropertyAndInitializer(Constants.OK_HTTP_CLIENT_VAR, okHttpKSClassDeclaration.toClassName(), KModifier.PRIVATE)

/**
 * 该函数会检查入参是否为被[Api]注解的值，如果是设置为函数返回值，如果不是将会抛出[NotAValidApiDecl]
 */
context (SymbolProcessorContext)
fun FunBuilder.returnApiType(api: KSClassDeclaration) {
    assertIsApiAnnotated(api)
    returns(api.asStarProjectedType().toTypeName())
}

/**
 * 设置 [Apis] 对象作为函数接收器类型
 */
fun FunBuilder.useApisAsReceiver() = receiver(Apis::class)

/**
 * 该函数会检查入参是否为被[Api]注解的值，如果是设置为类型超类型，如果不是将会抛出[NotAValidApiDecl]
 */
context (SymbolProcessorContext)
fun TypeBuilder.setApiSuperInterface(api: KSClassDeclaration) = run {
    assertIsApiAnnotated(api)
    addSuperinterface(api.asStarProjectedType().toTypeName())
}

/**
 * 设置类型为 [KModifier.PRIVATE]
 */
fun TypeBuilder.setPrivateModifier() = apply { modifiers.add(KModifier.PRIVATE) }

/**
 * 设置为重载类型函数 [KModifier.OVERRIDE]
 */
fun FunBuilder.setOverride() = apply { modifiers.add(KModifier.OVERRIDE) }

/**
 * 设置接收器函数的返回值与参数返回值相同，注意该函数不处理泛型
 */
fun FunBuilder.returnSameAs(func: KSFunctionDeclaration) = returns(func.returnType?.toTypeName()!!)

/**
 * 设置接收器函数参数列表与参数函数相同，注意该函数不处理泛型
 */
fun FunBuilder.importParameters(declaration: KSFunctionDeclaration) = apply {
    declaration.parameters.toList().forEach { addParameter(it.name?.asString()!!, it.type.toTypeName()) }
}

/**
 * 检查给定 Api 是否由 [Api] 标注
 * 如果没有，抛出 [NotAValidApiDecl] 异常
 * @throws NotAValidApiDecl
 */
context (SymbolProcessorContext)
fun assertIsApiAnnotated(api: KSClassDeclaration) {
    api.getAnnotationsByType(Api::class).toList().ifEmpty {
        throw NotAValidApiDecl(api)
    }
}

/**
 * 在使用 KotlinPoet 并且 %L 占位符时，使用该函数在字符串中拼接一个变量
 */
fun useVar(name: String) = "\"+$name+\""

/**
 * 在使用 KotlinPoet 并且使用 %L 占位符号时，使用该函数创造一个字符串
 */
fun varToString(name: String) = "\"$name\""

/**
 * 添加一条 return 语句
 */
fun FunSpec.Builder.buildReturnStatement(varName: String) {
    addStatement("return $varName")
}

/**
 * 使用 OkHttp 时，使用该函数来执行一个 Call
 */
fun FunSpec.Builder.executeCall() = addStatement(".execute()")

/**
 * 使用OkHttp时，使用该函数完成构建一个Request
 */
fun FunSpec.Builder.finishRequestBuild() = addStatement(".build()")

/**
 * 使用 OkHttp 时，使用该函数来新建一个Call变量
 * * [responseVarName] 响应的变量名
 * * [requestVarName] 请求的变量名
 */
fun FunSpec.Builder.buildNewCall(responseVarName: String, requestVarName: String) =
    addStatement("val $responseVarName = ${Constants.OK_HTTP_CLIENT_VAR}.newCall($requestVarName)")

/**
 * 添加一条类型初始化语句，使用类型无参数构造器
 * * [instanceVarName] 实体的名字
 * * [className] 类型名
 */
context (SymbolProcessorContext)
fun FunSpec.Builder.addCreateNewInstanceStatement(instanceVarName: String, className: ClassName) =
    addStatement("val $instanceVarName = %T()", className)

/**
 * FunSpec.builder 简易模式，允许使用 DSL 模式配置，而不是 . 模式
 */
fun FunSpec.Companion.builder(name: String, configuration: FunSpec.Builder.() -> Unit): FunSpec.Builder =
    builder(name).apply(configuration)

/**
 * TypeSpec.builder 简易模式，允许使用 DSL 模式配置，而不是 . 模式
 */
fun TypeSpec.Companion.classBuilder(name: String, configuration: TypeBuilder.() -> Unit) =
    classBuilder(name).apply(configuration)

fun TypeBuilder.buildPrimaryConstruct(configuration: FunSpec.Builder.() -> Unit) =
    primaryConstructor(FunSpec.constructorBuilder().apply(configuration).build())

inline fun <reified T : Annotation> TypeBuilder.addAnnotation(configuration: AnnotationSpec.Builder.() -> Unit) =
    addAnnotation(AnnotationSpec.builder(T::class).apply(configuration).build())

/**
 * 生成一个 [GeneratedCode] 注解，声明为生成代码
 */
fun TypeSpec.Builder.addGeneratedAnnotation() = addAnnotation<GeneratedCode> {
    addMember(varToString("Generated by KRequest"))
}
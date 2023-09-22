package utils

import Constants
import Constants.okHttpRequestBuilderFqName
import Constants.okHttpResponseFqName
import DELETE
import GET
import HttpMethod
import POST
import PostBody
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import context.SymbolProcessorContext
import exception.ApiMemberFunctionMustAnnotatedWithHttpMethod
import exception.HttpMethodShouldBeUniqueOnOneMethod
import exception.KSClassDeclarationNotFound
import exception.NotResponseTypeShouldBeNullable
import kotlin.reflect.KClass

val List<KSValueArgument>.uri get() = first { it.name?.asString() == "uri" }.value as String
fun KSValueParameter.hasPostBodyAnnotation() =
    annotations.filter { anno -> anno.isSameType<PostBody>() }.toList().isNotEmpty()

val Resolver.requestBodyDecl: KSType
    get() = this.getClassDeclarationByNameOrException("okhttp3.RequestBody").asStarProjectedType()

fun KSValueParameter.typeIsRequestBody(resolver: Resolver) = type.resolve().isAssignableFrom(resolver.requestBodyDecl)

fun Resolver.getClassDeclarationByNameOrException(name: String) =
    getClassDeclarationByName(name) ?: throw KSClassDeclarationNotFound(name)

fun KSTypeReference?.isTypeQualifiedNameEquals(qualifiedName: String) =
    this?.resolve()?.declaration?.qualifiedName?.asString() == qualifiedName

fun KSFunctionDeclaration.isReturnTypeQualifiedNameEquals(qualifiedName: String) =
    returnType?.isTypeQualifiedNameEquals(qualifiedName) ?: false

fun KSFunctionDeclaration.isSuspend(): Boolean = modifiers.contains(Modifier.SUSPEND)

fun qualifiedNameSetOf(vararg classes: KClass<*>) = classes
    .mapNotNull { it::class.qualifiedName }.toSet()

fun KSAnnotation.hasHttpMethodAnnotation() = annotations.filterType<HttpMethod>().toList().isNotEmpty()

fun extractHttpMethod(decl: KSFunctionDeclaration) =
    decl.annotations.toList()
        .first { it.qualifiedName in qualifiedNameSetOf(GET::class, POST::class, DELETE::class) }

context (SymbolProcessorContext)
val okHttpRequestBuilderDecl
    get() = resolver.getClassDeclarationByNameOrException(okHttpRequestBuilderFqName)

val KSFunctionDeclaration.returnTypeIsResponse get() = isReturnTypeQualifiedNameEquals(okHttpResponseFqName)

fun KSFunctionDeclaration.assertCustomizeReturnTypeShouldBeNullable() {
    if (!returnType?.resolve()?.isMarkedNullable!!) {
        throw NotResponseTypeShouldBeNullable(this)
    }
}

context (SymbolProcessorContext)
@Suppress(Constants.UNCHECKED_CAST)
fun KSFunctionDeclaration.assertAnnotatedWithHttpMethodImpl(api: KSClassDeclaration) {
    val annotations = this.annotations.filter { anno -> anno.hasHttpMethodAnnotation() }.toList()
    when (annotations.size) {
        0 -> logger.exception(ApiMemberFunctionMustAnnotatedWithHttpMethod(api, this))
        1 -> {}
        else -> logger.exception(HttpMethodShouldBeUniqueOnOneMethod(annotations, this))
    }
}
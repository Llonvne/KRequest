package utils

import Constants
import Constants.OK_HTTP_REQUEST_BUILDER_FQ_NAME
import Constants.OK_HTTP_RESPONSE_FQ_NAME
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

@OptIn(CacheValueProtection::class)
val Resolver.requestBodyDecl: KSType
    get() = cacheValue { this.getClassDeclarationByNameOrException("okhttp3.RequestBody").asStarProjectedType() }

context(SymbolProcessorContext)
fun KSValueParameter.typeIsRequestBody() = type.resolve().isAssignableFrom(resolver.requestBodyDecl)

@OptIn(CacheValueProtection::class)
fun Resolver.getClassDeclarationByNameOrException(name: String) =
    cacheValue(name) { getClassDeclarationByName(it) ?: throw KSClassDeclarationNotFound(it) }

fun KSTypeReference?.isTypeQualifiedNameEquals(qualifiedName: String) =
    this?.resolve()?.declaration?.qualifiedName?.asString() == qualifiedName

fun KSFunctionDeclaration.isReturnTypeQualifiedNameEquals(qualifiedName: String) =
    returnType?.isTypeQualifiedNameEquals(qualifiedName) ?: false

fun KSFunctionDeclaration.isSuspend(): Boolean = modifiers.contains(Modifier.SUSPEND)

fun simpleNameSetOf(vararg classes: KClass<*>) = classes
    .mapNotNull { it.simpleName }.toSet()

fun KSAnnotation.hasHttpMethodAnnotation() = annotations.filterType<HttpMethod>().toList().isNotEmpty()

@OptIn(CacheValueProtection::class)
fun extractHttpMethod(decl: KSFunctionDeclaration) =
    decl.annotations.toList().first {
        it.simpleName in cacheValue {
            simpleNameSetOf(
                GET::class, POST::class, DELETE::class
            )
        }
    }


context (SymbolProcessorContext)
val okHttpRequestBuilderDecl
    get() = resolver.getClassDeclarationByNameOrException(OK_HTTP_REQUEST_BUILDER_FQ_NAME)

val KSFunctionDeclaration.returnTypeIsResponse get() = isReturnTypeQualifiedNameEquals(OK_HTTP_RESPONSE_FQ_NAME)

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
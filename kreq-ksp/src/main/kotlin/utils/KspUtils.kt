package utils

import KSClassDeclarationNotFound
import PostBody
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*

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
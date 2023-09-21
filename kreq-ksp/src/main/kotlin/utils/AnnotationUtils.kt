package utils

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration

inline fun <reified T> Resolver.getSymbolsWithAnnotation() =
    getSymbolsWithAnnotation(T::class.java.name)

val KSAnnotation.declaration get() = annotationType.resolve().declaration

val KSAnnotation.annotations get() = annotationType.resolve().declaration.annotations

val KSAnnotation.simpleName get() = declaration.simpleName.asString()

val KSAnnotation.qualifiedName get() = declaration.qualifiedName?.asString()

val KSClassDeclaration.isInterface get() = classKind == ClassKind.INTERFACE

inline fun <reified Annotation> KSAnnotation.isSameWith() = qualifiedName == Annotation::class.qualifiedName

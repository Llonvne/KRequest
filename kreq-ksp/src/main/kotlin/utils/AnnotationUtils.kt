package utils

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * 泛型形式的 [Resolver.getSymbolsWithAnnotation]
 * 等价于 getSymbolsWithAnnotation(T::class)
 */
inline fun <reified T> Resolver.getSymbolsWithAnnotation() = getSymbolsWithAnnotation(T::class.java.name)

/**
 * 该注解解析[KSAnnotation]为[KSClassDeclaration]
 * 等价于 annotationType.resolve().declaration
 */
val KSAnnotation.declaration get() = annotationType.resolve().declaration

/**
 * 该注解尝试解析 [KSAnnotation] 上的注解
 * 由于[KSAnnotation]的特殊性，必须要通过 annotationType.resolve().declaration.annotations
 * 才能获得标注在注解上方的元注解
 */
val KSAnnotation.annotations get() = declaration.annotations

/**
 * 该方法返回[KSAnnotation]的[simpleName]的[String]形式
 * 等价于 declaration.simpleName.asString()
 */
val KSAnnotation.simpleName get() = declaration.simpleName.asString()

/**
 * 该方法返回 [KSAnnotation] 的 [String] 形式
 * 等价于 declaration.qualifiedName?.asString()
 */
val KSAnnotation.qualifiedName get() = declaration.qualifiedName?.asString()

/**
 * 判断 [KSClassDeclaration] 是否一个接口
 */
val KSClassDeclaration.isInterface get() = classKind == ClassKind.INTERFACE

/**
 * 通过[KSAnnotation.qualifiedName] 判断与 [Annotation] 是否同一类型
 */
inline fun <reified Annotation> KSAnnotation.isSameType() = qualifiedName == Annotation::class.qualifiedName

/**
 * 过滤序列中的[KSAnnotation]为给定[Type]
 */
inline fun <reified Type> Sequence<KSAnnotation>.filterType() = filter { it.isSameType<Type>() }

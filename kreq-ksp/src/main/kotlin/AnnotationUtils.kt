import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation

inline fun <reified T> Resolver.getSymbolsWithAnnotation() =
    getSymbolsWithAnnotation(T::class.java.name)

val KSAnnotation.declaration get() = annotationType.resolve().declaration

val KSAnnotation.annotations get() = annotationType.resolve().declaration.annotations

val KSAnnotation.simpleName get() = declaration.simpleName.asString()

val KSAnnotation.qualifiedName get() = declaration.qualifiedName?.asString()
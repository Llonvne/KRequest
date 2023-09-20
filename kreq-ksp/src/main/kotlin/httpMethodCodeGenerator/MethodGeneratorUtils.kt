package httpMethodCodeGenerator

import Path
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.FunSpec
import context.AnnotationContext

val AnnotationContext.buildUrl: String
    get() {
        return useVarInString("baseUrl") + this.httpMethodAnnotation.arguments.first {
            it.name?.asString() == "uri"
        }.value as String
    }

context (FunSpec.Builder)
fun AnnotationContext.resolveUrl() {
    val pathVars = matchPathVar(buildUrl)
    val pathVarsMap = pathVars.associateWith {
        parameters.findPathWithSameName(it).name?.asString()!!
    }

    addStatement(".url(%L)", varToString(replaceWithMapValues(buildUrl, pathVarsMap)))
}

fun replaceWithMapValues(input: String, replacements: Map<String, String>): String {
    var result = input
    replacements.forEach { (key, value) ->
        val pattern = """\{$key\}""".toRegex()
        result = result.replace(pattern, useVarInString(value))
    }
    return result
}

private fun List<KSValueParameter>.findPathWithSameName(pathVarName: String): KSValueParameter {
    val matchedParameters = this.filter {
        val pathAnnotation = it.annotations.singleOrNull { annotation -> annotation.isSameWith<Path>() }

        pathAnnotation?.arguments?.first()?.value == pathVarName
    }

    return when {
        matchedParameters.size > 1 -> throw IllegalArgumentException("超过一个KSValueParameter具有相同的Path名称：$pathVarName")
        matchedParameters.size == 1 -> matchedParameters.first()
        else -> throw IllegalArgumentException("找不到一个KSValueParameter具有相同的Path名称：$pathVarName")
    }
}

private fun matchPathVar(input: String): List<String> {
    val pattern = """\{(.*?)\}""".toRegex()
    return pattern.findAll(input).toList().map { it.groupValues[1] }
}

inline fun <reified Annotation> KSAnnotation.isSameWith() = annotationType
    .resolve().declaration.qualifiedName?.asString() == Annotation::class.qualifiedName

fun useVarInString(name: String) = "\"+$name+\""

fun varToString(name: String) = "\"$name\""
package httpMethodCodeGenerator

import Constants
import Path
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.FunSpec
import context.HttpMethodBuildContext
import utils.FunBuilder

val HttpMethodBuildContext.buildUrl get() = Var(Constants.BASE_URL_VAR) + httpMethodAnnotation.arguments.uri


val List<KSValueArgument>.uri get() = first { it.name?.asString() == "uri" }.value as String

context (FunSpec.Builder)
fun FunBuilder.resolveUrl(annoCtx: HttpMethodBuildContext) {
    val pathVars = matchPathVar(annoCtx.buildUrl)
    val pathVarsMap = pathVars.associateWith {
        annoCtx.parameters.findPathWithSameName(it).name?.asString()!!
    }

    addStatement(".url(%L)", varToString(replaceWithMapValues(annoCtx.buildUrl, pathVarsMap)))
}

fun replaceWithMapValues(input: String, replacements: Map<String, String>): String {
    var result = input
    replacements.forEach { (key, value) ->
        val pattern = """\{$key\}""".toRegex()
        result = result.replace(pattern, Var(value))
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

fun Var(name: String) = "\"+$name+\""

fun varToString(name: String) = "\"$name\""

fun FunSpec.Builder.finishRequestBuild() = addStatement(".build()")
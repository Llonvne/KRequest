package url

import Path
import com.google.devtools.ksp.symbol.KSValueParameter
import utils.isSameType
import utils.useVar

fun replaceWithMapValues(input: String, replacements: Map<String, String>): String {
    var result = input
    replacements.forEach { (key, value) ->
        val pattern = """\{$key\}""".toRegex()
        result = result.replace(pattern, useVar(value))
    }
    return result
}

fun List<KSValueParameter>.findPathWithSameName(pathVarName: String): KSValueParameter {
    val matchedParameters = this.filter {
        val pathAnnotation = it.annotations.singleOrNull { annotation -> annotation.isSameType<Path>() }
        pathAnnotation?.arguments?.first()?.value == pathVarName
    }

    return when {
        matchedParameters.size > 1 -> throw IllegalArgumentException("超过一个KSValueParameter具有相同的Path名称：$pathVarName")
        matchedParameters.size == 1 -> matchedParameters.first()
        else -> throw IllegalArgumentException("找不到一个KSValueParameter具有相同的Path名称：$pathVarName")
    }
}
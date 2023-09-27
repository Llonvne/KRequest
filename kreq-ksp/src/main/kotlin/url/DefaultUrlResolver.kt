package url

import com.squareup.kotlinpoet.FunSpec
import context.HttpMethodBuildContext
import context.buildUrl
import utils.varToString

class DefaultUrlResolver : UrlResolver {
    context(FunSpec.Builder) override fun resolve(ctx: HttpMethodBuildContext) {
        val pathVars = matchPathVar(ctx.buildUrl)
        val pathVarsMap = pathVars.associateWith {
            ctx.parameters.findPathWithSameName(it).name?.asString()!!
        }

        addStatement(".url(%L)", varToString(replaceWithMapValues(ctx.buildUrl, pathVarsMap)))
    }

    private fun matchPathVar(input: String): List<String> {
        val pattern = """\{(.*?)\}""".toRegex()
        return pattern.findAll(input).toList().map { it.groupValues[1] }
    }
}
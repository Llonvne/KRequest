package url

import com.squareup.kotlinpoet.FunSpec
import context.HttpMethodBuildContext

interface UrlResolver {
    context (FunSpec.Builder)
    fun resolve(ctx: HttpMethodBuildContext)
}


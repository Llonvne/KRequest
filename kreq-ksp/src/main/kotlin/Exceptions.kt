import com.google.devtools.ksp.symbol.*

open class ApiException(msg: String) : Exception(msg)

open class ApisInternalException(msg: String) : Exception(msg)

private fun String.withLocation(node: KSNode): String {
    return this + "at ${node.location.format()}"
}

class NotAValidApiDecl(value: KSClassDeclaration) : ApisInternalException(
    "$value is not a valid API annotated target!".withLocation(value)
)

class InvalidApiDeclarationException(value: KSAnnotated) : ApiException(
    "$value is not a valid API annotated target,API annotation only can apply on Interface".withLocation(value)
)

class InvalidApiAbstractFunctionDeclarationException(value: KSDeclaration) : ApiException(
    "$value is not a abstract function declaration in Api annotated interface".withLocation(value)
)

class AlreadyRegisterFunctionInCacheException(api: KSClassDeclaration) : ApisInternalException(
    "$api has already register function in functions cache".withLocation(api)
)

class ApiNotRegisterInException(api: KSClassDeclaration) : ApisInternalException(
    "$api has already register function in functions cache".withLocation(api)
)

class ApiAnnotatedInterfaceNotSupportTypeParameter(api: KSClassDeclaration) : ApiException(
    "$api do not support type parameter".withLocation(api)
)

class ApiMemberFunctionMustAnnotatedWithHttpMethod(api: KSClassDeclaration, func: KSFunctionDeclaration) :
    ApiException("$api.$func must annotated with HttpMethod annotation".withLocation(api))

fun Location.format() = when (this) {
    is NonExistLocation -> "cannot find code location..."
    is FileLocation -> "${this.filePath}:${this.lineNumber}"
    else -> "error in location finding..."
}

class HttpMethodShouldBeUniqueOnOneMethod(annotations: List<KSAnnotation>, func: KSFunctionDeclaration) : ApiException(
    "HttpMethod should be unique on a method: ${annotations.size} found! ".withLocation(func)
)

class PostBodyNeededException(decl: KSFunctionDeclaration) :
    ApiException("PostBody 注解数量为 0 ,需要一个 PostBody!".withLocation(decl))

class PostBodyMoreThanOneException(size: Int, decl: KSFunctionDeclaration) : ApiException(
    "PostBody 注解数量为 $size ,仅需要一个 PostBody!".withLocation(decl)
)

class KSClassDeclarationNotFound(name: String) : ApisInternalException(
    "class $name not found when in ksp!"
)
import com.google.devtools.ksp.symbol.*

open class ApiException(msg: String) : Exception(msg)

open class ApisInternalException(msg: String) : Exception(msg)

class NotAValidApiForReturnValue(value: KSClassDeclaration) : ApisInternalException(
    "$value is not a valid API annotated target for return!"
)

class InvalidApiDeclarationException(value: KSAnnotated) : ApiException(
    "$value is not a valid API annotated target,API annotation only can apply on Interface"
)

class InvalidApiAbstractFunctionDeclarationException(value: KSDeclaration) : ApiException(
    "$value is not a abstract function declaration in Api annotated interface"
)

class AlreadyRegisterFunctionInCacheException(api: KSClassDeclaration) : ApisInternalException(
    "$api has already register function in functions cache"
)

class ApiNotRegisterInException(api: KSClassDeclaration) : ApisInternalException(
    "$api has already register function in functions cache"
)

class ApiAnnotatedInterfaceNotSupportTypeParameter(api: KSClassDeclaration) : ApiException(
    "$api do not support type parameter"
)

class ApiMemberFunctionMustAnnotatedWithHttpMethodAnnotation(api: KSClassDeclaration, func: KSFunctionDeclaration) :
    ApiException(
        "$api.$func must annotated with HttpMethod annotation at ${func.location.format()}"
    )

fun Location.format() = when (this) {
    is NonExistLocation -> "cannot find code location..."
    is FileLocation -> "${this.filePath}:${this.lineNumber}"
    else -> "!!!"
}

class OkHttpClientNotFoundException : ApiException("OkHttpClient not found ...")

class HttpMethodShouldBeUniqueOnOneMethod(annotations: List<KSAnnotation>, func: KSFunctionDeclaration) : ApiException(
    "HttpMethod should be unique on a method: ${annotations.size} found! at ${func.location.format()}"
)
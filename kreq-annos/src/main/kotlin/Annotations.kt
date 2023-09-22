import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
/**
 * Api 注解声明注解接口为 API，需要被处理
 */
annotation class Api

@Inherited
@Target(AnnotationTarget.ANNOTATION_CLASS)
annotation class HttpMethod

@HttpMethod
@Target(AnnotationTarget.FUNCTION)
annotation class GET(val uri: String)

@HttpMethod
@Target(AnnotationTarget.FUNCTION)
annotation class POST(val uri: String)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Path(val name: String)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class PostBody

@Target(AnnotationTarget.CLASS)
annotation class GeneratedCode(val value: String)

@Target(AnnotationTarget.FUNCTION)
annotation class DELETE(val value: String)
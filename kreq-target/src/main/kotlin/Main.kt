import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Suppress("")
fun main() {
    val json = """
        {
            "key1": "value1",
            "key2": "value2"
        }
    """.trimIndent()

    val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())

    create<MyApi>("https://www.baidu.com", OkHttpClient())
        .getUser(1, body)
}

inline fun <reified Type> create(baseUrl: String, okHttpClient: OkHttpClient): Type =
    Class.forName(Type::class.qualifiedName + "Impl")
        .constructors.first().newInstance(baseUrl, okHttpClient) as Type

@Api
interface MyApi {
    @POST("/user/{id}")
    fun getUser(@Path("id") userId: Int, @PostBody body: RequestBody)
}
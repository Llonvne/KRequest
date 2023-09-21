import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.Response

fun main() {
    println(
        create<GitHubApi>("https://api.github.com", OkHttpClient())
    )
}

inline fun <reified Type> create(baseUrl: String, okHttpClient: OkHttpClient): Type =
    Class.forName(Type::class.qualifiedName + "Impl")
        .constructors.first().newInstance(baseUrl, okHttpClient) as Type

@Api
interface GitHubApi {
    @GET("/users/{login}")
    fun getUserAsync(@Path("login") login: String): Response

    @POST("/users/{login}")
    fun getUser(@Path("login") login: String, @PostBody body: RequestBody): Response
}





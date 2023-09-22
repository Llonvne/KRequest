import okhttp3.OkHttpClient

fun main() {
    println(
        create<GitHubApi>("https://api.github.com", OkHttpClient())
            .getUser("llonvne")
    )
}

inline fun <reified Type> create(baseUrl: String, okHttpClient: OkHttpClient): Type =
    Class.forName(Type::class.qualifiedName + "Impl")
        .constructors.first().newInstance(baseUrl, okHttpClient) as Type

@Api
interface GitHubApi {
    @GET("/users/{login}")
    suspend fun getUserAsync(@Path("login") login: String): GitUser?

    @GET("/users/{login}")
    fun getUser(@Path("login") login: String): GitUser?
}

data class GitUser(
    val id: Int,
    val login: String,
    val url: String
)




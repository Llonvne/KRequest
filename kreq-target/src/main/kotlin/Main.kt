import okhttp3.OkHttpClient

fun main() {
    val githubApi = createAPI<GitHubApi>("https://api.github.com", OkHttpClient())
    val user = githubApi.getUser("llonvne")
}

@Api
interface GitHubApi {
//    @GET("/users/{login}")
//    suspend fun getUserAsync(@Path("login") login: String): GitUser?

    @GET("/users/{login}")
    fun getUser(@Path("login") login: String): GitUser?
}

data class GitUser(
    val id: Int,
    val login: String,
    val url: String
)




import okhttp3.OkHttpClient

suspend fun main() {
    val githubApi = createAPI<GitHubApi>("https://api.github.com", OkHttpClient())
    val user = githubApi.getUser("llonvne")
}

@Api
interface GitHubApi {
    @GET("/users/{login}")
    suspend fun getUser(@Path("login") login: String): GitUser?
}

data class GitUser(
    val id: Int,
    val login: String,
    val url: String
)




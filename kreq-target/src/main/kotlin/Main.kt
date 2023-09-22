import okhttp3.OkHttpClient

fun main() {
    val githubApi = createAPI<GitHubApi>("https://api.github.com", OkHttpClient())
//    val user = githubApi.getUser("llonvne")
}

@Api
interface GitHubApi {

    @GET("/users/{login}")
    fun getUser(@Path("login") login: String): GitUser?

    @Ignored
    fun helloWorld() = println("HelloWorld")
}

data class GitUser(
    val id: Int,
    val login: String,
    val url: String
)




# KRequest

## Introduction

KRequest is a **Declarative** HTTP client/Kotlin Symbol Processor using KSP and OkHttp3

## Status  ![Static Badge](https://img.shields.io/badge/status-Development-8A2BE2)

In fact, KRequest is still under **Development**, and the following are the available annotations.

![Static Badge](https://img.shields.io/badge/KRequest-API-blue) The API annotation indicates that the annotated
interface is a network request interface, which needs to be implemented by KRequest.

![Static Badge](https://img.shields.io/badge/KRequest-Ignored-blue) The Ignored annotation indicates KRequest will
ignore to implement this function if you want to give an implementation in an interface declaration. Try to use kotlin
extension function when you consider to use this annotation

![Static Badge](https://img.shields.io/badge/HTTP_Method-GET-Green) The GET annotation indicates KRequest use HTTP GET
request, with the parameter uri.

![Static Badge](https://img.shields.io/badge/HTTP_Method-POST-Green) The POST annotation indicates KRequest use HTTP
POST request, with the parameter uri.

![Static Badge](https://img.shields.io/badge/Parameter-Path-yellow) The Path annotation indicates KRequest use find a
path variable with the same name in the annotation paramteter and use this variable to replace it.

![Static Badge](https://img.shields.io/badge/Parameter-PostBody-yellow) The PostBody annotation indicates KRequest will
use this annotated variable as the request body.

## Usage

Let's understand the usage of KRequest through an example. When we need to access a GitHub API with the following
URL https://api.github.com/users/{login}, where {login} represents the username parameter, we use the HTTP GET method
and the response body is need to be converted to GitUser.

```kotlin
data class GitUser(
    val id: Int,
    val login: String,
    val url: String
)

val client = OkHttpClient()

val login = "your_github_username"
val url = "https://api.github.com/users/$login"

val request = Request.Builder()
    .url(url)
    .build()

return client.newCall(request).execute().use { response ->
    if (response.isSuccessful) {
        val user = Gson().fromJson(response.body?.string(), GitUser::class.java)
        return@use user
    } else {
        return@use null
    }
}
```

This is a standard implementation of a network request using OkHttp, but it contains too much information about the
implementation. We neither need nor want to know how it's implemented. We can describe our API in a simpler way and then
let KRequest help us implement it.

```kotlin
@Api
interface GitHubApi {
    @GET("/users/{login}")
    fun getUser(@Path("login") login: String): GitUser?
}
```

Then, we use the **createAPI** function to obtain its implementation, and then enjoy using it freely.

```kotlin
val githubApi = createAPI<GitHubApi>("https://api.github.com", OkHttpClient())
val user = githubApi.getUser("llonvne")
```

Sometimes, overly magical behaviors might make users feel puzzled. For KRequest, just search for '
{YourApiClassName}Impl', and you can find the implementation class that KRequest generates for you. In fact, it's no
different from the implementation class you would write directly!

```kotlin
public fun Apis.GitHubApi(baseUrl: String, okHttpClient: OkHttpClient): GitHubApi =
    GitHubApiImpl(baseUrl, okHttpClient)

@GeneratedCode("Generated by KRequest")
private class GitHubApiImpl(
    private val baseUrl: String,
    private val okHttpClient: OkHttpClient,
) : GitHubApi {
    public override fun getUser(login: String): GitUser? {
        val request = Request.Builder()
            .get()
            .url("" + baseUrl + "/users/" + login + "")
            .build()
        val resp = okHttpClient.newCall(request)
            .execute()
        return converter(resp)
    }
}
```

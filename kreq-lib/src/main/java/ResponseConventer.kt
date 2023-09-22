import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Response

inline fun <reified Type> createAPI(baseUrl: String, okHttpClient: OkHttpClient): Type =
    Class.forName(Type::class.qualifiedName + "Impl")
        .constructors.first().newInstance(baseUrl, okHttpClient) as Type

inline fun <reified T> converter(response: Response?): T? {
    if (response == null) {
        return null
    }
    val body = response.body
    return Gson().fromJson(body?.string(), T::class.java)
}

suspend inline fun <reified T> suspended(crossinline request: suspend () -> T): T? =
    withContext(Dispatchers.IO) {
        request()
    }


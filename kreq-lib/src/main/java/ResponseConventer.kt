import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Response

inline fun <reified T> converter(response: Response?): T? {
    if (response == null) {
        return null
    }
    val body = response.body
    return Gson().fromJson(body?.string(), T::class.java)
}

suspend inline fun <reified T> suspended(crossinline request: suspend () -> T) = withContext(Dispatchers.IO) {
    request()
}


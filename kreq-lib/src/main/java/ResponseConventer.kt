import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response

inline fun <reified T> converter(response: Response?): T? {
    if (response == null) {
        return null
    }
    val body = response.body
    return Gson().fromJson(body?.string(), T::class.java)
}

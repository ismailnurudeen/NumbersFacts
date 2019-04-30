package xyz.ismailnurudeen.numberfacts

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

object QueryUtils {
    private const val apiEndPoint = "http://numbersapi.com/"
    fun getUrl(num: String, type: Int, isRandom: Boolean = false): URL {
        return URL(when (type) {
            Constants.TYPE_TRIVIA -> {
                if (isRandom)
                    apiEndPoint + "random/trivia?json"
                else "$apiEndPoint$num/trivia?json"
            }
            Constants.TYPE_DATE -> {
                if (isRandom)
                    apiEndPoint + "random/date?json"
                else "$apiEndPoint$num/date?json"
            }
            Constants.TYPE_YEAR -> {
                if (isRandom)
                    apiEndPoint + "random/year?json"
                else "$apiEndPoint$num/year?json"
            }
            else -> "${apiEndPoint}random?json"
        })
    }

    fun getApiResult(url: URL): Fact {
        val result: String
        try {
            val urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.requestMethod = "GET"
            urlConnection.readTimeout = 10000
            urlConnection.connectTimeout = 15000
            urlConnection.connect()

            return if (urlConnection.responseCode == 200) {
                val inputStream = urlConnection.inputStream
                result = readInputStream(inputStream)
                jsonStringToFactObject(result)
            } else {
                Log.i("RESPONSE_CODE", "URL:${url.path} CODE:${urlConnection.responseCode}")
                Fact("", "", false, "")
            }
        } catch (e: Exception) {
            return Fact("", "", false, "")
        }
    }

    private fun readInputStream(stream: InputStream): String {
        val outputString = StringBuilder()
        val streamReader = InputStreamReader(stream, Charsets.UTF_8)
        val scanner = Scanner(streamReader)
        while (scanner.hasNext()) {
            outputString.append(scanner.nextLine())
        }
        return outputString.toString()
    }

    private fun jsonStringToFactObject(jsonString: String): Fact {
        Log.i("JSON_STRING", jsonString)
        return try {
            val jsonObj = JSONObject(jsonString)
            val text = jsonObj.getString("text")
            val number = jsonObj.getString("number")
            val isFound = jsonObj.getString("found").equals("true", true)
            val type = jsonObj.getString("type")
            Fact(text, number, isFound, type)
        } catch (je: JSONException) {
            Log.i("JSON_ERROR", je.message)
            Fact("", "", false, "")
        }
    }
}
package com.example.minstrm

// Husk imports:
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import android.content.Context
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject
import java.io.InputStream

private const val OPENAI_API_KEY = "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" // <- udskift med din egen nøgle


data class DeviceInfo(
    val produkt: String,
    val model: String,
    val effekt: String,
    val estimeretTid: String
)

suspend fun parseLLMResponse(context: Context, imageUri: Uri): DeviceInfo = withContext(Dispatchers.IO) {
    val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
    val imageBytes = inputStream?.readBytes()
    inputStream?.close()

    if (imageBytes == null) throw IllegalArgumentException("Billedet kunne ikke læses")

    val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

    val json = """
        {
          "model": "gpt-4o",
          "messages": [
            {
              "role": "user",
              "content": [
                { "type": "text", "text": "Uddrag modelnummer, produktnavn, effekt (watt), og typisk programtid fra dette billede. Svar i kun følgende JSON-format uden forklaringer: { \"produkt\": \"\", \"model\": \"\", \"effekt\": \"\", \"estimeretTid\": \"\" }" },
                { "type": "image_url", "image_url": { "url": "data:image/jpeg;base64,$base64Image" } }
              ]
            }
          ]
        }
    """.trimIndent()

    val body = RequestBody.create("application/json".toMediaTypeOrNull(), json)

    val request = Request.Builder()
        .url("https://api.openai.com/v1/chat/completions")
        .header("Authorization", "Bearer $OPENAI_API_KEY")
        .post(body)
        .build()

    val client = OkHttpClient()
    val response = client.newCall(request).execute()

    if (!response.isSuccessful) throw Exception("OpenAI API fejl: ${response.code}")

    val responseBody = response.body?.string() ?: throw Exception("Tomt svar fra API")
    val responseJson = JSONObject(responseBody)
    val content = responseJson
        .getJSONArray("choices")
        .getJSONObject(0)
        .getJSONObject("message")
        .getString("content")

    val parsed = JSONObject(content)

    return@withContext DeviceInfo(
        produkt = parsed.getString("produkt"),
        model = parsed.getString("model"),
        effekt = parsed.getString("effekt"),
        estimeretTid = parsed.getString("estimeretTid")
    )
}

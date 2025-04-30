package com.example.minstrm


import okhttp3.MediaType.Companion.toMediaType
import android.util.Log
import okhttp3.RequestBody.Companion.toRequestBody
import android.content.Context
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONObject


data class DeviceInfo(
    val produkt: String,
    val model: String,
    val effekt: String,
    val estimeretTid: String
)

suspend fun parseLLMResponse(context: Context, imageUri: Uri): DeviceInfo = withContext(Dispatchers.IO) {
    val client = OkHttpClient()

    val inputStream = context.contentResolver.openInputStream(imageUri)
        ?: throw Exception("Kunne ikke åbne billede")
    val imageBytes = inputStream.readBytes()
    val imageBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

    val systemPrompt = """
        Du er en billedmodel, som modtager billeder af husholdningsapparater som f.eks. vaskemaskiner, kaffemaskiner, elkedler osv.
        Din opgave er at identificere:
        - produktnavn eller type (f.eks. vaskemaskine, elkedel)
        - modelnavn eller nummer (hvis det kan findes, aflæses eller gættes)
        - et kvalificeret gæt på effekt i watt (effekt), baseret på enhedens type og mærke
        - et kvalificeret gæt på estimeret programtid i minutinterval (estimeretTid)

        Returnér udelukkende et JSON-objekt med disse nøgler: produkt, model, effekt, estimeretTid.
        Hvis du ikke er helt sikker, så brug det mest sandsynlige gæt. Brug tom streng kun hvis der slet intet kan udledes.
    """.trimIndent()

    val messages = """
        [
          {
            "role": "system",
            "content": ${JSONObject.quote(systemPrompt)}
          },
          {
            "role": "user",
            "content": [
              {
                "type": "image_url",
                "image_url": {
                  "url": "data:image/jpeg;base64,$imageBase64",
                  "detail": "low"
                }
              }
            ]
          }
        ]
    """.trimIndent()

    val json = """
        {
          "model": "gpt-4o",
          "messages": $messages,
          "temperature": 0.2
        }
    """.trimIndent()

    val body = json.toRequestBody("application/json".toMediaType())
    val request = Request.Builder()
        .url("https://api.openai.com/v1/chat/completions")
        .post(body)
        .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
        .build()

    val response = client.newCall(request).execute()
    val responseBody = response.body?.string()

    if (!response.isSuccessful || responseBody == null) {
        Log.e("OpenAI_API_ERROR", "Fejl ved forespørgsel: ${response.code}")
        throw Exception("Fejl ved OpenAI API")
    }

    Log.d("OpenAI_RESPONSE", responseBody)

    try {
        val jsonResponse = JSONObject(responseBody)
        val content = jsonResponse
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")

        Log.d("OpenAI_CONTENT", content)

        val cleanedJson = content
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val parsed = JSONObject(cleanedJson)

        return@withContext DeviceInfo(
            produkt = parsed.optString("produkt", ""),
            model = parsed.optString("model", ""),
            effekt = parsed.optString("effekt", ""),
            estimeretTid = parsed.optString("estimeretTid", "")
        )
    } catch (e: Exception) {
        Log.e("OpenAI_API_ERROR", "Fejl under OpenAI-kald", e)
        throw Exception("OpenAI returnerede ikke gyldig JSON.")
    }
}

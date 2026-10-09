package com.example.englishcentre.data

import com.example.englishcentre.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** OpenRouter chat completions (same provider as web_languagecentre). Key and model come from local.properties. */
object Ai {
    val configured get() = BuildConfig.OPENROUTER_API_KEY.isNotBlank()

    /** messages: role ("system" | "user" | "assistant") to content. Throws with a readable message on failure. */
    suspend fun ask(messages: List<Pair<String, String>>): String = withContext(Dispatchers.IO) {
        val c = URL(BuildConfig.OPENROUTER_URL).openConnection() as HttpURLConnection
        c.requestMethod = "POST"
        c.doOutput = true
        c.connectTimeout = 15000
        c.readTimeout = 60000
        c.setRequestProperty("Authorization", "Bearer ${BuildConfig.OPENROUTER_API_KEY}")
        c.setRequestProperty("Content-Type", "application/json")
        val body = JSONObject().put("model", BuildConfig.OPENROUTER_MODEL)
            .put("messages", JSONArray(messages.map { (role, text) -> JSONObject().put("role", role).put("content", text) }))
        c.outputStream.use { it.write(body.toString().toByteArray()) }
        val ok = c.responseCode < 400
        val text = (if (ok) c.inputStream else c.errorStream).bufferedReader().use { it.readText() }
        c.disconnect()
        if (!ok) error("AI error ${c.responseCode}: ${text.take(200)}")
        JSONObject(text).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
    }
}

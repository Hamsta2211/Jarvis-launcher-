package com.example.data.service

import com.example.data.model.TavilySearchItem
import com.example.data.model.TavilySearchResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class TavilySearchService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun search(apiKey: String, query: String): Result<TavilySearchResponse> =
        withContext(Dispatchers.IO) {
            try {
                if (apiKey.isBlank()) {
                    return@withContext Result.failure(
                        IllegalArgumentException("Tavily API-Key fehlt. Bitte in den Einstellungen hinterlegen.")
                    )
                }

                val jsonBody = JSONObject().apply {
                    put("api_key", apiKey)
                    put("query", query)
                    put("include_answer", true)
                    put("search_depth", "basic")
                    put("max_results", 5)
                }

                val requestBody = jsonBody.toString()
                    .toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url("https://api.tavily.com/search")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    val bodyString = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        return@withContext Result.failure(
                            Exception("Tavily HTTP Fehler ${response.code}: $bodyString")
                        )
                    }

                    val json = JSONObject(bodyString)
                    val answer = json.optString("answer", "").ifBlank { null }
                    val resultsArr = json.optJSONArray("results")
                    val items = mutableListOf<TavilySearchItem>()

                    if (resultsArr != null) {
                        for (i in 0 until resultsArr.length()) {
                            val itemObj = resultsArr.getJSONObject(i)
                            items.add(
                                TavilySearchItem(
                                    title = itemObj.optString("title", "Kein Titel"),
                                    url = itemObj.optString("url", ""),
                                    content = itemObj.optString("content", "")
                                )
                            )
                        }
                    }

                    Result.success(
                        TavilySearchResponse(
                            query = query,
                            answer = answer,
                            results = items
                        )
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}

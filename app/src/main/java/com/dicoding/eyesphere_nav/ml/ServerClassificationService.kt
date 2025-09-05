package com.dicoding.eyesphere_nav.ml

import android.content.Context
import android.util.Log
import com.dicoding.eyesphere_nav.data.database.DatabaseHelper
import com.dicoding.eyesphere_nav.data.database.ProcessingStatus
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Service for communicating with the eye classification server.
 * 
 * SSL Handling:
 * - First attempts HTTPS with SSL certificate validation bypassed (for development)
 * - Falls back to HTTP if SSL issues occur
 * - Includes comprehensive error handling and logging
 * 
 * WARNING: SSL certificate bypass is for development/testing only.
 * In production, proper SSL certificates should be used.
 */
class ServerClassificationService {
    
    companion object {
        private const val TAG = "ServerClassificationService"
        private const val BASE_URL_HTTPS = "https://novilserv.my.id"
        private const val BASE_URL_HTTP = "http://novilserv.my.id"
        private const val PREDICT_ENDPOINT = "/predict"
        
        suspend fun classifyImage(
            context: Context,
            itemId: Long,
            imagePath: String,
            onProgressUpdate: (Int) -> Unit
        ): Pair<String, Float> {
            return withContext(Dispatchers.IO) {
                try {
                    Log.d(TAG, "Starting server classification for item $itemId")
                    
                    // Simulate progress updates
                    onProgressUpdate(10)
                    delay(200)
                    onProgressUpdate(30)
                    delay(200)
                    onProgressUpdate(50)
                    delay(200)
                    onProgressUpdate(70)
                    delay(200)
                    onProgressUpdate(90)
                    delay(200)
                    
                    // Perform actual classification
                    val result = performServerClassification(context, itemId, imagePath)
                    onProgressUpdate(100)
                    
                    Log.d(TAG, "Server classification completed: ${result.first} (${result.second})")
                    result
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error in server classification for item $itemId", e)
                    throw e
                }
            }
        }
        
        private suspend fun performServerClassification(context: Context, itemId: Long, imagePath: String): Pair<String, Float> {
            return withContext(Dispatchers.IO) {
                try {
                    // Try HTTPS first with SSL bypass
                    try {
                        performHttpsRequest(context, itemId, imagePath)
                    } catch (e: Exception) {
                        if (e is javax.net.ssl.SSLHandshakeException || 
                            e is javax.net.ssl.SSLPeerUnverifiedException ||
                            e.message?.contains("SSL") == true ||
                            e.message?.contains("certificate") == true) {
                            
                            Log.w(TAG, "HTTPS failed due to SSL issues, trying HTTP fallback", e)
                            // Fallback to HTTP
                            performHttpRequest(context, itemId, imagePath)
                        } else {
                            throw e
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Both HTTPS and HTTP requests failed", e)
                    throw e
                }
            }
        }
        
        private suspend fun performHttpsRequest(context: Context, itemId: Long, imagePath: String): Pair<String, Float> {
            return withContext(Dispatchers.IO) {
                // WARNING: This bypasses SSL certificate validation for development/testing purposes
                // In production, proper SSL certificates should be used
                // Create SSL context that trusts all certificates (for development/testing)
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                })
                
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())
                
                val client = OkHttpClient.Builder()
                    .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                    .hostnameVerifier { _, _ -> true } // Skip hostname verification
                    .addInterceptor { chain ->
                        val request = chain.request()
                        Log.d(TAG, "HTTPS Request: ${request.method} ${request.url}")
                        val response = chain.proceed(request)
                        Log.d(TAG, "HTTPS Response: ${response.code}")
                        response
                    }
                    .build()
                
                val imageFile = File(imagePath)
                if (!imageFile.exists()) {
                    throw IOException("Image file not found: $imagePath")
                }
                
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "image",
                        imageFile.name,
                        imageFile.asRequestBody("image/*".toMediaType())
                    )
                    .build()
                
                val request = Request.Builder()
                    .url("$BASE_URL_HTTPS$PREDICT_ENDPOINT")
                    .post(requestBody)
                    .build()
                
                Log.d(TAG, "Sending HTTPS request to server: $BASE_URL_HTTPS$PREDICT_ENDPOINT")
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Server error: ${response.code} ${response.message}")
                    }
                    
                    val responseBody = response.body?.string()
                        ?: throw IOException("Empty response from server")
                    
                    Log.d(TAG, "HTTPS Server response: $responseBody")
                    
                    // Parse server response
                    val jsonResponse = JSONObject(responseBody)
                    
                    if (jsonResponse.getBoolean("success")) {
                        val data = jsonResponse.getJSONObject("data")
                        val diagnosis = data.getJSONObject("diagnosis")
                        val medicalRecommendation = data.getString("medical_recommendation")
                        
                        val className = diagnosis.getString("class_name")
                        val confidence = diagnosis.getDouble("confidence").toFloat() / 100f // Convert from percentage to 0-1
                        
                        // Save medical recommendation to database
                        saveMedicalRecommendation(context, itemId, medicalRecommendation)
                        
                        // Use Android's timestamp instead of server timestamp
                        Log.d(TAG, "HTTPS Classification completed successfully: $className ($confidence)")
                        
                        Pair(className, confidence)
                    } else {
                        throw IOException("Server returned error: ${jsonResponse.optString("message", "Unknown error")}")
                    }
                }
            }
        }
        
        private suspend fun performHttpRequest(context: Context, itemId: Long, imagePath: String): Pair<String, Float> {
            return withContext(Dispatchers.IO) {
                val client = OkHttpClient.Builder()
                    .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .addInterceptor { chain ->
                        val request = chain.request()
                        Log.d(TAG, "HTTP Request: ${request.method} ${request.url}")
                        val response = chain.proceed(request)
                        Log.d(TAG, "HTTP Response: ${response.code}")
                        response
                    }
                    .build()
                
                val imageFile = File(imagePath)
                if (!imageFile.exists()) {
                    throw IOException("Image file not found: $imagePath")
                }
                
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "image",
                        imageFile.name,
                        imageFile.asRequestBody("image/*".toMediaType())
                    )
                    .build()
                
                val request = Request.Builder()
                    .url("$BASE_URL_HTTP$PREDICT_ENDPOINT")
                    .post(requestBody)
                    .build()
                
                Log.d(TAG, "Sending HTTP fallback request to server: $BASE_URL_HTTP$PREDICT_ENDPOINT")
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("Server error: ${response.code} ${response.message}")
                    }
                    
                    val responseBody = response.body?.string()
                        ?: throw IOException("Empty response from server")
                    
                    Log.d(TAG, "HTTP Server response: $responseBody")
                    
                    // Parse server response
                    val jsonResponse = JSONObject(responseBody)
                    
                    if (jsonResponse.getBoolean("success")) {
                        val data = jsonResponse.getJSONObject("data")
                        val diagnosis = data.getJSONObject("diagnosis")
                        val medicalRecommendation = data.getString("medical_recommendation")
                        
                        val className = diagnosis.getString("class_name")
                            .replace("_", " ") // Convert snake_case to readable format
                            .replaceFirstChar { it.uppercase() } // Capitalize first letter
                        
                        val confidence = diagnosis.getDouble("confidence").toFloat() / 100f // Convert from percentage to 0-1
                        
                        // Save medical recommendation to database
                        saveMedicalRecommendation(context, itemId, medicalRecommendation)
                        
                        // Use Android's timestamp instead of server timestamp
                        Log.d(TAG, "HTTP Classification completed successfully: $className ($confidence)")
                        
                        Pair(className, confidence)
                    } else {
                        throw IOException("Server returned error: ${jsonResponse.optString("message", "Unknown error")}")
                    }
                }
            }
        }
        
        private fun saveMedicalRecommendation(context: Context, itemId: Long, recommendation: String) {
            try {
                // Process the recommendation text to convert markdown formatting
                val processedRecommendation = processRecommendationText(recommendation)
                
                val databaseHelper = DatabaseHelper(context)
                databaseHelper.updateRecommendation(itemId, processedRecommendation)
                databaseHelper.close()
                Log.d(TAG, "Medical recommendation saved for item $itemId")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving medical recommendation for item $itemId", e)
            }
        }
        
        /**
         * Process recommendation text to convert markdown formatting to Android-compatible format
         * 
         * Converts:
         * - **text** → <b>text</b> (bold)
         * - __text__ → <b>text</b> (bold alternative)
         * - *text* → <i>text</i> (italic)
         * - \n → <br> (line breaks)
         * - • and - → bullet points with proper formatting
         * 
         * Also formats:
         * - Medical measurements (%, mmHg, HbA1c values)
         * - Important medical terms (Dokter Spesialis Mata, segera, penting)
         * - Ensures proper spacing and readability
         */
        private fun processRecommendationText(recommendation: String): String {
            return try {
                var processedText = recommendation
                
                // Convert **text** to bold format (using HTML tags for Android)
                processedText = processedText.replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
                
                // Convert __text__ to bold format (alternative markdown syntax)
                processedText = processedText.replace(Regex("__(.*?)__"), "<b>$1</b>")
                
                // Convert *text* to italic format (if not already processed)
                processedText = processedText.replace(Regex("(?<!\\*)\\*(?!\\*)(.*?)(?<!\\*)\\*(?!\\*)"), "<i>$1</i>")
                
                // Remove any remaining single asterisks that weren't part of formatting
                processedText = processedText.replace(Regex("(?<!<[bi]>)\\*(?![bi]>|\\*)"), "")
                
                // Convert line breaks to proper format
                processedText = processedText.replace("\n", "<br>")
                
                // Convert bullet points to proper format
                processedText = processedText.replace(Regex("^\\s*•\\s*", RegexOption.MULTILINE), "• ")
                processedText = processedText.replace(Regex("^\\s*-\\s*", RegexOption.MULTILINE), "• ")
                
                // Clean up any extra spaces and formatting
                processedText = processedText.trim()
                
                // Ensure proper spacing around HTML tags
                processedText = processedText.replace(Regex(">\\s+<"), "><")
                processedText = processedText.replace(Regex("\\s+<"), " <")
                processedText = processedText.replace(Regex(">\\s+"), "> ")
                
                // Format medical measurements and numbers
                processedText = processedText.replace(Regex("(\\d+)\\s*%"), "<b>$1%</b>")
                processedText = processedText.replace(Regex("(\\d+)\\s*mmHg"), "<b>$1 mmHg</b>")
                processedText = processedText.replace(Regex("HbA1c\\s*<\\s*(\\d+)"), "HbA1c < <b>$1</b>")
                
                // Format important medical terms
                processedText = processedText.replace(Regex("\\b(Dokter Spesialis Mata)\\b", RegexOption.IGNORE_CASE), "<b>$1</b>")
                processedText = processedText.replace(Regex("\\b(segera)\\b", RegexOption.IGNORE_CASE), "<b>$1</b>")
                processedText = processedText.replace(Regex("\\b(penting)\\b", RegexOption.IGNORE_CASE), "<b>$1</b>")
                
                // Clean up any double HTML tags that might have been created
                processedText = processedText.replace("<b><b>", "<b>")
                processedText = processedText.replace("</b></b>", "</b>")
                processedText = processedText.replace("<i><i>", "<i>")
                processedText = processedText.replace("</i></i>", "</i>")
                
                // Final cleanup: remove any empty HTML tags
                processedText = processedText.replace("<b></b>", "")
                processedText = processedText.replace("<i></i>", "")
                
                // Ensure proper paragraph breaks
                processedText = processedText.replace("<br><br><br>", "<br><br>")
                processedText = processedText.replace("<br><br><br>", "<br><br>")
                
                // Add some spacing for better readability
                processedText = processedText.replace("• ", "<br>• ")
                
                Log.d(TAG, "Processed recommendation text: ${processedText.take(100)}...")
                processedText
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing recommendation text", e)
                // Return original text if processing fails
                recommendation
            }
        }
    }
}

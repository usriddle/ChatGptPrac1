package com.example.chatgptprac1

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.create
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

//https://platform.openai.com/docs/guides/gpt/chat-completions-api

class chatGPT {

    var messages by mutableStateOf(mutableStateListOf<Message>())
    val json: MediaType = "application/json; charset=utf-8".toMediaType()
    val key="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"
    val arr = JSONArray()
    val baseAi = JSONObject()
    val userMsg = JSONObject()
    val jsonObject = JSONObject()

    fun callAPI(question: String?){
        //okhttp

        try {

            baseAi.put("role", "system");
            baseAi.put("content", "You should speak to korean")
            //유저 메세지
            userMsg.put("role", "user")
            userMsg.put("content", question)

            jsonObject.put("model", "gpt-3.5-turbo")
            jsonObject.put("messages", arr)
            //array로 담아서 한번에 보낸다
            arr.put(baseAi)
            arr.put(userMsg)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val body: RequestBody = jsonObject.toString().toRequestBody(json)
        val request: Request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions") //url 경로 수정됨
            .header("Authorization", "Bearer $key")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                addResponse("Failed to load response due to " + e.message)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    var jsonObject: JSONObject?
                    try {
                        jsonObject = JSONObject(response.body!!.string())
                        val jsonArray = jsonObject.getJSONArray("choices")
                        val result =
                            jsonArray.getJSONObject(0).getJSONObject("message").getString("content")
                        addResponse(result.trim())
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    addResponse("Failed to load response due to " + response.body!!.string())
                }
            }
        })
    }
    fun addResponse(response: String?) {
        if (response != null) {
            messages.add(Message(false, response))
            MainActivity.ps.edit().putString("gpt"+(MainActivity.num+1),response).apply()
            MainActivity.ps.edit().putInt("chatNum",++(MainActivity.num)).apply()
        }
    }

}
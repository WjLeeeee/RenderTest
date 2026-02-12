package com.woojin.android_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.woojin.android_app.ui.theme.BackendTestTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 여기에 내 컴퓨터 IP를 입력하세요! (예: 192.168.0.15)
            // 포트 번호(:8080)도 잊지 마세요.
            val myPcIpAddress = "http://192.168.0.98:8080/"

            var resultText by remember { mutableStateOf("휴대폰에서 서버 찾는 중...") }
            var inputText by remember { mutableStateOf("") } //post용
            var messageList by remember { mutableStateOf(listOf<String>()) } //저장된 데이터 불러오기 용
            val scope = rememberCoroutineScope()

            // Retrofit 객체 생성 (한 번만 만들어서 재사용)
            val api = remember {
                Retrofit.Builder()
                    .baseUrl(myPcIpAddress)
                    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
                    .build()
                    .create(MyApiService::class.java)
            }

            Scaffold { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(20.dp)
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text("서버에 할 말") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    // 서버로 전송 (DB 저장)
                                    api.sendMessage(UserRequest(inputText))
                                    inputText = "" // 입력창 비우기

                                    // 저장 후 즉시 목록 새로고침!
                                    messageList = api.getAllMessages()
                                } catch (e: Exception) {
                                    resultText = "에러: ${e.message}"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Text("전송하기 (POST)")
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val retrofit = Retrofit.Builder()
                                        .baseUrl(myPcIpAddress)
                                        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
                                        .build()

                                    val api = retrofit.create(MyApiService::class.java)
                                    val response = api.getData()

                                    resultText = "성공!\n\nID: ${response.id}\n메시지: ${response.message}\n버전: ${response.version}"
                                } catch (e: Exception) {
                                    resultText = "실패... ㅠㅠ\n\n에러: ${e.message}\n\n"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("기본 데이터 가져오기")
                    }

                    LazyColumn {
                        items(messageList) { msg ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Text(
                                    text = "📩 $msg",
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    messageList = api.getAllMessages()
                                } catch (e: Exception) {}
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                    ) {
                        Text("목록 새로고침")
                    }

                    Text(
                        text = resultText,
                        modifier = Modifier.padding(top = 20.dp)
                    )
                }
            }
        }
    }
}

@Serializable
data class MyData(val id: Int, val message: String, val version: String)

@Serializable
data class UserRequest(val userMessage: String)

// API 인터페이스
interface MyApiService {
    @GET("api/data")
    suspend fun getData(): MyData

    @POST("api/send")
    suspend fun sendMessage(@Body request: UserRequest): MyData

    @GET("api/messages")
    suspend fun getAllMessages(): List<String>
}

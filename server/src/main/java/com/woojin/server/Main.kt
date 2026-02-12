package com.woojin.server

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils
import kotlinx.serialization.Serializable

@Serializable
data class MyData(
    val id: Int,
    val message: String,
    val version: String
)

@Serializable
data class UserRequest(
    val userMessage: String
)

object Messages : Table() {
    val id = integer("id").autoIncrement() // 1, 2, 3... 자동 증가
    val content = varchar("content", 255) // 내용 (최대 255자)

    override val primaryKey = PrimaryKey(id)
}

fun main() {
    // DB 연결 (파일로 저장: ./my_db 라는 파일이 생깁니다)
    val port = System.getenv("PORT")?.toInt() ?: 8080
    Database.connect("jdbc:h2:./my_db", driver = "org.h2.Driver", user = "root", password = "")
    // 테이블 생성 (앱 켜질 때 테이블 없으면 만듦)
    transaction {
        SchemaUtils.create(Messages)
    }
    // 서버 엔진 설정 (Netty 사용, 8080 포트)
    embeddedServer(Netty, port = port, host = "0.0.0.0") {

        // JSON 변환 플러그인 설치 (안드로이드의 Retrofit Converter 역할)
        install(ContentNegotiation) {
            json()
        }

        // 라우팅 (엔드포인트 설정)
        routing {
            get("/api/messages") {
                val messageList = transaction {
                    // SELECT * FROM Messages -> 리스트로 변환
                    Messages.selectAll().map { it[Messages.content] }
                }
                call.respond(messageList) // ["안녕", "반가워", ...] 형태로 반환
            }

            get("/api/data") {
                val responseData = MyData(
                    id = 1,
                    message = "Hello! This is a JSON from your local server.",
                    version = "1.0.0"
                )
                // JSON으로 자동 변환되어 클라이언트에 전달됨
                call.respond(responseData)
            }

            post("/api/send") {
                val request = call.receive<UserRequest>()
                // ★ DB에 INSERT ★
                transaction {
                    Messages.insert {
                        it[content] = request.userMessage
                    }
                }
                println("💾 DB 저장 완료: ${request.userMessage}")

                // 잘 받았다고 응답 보내기
                val response = MyData(
                    id = 200,
                    message = "저장 완료!",
                    version = "2.0"
                )
                call.respond(response)
            }
        }
    }.start(wait = true)
}
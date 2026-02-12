# 1. 짐 쌀 상자 준비 (Java 17 버전 사용)
FROM amazoncorretto:17-alpine-jdk

# 2. 작업 폴더 생성
WORKDIR /app

# 3. 프로젝트 파일 전체 복사
COPY . .

# 4. gradlew 실행 권한 부여 (맥/리눅스 호환용)
RUN chmod +x ./gradlew

# 5. 빌드 실행 (짐 싸기!)
# 안드로이드 관련 작업 제외하고 서버만 빌드
RUN ./gradlew :server:shadowJar --no-daemon

# 6. 서버 실행 (포트 8080 열기)
# Render가 자동으로 PORT 환경변수를 주지만, 명시적으로 적어줍니다.
ENV PORT=8080
EXPOSE 8080

# 7. 실행 명령어
CMD ["java", "-jar", "server/build/libs/server-1.0.0-all.jar"]
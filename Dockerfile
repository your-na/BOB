# 1. OpenJDK 17 이미지 사용
FROM openjdk:17-jdk-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. Gradle 빌드 결과 복사
COPY build/libs/*.jar app.jar

# 4. 서버 실행
ENTRYPOINT ["java","-jar","app.jar"]

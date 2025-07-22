package com.example.bob.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class GroqService {

    @Value("${groq.api-key}")
    private String apiKey;

    @Value("${groq.endpoint}")
    private String endpoint;

    public String ask(String message) {
        try {
            System.out.println("📨 ask() 호출됨, 사용자 메시지: " + message);
            System.out.println("🔑 API 키 일부: " + (apiKey != null ? apiKey.substring(0, 5) + "..." : "null"));
            System.out.println("🌐 Endpoint: " + endpoint);

            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");

            String body = """
                {
                  "model": "llama3-8b-8192",
                  "messages": [
                           { "role": "system", "content": "모든 답변은 반드시 한국어로 해주세요." },
                           { "role": "user", "content": "%s" }
                         ]
                }
                """.formatted(message);

            System.out.println("📤 요청 본문: " + body);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            InputStream is = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode response = mapper.readTree(is);

            System.out.println("📥 응답 전체: " + response.toPrettyString());

            if (responseCode != 200) {
                return "⚠ Groq API 오류: " + response.path("error").path("message").asText("응답 형식 오류");
            }

            // ✅ 응답 구조 안전하게 접근
            JsonNode choices = response.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode contentNode = choices.get(0).path("message").path("content");
                return contentNode.asText("⚠ AI 응답 내용을 불러올 수 없습니다.");
            } else {
                return "⚠ Groq 응답 형식이 예상과 다릅니다.";
            }

        } catch (Exception e) {
            System.err.println("❌ 예외 발생: " + e.getMessage());
            e.printStackTrace();
            return "⚠ Groq 응답 오류가 발생했습니다.";
        }
    }
}

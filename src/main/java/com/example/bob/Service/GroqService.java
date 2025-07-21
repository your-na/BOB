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
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setRequestProperty("Content-Type", "application/json");

            String body = """
                {
                  "model": "mixtral-8x7b-32768",
                  "messages": [
                    { "role": "user", "content": "%s" }
                  ]
                }
                """.formatted(message);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            try (InputStream is = conn.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode response = mapper.readTree(is);
                return response.get("choices").get(0).get("message").get("content").asText();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠ Groq 응답 오류가 발생했습니다.";
        }
    }
}

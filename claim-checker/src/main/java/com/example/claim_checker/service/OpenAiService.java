package com.example.claim_checker.service;

import com.example.claim_checker.model.ClaimRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    @Value("${ollama.api.url}")
    private String apiUrl;

    @Value("${ollama.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String classifyClaim(ClaimRequest description) {
        try {
            OkHttpClient client = new OkHttpClient();

            String prompt = "Ты — ассистент медицинской страховой компании. "
                    + "На основе описания страхового случая ответь только одним из двух вариантов: "
                    + "\"Вероятно одобрить\", \"Вероятно отказать\". "
                    + "Описание: " + description.getDescription();

            // JSON без лишних переносов строк
            String requestBody = objectMapper.writeValueAsString(
                    new PromptRequest(model, prompt)
            );

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println(response.toString());
                    return "Нужна дополнительная проверка (ошибка AI)";
                }

                String responseBody = response.body().string();
                StringBuilder fullResponse = new StringBuilder();

                // Разделяем на строки и собираем все response
                String[] lines = responseBody.split("\\r?\\n");
                for (String line : lines) {
                    JsonNode json = objectMapper.readTree(line);
                    if (json.has("response")) {
                        fullResponse.append(json.get("response").asText());
                    }
                }

                return fullResponse.toString().trim();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Нужна дополнительная проверка (ошибка AI)";
        }
    }

    static class PromptRequest {
        public String model;
        public String prompt;

        public PromptRequest(String model, String prompt) {
            this.model = model;
            this.prompt = prompt;
        }
    }
}

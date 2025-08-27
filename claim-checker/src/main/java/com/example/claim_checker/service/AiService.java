package com.example.claim_checker.service;

import com.example.claim_checker.exception.InvalidUserClaimException;
import com.example.claim_checker.model.ClaimRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${ollama.api.url}")
    private String apiUrl;

    @Value("${ollama.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String classifyClaim(ClaimRequest description) {
        try {
            OkHttpClient client = new OkHttpClient();

            String prompt = "You are an assistant at a health insurance company. "
                    + "Based on the description of an insurance claim, make a verdict using only one of two options: "
                    + "\"Likely to approve\", \"Likely to deny\". Then justify your decision."
                    + "Description: " + description.getDescription();

            String requestBody = objectMapper.writeValueAsString(
                    new PromptRequest(model, prompt)
            );

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new InvalidUserClaimException(HttpStatus.valueOf(503), "Error during connection with AI");
                }

                String responseBody = response.body().string();
                StringBuilder fullResponse = new StringBuilder();

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
            throw new InvalidUserClaimException(HttpStatus.valueOf(500), "Error during solving claim");
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

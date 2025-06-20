package com.anhtester.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class GeminiAPIClientHelper {
    private static final String API_KEY = ""; //Get API key https://aistudio.google.com/apikey
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    public static String sendErrorForGeminiAI(String prompt) {
        // Tạo client OkHttp với timeout được cấu hình đầy đủ
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(180, TimeUnit.SECONDS)      // Timeout kết nối
                .readTimeout(180, TimeUnit.SECONDS)         // Timeout đọc dữ liệu
                .writeTimeout(180, TimeUnit.SECONDS)        // Timeout ghi dữ liệu
                .callTimeout(180, TimeUnit.SECONDS)         // Timeout tổng cho toàn bộ call
                .retryOnConnectionFailure(false)            // Tắt auto retry để control manually
                .protocols(Arrays.asList(Protocol.HTTP_1_1))
                .build();

        // Xây dựng JSON với cấu hình để đảm bảo response đầy đủ
        Gson gson = new Gson();
        JsonObject payload = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject content = new JsonObject();
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        payload.add("contents", contents);

        // Thêm generationConfig để tối ưu cho response dài
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.7);          // Giảm randomness
        generationConfig.addProperty("maxOutputTokens", 8192);     // Tăng lên 8192 tokens (max cho Gemini 2.0)
        generationConfig.addProperty("topP", 0.8);                 // Cải thiện quality
        generationConfig.addProperty("topK", 40);                  // Thêm control
        // Không set candidateCount để dùng default = 1 (tiết kiệm tokens)
        payload.add("generationConfig", generationConfig);

        // Thêm safetySettings để tránh bị block
        JsonArray safetySettings = new JsonArray();
        String[] categories = {
                "HARM_CATEGORY_HARASSMENT",
                "HARM_CATEGORY_HATE_SPEECH",
                "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                "HARM_CATEGORY_DANGEROUS_CONTENT"
        };

        for (String category : categories) {
            JsonObject setting = new JsonObject();
            setting.addProperty("category", category);
            setting.addProperty("threshold", "BLOCK_MEDIUM_AND_ABOVE");
            safetySettings.add(setting);
        }
        payload.add("safetySettings", safetySettings);

        String json = gson.toJson(payload);
        System.out.println("Request payload size: " + json.length() + " characters");

        // Tạo request với headers đầy đủ
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "SeleniumTestClient/1.0")
                .addHeader("Accept", "application/json")
                .addHeader("Connection", "keep-alive")
                .build();

        // Thực hiện request với monitoring chi tiết
        long startTime = System.currentTimeMillis();
        System.out.println("Starting API call to Gemini...");

        try (Response response = client.newCall(request).execute()) {
            long responseReceivedTime = System.currentTimeMillis();
            long timeToResponse = responseReceivedTime - startTime;
            System.out.println("Response received after: " + timeToResponse + "ms");

            // Kiểm tra mã trạng thái trước
            System.out.println("Response status: " + response.code());
//            System.out.println("Response headers: " + response.headers());

            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No response body";
                System.err.println("API request failed: Status " + response.code());
                System.err.println("Error response: " + errorBody);
                return "";
            }

            // Đọc response body
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                System.err.println("Response body is null");
                return "";
            }

            String responseString = responseBody.string();
            long totalTime = System.currentTimeMillis() - startTime;
            System.out.println("Total processing time: " + totalTime + "ms");
            System.out.println("Response body size: " + responseString.length() + " characters");

            // Log first 200 characters của response để debug
            if (responseString.length() > 0) {
                String preview = responseString.length() > 200 ?
                        responseString.substring(0, 200) + "..." : responseString;
                System.out.println("Response preview: " + preview);
            }

            // Parse JSON response
            try {
                JsonObject responseJson = gson.fromJson(responseString, JsonObject.class);

                // Kiểm tra có lỗi trong response không
                if (responseJson.has("error")) {
                    JsonObject error = responseJson.getAsJsonObject("error");
                    System.err.println("API returned error: " + error.toString());
                    return "";
                }

                // Kiểm tra candidates
                if (!responseJson.has("candidates")) {
                    System.err.println("No 'candidates' field in response");
                    System.err.println("Full response: " + responseString);
                    return "";
                }

                JsonArray candidates = responseJson.getAsJsonArray("candidates");
                if (candidates == null || candidates.size() == 0) {
                    System.err.println("Empty candidates array");
                    return "";
                }

                // Lấy candidate đầu tiên
                JsonObject firstCandidate = candidates.get(0).getAsJsonObject();

                // Kiểm tra finishReason
                if (firstCandidate.has("finishReason")) {
                    String finishReason = firstCandidate.get("finishReason").getAsString();
                    System.out.println("Finish reason: " + finishReason);

                    if ("MAX_TOKENS".equals(finishReason)) {
                        System.err.println("⚠️  CẢNH BÁO: Response bị cắt do đạt MAX_TOKENS limit!");
                        System.err.println("💡 Giải pháp: Chia nhỏ prompt hoặc tăng maxOutputTokens");
                        // Vẫn tiếp tục lấy text có sẵn
                    } else if ("SAFETY".equals(finishReason)) {
                        System.err.println("⚠️  CẢNH BÁO: Response bị block do safety filters!");
                        return "";
                    } else if (!"STOP".equals(finishReason)) {
                        System.err.println("⚠️  Generation không hoàn tất. Reason: " + finishReason);
                        // Vẫn tiếp tục lấy text nếu có
                    } else {
                        System.out.println("✅ Generation completed successfully!");
                    }
                }

                // Lấy content
                if (!firstCandidate.has("content")) {
                    System.err.println("No 'content' field in candidate");
                    System.err.println("Candidate: " + firstCandidate.toString());
                    return "";
                }

                content = firstCandidate.getAsJsonObject("content");
                if (!content.has("parts")) {
                    System.err.println("No 'parts' field in content");
                    return "";
                }

                parts = content.getAsJsonArray("parts");
                if (parts.size() == 0) {
                    System.err.println("Empty parts array");
                    return "";
                }

                // Lấy tất cả text parts (có thể có nhiều parts)
                StringBuilder fullText = new StringBuilder();
                for (int i = 0; i < parts.size(); i++) {
                    JsonObject partObj = parts.get(i).getAsJsonObject();
                    if (partObj.has("text")) {
                        String partText = partObj.get("text").getAsString();
                        fullText.append(partText);
                        if (i < parts.size() - 1) {
                            fullText.append("\n"); // Thêm newline giữa các parts
                        }
                    }
                }

                String generatedText = fullText.toString();
                System.out.println("Generated text length: " + generatedText.length() + " characters");

                // Ước tính số tokens (thường 1 token ≈ 4 characters cho tiếng Anh)
                int estimatedTokens = generatedText.length() / 4;
                System.out.println("Estimated tokens used: " + estimatedTokens);

                if (generatedText.length() > 100) {
                    System.out.println("First 100 chars: " + generatedText.substring(0, 100) + "...");
                    System.out.println("Last 100 chars: ..." +
                            generatedText.substring(Math.max(0, generatedText.length() - 100)));
                } else {
                    System.out.println("Full text: " + generatedText);
                }

                return generatedText;

            } catch (Exception parseException) {
                System.err.println("Error parsing JSON response: " + parseException.getMessage());
                System.err.println("Raw response: " + responseString);
                parseException.printStackTrace();
                return "";
            }

        } catch (SocketTimeoutException e) {
            long duration = System.currentTimeMillis() - startTime;
            System.err.println("SocketTimeoutException after " + duration + "ms: " + e.getMessage());
            return "";

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            System.err.println("Exception after " + duration + "ms: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

}
package com.anhtester.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import okhttp3.Protocol;

public class GeminiAPIClientHelper {
    private static final String API_KEY = "AIzaSyDuC8P1DV3IFvP3JT5ksQ3GqLUSKipfnZ4";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY;

    public static String generateTestCase(String prompt) {
        // Tạo client OkHttp
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .protocols(Arrays.asList(Protocol.HTTP_1_1))
                .build();

        // Xây dựng JSON bằng Gson
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

        // Thêm generationConfig
        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.7);
        generationConfig.addProperty("maxOutputTokens", 500);
        payload.add("generationConfig", generationConfig);

        String json = gson.toJson(payload);

        // Tạo request
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        // Thử lại tối đa 2 lần
        int maxRetries = 2;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try (Response response = client.newCall(request).execute()) {
                // Kiểm tra mã trạng thái
                if (!response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "No response body";
                    System.err.println("Yêu cầu API thất bại: Mã trạng thái " + response.code() + ", Phản hồi: " + responseBody);
                    return "";
                }

                // Lấy phản hồi
                String responseBody = response.body() != null ? response.body().string() : "";
                //System.out.println("Mã trạng thái: " + response.code());
                //System.out.println("Phản hồi: " + responseBody);

                // Parse JSON để lấy nội dung
                JsonObject responseJson = gson.fromJson(responseBody, JsonObject.class);
                JsonArray candidates = responseJson.getAsJsonArray("candidates");
                if (candidates == null || candidates.size() == 0) {
                    System.err.println("Không tìm thấy candidates trong phản hồi API: " + responseBody);
                    return "";
                }
                String generatedText = candidates.get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();
                return generatedText;

            } catch (SocketTimeoutException e) {
                retryCount++;
                System.err.println("Timeout lần " + retryCount + ": " + e.getMessage());
                if (retryCount == maxRetries) {
                    System.err.println("Không thể kết nối sau " + maxRetries + " lần thử do timeout: " + e.getMessage());
                    return "";
                }
                try {
                    Thread.sleep(2000 * retryCount); // Đợi 2s, 4s
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    System.err.println("Đã bị gián đoạn khi chờ retry: " + ie.getMessage());
                    return "";
                }
            } catch (Exception e) {
                System.err.println("Lỗi khi gọi API: " + e.getMessage());
                return "";
            }
        }
        System.err.println("Không thể kết nối sau " + maxRetries + " lần thử");
        return "";
    }
}
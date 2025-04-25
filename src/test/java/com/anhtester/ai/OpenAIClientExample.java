package com.anhtester.ai;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.testng.annotations.Test;


public class OpenAIClientExample {
    @Test
    public void testOpenAIClient() {
        // Khởi tạo client với API key từ biến môi trường
        //String apiKey = System.getenv("OPENAI_API_KEY");
        String apiKey = "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";

        if (apiKey == null) {
            throw new IllegalArgumentException("API key is not set in environment variables.");
        }

        System.out.println("API Key: " + apiKey);

        OpenAIClient client = OpenAIOkHttpClient.builder().apiKey(apiKey).build();

        // Tạo parameters cho chat completion
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .addSystemMessage("Bạn là một trợ lý AI chuyên về testing.")
                .addUserMessage("Viết cho tôi một đoạn giới thiệu về Anh Tester.")
                .model(ChatModel.GPT_4O)
                .build();

        // Gọi API và xử lý response
        ChatCompletion chatCompletion = client.chat().completions().create(params);

        // In kết quả
        System.out.println(chatCompletion.choices().get(0).message().content());
    }
}

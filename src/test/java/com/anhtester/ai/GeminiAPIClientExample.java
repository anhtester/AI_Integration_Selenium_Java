package com.anhtester.ai;

import com.anhtester.helpers.GeminiAPIClientHelper;
import org.testng.annotations.Test;

public class GeminiAPIClientExample {
    @Test
    public void testGeminiAPIClient() {
        String prompt = "Tạo test case Selenium Java để kiểm tra chức năng đăng nhập tại trang 'https://crm.anhtester.com/admin/authentication' với email 'admin@example.com' và password '123456'.";
        String testCase = GeminiAPIClientHelper.sendErrorForGeminiAI(prompt);
        System.out.println("Kết quả: " + testCase);
    }
}

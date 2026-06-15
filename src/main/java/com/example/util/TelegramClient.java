package com.example.util;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Sends messages through the Telegram Bot API. */
public final class TelegramClient {

    private TelegramClient() {
    }

    public static void sendMessage(String token, String chatId, String message) {
        if (token == null || token.isEmpty() || chatId == null || chatId.isEmpty()) {
            System.out.println("Please enter Telegram token and chat_id.");
            return;
        }

        try {
            URL url = new URL("https://api.telegram.org/bot" + token + "/sendMessage");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

            String postData = "chat_id=" + URLEncoder.encode(chatId, StandardCharsets.UTF_8)
                    + "&text=" + URLEncoder.encode(message, StandardCharsets.UTF_8);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("Telegram send failed: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to send Telegram message: " + e.getMessage());
        }
    }
}

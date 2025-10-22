package com.igorgorbachev.SpringBootBK.service.telegram.impl;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class SimpleTelegramService {

    private final String botToken;
    private final String chatId;
    private final HttpClient httpClient;

    public SimpleTelegramService(
            @Value("${telegram.bot.token:}") String botToken,
            @Value("${telegram.chat.id:}") String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void sendMessage(String message) {
        if (botToken == null || botToken.isEmpty() || chatId == null || chatId.isEmpty()) {
            System.out.println("Telegram credentials not configured. Message: " + message);
            return;
        }

        try {
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
            String url = String.format("https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s&parse_mode=Markdown",
                    botToken, chatId, encodedMessage);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() == 200) {
                            System.out.println("Telegram message sent successfully");
                        } else {
                            System.out.println("Failed to send Telegram message. Status: " + response.statusCode() + ", Response: " + response.body());
                        }
                    })
                    .exceptionally(e -> {
                        System.out.println("Error sending Telegram message: " + e.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            System.out.println("Exception in Telegram service: " + e.getMessage());
        }
    }
}

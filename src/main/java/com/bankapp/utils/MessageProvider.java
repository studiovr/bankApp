package com.bankapp.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class MessageProvider {

    private static final ResourceBundle bundle;

    static {
        try (var inputStream = MessageProvider.class.getClassLoader().getResourceAsStream("messages.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("Файл messages.properties не найден");
            }
            System.out.println("Файл messages.properties успешно загружен");
            bundle = new PropertyResourceBundle(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при загрузке messages.properties", e);
        }
    }

    public static String getMessage(String key) {
        return bundle.getString(key);
    }
}
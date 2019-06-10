package com.lokoproject.mailing.service;


import com.haulmont.cuba.security.entity.User;


import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

public interface BotService {
    String NAME = "mailing_BotService";

    void startBot();

    void sendMessageToUser(User user, String message);

    void sendImageToUser(User user, byte[] image, String caption);
}
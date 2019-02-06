package com.lokoproject.mailing.core.telegram.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Antonlomako. created on 06.02.2019.
 */
public class CheckInBot implements BotBehavior {

    TelegramLongPollingBot bot;

    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            // Set variables
            Date currentDate = new Date();
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            InlineKeyboardMarkup inlineKeyboardMarkup =new InlineKeyboardMarkup();



            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText("Тык");
            inlineKeyboardButton.setCallbackData("Button \"Тык\" has been pressed");

            List<InlineKeyboardButton> keyboardButtons = new ArrayList<>();
            keyboardButtons.add(inlineKeyboardButton);

            List<List<InlineKeyboardButton>> rowList= new ArrayList<>();
            rowList.add(keyboardButtons);

            inlineKeyboardMarkup.setKeyboard(rowList);

            KeyboardButton button=new KeyboardButton("777");
            KeyboardRow row=new KeyboardRow();
            row.add(button);

            ReplyKeyboardMarkup replyKeyboardMarkup=new ReplyKeyboardMarkup();
            replyKeyboardMarkup.setKeyboard(Arrays.asList(row));

            if (message_text.equals("/time")) {
                String messageSend = currentDate.toString();
                SendMessage message = new SendMessage().setChatId(chat_id).setText(messageSend);
                message.setReplyMarkup(replyKeyboardMarkup);

                try {
                    bot.execute(message); // Sending our message object to user
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else {
                String messageSend = message_text;
                SendMessage message = new SendMessage().setChatId(chat_id).setText(messageSend);
                try {
                    bot.execute(message); // Sending our message object to user
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}

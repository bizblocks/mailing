package com.lokoproject.mailing.core.telegram; /**
 /**
 * @author Antonlomako. created on 02.02.2019.
 */
import com.haulmont.cuba.security.entity.User;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.generics.BotOptions;
import org.telegram.telegrambots.meta.generics.LongPollingBot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class MainBot extends AbilityBot {

    public MainBot(String botToken, String botUsername, DefaultBotOptions options) {
        super(botToken, botUsername, options);
    }


    @Override
    public int creatorId() {
        return 0;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text

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
                    execute(message); // Sending our message object to user
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else {
                String messageSend = message_text;
                SendMessage message = new SendMessage().setChatId(chat_id).setText(messageSend);
                try {
                    execute(message); // Sending our message object to user
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public void sendMessageToChat(long chat_id,String messageContent){
        SendMessage message = new SendMessage().setChatId(chat_id).setText(messageContent);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
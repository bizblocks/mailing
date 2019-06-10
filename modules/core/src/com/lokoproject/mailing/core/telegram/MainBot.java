package com.lokoproject.mailing.core.telegram; /**
 /**
 * @author Antonlomako. created on 02.02.2019.
 */

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class MainBot extends AbilityBot {

    private BotManager botManager;

    public MainBot(String botToken, String botUsername, DefaultBotOptions options) {
        super(botToken, botUsername, options);
    }


    @Override
    public int creatorId() {
        return 0;
    }

    @Override
    public void onUpdateReceived(Update update) {
        SendMessage sendMessage=botManager.processUpdate(update);
        try {
            execute(sendMessage); // Sending our message object to user
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageToChat(long chat_id,String messageContent){
        SendMessage message = new SendMessage().setChatId(chat_id).setText(messageContent);
        message.enableHtml(true);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendFileToChat(Long chatId, java.io.File file,String caption) throws TelegramApiException {

        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(chatId);
        sendDocumentRequest.setDocument(file);
        sendDocumentRequest.setCaption(caption);
        try {
            execute(sendDocumentRequest);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public BotManager getBotManager() {
        return botManager;
    }

    public void setBotManager(BotManager botManager) {
        this.botManager = botManager;
    }
}
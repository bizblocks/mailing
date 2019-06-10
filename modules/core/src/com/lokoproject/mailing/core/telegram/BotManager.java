package com.lokoproject.mailing.core.telegram;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.security.app.Authenticated;
import com.haulmont.cuba.security.app.Authentication;
import com.haulmont.cuba.security.entity.User;
import com.lokoproject.mailing.config.TelegramConfig;
import com.lokoproject.mailing.core.telegram.bot.BotBehavior;
import com.lokoproject.mailing.core.telegram.bot.CheckInBot;
import com.lokoproject.mailing.core.telegram.bot.NotificationBot;
import com.lokoproject.mailing.notification.event.TelegramNotificationEvent;
import com.lokoproject.mailing.service.IdentifierService;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Inject;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Antonlomako. created on 06.02.2019.
 */
@Component
public class BotManager {

    @Inject
    private IdentifierService identifierService;

    @Inject
    protected Authentication authentication;

    @Inject
    private TelegramConfig telegramConfig;

    private MainBot mainBot;

    private Map<StandardEntity,BotBehavior> entityBotBehaviorMap=new HashMap<>();
    private Map<Long,BotBehavior> botBehaviorMap=new HashMap<>();

    public SendMessage processUpdate(Update update){
        authentication.begin();
        long chat_id = update.getMessage().getChatId();

        BotBehavior botBehavior=getBotBehaviorForChat(chat_id);
        SendMessage sendMessage=new SendMessage().setChatId(chat_id);
        botBehavior.onUpdateReceived(sendMessage,update);
        authentication.end();
        return sendMessage;
    }

    private BotBehavior getBotBehaviorForChat(long chat_id) {

        if(botBehaviorMap.get(chat_id)==null){
            StandardEntity entity=identifierService.getEntityByIdentifier(String.valueOf(chat_id), "Telegram");
            if(entity==null){
                botBehaviorMap.put(chat_id,new CheckInBot(chat_id,telegramConfig));
            }
            else{
                BotBehavior botBehavior=new NotificationBot(entity,chat_id);
                entityBotBehaviorMap.put(entity,botBehavior);
                botBehaviorMap.put(chat_id,botBehavior);
            }
        }
       return botBehaviorMap.get(chat_id);

    }

    public void sendMessage(StandardEntity entity, String message){
        BotBehavior botBehavior=getBotBehavior(entity);
        if(botBehavior==null) return;
        mainBot.sendMessageToChat(botBehavior.getChatId(),message);
    }

    private BotBehavior getBotBehavior(StandardEntity entity){
        BotBehavior botBehavior=entityBotBehaviorMap.get(entity);
        if(botBehavior==null){
            String chatId=identifierService.getIdentifier(entity.getMetaClass().getName(),entity.getId().toString(),"Telegram");
            if(chatId!=null){
                botBehavior=new NotificationBot(entity,Long.valueOf(chatId));
                entityBotBehaviorMap.put(entity,botBehavior);
                botBehaviorMap.put(Long.valueOf(chatId),botBehavior);
            }
            else{
                return null;
            }
        }
        return botBehavior;
    }


    public void onCheckIn(StandardEntity entity, long chat_id){
        identifierService.createIdentifier(entity,String.valueOf(chat_id),"Telegram");
        botBehaviorMap.put(chat_id,new NotificationBot(entity,chat_id));
    }

    public MainBot getMainBot() {
        return mainBot;
    }

    public void setMainBot(MainBot mainBot) {
        this.mainBot = mainBot;
    }

    public void sendImage(StandardEntity entity, byte[] image, String caption) {
        BotBehavior botBehavior=getBotBehavior(entity);
        if(botBehavior==null) return;
        FileOutputStream fos=null ;
        File file=new File("temp");
        try {
            fos=new FileOutputStream(file);
            fos.write(image);
            mainBot.sendFileToChat(botBehavior.getChatId(),file,caption);

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                assert fos != null;
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

package com.lokoproject.mailing.service;

import com.haulmont.cuba.security.entity.User;
import com.lokoproject.mailing.config.TelegramConfig;
import com.lokoproject.mailing.core.telegram.BotManager;
import com.lokoproject.mailing.core.telegram.MainBot;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

@Service(BotService.NAME)
public class BotServiceBean implements BotService {


    @Inject
    private TelegramConfig telegramConfig;

    @Inject
    private BotManager botManager;

    MainBot bot;

    @Override
    public void startBot(){
       if(bot!=null) return;
        try {
            ApiContextInitializer.init();

            // Create the TelegramBotsApi object to register your bots
            TelegramBotsApi botsApi = new TelegramBotsApi();

            DefaultBotOptions botOptions = ApiContext
                    .getInstance(DefaultBotOptions.class);
            botOptions.setProxyHost("127.0.0.1");
            botOptions.setProxyPort(9150);
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);



            // Register your newly created AbilityBot
            bot = new MainBot(telegramConfig.getNotificationBotToken(), telegramConfig.getNotificationBotName(), botOptions);
            bot.setBotManager(botManager);
            botManager.setMainBot(bot);

            botsApi.registerBot(bot);

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessageToUser(User user, String message){
        botManager.sendMessage(user,message);
    }

    @Override
    public void sendImageToUser(User user, byte[] image, String caption) {
        botManager.sendImage(user,image,caption);
    }

}
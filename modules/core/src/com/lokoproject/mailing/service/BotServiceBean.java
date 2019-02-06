package com.lokoproject.mailing.service;

import com.lokoproject.mailing.core.telegram.MainBot;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;

@Service(BotService.NAME)
public class BotServiceBean implements BotService {

    private static String PROXY_HOST = "iibcc.teletype.live" /* proxy host */;
    private static Integer PROXY_PORT = 1080 /* proxy port */;
    private static String PROXY_USER = "telegram" /* proxy user */;
    private static String PROXY_PASSWORD = "telegram" /* proxy password */;

    private static String BOT_NAME = "My test bot";
    private static String BOT_TOKEN = "726369805:AAHLmZ5em_SFlRebYOxgMm8oDzPzUdIqh20" /* your bot's token here */;

    @Override
    public void startBot(){
        try {
            ApiContextInitializer.init();

            // Create the TelegramBotsApi object to register your bots
            TelegramBotsApi botsApi = new TelegramBotsApi();

            // Set up Http proxy
            DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

            botOptions.setProxyHost(PROXY_HOST);
            botOptions.setProxyPort(PROXY_PORT);
            // Select proxy type: [HTTP|SOCKS4|SOCKS5] (default: NO_PROXY)
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);

            // Register your newly created AbilityBot
            MainBot bot = new MainBot(BOT_TOKEN, BOT_NAME, botOptions);

            botsApi.registerBot(bot);

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

}
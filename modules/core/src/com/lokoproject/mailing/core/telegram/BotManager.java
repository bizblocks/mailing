package com.lokoproject.mailing.core.telegram;

import com.haulmont.cuba.security.entity.User;
import com.lokoproject.mailing.core.telegram.bot.BotBehavior;
import com.lokoproject.mailing.core.telegram.bot.CheckInBot;
import com.lokoproject.mailing.core.telegram.bot.TalkingToUserBot;
import com.lokoproject.mailing.service.IdentifierService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Antonlomako. created on 06.02.2019.
 */
@Component
public class BotManager {

    @Inject
    private IdentifierService identifierService;

    private Map<Long,User> userChatIdMap=new HashMap<>();

    private MainBot mainBot;

    public void sendMessageToUser(User user, String message){

    }

    public BotBehavior getBotByChatId(long chatId){
        if(userChatIdMap.get(chatId)==null){
            return new CheckInBot();
        }
        else return new TalkingToUserBot();
    }
}

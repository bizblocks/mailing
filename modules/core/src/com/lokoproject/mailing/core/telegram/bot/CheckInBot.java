package com.lokoproject.mailing.core.telegram.bot;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.MessageTools;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Metadata;
import com.lokoproject.mailing.config.TelegramConfig;
import com.lokoproject.mailing.core.telegram.BotManager;
import com.lokoproject.mailing.service.IdentifierService;
import com.lokoproject.mailing.service.NotificationService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.*;

/**
 * @author Antonlomako. created on 06.02.2019.
 */

public class CheckInBot implements BotBehavior {

    private Messages messages=AppBeans.get(Messages.class);
    private Metadata metadata=AppBeans.get(Metadata.class);
    private MessageTools messageTools=AppBeans.get(MessageTools.class);
    private IdentifierService identifierService=AppBeans.get(IdentifierService.class);
    private BotManager botManager=AppBeans.get(BotManager.class);
    private NotificationService notificationService=AppBeans.get(NotificationService.class);

    private TelegramConfig telegramConfig;

    private Set<String> availableEntityTypes;
    private CheckInStage stage;
    private String entityType;
    private StandardEntity entity;
    private long chatId;
    private String password;

    private Map<String,String> typeMap=new HashMap<>();

    public CheckInBot(long chatId, TelegramConfig telegramConfig) {
        this.setChatId(chatId);
        this.telegramConfig=telegramConfig;
    }

    public void onUpdateReceived(SendMessage message,Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {

            String message_text = update.getMessage().getText();

            if(stage==null){
                stage=CheckInStage.NEW;
            }

            switch (stage){
                case NEW: {
                    message.setText(messages.getMainMessage("telegram_greeting_message"));

                    makeKeyboard(message,getAvailableEntityTypeNames(),true);

                    stage=CheckInStage.WAITING_TYPE;
                }
                break;

                case WAITING_TYPE:{
                    if(checkType(message_text)){
                        entityType=message_text;
                        message.setText(messages.getMainMessage("telegram_ask_for_identifier_message"));
                        stage=CheckInStage.WAITING_IDENTIFIER;
                    }
                    else{
                        message.setText(messages.getMainMessage("telegram_type_error_message"));
                        makeKeyboard(message,getAvailableEntityTypeNames(),true);
                    }
                }
                break;

                case WAITING_IDENTIFIER:{
                    if(checkIdentifier(message_text)){
                        if((telegramConfig.getEntityTypesWithPasswordAuthorization()!=null)&&(
                                telegramConfig.getEntityTypesWithPasswordAuthorization().contains(entityType))
                                &&(telegramConfig.getMailingToRequestPassword()!=null)){
                            message.setText(messages.getMainMessage("telegram_password_send_message"));
                            stage=CheckInStage.WAITING_PASSWORD;
                            sendPassword();
                        }
                        else{
                            message.setText(messages.getMainMessage("telegram_check_in_complete_message"));
                            stage=CheckInStage.ENTITY_IDENTIFIER_RECEIVED;
                            botManager.onCheckIn(entity, getChatId());
                        }
                    }
                    else{
                        message.setText(messages.getMainMessage("telegram_wrong_identifier_message"));
                    }
                }
                break;

                case WAITING_PASSWORD:{
                    if(message_text.equals(password)){
                        message.setText(messages.getMainMessage("telegram_check_in_complete_message"));
                        stage=CheckInStage.ENTITY_IDENTIFIER_RECEIVED;
                        botManager.onCheckIn(entity, getChatId());
                    }
                    else{
                        if(message_text.equals(messages.getMainMessage("telegram_send_again_message"))){
                            sendPassword();
                            message.setText(messages.getMainMessage("telegram_password_send_message"));
                        }
                        else{
                            message.setText(messages.getMainMessage("telegram_wrong_password_message"));
                            makeKeyboard(message,Arrays.asList(messages.getMainMessage("telegram_send_again_message")),true);
                        }
                    }
                }
                break;

            }

        }
    }

    private Collection<String> getAvailableEntityTypeNames() {
        List<String> result=new ArrayList<>();
        getAvailableEntityTypes().forEach(type->{
            result.add(typeMap.get(type));
        });
        return result;
    }

    private void sendPassword() {
        password=String.valueOf((int)Math.random()*1000);

        notificationService.addNotification(telegramConfig.getMailingToRequestPassword()
                , ParamsMap.of("entityType",entityType,
                                "entity",entity,
                                "password",password));
    }

    private void makeKeyboard(SendMessage message,Collection<String> options,boolean oneTime){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(oneTime);

        List<KeyboardRow> keyboard = new ArrayList<>();

        options.forEach(option->{
            KeyboardRow keyboardFirstRow = new KeyboardRow();
            keyboardFirstRow.add(option);
            keyboard.add(keyboardFirstRow);
        });
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    private void makeInlineKeyboard(SendMessage message,Collection<String> options){
        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();
        message.setReplyMarkup(replyKeyboardMarkup);

        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        options.forEach(option->{
            InlineKeyboardButton inlineKeyboardButton=new InlineKeyboardButton();
            inlineKeyboardButton.setText(option);
            inlineKeyboardButton.setCallbackData(option);
            keyboard.add(Arrays.asList(inlineKeyboardButton));
        });
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    private boolean checkType(String message_text) {
        return getAvailableEntityTypes().contains(typeMap.get(message_text));
    }

    private boolean checkIdentifier(String extIdentifierValue) {
        entity=identifierService.getEntityByFieldMarkedAsExternalIdentifier(typeMap.get(entityType),extIdentifierValue,"Telegram");
        return entity!=null;
    }

    private Set<String> getAvailableEntityTypes() {
        if(availableEntityTypes ==null) {
            availableEntityTypes =identifierService.getAvailableEntityTypesForChanel("Telegram");
            if(availableEntityTypes==null) availableEntityTypes=new HashSet<>();
            availableEntityTypes.add("sec$User");

            availableEntityTypes.forEach(type->{
                String caption=typeMap.get(type);
                if(caption==null){
                    caption=messageTools.getEntityCaption(metadata.getClass(type));
                    typeMap.put(caption,type);
                    typeMap.put(type,caption);
                }
            });
        }
        return availableEntityTypes;
    }

    @Override
    public long getChatId() {
        return chatId;
    }

    public void setChatId(long chatId) {
        this.chatId = chatId;
    }

    enum CheckInStage{
        NEW,
        WAITING_TYPE,
        WAITING_IDENTIFIER,
        WAITING_PASSWORD,

        ENTITY_IDENTIFIER_RECEIVED
    }
}

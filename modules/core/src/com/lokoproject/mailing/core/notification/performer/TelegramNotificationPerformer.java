package com.lokoproject.mailing.core.notification.performer;

import com.haulmont.cuba.core.entity.StandardEntity;
import com.lokoproject.mailing.config.TelegramConfig;
import com.lokoproject.mailing.core.telegram.BotManager;
import com.lokoproject.mailing.notification.event.CubaEmailNotificationEvent;
import com.lokoproject.mailing.notification.event.TelegramNotificationEvent;
import com.lokoproject.mailing.notification.template.element.*;
import com.lokoproject.mailing.service.DaoService;
import com.lokoproject.mailing.service.IdentifierService;
import com.lokoproject.mailing.utils.HtmlTemplateHelper;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Antonlomako. created on 15.12.2018.
 */
@Component
public class TelegramNotificationPerformer implements ApplicationListener<TelegramNotificationEvent> {

    @Inject
    private IdentifierService identifierService;

    @Inject
    private TelegramConfig telegramConfig;

    @Inject
    private BotManager botManager;

    @Inject
    private DaoService daoService;

    @Override
    public void onApplicationEvent(TelegramNotificationEvent event) {
        String telegramId=identifierService.getIdentifier(event.getNotification().getTargetEntityType(),
                event.getNotification().getTargetEntityUuid().toString(),
                "Telegram");

        StandardEntity target=daoService.getEntity(event.getNotification().getTargetEntityType(),event.getNotification().getTargetEntityUuid().toString());
        if(telegramId==null){
            sendCheckInRequest(event.getNotification().getTargetEntityType(),event.getNotification().getTargetEntityUuid());
            return;
        }

        List<SendItem> sendItemList=new ArrayList<>();
        event.getNotification().getTemplate().getChildren().forEach(child->{
            processTemplateRecur(child,sendItemList);
        });
        sendItemList.forEach(item->{
            item.send(botManager,target);
        });
    }

    private void sendCheckInRequest(String targetEntityType, UUID targetEntityUuid) {

    }
    
    private void processTemplateRecur(TemplateElement templateElement, List<SendItem> result){
        if(templateElement instanceof TemplateContainerElement){
            TemplateContainerElement templateContainerElement= (TemplateContainerElement) templateElement;
            templateContainerElement.getChildren().forEach(child->{
                processTemplateRecur(templateElement,result);
            });
        }
        else{
            if(templateElement instanceof Table){
                processTable((Table) templateElement,result);
            }
            else if(templateElement instanceof com.lokoproject.mailing.notification.template.element.List){
                processList((com.lokoproject.mailing.notification.template.element.List) templateElement,result);
            }
            else if(templateElement instanceof Header){
                processHeader((Header) templateElement,result);
            }
            else if(templateElement instanceof Text){
                processText((Text) templateElement,result);
            }
        }
    }

    private void processTable(Table table, List<SendItem> result){
        ImageSendItem sendItem=new ImageSendItem();
        sendItem.caption="table";
        sendItem.setContent(HtmlTemplateHelper.createImageByHtml(HtmlTemplateHelper.buildTable(null,table).toString(),1024,768));
        result.add(sendItem);
    }
    private void processList(com.lokoproject.mailing.notification.template.element.List list, List<SendItem> result){
        TextSendItem textSendItem=getTextSendItem(result);

        if(list.getElements().size()>0){
            textSendItem.addEmptyRow();
        }
        for(String element:list.getElements()){
            textSendItem.addText(element);
        }

    }
    private void processHeader(Header header, List<SendItem> result){
        TextSendItem textSendItem=getTextSendItem(result);
        textSendItem.addText("<b>"+header.getContent()+"</b>");
    }
    private void processText(Text text, List<SendItem> result){
        TextSendItem textSendItem=getTextSendItem(result);
        textSendItem.addText(text.getContent());
    }

    private TextSendItem getTextSendItem(List<SendItem> result){
        TextSendItem textSendItem=null;
        if(result.size()>0){
            SendItem sendItem=result.get(result.size()-1);
            if(sendItem instanceof TextSendItem){
                textSendItem= (TextSendItem) sendItem;
            }
        }
        if (textSendItem == null) {
            textSendItem=new TextSendItem();
            result.add(textSendItem);
        }
        return textSendItem;
    }

    abstract class SendItem{
        abstract void send(BotManager botManager, StandardEntity target);
        abstract void setContent(Object content);
    }

    class TextSendItem extends SendItem{

        StringBuilder contentBuilder=new StringBuilder();

        public void addText(String text){
            if(contentBuilder.length()>0){
                contentBuilder.append("\n");
            }
            contentBuilder.append(text);
        }
        public void addEmptyRow(){
            contentBuilder.append("\n\n");
        }

        @Override
        void send(BotManager botManager, StandardEntity target) {
            botManager.sendMessage(target,contentBuilder.toString());
        }

        @Override
        void setContent(Object content) {
            contentBuilder.append((String)content);
        }
    }
    class ImageSendItem extends FileSendItem{

    }
    class FileSendItem extends SendItem{
        byte[] content;
        String caption;

        @Override
        void send(BotManager botManager, StandardEntity target) {
            botManager.sendImage(target,content,caption);
        }

        @Override
        void setContent(Object content) {
            this.content= (byte[]) content;
        }
    }
}

package com.lokoproject.mailing.web.screens;

import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.security.global.UserSession;
import com.lokoproject.mailing.conditions.Condition;
import com.lokoproject.mailing.conditions.ConditionException;
import com.lokoproject.mailing.conditions.OrCondition;
import com.lokoproject.mailing.conditions.schedule.DayOfWeek;
import com.lokoproject.mailing.conditions.schedule.HourSchedule;
import com.lokoproject.mailing.notification.template.TemplateBuilder;
import com.lokoproject.mailing.service.BotService;
import com.lokoproject.mailing.service.NotificationService;
import com.lokoproject.mailing.web.beens.notification.CubaWebClientNotificationPerformer;

import javax.inject.Inject;
import java.util.*;

public class Screen extends AbstractWindow {

    @Inject
    private TextField textField1;

    @Inject
    private TextField textField2;

    @Inject
    private TextField textField3;

    @Inject
    private NotificationService notificationService;

    @Inject
    private CubaWebClientNotificationPerformer notificationProcessor;

    @Inject
    private BotService botService;

    @Inject
    private UserSession userSession;

    private Condition condition;

    public void onStrClick() {

        if(condition==null){
            OrCondition orCondition=new OrCondition();
            HourSchedule hourSchedule=new HourSchedule();
            DayOfWeek dayOfWeek=new DayOfWeek();

            orCondition.addChild(hourSchedule);
            orCondition.addChild(dayOfWeek);

            condition=orCondition;
        }


        Consolidation consolidation= (Consolidation) openWindow("consolidation", WindowManager.OpenType.DIALOG,ParamsMap.of("condition",condition));
        consolidation.addCloseListener(event->{
            setCondition(consolidation.getCondition());
            Calendar calendar=Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR,-1);
            try {
                showNotification(String.valueOf(condition.check(ParamsMap.of("now",new Date(),"lastSendDate",calendar.getTime(),"objectsValue",3))));
            } catch (ConditionException e) {
                e.printStackTrace();
            }
        });
    }

    public void onIntClick() {
//        notificationService.addNotification(Integer.valueOf(textField1.getValue()));
        List<Map<String,String>> data=new ArrayList<>();

        for(int i=0;i<5;i++){
            Map<String,String> row=new HashMap<>();
            for(int j=0;j<3;j++){
                row.put("col"+String.valueOf(j),String.valueOf(j+i));
            }
            data.add(row);
        }
        TemplateBuilder.MainTemplateBuilder builder=TemplateBuilder.createBuilder("тема","описание","smile");

        builder.withChild(builder.createTableBuilder(data)
                        .withColumns(Arrays.asList("col0","col1","col2"))
                        .build())
                .withChild(builder.createList(Arrays.asList("col0","col1","col2")))
                .withChild(builder.createText("some text"));

        openWindow("notificationTemplateProcessor", WindowManager.OpenType.DIALOG, ParamsMap.of("notificationTemplate",builder.build()));
    }

    public void onSClick() {
        notificationService.addNotification(Arrays.asList(textField1.getValue(), textField2.getRawValue(), textField3.getRawValue()));
    }

    public void onIClick() {
        notificationService.addNotification(Integer.valueOf(textField1.getValue()));
    }

    public void onBotClick() {
        botService.startBot();
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }
}
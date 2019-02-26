package com.lokoproject.mailing.web.beens;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Events;
import com.lokoproject.mailing.notification.event.WebEvent;
import com.lokoproject.mailing.utils.Serializer;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/receiver")
public class EventReceiver {

    private Events events;

    @PostMapping("/event")
    public void greeting(@RequestParam(value="data", defaultValue="null") String data) {

        if(!("null".equals(data))) {
            try {
                WebEvent event= (WebEvent) Serializer.fromString(data);
                publishEvent((ApplicationEvent) event);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void publishEvent(ApplicationEvent event){
        if(events==null) events=AppBeans.get(Events.class);
        events.publish( event);
    }
}

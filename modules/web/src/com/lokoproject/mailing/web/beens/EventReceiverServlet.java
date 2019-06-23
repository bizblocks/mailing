package com.lokoproject.mailing.web.beens;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Events;
import com.lokoproject.mailing.notification.event.WebEvent;
import com.lokoproject.mailing.utils.Serializer;
import org.springframework.context.ApplicationEvent;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Anton on 23.06.2019.
 */
public class EventReceiverServlet extends HttpServlet {

    private Events events;

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response){
        String data=request.getParameter("data");
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
        if(events==null) events= AppBeans.get(Events.class);
        events.publish( event);
    }
}

package com.lokoproject.mailing.web.beens;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Events;
import com.haulmont.cuba.web.sys.CubaHttpFilter;
import com.lokoproject.mailing.notification.event.WebEvent;
import com.lokoproject.mailing.service.NotificationService;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;


/**
 * @author Antonlomako. created on 16.12.2018.
 */
@Component
public class CustomFilter extends CubaHttpFilter implements Filter {

    private Events events;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {


        if(events==null) events=AppBeans.get(Events.class);

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if(events!=null){
            if(request.getRequestURI().contains("event")) {

                String serializedEvent=request.getParameter("event");
                try {
                    WebEvent event= (WebEvent) fromString(serializedEvent);
                    publishEvent((ApplicationEvent) event);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        super.doFilter(request, response, chain);
    }


    private void publishEvent(ApplicationEvent event){
        events.publish( event);
    }

    private static Object fromString( String s ) throws IOException ,
            ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode( s );
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
    }
}

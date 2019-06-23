package com.lokoproject.mailing.web.beens;

import com.haulmont.cuba.core.sys.servlet.ServletRegistrationManager;
import com.haulmont.cuba.core.sys.servlet.events.ServletContextInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.Servlet;

/**
 * Created by Anton on 23.06.2019.
 */
@Component
public class WebInitializer {

    @Inject
    private ServletRegistrationManager servletRegistrationManager;

    @EventListener
    public void initializeHttpServlet(ServletContextInitializedEvent e) {
        Servlet eventReceiver = servletRegistrationManager.createServlet(e.getApplicationContext(), "com.lokoproject.mailing.web.beens.EventReceiverServlet");

        e.getSource().addServlet("eventReceiver", eventReceiver)
                .addMapping("/rest/receiver/event");
    }
}

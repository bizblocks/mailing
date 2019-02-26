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


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {




        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;


        super.doFilter(request, response, chain);
    }

}

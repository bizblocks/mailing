package com.lokoproject.mailing.core;

import com.lokoproject.mailing.notification.event.AbstractNotificationEvent;
import com.lokoproject.mailing.notification.event.InstantWebEvent;
import com.lokoproject.mailing.notification.event.WebEvent;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Antonlomako. created on 05.02.2019.
 */
@Component
@SuppressWarnings("unchecked")
public class EventTransmitter implements ApplicationListener<AbstractNotificationEvent> {
    private String url ;

    @Override
    public void onApplicationEvent(AbstractNotificationEvent event) {
        if(event instanceof WebEvent){

            eventList.add((WebEvent) event);
            if(event instanceof InstantWebEvent){
                try {
                    sendEvent((WebEvent) event);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Collection<WebEvent> getNewEvents(){
        List<WebEvent> result= new ArrayList<>(eventList);
        eventList.clear();
        return result;
    }

    private List<WebEvent> eventList=new CopyOnWriteArrayList<>();

    private void sendEvent(WebEvent event) throws Exception {

        if(url==null) return;

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(getUrl());
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("data", serializeObject(event)));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity respEntity = response.getEntity();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static String serializeObject( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        if(this.url==null) {
            this.url = "http://"+url + "/rest/receiver/event";
        }
    }
}

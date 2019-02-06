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
import org.apache.http.util.EntityUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
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
    @Override
    public void onApplicationEvent(AbstractNotificationEvent event) {
        if(event instanceof WebEvent){

            eventList.add((WebEvent) event);
            if(event instanceof InstantWebEvent){
                try {
                    sendEvent((WebEvent) event);
                } catch (Exception ignored) { }
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

        String url = "http://192.168.0.2:8080/app/"+"event";

//        URL obj = new URL(url);
//        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//        con.setRequestMethod("GET");
//
//        BufferedReader in = new BufferedReader(
//                new InputStreamReader(con.getInputStream()));
//        String inputLine;
//        StringBuffer response = new StringBuffer();
//
//        while ((inputLine = in.readLine()) != null) {
//            response.append(inputLine);
//        }
//        in.close();



        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("event", serializeObject(event)));
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
}

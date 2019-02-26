package com.lokoproject.mailing.service;

import com.lokoproject.mailing.core.EventTransmitter;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service(EventTransmitterService.NAME)
public class EventTransmitterServiceBean implements EventTransmitterService {

    @Inject
    private EventTransmitter eventTransmitter;

    @Override
    public void setUrlOfWebModule(String url){
        eventTransmitter.setUrl(url);
    }

}
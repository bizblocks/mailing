package com.lokoproject.mailing.service;


import com.lokoproject.mailing.dto.ChanelInfo;

import java.util.Collection;

public interface ChannelStateService {
    String NAME = "mailing_ChannelStateService";

    void setChanelState(String channelName, ChannelState channelState, String stateDescription);

    Collection<String> getAvailableChannelNames();

    ChanelInfo getChannelInfo(String channelName);


    enum ChannelState {
        READY,UNKNOWN,ERROR
    }

}
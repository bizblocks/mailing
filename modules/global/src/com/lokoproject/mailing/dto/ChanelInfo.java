package com.lokoproject.mailing.dto;

import com.lokoproject.mailing.service.ChannelStateService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anton on 12.05.2019.
 */
public class ChanelInfo{
    private ChannelStateService.ChannelState channelState;
    private String errorDescription;

    public ChannelStateService.ChannelState getChannelState() {
        return channelState;
    }

    public void setChannelState(ChannelStateService.ChannelState channelState) {
        this.channelState = channelState;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public ChanelInfo() {
    }

    public ChanelInfo(ChannelStateService.ChannelState channelState, String errorDescription) {

        this.channelState = channelState;
        this.errorDescription = errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

}

package com.lokoproject.mailing.service;

import com.lokoproject.mailing.dto.ChanelInfo;
import com.lokoproject.mailing.utils.ReflectionHelper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service(ChannelStateService.NAME)
public class ChannelStateServiceBean implements ChannelStateService {

    private Map<String,ChanelInfo> channelInfoMap =new HashMap<>();

    @Override
    public void setChanelState(String channelName, ChannelState channelState, String stateDescription){
        channelInfoMap.put(channelName,new ChanelInfo(channelState,stateDescription));
    }

    @Override
    public Collection<String> getAvailableChannelNames(){
        List<String> result=new ArrayList<>();
        ReflectionHelper.getAllAvailableNotificationEvents().forEach(classItem->{
            result.add(classItem.getName().substring(0,classItem.getName().indexOf("NotificationEvent")));
        });
        return result;
    }

    @Override
    public ChanelInfo getChannelInfo(String chanelName){
        return channelInfoMap.get(chanelName);

    }



}
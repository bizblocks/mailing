package com.lokoproject.mailing.conditions.schedule;

import com.lokoproject.mailing.conditions.Condition;
import com.lokoproject.mailing.conditions.ConditionException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * @author Antonlomako. created on 15.02.2019.
 */
public class HourDuration extends AbstractScheduleCondition {
    @Condition.FieldDescription(type = FieldDescription.ParameterType.TIME)
    Date begin;

    @Condition.FieldDescription(type = FieldDescription.ParameterType.TIME)
    Date end;


    @Override
    public boolean check(Map<String,Object> params) throws ConditionException {

        fillFieldsFromParamMap(params);

        Calendar calendar=Calendar.getInstance();

        int nowMinuteValue,beginMinuteValue,endMinuteValue;

        calendar.setTime(getNow());
        nowMinuteValue=calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE);

        calendar.setTime(begin);
        beginMinuteValue=calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE);

        calendar.setTime(end);
        endMinuteValue=calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE);

        return (nowMinuteValue >= beginMinuteValue) && (nowMinuteValue <= endMinuteValue);
    }

    @Override
    public String makeStateDescription() {
        if((begin==null)||(end==null))return "";
        String pattern = "hh:mm";
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(begin)+" - "+df.format(end);
    }
}

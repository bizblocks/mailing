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
public class HourSchedule extends AbstractScheduleCondition {

    @Condition.FieldDescription(type = FieldDescription.ParameterType.TIME)
    Date time;

    @Condition.FieldDescription(type = FieldDescription.ParameterType.INTEGER,name = "allowable error, min")
    int allowableErrorMin=1;          //погрешность на перидичность выполнения проверки

    @Override
    public boolean check(Map<String, Object> params) throws ConditionException {
        if(time==null) return false;

        fillFieldsFromParamMap(params);

        Calendar calendar=Calendar.getInstance();

        int nowMinuteValue,targetMinutesValue;

        calendar.setTime(getNow());
        nowMinuteValue=calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE);

        calendar.setTime(time);
        targetMinutesValue=calendar.get(Calendar.HOUR_OF_DAY)*60+calendar.get(Calendar.MINUTE);

        return (((nowMinuteValue-allowableErrorMin)<targetMinutesValue)&&((nowMinuteValue+allowableErrorMin)>targetMinutesValue));

    }

    @Override
    public String makeStateDescription() {
        if(time==null) return null;
        String pattern = "hh:mm";
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(time);
    }
}

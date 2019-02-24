package com.lokoproject.mailing.conditions.schedule;

import com.lokoproject.mailing.conditions.Condition;
import com.lokoproject.mailing.conditions.ConditionException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author Antonlomako. created on 15.02.2019.
 */
public class DateDuration extends AbstractScheduleCondition {

    @Condition.FieldDescription(type = Condition.FieldDescription.ParameterType.DATE)
    Date begin;

    @Condition.FieldDescription(type = Condition.FieldDescription.ParameterType.DATE)
    Date end;


    @Override
    public boolean check(Map<String,Object> params) throws ConditionException {

        fillFieldsFromParamMap(params);

        if((getNow().getTime()>=begin.getTime())&&(getNow().getTime()<end.getTime())){
            return true;
        }

        return false;
    }

    @Override
    public String makeStateDescription() {
        if((begin==null)||(end==null))return "";
        String pattern = "dd.MM.yyyy";
        DateFormat df = new SimpleDateFormat(pattern);
        return df.format(begin)+" - "+df.format(end);
    }

}

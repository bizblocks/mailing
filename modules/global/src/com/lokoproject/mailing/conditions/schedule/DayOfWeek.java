package com.lokoproject.mailing.conditions.schedule;

import com.lokoproject.mailing.conditions.ConditionException;

import java.util.*;

/**
 * @author Antonlomako. created on 15.02.2019.
 */
public class DayOfWeek extends AbstractScheduleCondition {

    @FieldDescription(type = FieldDescription.ParameterType.SELECT_MANY_STRINGS,options = {
            "Mon","Tue","Wen","Thu","Fri","Sat","Sun"

    })
    List<String> selectedDays;

    @Override
    public boolean check(Map<String, Object> params) throws ConditionException {
        if((selectedDays==null)||(selectedDays.size()==0)) return false;

        fillFieldsFromParamMap(params);

        Calendar currentDate=new GregorianCalendar();
        currentDate.setTime(getNow());
        String dayOfWeek = currentDate.getDisplayName( Calendar.DAY_OF_WEEK , Calendar.SHORT, Locale.US);

        return selectedDays.contains(dayOfWeek);

    }

    @Override
    public String makeStateDescription() {
        if((selectedDays==null)||(selectedDays.size()==0)) return null;

        StringBuilder sb=new StringBuilder();
        selectedDays.forEach(day->{
            sb.append(day);
            if(selectedDays.indexOf(day)<selectedDays.size()-1){
                sb.append(", ");
            }
        });
        return sb.toString();
    }
}

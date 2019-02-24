package com.lokoproject.mailing.conditions.schedule;

import com.lokoproject.mailing.conditions.ConditionException;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Antonlomako. created on 15.02.2019.
 */
public class DayOfMonth extends AbstractScheduleCondition {

    @FieldDescription(type = FieldDescription.ParameterType.SELECT_MANY_STRINGS,options = {
            "1","2","3","4","5","6","7","8","9",
            "10","11","12","13","14","15","16","17","18","19",
            "20","21","22","23","24","25","26","27","28","29",
            "30","31"

    })
    List<String> selectedDays;

    @Override
    public boolean check(Map<String, Object> params) throws ConditionException {

        fillFieldsFromParamMap(params);
        Calendar calendar=Calendar.getInstance();
        calendar.setTime(getNow());
        return selectedDays.contains(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));

    }

    @Override
    public String makeStateDescription() {
        if(selectedDays==null) return null;

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

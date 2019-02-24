package com.lokoproject.mailing.conditions.schedule;

import com.lokoproject.mailing.conditions.Condition;
import com.lokoproject.mailing.conditions.ConditionException;

import java.util.*;

/**
 * @author Antonlomako. created on 15.02.2019.
 */
public class Month extends AbstractScheduleCondition{

    @Condition.FieldDescription(type = Condition.FieldDescription.ParameterType.SELECT_MANY_STRINGS,options = {
            "January","February","March","April","May","June","July","August","September","October","November","December"
    })
    List<String> selectedMonths;


    @Override
    public boolean check(Map<String, Object> params) throws ConditionException {
        if((selectedMonths==null)||(selectedMonths.size()==0)) return false;

        fillFieldsFromParamMap(params);

        Calendar currentDate=new GregorianCalendar();
        currentDate.setTime(getNow());
        String currentMonth = currentDate.getDisplayName( Calendar.MONTH , Calendar.LONG, Locale.US);

        return selectedMonths.contains(currentMonth);
    }

    @Override
    public String makeStateDescription() {
        StringBuilder sb = new StringBuilder();
        if(selectedMonths==null) return null;
        for (String month:selectedMonths)
        {
            sb.append(month);
            if(selectedMonths.indexOf(month)!=selectedMonths.size()-1){
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}

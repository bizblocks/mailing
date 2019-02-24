package com.lokoproject.mailing.conditions.schedule;

import com.lokoproject.mailing.conditions.Condition;
import com.lokoproject.mailing.conditions.ConditionException;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

/**
 * @author Antonlomako. created on 15.02.2019.
 */
public class WeekDayOfMonth extends AbstractScheduleCondition{

    @Condition.FieldDescription(type = Condition.FieldDescription.ParameterType.SELECT_ONE_STRING,options = {
            "First","Second","Third","Last"
    })
    String selectedOrder;

    @Condition.FieldDescription(type = Condition.FieldDescription.ParameterType.SELECT_ONE_STRING,options = {
            "Mon","Tue","Wen","Thu","Fri","Sat","Sun"
    })
    String selectedDay;


    @Override
    public boolean check(Map<String, Object> params) throws ConditionException {

        if((selectedDay==null)||(selectedOrder==null))return false;
        fillFieldsFromParamMap(params);

        Calendar currentDate=new GregorianCalendar();
        currentDate.setTime(getNow());
        String dayOfWeek = currentDate.getDisplayName( Calendar.DAY_OF_WEEK , Calendar.SHORT, Locale.US);

        if(dayOfWeek.equalsIgnoreCase(selectedDay)){
            int dayOfMonth=currentDate.get(Calendar.DAY_OF_MONTH);
            int totalDaysOfMonth=currentDate.getActualMaximum(Calendar.DAY_OF_MONTH);

            switch (selectedOrder){
                case "First":
                    return (dayOfMonth<=7);
                case "Second":
                    return (dayOfMonth>7)&&(dayOfMonth<=14);
                case "Third":
                    return (dayOfMonth>14)&&(dayOfMonth<=21);
                case "Last":
                    return (totalDaysOfMonth-dayOfMonth<7);
            }
        }
        return false;
    }

    @Override
    public String makeStateDescription() {
        if((selectedOrder==null)||(selectedDay==null)) return null;
        return String.format("%s %s",selectedOrder,selectedDay);
    }
}

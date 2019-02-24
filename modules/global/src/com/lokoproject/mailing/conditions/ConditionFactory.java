package com.lokoproject.mailing.conditions;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Scripting;
import com.lokoproject.mailing.conditions.consolidation.HourPeriod;
import com.lokoproject.mailing.conditions.consolidation.ObjectValueLimit;
import com.lokoproject.mailing.conditions.schedule.*;
import com.lokoproject.mailing.utils.ReflectionHelper;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Antonlomako. created on 17.02.2019.
 */
public class ConditionFactory {

    private List<Class<?>> conditionClasses;
    private Map<String,Class<?>> classNameMap=new HashMap<>();

    public Collection<String> getAllExecutionCheckItemsNames(){
        List<String> result=new ArrayList<>();
        getConditionClasses().forEach(item->{
            result.add(item.getName());
        });
        return result;
    }

    public Map<String,List<String>> getAllExecutionCheckItemsNamesGroupedByType(){
        Map<String,List<String>> result=new HashMap<>();

        getConditionClasses().forEach(item->{
            result.putIfAbsent(item.getPackage().getName(),new ArrayList<>());
            result.get(item.getPackage().getName()).add(item.getSimpleName());
        });

        return result;
    }

    public Condition createCondition(String name) throws ConditionException {
        getConditionClasses();
        Class itemClass=classNameMap.get(name);
        if(itemClass==null) throw new ConditionException("no class with name "+name);

        Condition result;
        try {
            result= (Condition) itemClass.newInstance();
        } catch (Exception e) {
            throw new ConditionException(e);
        }
        return result;
    }

    public Condition createOrCondition(){
        return new OrCondition();
    }

    public Condition createAndCondition(){
        return new AndCondition();
    }



    private List<Class<?>> getConditionClasses() {

        if(conditionClasses ==null) {
            conditionClasses=new ArrayList<>();
            conditionClasses.add(HourPeriod.class);
            conditionClasses.add(ObjectValueLimit.class);
            conditionClasses.add(DateDuration.class);
            conditionClasses.add(DayOfMonth.class);
            conditionClasses.add(DayOfWeek.class);
            conditionClasses.add(HourSchedule.class);
            conditionClasses.add(HourDuration.class);
            conditionClasses.add(Month.class);
            conditionClasses.add(WeekDayOfMonth.class);

            conditionClasses.forEach(classItem->{
                classNameMap.put(classItem.getSimpleName(),classItem);
            });
        }
        return conditionClasses;
    }


}

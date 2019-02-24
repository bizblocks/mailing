package com.lokoproject.mailing.conditions;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.lang.reflect.Field;

/**
 * @author Antonlomako. created on 19.02.2019.
 */
public abstract class AbstractCondition implements Condition {

    @Override
    public boolean validateFields(){
        for(Field field:this.getClass().getDeclaredFields()){
            field.setAccessible(true);
            try {
                if(field.get(this)==null){
                    return false;
                }
            } catch (IllegalAccessException e) {
                return false;
            }
        }

        return true;
    }
}

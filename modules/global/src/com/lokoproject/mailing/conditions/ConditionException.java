package com.lokoproject.mailing.conditions;

/**
 * @author Antonlomako. created on 18.02.2019.
 */
public class ConditionException extends Exception {
    public ConditionException(String description){
        super(description);
    }

    public ConditionException(Exception e){
        super(e);
    }
}

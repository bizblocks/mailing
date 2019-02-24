package com.lokoproject.mailing.conditions;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * @author Antonlomako. created on 18.02.2019.
 */
public interface Condition extends Serializable{
    boolean check(Map<String,Object> params) throws ConditionException;
    String makeStateDescription();
    boolean validateFields();

//    Condition getParent();
//    void setParent(Condition Condition);

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface FieldDescription {

        enum ParameterType {
            TIME,
            DATE,
            DATE_TIME,
            INTEGER,
            SELECT_ONE_STRING,
            SELECT_MANY_STRINGS
        }

        ParameterType type();
        String name() default "default";
        String[] options() default {};
    }
}

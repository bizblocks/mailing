package com.lokoproject.mailing.utils;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.cuba.core.entity.BaseEntityInternalAccess;
import com.haulmont.cuba.core.entity.BaseGenericIdEntity;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;

import javax.persistence.Transient;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Antonlomako. created on 11.12.2018.
 */
public class EntityUtil {

    static Metadata metadata;

    public static Map<UUID,Entity> buildEntityIdMap(Collection<Entity<UUID>> source){
        Map<UUID,Entity> result=new HashMap<>();
        source.forEach(item->{
            result.put(item.getId(),item);
        });
        return result;
    }

    public static StandardEntity createEntityCopy(StandardEntity source){
        return createEntityCopy(source,false, Collections.emptyList());
    }

    public static Collection<String> getTransientFields(MetaClass metaClass){
        List<String> result=new ArrayList<>();

        Collection<MetaProperty> properties = metaClass.getProperties();

        for (MetaProperty property : properties) {
            AnnotatedElement a=property.getAnnotatedElement();
            if(a.isAnnotationPresent(Transient.class)) result.add(property.getName());
        }
        return result;
    }

    public static StandardEntity createEntityCopy(StandardEntity source,boolean copyState,Collection<String> propertiesToIgnore){
        StandardEntity target = getMetadata().create(source.getClass());
        return createEntityCopy(source,target,copyState,propertiesToIgnore);
    }

    public static StandardEntity createEntityCopy(StandardEntity source,StandardEntity target,boolean copyState,Collection<String> propertiesToIgnore){
        Collection<MetaProperty> properties = source.getMetaClass().getProperties();

        List<Field> fields=getFieldsUpTo(source.getClass(),BaseGenericIdEntity.class);
        Map<String,Field> fieldNameMap=new HashMap<>();
        fields.forEach(item->{
            fieldNameMap.put(item.getName(),item);
        });

        for (MetaProperty property : properties) {

            if(propertiesToIgnore.contains(property.getName())) continue;

            String name = property.getName();
            try {
                Field f=fieldNameMap.get(name);
                f.setAccessible(true);
                Object value = f.get(source);
                f.set(target,value);
            }
            catch (Exception ed){
                ed.printStackTrace();
            }
        }

        if(copyState) {
            BaseEntityInternalAccess.setNew(target, BaseEntityInternalAccess.isNew(source));
            BaseEntityInternalAccess.setDetached(target, BaseEntityInternalAccess.isDetached(source));
            BaseEntityInternalAccess.setManaged(target, BaseEntityInternalAccess.isManaged(source));
            BaseEntityInternalAccess.setRemoved(target, BaseEntityInternalAccess.isRemoved(source));
        }

        return target;
    }

    public static List<Field> getFieldsUpTo(Class<?> startClass,
                                                 Class<?> exclusiveParent) {

        List<Field> currentClassFields = new ArrayList<>(Arrays.asList(startClass.getDeclaredFields()));
        Class<?> parentClass = startClass.getSuperclass();

        if (parentClass != null &&
                (exclusiveParent == null || !(parentClass.equals(exclusiveParent)))) {
            List<Field> parentClassFields =
                    (List<Field>) getFieldsUpTo(parentClass, exclusiveParent);
            currentClassFields.addAll(parentClassFields);
        }

        return currentClassFields;
    }

    private static Metadata getMetadata(){
        if (metadata==null){
            metadata= AppBeans.get(Metadata.class);
        }
        return metadata;
    }
}

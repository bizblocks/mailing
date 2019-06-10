package com.lokoproject.mailing.dto;

/**
 * Created by Anton on 11.05.2019.
 */
public class EntityDescription {
    private String type;
    private String id;
    public EntityDescription(String type,String id){
        this.id=id;
        this.type=type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

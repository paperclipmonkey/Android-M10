package com.android.demo.notepad3;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by michaelwaterworth on 21/03/15.
 */
public class Note implements Serializable{
    public static String NORMAL = "normal";
    public static String IMPORTANT = "important";
    public static String URGENT = "urgent";

    private int id;
    private String body;
    private String type;
    private Date alarm;

    Note(){
        type = "normal";
    }

    Note(int pId, String pBody, String pType, Date pAlarm){
        id = pId;
        body = pBody;
        type = pType;
        alarm = pAlarm;
    }

    //- - - - - - - - - Setters - - - - - - - - - - -

    public void setAlarm(Date alarm) {
        this.alarm = alarm;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setBody(String body) {
        this.body = body;
    }

    //- - - - - - - -Getters - - - - - - - -

    public String getBody() {
        return body;
    }

    public Date getAlarm() {
        return alarm;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}

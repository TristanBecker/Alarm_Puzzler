package com.alarmpuzzler;

import java.io.Serializable;

public class Alarm implements Serializable {
    private long time; // Alarm time in milliseconds
    private String message;
    private String alarmType;

    public Alarm(long time, String message, String alarmType){
        this.time = time;
        this.message = message;
        this.alarmType = alarmType;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }
}

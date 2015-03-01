package com.sicomeafit.campusmovil.models;

/**
 * Created by Alejandro on 24/01/2015.
 */
public class Note {

    private String title;
    private String content;
    private int hour;
    private int minute;
    private String days;

    public Note(String title, String content, int hour, int minute, String days){
        this.title = title;
        this.content = content;
        this.hour = hour;
        this.minute = minute;
        this.days = days;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public String getDays() {
        return days;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public void setDays(String days) {
        this.days = days;
    }

}

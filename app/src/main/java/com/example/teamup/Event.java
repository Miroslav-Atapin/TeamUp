package com.example.teamup;

public class Event {
    public String name;
    public String date;
    public String timeStart;
    public String timeEnd;
    public String location;
    public String info;
    public String category;
    public String level;
    public String creatorId;
    public String participants;

    public Event() {}

    public Event(String name, String date, String timeStart, String timeEnd,
                 String location, String info, String category, String level, String creatorId, String participants) {
        this.name = name;
        this.date = date;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.location = location;
        this.info = info;
        this.category = category;
        this.level = level;
        this.creatorId = creatorId;
        this.participants = participants;
    }
}
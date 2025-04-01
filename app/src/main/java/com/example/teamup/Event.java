package com.example.teamup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Event implements Serializable {
    public String name;
    public String date;
    public String timeStart;
    public String timeEnd;
    public String location;
    public String info;
    public String category;
    public String level;
    public String creatorId;
    public String participantsCount;
    public List<String> participantsList;

    public Event() {
        participantsList = new ArrayList<>();
    }

    public Event(String name, String date, String timeStart, String timeEnd,
                 String location, String info, String category, String level, String creatorId, String participantsCount) {
        this.name = name;
        this.date = date;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.location = location;
        this.info = info;
        this.category = category;
        this.level = level;
        this.creatorId = creatorId;
        this.participantsCount = participantsCount;
    }

}
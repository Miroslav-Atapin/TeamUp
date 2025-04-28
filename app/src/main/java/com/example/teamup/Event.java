package com.example.teamup;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Event implements Serializable {
    public String id;
    public String name;
    public String city;
    public String date;
    public String timeStart;
    public String timeEnd;
    public String location;
    public String info;
    public String category;
    public String level;
    public String creatorId;
    public Map<String, Boolean> participants;
    public int maxParticipants;

    public Event() {

    }

    public Event(String id, String name, String city, String date, String timeStart, String timeEnd,
                 String location, String info, String category, String level, String creatorId, Map<String, Boolean> participants, int maxParticipants) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.date = date;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.location = location;
        this.info = info;
        this.category = category;
        this.level = level;
        this.creatorId = creatorId;
        this.participants = participants != null ? participants : new HashMap<>();
        this.maxParticipants = maxParticipants;
    }

    public boolean isParticipant(String participantId) {
        return participants.containsKey(participantId);
    }

    public int getNumberOfParticipants() {
        return participants.size();
    }

    public boolean hasAvailableSlots() {
        return getNumberOfParticipants() < maxParticipants;
    }

    public int getAvailableSlots() {
        return maxParticipants - getNumberOfParticipants();
    }

}
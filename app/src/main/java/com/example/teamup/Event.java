package com.example.teamup;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Event implements Serializable {
    public String id; // Уникальный идентификатор события
    public String name;
    public String date;
    public String timeStart;
    public String timeEnd;
    public String location;
    public String info;
    public String category;
    public String level;
    public String creatorId;
    public Map<String, Boolean> participants; // Используем Map для участников
    public int maxParticipants; // Максимальное количество участников

    public Event() {
        participants = new HashMap<>(); // Инициализируем пустой Map
    }

    public Event(String id, String name, String date, String timeStart, String timeEnd,
                 String location, String info, String category, String level, String creatorId, Map<String, Boolean> participants, int maxParticipants) {
        this.id = id;
        this.name = name;
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

    public void addParticipant(String participantId) {
        participants.put(participantId, true);
    }

    public void removeParticipant(String participantId) {
        participants.remove(participantId);
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

}
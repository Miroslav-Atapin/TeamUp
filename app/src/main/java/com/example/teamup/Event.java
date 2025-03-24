package com.example.teamup;

import java.util.ArrayList;
import java.util.List;

public class Event {
    public String creatorId;
    public String name;
    public String date;
    public String timeStart;
    public String timeEnd;
    public String location;
    public String category;
    public String level;
    public String numberPlayers;
    public String info;

    // Поле для хранения списка участников
    public List<String> participants;

    public Event() {} // Необходимый конструктор для Firebase

    public Event(String creatorId, String name, String date, String timeStart, String timeEnd,
                 String location, String category, String level, String numberPlayers, String info) {
        this.creatorId = creatorId;
        this.name = name;
        this.date = date;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.location = location;
        this.category = category;
        this.level = level;
        this.numberPlayers = numberPlayers;
        this.info = info;

        // Инициализация пустого списка участников
        this.participants = new ArrayList<>();
    }

    // Методы для управления участниками
    public void addParticipant(String participantId) {
        if (!this.participants.contains(participantId)) {
            this.participants.add(participantId);
        }
    }

    public void removeParticipant(String participantId) {
        this.participants.remove(participantId);
    }

    public boolean isParticipant(String userId) {
        return this.participants.contains(userId);
    }
}
package com.wimf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FriendProfile {

    // 1. ПОЛЯ КЛАССА 
    private String nickname;
    private List<String> notes = new ArrayList<>();
    private String onlineColor;  // Цвет, когда друг в сети
    private String offlineColor; // Цвет, когда друг не в сети
    private long lastSeenTimestamp = 0;

    // 2. КОНСТРУКТОР
    public FriendProfile(String nickname) { // Теперь принимаем только ник
        this.nickname = nickname;
        this.notes = new ArrayList<>();
        // Устанавливаем разумные значения по умолчанию
        this.onlineColor = "#55FF55";  // Ярко-зеленый
        this.offlineColor = "#AAAAAA"; // Серый
    }

    // 3. МЕТОДЫ

    // --- Геттеры ---
    public String getNickname() { return nickname; }

    public String getOnlineColor() { return onlineColor; }
    public String getOfflineColor() { return offlineColor; }

    public long getLastSeenTimestamp() {
        return lastSeenTimestamp;
    }

    public void setLastSeenTimestamp(long lastSeenTimestamp) {
        this.lastSeenTimestamp = lastSeenTimestamp;
    }
    // --- Сеттеры ---
    public List<String> getNotes() { return this.notes; }
    public void setNotes(List<String> notes) { this.notes = notes; }
    // Добавим вспомогательные методы
    public void addNote(String note) { this.notes.add(note); }
    public boolean removeNote(int index) {
        if (index >= 0 && index < this.notes.size()) {
            this.notes.remove(index);
            return true;
        }
        return false;
    }
    public boolean setNote(int index, String newNote) {
        if (index >= 0 && index < this.notes.size()) {
            this.notes.set(index, newNote);
            return true;
        }
        return false;
    }

    public void setOnlineColor(String onlineColor) { this.onlineColor = onlineColor; }
    public void setOfflineColor(String offlineColor) { this.offlineColor = offlineColor; }


}

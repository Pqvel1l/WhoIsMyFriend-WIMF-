package com.wimf;

import java.util.ArrayList;
import java.util.List;

public class ModConfig {
    // Поле для хранения глобальных настроек
    private String onlineColor = "§a"; // Зеленый (&a)
    private String offlineColor = "§7"; // Серый (&7)

    // Поле для хранения списка друзей
    private List<FriendProfile> friends = new ArrayList<>();

    // Геттеры и сеттеры для всех полей
    public String getOnlineColor() {
        return onlineColor;
    }

    public void setOnlineColor(String onlineColor) {
        this.onlineColor = onlineColor;
    }

    public String getOfflineColor() {
        return offlineColor;
    }

    public void setOfflineColor(String offlineColor) {
        this.offlineColor = offlineColor;
    }

    public List<FriendProfile> getFriends() {
        return friends;
    }

    public void setFriends(List<FriendProfile> friends) {
        this.friends = friends;
    }
    private String friendIcon = "👤";
    private String friendIconColor = "#ffdc00"; // Используем названия или HEX

    public String getFriendIcon() { return friendIcon; }
    public void setFriendIcon(String icon) { this.friendIcon = icon; }
    public String getFriendIconColor() { return friendIconColor; }
    public void setFriendIconColor(String color) { this.friendIconColor = color; }
}
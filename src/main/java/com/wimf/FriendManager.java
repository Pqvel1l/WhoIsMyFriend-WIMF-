package com.wimf;

import java.util.List;
import java.util.Optional;

public class FriendManager {
    private static final FriendManager INSTANCE = new FriendManager();
    public static FriendManager getInstance() { return INSTANCE; }
    private FriendManager() {}

    // Мы больше не храним список здесь. Мы просто проксируем вызовы к конфигу.
    private List<FriendProfile> getFriendsList() {
        return ConfigManager.getInstance().getConfig().getFriends();
    }

    public boolean addFriend(String nickname) {
        if (isFriend(nickname)) {
            return false;
        }
        getFriendsList().add(new FriendProfile(nickname));
        ConfigManager.getInstance().save(); // Сохраняем весь конфиг
        return true;
    }

    public boolean removeFriend(String nickname) {
        boolean removed = getFriendsList().removeIf(profile -> profile.getNickname().equalsIgnoreCase(nickname));
        if (removed) {
            ConfigManager.getInstance().save(); // Сохраняем, только если что-то изменилось
        }
        return removed;
    }

    public boolean isFriend(String nickname) {
        return getFriendsList().stream().anyMatch(profile -> profile.getNickname().equalsIgnoreCase(nickname));
    }

    public Optional<FriendProfile> getFriend(String nickname) {
        return getFriendsList().stream()
                .filter(profile -> profile.getNickname().equalsIgnoreCase(nickname))
                .findFirst();
    }

    public List<FriendProfile> getAllFriends() {
        return getFriendsList(); // Теперь можно возвращать напрямую
    }
}
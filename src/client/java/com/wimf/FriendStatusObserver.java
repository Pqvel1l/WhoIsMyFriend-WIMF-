package com.wimf;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

import java.util.Set;
import java.util.stream.Collectors;

public class FriendStatusObserver {
    private static int tickCounter = 0;
    private static final int CHECK_INTERVAL = 20 * 5; // 20 тиков = 1 секунда. Проверяем каждые 5 секунд.

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(FriendStatusObserver::onClientTick);
    }

    private static void onClientTick(MinecraftClient client) {
        // Увеличиваем счетчик тиков
        tickCounter++;
        if (tickCounter < CHECK_INTERVAL) {
            return; // Если время проверки еще не пришло, выходим
        }
        tickCounter = 0; // Сбрасываем счетчик

        // Проверяем, зашли ли мы в мир
        if (client.player == null || client.getNetworkHandler() == null) {
            return;
        }

        // Получаем актуальный список ников всех игроков на сервере
        Set<String> onlinePlayerNames = client.getNetworkHandler().getPlayerList().stream()
                .map(p -> p.getProfile().getName())
                .collect(Collectors.toSet());

        boolean needsSave = false;

        // Проходимся по всем нашим друзьям
        for (FriendProfile friend : FriendManager.getInstance().getAllFriends()) {
            boolean isCurrentlyOnline = onlinePlayerNames.contains(friend.getNickname());
            long lastSeen = friend.getLastSeenTimestamp();

            if (isCurrentlyOnline) {
                // Друг сейчас в сети
                if (lastSeen != 1) { // Если он не был помечен как "в сети"
                    friend.setLastSeenTimestamp(1); // Помечаем как "в сети"
                    needsSave = true;
                }
            } else {
                // Друг сейчас оффлайн
                if (lastSeen == 1) { // Если он ТОЛЬКО ЧТО был "в сети", а теперь пропал
                    friend.setLastSeenTimestamp(System.currentTimeMillis()); // Фиксируем время выхода
                    needsSave = true;
                }
            }
        }

        // Сохраняем конфиг, только если были изменения
        if (needsSave) {
            ConfigManager.getInstance().save();
        }
    }
}
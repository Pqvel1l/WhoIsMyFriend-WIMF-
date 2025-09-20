package com.wimf;

import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FriendTeam {
    private static final String TEAM_NAME = "wimf_friends";
    private static Team teamInstance;

    public static void initialize() {
        MinecraftClient client = MinecraftClient.getInstance();
        // Мы должны дождаться, пока мир загрузится, чтобы получить доступ к Scoreboard
        // Этот код будет вызван позже
        if (client.world == null) return;

        Scoreboard scoreboard = client.world.getScoreboard();
        Team existingTeam = scoreboard.getTeam(TEAM_NAME);

        if (existingTeam == null) {
            // Команды не существует, создаем ее
            teamInstance = scoreboard.addTeam(TEAM_NAME);
            // Это название, которое будет использоваться для сортировки. Пробел в начале ставит его наверх.
            teamInstance.setDisplayName(Text.literal(" WIMF Friends"));
            // Сделаем так, чтобы у команды не было никаких видимых префиксов или цветов
            teamInstance.setColor(Formatting.RESET);
        } else {
            teamInstance = existingTeam;
        }
    }

    public static Team getTeam() {
        // Если команда по какой-то причине не создалась, вызываем initialize() еще раз.
        if (teamInstance == null) {
            initialize();
        }
        return teamInstance;
    }
}
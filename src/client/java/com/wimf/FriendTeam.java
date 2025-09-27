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
        if (client.world == null) return;

        Scoreboard scoreboard = client.world.getScoreboard();
        Team existingTeam = scoreboard.getTeam(TEAM_NAME);

        if (existingTeam == null) {
            teamInstance = scoreboard.addTeam(TEAM_NAME);
            teamInstance.setDisplayName(Text.literal(" WIMF Friends"));
            teamInstance.setColor(Formatting.RESET);
        } else {
            teamInstance = existingTeam;
        }
    }

    public static Team getTeam() {
        if (teamInstance == null) {
            initialize();
        }
        return teamInstance;
    }
}

package com.wimf; // Убедись, что пакет правильный!

import com.wimf.ConfigManager;
import com.wimf.FriendTeam;
import com.wimf.ModCommands;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class WhoIsMyFriendsWIMFClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ConfigManager.getInstance().load();
		ModCommands.register();

		// Регистрируем "слушателя" события входа на сервер/в мир
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			// Как только мы зашли в мир, инициализируем нашу команду
			FriendTeam.initialize();
		});
	}
}
package com.wimf.mixin.client;

import com.wimf.ConfigManager;
import com.wimf.FriendManager;
import com.wimf.ModConfig;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.wimf.ModUtils; // <-- ДОБАВЬТЕ ЭТОТ ИМПОРТ

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

	@Inject(method = "getPlayerName", at = @At("RETURN"), cancellable = true)
	private void modifyFriendName(PlayerListEntry entry, CallbackInfoReturnable<Text> cir) {
		// Проверяем, является ли игрок другом
		if (FriendManager.getInstance().isFriend(entry.getProfile().getName())) {
			ModConfig config = ConfigManager.getInstance().getConfig();

			// 1. Получаем оригинальное имя, которое вернул сервер
			Text originalName = cir.getReturnValue();

			// 2. Создаем иконку
			String iconString = config.getFriendIcon().trim();
			if (iconString.isEmpty()) {
				return; // Если иконка пустая, ничего не делаем
			}
			MutableText iconText = Text.literal(iconString + " ");

			// 3. Получаем строку с цветом из конфига
			String iconColorString = config.getFriendIconColor();

			// 4. Используем "умный" парсер для получения цвета
			TextColor color = ModUtils.parseColor(iconColorString);

			// 5. Применяем цвет, если он валидный
			if (color != null) {
				iconText.styled(style -> style.withColor(color));
			}

			// 6. Собираем финальный результат: [наша цветная иконка] + [оригинальное имя]
			// Это сохранит любые префиксы от сервера.
			MutableText finalName = iconText.append(originalName);

			// 7. Подменяем возвращаемое значение
			cir.setReturnValue(finalName);
		}
	}
}

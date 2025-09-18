package com.wimf;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ModUtils {

    // Наш префикс. Мы можем легко поменять его здесь в любой момент.
    private static final Text PREFIX = Text.literal("[WIMF] ").formatted(Formatting.GOLD); // GOLD = золотой цвет (§6)

    /**
     * Создает сообщение для чата с префиксом мода.
     * @param message Текст сообщения.
     * @return Готовое к отправке Text-сообщение.
     */
    public static Text translatable(String message) {
        // .copy() создает копию, чтобы не изменять оригинальный PREFIX
        // .append() добавляет наш текст к префиксу
        return PREFIX.copy().append(Text.literal(message).formatted(Formatting.GRAY)); // GRAY = серый цвет (§7)
    }

    /**
     * Перегруженная версия для сообщений с форматированием (цветами).
     * @param message Текст сообщения, уже содержащий цветовые коды.
     * @return Готовое к отправке Text-сообщение.
     */
    public static Text translatableWithCodes(String message) {
        return PREFIX.copy().append(Text.literal(message));
    }
}
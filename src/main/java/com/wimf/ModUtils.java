package com.wimf;

import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModUtils {

    // --- Логика для сообщений ---
    private static final Text PREFIX = Text.translatable("wimf.prefix");

    public static Text translatable(String key, Object... args) {
        return PREFIX.copy().append(Text.translatable(key, args));
    }


    // --- Логика для парсинга цветов ---

    // Статическая карта для мгновенного преобразования кодов '&' в объекты Formatting
    private static final Map<Character, Formatting> LEGACY_COLOR_MAP = Stream.of(Formatting.values())
            .filter(Formatting::isColor)
            .collect(Collectors.toMap(Formatting::getCode, formatting -> formatting));

    /**
     * "Умный" парсер цвета. Принимает строку и пытается преобразовать ее в TextColor.
     * Поддерживает:
     * - HEX формат: "#RRGGBB"
     * - Устаревшие коды: "&c", "&6", "&d"
     */
    public static TextColor parseColor(String colorInput) {
        if (colorInput == null || colorInput.isEmpty()) {
            return null;
        }

        // 1. Пробуем обработать устаревший формат (& + код)
        if (colorInput.startsWith("&") && colorInput.length() == 2) {
            Formatting formatting = LEGACY_COLOR_MAP.get(colorInput.charAt(1));
            if (formatting != null) {
                // Преобразуем enum Formatting в TextColor
                return TextColor.fromFormatting(formatting);
            }
        }

        // 2. Если не получилось, доверяем ванильному парсеру (он умеет в HEX и имена)
        try {

            return TextColor.parse(colorInput).getOrThrow();
        } catch (Exception ignored) {

            return null;
        }
    }
}

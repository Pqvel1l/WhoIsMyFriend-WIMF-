package com.wimf;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ModCommands {

    private static final int FRIENDS_PER_PAGE = 5;

    // --- Провайдеры подсказок ---
    private static final SuggestionProvider<FabricClientCommandSource> FRIEND_SUGGESTIONS = (context, builder) ->
            CommandSource.suggestMatching(FriendManager.getInstance().getAllFriends().stream().map(FriendProfile::getNickname), builder);
    private static final SuggestionProvider<FabricClientCommandSource> PLAYER_SUGGESTIONS = (context, builder) ->
            CommandSource.suggestMatching(context.getSource().getClient().getNetworkHandler().getPlayerList().stream().map(p -> p.getProfile().getName()), builder);


    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

            dispatcher.register(ClientCommandManager.literal("friend")
                    // --- ГЛАВНОЕ МЕНЮ ---
                    .executes(context -> {
                        FabricClientCommandSource source = context.getSource();
                        sendBlankLine(source);
                        source.sendFeedback(Text.translatable("wimf.menu.header"));
                        sendBlankLine(source);
                        source.sendFeedback(createSuggestButton(Text.translatable("wimf.menu.button.add").getString(), "/friend add ", Text.translatable("wimf.menu.button.add").getString()));
                        sendBlankLine(source);
                        source.sendFeedback(createRunButton(Text.translatable("wimf.menu.button.list").getString(), "/friend list 1", Text.translatable("wimf.menu.button.list").getString()));
                        sendBlankLine(source);
                        source.sendFeedback(createRunButton(Text.translatable("wimf.menu.button.settings").getString(), "/friend setting", Text.translatable("wimf.menu.button.settings").getString()));
                        sendBlankLine(source);
                        source.sendFeedback(Text.translatable("wimf.menu.footer"));
                        return 1;
                    })
                    // --- ADD ---
                    .then(ClientCommandManager.literal("add")
                            .then(ClientCommandManager.argument("nickname", StringArgumentType.word()).suggests(PLAYER_SUGGESTIONS)
                                    .executes(context -> {
                                        String nickname = StringArgumentType.getString(context, "nickname");
                                        boolean success = FriendManager.getInstance().addFriend(nickname);
                                        if (success) {
                                            context.getSource().sendFeedback(ModUtils.translatable("wimf.message.friend_added", nickname));
                                        } else {
                                            context.getSource().sendFeedback(ModUtils.translatable("wimf.message.friend_exists", nickname));
                                        }
                                        return 1;
                                    })
                            )
                    )
// Файл: com/wimf/ModCommands.java

// --- REMOVE ---
                            .then(ClientCommandManager.literal("remove")
                                    .then(ClientCommandManager.argument("nickname", StringArgumentType.word()).suggests(FRIEND_SUGGESTIONS)
                                            // Этот блок выполняется после нажатия кнопки [Да]
                                            .then(ClientCommandManager.literal("--confirm")
                                                    .executes(context -> {
                                                        String nickname = StringArgumentType.getString(context, "nickname");
                                                        boolean success = FriendManager.getInstance().removeFriend(nickname);
                                                        if (success) {
                                                            context.getSource().sendFeedback(ModUtils.translatable("wimf.message.friend_removed", nickname));
                                                        } else {
                                                            // Эта проверка остается на случай, если пользователь введет команду вручную
                                                            context.getSource().sendFeedback(ModUtils.translatable("wimf.message.friend_not_found", nickname));
                                                        }
                                                        return 1;
                                                    })
                                            )
                                            // Этот блок выполняется при вводе /friend remove <ник>
                                            .executes(context -> {
                                                String nickname = StringArgumentType.getString(context, "nickname");
                                                FabricClientCommandSource source = context.getSource();

                                                // <--- ВОТ ИСПРАВЛЕНИЕ: ПРОВЕРЯЕМ, ЯВЛЯЕТСЯ ЛИ ИГРОК ДРУГОМ ---
                                                if (FriendManager.getInstance().isFriend(nickname)) {
                                                    // Если да, то показываем диалог подтверждения
                                                    sendBlankLine(source);
                                                    source.sendFeedback(ModUtils.translatable("wimf.message.remove_confirm", nickname));
                                                    MutableText confirmation = Text.literal("  ")
                                                            .append(createRunButton(Text.translatable("wimf.message.button.confirm_yes").getString(), "/friend remove " + nickname + " --confirm", "§cThis action cannot be undone!"))
                                                            .append(Text.literal("  "))
                                                            .append(createRunButton(Text.translatable("wimf.message.button.confirm_no").getString(), "/friend profile " + nickname, "Return to profile"));
                                                    source.sendFeedback(confirmation);
                                                    sendBlankLine(source);
                                                } else {
                                                    // Если нет, то сразу выводим ошибку
                                                    source.sendFeedback(ModUtils.translatable("wimf.message.friend_not_found", nickname));
                                                }
                                                // <--- КОНЕЦ ИСПРАВЛЕНИЯ ---

                                                return 1;
                                            })
                                    )
                            )
                    // --- LIST (с пагинацией) ---
                    .then(ClientCommandManager.literal("list")
                            .then(ClientCommandManager.argument("page", IntegerArgumentType.integer(1))
                                    .executes(context -> executeListCommand(context.getSource(), IntegerArgumentType.getInteger(context, "page")))
                            )
                            .executes(context -> executeListCommand(context.getSource(), 1))
                    )
                    // --- PROFILE ---
                    .then(ClientCommandManager.literal("profile")
                            .then(ClientCommandManager.argument("nickname", StringArgumentType.word()).suggests(FRIEND_SUGGESTIONS)
                                    .executes(context -> executeProfileCommand(context.getSource(), StringArgumentType.getString(context, "nickname")))
                            )
                    )
                    // --- SETTING ---
                    .then(ClientCommandManager.literal("setting")
                            .executes(context -> executeSettingMenu(context.getSource()))
                            .then(ClientCommandManager.literal("color")
                                    .then(ClientCommandManager.literal("online").then(ClientCommandManager.argument("color_input", StringArgumentType.greedyString()).suggests((c, b) -> CommandSource.suggestMatching(new String[]{"#55FF55", "gold", "&a"}, b)).executes(c -> executeColorSetting(c.getSource(), "online", StringArgumentType.getString(c, "color_input")))))
                                    .then(ClientCommandManager.literal("offline").then(ClientCommandManager.argument("color_input", StringArgumentType.greedyString()).suggests((c, b) -> CommandSource.suggestMatching(new String[]{"#AAAAAA", "gray", "&7"}, b)).executes(c -> executeColorSetting(c.getSource(), "offline", StringArgumentType.getString(c, "color_input")))))
                                    .then(ClientCommandManager.literal("icon").then(ClientCommandManager.argument("color_input", StringArgumentType.greedyString()).suggests((c, b) -> CommandSource.suggestMatching(new String[]{"gold", "#FFD700", "&6"}, b)).executes(c -> executeColorSetting(c.getSource(), "icon", StringArgumentType.getString(c, "color_input")))))
                            )
                            .then(ClientCommandManager.literal("icon")
                                    .then(ClientCommandManager.argument("icon_text", StringArgumentType.string())
                                            .executes(context -> {
                                                String icon = StringArgumentType.getString(context, "icon_text");
                                                if (icon.length() > 5) {
                                                    context.getSource().sendFeedback(ModUtils.translatable("wimf.message.icon_too_long"));
                                                    return 0;
                                                }
                                                ConfigManager.getInstance().getConfig().setFriendIcon(icon);
                                                ConfigManager.getInstance().save();
                                                context.getSource().sendFeedback(ModUtils.translatable("wimf.message.icon_set"));
                                                return executeSettingMenu(context.getSource());
                                            })
                                    )
                            )
                    )
                    // --- NOTE ---
                    .then(ClientCommandManager.literal("note")
                            .then(ClientCommandManager.argument("nickname", StringArgumentType.word()).suggests(FRIEND_SUGGESTIONS)
                                    .then(ClientCommandManager.literal("list").executes(context -> executeNoteList(context.getSource(), StringArgumentType.getString(context, "nickname"))))
                                    .then(ClientCommandManager.literal("add").then(ClientCommandManager.argument("text", StringArgumentType.greedyString()).executes(context -> executeNoteAdd(context.getSource(), StringArgumentType.getString(context, "nickname"), StringArgumentType.getString(context, "text")))))
                                    .then(ClientCommandManager.literal("remove").then(ClientCommandManager.argument("id", IntegerArgumentType.integer(1)).executes(context -> executeNoteRemove(context.getSource(), StringArgumentType.getString(context, "nickname"), IntegerArgumentType.getInteger(context, "id")))))
                                    .then(ClientCommandManager.literal("set").then(ClientCommandManager.argument("id", IntegerArgumentType.integer(1)).then(ClientCommandManager.argument("text", StringArgumentType.greedyString()).executes(context -> executeNoteSet(context.getSource(), StringArgumentType.getString(context, "nickname"), IntegerArgumentType.getInteger(context, "id"), StringArgumentType.getString(context, "text"))))))
                            )
                    )
            );
        });
    }

    // --- ЛОГИКА КОМАНД ---

    private static int executeListCommand(FabricClientCommandSource source, int page) {
        List<FriendProfile> friends = FriendManager.getInstance().getAllFriends();
        ModConfig config = ConfigManager.getInstance().getConfig();
        int totalPages = Math.max(1, (int) Math.ceil((double) friends.size() / FRIENDS_PER_PAGE));
        if (page > totalPages) page = totalPages;

        sendBlankLine(source);
        source.sendFeedback(Text.translatable("wimf.list.header", page, totalPages));
        sendBlankLine(source);

        if (friends.isEmpty()) {
            source.sendFeedback(Text.translatable("wimf.list.empty"));
        } else {
            int startIndex = (page - 1) * FRIENDS_PER_PAGE;
            int endIndex = Math.min(startIndex + FRIENDS_PER_PAGE, friends.size());
            var playerList = source.getClient().getNetworkHandler().getPlayerList();

            for (int i = startIndex; i < endIndex; i++) {
                FriendProfile profile = friends.get(i);
                boolean isOnline = playerList.stream().anyMatch(p -> p.getProfile().getName().equalsIgnoreCase(profile.getNickname()));
                String colorCode = isOnline ? config.getOnlineColor() : config.getOfflineColor();

                MutableText nicknameText = Text.literal(profile.getNickname());

                // --- ИЗМЕНЕНИЕ ЗДЕСЬ ---
                // БЫЛО: try-catch с TextColor.parse(colorCode)
                // СТАЛО: Используем ModUtils.parseColor
                TextColor color = ModUtils.parseColor(colorCode);
                if (color != null) {
                    nicknameText.setStyle(Style.EMPTY.withColor(color));
                } else {
                    nicknameText.formatted(Formatting.WHITE); // Цвет по умолчанию, если в конфиге ошибка
                }
                // --- КОНЕЦ ИЗМЕНЕНИЯ ---

                MutableText message = Text.literal("  §7- ").append(nicknameText).append(" ");
                message.append(createRunButton(Text.translatable("wimf.list.button.profile").getString(), "/friend profile " + profile.getNickname(), ""));
                source.sendFeedback(message);
            }
        }
        sendBlankLine(source);

        // ... (остальная часть метода без изменений)
        MutableText navLine = Text.literal("  ");
        if (page > 1) navLine.append(createRunButton(Text.translatable("wimf.list.nav.back").getString(), "/friend list " + (page - 1), ""));
        if (page > 1 && page < totalPages) navLine.append(Text.literal("   "));
        if (page < totalPages) navLine.append(createRunButton(Text.translatable("wimf.list.nav.next").getString(), "/friend list " + (page + 1), ""));
        if (navLine.getString().trim().length() > 0) source.sendFeedback(navLine);
        source.sendFeedback(createRunButton(Text.translatable("wimf.list.nav.main_menu").getString(), "/friend", ""));
        source.sendFeedback(Text.translatable("wimf.menu.footer"));
        return 1;
    }
    private static int executeProfileCommand(FabricClientCommandSource source, String nickname) {
        Optional<FriendProfile> friendOpt = FriendManager.getInstance().getFriend(nickname);
        if (friendOpt.isPresent()) {
            FriendProfile profile = friendOpt.get();


            source.sendFeedback(Text.translatable("wimf.profile.header", profile.getNickname()));
            sendBlankLine(source);
            source.sendFeedback(Text.translatable("wimf.profile.status_header", formatLastSeen(profile.getLastSeenTimestamp())));
            sendBlankLine(source);
            // --- КОНЕЦ НОВОГО БЛОКА ---
            source.sendFeedback(Text.translatable("wimf.profile.notes_header"));
            List<String> notes = profile.getNotes();

            if (notes.isEmpty()) {
                source.sendFeedback(Text.translatable("wimf.profile.notes_empty"));
            } else {
                for (int i = 0; i < notes.size(); i++) {
                    int noteId = i + 1; String noteText = notes.get(i);
                    MutableText noteLine = Text.literal("    §7" + noteId + ". §f" + noteText + " ");
                    noteLine.append(createSuggestButton(Text.translatable("wimf.profile.button.note_edit").getString(), "/friend note " + nickname + " set " + noteId + " " + noteText, ""));
                    noteLine.append(Text.literal(" ")).append(createRunButton(Text.translatable("wimf.profile.button.note_delete").getString(), "/friend note " + nickname + " remove " + noteId, ""));
                    source.sendFeedback(noteLine);
                }
            }
            sendBlankLine(source);
            source.sendFeedback(createSuggestButton(Text.translatable("wimf.profile.button.note_add").getString(), "/friend note " + nickname + " add ", ""));

            // --- НОВАЯ КНОПКА "НАПИСАТЬ В ЛС" ---
            source.sendFeedback(createSuggestButton(Text.translatable("wimf.profile.button.friend_msg").getString(), "/msg " + nickname + " ", ""));
            source.sendFeedback(createRunButton(Text.translatable("wimf.profile.button.friend_delete").getString(), "/friend remove " + nickname, ""));
            sendBlankLine(source);
            source.sendFeedback(createRunButton(Text.translatable("wimf.profile.nav.back_to_list").getString(), "/friend list 1", ""));
            source.sendFeedback(Text.translatable("wimf.menu.footer"));
        } else {
            source.sendFeedback(ModUtils.translatable("wimf.message.friend_not_found", nickname));
        }
        return 1;
    }
    private static int executeSettingMenu(FabricClientCommandSource source) {
        ModConfig config = ConfigManager.getInstance().getConfig();
        sendBlankLine(source);
        source.sendFeedback(Text.translatable("wimf.settings.header"));
        sendBlankLine(source);

        // --- ИЗМЕНЕНИЕ ДЛЯ ONLINE ЦВЕТА ---
        MutableText onlineLine = Text.literal(Text.translatable("wimf.settings.online_color").getString());
        TextColor onlineColor = ModUtils.parseColor(config.getOnlineColor());
        if (onlineColor != null) {
            onlineLine.append(Text.translatable("wimf.settings.text.example").getString()).setStyle(Style.EMPTY.withColor(onlineColor));
        } else {
            onlineLine.append(Text.translatable("wimf.settings.text.example_error").getString()).formatted(Formatting.BOLD);
        }
        onlineLine.append(" ").append(createSuggestButton(Text.translatable("wimf.settings.button.edit").getString(), "/friend setting color online " + config.getOnlineColor(), ""));
        source.sendFeedback(onlineLine);
        sendBlankLine(source);

        // --- ИЗМЕНЕНИЕ ДЛЯ OFFLINE ЦВЕТА ---
        MutableText offlineLine = Text.literal(Text.translatable("wimf.settings.offline_color").getString());
        TextColor offlineColor = ModUtils.parseColor(config.getOfflineColor());
        if (offlineColor != null) {
            offlineLine.append(Text.translatable("wimf.settings.text.example").getString()).setStyle(Style.EMPTY.withColor(offlineColor));
        } else {
            offlineLine.append(Text.translatable("wimf.settings.text.example_error").getString()).formatted(Formatting.BOLD);
        }
        offlineLine.append(" ").append(createSuggestButton(Text.translatable("wimf.settings.button.edit").getString(), "/friend setting color offline " + config.getOfflineColor(), ""));
        source.sendFeedback(offlineLine);
        sendBlankLine(source);

        // --- ИЗМЕНЕНИЕ ДЛЯ ЦВЕТА ИКОНКИ ---
        MutableText iconLine = Text.literal(Text.translatable("wimf.settings.icon").getString());
        MutableText iconPreview = Text.literal(config.getFriendIcon());
        TextColor iconColor = ModUtils.parseColor(config.getFriendIconColor());
        if (iconColor != null) {
            iconPreview.setStyle(Style.EMPTY.withColor(iconColor));
        }
        iconLine.append(iconPreview);
        iconLine.append(" ").append(createSuggestButton(Text.translatable("wimf.settings.button.icon_edit").getString(), "/friend setting icon ", ""));
        iconLine.append(" ").append(createSuggestButton(Text.translatable("wimf.settings.button.icon_color_edit").getString(), "/friend setting color icon " + config.getFriendIconColor(), ""));
        source.sendFeedback(iconLine);
        sendBlankLine(source);

        source.sendFeedback(createRunButton(Text.translatable("wimf.list.nav.main_menu").getString(), "/friend", ""));
        source.sendFeedback(Text.translatable("wimf.menu.footer"));
        return 1;
    }
// Файл: com/wimf/client/ModCommands.java

    private static int executeColorSetting(FabricClientCommandSource source, String type, String colorInput) {
        // 1. Вызываем наш новый "умный" парсер
        TextColor parsedColor = ModUtils.parseColor(colorInput);

        // 2. Проверяем результат. Если null - цвет невалидный.
        if (parsedColor == null) {
            source.sendFeedback(ModUtils.translatable("wimf.message.color_error"));
            return 0; // Возвращаем 0 или -1 для обозначения неудачи
        }

        // 3. Если цвет валидный, сохраняем ИСХОДНУЮ строку в конфиг
        // Это позволит пользователю видеть в конфиге именно то, что он ввел (&6, а не "gold")
        switch (type) {
            case "online" -> ConfigManager.getInstance().getConfig().setOnlineColor(colorInput);
            case "offline" -> ConfigManager.getInstance().getConfig().setOfflineColor(colorInput);
            case "icon" -> ConfigManager.getInstance().getConfig().setFriendIconColor(colorInput);
        }

        ConfigManager.getInstance().save();
        source.sendFeedback(ModUtils.translatable("wimf.message.color_set", type, colorInput));

        // Перезапускаем команду, чтобы пользователь сразу увидел примененные изменения
        // Это хорошая практика для UI
        if (source.getClient().player != null) {
            source.getClient().player.networkHandler.sendChatCommand("friend setting");
        }

        return 1; // Успех
    }

    // --- МЕТОДЫ ДЛЯ ЗАМЕТОК ---
    private static int executeNoteAdd(FabricClientCommandSource source, String nickname, String text) {
        return FriendManager.getInstance().getFriend(nickname).map(profile -> {
            profile.addNote(text);
            ConfigManager.getInstance().save();
            source.sendFeedback(ModUtils.translatable("wimf.message.note_added", nickname));
            source.getClient().getNetworkHandler().sendChatCommand("friend profile " + nickname);
            return 1;
        }).orElseGet(() -> {
            source.sendFeedback(ModUtils.translatable("wimf.message.friend_not_found", nickname));
            return 0;
        });
    }

    private static int executeNoteSet(FabricClientCommandSource source, String nickname, int id, String text) {
        return FriendManager.getInstance().getFriend(nickname).map(profile -> {
            if (profile.setNote(id - 1, text)) {
                ConfigManager.getInstance().save();
                source.sendFeedback(ModUtils.translatable("wimf.message.note_set", id));
                source.getClient().getNetworkHandler().sendChatCommand("friend profile " + nickname);
            } else {
                source.sendFeedback(ModUtils.translatable("wimf.message.note_not_found"));
            }
            return 1;
        }).orElseGet(() -> {
            source.sendFeedback(ModUtils.translatable("wimf.message.friend_not_found", nickname));
            return 0;
        });
    }

    private static int executeNoteRemove(FabricClientCommandSource source, String nickname, int id) {
        return FriendManager.getInstance().getFriend(nickname).map(profile -> {
            if (profile.removeNote(id - 1)) {
                ConfigManager.getInstance().save();
                source.sendFeedback(ModUtils.translatable("wimf.message.note_removed", id));
                source.getClient().getNetworkHandler().sendChatCommand("friend profile " + nickname);
            } else {
                source.sendFeedback(ModUtils.translatable("wimf.message.note_not_found"));
            }
            return 1;
        }).orElseGet(() -> {
            source.sendFeedback(ModUtils.translatable("wimf.message.friend_not_found", nickname));
            return 0;
        });
    }

    private static int executeNoteList(FabricClientCommandSource source, String nickname) {
        return FriendManager.getInstance().getFriend(nickname).map(profile -> {
            List<String> notes = profile.getNotes();
            source.sendFeedback(ModUtils.translatable("wimf.profile.notes_header", nickname));
            if (notes.isEmpty()) {
                source.sendFeedback(Text.translatable("wimf.profile.notes_empty"));
            } else {
                for (int i = 0; i < notes.size(); i++) {
                    source.sendFeedback(Text.literal("  §7" + (i + 1) + ". §f" + notes.get(i)));
                }
            }
            return 1;
        }).orElseGet(() -> {
            source.sendFeedback(ModUtils.translatable("wimf.message.friend_not_found", nickname));
            return 0;
        });
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ UI ---
    private static void sendBlankLine(FabricClientCommandSource source) {
        source.sendFeedback(Text.literal(" "));
    }

    private static MutableText createSuggestButton(String text, String command, String hoverText) {
        return Text.literal(text).styled(style -> style
                .withClickEvent(new ClickEvent.SuggestCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(Text.literal(hoverText))));
    }

    private static MutableText createRunButton(String text, String command, String hoverText) {
        return Text.literal(text).styled(style -> style
                .withClickEvent(new ClickEvent.RunCommand(command))
                .withHoverEvent(new HoverEvent.ShowText(Text.literal(hoverText))));
    }

    private static Text formatLastSeen(long timestamp) {
        if (timestamp == 1) {
            return Text.translatable("wimf.profile.status.online");
        }
        if (timestamp == 0) {
            return Text.translatable("wimf.profile.status.never");
        }


        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        Date resultDate = new Date(timestamp);

        String dateString = dateFormat.format(resultDate);
        String timeString = timeFormat.format(resultDate);
        return Text.translatable("wimf.profile.status.last_seen_format", dateString, timeString);
    }

}
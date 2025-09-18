package com.wimf;

// Стандартные импорты для команд
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
// Новые импорты для интерактивных элементов
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

// Импорт для Optional
import java.util.List;
import java.util.Optional;

public class ModCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

            dispatcher.register(CommandManager.literal("friend")
                    // --- ГЛАВНОЕ МЕНЮ ---
                    .executes(context -> {
                        ServerCommandSource source = context.getSource();
                        sendBlankLine(source);
                        source.sendFeedback(() -> Text.literal("§6-----------§e[ WIMF ]§6-----------"), false);
                        sendBlankLine(source);
                        source.sendFeedback(() -> createSuggestButton(" §a[Добавить друга]", "/friend add ", "Ввести ник для добавления"), false);
                        sendBlankLine(source);
                        source.sendFeedback(() -> createRunButton(" §b[Посмотреть список друзей]", "/friend list", "Показать всех друзей"), false);
                        sendBlankLine(source);
                        source.sendFeedback(() -> createSuggestButton(" §c[Настройки]", "/friend setting ", "Изменить настройки мода"), false);
                        sendBlankLine(source);
                        source.sendFeedback(() -> Text.literal("§6---------------------------------"), false);
                        return 1;
                    })
                    // --- ПОДКОМАНДА "ADD" ---
                    .then(CommandManager.literal("add")
                            .then(CommandManager.argument("nickname", StringArgumentType.word())
                                    .suggests(PLAYER_SUGGESTIONS)
                                    .executes(context -> {
                                        String nickname = StringArgumentType.getString(context, "nickname");
                                        boolean success = FriendManager.getInstance().addFriend(nickname);
                                        if (success) {
                                            context.getSource().sendFeedback(() -> ModUtils.translatableWithCodes("§aИгрок " + nickname + " добавлен в друзья."), false);
                                        } else {
                                            context.getSource().sendFeedback(() -> ModUtils.translatableWithCodes("§cИгрок " + nickname + " уже в друзьях."), false);
                                        }
                                        return 1;
                                    })
                            )
                    )
                    // --- ПОДКОМАНДА "REMOVE" ---
                    .then(CommandManager.literal("remove")
                            .then(CommandManager.argument("nickname", StringArgumentType.word())
                                    // Новая подкоманда для подтверждения
                                    .suggests(FRIEND_SUGGESTIONS)
                                    .then(CommandManager.literal("--confirm")
                                            .executes(context -> {
                                                String nickname = StringArgumentType.getString(context, "nickname");
                                                boolean success = FriendManager.getInstance().removeFriend(nickname);
                                                if (success) {
                                                    context.getSource().sendFeedback(() -> ModUtils.translatableWithCodes("§eИгрок " + nickname + " удален из друзей."), false);
                                                } else {
                                                    // Эта ветка почти никогда не выполнится, но нужна для безопасности
                                                    context.getSource().sendFeedback(() -> ModUtils.translatableWithCodes("§cИгрок " + nickname + " не найден в списке друзей."), false);
                                                }
                                                return 1;
                                            })
                                    )
                            )
                    )
                    // --- ПОДКОМАНДА "LIST" ---
                    .then(CommandManager.literal("list")
                            .executes(context -> {
                                var friends = FriendManager.getInstance().getAllFriends();
                                ServerCommandSource source = context.getSource();

                                sendBlankLine(source);
                                source.sendFeedback(() -> Text.literal("§6-----------§e[ Список друзей ]§6-----------"), false);
                                sendBlankLine(source);

                                if (friends.isEmpty()) {
                                    source.sendFeedback(() -> Text.literal("§7Ваш список друзей пуст."), false);
                                } else {
                                    for (FriendProfile profile : friends) {
                                        MutableText message = Text.literal("  §7- " + profile.getNickname() + " ");
                                        message.append(createRunButton("§a[Профиль]", "/friend profile " + profile.getNickname(), "Открыть профиль игрока " + profile.getNickname()));
                                        source.sendFeedback(() -> message, false);

                                    }
                                }
                                sendBlankLine(source);
                                source.sendFeedback(() -> Text.literal("§6------------------------------------"), false);
                                return 1;
                            })
                    )
                    // --- ПОДКОМАНДА "PROFILE" ---
                    .then(CommandManager.literal("profile")
                            .then(CommandManager.argument("nickname", StringArgumentType.word())
                                    .suggests(FRIEND_SUGGESTIONS)
                                    .executes(context -> {
                                        String nickname = StringArgumentType.getString(context, "nickname");
                                        ServerCommandSource source = context.getSource();
                                        Optional<FriendProfile> friendOpt = FriendManager.getInstance().getFriend(nickname);

                                        if (friendOpt.isPresent()) {
                                            FriendProfile profile = friendOpt.get();
                                            sendBlankLine(source);
                                            source.sendFeedback(() -> Text.literal("§6-----------§e[ Профиль: " + profile.getNickname() + " ]§6-----------"), false);
                                            sendBlankLine(source);

                                            // Строка для заметок
// --- Строки для заметок ---
                                            source.sendFeedback(() -> Text.literal("  §7Заметки:"), false);
                                            List<String> notes = profile.getNotes();
                                            if (notes.isEmpty()) {
                                                source.sendFeedback(() -> Text.literal("    §oпусто"), false);
                                            } else {
                                                for (int i = 0; i < notes.size(); i++) {
                                                    int noteId = i + 1;
                                                    String noteText = notes.get(i);
                                                    MutableText noteLine = Text.literal("    §7" + noteId + ". §f" + noteText + " ");
                                                    noteLine.append(createSuggestButton("§e[Изменить]", "/friend note " + nickname + " set " + noteId + " " + noteText, "Изменить эту заметку"));
                                                    noteLine.append(Text.literal(" "));
                                                    noteLine.append(createRunButton("§c[X]", "/friend note" + nickname + " remove " + noteId, "§cУдалить эту заметку"));
                                                    source.sendFeedback(() -> noteLine, false);
                                                }
                                            }
                                            source.sendFeedback(() -> createSuggestButton("  §a[+ Добавить новую заметку]", "/friend note " + nickname + " add ", "Добавить новую заметку"), false);
                                            sendBlankLine(source);

                                            // Строка для удаления друга
                                            MutableText deleteLine = Text.literal("  ")
                                                    .append(createRunButton("§c[Удалить друга]", "/friend remove " + nickname + " --confirm", "§cВНИМАНИЕ! Кликните для удаления!"));
                                            source.sendFeedback(() -> deleteLine, false);
                                            sendBlankLine(source);
                                            source.sendFeedback(() -> Text.literal("§6-------------------------------------"), false);

                                        } else {
                                            source.sendFeedback(() -> ModUtils.translatableWithCodes("§cИгрок " + nickname + " не найден в списке друзей."), false);
                                        }
                                        return 1;
                                    })
                            )
                    )
//
//                    .then(CommandManager.literal("setting")
//                            // Главное меню настроек, вызывается по /friend setting
//                            .executes(context -> {
//                                ServerCommandSource source = context.getSource();
//                                ModConfig config = ConfigManager.getInstance().getConfig();
//
//                                sendBlankLine(source);
//                                source.sendFeedback(() -> Text.literal("§6-----------§e[ Настройки WIMF ]§6-----------"), false);
//                                sendBlankLine(source);
//
//                                // --- Строка для цвета "Онлайн" ---
//                                MutableText onlineLine = Text.literal("  §7Цвет статуса 'Онлайн': ");
//                                try {
//                                    // Парсим цвет из конфига
//                                    net.minecraft.text.TextColor textColor = net.minecraft.text.TextColor.parse(config.getOnlineColor()).getOrThrow();
//                                    // Создаем стиль с этим цветом
//                                    var style = net.minecraft.text.Style.EMPTY.withColor(textColor);
//                                    // Добавляем текст-пример с этим стилем
//                                    onlineLine.append(Text.literal("ПРИМЕР").setStyle(style));
//                                } catch (Exception e) {
//                                    // Если цвет в конфиге некорректный, показываем ошибку
//                                    onlineLine.append(Text.literal("§c[ОШИБКА]").formatted(Formatting.BOLD));
//                                }
//                                onlineLine.append(" ").append(createSuggestButton("§e[Изменить]", "/friend setting color online ", "Изменить цвет"));
//                                source.sendFeedback(() -> onlineLine, false);
//                                sendBlankLine(source);
//
//                                // --- Строка для цвета "Оффлайн" ---
//                                MutableText offlineLine = Text.literal("  §7Цвет статуса 'Оффлайн': ");
//                                try {
//                                    net.minecraft.text.TextColor textColor = net.minecraft.text.TextColor.parse(config.getOfflineColor()).getOrThrow();
//                                    var style = net.minecraft.text.Style.EMPTY.withColor(textColor);
//                                    offlineLine.append(Text.literal("ПРИМЕР").setStyle(style));
//                                } catch (Exception e) {
//                                    offlineLine.append(Text.literal("§c[ОШИБКА]").formatted(Formatting.BOLD));
//                                }
//                                offlineLine.append(" ").append(createSuggestButton("§e[Изменить]", "/friend setting color offline ", "Изменить цвет"));
//                                source.sendFeedback(() -> offlineLine, false);
//                                sendBlankLine(source);
//
//                                source.sendFeedback(() -> Text.literal("§6-------------------------------------"), false);
//                                return 1;
//                            })
//                            // Подменю для изменения цветов: /friend setting color ...
//                            .then(CommandManager.literal("color")
//                                    // /friend setting color online <цвет>
//                                    .then(CommandManager.literal("online")
//                                            .then(CommandManager.argument("color_input", StringArgumentType.string())
//                                                    .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{"#55FF55", "gold", "red", "aqua"}, builder))
//                                                    .executes(context -> {
//                                                        String colorInput = StringArgumentType.getString(context, "color_input");
//                                                        return executeColorSetting(context.getSource(), "online", colorInput);
//                                                    })
//                                            )
//                                    )
//                                    // /friend setting color offline <цвет>
//                                    .then(CommandManager.literal("offline")
//                                            .then(CommandManager.argument("color_input", StringArgumentType.string())
//                                                    .suggests((context, builder) -> CommandSource.suggestMatching(new String[]{"#AAAAAA", "gray", "dark_red", "dark_gray"}, builder))
//                                                    .executes(context -> {
//                                                        String colorInput = StringArgumentType.getString(context, "color_input");
//                                                        return executeColorSetting(context.getSource(), "offline", colorInput);
//                                                    })
//                                            )
//                                    )
//                            )
//                    ) // <-- Это закрывающая скобка для .then("setting")

                    .then(CommandManager.literal("note")
                            .then(CommandManager.argument("nickname", StringArgumentType.word()).suggests(FRIEND_SUGGESTIONS)
                                    // Новая подкоманда "list"
                                    .then(CommandManager.literal("list")
                                            .executes(context -> {
                                                String nickname = StringArgumentType.getString(context, "nickname");
                                                ServerCommandSource source = context.getSource();

                                                return FriendManager.getInstance().getFriend(nickname).map(profile -> {
                                                    List<String> notes = profile.getNotes();
                                                    source.sendFeedback(() -> ModUtils.translatableWithCodes("§6Заметки для игрока §e" + nickname + ":"), false);
                                                    if (notes.isEmpty()) {
                                                        source.sendFeedback(() -> Text.literal("  §oпусто"), false);
                                                    } else {
                                                        for (int i = 0; i < notes.size(); i++) {
                                                            int finalI = i;
                                                            source.sendFeedback(() -> Text.literal("  §7" + (finalI + 1) + ". §f" + notes.get(finalI)), false);
                                                        }
                                                    }
                                                    return 1;
                                                }).orElseGet(() -> {
                                                    source.sendFeedback(() -> ModUtils.translatableWithCodes("§cДруг не найден."), false);
                                                    return 0;
                                                });
                                            })
                                    )
                                    .then(CommandManager.literal("add")
                                            .then(CommandManager.argument("text", StringArgumentType.greedyString())
                                                    .executes(context -> executeNoteAdd(context.getSource(),
                                                            StringArgumentType.getString(context, "nickname"),
                                                            StringArgumentType.getString(context, "text"))
                                                    )
                                            )
                                    )
                                    // /friend note <ник> remove <ID>
                                    .then(CommandManager.literal("remove")
                                            .then(CommandManager.argument("id", IntegerArgumentType.integer(1)) // ID не может быть меньше 1
                                                    .executes(context -> executeNoteRemove(context.getSource(),
                                                            StringArgumentType.getString(context, "nickname"),
                                                            IntegerArgumentType.getInteger(context, "id"))
                                                    )
                                            )
                                    )
                                    // /friend note <ник> set <ID> <текст>
                                    .then(CommandManager.literal("set")
                                            .then(CommandManager.argument("id", IntegerArgumentType.integer(1))
                                                    .then(CommandManager.argument("text", StringArgumentType.greedyString())
                                                            .executes(context -> executeNoteSet(context.getSource(),
                                                                    StringArgumentType.getString(context, "nickname"),
                                                                    IntegerArgumentType.getInteger(context, "id"),
                                                                    StringArgumentType.getString(context, "text"))
                                                            )
                                                    )
                                            )
                                    )
                            )
                    )
            );
        });
    }
    // Метод для добавления заметки
    private static int executeNoteAdd(ServerCommandSource source, String nickname, String text) {
        return FriendManager.getInstance().getFriend(nickname).map(profile -> {
            profile.addNote(text);
            ConfigManager.getInstance().save();
            source.sendFeedback(() -> ModUtils.translatable("Заметка добавлена для " + nickname), false);
            return 1;
        }).orElseGet(() -> {
            source.sendFeedback(() -> ModUtils.translatableWithCodes("§cДруг не найден."), false);
            return 0;
        });
    }
    // Метод для изменения заметки
    private static int executeNoteSet(ServerCommandSource source, String nickname, int id, String text) {
        return FriendManager.getInstance().getFriend(nickname).map(profile -> {
            profile.setNote(id - 1,text);
            ConfigManager.getInstance().save();
            source.sendFeedback(() -> ModUtils.translatable("Заметка изменена для " + nickname), false);
            return 1;
        }).orElseGet(() -> {
            source.sendFeedback(() -> ModUtils.translatableWithCodes("§cДруг не найден."), false);
            return 0;
        });
    }
    // Метод для удаления заметки
    private static int executeNoteRemove(ServerCommandSource source, String nickname, int id) {
        return FriendManager.getInstance().getFriend(nickname).map(profile -> {
            if (profile.removeNote(id - 1)) { // ID для игрока с 1, индекс в списке с 0
                ConfigManager.getInstance().save();
                source.sendFeedback(() -> ModUtils.translatable("Заметка #" + id + " удалена."), false);
            } else {
                source.sendFeedback(() -> ModUtils.translatableWithCodes("§cЗаметки с таким ID не существует."), false);
            }
            return 1;
        }).orElseGet(() -> {
            source.sendFeedback(() -> ModUtils.translatableWithCodes("§cДруг не найден."), false);
            return 0;
        });
    }

    private static void sendBlankLine(ServerCommandSource source) {
        source.sendFeedback(() -> Text.literal(" "), false);
    }

    private static MutableText createSuggestButton(String text, String command, String hoverText) {
        return Text.literal(text)
                .styled(style -> style
                        .withClickEvent(new ClickEvent.SuggestCommand(command))
                        .withHoverEvent(new HoverEvent.ShowText(Text.literal(hoverText)))
                );
    }
    private static final SuggestionProvider<ServerCommandSource> FRIEND_SUGGESTIONS = (context, builder) ->
            CommandSource.suggestMatching(
                    FriendManager.getInstance().getAllFriends().stream().map(FriendProfile::getNickname),
                    builder
            );

    // Провайдер подсказок для ников ИГРОКОВ НА СЕРВЕРЕ
    private static final SuggestionProvider<ServerCommandSource> PLAYER_SUGGESTIONS = (context, builder) ->
            CommandSource.suggestMatching(context.getSource().getPlayerNames(), builder);

    private static int executeColorSetting(ServerCommandSource source, String type, String colorInput) {
        try {
            // Проверяем, валиден ли введенный цвет
            net.minecraft.text.TextColor.parse(colorInput).getOrThrow();

            if (type.equals("online")) {
                ConfigManager.getInstance().getConfig().setOnlineColor(colorInput);
            } else {
                ConfigManager.getInstance().getConfig().setOfflineColor(colorInput);
            }

            ConfigManager.getInstance().save();
            source.sendFeedback(() -> ModUtils.translatable("Цвет для статуса '" + type + "' успешно изменен."), false);

            // Автоматически показываем обновленное меню настроек
            source.getServer().getCommandManager().executeWithPrefix(source, "/friend setting");

        } catch (Exception e) {
            source.sendFeedback(() -> ModUtils.translatableWithCodes("§cНеверный формат цвета! Используйте HEX (#RRGGBB) или название (например, gold, aqua)."), false);
        }
        return 1;
    }
    private static MutableText createRunButton(String text, String command, String hoverText) {
        return Text.literal(text)
                .styled(style -> style
                        .withClickEvent(new ClickEvent.RunCommand(command))
                        .withHoverEvent(new HoverEvent.ShowText(Text.literal(hoverText)))
                );
    }
}
package unnamed.mmo.chat.command;

import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import org.jetbrains.annotations.NotNull;
import unnamed.mmo.chat.ChatMessage;
import unnamed.mmo.chat.ChatQuery;
import unnamed.mmo.chat.storage.ChatStorage;
import unnamed.mmo.util.FutureUtil;

import java.util.List;

public class LogCommand extends Command {
    private final ChatStorage storage;

    public LogCommand(@NotNull ChatStorage storage) {
        super("log");
        this.storage = storage;

        /*
        Thinking about time filtering, there are a few things you might want to do
        - All content on a specific day
        /log 2022/12/10
        - in the last 5 minutes
        /log after now-5m
        /log between now now-5m
        - more than 5 minutes ago
        /log before now-5m
        - between 5 and 10 minutes ago
        /log after now-10m before now-5m
        - between day 1 and day 2
        /log between 2022/12/2 2022/12/5
        - in a specific year / month in a year
        /log 2022/12
        /log 2022
        /log commands in 2022 on build_2 "glitch"

        A "between" clause seems like it fixes a lot of these weird cases
        Parsing all the variants here still seems like some effort
        - date: 2022/12/2
        - time: 10:23
        - datetime: 2022/12/2-10:23 (dont love the -, but a space adds ambiguity)
        - relative: now, now-2m, now-10d
        /log between 2022/12 now
        /log between now-12m now-2m
        /log between 10:23 10:55

        Having a parser for relative times, eg now-10m, 10m, etc
        will be helpful in various places. Would much prefer to use an existing one though, but havent seen one yet.
         */

        var filters = ArgumentType.Loop(
                "filters",
                ArgumentType.Group(
                        "serverGroup",
                        ArgumentType.Word("server").from("server", "on"),
                        //todo would be nice to suggest some entries? Not sure where this would come from
                        ArgumentType.Word("serverId")
                ),
                ArgumentType.Group(
                        "contextGroup",
                        ArgumentType.Word("context").from("context", "in"),
                        ArgumentType.Word("contextId")
                                //todo better way to add an optional suggestion?
                                .setSuggestionCallback((sender, context, suggestion) -> suggestion.addEntry(new SuggestionEntry("global")))
                )
        );

        // log [...]
        addSyntax(this::onLog, filters);
    }

    private void onLog(CommandSender sender, CommandContext context) {

        ChatQuery.Builder query = ChatQuery.builder();
        parseFilters(query, context.get("filters"));

        storage.queryChatMessages(query.build())
                .exceptionally(FutureUtil::handleException)
                .thenAccept(messages -> {
                    if (messages == null) {
                        // An error occurred, it was already reported internally. Inform the sender
                        sender.sendMessage(Component.translatable("command.chat.log.err_unknown"));
                        return;
                    }

                    if (messages.isEmpty()) {
                        sender.sendMessage(Component.translatable("command.chat.log.err_no_results"));
                        return;
                    }

                    for (ChatMessage message : messages) {
                        //todo better message/translation/whatever
                        sender.sendMessage(String.format("%s: %s", message.sender(), message.message()));
                    }
                })
                .exceptionally(FutureUtil::handleException);
    }

    private void parseFilters(ChatQuery.Builder query, List<CommandContext> filters) {
        for (CommandContext context : filters) {
            switch (context.getCommandName()) {
                case "server", "on" -> {
                    final String serverId = context.get("serverId");
                    query.serverId(serverId);
                }
                case "context", "in" -> {
                    final String contextId = context.get("contextId");
                    query.context(contextId);
                }
                default -> throw new RuntimeException("bad command");
            }
        }
    }


}

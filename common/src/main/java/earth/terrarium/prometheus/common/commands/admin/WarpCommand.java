package earth.terrarium.prometheus.common.commands.admin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.teamresourceful.resourcefullib.common.utils.CommonUtils;
import earth.terrarium.prometheus.common.constants.ConstantComponents;
import earth.terrarium.prometheus.common.handlers.WarpHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

public class WarpCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGGESTION_PROVIDER = (context, builder) -> {
        SharedSuggestionProvider.suggest(WarpHandler.getWarps(context.getSource().getPlayerOrException()).keySet(), builder);
        return builder.buildFuture();
    };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("warp")
            .then(add())
            .then(remove())
            .then(list())
            .then(Commands.argument("name", StringArgumentType.greedyString())
                .suggests(SUGGESTION_PROVIDER)
                .executes(context -> {
                    WarpHandler.teleport(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "name"));
                    return 1;
                })
            )
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> add() {
        return Commands.literal("add")
            .requires(source -> source.hasPermission(2))
            .then(Commands.argument("name", StringArgumentType.greedyString())
                .executes(context -> {
                    WarpHandler.add(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "name"));
                    return 1;
                })

            );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> remove() {
        return Commands.literal("remove")
            .requires(source -> source.hasPermission(2))
            .then(Commands.argument("name", StringArgumentType.greedyString())
                .suggests(SUGGESTION_PROVIDER)
                .executes(context -> {
                    WarpHandler.remove(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "name"));
                    return 1;
                })
            );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> list() {
        return Commands.literal("list")
            .executes(context -> {
                context.getSource().sendSuccess(() -> ConstantComponents.WARPS_COMMAND_TITLE, false);
                WarpHandler.getWarps(context.getSource().getPlayerOrException())
                    .keySet()
                    .stream()
                    .map(WarpCommand::createListEntry)
                    .forEach(msg -> context.getSource().sendSuccess(() -> msg, false));
                return 1;
            });
    }

    private static Component createListEntry(String name) {
        return Component.literal(" - " + name).setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            CommonUtils.serverTranslatable("prometheus.locations.warp.to", name)
        )).withClickEvent(new ClickEvent(
            ClickEvent.Action.RUN_COMMAND,
            "/warp " + name
        )));
    }
}

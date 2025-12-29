package org.dopelegend.FourInARow.Listeners;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.dopelegend.FourInARow.UtilClasses.GameRequest;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CommandListener {

    public CommandListener(Plugin plugin) {
        LifecycleEventManager<Plugin> lifecycleEventManager = plugin.getLifecycleManager();

        lifecycleEventManager.registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(fourInARowCommand());
        });
    }

    private static LiteralCommandNode<CommandSourceStack> fourInARowCommand(){
        return Commands.literal("fourInARow")
                .then(Commands.literal("deny")
                        .then(Commands.argument("playerName", StringArgumentType.word())
                                .suggests((ctx, sb) -> playerListSuggestionBuilder(sb))
                                .executes(ctx -> {
                                    // Deny logic
                                    GameRequest.DenyCommand(ctx);
                                    return 1;
                                })))
                .then(Commands.literal("accept")
                        .then(Commands.argument("playerName", StringArgumentType.word())
                                .suggests((ctx, sb) -> playerListSuggestionBuilder(sb))
                                .executes(ctx -> {
                                    // Accept logic
                                    GameRequest.AcceptCommand(ctx);
                                    return 1;
                                })))
                .then(Commands.literal("request")
                        .then(Commands.argument("playerName", StringArgumentType.word())
                                .suggests((ctx, sb) -> playerListSuggestionBuilder(sb))
                                .executes(ctx -> {
                                    GameRequest.RequestCommand(ctx);
                                    return 1;
                                }))
                ).build();

    }

    private static CompletableFuture<Suggestions> playerListSuggestionBuilder(SuggestionsBuilder sb){
        Collection<?> playerList = Bukkit.getOnlinePlayers();
        for(Object object : playerList){
            if(!(object instanceof Player player)){continue;}
            sb.suggest(player.getName());
        }
        return sb.buildFuture();
    }

}

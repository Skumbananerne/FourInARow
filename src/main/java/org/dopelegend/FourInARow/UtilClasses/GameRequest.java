package org.dopelegend.FourInARow.UtilClasses;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.dopelegend.FourInARow.Inventory.FourInARowWindow;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.TextColor.color;

public class GameRequest {

        public static Map<UUID, GameRequest> requestMap = new HashMap<UUID, GameRequest>();

        public UUID requestPlayerUuid;
        public Long requestTimeMs;

        public GameRequest(UUID requestPlayerUuid, Long requestTimeMs) {
            this.requestPlayerUuid = requestPlayerUuid;
            this.requestTimeMs = requestTimeMs;
        }

    /**
     *
     * Sends a game request from a player to another. This function also checks if sender has already invited the receiver, and if either of them is in a game.
     *
     * @param sender The player sending the request
     * @param receiver The player receiving the request
     */
    public static void SendGameRequest(Player sender, Player receiver) {

        if(GameRequest.CheckIfPlayerHasRequestedPlayer(receiver, sender)){
            sender.sendMessage(text().content("Du har allerede en aktiv invitation til denne spiller.")
                    .color(color(227, 2, 36))
                    .decorate(TextDecoration.BOLD)
                    .build());
            return;
        }

        if(FourInARowWindow.IsPlayerInAGame(sender.getUniqueId())){
            // Initiator is in a game
            return;
        }
        if(FourInARowWindow.IsPlayerInAGame(receiver.getUniqueId())){
            // Clicked player is already in a game
            sender.sendMessage(text().content("Spilleren er allerede i et spil")
                    .color(color(255, 0, 0))
                    .decorate(TextDecoration.BOLD));
            return;
        }

        // Add to request map
        GameRequest.requestMap.put(sender.getUniqueId(),
                new GameRequest(receiver.getUniqueId(), System.currentTimeMillis()));

        // Make request message
        Component acceptText = text().content("(Accept)")
                .clickEvent(ClickEvent.runCommand("/fourInARow accept "+sender.getName()))
                .color(color(0, 255, 0))
                .decorate(TextDecoration.BOLD)
                .build();

        Component denyText = text().content(" (deny)")
                .clickEvent(ClickEvent.runCommand("/fourInARow deny "+sender.getName()))
                .color(color(255, 0, 0))
                .decorate(TextDecoration.BOLD)
                .build();

        Component requestText = text()
                .content(sender.getName()+" har inviteret dig til fire på stribe: ")
                .color(color(252, 127, 3))
                .append(acceptText)
                .append(denyText)
                .decorate(TextDecoration.BOLD)
                .build();

        // Send request message
        receiver.sendMessage(requestText);
        receiver.playSound(receiver.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3f, 0.8f);

        // Send message to requesting player
        sender.sendMessage(text()
                .content("Spil anmodning send til "+receiver.getName())
                .color(color(0, 255, 0))
                .decorate(TextDecoration.BOLD)
                .build());
        sender.playSound(receiver.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3f, 0.8f);
    }

    /**
     *
     * Checks if player A has a game request from player B
     *
     * @param playerA Player A
     * @param playerB Player B
     * @return True if player A has a game request from player B, false otherwise
     */
    public static boolean CheckIfPlayerHasRequestedPlayer(Player playerA, Player playerB) {
        if (!requestMap.containsKey(playerB.getUniqueId())) return false;
        if (requestMap.get(playerB.getUniqueId()).requestPlayerUuid != playerA.getUniqueId()) return false;
        return requestMap.get(playerB.getUniqueId()).requestTimeMs+30000 > System.currentTimeMillis();
    }

    public static void AcceptCommand(CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getSender() instanceof Player accepter)){
            ctx.getSource().getSender().sendMessage(text().content("Den her command kan kun bruges af spillere"));
            return;
        }
        Player target = Bukkit.getPlayer(ctx.getArgument("playerName", String.class));
        if (target == null){
            accepter.sendMessage(text().content("Denne spiller findes ikke")
                    .color(color(255, 0, 0)).decorate(TextDecoration.BOLD));

            return;
        }
        if (!CheckIfPlayerHasRequestedPlayer(accepter, target)){
            accepter.sendMessage(text().content("Du har ikke en spil anmodning fra denne spiller")
                    .decorate(TextDecoration.BOLD)
                    .color(color(255, 0, 0)));
            return;
        }

        GameRequest.requestMap.remove(accepter.getUniqueId());
        GameRequest.requestMap.remove(target.getUniqueId());

        new FourInARowWindow(target, accepter);
    }

    public static void RequestCommand(CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getSender() instanceof Player requester)){
            ctx.getSource().getSender().sendMessage(text().content("Den her command kan kun bruges af spillere"));
            return;
        }

        Player target = Bukkit.getPlayer(ctx.getArgument("playerName", String.class));
        if (target == null){
            requester.sendMessage(text().content("Denne spiller findes ikke")
                    .decorate(TextDecoration.BOLD)
                    .color(color(255, 0, 0)));
            return;
        }

        if (CheckIfPlayerHasRequestedPlayer(requester, target)){

            GameRequest.requestMap.remove(requester.getUniqueId());
            GameRequest.requestMap.remove(target.getUniqueId());
            new FourInARowWindow(target, requester);
        }

        GameRequest.SendGameRequest(requester, target);
    }

    public static void DenyCommand(CommandContext<CommandSourceStack> ctx){
        if (!(ctx.getSource().getSender() instanceof Player denyer)){
            ctx.getSource().getSender().sendMessage(text().content("Den her command kan kun bruges af spillere"));
            return;
        }

        Player target = Bukkit.getPlayer(ctx.getArgument("playerName", String.class));
        if (target == null){
            denyer.sendMessage(text().content("Denne spiller findes ikke")
                    .decorate(TextDecoration.BOLD)
                    .color(color(255, 0, 0)));
            return;
        }

        if (!CheckIfPlayerHasRequestedPlayer(denyer, target)){
            denyer.sendMessage(text().content("Du har ikke en spil anmodning fra denne spiller")
                    .decorate(TextDecoration.BOLD)
                    .color(color(255, 0, 0)));
            return;
        }

        target.sendMessage(text().content(denyer.getName()+" har afvist din spil anmodning")
                .color(color(255, 0, 0))
                .decorate(TextDecoration.BOLD));
        target.playSound(target.getLocation(), Sound.BLOCK_ANVIL_PLACE, 3f, 0.6f);


        denyer.sendMessage(text().content("Anmodning afvist!")
                .color(color(0, 255, 0))
                .decorate(TextDecoration.BOLD));

        denyer.playSound(denyer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3f, 0.5f);


        requestMap.remove(target.getUniqueId());
    }
}

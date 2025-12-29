package org.dopelegend.FourInARow.Listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.dopelegend.FourInARow.Inventory.FourInARowWindow;
import org.dopelegend.FourInARow.Inventory.MenuHolder;
import org.dopelegend.FourInARow.UtilClasses.GameRequest;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextColor.color;

public class EventListener implements Listener {

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player clickedPlayer)) return;
        if (!event.getPlayer().isSneaking()) return;
        if (event.getHand().equals(EquipmentSlot.OFF_HAND)) return;

        // Make window if both players have requested to play within 30 seconds
        if (GameRequest.CheckIfPlayerHasRequestedPlayer(event.getPlayer(), clickedPlayer)) {
            GameRequest.requestMap.remove(clickedPlayer.getUniqueId());
            GameRequest.requestMap.remove(event.getPlayer().getUniqueId());

            new FourInARowWindow(event.getPlayer(), clickedPlayer);
            return;
        }

        // Send game request
        GameRequest.SendGameRequest(event.getPlayer(), clickedPlayer);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top.getHolder() instanceof MenuHolder holder) {
            switch (holder.getId()) {
                case "FOURINAROW" -> {
                    event.setCancelled(true);

                    FourInARowWindow game = FourInARowWindow.getPlayerGame(event.getWhoClicked().getUniqueId());
                    if (game == null) return;

                    int clickedSlot = event.getSlot();

                    int col = clickedSlot % 9;

                    if (col == 0 || col == 8) {
                        if (event.getWhoClicked() instanceof Player player) {player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 3f, 0.6f);}
                        return;}
                    int code = game.makeMove(col - 1, event.getWhoClicked().getUniqueId());
                    if (code == 5) {
                        if (event.getWhoClicked() instanceof Player player) {player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 3f, 0.6f);}
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (top.getHolder() instanceof MenuHolder holder) {
            switch (holder.getId()) {
                case "FOURINAROW" -> {
                    FourInARowWindow game = FourInARowWindow.getPlayerGame(event.getPlayer().getUniqueId());
                    if (game == null) return;

                    game.stopGame();
                }
            }
        }
    }
}
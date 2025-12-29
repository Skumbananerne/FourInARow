package org.dopelegend.FourInARow.Inventory;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.dopelegend.FourInARow.FourInARow;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.TextColor.color;

public class FourInARowWindow{

    static List<FourInARowWindow> activeGames = new ArrayList<>();

    public static List<FourInARowWindow> GetActiveGames() {
        return activeGames;
    }

    public static boolean IsPlayerInAGame(UUID player){
        for(FourInARowWindow game : activeGames){
            if(game.p1 == player || game.p2 == player) return true;
        }
        return false;
    }
    public static FourInARowWindow getPlayerGame(UUID player){
        for(FourInARowWindow game : activeGames){
            if(game.p1 == player || game.p2 == player) return game;
        }
        return null;
    }

    //Map size
    //Width: 7
    //Height: 6
    int[][] map = new int[6][7];
    int[][] conencted = new int[6][7];

    UUID p1;
    UUID p2;

    UUID winner = null;

    Inventory p1Inventory;
    Inventory p2Inventory;

    ItemStack p1Piece;
    ItemStack p2Piece;

    int nextPlayer;
    boolean isTie;
    boolean finished;

    boolean closed;

    public FourInARowWindow(Player player1, Player player2) {
        p1 = player1.getUniqueId();
        p2 = player2.getUniqueId();

        activeGames.add(this);

        p1Piece = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        ItemMeta p1Meta = p1Piece.getItemMeta();

        p1Meta.displayName(Component.text(player1.getName() + "'s Brik")
                .color(NamedTextColor.BLUE)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        p1Piece.setItemMeta(p1Meta);


        p2Piece = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta p2Meta = p2Piece.getItemMeta();

        p2Meta.displayName(Component.text(player2.getName() + "'s Brik")
                .color(NamedTextColor.YELLOW)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        p2Piece.setItemMeta(p2Meta);


        MenuHolder p1Holder = new MenuHolder("FOURINAROW", 54, Component.text("Fire på stribe med " + player2.getName()));
        MenuHolder p2Holder = new MenuHolder("FOURINAROW", 54, Component.text("Fire på stribe med " + player1.getName()));

        p1Inventory = toggleTurnInventory(p1Holder.getInventory(), true);
        p2Inventory = toggleTurnInventory(p2Holder.getInventory(), false);

        nextPlayer = 1;
        player1.openInventory(p1Inventory);
        player2.openInventory(p2Inventory);
        player1.playSound(player1.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_0, 3f, 1f);
        player2.playSound(player1.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_0, 3f, 1f);
    }

    public Inventory toggleTurnInventory(Inventory inventory, boolean turn){
        ItemStack itemStack = null;

        if(turn){
            itemStack = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
            ItemMeta meta = itemStack.getItemMeta();

            meta.displayName(Component.text("Din tur!")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(List.of(
                    Component.text("Tryk på en plads for at bruge din tur!.")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));

            itemStack.setItemMeta(meta);
        } else {
            itemStack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
            ItemMeta meta = itemStack.getItemMeta();

            meta.displayName(Component.text("Vent på din tur!")
                    .color(NamedTextColor.RED)
                    .decorate(TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));

            meta.lore(List.of(
                    Component.text("Vent på at din modstander bruger sin tur!")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, false)
            ));

            itemStack.setItemMeta(meta);
        }
        for(int i = 0; i < 6; i++){
            inventory.setItem(i * 9, itemStack);
            inventory.setItem((i * 9) + 8, itemStack);
        }

        return inventory;
    }

    // code
    // 0 = continue
    // 1 = player 1 won
    // 2 = player 2 won
    // 3 = game is tie
    // 4 = space already in use
    // 5 = wrong player
    // 6 = already finished
    // -1 = internal error
    public int makeMove(int col, UUID player) {
        if(finished) return 6;
        Player currentPlayer;
        if (nextPlayer == 1) {
            // Check if its the correct player
            if (p1 != player) return 5;
            currentPlayer = Bukkit.getPlayer(p1);
        } else if (nextPlayer == 2) {
            // Check if its the correct player
            if (p2 != player) return 5;
            currentPlayer = Bukkit.getPlayer(p2);
        } else {
            // Idk how it got here
            // A player who is not in the game tried to make a move
            FourInARow.plugin.getLogger().warning("We fucked something up. We should not be able to reach this code in any way");
            return -1;
        }

        int row = -1;

        for (int i = 0; i < map.length; i++){
            if(map[i][col] != 0) continue;
            row = i;
        }

        if (row == -1) {
            // Space already in use
            currentPlayer.playSound(currentPlayer.getLocation(), Sound.BLOCK_ANVIL_PLACE, 3f, 0.6f);
            return 4;
        }

        currentPlayer.playSound(currentPlayer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3f, 0.5f);


        map[row][col] = nextPlayer;

        int code = checkMove(row, col);

        //TODO spawn animation
        int index = (row * 9) + (col + 1);

        if (nextPlayer == 1) {
            nextPlayer = 2;
            p1Inventory = toggleTurnInventory(p1Inventory, false);
            p2Inventory = toggleTurnInventory(p2Inventory, true);

            p1Inventory.setItem(index, p1Piece);
            p2Inventory.setItem(index, p1Piece);
        } else {
            nextPlayer = 1;
            p1Inventory = toggleTurnInventory(p1Inventory, true);
            p2Inventory = toggleTurnInventory(p2Inventory, false);

            p1Inventory.setItem(index, p2Piece);
            p2Inventory.setItem(index, p2Piece);
        }



        if(code == 1 || code == 2){
            endAnimation();
        }
        if(code == 3){
            isTie = true;
            stopGame();
        }
        return code;
    }


    /**
     * Check the outcome of the move that has just been made.
     * return:
     *  0 = Game still in progress
     *  1 = player 1 has won
     *  2 = player 2 has wom
     *  3 = Game is a tie
     */
    int checkMove(int row, int col){
        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {

                if (i < 0 || i >= map.length || j < 0 || j >= map[0].length) {
                    continue;
                }

                if(map[i][j] == 0) {
                    continue;
                }

                if (i == row && j == col) {
                    continue;
                }

                // Check if found tiles owner is current player
                if(map[i][j] == nextPlayer) {
                    conencted[i][j] = nextPlayer;
                    int difRow = i - row;
                    int difCol = j - col;

                    int totalLineFound = 1;

                    int nextRow = i;
                    int nextCol = j;
                    for(int k = 0; k < 4; k++){
                        if(k == 2) {
                            nextRow = i;
                            nextCol = j;
                        }
                        if(difRow != 0){
                            if(k < 2) nextRow += difRow;
                            else nextRow -= difRow;
                        }
                        if(difCol != 0){
                            if(k < 2) nextCol += difCol;
                            else nextCol -= difCol;
                        }

                        if (nextRow < 0 || nextRow >= map.length || nextCol < 0 || nextCol >= map[0].length) {
                            continue;
                        }
                        if(map[nextRow][nextCol] == nextPlayer) {
                            totalLineFound++;
                            conencted[nextRow][nextCol] = nextPlayer;
                        }else {
                            if(k < 2) {
                                k = 1;
                                continue;
                            } else {
                                k = 3;
                                continue;
                            }
                        }
                    }

                    if(totalLineFound >= 4){
                        if(nextPlayer == 1){
                            winner = p1;
                        } else{
                            winner = p2;
                        }
                        return nextPlayer;
                    }
                    conencted = new int[6][7];
                }
            }
        }

        int emptySpotCount = 0;
        for(int i = 0; i < map.length; i++){
            for (int j = 0; j < map[0].length; j++){
                if(map[i][j] == 0) {
                    emptySpotCount++;
                    continue;
                }
            }
            if(emptySpotCount > 0) continue;
        }
        if(emptySpotCount == 0){
            return 3;
        }
        return 0;
    }

    public void stopGame(){
        if(closed) return;
        closed = true;
        Player player1 = Bukkit.getPlayer(p1);
        Player player2 = Bukkit.getPlayer(p2);

        if(winner == null){
            if(isTie){
                player1.sendMessage(text().content("Spillet blev uafgjort!")
                        .color(color(255, 0, 0))
                        .decorate(TextDecoration.BOLD));
                player1.playSound(player1.getLocation(), Sound.ENTITY_PILLAGER_AMBIENT, 3.0f, 1.0f);

                player2.sendMessage(text().content("Spillet blev uafgjort!")
                        .color(color(255, 0, 0))
                        .decorate(TextDecoration.BOLD));
                player2.playSound(player1.getLocation(), Sound.ENTITY_PILLAGER_AMBIENT, 3.0f, 1.0f);
            } else {
                player1.sendMessage(text().content("Spillet blev aflyst!")
                        .color(color(255, 0, 0))
                        .decorate(TextDecoration.BOLD));
                player1.playSound(player1.getLocation(), Sound.ENTITY_PILLAGER_AMBIENT, 3.0f, 1.0f);

                player2.sendMessage(text().content("Spillet blev aflyst!")
                        .color(color(255, 0, 0))
                        .decorate(TextDecoration.BOLD));
                player2.playSound(player1.getLocation(), Sound.ENTITY_PILLAGER_AMBIENT, 3.0f, 1.0f);
            }
        } else {
            Player playerWinner = Bukkit.getPlayer(winner);
            Player playerLoser = Bukkit.getPlayer(winner == p1 ? p2 : p1);

            playerWinner.sendMessage(text().content("Du vandt spillet!")
                    .color(color(0, 255, 0))
                    .decorate(TextDecoration.BOLD));
            playerWinner.playSound(playerWinner.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 3.0f, 0.5f);

            playerLoser.sendMessage(text().content("Du tabte spillet!")
                    .color(color(255, 0, 0))
                    .decorate(TextDecoration.BOLD));
            playerLoser.playSound(playerLoser.getLocation(), Sound.BLOCK_ANVIL_PLACE, 3.0f, 0.6f);
        }

        activeGames.remove(this);

        player1.closeInventory();
        player2.closeInventory();
    }

    void endAnimation(){
        finished = true;
        final int[] winner = {this.winner == p1 ? 1 : 2};

        int[][] finalConencted = conencted;
        new BukkitRunnable(){
            int runCount = 0;
            @Override
            public void run(){
                if(runCount >= 3){
                    stopGame();
                    this.cancel();
                    return;
                }

                if(runCount % 2 == 0){
                    for(int k = 0; k < finalConencted.length; k++){
                        for (int l = 0; l < finalConencted[0].length; l++) {
                            if(finalConencted[k][l] == winner[0]) {
                                int index = (k * 9) + (l + 1);

                                ItemStack concrete;

                                if (winner[0] == 1){
                                    // Blå
                                    concrete = new ItemStack(Material.BLUE_CONCRETE);
                                    ItemMeta concreteMeta = concrete.getItemMeta();
                                    concreteMeta.displayName(Component.text(Bukkit.getPlayer(p1).getName() + "'s Brik")
                                            .color(NamedTextColor.BLUE)
                                            .decorate(TextDecoration.BOLD)
                                            .decoration(TextDecoration.ITALIC, false));
                                    concrete.setItemMeta(concreteMeta);
                                }
                                else {
                                    concrete = new ItemStack(Material.YELLOW_CONCRETE);
                                    ItemMeta concreteMeta = concrete.getItemMeta();
                                    concreteMeta.displayName(Component.text(Bukkit.getPlayer(p2).getName() + "'s Brik")
                                            .color(NamedTextColor.BLUE)
                                            .decorate(TextDecoration.BOLD)
                                            .decoration(TextDecoration.ITALIC, false));
                                    concrete.setItemMeta(concreteMeta);
                                }

                                p1Inventory.setItem(index, concrete);
                                p2Inventory.setItem(index, concrete);
                            }
                        }
                    }
                } else {
                    for(int k = 0; k < finalConencted.length; k++){
                        for (int l = 0; l < finalConencted[0].length; l++) {
                            if(finalConencted[k][l] == winner[0]) {
                                int index = (k * 9) + (l + 1);
                                if (winner[0] == 1){
                                    p1Inventory.setItem(index, p1Piece);
                                    p2Inventory.setItem(index, p1Piece);
                                }else {
                                    p1Inventory.setItem(index, p2Piece);
                                    p2Inventory.setItem(index, p2Piece);
                                }
                            }
                        }
                    }
                }
                runCount++;
            }

        }.runTaskTimer(FourInARow.plugin, 20, 20);
    }
}
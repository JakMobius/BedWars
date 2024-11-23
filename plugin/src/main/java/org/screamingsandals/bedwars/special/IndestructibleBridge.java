/*
 * Copyright (C) 2023 ScreamingSandals
 *
 * This file is part of Screaming BedWars.
 *
 * Screaming BedWars is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Screaming BedWars is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Screaming BedWars. If not, see <https://www.gnu.org/licenses/>.
 */

package org.screamingsandals.bedwars.special;

import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.Team;
import org.screamingsandals.bedwars.game.TeamColor;
import org.screamingsandals.bedwars.utils.MiscUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.screamingsandals.bedwars.lib.lang.I.i18nc;
import static org.screamingsandals.bedwars.lib.lang.I.i18nonly;
import static org.screamingsandals.bedwars.lib.lang.I18n.i18n;
import static org.screamingsandals.bedwars.lib.lang.I18n.i18nonly;

public class IndestructibleBridge extends SpecialItem implements org.screamingsandals.bedwars.api.special.IndestructibleBridge {
    private Game game;
    private Player player;
    private Team team;

    private int breakingTime;
    private int livingTime;
    private int width;
    private int length;
    private int distance;
    private boolean canBreak;

    private ItemStack item;
    private Material buildingMaterial;
    private List<Block> wallBlocks;

    public IndestructibleBridge(Game game, Player player, Team team, ItemStack item) {
        super(game, player, team);
        this.game = game;
        this.player = player;
        this.team = team;
        this.item = item;
    }

    @Override
    public int getBreakingTime() {
        return breakingTime;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public int getDistance() {
        return distance;
    }

    @Override
    public boolean canBreak() {
        return canBreak;
    }

    @Override
    public Material getMaterial() {
        return buildingMaterial;
    }

    @Override
    public void runTask() {
        new BukkitRunnable() {

            @Override
            public void run() {
                livingTime++;
                int time = breakingTime - livingTime;

                if (!Main.getConfigurator().config.getBoolean("specials.dont-show-success-messages")) {
                    if (time < 6 && time > 0) {
                        MiscUtils.sendActionBarMessage(
                                player, i18nonly("specials_bridge_wall_destroy").replace("%time%", Integer.toString(time)));
                    }
                }

                if (livingTime == breakingTime) {
                    for (Block block : wallBlocks) {
                        block.getChunk().load(false);
                        block.setType(Material.AIR);

                        game.getRegion().removeBlockBuiltDuringGame(block.getLocation());
                    }
                    game.unregisterSpecialItem(IndestructibleBridge.this);
                    this.cancel();
                }
            }
        }.runTaskTimer(Main.getInstance(), 20L, 20L);
    }

    @Override
    public List<Block> getWallBlocks() {
        return wallBlocks;
    }

    private void addBlockToList(Block block) {
        wallBlocks.add(block);
        game.getRegion().addBuiltDuringGame(block.getLocation());
    }

    public void createWall(boolean bre, int time, int wid, int len, int dis, Material mat, short legacyData) {
        canBreak = bre;
        breakingTime = time;
        width = wid;
        length = len;
        distance = dis;
        buildingMaterial = mat;
        wallBlocks = new ArrayList<>();

        if (width % 2 == 0) {
            player.sendMessage(i18nc("The width of a the bridge has to be odd! " + width + " is not an odd number.", game.getCustomPrefix()));
            width = width + 1;
            if (width % 2 == 0) {
                return;
            }
        }

        Location wallLocation = player.getLocation();
        wallLocation.add(wallLocation.getDirection().setY(0).normalize().multiply(distance));

        BlockFace face = MiscUtils.getCardinalDirection(player.getLocation());
        int widthStart = (int) Math.floor(((double) width) / 2.0);

        if (face == BlockFace.SELF) {
           face = BlockFace.NORTH;
        }

        for (int w = widthStart * (-1); w < width - widthStart; w++) {
            for (int l = 0; l < length; l++) {
                for(int z = 0; z < 2; z++) {
                    Location wallBlock = wallLocation.clone();
                    switch (face) {
                        case SOUTH:
                            // Logger.getGlobal().info("SOUTH");
                            if (z == 1) wallBlock = null;
                            else wallBlock.add(l, 0, w);
                            break;
                        case NORTH:
                            // Logger.getGlobal().info("NORTH");
                            if (z == 1) wallBlock = null;
                            else wallBlock.add(-l, 0, w);
                            break;
                        case WEST:
                            // Logger.getGlobal().info("WEST");
                            if (z == 1) wallBlock = null;
                            else wallBlock.add(w, 0, l);
                            break;
                        case EAST:
                            // Logger.getGlobal().info("EAST");
                            if (z == 1) wallBlock = null;
                            else wallBlock.add(w, 0, -l);
                            break;
                        case SOUTH_WEST:
                            // Logger.getGlobal().info("SOUTH_WEST");
                            if (z == 1) wallBlock.add(l + w, 0, l - w);
                            else if (l != length - 1 && w < width - widthStart - 1) {
                                wallBlock.add(l + w + 1, 0, l - w);
                            } else wallBlock = null;
                            break;
                        case SOUTH_EAST:
                            // Logger.getGlobal().info("SOUTH_EAST");
                            if (z == 1) wallBlock.add(l + w, 0, -l + w);
                            else if (l != length - 1 && w < width - widthStart - 1) {
                                wallBlock.add(l + w + 1, 0, -l + w);
                            } else wallBlock = null;
                            break;
                        case NORTH_WEST:
                            // Logger.getGlobal().info("NORTH_WEST");
                            if (z == 1) wallBlock.add(-l + w, 0, l + w);
                            else if (l != 0 && w < width - widthStart - 1) {
                                wallBlock.add(-l + w + 1, 0, l + w);
                            } else wallBlock = null;
                            break;
                        case NORTH_EAST:
                            // Logger.getGlobal().info("NORTH_EAST");
                            if (z == 1) wallBlock.add(-l + w, 0, -l - w);
                            else if (l != 0 && w < width - widthStart - 1) {
                                wallBlock.add(-l + w + 1, 0, -l - w);
                            } else wallBlock = null;
                            break;
                        default:
                            wallBlock = null;
                            break;
                    }

                    if (wallBlock == null) {
                        continue;
                    }

                    Block placedBlock = wallBlock.getBlock();
                    if (!placedBlock.getType().equals(Material.AIR)) {
                        continue;
                    }

                    ItemStack coloredStack = Main.applyColor(
                            TeamColor.fromApiColor(team.getColor()), new ItemStack(buildingMaterial, 1, legacyData));
                    if (Main.isLegacy()) {
                        placedBlock.setType(coloredStack.getType());
                        try {
                            // The method is no longer in API, but in legacy versions exists
                            Block.class.getMethod("setData", byte.class).invoke(placedBlock, (byte) coloredStack.getDurability());
                        } catch (Exception e) {
                        }
                    } else {
                        placedBlock.setType(coloredStack.getType());
                    }
                    addBlockToList(placedBlock);
                }
            }
        }

        if (breakingTime > 0) {
            game.registerSpecialItem(this);
            runTask();

            if (!Main.getConfigurator().config.getBoolean("specials.dont-show-success-messages")) {
                MiscUtils.sendActionBarMessage(player, i18nonly("specials_bridge_created").replace("%time%", Integer.toString(breakingTime)));
            }

            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                try {
                    if (player.getInventory().getItemInOffHand().equals(item)) {
                        player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                    } else {
                        player.getInventory().remove(item);
                    }
                } catch (Throwable e) {
                    player.getInventory().remove(item);
                }
            }
            player.updateInventory();
        } else {
            game.registerSpecialItem(this);

            if (!Main.getConfigurator().config.getBoolean("specials.dont-show-success-messages")) {
                MiscUtils.sendActionBarMessage(player, i18nonly("specials_bridge_created_unbreakable"));
            }

            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                try {
                    if (player.getInventory().getItemInOffHand().equals(item)) {
                        player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
                    } else {
                        player.getInventory().remove(item);
                    }
                } catch (Throwable e) {
                    player.getInventory().remove(item);
                }
            }
            player.updateInventory();
        }
    }
}

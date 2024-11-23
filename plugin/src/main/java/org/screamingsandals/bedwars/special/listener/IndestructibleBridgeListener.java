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

package org.screamingsandals.bedwars.special.listener;


import org.screamingsandals.bedwars.Main;
import org.screamingsandals.bedwars.api.APIUtils;
import org.screamingsandals.bedwars.api.events.BedwarsPlayerBreakBlock;
import org.screamingsandals.bedwars.api.game.Game;
import org.screamingsandals.bedwars.api.game.GameStatus;
import org.screamingsandals.bedwars.api.events.BedwarsApplyPropertyToBoughtItem;
import org.screamingsandals.bedwars.api.special.SpecialItem;
import org.screamingsandals.bedwars.game.GamePlayer;
import org.screamingsandals.bedwars.special.IndestructibleBridge;
import org.screamingsandals.bedwars.utils.DelayFactory;
import org.screamingsandals.bedwars.utils.MiscUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.screamingsandals.simpleinventories.utils.MaterialSearchEngine;

import java.util.ArrayList;

import static org.screamingsandals.bedwars.lib.lang.I18n.i18nonly;

public class IndestructibleBridgeListener implements Listener {
    public static final String PROTECTION_WALL_PREFIX = "Module:IndestructibleBridge:";

    @EventHandler
    public void onIndestructibleBridgeRegistered(BedwarsApplyPropertyToBoughtItem event) {
        if (event.getPropertyName().equalsIgnoreCase("IndestructibleBridge")) {
            ItemStack stack = event.getStack();
            APIUtils.hashIntoInvisibleString(stack, applyProperty(event));
        }

    }

    @EventHandler
    public void onPlayerUseItem(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!Main.isPlayerInGame(player)) {
            return;
        }

        GamePlayer gPlayer = Main.getPlayerGameProfile(player);
        Game game = gPlayer.getGame();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (game.getStatus() == GameStatus.RUNNING && !gPlayer.isSpectator && event.getItem() != null) {
                ItemStack stack = event.getItem();
                String unhidden = APIUtils.unhashFromInvisibleStringStartsWith(stack, PROTECTION_WALL_PREFIX);

                if (unhidden != null) {
                    if (!game.isDelayActive(player, IndestructibleBridge.class)) {
                        event.setCancelled(true);

                        boolean isBreakable = Boolean.parseBoolean(unhidden.split(":")[2]);
                        int delay = Integer.parseInt(unhidden.split(":")[3]);
                        int breakTime = Integer.parseInt(unhidden.split(":")[4]);
                        int width = Integer.parseInt(unhidden.split(":")[5]);
                        int height = Integer.parseInt(unhidden.split(":")[6]);
                        int distance = Integer.parseInt(unhidden.split(":")[7]);
                        MaterialSearchEngine.Result result = MiscUtils.getMaterialFromString(unhidden.split(":")[8], "CUT_SANDSTONE");
                        Material material = result.getMaterial();
                        short damage = result.getDamage();


                        IndestructibleBridge IndestructibleBridge = new IndestructibleBridge(game, event.getPlayer(),
                                game.getTeamOfPlayer(event.getPlayer()), stack);

                        if (event.getPlayer().getEyeLocation().getBlock().getType() != Material.AIR) {
                            MiscUtils.sendActionBarMessage(event.getPlayer(), i18nonly("specials_bridge_not_usable_here"));
                            return;
                        }

                        if (delay > 0) {
                            DelayFactory delayFactory = new DelayFactory(delay, IndestructibleBridge, player, game);
                            game.registerDelay(delayFactory);
                        }

                        IndestructibleBridge.createWall(isBreakable, breakTime, width, height, distance, material, damage);
                    } else {
                        event.setCancelled(true);

                        int delay = game.getActiveDelay(player, IndestructibleBridge.class).getRemainDelay();
                        MiscUtils.sendActionBarMessage(player, i18nonly("special_item_delay").replace("%time%", String.valueOf(delay)));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BedwarsPlayerBreakBlock event) {
        final Game game = event.getGame();
        final Block block = event.getBlock();

        for (IndestructibleBridge checkedWall : getCreatedWalls(game)) {
            if (checkedWall != null) {
                for (Block wallBlock : checkedWall.getWallBlocks()) {
                    if (wallBlock.equals(block) && !checkedWall.canBreak()) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    private ArrayList<IndestructibleBridge> getCreatedWalls(Game game) {
        ArrayList<IndestructibleBridge> createdWalls = new ArrayList<>();
        for (SpecialItem specialItem : game.getActivedSpecialItems(IndestructibleBridge.class)) {
            if (specialItem instanceof IndestructibleBridge) {
                IndestructibleBridge wall = (IndestructibleBridge) specialItem;
                createdWalls.add(wall);
            }
        }
        return createdWalls;
    }

    private String applyProperty(BedwarsApplyPropertyToBoughtItem event) {
        return PROTECTION_WALL_PREFIX
                + MiscUtils.getBooleanFromProperty(
                "is-breakable", "specials.indestructible-bridge.is-breakable", event) + ":"
                + MiscUtils.getIntFromProperty(
                "delay", "specials.indestructible-bridge.delay", event) + ":"
                + MiscUtils.getIntFromProperty(
                "break-time", "specials.indestructible-bridge.break-time", event) + ":"
                + MiscUtils.getIntFromProperty(
                "width", "specials.indestructible-bridge.width", event) + ":"
                + MiscUtils.getIntFromProperty(
                "length", "specials.indestructible-bridge.length", event) + ":"
                + MiscUtils.getIntFromProperty(
                "distance", "specials.indestructible-bridge.distance", event) + ":"
                + MiscUtils.getMaterialFromProperty(
                "material", "specials.indestructible-bridge.material", event);
    }

}

/*
 * Copyright (C) 2022 ScreamingSandals
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

package org.screamingsandals.bedwars.commands.admin;

import cloud.commandframework.Command;
import cloud.commandframework.CommandManager;
import cloud.commandframework.context.CommandContext;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.screamingsandals.bedwars.commands.AdminCommand;
import org.screamingsandals.bedwars.commands.CommandService;
import org.screamingsandals.bedwars.game.GameImpl;
import org.screamingsandals.bedwars.game.GameManagerImpl;
import org.screamingsandals.bedwars.lang.LangKeys;
import org.screamingsandals.lib.lang.Message;
import org.screamingsandals.lib.sender.CommandSenderWrapper;
import org.screamingsandals.lib.utils.TriFunction;
import org.screamingsandals.lib.utils.annotations.ServiceDependencies;
import org.screamingsandals.lib.utils.annotations.methods.OnPostEnable;
import org.screamingsandals.lib.utils.annotations.parameters.ProvidedBy;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@ServiceDependencies(dependsOn = {
        AdminCommand.class
})
@RequiredArgsConstructor
public abstract class BaseAdminSubCommand {

    private final String name;

    @OnPostEnable
    public void onPostEnable(@ProvidedBy(CommandService.class) CommandManager<CommandSenderWrapper> manager, @ProvidedBy(AdminCommand.class) Command.Builder<CommandSenderWrapper> builder) {
        construct(manager, builder.literal(name));
    }

    public abstract void construct(CommandManager<CommandSenderWrapper> manager, Command.Builder<CommandSenderWrapper> commandSenderWrapperBuilder);

    protected void editMode(CommandContext<CommandSenderWrapper> commandContext, BiConsumer<CommandSenderWrapper, GameImpl> handler) {
        var sender = commandContext.getSender();
        var game = editMode(commandContext);

        if (game != null) {
            handler.accept(sender, game);
        } else {
            sender.sendMessage(Message.of(LangKeys.ADMIN_ARENA_ERROR_ARENA_NOT_IN_EDIT).defaultPrefix());
        }
    }

    protected void viewMode(CommandContext<CommandSenderWrapper> commandContext, BiConsumer<CommandSenderWrapper, GameImpl> handler) {
        var sender = commandContext.getSender();
        var game = viewMode(commandContext);

        if (game != null) {
            handler.accept(sender, game);
        } else {
            sender.sendMessage(Message.of(LangKeys.ADMIN_ARENA_ERROR_ARENA_NOT_IN_EDIT).defaultPrefix());
        }
    }

    protected BiFunction<CommandContext<CommandSenderWrapper>, String, List<String>> editModeSuggestion(TriFunction<CommandContext<CommandSenderWrapper>, CommandSenderWrapper, GameImpl, List<String>> handler) {
        return (commandContext, s) -> {
            var game = editMode(commandContext);
            var sender = commandContext.getSender();

            if (game != null) {
                return handler.apply(commandContext, sender, game);
            }
            return List.of();
        };
    }

    protected BiFunction<CommandContext<CommandSenderWrapper>, String, List<String>> viewModeSuggestion(TriFunction<CommandContext<CommandSenderWrapper>, CommandSenderWrapper, GameImpl, List<String>> handler) {
        return (commandContext, s) -> {
            var game = viewMode(commandContext);
            var sender = commandContext.getSender();

            if (game != null) {
                return handler.apply(commandContext, sender, game);
            }
            return List.of();
        };
    }

    @Nullable
    protected GameImpl editMode(CommandContext<CommandSenderWrapper> commandContext) {
        String gameName = commandContext.get("game");

        if (AdminCommand.gc.containsKey(gameName)) {
            return AdminCommand.gc.get(gameName);
        }
        return null;
    }

    @Nullable
    protected GameImpl viewMode(CommandContext<CommandSenderWrapper> commandContext) {
        String gameName = commandContext.get("game");

        if (AdminCommand.gc.containsKey(gameName)) {
            return AdminCommand.gc.get(gameName);
        } else if (GameManagerImpl.getInstance().hasGame(gameName)) {
            return GameManagerImpl.getInstance().getGame(gameName).orElseThrow();
        }
        return null;
    }
}

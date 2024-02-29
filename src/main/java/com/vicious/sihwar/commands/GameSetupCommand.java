package com.vicious.sihwar.commands;

import com.vicious.sihwar.SIHWar;
import com.vicious.sihwar.data.GameInstance;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * A command only runnable during the setup stage.
 */
public abstract class GameSetupCommand extends GameCommand {
    protected GameSetupCommand(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull List<String> aliases) {
        super(name, description, usageMessage, aliases);
    }

    public GameSetupCommand(@NotNull String name, @NotNull String description, @NotNull String usageMessage, String... aliases) {
        this(name,description,usageMessage,List.of(aliases));
    }

    @Override
    protected void whenGameLoaded(CommandSender sender, Consumer<GameInstance> cons) {
        if(SIHWar.hasGame()){
            if(SIHWar.game.inSetup()) {
                cons.accept(SIHWar.game);
            }
            else{
                error(sender, "You cannot run this command after the game has started.");
            }
        }
        else{
            error(sender,"You cannot run this command right now.");
        }
    }
}

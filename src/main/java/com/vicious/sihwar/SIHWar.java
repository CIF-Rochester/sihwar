package com.vicious.sihwar;

import com.vicious.sihwar.commands.sub.WarAdminCommand;
import com.vicious.sihwar.commands.sub.WarInfoCommand;
import com.vicious.sihwar.commands.sub.WarTeamCommand;
import com.vicious.sihwar.data.GameInstance;
import com.vicious.sihwar.data.GameStages;
import com.vicious.sihwar.data.Templates;
import com.vicious.sihwar.game.FeatureDisabler;
import com.vicious.sihwar.player.PlayerEventListener;
import com.vicious.viciouslib.aunotamation.Aunotamation;
import com.vicious.viciouslib.persistence.vson.SerializationHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Logger;

public final class SIHWar extends JavaPlugin {
    public static SIHWar INSTANCE;
    public static GameInstance game;
    public static Logger logger;

    public SIHWar(){
        INSTANCE=this;
        logger=getLogger();
    }

    /**
     * There isn't always a game loaded, this is a safety method.
     */
    public static void whenHasGame(Consumer<GameInstance> consumer) {
        if(hasGame()){
            consumer.accept(game);
        }
    }

    public static boolean hasGame() {
        return game != null;
    }

    @Override
    public void onEnable() {
        //Initialize Aunotamation Library for Serialization Helping.
        Aunotamation.init();
        //Add UUID to the SerializationHandler.
        SerializationHandler.registerHandler(UUID.class,UUID::fromString,UUID::toString);
        WarConfig.init();
        GameStages.init();
        Templates.reload();
        //Schedule the game ticker.
        Bukkit.getScheduler().scheduleSyncRepeatingTask(SIHWar.INSTANCE, ()->{
            whenHasGame(GameInstance::tickSecond);
        },0,20L);
        //Schedule the save ticker.
        Bukkit.getScheduler().scheduleSyncRepeatingTask(SIHWar.INSTANCE, ()-> {
            whenHasGame(GameInstance::save);
        },WarConfig.saveIntervalSeconds*20L,WarConfig.saveIntervalSeconds*20L);
        Bukkit.getPluginManager().registerEvents(new PlayerEventListener(),this);
        Bukkit.getPluginManager().registerEvents(new FeatureDisabler(),this);
        CommandMap map = Bukkit.getCommandMap();
        map.register("waradmin","sihwar:waradmin",new WarAdminCommand());
        map.register("warteam","sihwar:warteam",new WarTeamCommand());
        map.register("warinfo","sihwar:warinfo",new WarInfoCommand());
    }

    @Override
    public void onDisable() {
        whenHasGame(GameInstance::save);
    }
}

package com.vicious.sihwar.player;

import com.vicious.sihwar.SIHWar;
import com.vicious.sihwar.data.GameInstance;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

/**
 * Represents the player state.
 */
public enum PlayerState {
    //During setup period only.
    WAITING{
        @Override
        public void apply(Player p, PlayerData data, GameInstance instance) {
            if(data != null) {
                data.state = WAITING;
            }
            p.setGameMode(GameMode.ADVENTURE);
            if(SIHWar.game != null) {
                p.teleport(SIHWar.game.world.getSpawnLocation());
            }
            else{
                p.teleport(p.getWorld().getSpawnLocation());
            }
            p.getInventory().clear();
        }
    },
    ALIVE{
        @Override
        public void apply(Player p, PlayerData data, GameInstance instance) {
            data.state=ALIVE;
            p.setGameMode(GameMode.SURVIVAL);
        }
    },
    DEAD{
        @Override
        public void apply(Player p, PlayerData data, GameInstance instance) {
            p.setGameMode(GameMode.SPECTATOR);
            data.spectate(instance);
        }
    };

    public void apply(Player p, PlayerData data, GameInstance instance){

    }
}

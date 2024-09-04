package com.vicious.sihwar.player;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.vicious.sihwar.SIHWar;
import com.vicious.sihwar.TeamData;
import com.vicious.sihwar.WarConfig;
import com.vicious.sihwar.data.GameFlag;
import com.vicious.sihwar.data.GameInstance;
import com.vicious.sihwar.game.Overlays;
import com.vicious.sihwar.util.QuadConsumer;
import com.vicious.viciouslib.util.interfaces.TriConsumer;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.function.Consumer;

public class PlayerEventListener implements Listener {
    /**
     * Cancels movement if the player is frozen, the game is paused, or there is supposed to be a game but it isn't running yet (may occur if a crash happens).
     */
    @EventHandler
    public void onMove(PlayerMoveEvent pme) {
        if(pme.getPlayer().hasPermission("sihwar.bypass")){
            return;
        }
        whenParticipating(pme.getPlayer(),(game,player,data)->{
            if (data.frozen || game.paused) {
                pme.setCancelled(true);
            }
        });
        if(!SIHWar.hasGame() && WarConfig.running){
            pme.setCancelled(true);
        }
    }

    /**
     * Cancels all damage when waiting or frozen.
     */
    @EventHandler
    public void onDamage(EntityDamageEvent event){
        if(event.getEntity() instanceof Player) {
            whenParticipating((Player)event.getEntity(),((game, player, data) -> {
                if(data.frozen){
                    event.setCancelled(true);
                }
            }));
        }
    }

    /**
     * When the player has respawned dead, spectates a random living teammate or random player.
     */
    @EventHandler
    public void postRespawn(PlayerPostRespawnEvent ppre){
        whenParticipating(ppre.getPlayer(),(game,player,data)->{
            data.applyState(game,player);
        });
        whenGameLoaded(game->{
            if(game.inSetup()){
                PlayerState.WAITING.apply(ppre.getPlayer(),null, game);
            }
        });
    }

    /**
     * If the player is not dead, respawns them at team spawn or at their bed spawn. If they are dead, respawns them at their dead point.
     */
    @EventHandler
    public void onRespawn(PlayerRespawnEvent pre) {
        whenParticipating(pre.getPlayer(),(game,player,data)->{
            if (!data.isDead()) {
                if(!pre.isBedSpawn()){
                    pre.setRespawnLocation(data.getTeam(game).getSpawnPoint(game));
                }
            }
            else{
                pre.setRespawnLocation(player.getLocation());
            }
        });
    }

    /**
     * If grace is over, sets the player to dead and checks if the team is dead.
     * If all but one team is dead, ends the game.
     */
    @EventHandler
    public void onDeath(PlayerDeathEvent pde) {
        whenParticipating(pde.getPlayer(),(game,player,data)->{
            if(!game.inGrace()){
                data.state = PlayerState.DEAD;
                data.getTeam(game).checkDead(game);
                game.save();
            }
            game.checkEnded();
        });
    }

    /**
     * When a player joins while participating:
     * - If waiting and the game is in the grace stage or the starting stage, performs initial spawns.
     * - If waiting and the game is out of the grace stage sets the player to dead and spectates.
     * When not participating adds the player to the spectator team and spectates a random player.
     */
    @EventHandler
    public void onLogin(PlayerJoinEvent pje){
        whenGameLoaded(game->{
            PlayerData data = game.playerData.get(pje.getPlayer());
            Player player = pje.getPlayer();
            if(data.isWaiting()) {
                if (game.inSetup()) {
                    PlayerState.WAITING.apply(player, data, game);
                } else if (data.isParticipating() && game.getStage().flags.contains(GameFlag.GRACE)) {
                    data.spawnInitial(game,data.getTeam(game).getSpawnPoint(game));
                }
                else{
                    data.state = PlayerState.DEAD;
                    data.applyState(game,player);
                }
            }
            else{
                if(data.isDead()){
                    game.addSpectatorPlayer(player);
                    data.applyState(game,player);
                }
            }
            if(game.inSetup() && game.settings.teamSelector.random && !data.hasTeam()){
                game.genTeams();
            }
        });
        Overlays.setup(pje.getPlayer());
    }

    /**
     * Asks the player's team to perform death checks.
     */
    @EventHandler
    public void onLeave(PlayerQuitEvent pqe){
        whenParticipating(pqe.getPlayer(),(game,player,data)->{
            data.getTeam(game).checkDead(game);
        });
    }

    private void cancel(Player p, Cancellable e){
        if(p.hasPermission("sihwar.bypass")){
            return;
        }
        whenGameLoaded(game->{
            if(game.paused || game.getStage().flags.contains(GameFlag.FREEZE)){
                e.setCancelled(true);
            }
        });
    }

    @EventHandler
    public void onAttack(PrePlayerAttackEntityEvent ppaee){
        cancel(ppaee.getPlayer(),ppaee);
        if(!ppaee.isCancelled()){
            whenInTeam(ppaee.getPlayer(),(game,team,player,data)->{
                if(team.players.contains(ppaee.getAttacked().getUniqueId())){
                    ppaee.setCancelled(true);
                }
            });
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent bbe){
        cancel(bbe.getPlayer(),bbe);
    }


    @EventHandler
    public void onPlace(BlockPlaceEvent bpe){
        cancel(bpe.getPlayer(),bpe);
    }

    /*
    Various safety checking methods.
     */

    protected void whenGameLoaded(Consumer<GameInstance> cons){
        if(SIHWar.hasGame()){
            cons.accept(SIHWar.game);
        }
    }

    protected void whenParticipating(Player p, TriConsumer<GameInstance, Player, PlayerData> consumer){
        whenGameLoaded(game->{
            PlayerData data = game.playerData.get(p);
            if(data != null){
                consumer.accept(game,p,data);
            }
        });
    }

    private void whenNotParticipating(Player player, Consumer<GameInstance> consumer) {
        whenGameLoaded(game->{
            if(game.playerData.get(player) == null) {
                consumer.accept(game);
            }
        });
    }

    private void whenInTeam(Player p, QuadConsumer<GameInstance, TeamData, Player, PlayerData> consumer){
        whenParticipating(p,(game,player,data)->{
            if(data.hasTeam()){
                TeamData team = data.getTeam(game);
                if(team != null) {
                    consumer.accept(game, team, player, data);
                }
                else{
                    data.team="";
                }
            }
        });
    }
}

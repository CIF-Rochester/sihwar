package com.vicious.sihwar.player;


import com.vicious.sihwar.SIHWar;
import com.vicious.sihwar.TeamData;
import com.vicious.sihwar.data.GameInstance;
import com.vicious.sihwar.util.Vec2D;
import com.vicious.viciouslib.persistence.storage.aunotamations.Save;
import com.vicious.viciouslib.persistence.storage.aunotamations.Unmapped;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerData implements Listener {
    @Save
    public boolean frozen = false;
    @Save
    @Unmapped
    public PlayerState state = PlayerState.WAITING;
    @Save
    public String team = "";

    public Set<String> invites = new HashSet<>();

    public UUID uuid;

    public PlayerData() {
    }

    public PlayerData(UUID u) {
        this.uuid = u;
    }

    public boolean hasTeam() {
        return !team.isEmpty();
    }

    public TeamData getTeam(GameInstance instance) {
        return instance.teams.getTeam(team);
    }

    public OfflinePlayer getPlayer() {
        OfflinePlayer out = Bukkit.getPlayer(uuid);
        return out == null ? Bukkit.getOfflinePlayer(uuid) : out;
    }

    public void whenOnline(Consumer<Player> cons) {
        OfflinePlayer op = getPlayer();
        if (op instanceof Player p) {
            cons.accept(p);
        }
    }

    /**
     * Spawns the player at the highest block at the XZ position provided. Temporarily freezes them and grants invulnerability until the game starts.
     */
    public void spawnInitial(GameInstance instance, Location pos) {
        whenOnline(p -> {
            p.getInventory().clear();
            p.setGameMode(GameMode.SURVIVAL);
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
            state = PlayerState.ALIVE;
            p.teleport(pos);
            System.out.println(instance.stageIndex + " : " + uuid);
            if(instance.stageIndex == 0) {
                p.setInvulnerable(true);
                frozen = true;
                Bukkit.getScheduler().scheduleSyncDelayedTask(SIHWar.INSTANCE, () -> {
                    frozen = false;
                    p.setInvulnerable(false);
                }, instance.period * 20L);
            }
        });
    }

    /**
     * Spectates a live online player on their team.
     */
    public void spectate(GameInstance instance){
        whenOnline(self->{
            TeamData team = getTeam(instance);
            if(team.isDead(instance)){
                instance.spectateRandom(self);
            }
            else {
                for (OfflinePlayer player : team.getPlayers()) {
                    if (instance.playerData.get(player).isAlive()) {
                        if (player instanceof Player p) {
                            self.setSpectatorTarget(p);
                        }
                    }
                }
            }
        });
    }

    private Location getSpawnPosition(Vec2D location, GameInstance instance) {
        return instance.world.getHighestBlockAt((int) location.x, (int) location.y).getLocation().add(0,1,0);
    }

    public boolean isWaiting() {
        return state == PlayerState.WAITING;
    }

    public boolean isAlive() {
        return state != PlayerState.DEAD;
    }

    public boolean isDead() {
        return state == PlayerState.DEAD;
    }

    public void applyState(GameInstance instance,Player p) {
        state.apply(p,this, instance);
    }

    public void setState(PlayerState state) {
        this.state=state;
    }

    public boolean isInvincible() {
        return state == PlayerState.WAITING || frozen;
    }

    public boolean isLeader(GameInstance instance) {
        return getTeam(instance).isLeader(uuid);
    }

    public void addInvite(String teamName) {
        invites.add(teamName.toLowerCase());
    }

    public boolean hasInvite(String teamName){
        return invites.contains(teamName.toLowerCase());
    }

    public void removeInvite(String teamName){
        invites.remove(teamName);
    }

    public String getName() {
        return getPlayer().getName();
    }

    public boolean isParticipating() {
        return team != null;
    }
}

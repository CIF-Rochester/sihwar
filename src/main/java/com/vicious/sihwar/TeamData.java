package com.vicious.sihwar;

import com.vicious.sihwar.data.GameInstance;
import com.vicious.sihwar.player.PlayerData;
import com.vicious.sihwar.player.PlayerState;
import com.vicious.sihwar.util.Announcement;
import com.vicious.sihwar.util.Vec2D;
import com.vicious.viciouslib.persistence.storage.aunotamations.Save;
import com.vicious.viciouslib.persistence.storage.aunotamations.Typing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

public class TeamData {
    @Save
    public String name = "";
    @Save
    public Vec2D spawn = new Vec2D(0,0);
    @Save
    public boolean isDead = false;
    @Save
    public UUID leader;
    @Save
    public boolean isOpen = false;
    @Save
    @Typing(UUID.class)
    public Set<UUID> players = new HashSet<>();

    public TeamData(){};

    public TeamData(String name){
        this.name=name;
    }

    /**
     * Gets the team spawn point at the highest solid block.
     */
    public Location getSpawnPoint(GameInstance instance){
        return instance.world.getHighestBlockAt((int) spawn.x, (int) spawn.y).getLocation().add(0,1,0);
    }

    public void setSpawn(Vec2D spawn){
        this.spawn=spawn;
    }

    public void addPlayer(PlayerData data, GameInstance instance) {
        players.add(data.uuid);
        if(data.hasTeam()){
            data.getTeam(instance).removePlayer(data);
        }
        data.team=name;
        if(leader == null){
            promote(data.getPlayer());
        }
    }

    public void removePlayer(OfflinePlayer player) {
        players.remove(player.getUniqueId());
    }

    public void removePlayer(PlayerData data) {
        players.remove(data.uuid);
    }

    public void setName(String name) {
        this.name=name;
    }

    /**
     * @return List of Player and OfflinePlayer objects.
     */
    public List<OfflinePlayer> getPlayers(){
        List<OfflinePlayer> players = new ArrayList<>();
        this.players.forEach(u->{
            Player p = Bukkit.getPlayer(u);
            players.add(Objects.requireNonNullElseGet(p, () -> Bukkit.getOfflinePlayer(u)));
        });
        return players;
    }

    /**
     * Executed when the team is determined to be dead for the first time.
     */
    public void onTeamDeath(GameInstance instance){
        isDead=true;
        for (PlayerData playerData : getPlayerDatas(instance)) {
            playerData.state = PlayerState.DEAD;
        }
        Component out = Component.text(name + " has been eliminated").color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
        Announcement.of(out, Sound.ITEM_TRIDENT_THUNDER).send().log();
        instance.save();
    }


    public PlayerData getPlayerData(UUID uuid, GameInstance instance){
        return instance.playerData.get(uuid);
    }

    /**
     * Gets all the PlayerData objects for the team members.
     */
    public List<PlayerData> getPlayerDatas(GameInstance instance) {
        List<PlayerData> out = new ArrayList<>();
        for (OfflinePlayer player : getPlayers()) {
            if(player instanceof Player){
                out.add(getPlayerData(player.getUniqueId(),instance));
            }
        }
        return out;
    }

    /**
     * If the grace period is over, checks if all online players are dead and all live players are offline.
     */
    public boolean isDead(GameInstance instance){
        if(isDead){
            return true;
        }
        if(instance.inGrace()){
            return false;
        }
        List<OfflinePlayer> plrs = getPlayers();
        for (OfflinePlayer plr : plrs) {
            PlayerData data = getPlayerData(plr.getUniqueId(),instance);
            //Returns true if either all online players are dead or all online players are dead and all live players are offline.
            if(data.isAlive() && plr instanceof Player){
                return false;
            }
        }
        return true;
    }

    /**
     * Spawns the team at the initial grace period spawn point.
     */
    public void spawnInitial(GameInstance instance) {
        Location l = getSpawnPoint(instance);
        for (PlayerData playerData : getPlayerDatas(instance)) {
            playerData.spawnInitial(instance,l);
        }
    }

    public void checkDead(GameInstance instance) {
        boolean prev = isDead;
        if(!prev && isDead(instance)){
            onTeamDeath(instance);
        }
    }

    public boolean isLeader(UUID uniqueId) {
        return leader.equals(uniqueId);
    }

    public void promoteRandom() {
        for (UUID player : players) {
            promote(Bukkit.getOfflinePlayer(player));
            break;
        }
    }

    public void promote(OfflinePlayer promotee) {
        leader = promotee.getUniqueId();
        Announcement.soundless(Component.text(promotee.getName() + " was promoted to leader!").color(NamedTextColor.LIGHT_PURPLE)).send(this);
    }

    public void disband(GameInstance instance) {
        for (PlayerData playerData : getPlayerDatas(instance)) {
            removePlayer(playerData);
            playerData.whenOnline(plr->{
                plr.sendMessage("Your team was disbanded.");
            });
        }
    }

    public boolean isFull(GameInstance instance) {
        return players.size() >= instance.settings.teamSelector.maxTeamSize;
    }

    public void kick(OfflinePlayer kicked) {
        Announcement.of(Component.text("You have been kicked from your team!").color(NamedTextColor.RED),Sound.BLOCK_GLASS_BREAK).send(kicked);
        players.remove(kicked.getUniqueId());
        Announcement.soundless(Component.text(kicked.getName() + " was kicked!").color(NamedTextColor.RED)).send(this);
    }

    public void leave(PlayerData data) {
        removePlayer(data);
        if(isLeader(data.uuid)){
            if(players.isEmpty()){
                return;
            }
            promoteRandom();
        }
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void join(PlayerData joiner, GameInstance instance) {
        Announcement.soundless(Component.text(joiner.getName() + " joined the team!")).send(this);
        addPlayer(joiner,instance);
    }
}

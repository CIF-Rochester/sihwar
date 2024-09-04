package com.vicious.sihwar.data;

import com.vicious.sihwar.TeamData;
import com.vicious.sihwar.player.PlayerData;
import com.vicious.sihwar.util.Announcement;
import com.vicious.sihwar.util.Util;
import com.vicious.viciouslib.persistence.storage.aunotamations.Save;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Supplier;

public class TeamSelector {
    @Save
    public int maxTeamSize = -1;
    @Save
    public int maxNoTeams = -1;
    @Save
    public boolean random = false;

    public TeamSelector(){}

    public TeamSelector(int maxTeamSize, int maxNoTeams, boolean random) {
        this.maxTeamSize = maxTeamSize;
        this.maxNoTeams = maxNoTeams;
        this.random = random;
    }

    public boolean generate(GameInstance instance, boolean unsafe){
        if(random){
            instance.teams.teams.clear();
            List<Player> players = Util.getRandomizedPlayers();
            List<List<Player>> fractions = Util.fractionate(players,maxTeamSize == -1 ? Integer.MAX_VALUE : maxTeamSize, maxNoTeams == -1 ? Integer.MAX_VALUE : maxNoTeams);
            int n = 0;
            for (List<Player> fraction : fractions) {
                n+=fraction.size();
            }
            if(n < players.size() && !unsafe){
                throw new IllegalStateException("Not enough space with current settings to fit all players into a team.");
            }
            for (List<Player> fraction : fractions) {
                instance.teams.makeRandomTeam(fraction,instance);
            }
            return true;
        }
        return false;
    }

    public String processManualCreate(GameInstance instance, PlayerData creator, String name){
        if(random){
            return "Error: You are not allowed to create teams. Game is in random team mode.";
        }
        if(instance.teams.exists(name)){
            return "Error: A team with that name already exists.";
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if(!Character.isLetterOrDigit(c) && !(c == '-' || c == '_')){
                return "Illegal character in team name. Only use letters and numbers, spaces, _ and - also allowed.";
            }
        }
        TeamData data = instance.teams.create(name);
        if(creator != null) {
            data.addPlayer(creator, instance);
        }
        instance.genSpawns();
        return "Successfully created the team!";
    }
    public String processManualDisband(GameInstance instance, PlayerData disbander){
        if(random){
            return "Error: You are not allowed to create teams. Game is in random team mode.";
        }
        return whenTeamLeader(instance,disbander,()->{
            instance.teams.disband(disbander.getTeam(instance),instance);
            return "Your team was disbanded";
        });
    }

    public String processInvite(GameInstance game, PlayerData inviter, String... targets){
        if(random){
            return "Error: You are not allowed to invite players. Game is in random team mode.";
        }
        return whenTeamLeader(game,inviter,()->{
            if(inviter.getTeam(game).isFull(game)){
                return "Error: Your team is full.";
            }
            String teamName = inviter.getTeam(game).name;
            StringBuilder failed = new StringBuilder();
            StringBuilder success = new StringBuilder();
            for (String arg : targets) {
                OfflinePlayer offline = Bukkit.getOfflinePlayerIfCached(arg);
                if (offline == null) {
                    failed.append("No such player: ").append(arg).append("\n");
                } else {
                    game.playerData.get(offline.getUniqueId()).addInvite(teamName);
                    Announcement.soundless(Component.text(arg + " has been invited to your team.")).send(inviter.getTeam(game));
                    if (offline instanceof Player plr) {
                        plr.sendMessage(Component.text("You have been invited to team " + teamName).color(NamedTextColor.GREEN));
                    }
                }
            }
            return failed.append(success).toString();
        });
    }
    public String processKick(GameInstance instance, PlayerData kicker, String... targets){
        if(random){
            return "Error: You are not allowed to kick players. Game is in random team mode.";
        }
        return whenTeamLeader(instance,kicker,()->{
            StringBuilder failed = new StringBuilder();
            TeamData team = kicker.getTeam(instance);
            for (String target : targets) {
                OfflinePlayer kicked = Bukkit.getOfflinePlayerIfCached(target);
                if(kicked == null){
                    failed.append(target).append(" has never joined the server.").append("\n");
                }
                else if(kicked.getUniqueId().equals(kicker.uuid)){
                    failed.append("You cannot kick yourself!").append("\n");
                }
                else{
                    if(team.players.contains(kicked.getUniqueId())){
                        team.kick(kicked);
                    }
                    else{
                        failed.append(target + " is not in your team.");
                    }
                }
            }

            return failed.toString();
        });
    }
    public String processLeave(GameInstance instance, PlayerData leaver){
        if(random){
            return "Error: You are not allowed to leave your team. Game is in random team mode.";
        }
        return whenInTeam(leaver,()->{
            TeamData team = leaver.getTeam(instance);
            team.leave(leaver);
            instance.teams.update(instance,team);
            return "You left " + team;
        });
    }
    public String processJoin(GameInstance instance, PlayerData joiner, String name){
        if(random){
            return "Error: You are not allowed to join teams. Game is in random team mode.";
        }
        if(instance.teams.getTeam(name).isFull(instance)){
            return "Error: That team is full.";
        }
        TeamData team = instance.teams.getTeam(name);
        if(team == null){
            return "Error: " + name + " does not exist.";
        }
        if(joiner.hasInvite(name) || team.isOpen()){
            joiner.removeInvite(name);
            team.join(joiner, instance);
            return "You joined " + name  + "!";
        }
        else {
            return "Error: You do not have an invite for that team.";
        }
    }

    private String whenInTeam(PlayerData data, Supplier<String> action){
        if(data.hasTeam()){
            return action.get();
        }
        return "Error: You are not in a team.";
    }

    private String whenTeamLeader(GameInstance instance, PlayerData data, Supplier<String> action){
        return whenInTeam(data,()->{
            if(data.isLeader(instance)) {
                return action.get();
            }
            return "Error: You must be team leader to do this.";
        });
    }
}

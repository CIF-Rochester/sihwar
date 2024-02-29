package com.vicious.sihwar.data;

import com.vicious.sihwar.TeamData;
import com.vicious.sihwar.util.Util;
import com.vicious.viciouslib.persistence.storage.aunotamations.Save;
import com.vicious.viciouslib.persistence.storage.aunotamations.Typing;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamsList {
    @Save
    @Typing({String.class, TeamData.class})
    public Map<String, TeamData> teams = new HashMap<>();

    public boolean exists(String name) {
        return teams.containsKey(name.toLowerCase());
    }

    public TeamData create(String name){
        TeamData out = new TeamData(name);
        add(name,out);
        return out;
    }

    private void add(String name, TeamData teamData) {
        teams.put(name.toLowerCase(),teamData);
    }

    public void disband(TeamData team, GameInstance instance) {
        team.disband(instance);
        remove(team.name);
    }

    public void remove(String name){
        teams.remove(name.toLowerCase());
    }

    public void update(GameInstance instance, TeamData team) {
        if(team.players.isEmpty()){
            disband(team, instance);
        }
    }

    public TeamData getTeam(String name) {
        return teams.get(name.toLowerCase());
    }

    public void makeRandomTeam(List<Player> players, GameInstance instance) {
        String name = Util.getRandomName();
        while(exists(name)){
            name = Util.getRandomName();
        }
        TeamData team = create(name);
        for (Player player : players) {
            team.addPlayer(instance.playerData.get(player),instance);
        }
    }

    public Collection<TeamData> getTeams() {
        return teams.values();
    }

    public int size() {
        return teams.size();
    }
}

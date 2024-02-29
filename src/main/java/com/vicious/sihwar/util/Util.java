package com.vicious.sihwar.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Util {
    private static final List<String> randomName = List.of("Goombers","AIF","Normandy","Russia","Ukraine","USA","Rippers","RIFE","Kill","McDonaldsCheeseyFries","Gamers","CIF","MIF","SIH","PVPGods","Warriors","Soldiers","USMilitaryBudget","EvilPeople","Heroes","Rochesterees","Rockies","PoolCleaners","Albatross","VICIOUS","Dystopia","TTEAM","Wilco","Goofies","Funnies","Rizzers","Joker","Society","Slammer","Maillords","Lords","Leards","Oops","FourChances","FiveChances","Root","BackdoorWizards","LoopyLoopers","Alters","Salters","Winners","Losers","Excelsus","Bonerattlers","Gamers");

    public static List<String> getRandomNamesShuffled() {
        List<String> out = new ArrayList<>();
        List<String> copy = new ArrayList<>(randomName);
        while(!copy.isEmpty()){
            out.add(String.valueOf(copy.remove((int)(copy.size()*Math.random()))));
        }
        return out;
    }

    public static List<Player> getRandomizedPlayers() {
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        List<Player> out = new ArrayList<>();
        while(!players.isEmpty()){
            out.add(players.remove((int)(Math.random()*players.size())));
        }
        return out;
    }

    public static <T> List<List<T>> fractionate(List<T> lst, int maxPer, int maxTeams){
        List<List<T>> out = new ArrayList<>();
        int maxGroups = Math.min((int)Math.ceil(lst.size()/(float)maxPer),maxTeams);
        for (int i = 0; i < maxGroups; i++) {
            out.add(new ArrayList<>());
        }
        int j = 0;
        for (List<T> vals : out) {
            for (int k = 0; k < maxPer && j < lst.size(); k++) {
                vals.add(lst.get(j));
                j++;
            }
        }
        if(out.size() > 2) {
            int s1 = out.get(out.size() - 1).size();
            int s2 = out.get(out.size() - 2).size();
            if (s1 < maxPer) {
                int total = s1+s2;
                if (Math.ceil(total/(float)maxPer) < maxPer) {
                    T t = out.get(out.size()-2).remove(0);
                    out.get(out.size()-1).add(t);
                }
            }
        }
        return out;
    }

    public static String getRandomName() {
        return randomName.get((int) (Math.random()*randomName.size()));
    }
}

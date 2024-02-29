package com.vicious.sihwar.util;

import com.vicious.sihwar.SIHWar;
import com.vicious.sihwar.TeamData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * Represents messages and handles sending them to the relevant parties. Allows including sound effects as well.
 */
public class Announcement {
    /**
     * Used for convenience, allows avoiding null checks.
     */
    public static final Announcement EMPTY = new Announcement(null,null){
        @Override
        public Announcement send() {
            return this;
        }

        @Override
        public Announcement sendPermitted(String permission) {
            return this;
        }

        @Override
        public Announcement broadcast() {
            return this;
        }

        @Override
        public Announcement send(TeamData teamData) {
            return this;
        }

        public Announcement log() {
            return this;
        }
    };
    public Component component;
    public Sound sound;

    public Announcement(Component component, Sound sound){
        this.component=component;
        this.sound=sound;
    }

    public static Announcement of(Component component, Sound sound) {
        return new Announcement(component, sound);
    }

    public static Announcement soundless(Component component) {
        return of(component,null);
    }

    public static Announcement sendOnce(Component component, Sound sound) {
        return new Announcement(component,sound){
            boolean sent = false;
            boolean logged = false;

            @Override
            public Announcement send() {
                if(!sent){
                    super.send();
                    sent=true;
                }
                return this;
            }
            @Override
            public Announcement log() {
                if(!logged){
                    super.log();
                    logged=true;
                }
                return this;
            }

            @Override
            public Announcement sendPermitted(String permission) {
                if(!sent){
                    super.sendPermitted(permission);
                    sent=true;
                }
                return this;

            }
        };
    }

    /**
     * Sends a chat message to all players
     */
    public Announcement send(){
        Bukkit.getServer().broadcast(component);
        if(sound != null){
            net.kyori.adventure.sound.Sound altSound = net.kyori.adventure.sound.Sound.sound(sound.key(), net.kyori.adventure.sound.Sound.Source.MASTER,1,1);
            Bukkit.getServer().playSound(altSound);
        }
        return this;
    }

    /**
     * Sends to a player
     * @return
     */
    public Announcement send(OfflinePlayer player){
        if(player instanceof Player p) {
            p.sendMessage(component);
            if (sound != null) {
                net.kyori.adventure.sound.Sound altSound = net.kyori.adventure.sound.Sound.sound(sound.key(), net.kyori.adventure.sound.Sound.Source.MASTER, 1, 1);
                p.playSound(altSound);
            }
        }
        return this;
    }

    /**
     * Sends a chat message to all players with the permission.
     */
    public Announcement sendPermitted(String permission) {
        Bukkit.getOnlinePlayers().forEach(p->{
            if(p.hasPermission(permission)){
                p.sendMessage(component);
                if(sound != null){
                    net.kyori.adventure.sound.Sound altSound = net.kyori.adventure.sound.Sound.sound(sound.key(), net.kyori.adventure.sound.Sound.Source.MASTER,1,1);
                    Bukkit.getServer().playSound(altSound);
                }
            }
        });
        return this;
    }

    /**
     * Sends to the server logs.
     */
    public Announcement log() {
        Bukkit.getServer().getConsoleSender().sendMessage(component);
        return this;
    }

    /**
     * Sends a chat message to all online team members.
     */
    public Announcement send(TeamData teamData) {
        for (OfflinePlayer player : teamData.getPlayers()) {
            if(player instanceof Player p){
                send(p);
            }
        }
        return this;
    }

    /**
     * Presents a title to all players for 10 seconds.
     */
    public Announcement broadcast() {
        Bukkit.getServer().clearTitle();
        Bukkit.getServer().showTitle(Title.title(component,Component.text().build()));
        Bukkit.getScheduler().scheduleSyncDelayedTask(SIHWar.INSTANCE,()->{
            Bukkit.getServer().clearTitle();
        },200L);
        if(sound != null){
            net.kyori.adventure.sound.Sound altSound = net.kyori.adventure.sound.Sound.sound(sound.key(), net.kyori.adventure.sound.Sound.Source.MASTER,1,1);
            Bukkit.getServer().playSound(altSound);
        }
        return this;
    }
}

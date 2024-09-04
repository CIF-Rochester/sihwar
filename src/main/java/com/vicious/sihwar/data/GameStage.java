package com.vicious.sihwar.data;

import com.vicious.sihwar.player.PlayerData;
import com.vicious.sihwar.util.Announcement;
import com.vicious.viciouslib.persistence.storage.aunotamations.Save;
import com.vicious.viciouslib.persistence.storage.aunotamations.Typing;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GameStage {
    public static final GameStage SETUP = new GameStage("setup",-1,3000);
    public static final GameStage COMPLETE = new GameStage("setup",-1,3000);

    static {
        COMPLETE.startAnnouncement="";
        COMPLETE.endAnnouncement="";
        COMPLETE.warningAnnouncement="";
        SETUP.startAnnouncement="";
        SETUP.endAnnouncement="";
        SETUP.warningAnnouncement="";
    }

    @Save
    public String name;
    @Save
    public int period = 0;
    @Save
    public int borderEnd = 0;
    @Save
    @Typing(GameFlag.class)
    public Set<GameFlag> flags = new HashSet<>();
    @Save
    @Typing(Integer.class)
    public Set<Integer> announcementIntervals = new HashSet<>();

    @Save
    public String startAnnouncement = "BLOCK_NOTE_BLOCK_PLING:LIGHT_PURPLE:BOLD::name: has begun!";
    @Save
    public String warningAnnouncement = "BLOCK_NOTE_BLOCK_PLING:GOLD:NONE::name: will end in :period:!";
    @Save
    public String endAnnouncement = "ENTITY_ENDER_DRAGON_GROWL:RED:BOLD::name: has ended!";

    public GameStage(String name, int period, int borderEnd) {
        this.name = name;
        this.period = period;
        this.borderEnd = borderEnd;
    }

    public GameStage(){}

    public String getName() {
        return name;
    }

    public boolean ticks() {
        return period > -1;
    }

    private Announcement of(String text, GameInstance instance){
        int index = text.indexOf(":");
        Sound sound = Sound.valueOf(text.substring(0,index));
        text = text.substring(index+1);
        index = text.indexOf(":");
        try {
            String name = text.substring(0,index);
            NamedTextColor color = name.equals("NONE") ? NamedTextColor.WHITE : (NamedTextColor) NamedTextColor.class.getDeclaredField(name).get(NamedTextColor.class);
            text = text.substring(index+1);
            index = text.indexOf(":");
            name = text.substring(0,index);
            TextDecoration decor = name.equals("NONE") ? null : TextDecoration.valueOf(name);
            if(decor != null) {
                return Announcement.of(Component.text(
                        text.substring(index + 1)
                                .replaceAll(":name:",name)
                                .replaceAll(":period:",instance.getTimeString())
                                .replaceAll(":end:", String.valueOf(borderEnd))).color(color).decorate(decor),sound);
            }
            else {
                return Announcement.of(Component.text(
                        text.substring(index + 1)
                                .replaceAll(":name:",name)
                                .replaceAll(":period:",instance.getTimeString())
                                .replaceAll(":end:", String.valueOf(borderEnd))).color(color),sound);
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public void end(GameInstance instance){
        if(endAnnouncement.isEmpty()){
            return;
        }
        of(endAnnouncement,instance).send();
    }

    public void warning(GameInstance instance){
        if(warningAnnouncement.isEmpty()){
            return;
        }
        of(warningAnnouncement,instance).send();
    }

    public boolean shrinks() {
        return flags.contains(GameFlag.SHRINK);
    }

    public void onRespawn(GameInstance instance, PlayerData player){
        player.whenOnline(plr->{
            if(flags.contains(GameFlag.HASTE)){
                plr.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING,(int)instance.period*20,1,false,false,true));
            }
            if(flags.contains(GameFlag.NIGHTVIS)){
                plr.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION,(int)instance.period*20,0,false,false,true));
            }
            if(flags.contains(GameFlag.STRENGTH)){
                plr.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,(int)instance.period*20,0,false,false,true));
            }
        });
    }

    public void start(GameInstance game){
        for (PlayerData player : game.playerData.getPlayerData()) {
            onRespawn(game,player);
        }
        if(startAnnouncement.isEmpty()){
            return;
        }
        of(startAnnouncement,game).send();
    }

    public boolean inGrace() {
        return flags.contains(GameFlag.GRACE);
    }

    public GameStage intervals(Integer... vals) {
        announcementIntervals.addAll(Arrays.asList(vals));
        return this;
    }
    public GameStage startAnnouncement(String text, Sound sound, NamedTextColor color, @Nullable TextDecoration decoration){
        if(text.isEmpty()){
            startAnnouncement = "";
        }
        else {
            startAnnouncement = sound.name() + ":" + color.toString().toUpperCase() + ":" + (decoration == null ? "NONE" : decoration.name()) + ":" + text;
        }
        return this;
    }
    public GameStage endAnnouncement(String text, Sound sound, NamedTextColor color, @Nullable TextDecoration decoration){
        if(text.isEmpty()){
            endAnnouncement = "";
        }
        else {
            endAnnouncement = sound.name() + ":" + color.toString().toUpperCase() + ":" + (decoration == null ? "NONE" : decoration.name()) + ":" + text;
        }
        return this;
    }
    public GameStage warningAnnouncement(String text, Sound sound, NamedTextColor color, @Nullable TextDecoration decoration){
        if(text.isEmpty()){
            warningAnnouncement = "";
        }
        else {
            warningAnnouncement = sound.name() + ":" + color.toString().toUpperCase() + ":" + (decoration == null ? "NONE" : decoration.name()) + ":" + text;
        }
        return this;
    }
}

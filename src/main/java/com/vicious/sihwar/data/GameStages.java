package com.vicious.sihwar.data;

import com.vicious.viciouslib.persistence.PersistenceHandler;
import com.vicious.viciouslib.persistence.storage.aunotamations.PersistentPath;
import com.vicious.viciouslib.persistence.storage.aunotamations.Save;
import com.vicious.viciouslib.persistence.storage.aunotamations.Typing;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Sound;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GameStages {
    @PersistentPath
    public static String path = "plugins/config/vicious/warstages.txt";
    @Save
    @Typing({String.class, GameStage.class})
    public static Map<String,GameStage> stages = new HashMap<>();

    public static void init() {
        newGameStage("Starting",20,2300,GameFlag.REVIVE,GameFlag.GRACE,GameFlag.FREEZE)
                .intervals(15,10,5,4,3,2,1)
                .startAnnouncement("The game Is starting!", Sound.BLOCK_NOTE_BLOCK_PLING, NamedTextColor.GOLD, TextDecoration.BOLD)
                .endAnnouncement("The game has begun!",Sound.BLOCK_GLASS_BREAK, NamedTextColor.GOLD, TextDecoration.BOLD)
                .warningAnnouncement("The game will start in :period:!",Sound.BLOCK_NOTE_BLOCK_PLING, NamedTextColor.GREEN, null);
        newGameStage("Grace",60*40,2300,GameFlag.HASTE,GameFlag.NIGHTVIS,GameFlag.STRENGTH,GameFlag.REVIVE,GameFlag.GRACE)
                .intervals(60*30,60*20,60*10,60*5,60,30,15,10,5)
                .startAnnouncement("The grace period has begun! You cannot attack enemy teams yet! Death is not permanent yet!",Sound.BLOCK_NOTE_BLOCK_PLING, NamedTextColor.GREEN, TextDecoration.BOLD)
                .warningAnnouncement("The grace period will end in :period:!",Sound.BLOCK_NOTE_BLOCK_PLING, NamedTextColor.RED, null)
                .endAnnouncement("The grace period has ended! Violence is now legal and death is permanent!",Sound.ENTITY_ENDER_DRAGON_GROWL, NamedTextColor.RED, TextDecoration.BOLD);
        newGameStage("Shrink",60*30,100,GameFlag.SHRINK,GameFlag.NIGHTVIS)
                .intervals(60*20,60*10,60*5,60,30,15,10,5)
                .startAnnouncement("The border has began shrinking to :end: for :period:. Escape its deadly grasp!",Sound.ITEM_GOAT_HORN_PLAY, NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD)
                .warningAnnouncement(":period: left of border shrink time.",Sound.BLOCK_NOTE_BLOCK_PLING,NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD)
                .endAnnouncement("The border has reached its end.", Sound.BLOCK_NOTE_BLOCK_PLING,NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD);
        newGameStage("Battle",60*5,75)
                .intervals(5*60,4*60,3*60,2*60,60,30,15,10,9,8,7,6,5,4,3,2,1)
                .startAnnouncement("Armageddon will begin in :period:. Players not in the Overworld will be punished with death!",Sound.BLOCK_NOTE_BLOCK_PLING,NamedTextColor.RED, TextDecoration.BOLD)
                .warningAnnouncement("Armageddon will begin in :period:.",Sound.BLOCK_NOTE_BLOCK_PLING,NamedTextColor.LIGHT_PURPLE, null)
                .endAnnouncement("",null,null,null);
        newGameStage("Armageddon",60*3,5,GameFlag.SHRINK,GameFlag.COLLAPSE)
                .startAnnouncement("Armageddon has begun!!! Shrinking Border to 0,0!",Sound.ENTITY_ENDER_DRAGON_GROWL, NamedTextColor.RED, TextDecoration.BOLD)
                .warningAnnouncement("",null,null,null)
                .endAnnouncement("Let the finale begin!",Sound.ENTITY_ENDER_DRAGON_GROWL, NamedTextColor.RED, TextDecoration.BOLD);
        newGameStage("Finale",2000000000,5,GameFlag.COLLAPSE)
                .startAnnouncement("",null,null,null)
                .warningAnnouncement("",null,null,null)
                .endAnnouncement("",null,null,null);
        PersistenceHandler.init(GameStages.class);
    }

    public static GameStage get(String s) {
        return stages.get(s.toLowerCase());
    }

    public static GameStage newGameStage(String name, int period, int border, GameFlag... flags){
        GameStage stage = new GameStage(name,period,border);
        stage.flags.addAll(Arrays.asList(flags));
        stages.put(name.toLowerCase(),stage);
        return stage;
    }
}

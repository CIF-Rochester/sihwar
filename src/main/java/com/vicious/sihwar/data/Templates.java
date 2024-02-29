package com.vicious.sihwar.data;

import com.vicious.viciouslib.persistence.PersistenceHandler;
import com.vicious.viciouslib.persistence.storage.aunotamations.PersistentPath;
import com.vicious.viciouslib.persistence.storage.aunotamations.Save;
import com.vicious.viciouslib.persistence.storage.aunotamations.Typing;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Templates {
    @PersistentPath
    public static String path = "plugins/config/vicious/wartemplates.txt";
    @Save
    @Typing({String.class, GameTemplate.class})
    public static Map<String,GameTemplate> templates = new HashMap<>();

    public static void reload() {
        templates.clear();
        newTemplate("solos",t->{
            t.teamSelector.maxTeamSize=1;
        });
        newTemplate("rduos",t->{
            t.teamSelector.maxTeamSize=2;
        });
        newTemplate("rtrios",t->{
            t.teamSelector.maxTeamSize=3;
        });
        newTemplate("rquads",t->{
            t.teamSelector.maxTeamSize=4;
        });
        newTemplate("duos",t->{
            t.teamSelector.maxTeamSize=2;
            t.teamSelector.random=false;
        });
        newTemplate("trios",t->{
            t.teamSelector.maxTeamSize=3;
            t.teamSelector.random=false;
        });
        newTemplate("quads",t->{
            t.teamSelector.maxTeamSize=4;
            t.teamSelector.random=false;
        });
        newTemplate("war",t->{
            t.teamSelector.maxNoTeams=2;
            t.teamSelector.random=false;
        });
        newTemplate("rwar",t->{
            t.teamSelector.maxNoTeams=2;
            t.teamSelector.random=true;
        });
        PersistenceHandler.init(Templates.class);
    }

    public static void newTemplate(String name, Consumer<GameTemplate> consumer){
        GameTemplate template = new GameTemplate(name);
        template.name=name;
        consumer.accept(template);
        templates.put(template.name, template);
    }
}

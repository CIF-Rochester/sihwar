package com.vicious.sihwar.data;

import com.vicious.viciouslib.persistence.storage.aunotamations.Save;
import com.vicious.viciouslib.persistence.storage.aunotamations.Typing;

import java.util.ArrayList;
import java.util.List;

public class GameTemplate {
    @Save
    public TeamSelector teamSelector = new TeamSelector(-1,-1,true);
    @Save
    @Typing(String.class)
    public List<String> stages = new ArrayList<>();
    @Save
    public String name = "solos";

    public GameTemplate(){}

    public GameTemplate(String name){
        this.name=name;
        stages.add("starting");
        stages.add("grace");
        stages.add("shrink");
        stages.add("battle");
        stages.add("armageddon");
        stages.add("finale");
    }
}

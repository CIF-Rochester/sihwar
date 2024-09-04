package com.vicious.sihwar.game;

import com.vicious.sihwar.data.GameInstance;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class Overlays {
    public static void setup(Player player){
        Bukkit.getScoreboardManager();
        ScoreboardManager m = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = m.getNewScoreboard();


        Objective o = scoreboard.registerNewObjective("info", Criteria.DUMMY, Component.text("Game Info").color(TextColor.color(NamedTextColor.LIGHT_PURPLE)).style(Style.style(TextDecoration.BOLD)),RenderType.INTEGER);
        o.setDisplaySlot(DisplaySlot.SIDEBAR);
        o.getScore("Stage Time (Sec)").setScore(0);
        o.getScore("Border Size").setScore(0);
        o.getScore("Border Distance").setScore(0);
        /*
        o.getScore("Team Center X").setScore(0);
        o.getScore("Team Center Z").setScore(0);
        o.getScore("Dist From TC").setScore(0);*/


        /*o = scoreboard.registerNewObjective("Border", Criteria.DUMMY, Component.text("Border Position"),RenderType.INTEGER);
        o.setDisplaySlot(DisplaySlot.SIDEBAR);
        o.isModifiable();
        o.getScore("Border").setScore(0);

        o = scoreboard.registerNewObjective("Distance", Criteria.DUMMY, Component.text("Bordeer Distance"),RenderType.INTEGER);
        o.setDisplaySlot(DisplaySlot.SIDEBAR);
        o.isModifiable();
        o.getScore("Distance").setScore(0);*/

        player.setScoreboard(scoreboard);
    }

    @SuppressWarnings("all")
    public static void update(Player player, GameInstance instance){
        Scoreboard scoreboard = player.getScoreboard();

        Objective o = scoreboard.getObjective("info");
        if(o != null) {
            int size = (int) (instance.world.getWorldBorder().getSize() / 2);
            o.getScore("Border Size").setScore(size);
            int dist = (int) Math.min(size - Math.abs(player.getX()), size - Math.abs(player.getZ()));
            o.getScore("Border Distance").setScore(dist);
            o.getScore("Stage Time (Sec)").setScore(instance.period);

            /*Vec2D vec = instance.playerData.get(player).getTeam(instance).calcCenter();

            if (player.getWorld().getEnvironment() == World.Environment.NETHER) {
                vec = new Vec2D(vec.x / 8, vec.y / 8);
            }

            o.getScore("Team Center X").setScore((int) vec.x);
            o.getScore("Team Center Z").setScore((int) vec.y);

            double x = player.getX()-vec.x;
            double y = player.getY()-vec.y;
            o.getScore("Dist From TC").setScore((int) Math.sqrt(x*x+y*y));*/
            player.setScoreboard(scoreboard);
        }
    }
}

package com.vicious.sihwar.commands.sub;

import com.vicious.sihwar.TeamData;
import com.vicious.sihwar.commands.CommandException;
import com.vicious.sihwar.commands.GameCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class WarInfoCommand extends GameCommand {
    public WarInfoCommand() {
        super("warinfo", "Shows info about the game.", "/warinfo", "warinfo");
    }

    @Override
    protected void process(CommandSender sender, String[] args) throws CommandException {
        whenGameLoaded(sender,game->{
            Component out = Component.text(game.name).color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD);
            out = out.append(Component.text("\nStage: " + game.getStage().getName()).color(NamedTextColor.DARK_AQUA));
            out = out.append(Component.text("\nTime remaining in stage: " + game.getTimeString()).color(NamedTextColor.DARK_AQUA));
            out = out.append(Component.text("\nWorld Border Shrinking To: " + game.getStage().borderEnd));
            out = out.append(Component.text("\nTeams: ").color(NamedTextColor.WHITE)).decorate(TextDecoration.BOLD);
            List<TeamData> teams = new ArrayList<>(game.teams.getTeams());
            for (int i = 0; i < teams.size(); i++) {
                TeamData team = teams.get(i);
                if(!team.isDead(game)){
                    out = out.append(Component.text(team.name).color(NamedTextColor.GREEN));
                }
                else{
                    out = out.append(Component.text(team.name).color(NamedTextColor.DARK_GRAY));
                }
                if(i < teams.size()-1){
                    out = out.append(Component.text(", ").color(NamedTextColor.WHITE));
                }
            }
            sender.sendMessage(out);
        });
    }
}

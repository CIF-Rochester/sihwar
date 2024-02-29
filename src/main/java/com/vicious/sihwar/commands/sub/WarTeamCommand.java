package com.vicious.sihwar.commands.sub;

import com.vicious.sihwar.TeamData;
import com.vicious.sihwar.commands.CommandException;
import com.vicious.sihwar.commands.GameCommand;
import com.vicious.sihwar.commands.GameSetupCommand;
import com.vicious.sihwar.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class WarTeamCommand extends GameCommand {
    public WarTeamCommand() {
        super("warteam","The root command for players","","warteam","sihwarteam");
        addSubCommand(new Create());
        addSubCommand(new Invite());
        addSubCommand(new Join());
        addSubCommand(new Leave());
        addSubCommand(new Promote());
        addSubCommand(new InfoOther());
        addSubCommand(new Kick());
        addSubCommand(new Open());
        addSubCommand(new HelpCommand(this));
    }

    @Override
    protected void process(CommandSender sender, String[] args) throws CommandException {
        whenInTeam(sender,(game,team,player,data)->{
            sender.sendMessage(getTeamInfo(team));
        });
    }


    private static Component getTeamInfo(TeamData team){
        Component out = Component.text("Team " + team.name + ": ");
        List<OfflinePlayer> players = team.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            OfflinePlayer plr = players.get(i);
            if(plr.getName() != null) {
                if (team.leader.equals(plr.getUniqueId())) {
                    out = out.append(Component.text(plr.getName()).decorate(TextDecoration.BOLD));
                }
                else{
                    out = out.append(Component.text(plr.getName()));
                }
                if(i < players.size()-1){
                    out = out.append(Component.text(", "));
                }
            }
        }
        return out;
    }

    private static class Create extends GameSetupCommand {
        protected Create() {
            super("create", "Creates a team and sets you as its leader.", "/team create <name>", "create");
        }

        @Override
        public int requiresArgs() {
            return 1;
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            forceParticipating(sender,(game,player,data)->{
                String name = args[0];
                if(name.equalsIgnoreCase("spectators")){
                    error(sender,"That name is reserved by the plugin!");
                }
                else {
                    result(sender,game.settings.teamSelector.processManualCreate(game,data,name));
                }
            });
        }


        @Override
        public List<String> tabComplete(String[] args, CommandSender sender) {
            return Util.getRandomNamesShuffled();
        }
    }

    private static class Invite extends GameSetupCommand {
        protected Invite() {
            super("invite", "Invites a player to your team", "/warteam invite <name>", "invite");
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            whenTeamLeader(sender,(game,team,player,data)->{
                result(sender,game.settings.teamSelector.processInvite(game,data,args));
            });
        }

        public List<String> tabComplete(String[] args, CommandSender sender) {

            return matchPlayers(args.length > 0 ? args[0] : "",sender instanceof Player ? (Player) sender : null);
        }
    }
    private static class Join extends GameSetupCommand {
        protected Join(){
            super("join","Joins a team if you have been invited to it","/team join <name>","join");
        }

        @Override
        public int requiresArgs() {
            return 1;
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            whenParticipating(sender,(game, player, data) -> {
                result(sender,game.settings.teamSelector.processJoin(game,data,args[0]));
            });
        }

        @Override
        public List<String> tabComplete(String[] args, CommandSender sender) {
            AtomicReference<List<String>> invite = new AtomicReference<>(new ArrayList<>());
            whenParticipating(sender,(instance,player,data)->{
                invite.set(new ArrayList<>(data.invites));
            });
            return invite.get();
        }
    }
    private static class Leave extends GameSetupCommand {
        protected Leave() {
            super("leave", "Makes you leave your team", "/leave", "leave");
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            whenInTeam(sender,(game,team,player,data)->{
                result(sender,game.settings.teamSelector.processLeave(game,data));
            });
        }
    }

    private static class Promote extends GameCommand {
        protected Promote() {
            super("promote", "Promotes a team member to leader", "/promote <name>", "promote");
        }

        @Override
        public int requiresArgs() {
            return 1;
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            whenTeamLeader(sender,(game,team,player,data)->{
                OfflinePlayer promotee = Bukkit.getOfflinePlayerIfCached(args[0]);
                if(promotee == null){
                    error(sender,"That player has never joined the server.");
                }
                else{
                    if(team.players.contains(promotee.getUniqueId())){
                        team.promote(promotee);
                    }
                    else{
                        error(sender,"That player is not in your team, invite them first");
                    }
                }
            });
        }
    }

    private static class Kick extends GameCommand {
        protected Kick() {
            super("kick", "Kicks a team member", "/kick <name>", "kick");
        }

        @Override
        public int requiresArgs() {
            return 1;
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            whenTeamLeader(sender,(game,team,player,data)->{
                OfflinePlayer kicked = Bukkit.getOfflinePlayerIfCached(args[0]);
                if(kicked == null){
                    error(sender,"That player has never joined the server.");
                }
                else{
                    if(team.players.contains(kicked.getUniqueId())){
                        team.kick(kicked);
                    }
                    else{
                        error(sender,"That player is not in your team.");
                    }
                }
            });
        }
    }

    private static class Open extends GameCommand {
        protected Open() {
            super("open", "Toggles the open setting.", "/team open", "open");
        }

        @Override
        public int requiresArgs() {
            return 1;
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            whenTeamLeader(sender,(game,team,player,data)->{
                if(team.isOpen){
                    success(sender,"Closed the team.");
                    team.isOpen=false;
                }
                else{
                    success(sender,"Opened the team.");
                    team.isOpen=true;
                }
            });
        }
    }

    private static class InfoOther extends GameCommand {
        protected InfoOther(){
            super("info","Shows information about the team provided","/warteam info <optional:teamname>","info");
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            whenParticipating(sender,(game,plr,data)->{
                if(args.length == 0){
                    if(data.hasTeam()){
                        sender.sendMessage(getTeamInfo(data.getTeam(game)));
                    }
                    else{
                        error(sender,"You must be in a team to use this command without a target team name.");
                    }
                }
                else{
                    TeamData team = game.teams.getTeam(args[0]);
                    if(team == null){
                        error(sender,args[0] + " does not exist.");
                    }
                    else{
                        sender.sendMessage(getTeamInfo(team));
                    }
                }
            });
        }
    }
}

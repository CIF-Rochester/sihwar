package com.vicious.sihwar.commands;

import com.vicious.sihwar.SIHWar;
import com.vicious.sihwar.TeamData;
import com.vicious.sihwar.data.GameInstance;
import com.vicious.sihwar.player.PlayerData;
import com.vicious.sihwar.util.QuadConsumer;
import com.vicious.viciouslib.util.interfaces.TriConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;

/**
 * Command class for all commands.
 */
public abstract class GameCommand extends Command {
    //The subcommands for this command.
    protected final Map<String, GameCommand> children = new HashMap<>();

    protected GameCommand(@NotNull String name, @NotNull String description, @NotNull String usageMessage, @NotNull List<String> aliases) {
        super(name, description, usageMessage, aliases);
    }

    public GameCommand(@NotNull String name, @NotNull String description, @NotNull String usageMessage, String... aliases) {
        this(name,description,usageMessage,List.of(aliases));
    }

    public void addSubCommand(GameCommand command){
        children.put(command.getName(),command);
    }

    protected void error(CommandSender sender, String message){
        if(message == null){
            message = "null";
        }
        sender.sendMessage(Component.text(message).color(NamedTextColor.RED));
    }

    protected void success(CommandSender sender, String message) {
        if(message == null){
            message = "null";
        }
        sender.sendMessage(Component.text(message).color(NamedTextColor.GREEN));
    }

    /**
     * Handles subcommands and error handling.
     */
    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        if(!hasPermission(sender)){
            error(sender,"No permission.");
            return false;
        }
        try{
            if(args.length > 0) {
                GameCommand subCommand = children.get(args[0]);
                if (subCommand != null) {
                    return subCommand.execute(sender, args[0], Arrays.copyOfRange(args, 1, args.length));
                }
            }
            if(requiresArgs() == 0) {
                process(sender, args);
                return true;
            } else if(requiresArgs() <= args.length){
                process(sender,args);
                return true;
            }
            else{
                error(sender,getUsage());
                return false;
            }
        } catch (Throwable t){
            error(sender,t.getMessage());
            if(t instanceof ConsoleCommandException || !(t instanceof CommandException)){
                SIHWar.logger.warning("Possible bug in command handling for " + getClass());
                t.printStackTrace();
            }
            return true;
        }
    }

    private boolean hasPermission(CommandSender sender) {
        if(getPermission() != null){
            return sender.hasPermission(getPermission());
        }
        else{
            return true;
        }
    }

    /**
     * When the command is run it must have at least this many arguments.
     */
    public int requiresArgs(){
        return 0;
    }

    /**
     * When command executable code is ran.
     */
    protected abstract void process(CommandSender sender, String[] args) throws CommandException;

    @Override
    public @NotNull String getUsage() {
        StringBuilder out = new StringBuilder(usageMessage);
        if(!children.isEmpty()) {
            out.append("\nSubcommands: ");
            for (GameCommand value : children.values()) {
                out.append('\n').append(value.getName()).append(": ").append(value.getDescription());
            }
        }
        return out.toString();
    }

    /**
     * Safety check for a loaded game.
     */
    protected void whenGameLoaded(CommandSender sender, Consumer<GameInstance> cons){
        if(SIHWar.hasGame()){
            cons.accept(SIHWar.game);
        }
        else{
            if(!sender.hasPermission("sihwar.admin")) {
                error(sender, "You cannot run this command right now.");
            }
            else{
                error(sender,"You don't have any game loaded.");
            }
        }
    }

    /**
     * Safety check for players only.
     */
    protected void playerOnly(CommandSender sender, Consumer<Player> consumer){
        if(sender instanceof Player p){
            consumer.accept(p);
        }
        else{
            error(sender,"Player only command.");
        }
    }

    /**
     * Forces a player to participate in the game.
     */
    protected void forceParticipating(CommandSender sender, TriConsumer<GameInstance,Player, PlayerData> consumer) {
        playerOnly(sender,p->{
            whenGameLoaded(sender,game->{
                PlayerData data = game.playerData.get(p);
                consumer.accept(game,p,data);
            });
        });
    }

    /**
     * Safety check for game participation.
     */
    protected void whenParticipating(CommandSender sender, TriConsumer<GameInstance, Player, PlayerData> consumer){
        playerOnly(sender,p->{
            whenGameLoaded(sender,game->{
                PlayerData data = game.playerData.get(p);
                if(data != null){
                    consumer.accept(game,p,data);
                }
                else{
                    error(sender,"You must be in a team to run this command, either create with /team create <new> or join one with /team join <name>");
                }
            });
        });
    }

    /**
     * Safety check for being in a team.
     */
    protected void whenInTeam(CommandSender sender, QuadConsumer<GameInstance, TeamData, Player, PlayerData> consumer){
        whenParticipating(sender,(game,player,data)->{
            if(data.hasTeam()){
                TeamData team = data.getTeam(game);
                if(team != null) {
                    consumer.accept(game, team, player, data);
                }
                else{
                    error(sender,"Your team no longer exists");
                    data.team="";
                }
            }
            else{
                error(sender,"You need to be in a team to use this command!");
            }
        });
    }

    /**
     * Safety check for being team leader.
     */
    protected void whenTeamLeader(CommandSender sender, QuadConsumer<GameInstance, TeamData, Player, PlayerData> consumer){
        whenInTeam(sender,(game,team,player,data)->{
            if(team.isLeader(player.getUniqueId())){
                consumer.accept(game,team,player,data);
            }
            else{
                error(sender,"You need to be team leader to use this command!");
            }
        });
    }

    /**
     * Safety check for providing a valid player name.
     */
    protected void whenCached(CommandSender sender, String name, Consumer<OfflinePlayer> consumer){
        OfflinePlayer player = Bukkit.getOfflinePlayerIfCached(name);
        if(player != null){
            consumer.accept(player);
        }
        else{
            error(sender,"No such player: " + name);
        }
    }


    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        return tabComplete(args, sender);
    }


    public List<String> tabComplete(String[] args, CommandSender sender){
        if(args.length > 0) {
            if (children.containsKey(args[0])){
                if(args.length > 1) {
                    return children.get(args[0]).tabComplete(Arrays.copyOfRange(args, 1, args.length), sender);
                }
                else{
                    return children.get(args[0]).tabComplete(new String[0], sender);
                }
            }
            else{
                return match(args[0],children.keySet());
            }
        }
        return new ArrayList<>(children.keySet());
    }

    public List<String> match(String str, Collection<String> options){
        List<String> matched = new ArrayList<>(options.stream().filter(val -> val.startsWith(str)).toList());
        matched.sort(String.CASE_INSENSITIVE_ORDER);
        return matched;
    }

    public List<String> matchPlayers(String str, Player self){
        ArrayList<String> matchedPlayers = new ArrayList<String>();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            String name = player.getName();
            if ((self == null || player.canSee(player)) && StringUtil.startsWithIgnoreCase(name, str)) {
                matchedPlayers.add(name);
            }
        }
        matchedPlayers.sort(String.CASE_INSENSITIVE_ORDER);
        return matchedPlayers;
    }

    protected boolean isError(String result) {
        return result.startsWith("Error");
    }

    protected void result(CommandSender sender, String result) {
        if(isError(result)){
            error(sender, result);
        }
        else{
            success(sender, result);
        }
    }
}

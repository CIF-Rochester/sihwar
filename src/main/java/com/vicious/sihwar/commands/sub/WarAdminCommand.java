package com.vicious.sihwar.commands.sub;

import com.vicious.sihwar.SIHWar;
import com.vicious.sihwar.TeamData;
import com.vicious.sihwar.WarConfig;
import com.vicious.sihwar.commands.CommandException;
import com.vicious.sihwar.commands.GameCommand;
import com.vicious.sihwar.commands.GameSetupCommand;
import com.vicious.sihwar.data.GameInstance;
import com.vicious.sihwar.data.GameTemplate;
import com.vicious.sihwar.data.Templates;
import com.vicious.sihwar.game.SpawnFinder;
import com.vicious.sihwar.player.PlayerData;
import com.vicious.sihwar.player.PlayerState;
import com.vicious.sihwar.util.Announcement;
import com.vicious.sihwar.util.Vec2D;
import com.vicious.viciouslib.persistence.PersistenceHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class WarAdminCommand extends GameCommand {
    public WarAdminCommand() {
        super("waradmin", "The all encompassing SIHWAR admin command.", "", List.of("sihwar","war"));
        addSubCommand(new Create());
        addSubCommand(new LoadPaused());
        addSubCommand(new Save());
        addSubCommand(new Start());
        addSubCommand(new Pause());
        addSubCommand(new Reset());
        addSubCommand(new NextStage());
        addSubCommand(new SetPlayerState());
        addSubCommand(new CheckFair());
        addSubCommand(new TeamManage());
        addSubCommand(new HelpCommand(this));
    }

    @Override
    public @Nullable String getPermission() {
        return "sihwar.admin";
    }

    @Override
    protected void process(CommandSender sender, String[] args) throws CommandException {
        error(sender,getUsage());
    }

    private static class Create extends GameCommand {
        protected Create() {
            super("create", "Creates a new battle and saves the instance.", "/waradmin create <name> <optional:world> <optional:template>", List.of("create"));
        }

        @Override
        public int requiresArgs(){
            return 1;
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            if(args.length == 1) {
                playerOnly(sender, p -> {
                    SIHWar.game = new GameInstance(args[0], ((Player) sender).getWorld());
                    success(sender, "Created game " + args[0] + " and set as the current game.");
                    WarConfig.spawnBox.build(p.getWorld());
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        PlayerState.WAITING.apply(onlinePlayer, null, SIHWar.game);
                    }
                });
            }
            else{
                World w = Bukkit.getWorld(args[1]);
                if(w == null){
                    error(sender,"No such world " + args[1]);
                }
                else{
                    GameTemplate template = Templates.templates.get("solos");
                    if(args.length == 3){
                        template = Templates.templates.get(args[2]);
                        if(template == null){
                            error(sender,"No such template: " + args[2]);
                            return;
                        }
                    }
                    SIHWar.game = new GameInstance(args[0],w,template);
                    success(sender, "Created game " + args[0] + " and set as the current game.");
                    WarConfig.spawnBox.build(w);
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        PlayerState.WAITING.apply(onlinePlayer, null, SIHWar.game);
                    }
                }
            }
        }
    }

    private static class LoadPaused extends GameCommand{
        protected LoadPaused() {
            super("load", "Loads a battle as the current one and pauses it.", "/waradmin load <name> <optional:world>", List.of("load","loadpaused"));
        }

        @Override
        public int requiresArgs(){
            return 1;
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            if(args.length == 1){
                playerOnly(sender,p->{
                    SIHWar.game=GameInstance.loadPaused(args[0],p.getWorld());
                    success(sender,"Loaded game " + args[0] + " and set as the current game.");
                });
            }
            else{
                World w = Bukkit.getWorld(args[1]);
                if(w == null){
                    error(sender,"No such world " + args[1]);
                }
                else{
                    SIHWar.game=GameInstance.loadPaused(args[0],w);
                    success(sender,"Loaded game " + args[0] + " and set as the current game.");
                }
            }
        }

        @Override
        public List<String> tabComplete(String[] args, CommandSender sender) {
            return GameInstance.getGameFiles();
        }
    }

    private static class Save extends GameCommand{
        protected Save() {
            super("save", "Saves the current battle.", "/waradmin save", List.of("save"));
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            whenGameLoaded(sender, GameInstance::save);
        }
    }

    private static class Start extends GameSetupCommand {
        protected Start() {
            super("start", "Starts the current battle.", "/waradmin start", List.of("start"));
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            whenGameLoaded(sender, game -> {
                success(sender,"Starting the game!");
                game.start();
                WarConfig.running=true;
                PersistenceHandler.save(WarConfig.class);
            });
        }
    }

    private static class Pause extends GameCommand{
        protected Pause() {
            super("pause", "Pauses and unpauses the current war.", "/waradmin pause", List.of("pause"));
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            whenGameLoaded(sender, GameInstance::togglePaused);
        }
    }

    private static class Reset extends GameCommand {
        protected Reset() {
            super("reset", "Resets the plugin and sets the world spawn at your position", "/waradmin reset", "reset");
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            SIHWar.game=null;
            WarConfig.running=false;
            PersistenceHandler.save(WarConfig.class);
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.getInventory().clear();
                onlinePlayer.setHealth(0D);
                onlinePlayer.setGameMode(GameMode.ADVENTURE);
            }
            success(sender,"Reset the plugin.");
        }
    }

    private static class NextStage extends GameCommand {
        protected NextStage(){
            super("nextstage", "Progresses the game to the next stage", "/waradmin nextstage", "nextstage");
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            whenGameLoaded(sender,game->{
                Announcement.of(Component.text("Forcing the game to the next stage!").color(NamedTextColor.LIGHT_PURPLE).style(Style.style(TextDecoration.BOLD)), Sound.ENTITY_ENDER_DRAGON_GROWL).send();
                game.nextStage();
            });
        }
    }

    private static class SetPlayerState extends GameCommand {
        protected SetPlayerState(){
            super("setplayerstate", "Sets a player's alive state.", "/waradmin setplayerstate <alive,dead>", "setplayerstate");
        }

        @Override
        public int requiresArgs() {
            return 2;
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            try {
                PlayerState state = PlayerState.valueOf(args[0].toUpperCase());
                whenGameLoaded(sender,game->{
                    for (int i = 1; i < args.length; i++) {
                        whenCached(sender,args[i],player->{
                            PlayerData data = game.playerData.get(player);
                            if(data == null){
                                error(sender,"That player is not participating");
                            }
                            else{
                                data.setState(state);
                            }
                        });
                    }
                });
            } catch (Exception e){
                error(sender,"No such player state: " + args[0]);
            }
        }

        @Override
        public List<String> tabComplete(String[] args, CommandSender sender) {
            return List.of("alive","dead");
        }
    }

    private static class TeamManage extends GameCommand {
        protected TeamManage(){
            super("team","Allows Managing Teams.", "/waradmin team <option>","team");
            addSubCommand(new TeamManageForceAdd());
            addSubCommand(new TeamManageForceRemove());
            addSubCommand(new TeamManageForceDisband());
            addSubCommand(new HelpCommand(this));
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {

        }
    }

    private static class TeamManageForceAdd extends GameCommand{
        protected TeamManageForceAdd(){
            super("add","Adds a player to a team", "/waradmin team add <team> <players>","add");
        }

        @Override
        public int requiresArgs() {
            return 2;
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            whenGameLoaded(sender, game->{
                TeamData data = game.teams.getTeam(args[0]);
                int i = 1;
                if(data == null){
                    String result = game.settings.teamSelector.processManualCreate(game,null,args[0]);
                    if(isError(result)){
                        error(sender,result);
                        return;
                    }
                    data = game.teams.getTeam(args[0]);
                }
                for(; i < args.length; i++){
                    String name = args[i];
                    PlayerData player = game.playerData.get(name);
                    if(player != null){
                        data.addPlayer(player,game);
                    }
                }
            });
        }
    }

    private static class TeamManageForceDisband extends GameCommand{
        protected TeamManageForceDisband(){
            super("disband","Disbands a team", "/waradmin team disband <team>","disband");
        }

        @Override
        public int requiresArgs() {
            return 1;
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            whenGameLoaded(sender, game->{
                TeamData data = game.teams.getTeam(args[0]);
                if(data == null){
                    error(sender, args[0] + " does not exist");
                }
                else{
                    game.teams.disband(data,game);
                }
            });
        }
    }

    private static class TeamManageForceRemove extends GameCommand{
        protected TeamManageForceRemove(){
            super("remove","Removes a player from a team", "/waradmin team remove <team> <players>","remove");
        }

        @Override
        public int requiresArgs() {
            return 2;
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            whenGameLoaded(sender, game->{
                TeamData data = game.teams.getTeam(args[0]);
                if(data == null){
                    error(sender, args[0] + " does not exist");
                }
                else{
                    for(int i = 1; i < args.length; i++){
                        String name = args[i];
                        PlayerData player = game.playerData.get(name);
                        if(player != null){
                            data.removePlayer(player);
                        }                    
                    }
                }
            });
        }
    }

    private static class CheckFair extends GameCommand {
        protected CheckFair(){
            super("checkfair", "Checks if the world spawns are fair as well as the world's 0,0", "/waradmin checkfair <#teams> <optional:worldname>", "checkfair");
        }

        @Override
        public int requiresArgs() {
            return 1;
        }

        @Override
        protected void process(CommandSender sender, String[] args) throws CommandException {
            World world = getWorld(sender,args);
            String s = args[0];
            try {
                List<Vec2D> spawns = SpawnFinder.findSpawnsCircular(Integer.parseInt(s), world);
                if(!SpawnFinder.isSafe(world.getBiome(0,255,0))){
                    error(sender,"This world has an UNSAFE biome: " + world.getBiome(0,255,0).name() + " at 0,0 new world recommended.");
                }
                else{
                    success(sender,"This world has a safe biome " + world.getBiome(0,255,0).name() + " at 0,0.");
                    if(args.length >= 3){
                        if(sender instanceof Player p ){
                            Vec2D spawn = spawns.get(Integer.parseInt(args[2]));
                            p.teleport(new Location(world,spawn.x, 200,spawn.y));
                        }
                    }
                }
            } catch (Exception e) {
                error(sender, "Not an integer.");
            }
        }

        private World getWorld(CommandSender sender, String[] args) {
            if(sender instanceof Player){
                return ((Player) sender).getWorld();
            }
            else if(args.length > 1){
                String worldName = args[1];
                World w = Bukkit.getWorld(worldName);
                if(w == null){
                    throw new CommandException("No such world: " + worldName);
                }
                else{
                    return w;
                }
            }
            throw new CommandException("When running this command through console, you need to specify a world to target.");
        }

        @Override
        public List<String> tabComplete(String[] args, CommandSender sender) {
            if(args.length == 0){
                return List.of("2","3","4","5","6","7","8","9","10");
            }
            else{
                return Bukkit.getServer().getWorlds().stream().map(World::getName).collect(Collectors.toList());
            }
        }
    }


}

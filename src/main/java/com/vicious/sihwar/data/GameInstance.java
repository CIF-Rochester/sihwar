package com.vicious.sihwar.data;

import com.vicious.sihwar.SIHWar;
import com.vicious.sihwar.TeamData;
import com.vicious.sihwar.WarConfig;
import com.vicious.sihwar.game.Overlays;
import com.vicious.sihwar.game.SpawnFinder;
import com.vicious.sihwar.player.PlayerData;
import com.vicious.sihwar.player.PlayerState;
import com.vicious.sihwar.util.Announcement;
import com.vicious.sihwar.util.Vec2D;
import com.vicious.viciouslib.persistence.IPersistent;
import com.vicious.viciouslib.persistence.storage.aunotamations.PersistentPath;
import com.vicious.viciouslib.persistence.storage.aunotamations.Save;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GameInstance implements IPersistent {
    public static GameInstance loadPaused(String arg, World world) {
        GameInstance out = new GameInstance(arg,world);
        out.paused=true;
        return out;
    }
    private GameStage stage = GameStage.SETUP;

    @Save
    public TeamsList teams = new TeamsList();
    @Save
    public PlayerDataList playerData = new PlayerDataList();
    @Save
    public GameTemplate settings = Templates.templates.get("solos");
    @Save
    public int stageIndex = -1;
    @Save
    public int period = 0;
    @Save
    public boolean paused = false;

    public World world;

    public String name = "";

    @PersistentPath
    public String path = "";

    public GameInstance(String name, World world){
        this.name=name;
        this.path="vicious/games/" + name + ".txt";
        this.world=world;
        load();
        save();
        initialize();
        if(settings.teamSelector.random){
            genTeams();
        }
    }

    public static List<String> getGameFiles() {
        File dir = new File("vicious/games");
        if(!dir.isDirectory()){
            return new ArrayList<>();
        }
        else {
            return Arrays.stream(Objects.requireNonNull(dir.listFiles())).map(f->f.getName().replaceAll(".txt","")).collect(Collectors.toList());
        }
    }

    public void start() {
        nextStage();
        for (TeamData team : teams.getTeams()) {
            team.spawnInitial(this);
        }
        for (PlayerData p : playerData.getPlayerData()) {
            if(!p.hasTeam()){
                p.whenOnline(this::addSpectatorPlayer);
            }
        }
        world.setTime(0);
        world.setClearWeatherDuration(Integer.MAX_VALUE);
        for (World world : Bukkit.getServer().getWorlds()) {
            world.setDifficulty(Difficulty.NORMAL);
        }
        WarConfig.spawnBox.destroy(world);
    }


    /**
     * Sets up the stage variable.
     */
    public void initialize(){
        if(stageIndex == -1) {
            stage = GameStage.SETUP;
        }
        else{
            stage = getStage();
        }
        for (World world : Bukkit.getServer().getWorlds()) {
            world.setDifficulty(Difficulty.PEACEFUL);
        }
    }

    /**
     * Runs every second. Controls the game loop.
     */
    public void tickSecond() {
        for (PlayerData playerDatum : playerData.getPlayerData()) {
            playerDatum.whenOnline(plr->{
                Overlays.update(plr,this);
            });
        }
        if (stage.ticks()) {
            if (stage.announcementIntervals.contains(period)) {
                stage.warning(this);
            }
            period--;
            if (period == 0) {
                nextStage();
            }
        }
        if(stage.flags.contains(GameFlag.COLLAPSE)){
            for (PlayerData p : playerData.getPlayerData()) {
                p.whenOnline(plr->{
                    if(plr.getWorld().getEnvironment() != World.Environment.NORMAL){
                        if (!plr.hasPotionEffect(PotionEffectType.WITHER)) {
                            plr.addPotionEffect(new PotionEffect(PotionEffectType.WITHER,5*20,2));
                            plr.sendMessage("This dimension is collapsing! ESCAPE!");
                        }
                    }
                });
            }
        }
    }

    /**
     * Checks if a team has won, if so ends the game.
     */
    public void checkEnded() {
        if(stage != GameStage.COMPLETE) {
            TeamData winner = getWinner();
            if (winner == null) {
                return;
            }
            forceEnd();
            save();
        }
    }

    /**
     * Returns null if there is no winner, returns the teamdata of the winner if there is one.
     */
    public TeamData getWinner(){
        TeamData winner = null;
        for (TeamData team : teams.getTeams()) {
            if(!team.isDead(this)){
                if(winner == null){
                    winner = team;
                }
                else{
                    return null;
                }
            }
        }
        return winner;
    }

    /**
     * Ends the game. Announces the winner if there is one.
     */
    private void forceEnd() {
        stageIndex = settings.stages.size();
        stage = GameStage.COMPLETE;
        TeamData winner = getWinner();
        if(winner == null){
            for (Player p : Bukkit.getOnlinePlayers()) {
                PlayerData data = playerData.get(p.getUniqueId());
                data.state= PlayerState.DEAD;
                data.spectate(this);
            }
            Announcement.of(Component.text("No one won??? HOW???").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD), Sound.ENTITY_WITHER_DEATH).send().broadcast().log();
        }
        else {
            //bar.setVisible(false);
            Announcement.of(Component.text(winner.name + " has won the game!").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD), Sound.ENTITY_WITHER_DEATH).send().broadcast().log();
            //cutscene();
        }
    }

    /**
     * Increments stage and calls the update function
     */
    public void nextStage(){
        stageIndex++;
        onStageIndexChanged();
    }


    public GameStage getStage() {
        return stage;
    }

    public void updateStage(){
        stage = stageIndex < settings.stages.size() ? GameStages.get(settings.stages.get(stageIndex)) : GameStage.COMPLETE;
    }

    /**
     * Updates the stage, sets the period to the stage length.
     */
    private void onStageIndexChanged() {
        stage.end(this);
        if(stageIndex == 0){
            updateStage();
            period = stage.period;
            updateBorder();
            stage.start(this);
        }
        else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(SIHWar.INSTANCE, () -> {
                updateStage();
                period = stage.period;
                updateBorder();
                stage.start(this);
            }, 40L);
        }
    }

    /**
     * Updates world borders for all dimensions.
     */
    private void updateBorder() {
        for (World world : Bukkit.getServer().getWorlds()) {
            setBorderSize(world);
        }
    }

    /**
     * Sets the border for a world.
     */
    private void setBorderSize(World world){
        if(stage.shrinks()) {
            world.getWorldBorder().setSize(stage.borderEnd*2, period);
        }
        else{
            world.getWorldBorder().setSize(stage.borderEnd*2);
        }
    }

    public void pause() {
        paused=true;
        for (PlayerData data : playerData.getPlayerData()) {
            data.frozen=true;
        }
        Announcement.of(Component.text("The game has been paused!").color(NamedTextColor.GOLD), Sound.BLOCK_SLIME_BLOCK_STEP).send();
        //updateBar();
    }

    public void unpause(){
        paused = false;
        Announcement.of(Component.text("The game has been unpaused!").color(NamedTextColor.GREEN),Sound.ENTITY_PLAYER_LEVELUP).send();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            PlayerData player = playerData.get(onlinePlayer);
            player.applyState(this,onlinePlayer);
        }
        //updateBar();
    }

    public String getTimeString() {
        return convertSeconds(period);
    }

    private static String convertSeconds(long tseconds) {
        int m2s = 60;
        int h2m2s = 60*m2s;
        int d2h2m2s = 24*h2m2s;
        long days = tseconds/d2h2m2s;
        long hours = (tseconds%d2h2m2s)/h2m2s;
        long minutes = ((tseconds%d2h2m2s)%h2m2s)/m2s;
        long seconds = (((tseconds%d2h2m2s))%h2m2s)%m2s;
        StringBuilder out = new StringBuilder();
        if(days > 0){
            out.append(days).append('d').append(' ');
        }
        if(hours > 0){
            out.append(hours).append('h').append(' ');
        }
        if(minutes > 0){
            out.append(minutes).append('m').append(' ');
        }
        if(seconds > 0){
            out.append(seconds).append('s');
        }
        return out.toString().endsWith(" ") ? out.substring(0,out.length()-1) : out.toString();
    }

    public void spectateRandom(Player player) {
        List<TeamData> teams = new ArrayList<>(this.teams.getTeams());
        teams.removeIf(t->t.isDead(this));
        if(teams.size() == 0){
            return;
        }
        TeamData target = teams.get((int) (Math.random()*teams.size()));
        List<OfflinePlayer> players = target.getPlayers();
        players.removeIf(op->!(op instanceof Player));
        if(players.size() != 0){
            Player entity = (Player) players.get((int) (Math.random()*players.size()));
            player.setSpectatorTarget(entity);
        }
    }

    public void addSpectatorPlayer(Player player) {
        PlayerData data = playerData.get(player);
        data.state = PlayerState.DEAD;
    }

    public boolean inSetup() {
        return stage == GameStage.SETUP;
    }

    public boolean inGrace() {
        return stage.inGrace();
    }

    public void togglePaused() {
        if(paused) {
            unpause();
        }
        else {
            pause();
        }
    }

    private int task = -1;
    private List<Vec2D> spawns;
    /**
     * Sets all the team spawns.
     */
    public void genSpawns() {
        if(task != -1){
            Announcement.soundless(Component.text("Cancelling old spawn finder task")).sendPermitted("sihwar.admin");
            Bukkit.getScheduler().cancelTask(task);
        }
        task = Bukkit.getScheduler().scheduleSyncDelayedTask(SIHWar.INSTANCE,()->{
            Announcement.soundless(Component.text("Finding spawns...")).sendPermitted("sihwar.admin");
            spawns = SpawnFinder.findSpawnsCircular(teams.size(),world);
            randomizeSpawns();
            task = -1;
        });
    }

    private void randomizeSpawns(){
        List<TeamData> teams = new ArrayList<>(this.teams.getTeams());
        int j = 0;
        while(!teams.isEmpty()){
            if(j >= spawns.size()){
                return;
            }
            int i = (int) (Math.random()*teams.size());
            teams.get(i).setSpawn(spawns.get(j));
            teams.remove(i);
            j++;
        }
    }

    public void genTeams() {
        settings.teamSelector.generate(this,false);
        genSpawns();
    }
}

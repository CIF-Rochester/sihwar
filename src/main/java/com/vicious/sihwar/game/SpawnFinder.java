package com.vicious.sihwar.game;

import com.vicious.sihwar.WarConfig;
import com.vicious.sihwar.util.Announcement;
import com.vicious.sihwar.util.Vec2D;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpawnFinder {

    /**
     * Finds a safe spawn point in a somewhat circular area for every team. Alerts if the world is unsafe.
     */
    public static List<Vec2D> findSpawnsCircular(int teams, World world){
        double borderRadius = WarConfig.spawnEdge;
        double radius32 = borderRadius/32;
        double radiusMax = radius32*30;
        double radiusMin = radius32*26;
        double radiansMax = 2*Math.PI;
        double safeRegionSize = radiusMax-radiusMin;
        double radiusSubtraction = Math.max(1,safeRegionSize/100L);
        double teamDistanceRads = radiansMax/teams;
        double radiansShift = radiansMax/360;

        List<Vec2D> out = new ArrayList<>();
        double shiftRadians = Math.random()*radiansMax;
        l1: while(shiftRadians <= radiansMax) {
            for (int i = 0; i < teams; i++) {
                double r = radiusMax;
                int x = blockXCircle(teamDistanceRads*i+shiftRadians,r);
                int z = blockZCircle(teamDistanceRads*i+shiftRadians,r);
                while(!isSafe(world,x,z)){
                    if(r < radiusMin){
                        out.clear();
                        shiftRadians+=radiansShift;
                        continue l1;
                    }
                    x = blockXCircle(teamDistanceRads*i+shiftRadians,r);
                    z = blockZCircle(teamDistanceRads*i+shiftRadians,r);
                    r-=radiusSubtraction;
                }
                out.add(new Vec2D(x,z));
            }
            break;
        }
        if(out.size() < teams){
            Announcement.of(Component.text("This world is unfair for the current number of teams!").color(NamedTextColor.RED),Sound.ENTITY_WITHER_DEATH).sendPermitted("sihwar.admin").log();
        }
        else{
            Announcement.of(Component.text("This world is fair for the current number of teams").color(NamedTextColor.GREEN), Sound.BLOCK_NOTE_BLOCK_PLING).sendPermitted("sihwar.admin").log();
        }
        return out;
    }

    private static boolean isSafe(World world,int x, int z) {
        Biome b = world.getBiome(x,255,z);
        if(!isSafe(b)){
            return false;
        }
        return true;
        //Block block = world.getBlockAt(world.getHighestBlockAt(x,z).getLocation().add(0,1,0));
        //return block.getType() == Material.AIR;
    }

    private static int blockXCircle(double radians, double radius){
        return (int) (Math.cos(radians)*radius);
    }
    private static int blockZCircle(double radians, double radius){
        return (int) (Math.sin(radians)*radius);
    }

    /*
    Biomes to consider bad.
     */
    private static final Set<Biome> unsafe = new HashSet<>();
    static {
        for (Biome value : Biome.values()) {
            if(value.name().contains("OCEAN") || value.name().contains("BADLANDS") || value.name().contains("RIVER") || value.name().contains("BEACH") || value.name().contains("STONY")){
                unsafe.add(value);
            }
            unsafe.add(Biome.DESERT);
            unsafe.add(Biome.ICE_SPIKES);
            unsafe.add(Biome.SNOWY_PLAINS);
        }
    }
    public static boolean isSafe(Biome b) {
        return !unsafe.contains(b);
    }

    private static Biome getBiome(Location location) {
        return location.getWorld().getBiome(location);
    }

}



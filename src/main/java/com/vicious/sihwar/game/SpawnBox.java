package com.vicious.sihwar.game;

import com.vicious.viciouslib.persistence.storage.aunotamations.Save;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

public class SpawnBox {
    @Save(description = "The floor position of the spawnbox.")
    public int y = 200;

    @Save(description = "The box radius")
    public int r = 7;

    @Save(description = "The box height")
    public int height = 7;

    @Save(description = "The box block type")
    public Material material = Material.BARRIER;

    public void build(World world){
        setSpawn(world,y);
        generate(world,r,height,r,y,material.createBlockData());
    }

    public void destroy(World world){
        generate(world,r,height,r,y,Material.AIR.createBlockData());
    }
    private static void setSpawn(World world, int ybottom){
        world.setSpawnLocation(0,ybottom+1,0);
    }
    private static void generate(World world, int radiusx, int heighty, int radiusz, int ybottom, BlockData data){
        //Floor
        for (int x = -radiusx; x <= radiusx; x++) {
            for (int z = -radiusz; z < radiusz; z++) {
               world.setBlockData(x,ybottom,z, data);
            }
        }
        //Ceiling
        heighty=heighty+ybottom;
        for (int x = -radiusx; x <= radiusx; x++) {
            for (int z = -radiusz; z < radiusz; z++) {
                world.setBlockData(x,heighty,z, data);
            }
        }
        //x walls
        for (int z = -radiusz; z < radiusz; z++) {
            for (int y = ybottom; y < heighty; y++) {
                world.setBlockData(-radiusx,y,z, data);
                world.setBlockData(radiusx,y,z, data);
            }
        }
        //z walls
        for (int x = -radiusx; x < radiusx; x++) {
            for (int y = ybottom; y < heighty; y++) {
                world.setBlockData(x,y,-radiusz, data);
                world.setBlockData(x,y,radiusx, data);
            }
        }
    }
}

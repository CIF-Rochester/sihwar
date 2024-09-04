package com.vicious.sihwar;

import com.vicious.sihwar.game.SpawnBox;
import com.vicious.viciouslib.persistence.PersistenceHandler;
import com.vicious.viciouslib.persistence.storage.aunotamations.PersistentPath;
import com.vicious.viciouslib.persistence.storage.aunotamations.Save;

public class WarConfig {
    @PersistentPath
    public static String path = "plugins/config/SIHWar.cfg";

    @Save(description = "Whether to resurrect people during the grace period")
    public static boolean graceResurrection = true;

    @Save(description = "The Game save interval in seconds")
    public static long saveIntervalSeconds = 300;

    @Save(description = "Whether a game was running. Do not edit this value.")
    public static boolean running = false;

    @Save(description = "The waiting box.")
    public static SpawnBox spawnBox = new SpawnBox();
    @Save(description = "The edge of player spawns. Keep this as the starting stage world border.")
    public static int spawnEdge = 2300;

    public static void init() {
        SIHWar.logger.info("Loading config");
        PersistenceHandler.init(WarConfig.class);
    }
}

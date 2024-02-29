package com.vicious.sihwar.data;

import com.vicious.sihwar.player.PlayerData;
import com.vicious.viciouslib.persistence.storage.aunotamations.Save;
import com.vicious.viciouslib.persistence.storage.aunotamations.Typing;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataList {
    @Save
    @Typing({UUID.class, PlayerData.class})
    public Map<UUID, PlayerData> players = new HashMap<>();

    public PlayerData get(UUID uniqueId) {
        return players.computeIfAbsent(uniqueId, PlayerData::new);
    }

    public Collection<PlayerData> getPlayerData() {
        return players.values();
    }

    public PlayerData get(OfflinePlayer player) {
        return get(player.getUniqueId());
    }

    public PlayerData get(String name) {
        OfflinePlayer plr = Bukkit.getOfflinePlayer(name);
        if(plr == null){
            return null;
        }
        return get(plr);
    }
}

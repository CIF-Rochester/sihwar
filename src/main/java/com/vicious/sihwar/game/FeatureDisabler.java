package com.vicious.sihwar.game;

import com.vicious.sihwar.SIHWar;
import com.vicious.sihwar.data.GameFlag;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class FeatureDisabler implements Listener {
    @EventHandler
    public void onPotion(EntityPotionEffectEvent event){
        if(event.getNewEffect() != null && event.getNewEffect().getType() == PotionEffectType.INCREASE_DAMAGE) {
            if (SIHWar.hasGame() && !SIHWar.game.getStage().flags.contains(GameFlag.STRENGTH)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEndPortalOpen(PlayerInteractEvent event){
        if(event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.END_PORTAL_FRAME){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEGap(PlayerItemConsumeEvent event){
        ItemStack stack = event.getItem();
        Player player = event.getPlayer();
        if(stack.getType() == Material.ENCHANTED_GOLDEN_APPLE){
            event.setCancelled(true);
            player.getInventory().setItem(event.getHand(),player.getInventory().getItem(event.getHand()).subtract());
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,20*5,0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,20*2*60,0));
        }
    }
}

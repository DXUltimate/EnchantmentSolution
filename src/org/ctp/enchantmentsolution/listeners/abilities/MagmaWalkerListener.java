package org.ctp.enchantmentsolution.listeners.abilities;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.ctp.enchantmentsolution.EnchantmentSolution;
import org.ctp.enchantmentsolution.enchantments.DefaultEnchantments;
import org.ctp.enchantmentsolution.enchantments.Enchantments;
import org.ctp.enchantmentsolution.utils.LocationUtils;

public class MagmaWalkerListener extends EnchantmentListener implements Runnable{
	
	public static List<Block> BLOCKS = new ArrayList<Block>();

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		if(!canRun(DefaultEnchantments.MAGMA_WALKER, event)) return;
		Player player = event.getPlayer();
		Location loc = player.getLocation();
		if(player.isFlying() || player.isGliding() || player.isInsideVehicle() || !(player.isOnGround())){
			return;
		}
		ItemStack boots = player.getInventory().getBoots();
		if(event.getTo().getX() != loc.getX() && event.getTo().getZ() != loc.getZ()){
			if(boots != null){
				if(Enchantments.hasEnchantment(boots, DefaultEnchantments.MAGMA_WALKER)){
					int radius = 1 + Enchantments.getLevel(boots, DefaultEnchantments.MAGMA_WALKER);
					for(int x = -radius; x <= radius; x++){
						for(int z = -radius; z <= radius; z++){
							Location lavaLoc = new Location(loc.getWorld(), loc.getBlockX() + x, loc.getBlockY() - 1, loc.getBlockZ() + z);
							Block lava = lavaLoc.getBlock();
							Location aboveLoc = new Location(loc.getWorld(), loc.getBlockX() + x, loc.getBlockY(), loc.getBlockZ() + z);
							if(aboveLoc.getBlock().getType().equals(Material.LEGACY_STATIONARY_LAVA) || aboveLoc.getBlock().getType().equals(Material.LAVA)){
								continue;
							}
							if((lava.getType().equals(Material.LEGACY_STATIONARY_LAVA) || lava.getType().equals(Material.LAVA)) && lava.getData() == 0){
								lava.setMetadata("MagmaWalker", new FixedMetadataValue(EnchantmentSolution.PLUGIN, new Integer(4)));
								lava.setType(Material.LEGACY_MAGMA);
								MagmaWalkerListener.BLOCKS.add(lava);
							}else if(lava.getType().equals(Material.LEGACY_MAGMA)){
								List<MetadataValue> values = lava.getMetadata("MagmaWalker");
								if(values != null){
									for(MetadataValue value : values){
										if(value.asInt() > 0){
											lava.setMetadata("MagmaWalker", new FixedMetadataValue(EnchantmentSolution.PLUGIN, new Integer(4)));
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent event) {
		if(!canRun(DefaultEnchantments.MAGMA_WALKER, event)) return;
		if(event.getCause().equals(DamageCause.HOT_FLOOR)) {
			Entity entity = event.getEntity();
			if(entity instanceof LivingEntity) {
				LivingEntity player = (LivingEntity) entity;
				ItemStack boots = player.getEquipment().getBoots();
				if(boots != null && Enchantments.hasEnchantment(boots, DefaultEnchantments.MAGMA_WALKER)) {
					event.setCancelled(true);
				}
			}
		}
	}

	public void run() {
		for(int i = BLOCKS.size() - 1; i >= 0; i--){
			Block block = BLOCKS.get(i);
			List<MetadataValue> values = block.getMetadata("MagmaWalker");
			if(values != null){
				for(MetadataValue value : values){
					if(value.asInt() > 0){
						boolean update = true;
						for(Player player : Bukkit.getOnlinePlayers()){
							if(!DefaultEnchantments.isEnabled(DefaultEnchantments.MAGMA_WALKER)) continue;
							ItemStack boots = player.getInventory().getBoots();
							if(boots != null){
								update = false;
								if(Enchantments.hasEnchantment(boots, DefaultEnchantments.MAGMA_WALKER)){
									int radius = 2 + Enchantments.getLevel(boots, DefaultEnchantments.MAGMA_WALKER);
									Location locMin = new Location(player.getWorld(), player.getLocation().getBlockX() - radius, player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ() - radius);
									Location locMax = new Location(player.getWorld(), player.getLocation().getBlockX() + radius, player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ() + radius);
									if(!(LocationUtils.getIntersecting(locMin, locMax, block.getLocation(), block.getLocation()))){
										block.setMetadata("MagmaWalker", new FixedMetadataValue(EnchantmentSolution.PLUGIN, new Integer(value.asInt() - 1)));
									}
									BLOCKS.set(i, block);
								}
							}
						}
						if(update){
							block.setMetadata("MagmaWalker", new FixedMetadataValue(EnchantmentSolution.PLUGIN, new Integer(value.asInt() - 1)));
						}
					}else{
						block.removeMetadata("MagmaWalker", EnchantmentSolution.PLUGIN);
						block.setType(Material.LAVA);
						BLOCKS.remove(i);
					}
				}
			}else{
				block.setType(Material.LAVA);
				BLOCKS.remove(i);
			}
		}
	}
}

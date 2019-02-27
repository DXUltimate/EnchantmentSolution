package org.ctp.enchantmentsolution.utils.save;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.ctp.enchantmentsolution.EnchantmentSolution;
import org.ctp.enchantmentsolution.listeners.abilities.MagmaWalkerListener;
import org.ctp.enchantmentsolution.listeners.abilities.VoidWalkerListener;
import org.ctp.enchantmentsolution.utils.config.YamlConfig;

public class SaveUtils {

	public static void getData() {
		if(EnchantmentSolution.getConfigFiles().getWalkerConfig() == null) {
			return;
		}
		YamlConfig config = EnchantmentSolution.getConfigFiles().getWalkerConfig();
		if (config.containsElements("magma_blocks")) {
			int i = 0;
			while (config.getString("magma_blocks." + i) != null) {
				String stringBlock = config.getString("magma_blocks." + i);
				String[] arrayBlock = stringBlock.split(" ");
				try {
					Block block = (new Location(Bukkit.getWorld(arrayBlock[0]),
							Integer.parseInt(arrayBlock[1]),
							Integer.parseInt(arrayBlock[2]),
							Integer.parseInt(arrayBlock[3]))).getBlock();
					block.setMetadata("MagmaWalker", new FixedMetadataValue(EnchantmentSolution.PLUGIN, Integer.parseInt(arrayBlock[4])));
					MagmaWalkerListener.BLOCKS.add(block);
				} catch (Exception ex) {
					Bukkit.getLogger().info(
							"Block at position " + i
									+ " was invalid, skipping.");
				}
				config.removeKey("magma_blocks." + i);
				i++;
			}
			config.removeKey("magma_blocks");
		}
		if(config.containsElements("obsidian_blocks")) {
			int i = 0;
			while (config.getString("obsidian_blocks." + i) != null) {
				String stringBlock = config.getString("obsidian_blocks." + i);
				String[] arrayBlock = stringBlock.split(" ");
				try {
					Block block = (new Location(Bukkit.getWorld(arrayBlock[0]),
							Integer.parseInt(arrayBlock[1]),
							Integer.parseInt(arrayBlock[2]),
							Integer.parseInt(arrayBlock[3]))).getBlock();
					block.setMetadata("VoidWalker", new FixedMetadataValue(EnchantmentSolution.PLUGIN, Integer.parseInt(arrayBlock[4])));
					VoidWalkerListener.BLOCKS.add(block);
				} catch (Exception ex) {
					Bukkit.getLogger().info(
							"Block at position " + i
									+ " was invalid, skipping.");
				}
				config.removeKey("obsidian_blocks." + i);
				i++;
			}
			config.removeKey("obsidian_blocks");
		}
		config.saveConfig();
	}

	public static void setWalkerData() {
		if(EnchantmentSolution.getConfigFiles().getWalkerConfig() == null) {
			return;
		}
		int i = 0;
		YamlConfig config = EnchantmentSolution.getConfigFiles().getWalkerConfig();
		for (Block block : MagmaWalkerListener.BLOCKS) {
			for(MetadataValue value : block.getMetadata("MagmaWalker")){
				config.set("magma_blocks." + i,
						(block.getWorld().getName() + " " + block.getX() + " "
								+ block.getY() + " " + block.getZ() + " " + value.asInt()));
			}
			i++;
		}
		for (Block block : VoidWalkerListener.BLOCKS) {
			for(MetadataValue value : block.getMetadata("VoidWalker")){
				config.set("obsidian_blocks." + i,
						(block.getWorld().getName() + " " + block.getX() + " "
								+ block.getY() + " " + block.getZ() + " " + value.asInt()));
			}
			i++;
		}
		config.saveConfig();
	}
}

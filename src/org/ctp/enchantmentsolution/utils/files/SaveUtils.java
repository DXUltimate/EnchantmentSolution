package org.ctp.enchantmentsolution.utils.files;

import java.util.*;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.ctp.enchantmentsolution.EnchantmentSolution;
import org.ctp.enchantmentsolution.advancements.ESAdvancement;
import org.ctp.enchantmentsolution.advancements.ESAdvancementProgress;
import org.ctp.enchantmentsolution.enchantments.CustomEnchantment;
import org.ctp.enchantmentsolution.enchantments.RegisterEnchantments;
import org.ctp.enchantmentsolution.enchantments.generate.TableEnchantments;
import org.ctp.enchantmentsolution.enchantments.helper.EnchantmentLevel;
import org.ctp.enchantmentsolution.events.blocks.DamageState;
import org.ctp.enchantmentsolution.nms.AnimalMobNMS;
import org.ctp.enchantmentsolution.nms.animalmob.AnimalMob;
import org.ctp.enchantmentsolution.rpg.RPGPlayer;
import org.ctp.enchantmentsolution.rpg.RPGUtils;
import org.ctp.enchantmentsolution.utils.ChatUtils;
import org.ctp.enchantmentsolution.utils.Configurations;
import org.ctp.enchantmentsolution.utils.abilityhelpers.WalkerBlock;
import org.ctp.enchantmentsolution.utils.abilityhelpers.WalkerUtils;
import org.ctp.enchantmentsolution.utils.config.ConfigString;
import org.ctp.enchantmentsolution.utils.yaml.YamlConfig;

public class SaveUtils {

	public static void getData() {
		if (Configurations.getDataFile() == null) return;
		YamlConfig config = Configurations.getDataFile().getConfig();
		if (config.containsElements("advancement_progress")) {
			int i = 0;
			while (config.containsElements("advancement_progress." + i)) {
				try {
					ESAdvancementProgress progress = EnchantmentSolution.getAdvancementProgress(Bukkit.getOfflinePlayer(UUID.fromString(config.getString("advancement_progress." + i + ".player"))), ESAdvancement.valueOf(config.getString("advancement_progress." + i + ".advancement")), config.getString("advancement_progress." + i + ".criteria"));
					progress.setCurrentAmount(config.getInt("advancement_progress." + i + ".current_amount"));
					config.removeKey("advancement_progress." + i);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				i++;
			}
			config.removeKeys("advancement_progress");
		}
		if (config.containsElements("blocks")) {
			int i = 0;
			List<WalkerBlock> blocks = new ArrayList<WalkerBlock>();
			while (config.getString("blocks." + i) != null) {
				String stringBlock = config.getString("blocks." + i);
				String[] arrayBlock = stringBlock.split(" ");
				try {
					Block block = new Location(Bukkit.getWorld(arrayBlock[1]), Integer.parseInt(arrayBlock[2]), Integer.parseInt(arrayBlock[3]), Integer.parseInt(arrayBlock[4])).getBlock();
					Enchantment enchantment = RegisterEnchantments.getByName(arrayBlock[0]).getRelativeEnchantment();
					WalkerBlock walkerBlock = new WalkerBlock(enchantment, block, Material.valueOf(arrayBlock[5]), Integer.parseInt(arrayBlock[6]), DamageState.valueOf(arrayBlock[7]));
					blocks.add(walkerBlock);
				} catch (Exception ex) {
					ChatUtils.sendInfo("Block at position " + i + " was invalid, skipping.");
				}
				i++;
			}
			WalkerUtils.addBlocks(blocks);
			config.removeKeys("blocks");
		}
		if (config.containsElements("animals")) {
			int i = 0;
			while (config.getString("animals." + i + ".entity_type") != null) {
				AnimalMobNMS.getFromConfig(config, i);
				i++;
			}
			config.removeKeys("animals");
		}

		if (!ConfigString.RESET_ON_RELOAD.getBoolean() && config.containsElements("enchanting_table")) {
			int i = 0;
			while (config.getString("enchanting_table." + i + ".player") != null) {
				TableEnchantments.getFromConfig(config, i);
				i++;
			}
		}
		config.removeKeys("enchanting_table");
		if (config.containsElements("rpg")) {
			int i = 0;
			while (config.getString("rpg." + i + ".player") != null) {
				String uuid = config.getString("rpg." + i + ".player");
				int level = config.getInt("rpg." + i + ".level");
				String experience = config.getString("rpg." + i + ".experience");
				RPGPlayer player = RPGUtils.addRPGPlayer(uuid, level, experience);
				List<String> data = config.getStringList("rpg." + i + ".enchants");
				if (data != null) for(String s: config.getStringList("rpg." + i + ".enchants"))
					player.giveEnchantment(s);
				i++;
			}
		}

		Configurations.getDataFile().saveOnLoad();
	}

	public static void setData() {
		if (Configurations.getDataFile() == null) return;
		int i = 0;
		YamlConfig config = Configurations.getDataFile().getConfig();
		for(ESAdvancementProgress progress: EnchantmentSolution.getAdvancementProgress()) {
			config.set("advancement_progress." + i + ".advancement", progress.getAdvancement().name());
			config.set("advancement_progress." + i + ".player", progress.getPlayer().getUniqueId());
			config.set("advancement_progress." + i + ".criteria", progress.getCriteria());
			config.set("advancement_progress." + i + ".current_amount", progress.getCurrentAmount());
			i++;
		}
		i = 0;
		List<WalkerBlock> blocks = WalkerUtils.getBlocks();
		if (blocks != null) for(WalkerBlock block: blocks) {
			Block loc = block.getBlock();
			CustomEnchantment enchantment = RegisterEnchantments.getCustomEnchantment(block.getEnchantment());
			config.set("blocks." + i, enchantment.getName() + " " + loc.getWorld().getName() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ() + " " + block.getReplaceType().name() + " " + block.getTick() + " " + block.getDamage().name());
			i++;
		}
		i = 0;
		try {
			for(AnimalMob animal: EnchantmentSolution.getAnimals()) {
				animal.setConfig(config, i);
				i++;
			}
		} catch (NoClassDefFoundError ex) {
			ex.printStackTrace();
		}

		if (!ConfigString.RESET_ON_RELOAD.getBoolean()) {
			i = 0;
			try {
				for(TableEnchantments table: TableEnchantments.getAllTableEnchantments()) {
					table.setConfig(config, i);
					i++;
				}
			} catch (NoClassDefFoundError ex) {
				ex.printStackTrace();
			}
		}
		i = 0;
		List<RPGPlayer> players = RPGUtils.getPlayers();
		if (players != null) for(RPGPlayer player: players) {
			config.set("rpg." + i + ".player", player.getPlayer().getUniqueId().toString());
			config.set("rpg." + i + ".level", player.getLevel());
			config.set("rpg." + i + ".experience", player.getExperience().toString());
			List<String> enchants = new ArrayList<String>();
			Iterator<Entry<Enchantment, Integer>> iterator = player.getEnchantmentList().entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Enchantment, Integer> entry = iterator.next();
				EnchantmentLevel level = new EnchantmentLevel(RegisterEnchantments.getCustomEnchantment(entry.getKey()), entry.getValue());
				enchants.add(level.toString());
			}
			config.set("rpg." + i + ".enchants", enchants);
			i++;
		}

		Configurations.getDataFile().save();
	}
}

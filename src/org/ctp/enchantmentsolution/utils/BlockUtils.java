package org.ctp.enchantmentsolution.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Snow;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.ctp.enchantmentsolution.EnchantmentSolution;
import org.ctp.enchantmentsolution.advancements.ESAdvancement;
import org.ctp.enchantmentsolution.enums.BlockSound;
import org.ctp.enchantmentsolution.enums.ItemBreakType;
import org.ctp.enchantmentsolution.enums.MatData;
import org.ctp.enchantmentsolution.events.blocks.BlockBreakMultiEvent;
import org.ctp.enchantmentsolution.utils.config.ConfigString;
import org.ctp.enchantmentsolution.utils.items.AbilityUtils;
import org.ctp.enchantmentsolution.utils.items.DamageUtils;
import org.ctp.enchantmentsolution.utils.items.ItemUtils;

public class BlockUtils {

	private static List<Location> MULTI_BLOCK_BREAK = new ArrayList<Location>();

	public static boolean multiBlockBreakContains(Location loc) {
		return MULTI_BLOCK_BREAK.contains(loc);
	}

	public static void addMultiBlockBreak(Location loc) {
		MULTI_BLOCK_BREAK.add(loc);
	}

	public static void removeMultiBlockBreak(Location loc) {
		MULTI_BLOCK_BREAK.remove(loc);
	}

	public static boolean isAdjacent(Block b1, Block b2) {
		List<BlockFace> faces = Arrays.asList(BlockFace.values());
		if (!ConfigString.MULTI_BLOCK_ALL_FACES.getBoolean()) Arrays.asList(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN);
		for(BlockFace face: faces)
			if (b1.getRelative(face).getLocation().equals(b2.getLocation())) return true;
		return false;
	}

	public static boolean multiBreakBlock(Player player, ItemStack item, Location b) {
		BlockBreakEvent newEvent = new BlockBreakMultiEvent(b.getBlock(), player);
		int exp = 0;
		if (!ItemUtils.hasEnchantment(item, Enchantment.SILK_TOUCH)) switch (newEvent.getBlock().getType().name()) {
			case "COAL_ORE":
				exp = (int) (Math.random() * 3);
				break;
			case "DIAMOND_ORE":
			case "EMERALD_ORE":
				exp = (int) (Math.random() * 5) + 3;
				break;
			case "LAPIS_ORE":
			case "NETHER_QUARTZ_ORE":
				exp = (int) (Math.random() * 4) + 2;
				break;
			case "REDSTONE_ORE":
				exp = (int) (Math.random() * 5) + 1;
				break;
			case "SPAWNER":
				exp = (int) (Math.random() * 29) + 15;
				break;
		}
		newEvent.setExpToDrop(exp);
		Bukkit.getServer().getPluginManager().callEvent(newEvent);
		if (item != null && !MatData.isAir(item.getType()) && !MatData.isAir(newEvent.getBlock().getType()) && !newEvent.isCancelled()) {
			Block newBlock = newEvent.getBlock();
			AdvancementUtils.awardCriteria(player, ESAdvancement.FAST_AND_FURIOUS, "diamond_pickaxe");
			if (newBlock.getType().equals(Material.SNOW) && ItemBreakType.getType(item.getType()).getBreakTypes().contains(Material.SNOW)) {
				int num = ((Snow) newBlock.getBlockData()).getLayers();
				ItemUtils.dropItem(new ItemStack(Material.SNOWBALL, num), newBlock.getLocation());
			}
			player.incrementStatistic(Statistic.MINE_BLOCK, newBlock.getType());
			player.incrementStatistic(Statistic.USE_ITEM, item.getType());
			Location loc = newBlock.getLocation().clone().add(0.5, 0.5, 0.5);
			if (ConfigString.USE_PARTICLES.getBoolean()) loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 20, newBlock.getBlockData());
			if (ConfigString.PLAY_SOUND.getBoolean()) {
				BlockSound sound = BlockSound.getSound(newBlock.getType());
				loc.getWorld().playSound(loc, sound.getSound(), sound.getVolume(newBlock.getType()), sound.getPitch(newBlock.getType()));
			}
			newBlock.breakNaturally(item);
			AbilityUtils.dropExperience(loc, newEvent.getExpToDrop());
			DamageUtils.damageItem(player, item);
			EnchantmentSolution.getESPlayer(player).breakBlock();
			BlockUtils.removeMultiBlockBreak(b);
			return true;
		}
		BlockUtils.removeMultiBlockBreak(b);
		return false;
	}
}

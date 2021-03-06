package org.ctp.enchantmentsolution.utils.items;

import java.util.*;
import java.util.Map.Entry;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;
import org.ctp.enchantmentsolution.enchantments.CustomEnchantment;
import org.ctp.enchantmentsolution.enchantments.CustomEnchantmentWrapper;
import org.ctp.enchantmentsolution.enchantments.RegisterEnchantments;
import org.ctp.enchantmentsolution.enchantments.helper.EnchantmentLevel;
import org.ctp.enchantmentsolution.enums.*;
import org.ctp.enchantmentsolution.events.ItemAddEvent;
import org.ctp.enchantmentsolution.events.ItemEquipEvent;
import org.ctp.enchantmentsolution.events.ItemEquipEvent.HandMethod;
import org.ctp.enchantmentsolution.nms.PersistenceNMS;
import org.ctp.enchantmentsolution.utils.Configurations;
import org.ctp.enchantmentsolution.utils.ESArrays;
import org.ctp.enchantmentsolution.utils.compatibility.MMOUtils;
import org.ctp.enchantmentsolution.utils.config.ConfigString;

public class ItemUtils {

	public static void giveItemsToPlayer(Player player, Collection<ItemStack> drops, Location fallback,
	boolean statistic) {
		giveItemsToPlayer(player, drops, fallback, statistic, HandMethod.COMMAND);
	}

	public static void giveItemsToPlayer(Player player, Collection<ItemStack> drops, Location fallback,
	boolean statistic, HandMethod method) {
		for(ItemStack drop: drops)
			giveItemToPlayer(player, drop, fallback, statistic, method);
	}

	public static void giveItemToPlayer(Player player, ItemStack item, Location fallback, boolean statistic) {
		giveItemToPlayer(player, item, fallback, statistic, HandMethod.COMMAND);
	}

	public static void giveItemToPlayer(Player player, ItemStack item, Location fallback, boolean statistic, HandMethod method) {
		int addedAmount = 0;
		addedAmount += addItems(player, item, method, false);
		if (item.getAmount() > 0) addedAmount += addItems(player, item, method, true);
		Location fallbackClone = fallback.clone();
		boolean dropNaturally = Configurations.getConfig().getBoolean("drop_items_naturally");
		if (item.getAmount() > 0 && !dropNaturally) {
			Item droppedItem = fallbackClone.getWorld().dropItem(fallbackClone, item);
			droppedItem.setVelocity(new Vector(0, 0, 0));
			droppedItem.teleport(fallbackClone);
		} else if (item.getAmount() > 0) fallbackClone.getWorld().dropItemNaturally(fallbackClone, item);
		if (addedAmount > 0 && statistic) player.incrementStatistic(Statistic.PICKUP, item.getType(), addedAmount);
	}
	
	private static int addItems(Player player, ItemStack item, HandMethod method, boolean empty) {
		int addedAmount = 0;
		for(int i = 0; i < 36; i++) {
			ItemStack prevItem = null;
			if (player.getInventory().getItem(i) != null) prevItem = player.getInventory().getItem(i).clone();
			
			if (empty && (prevItem == null || MatData.isAir(prevItem.getType())) || !empty && prevItem != null && prevItem.isSimilar(item)) {
				Event event = null;
				ItemStack finalItem = player.getInventory().getItem(i);
				if (finalItem == null || MatData.isAir(finalItem.getType())) {
					finalItem = item.clone();
					addedAmount += item.getAmount();
					item.setAmount(0);
					player.getInventory().setItem(i, finalItem);
				} else {
					int amount = Math.min(prevItem.getType().getMaxStackSize(), prevItem.getAmount() + item.getAmount());
					int leftover = prevItem.getAmount() + item.getAmount() - amount;
					addedAmount += item.getAmount() - amount;
					item.setAmount(leftover);
					finalItem.setAmount(amount);
				}
				if (i == player.getInventory().getHeldItemSlot()) event = new ItemEquipEvent(player, method, ItemSlotType.MAIN_HAND, prevItem, finalItem);
				else
					event = new ItemAddEvent(player, finalItem);
				Bukkit.getPluginManager().callEvent(event);
			}
		}
		return addedAmount;
	}

	public static void dropItems(Collection<ItemStack> drops, Location loc) {
		for(ItemStack drop: drops)
			dropItem(drop, loc);
	}

	public static void dropItem(ItemStack item, Location loc) {
		Location location = loc.clone();
		if (!ConfigString.DROP_ITEMS_NATURALLY.getBoolean()) {
			Item droppedItem = location.getWorld().dropItem(location, item);
			droppedItem.setVelocity(new Vector(0, 0, 0));
			droppedItem.teleport(location);
		} else
			location.getWorld().dropItemNaturally(location, item);
	}

	public static ItemStack convertToEnchantedBook(ItemStack item) {
		ItemStack newItem = new ItemStack(Material.ENCHANTED_BOOK, item.getAmount());
		EnchantmentStorageMeta enchantmentStorage = (EnchantmentStorageMeta) newItem.getItemMeta();

		ItemMeta meta = item.getItemMeta();
		List<EnchantmentLevel> newLevels = new ArrayList<EnchantmentLevel>();
		if (meta != null && meta.getEnchants().size() > 0) {
			for(Iterator<Entry<Enchantment, Integer>> it = meta.getEnchants().entrySet().iterator(); it.hasNext();) {
				Entry<Enchantment, Integer> e = it.next();
				Enchantment enchant = e.getKey();
				int level = e.getValue();
				enchantmentStorage.addStoredEnchant(enchant, level, true);
				if (enchant instanceof CustomEnchantmentWrapper) newLevels.add(new EnchantmentLevel(RegisterEnchantments.getCustomEnchantment(enchant), level));
				meta.removeEnchant(enchant);
			}
			meta = enchantmentStorage;
			newItem.setItemMeta(meta);
			for(EnchantmentLevel level: newLevels)
				PersistenceNMS.addEnchantment(newItem, new EnchantmentLevel(level.getEnchant(), level.getLevel()));
		}
		return newItem;
	}

	public static ItemStack convertToRegularBook(ItemStack item) {
		ItemStack newItem = new ItemStack(Material.BOOK, item.getAmount());
		EnchantmentStorageMeta enchantmentStorage = (EnchantmentStorageMeta) item.getItemMeta();

		ItemMeta meta = newItem.getItemMeta();
		List<EnchantmentLevel> newLevels = new ArrayList<EnchantmentLevel>();

		if (enchantmentStorage != null && enchantmentStorage.getStoredEnchants().size() > 0) {
			for(Iterator<Entry<Enchantment, Integer>> it = enchantmentStorage.getStoredEnchants().entrySet().iterator(); it.hasNext();) {
				Entry<Enchantment, Integer> e = it.next();
				Enchantment enchant = e.getKey();
				int level = e.getValue();
				meta.addEnchant(enchant, level, true);
				if (enchant instanceof CustomEnchantmentWrapper) newLevels.add(new EnchantmentLevel(RegisterEnchantments.getCustomEnchantment(enchant), level));
				enchantmentStorage.removeStoredEnchant(enchant);
			}
			newItem.setItemMeta(meta);
			for(EnchantmentLevel level: newLevels)
				PersistenceNMS.addEnchantment(newItem, new EnchantmentLevel(level.getEnchant(), level.getLevel()));
		}
		return newItem;
	}

	public static List<EnchantmentLevel> getEnchantmentLevels(ItemStack item) {
		List<EnchantmentLevel> levels = new ArrayList<EnchantmentLevel>();
		if (item.getItemMeta() != null) {
			ItemMeta meta = item.getItemMeta();
			Map<Enchantment, Integer> enchantments = meta.getEnchants();
			if (item.getType() == Material.ENCHANTED_BOOK) enchantments = ((EnchantmentStorageMeta) meta).getStoredEnchants();
			for(Iterator<Entry<Enchantment, Integer>> it = enchantments.entrySet().iterator(); it.hasNext();) {
				Entry<Enchantment, Integer> e = it.next();
				levels.add(new EnchantmentLevel(RegisterEnchantments.getCustomEnchantment(e.getKey()), e.getValue()));
			}
		}
		return levels;
	}

	public static boolean isEnchantable(ItemStack item) {
		if (item == null) return false;
		ItemMeta meta = item.getItemMeta();
		if (meta.hasEnchants() || item.getType() == Material.ENCHANTED_BOOK && ((EnchantmentStorageMeta) meta).hasStoredEnchants()) return false;
		if (ItemData.contains(ItemType.getAllEnchantMaterials(), item.getType())) return true;
		if (item.getType().equals(Material.BOOK)) return true;
		return false;
	}

	public static ItemStack addEnchantmentsToItem(ItemStack item, List<EnchantmentLevel> levels) {
		if (levels == null) return item;
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return item;
		List<EnchantmentLevel> remove = new ArrayList<EnchantmentLevel>();
		for(EnchantmentLevel level: levels) {
			if (level == null || level.getEnchant() == null) continue;
			if (level.getLevel() < 1) {
				remove.add(level);
				continue;
			}
			if (item.getType() == Material.ENCHANTED_BOOK) ((EnchantmentStorageMeta) meta).addStoredEnchant(level.getEnchant().getRelativeEnchantment(), level.getLevel(), true);
			else
				meta.addEnchant(level.getEnchant().getRelativeEnchantment(), level.getLevel(), true);
			item.setItemMeta(meta);
			if (!item.getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS) && level.getEnchant().getRelativeEnchantment() instanceof CustomEnchantmentWrapper) {
				PersistenceNMS.removeEnchantment(item, level.getEnchant());
				PersistenceNMS.addEnchantment(item, level);
			}
			meta = item.getItemMeta();
		}
		item.setItemMeta(meta);
		for (EnchantmentLevel level : remove)
			removeEnchantmentFromItem(item, level.getEnchant());
		return item;
	}

	public static ItemStack addEnchantmentToItem(ItemStack item, CustomEnchantment enchantment, int level) {
		return addEnchantmentsToItem(item, Arrays.asList(new EnchantmentLevel(enchantment, level)));
	}

	public static ItemStack removeEnchantmentFromItem(ItemStack item, CustomEnchantment enchantment) {
		if (enchantment == null) return item;
		if (enchantment instanceof CustomEnchantment) PersistenceNMS.removeEnchantment(item, enchantment);
		ItemMeta meta = item.getItemMeta();
		if (hasEnchantment(item, enchantment.getRelativeEnchantment()) && meta instanceof EnchantmentStorageMeta) ((EnchantmentStorageMeta) meta).removeStoredEnchant(enchantment.getRelativeEnchantment());
		else if (hasEnchantment(item, enchantment.getRelativeEnchantment())) meta.removeEnchant(enchantment.getRelativeEnchantment());
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack removeAllEnchantments(ItemStack item, boolean removeCurses) {
		for(CustomEnchantment enchantment: RegisterEnchantments.getEnchantments())
			if (!removeCurses || !enchantment.isCurse()) item = removeEnchantmentFromItem(item, enchantment);
		return item;
	}

	public static boolean hasEnchantment(ItemStack item, Enchantment enchant) {
		if (item.getItemMeta() != null) {
			Map<Enchantment, Integer> enchantments = item.getItemMeta().getEnchants();
			if (item.getType() == Material.ENCHANTED_BOOK) enchantments = ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants();
			for(Iterator<Entry<Enchantment, Integer>> it = enchantments.entrySet().iterator(); it.hasNext();) {
				Entry<Enchantment, Integer> e = it.next();
				if (e.getKey().equals(enchant)) return true;
			}
		}
		return false;
	}

	public static int getTotalEnchantments(ItemStack item) {
		if (item.getItemMeta() != null) {
			ItemMeta meta = item.getItemMeta();
			Map<Enchantment, Integer> enchantments = meta.getEnchants();
			if (item.getType() == Material.ENCHANTED_BOOK) enchantments = ((EnchantmentStorageMeta) meta).getStoredEnchants();
			if (enchantments == null) return 0;
			return enchantments.size();
		}
		return 0;
	}

	public static int getLevel(ItemStack item, Enchantment enchant) {
		if (item.getItemMeta() != null) {
			ItemMeta meta = item.getItemMeta();
			Map<Enchantment, Integer> enchantments = meta.getEnchants();
			if (item.getType() == Material.ENCHANTED_BOOK) enchantments = ((EnchantmentStorageMeta) meta).getStoredEnchants();
			for(Iterator<Entry<Enchantment, Integer>> it = enchantments.entrySet().iterator(); it.hasNext();) {
				Entry<Enchantment, Integer> e = it.next();
				if (e.getKey().equals(enchant)) return e.getValue();
			}
		}
		return 0;
	}

	public static boolean canAddEnchantment(CustomEnchantment customEnchant, ItemStack item) {
		ItemMeta meta = item.clone().getItemMeta();
		Map<Enchantment, Integer> enchants = meta.getEnchants();
		if (item.getType().equals(Material.ENCHANTED_BOOK)) enchants = ((EnchantmentStorageMeta) meta).getStoredEnchants();
		else if (!customEnchant.canAnvilItem(new ItemData(item))) return false;
		for(Iterator<Entry<Enchantment, Integer>> it = enchants.entrySet().iterator(); it.hasNext();) {
			Entry<Enchantment, Integer> e = it.next();
			Enchantment enchant = e.getKey();
			for(CustomEnchantment custom: RegisterEnchantments.getRegisteredEnchantments())
				if (custom.getRelativeEnchantment().equals(enchant)) if (CustomEnchantment.conflictsWith(customEnchant, custom) && !customEnchant.equals(custom)) return false;
		}
		return true;
	}

	public static Collection<ItemStack> getSoulboundShulkerBox(Player player, Block block,
	Collection<ItemStack> drops) {
		Iterator<ItemStack> i = drops.iterator();
		Collection<ItemStack> items = new ArrayList<ItemStack>();
		while (i.hasNext()) {
			ItemStack drop = i.next();
			if (ESArrays.getShulkerBoxes().contains(drop.getType())) {
				BlockStateMeta im = (BlockStateMeta) drop.getItemMeta();
				Container container = (Container) block.getState();
				im.setBlockState(container);
				if (block.getMetadata("shulker_name") != null) for(MetadataValue value: block.getMetadata("shulker_name"))
					im.setDisplayName(value.asString());
				drop.setItemMeta(im);
				if (block.getMetadata("soulbound").size() > 0) drop = ItemUtils.addEnchantmentsToItem(drop, Arrays.asList(new EnchantmentLevel(RegisterEnchantments.getCustomEnchantment(RegisterEnchantments.SOULBOUND), 1)));
				items.add(drop);
			}
		}
		return items;
	}

	public static boolean checkItemType(ItemData item, CustomItemType type) {
		if (type.getVanilla() == VanillaItemType.VANILLA) {
			MatData data = new MatData(type.getType().split(":")[1]);
			return data.hasMaterial() && data.getMaterial() == item.getMaterial();
		}
		return MMOUtils.check(item, type);
	}
}

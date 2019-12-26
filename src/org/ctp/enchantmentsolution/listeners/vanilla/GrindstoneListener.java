package org.ctp.enchantmentsolution.listeners.vanilla;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.GrindstoneInventory;
import org.bukkit.inventory.ItemStack;
import org.ctp.enchantmentsolution.enchantments.generate.GrindstoneEnchantments;
import org.ctp.enchantmentsolution.utils.items.AbilityUtils;

public class GrindstoneListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.GRINDSTONE) {
			GrindstoneInventory inv = (GrindstoneInventory) event.getInventory();
			ItemStack first = inv.getItem(0);
			ItemStack second = inv.getItem(1);
			if (event.getWhoClicked() instanceof Player) {
				Player player = (Player) event.getWhoClicked();
				GrindstoneEnchantments ench = GrindstoneEnchantments.getGrindstoneEnchantments(player, first, second);
				if (ench.canCombine() && event.getSlot() == 2 && (event.getCursor() == null || event.getCursor().getType() == Material.AIR)) {
					event.setCancelled(true);
					combine(ench, event.getClick(), inv);
				} else if (ench.canTakeEnchantments() && event.getSlot() == 2 && (event.getCursor() == null || event.getCursor().getType() == Material.AIR)) {
					event.setCancelled(true);
					combine(ench, event.getClick(), inv);
				}
				prepareGrindstone(player, inv);
			}
		}
	}

	private void prepareGrindstone(Player player, GrindstoneInventory inv) {
		ItemStack first = inv.getItem(0);
		ItemStack second = inv.getItem(1);

		GrindstoneEnchantments ench = GrindstoneEnchantments.getGrindstoneEnchantments(player, first, second);

		inv.setItem(2, ench.getCombinedItem());
	}

	private void combine(GrindstoneEnchantments ench, ClickType click, GrindstoneInventory inv) {
		switch (click) {
			case LEFT:
			case RIGHT:
			case SHIFT_RIGHT:
				ench.getPlayer().setItemOnCursor(ench.getCombinedItem());
				inv.setContents(new ItemStack[3]);
				AbilityUtils.dropExperience(inv.getLocation().clone().add(new Location(inv.getLocation().getWorld(), 0.5, 0.5, 0.5)), ench.getExperience());
				break;
			case SHIFT_LEFT:
				HashMap<Integer, ItemStack> items = ench.getPlayer().getInventory().addItem(ench.getCombinedItem());
				if (!items.isEmpty()) return;
				inv.setContents(new ItemStack[3]);
				AbilityUtils.dropExperience(inv.getLocation().clone().add(new Location(inv.getLocation().getWorld(), 0.5, 0.5, 0.5)), ench.getExperience());
				break;
			default:
				break;
		}
	}
}

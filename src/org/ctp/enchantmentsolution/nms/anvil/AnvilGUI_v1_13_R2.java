package org.ctp.enchantmentsolution.nms.anvil;

import java.util.HashMap;

import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.ctp.enchantmentsolution.inventory.Anvil;
import org.ctp.enchantmentsolution.inventory.InventoryData;

import net.minecraft.server.v1_13_R2.*;

public class AnvilGUI_v1_13_R2 extends AnvilGUI {
	private class AnvilContainer extends ContainerAnvil {
		public AnvilContainer(EntityHuman entity) {
			super(entity.inventory, entity.world, new BlockPosition(0, 0, 0), entity);
		}

		@Override
		public boolean canUse(EntityHuman entityhuman) {
			return true;
		}
	}

	private HashMap<AnvilSlot, ItemStack> items = new HashMap<>();

	public AnvilGUI_v1_13_R2(Player player, final AnvilClickEventHandler handler, InventoryData data) {
		super(player, handler, data);
	}

	@Override
	public void setSlot(AnvilSlot slot, ItemStack item) {
		items.put(slot, item);
	}

	@Override
	public void open() {
		EntityPlayer p = ((CraftPlayer) getPlayer()).getHandle();

		AnvilContainer container = new AnvilContainer(p);

		// Set the items to the items from the inventory given
		Inventory inv = container.getBukkitView().getTopInventory();

		for(AnvilSlot slot: items.keySet())
			inv.setItem(slot.getSlot(), items.get(slot));

		inv.setItem(0, getItemStack());

		setInventory(inv);

		// Counter stuff that the game uses to keep track of inventories
		int c = p.nextContainerCounter();

		// Send the packet
		p.playerConnection.sendPacket(new PacketPlayOutOpenWindow(c, "minecraft:anvil", new ChatMessage("Repairing"), 0));
		// Set their active container to the container
		p.activeContainer = container;

		// Set their active container window id to that counter stuff
		p.activeContainer.windowId = c;

		// Add the slot listener
		p.activeContainer.addSlotListener(p);
	}

	public static void createAnvil(Player player, InventoryData data) {
		AnvilClickEventHandler handler = AnvilClickEventHandler.getHandler(player, data);
		if (data instanceof Anvil) ((Anvil) data).setInLegacy(true);
		AnvilGUI_v1_13_R2 gui = new AnvilGUI_v1_13_R2(player, handler, data);
		gui.open();
	}

}

package org.ctp.enchantmentsolution.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.ctp.enchantmentsolution.EnchantmentSolution;
import org.ctp.enchantmentsolution.enchantments.helper.EnchantmentLevel;
import org.ctp.enchantmentsolution.rpg.RPGPlayer;
import org.ctp.enchantmentsolution.rpg.RPGUtils;
import org.ctp.enchantmentsolution.threads.SnapshotThread;
import org.ctp.enchantmentsolution.utils.config.ConfigString;
import org.ctp.enchantmentsolution.utils.player.ESPlayer;

public class GlobalPlayerListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerLogin(PlayerLoginEvent event) {
		Player player = event.getPlayer();
		if (ConfigString.ENCHANTMENT_CHECK_ON_LOGIN.getBoolean()) Bukkit.getScheduler().runTaskLater(EnchantmentSolution.getPlugin(), () -> {
			SnapshotThread.updateInventory(player);
		}, 1l);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		ESPlayer esPlayer = EnchantmentSolution.getESPlayer(event.getPlayer());
		Bukkit.getScheduler().runTaskLater(EnchantmentSolution.getPlugin(), () -> {
			esPlayer.reloadPlayer();
			RPGPlayer rpg = RPGUtils.getPlayer(esPlayer.getPlayer());
			rpg.giveEnchantment((EnchantmentLevel) null);
		}, 0l);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		ESPlayer esPlayer = EnchantmentSolution.getESPlayer(player);
		if (esPlayer != null && esPlayer.canFly(false)) esPlayer.logoutFlyer();
	}

}

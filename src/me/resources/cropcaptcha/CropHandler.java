package me.resources.cropcaptcha;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class CropHandler implements Listener {

	private static Main plugin;
	public CropHandler(Main instance) {
		plugin = instance;
	}
	
	Integer clickedSlot = 0;
	String customItemName = "";
	String itemType = "";
	Integer attempts = 0;
	Boolean applicable = true;
	
	@EventHandler
	public void playerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		String permission = plugin.getConfig().getString("Settings.notify-permission");
		if (plugin.getData().getBoolean("Players." + p.getUniqueId() + ".inGUI")) {
			Collection<? extends Player> online = Bukkit.getOnlinePlayers();
			System.out.println(online.size());
			if (online != null) {
			for (Player p1 : online) {
				if (online.size() != 1) {
					if (p1.hasPermission(permission)) {
						p1.sendMessage(translate(plugin.getConfig().getString("Settings.notify-disconnect").replace("%player%", p.getName())));
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void playerKicked(PlayerKickEvent e) {
		Player p = e.getPlayer();
		String permission = plugin.getConfig().getString("Settings.notify-permission");
		if (plugin.getData().getBoolean("Players." + p.getUniqueId() + ".inGUI")) {
			Collection<? extends Player> online = Bukkit.getOnlinePlayers();
			System.out.println(online.size());
			if (online != null) {
			for (Player p1 : online) {
				if (online.size() != 1) {
					if (p1.hasPermission(permission)) {
						p1.sendMessage(translate(plugin.getConfig().getString("Settings.notify-kicked").replace("%player%", p.getName())));
						}
					}
				}
			}
		}
	}
	
	@EventHandler
	public void blockBreakEvent(BlockBreakEvent e) {
		List<Material> materials = getCrops();
		Random random = new Random();
		Double roll = random.nextDouble()*100;
		Double chance = 100-getChance();
		Player p = e.getPlayer();
		
		if (materials.contains(e.getBlock().getType())) {
			if (roll > chance) {
				attempts = plugin.getConfig().getInt("Failure.attempts")-1;
				applicable = true;
				plugin.getData().set("Players." + p.getUniqueId().toString() + ".name", p.getName());
				plugin.getData().set("Players." + p.getUniqueId().toString() + ".inGUI", true);
				openInventory(e.getPlayer());
			}
		}
	}
	
	@EventHandler
	public void interact(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		List<String> commands = plugin.getConfig().getStringList("Failure.commands");
		String s = plugin.getConfig().getString("Settings.gui-name")
				.replace("%customname%", customItemName)
				.replace("%item_id%", itemType);
		if (e.getInventory().getName().equals(translate(s))) {
			e.setCancelled(true);
			if (e.getSlot() == clickedSlot) {
				applicable = false;
				p.closeInventory();
			} else {
				if (attempts < 1) {
					for (String s1 : commands) {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s1.replace("%player%", p.getName()));
					}
					attempts = plugin.getConfig().getInt("Failure.attempts");
				} else {
					attempts--;
					p.closeInventory();
					new BukkitRunnable() {
			            @Override
			            public void run() {
			                openInventory(p);
			            }
			        }.runTaskLater(plugin, (long) 0.5);
				}
			}
		}
	}
	
	@EventHandler
	public void closeEvent(InventoryCloseEvent e) {
		Inventory inv = e.getInventory();
		Player p = (Player) e.getPlayer();
		
		if (applicable) {
		if (inv.getTitle().equals(translate(plugin.getConfig().getString("Settings.gui-name")
				.replace("%customname%", customItemName)
				.replace("%item_id%", itemType)))) {
			new BukkitRunnable() {
	            @Override
	            public void run() {
	                openInventory(p);
	            	}
	        	}.runTaskLater(plugin, (long) 0.5);
			}
		}
	}
	
	public void openInventory(Player p) {
		List<Material> captcha = getCaptchaBlocks();
		Integer size = plugin.getConfig().getInt("Settings.gui-size");
		Integer range = plugin.getConfig().getInt("Settings.random-number-max");
		Random random = new Random();
		
		Material randomMaterial = captcha.get(random.nextInt(captcha.size()));
		String customName = translate(plugin.getConfig().getString("Settings.custom-item-name").replace("%random_number%", 
				String.valueOf(random.nextInt(range))));
		String customNameGUI = ChatColor.stripColor(customName);
		Inventory inv = Bukkit.createInventory(null, size, translate(plugin.getConfig().getString("Settings.gui-name")
				.replace("%customname%", customNameGUI)
				.replace("%item_id%", randomMaterial.toString())));
		itemType = randomMaterial.toString();
		customItemName = customNameGUI;
		List<Integer> slots = new ArrayList<Integer>();
		for (int y = 0; y < inv.getSize(); y++) {
			slots.add(y);
		}
		
		Integer insertSlot = random.nextInt(inv.getSize());
		ItemStack a2 = new ItemStack(randomMaterial);
		ItemMeta m2 = a2.getItemMeta();
		m2.setDisplayName(customName);
		a2.setItemMeta(m2);
		inv.setItem(insertSlot, a2);
		clickedSlot = insertSlot;
		slots.remove(insertSlot);
		
		for (Integer i : slots) {
			Material randomMat = captcha.get(random.nextInt(captcha.size()));
			ItemStack a = new ItemStack(randomMat);
			inv.setItem(i, a);
		}
		
		p.openInventory(inv);
	}
	
	public List<Material> getCrops() {
		List<Material> materials = new ArrayList<Material>();
		List<String> materialStrings = new ArrayList<String>();
		materialStrings = plugin.getConfig().getStringList("Settings.blocklist-crops");
		for (String s : materialStrings) {
			materials.add(Material.valueOf(s));
		}
		return materials;
	}
	
	public List<Material> getCaptchaBlocks() {
		List<Material> materials = new ArrayList<Material>();
		List<String> materialStrings = new ArrayList<String>();
		materialStrings = plugin.getConfig().getStringList("Settings.blocklist-captcha");
		for (String s : materialStrings) {
			materials.add(Material.valueOf(s));
		}
		return materials;
	}
	
	public Double getChance() {
		Double chance = 0.0;
		chance = plugin.getConfig().getDouble("Settings.chance");
		return chance;
	}
	
	public String translate(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}
}

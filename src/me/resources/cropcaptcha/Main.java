package me.resources.cropcaptcha;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
	
	private File configFile;
	private FileConfiguration config;
	private File configFile2;
	private FileConfiguration config2;
	
	private Logger log = Logger.getLogger("Minecraft");
	
	public void onEnable() {
		
		if (Bukkit.getPluginManager().getPlugin("SkyHoesReborn") != null) {
			log.info("[SkyCaptcha] Plugin has been successfully enabled.");
		} else {
			Bukkit.getPluginManager().disablePlugin(this);
			log.warning("[SkyCaptcha] SkyHoesReborn is required to run this expansion.");
		}
		
		if (Bukkit.getPluginManager().isPluginEnabled(this)) {
			getServer().getPluginManager().registerEvents(new CropHandler(this), this);
			createConfig();
			createData();
		}
		
	}
	
	public void onDisable() {
		log.info("[SkyCaptcha] Plugin has been successfully disabled.");
	}
	
	public FileConfiguration getConfig() {
		return this.config;
	}
	
	public File getConfigFile() {
		return this.configFile;
	}
	
	public void createConfig() {
		configFile = new File(getDataFolder(), "settings.yml");
		if (!configFile.exists()) {
			configFile.getParentFile().mkdirs();
			saveResource("settings.yml", false);
		}
		
		config = new YamlConfiguration();
		try {
			config.load(configFile);
		} catch
			(IOException | InvalidConfigurationException e) {
				e.printStackTrace();
		}
	}
	
	public FileConfiguration getData() {
		return this.config2;
	}
	
	public File getDataFile() {
		return this.configFile2;
	}
	
	public void createData() {
		configFile2 = new File(getDataFolder(), "data.yml");
		if (!configFile2.exists()) {
			configFile2.getParentFile().mkdirs();
			saveResource("data.yml", false);
		}
		
		config2 = new YamlConfiguration();
		try {
			config2.load(configFile2);
		} catch
			(IOException | InvalidConfigurationException e) {
				e.printStackTrace();
		}
	}
}

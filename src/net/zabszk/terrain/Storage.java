package net.zabszk.terrain;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Storage {
	public static YamlConfiguration get(String name)
	{
		File configfile = new File(name);
		return YamlConfiguration.loadConfiguration(configfile);
	}
	
	public static File getfile(String name)
	{
		return new File(name);
	}
	
	public static void setclaims(List<String> claims)
	{
		YamlConfiguration f = get(cfg.claims());
		f.set("Terrains", claims);
		save(cfg.claims(), f);
	}
	
	public static void save(String name, YamlConfiguration cfg)
	{
		File configfile = new File(name);
		
		try {
			cfg.save(configfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

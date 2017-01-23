package net.zabszk.terrain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Functions {
	public static void Add(File tconf, CommandSender sender, String target, String rnk)
	{
		FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
		
		List<String> members = tconfig.getStringList("Allowed");
		int rank = -1;
		int index = -1;
		
		for (int i = 0; i < members.size(); i++)
		{
			if (rank == -1)
			{
				String[] member = members.get(i).split(",");
				
				if (member[0].equalsIgnoreCase(target))
				{
					index = i;
					
					rank = Integer.valueOf(member[1]);
				}
			}
		}
		
		if (rank > -1)
		{
			if (Integer.toString(rank).equals(rnk.replace("helper", "0").replace("member", "1").replace("admin", "2"))) {
				sender.sendMessage(Main.format("e", Main.lang("add-fail-added").replace("%claim", tconfig.getString("Name"))));
			} else
			{
				members.set(index, target + "," + rnk.replace("helper", "0").replace("member", "1").replace("admin", "2"));
				
				tconfig.set("Allowed", members);
				
				try
				{
					tconfig.save(tconf);
				}
				catch (IOException ex)
				{
					System.out.println("[TerrainClaim] Config file saving error.");
				}
				
				sender.sendMessage(Main.format("3", Main.lang("add-changed").replace("%nick", target).replace("%claim", tconfig.getString("Name"))));
			}
		}
		else
		{
			members.add(target + "," + rnk.replace("helper", "0").replace("member", "1").replace("admin", "2"));
			
			tconfig.set("Allowed", members);
			
			try
			{
				tconfig.save(tconf);
			}
			catch (IOException ex)
			{
				System.out.println("[TerrainClaim] Config file saving error.");
			}
			
			sender.sendMessage(Main.format("3", Main.lang("add-added").replace("%nick", target).replace("%claim", tconfig.getString("Name"))));
		}
	}
	
	public static void Remove(File tconf, CommandSender sender, String target)
	{
		FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
		
		List<String> members = tconfig.getStringList("Allowed");
		int id = -1;
		
		for (int i = 0; i < members.size(); i++)
		{
			if (id == -1)
			{
				String[] member = members.get(i).split(",");
				
				if (member[0].equalsIgnoreCase(target)) id = i;
			}
		}
		
		if (id == -1) sender.sendMessage(Main.format("e", Main.lang("rm-fail-removed").replace("%nick", target).replace("%claim", tconfig.getString("Name"))));
		else
		{
			members.remove(id);
			
			tconfig.set("Allowed", members);
			
			try
			{
				tconfig.save(tconf);
			}
			catch (IOException ex)
			{
				System.out.println("[TerrainClaim] Config file saving error.");
			}
			
			sender.sendMessage(Main.format("3", Main.lang("rm-removed").replace("%nick", target).replace("%claim", tconfig.getString("Name"))));
		}
	}
	
	protected static void GenerateLang(String name)
	{
		File langfile = new File("plugins/TerrainClaim/lang/" + name + ".yml");
		
		if (langfile.exists())
		{
			FileConfiguration langconf = YamlConfiguration.loadConfiguration(langfile);
			
			if (langconf.getInt("DO-NOT-CHANGE-lang-ver") != Main.LangVersion) langfile.delete();
		}
		
		if (!langfile.exists()) Extract("resources/" + name + ".yml", "plugins/TerrainClaim/lang/ " + name + ".yml");
	}
	
	protected static void GenerateConfig(String name)
	{
		File cfile = new File("plugins/TerrainClaim/" + name + ".yml");
		
		if (!cfile.exists()) Extract("configs/" + name + ".yml", "plugins/TerrainClaim/" + name + ".yml");
	}
	
	private static void Extract(String source, String target)
	{
		try 
		{
			JarFile file = new JarFile(URLDecoder.decode(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
			ZipEntry entry = file.getEntry(source);
			InputStream inputStream = file.getInputStream(entry);
			
			Files.copy(inputStream, Paths.get(target));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static ItemStack getTerrainBlock()
	{
		ItemStack BlokTerenu = new ItemStack(Material.getMaterial(Main.config.getString("TerrainBlock")), 1);
		
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(BlokTerenu.getType());
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Main.config.getString("TerrainBlockName")));
		
		List<String> desc = new ArrayList<String>();
		List<String> lore = new ArrayList<String>();
		
		desc.clear();
		desc = Main.config.getStringList("TerrainBlockLore");
		
		for (String l:desc)
		{
			lore.add(ChatColor.translateAlternateColorCodes('&', l));
		}
		
		meta.setLore(lore);
		
		BlokTerenu.setItemMeta(meta);
		
		return BlokTerenu;
	}
	
	protected static void MigrateConfig()
	{
		//TODO: Lang in PL
		//TODO: This void (moves from old terrain.yml to config.yml and claims.yml).
		//TODO: Custom subcommands and aliases.
		//TODO: Change version.
		//TODO: Tests
	}
}

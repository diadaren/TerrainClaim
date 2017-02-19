package net.zabszk.terrain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Functions {
	public static void Add(File tconf, CommandSender sender, String target, String rnk)
	{
		target = GetUUID(target);
		
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
				
				sender.sendMessage(Main.format("3", Main.lang("add-changed").replace("%nick", GetNickname(target)).replace("%claim", tconfig.getString("Name"))));
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
			
			sender.sendMessage(Main.format("3", Main.lang("add-added").replace("%nick", GetNickname(target)).replace("%claim", tconfig.getString("Name"))));
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
				
				if (member[0].equalsIgnoreCase(GetUUID(target)) || member[0].equalsIgnoreCase(target)) id = i;
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
	
	protected static void Claim(Player target, String type)
	{
		Chunk ch = target.getLocation().getChunk();
		
		File tconf = new File("plugins/TerrainClaim/claims/" + target.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
		if (tconf.exists()) target.sendMessage(Main.format("4", Main.lang("already-claimed")));
		else
		{
			try
			{
				if (!tconf.exists()) tconf.createNewFile();
			}
			catch (IOException ex)
			{
				System.out.println("[TerrainClaim] Config file creation error.");
			}
			
			FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
			
			Location l = target.getLocation().add(0, 1, 0);
			
			tconfig.set("Name", target.getWorld().getName() + "," + ch.getX() + "," + ch.getZ());
			tconfig.set("Owner", target.getUniqueId().toString());
			tconfig.set("Allowed", new ArrayList<String>());
			tconfig.set("world", target.getWorld().getName());
			tconfig.set("X", l.getBlockX());
			tconfig.set("Y", l.getBlockY());
			tconfig.set("Z", l.getBlockZ());
			tconfig.set("Chunk", ch.getX() + "," + ch.getZ());
			tconfig.set("Method", type);
			
			try
			{
				tconfig.save(tconf);
			}
			catch (IOException ex)
			{
				System.out.println("[TerrainClaim] Config file saving error.");
			}
			
			List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
			
			tereny.add(target.getWorld().getName() + ";" + ch.getX() + ";" + ch.getZ() + ";" + target.getUniqueId().toString() + ";" + target.getWorld().getName() + "," + ch.getX() + "," + ch.getZ() + ";" + type);
			
			Storage.setclaims(tereny);
				
			try
			{
				if (Storage.get(cfg.experimental()).getBoolean("PlaySound"))
				{
					target.getWorld().playSound(target.getLocation().add(0, 1, 0), Sound.ENTITY_WITHER_AMBIENT, 1, 0);
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				System.out.println(ChatColor.RED + "Disable PlaySound in experimental config!!!");
			}
			
			try
			{
				if (Storage.get(cfg.experimental()).getBoolean("PlayEffect"))
				{
					for (int a = 0; a < 500; a++)
					{
						target.getWorld().playEffect(target.getLocation().add(0, 1, 0), Effect.CLOUD, 5);
					}
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				System.out.println(ChatColor.RED + "Disable PlayEffect in experimental config!!!");
			}
			
			
			target.sendMessage(Main.format("3", Main.lang("claim-done")));
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
		
		if (!langfile.exists()) Extract("resources/" + name + ".yml", "plugins/TerrainClaim/lang/" + name + ".yml");
	}
	
	protected static void GenerateConfig(String name)
	{
		File cfile = new File("plugins/TerrainClaim/" + name + ".yml");
		
		if (!cfile.exists()) Extract("configs/" + name + ".yml", "plugins/TerrainClaim/" + name + ".yml");
	}
	
	@SuppressWarnings("deprecation")
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
		if (Storage.getfile(cfg.OLDconfig()).exists())
		{
			System.out.println("[TerrainClaim] Updating config version...");
			
			YamlConfiguration file = Storage.get(cfg.OLDconfig());
			YamlConfiguration c = Storage.get(cfg.config());
			
			List<String> tereny = file.getStringList("Terrains");
			
			System.out.println("[TerrainClaim] Updating claims...");
			
			for (int i = 0; i < tereny.size(); i++)
			{
				System.out.println("[TerrainClaim] Updating claims [" + (i + 1) + "/" + tereny.size() + "]");
				
				tereny.set(i, tereny.get(i) + ";B");
				String[] sp = tereny.get(i).split(";");
				
				File tconf = new File("plugins/TerrainClaim/claims/" + sp[0] + "/" + sp[1] + "," + sp[2] + ".yml");
				
				System.out.println("[TerrainClaim] Updating claim " + sp[0] + "/" + sp[1] + "," + sp[2]);
				
				FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
				
				tconfig.set("Method", "B");
				
				try {
					tconfig.save(tconf);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			Storage.setclaims(tereny);
			
			System.out.println("[TerrainClaim] Claims updated and saved");
			
			Copy("PluginDisplayName", file, c);
			Copy("Lang", file, c);
			Copy("CheckForWorldGuardRegions", file, c);
			Copy("TerrainBlock", file, c);
			Copy("TerrainBlockName", file, c);
			Copy("TerrainBlockLore", file, c);
			Copy("Enable-PvP", file, c);
			Copy("Enable-AddedVsNonadded", file, c);
			Copy("Enable-PvE", file, c);
			Copy("Enable-EvE", file, c);
			Copy("Enable-Creeper", file, c);
			Copy("AllowMetrics", file, c);
			
			Storage.getfile(cfg.OLDconfig()).delete();
			Storage.save(cfg.config(), c);
			
			System.out.println("[TerrainClaim] Config updated");
		}
		
		//TODO: Transfer subcommand
		//TODO: Kick subcommand
	}
	
	private static void Copy(String value, YamlConfiguration old, YamlConfiguration n)
	{
		n.set(value, old.get(value));
	}
	
	@SuppressWarnings("deprecation")
	public static String GetUUID(String nick)
	{
		try {
			OfflinePlayer p = Bukkit.getOfflinePlayer(nick);
			return p.getUniqueId().toString();
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static String GetNickname(String uuid)
	{
		try {
			return Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
		}
		catch (Exception e) {
			return uuid;
		}
	}
	
	public static String GetNickname(UUID uuid)
	{
		try {
			return Bukkit.getOfflinePlayer(uuid).getName();
		}
		catch (Exception e) {
			return uuid.toString();
		}
	}
}

package net.zabszk.terrain;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;

public class Main extends JavaPlugin
{
	public static Plugin plugin;
	private static Main instance;
	
	private static File langfile;
	protected static FileConfiguration langconf;
	
	private static File wfile;
	protected static FileConfiguration wconf;
	
	private static Event event;
	
	protected static ItemStack BlokTerenu;
	
	private static File configfile;
	protected static FileConfiguration config;
	
	private static File efile;
	protected static FileConfiguration econf;
	
	public static final int LangVersion = 1;
	
	@Override
	public void onEnable()
	{
		plugin = this;
		instance = this;
		event = new Event();
		
		configfile = new File("plugins/TerrainClaim/terrains.yml");
		config = YamlConfiguration.loadConfiguration(configfile);
		
		wfile = new File("plugins/TerrainClaim/worlds.yml");
		wconf = YamlConfiguration.loadConfiguration(wfile);
		
		try
		{
			if (!configfile.exists()) configfile.createNewFile();
		}
		catch (IOException e)
		{
			System.out.println("[TerrainClaim] Config file creation error.");
		}
		
		try
		{
			if (!wfile.exists()) wfile.createNewFile();
		}
		catch (IOException e)
		{
			System.out.println("[TerrainClaim] Worlds file creation error.");
		}
		
		List<String> desc = new ArrayList<String>();
		
		desc.add("&6Protects a chunk (16x16).");
		
		config.addDefault("PluginDisplayName", "TerrainClaim");
		config.addDefault("Lang", "en");
		config.addDefault("CheckForWorldGuardRegions", true);
		config.addDefault("TerrainBlock", "DIAMOND_ORE");
		config.addDefault("TerrainBlockName", "&9&lTerrain Block");
		config.addDefault("TerrainBlockLore", desc);
		config.addDefault("Enable-PvP", false);
		config.addDefault("Enable-AddedVsNonadded", true);
		config.addDefault("Enable-PvE", false);
		config.addDefault("Enable-EvE", false);
		config.addDefault("Enable-Creeper", false);
		config.addDefault("AllowMetrics", true);
		config.addDefault("Terrains", new ArrayList<String>());
		config.options().copyDefaults(true);
		
		try
		{
			config.save(configfile);
		}
		catch (IOException e)
		{
			System.out.println("[TerrainClaim] Config file saving error.");
		}
		
		wconf.addDefault("UseBlacklist", false);
		wconf.addDefault("BlacklistedWorlds", new ArrayList<String>());
		
		wconf.addDefault("UseWhitelist", false);
		wconf.addDefault("WhitelistedWorlds", new ArrayList<String>());
		
		wconf.options().copyDefaults(true);
		
		try {
			wconf.save(wfile);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		File path = new File("plugins/TerrainClaim/lang/");
		
		try
		{
			if (!path.exists()) path.mkdir();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		Reload();
		
		getServer().getPluginManager().registerEvents(event, this);
		
		if (config.getBoolean("AllowMetrics"))
		{
			try {
			    Metrics metrics = new Metrics(this);
			    metrics.start();
			} catch (IOException e) {
				System.out.println("Metrics error!");
	            e.printStackTrace();
			}
		}
		
		System.out.println("[TerrainClaim] Plugin enabled!");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if (args.length == 0)
		{
			Boolean admin = sender.hasPermission("terrain.admin");
			
			sender.sendMessage("");
			sender.sendMessage("");
			sender.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "/terrain");
			sender.sendMessage(ChatColor.AQUA + "/terrain add <nick> <rank> [-a]");
			sender.sendMessage(ChatColor.GRAY + "Help: /terrain ranks");
			sender.sendMessage(ChatColor.AQUA + "/terrain remove <nick> [-a]");
			sender.sendMessage(ChatColor.AQUA + "/terrain list");
			sender.sendMessage(ChatColor.AQUA + "/terrain tp <terrain name>");
			sender.sendMessage(ChatColor.AQUA + "/terrain rename <new name>");
			sender.sendMessage(ChatColor.AQUA + "/terrain info");
			
			sender.sendMessage("");
			
			if (admin || sender.hasPermission("terrain.reload")) sender.sendMessage(ChatColor.GOLD + "/terrain reload");
			if (admin || sender.hasPermission("terrain.block")) sender.sendMessage(ChatColor.DARK_RED + "/terrain block [nick] [amount]");
			if (admin || sender.hasPermission("terrain.list.others")) sender.sendMessage(ChatColor.DARK_RED + "/terrain list [nick]");
			if (admin || sender.hasPermission("terrain.tp.others")) sender.sendMessage(ChatColor.DARK_RED + "/terrain tp <owner>:<name>");
			
			sender.sendMessage("");
		}
		else
		{
			if ((args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("ls")) && Perm("list", sender, true, true))
			{
				if (sender instanceof Player || args.length > 1)
				{
					String search = "";
					
					if (args.length > 1 && Perm("list.others", sender, false, true)) search = args[1];
					else if (args.length == 1) search = sender.getName();
					
					if (!search.equals(""))
					{
						configfile = new File("plugins/TerrainClaim/terrains.yml");
						config = YamlConfiguration.loadConfiguration(configfile);
						
						List<String> tereny = (List<String>) config.get("Terrains");
						Boolean found = false;
						
						for (int i = 0; i < tereny.size(); i++)
						{
							if (tereny.get(i).split(";")[3].equalsIgnoreCase(search))
							{
								if (!found) sender.sendMessage(format("3", lang("list")));
								sender.sendMessage(ChatColor.GRAY + "- " + tereny.get(i).split(";")[4]);
								
								found = true;
							}
						}
						
						if (found)
						{
							sender.sendMessage(lang("list-rename"));
							sender.sendMessage(lang("list-tp"));
						}
						else sender.sendMessage(format("4", lang("list-empty")));
					}
				}
				else sender.sendMessage(format("4", "When running from console, please provide player in second parameter."));
			}
			else if (args[0].equalsIgnoreCase("ranks"))
			{
				sender.sendMessage(ChatColor.GRAY + "Ranks:");
				sender.sendMessage(ChatColor.GREEN + "- helper (0) - accessing chests, interacting");
				sender.sendMessage(ChatColor.DARK_GREEN + "- member (1) - building and breaking blocks, accessing chests, interacting");
				sender.sendMessage(ChatColor.GOLD + "- admin (2) - terrain management, building and breaking blocks, accessing chests, interacting");
			}
			else if (args[0].equalsIgnoreCase("tp") && Perm("tp", sender, true, true))
			{
				if (sender instanceof Player)
				{
					if (args.length != 2) sender.sendMessage(format("4", "Syntax: /teren tp <terrain name>"));
					else
					{
						String player = sender.getName();
						String search = "";
						
						if (args[1].contains(":") && Perm("tp.others", sender, false, true))
						{
							player = args[1].split(":")[0];
							search = args[1].split(":")[1];
						}
						else if (!args[1].contains(":")) search = args[1];
						
						if (!search.equals(""))
						{
							configfile = new File("plugins/TerrainClaim/terrains.yml");
							config = YamlConfiguration.loadConfiguration(configfile);
							
							List<String> tereny = (List<String>) config.get("Terrains");
							Boolean found = false;
							
							for (int i = 0; i < tereny.size(); i++)
							{
								if (!found && tereny.get(i).split(";")[3].equalsIgnoreCase(player) && tereny.get(i).split(";")[4].equalsIgnoreCase(search))
								{
									File tconf = new File("plugins/TerrainClaim/claims/" + tereny.get(i).split(";")[0] + "/" + tereny.get(i).split(";")[1] + "," + tereny.get(i).split(";")[2] + ".yml");
									FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
									
									String world = tconfig.getString("world");
									int x = tconfig.getInt("X");
									int y = tconfig.getInt("Y");
									int z = tconfig.getInt("Z");
									
									World w = Bukkit.getWorld(world);
									
									w.getBlockAt(x, y + 1, z).setType(Material.AIR);
									w.getBlockAt(x, y + 2, z).setType(Material.AIR);
									
									((Player) sender).teleport(new org.bukkit.Location(Bukkit.getWorld(world), x, y + 1, z));
									
									found = true;
								}
							}
							
							if (found)
							{
								sender.sendMessage(format("3", lang("tp-done")));
							}
							else sender.sendMessage(format("4", lang("tp-not-found")));
						}
					}
				}
				else sender.sendMessage(format("4", "This command can be executed only from game level."));
			}
			else if ((args[0].equalsIgnoreCase("rename") || args[0].equalsIgnoreCase("name")) && Perm("reame", sender, true, true))
			{
				if (sender instanceof Player)
				{
					Chunk ch = ((Player) sender).getLocation().getChunk();
					
					File tconf = new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
					if (tconf.exists())
					{
						FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
						
						if (tconfig.getString("Owner").equalsIgnoreCase(sender.getName()) || Main.getInstance().Perm("rename.others", sender, false, true))
						{
							configfile = new File("plugins/TerrainClaim/terrains.yml");
							config = YamlConfiguration.loadConfiguration(configfile);
							
							String search = args[1].replace(";", ".").replace(":", ".");
							String target = tconfig.getString("Owner");
							
							List<String> tereny = (List<String>) config.get("Terrains");
							Boolean found = false;
							
							for (int i = 0; i < tereny.size(); i++)
							{
								if (tereny.get(i).split(";")[3].equalsIgnoreCase(target))
								{
									if (tereny.get(i).split(";")[4].equalsIgnoreCase(search)) found = true;
								}
							}
							
							if (!found)
							{
								tconfig.set("Name", args[1].replace(";", ".").replace(":", "."));
								
								try
								{
									tconfig.save(tconf);
								}
								catch (IOException ex)
								{
									System.out.println("[TerrainClaim] Config file saving error.");
								}
								
								tereny = (List<String>) config.getList("Terrains");
								
								found = false;
								
								for (int i = 0; i < tereny.size(); i++)
								{
									if (!found)
									{
										String[] s = tereny.get(i).split(";");									
										
										if (s[0].equalsIgnoreCase(ch.getWorld().getName()) && s[1].equalsIgnoreCase(Integer.toString(ch.getX()))  && s[2].equalsIgnoreCase(Integer.toString(ch.getZ())))
										{
											tereny.set(i, s[0] + ";" + s[1] + ";" + s[2] + ";" + s[3] + ";" + args[1].replace(";", ".").replace(":", "."));
											
											sender.sendMessage(format("3", lang("rename-done")));
											
											found = true;
										}
									}
								}
								
								config.set("Terrains", tereny);
								
								try
								{
									config.save(configfile);
								}
								catch (IOException ex)
								{
									System.out.println("[TerrainClaim] Config file saving error.");
								}
							}
							else sender.sendMessage(format("4", lang("rename-exists")));
						}
						else sender.sendMessage(format("4", lang("rename-not-permitted")));
					}
					else sender.sendMessage(format("4", lang("rename-not-claimed")));
				}
				else sender.sendMessage(format("4", "This command can be executed only from game level."));
			}
			else if (args[0].equalsIgnoreCase("add") && Perm("add", sender, true, true))
			{
				if (sender instanceof Player)
				{
					if (args.length < 3) sender.sendMessage(format("4", "Syntax: /teren add <nick> <rank> [-a]"));
					else
					{
						if (args[2].equalsIgnoreCase("helper") || args[2].equalsIgnoreCase("member") || args[2].equalsIgnoreCase("admin") || args[2].equalsIgnoreCase("0") || args[2].equalsIgnoreCase("1") || args[2].equalsIgnoreCase("2"))
						{
							if (args.length == 4 && args[3].equalsIgnoreCase("-a"))
							{
								configfile = new File("plugins/TerrainClaim/terrains.yml");
								config = YamlConfiguration.loadConfiguration(configfile);
								
								List<String> tereny = (List<String>) config.get("Terrains");
								
								for (int i = 0; i < tereny.size(); i++)
								{
									if (tereny.get(i).split(";")[3].equalsIgnoreCase(sender.getName()))
									{
										String[] split = tereny.get(i).split(";");
										
										File tconf = new File("plugins/TerrainClaim/claims/" + split[0] + "/" + split[1] + "," + split[2] + ".yml");
										Functions.Add(tconf, sender, args[1], args[2]);
									}
								}
							}
							else
							{
								Chunk ch = ((Player) sender).getLocation().getChunk();
								
								if (permitted(ch, (Player) sender, 2) || Perm("add.others", sender, false, true))
								{
									File tconf = new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
									if (tconf.exists()) Functions.Add(tconf, sender, args[1], args[2]);
									else sender.sendMessage(format("4", lang("add-not-claimed")));
								}
							}
						}
						else sender.sendMessage(format("4", "You provided an invalid rank. More on /terrain ranks."));
					}
				}
				else sender.sendMessage(format("4", "This command can be executed only from game level."));
			}
			else if ((args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("rm")) && Perm("remove", sender, true, true))
			{
				if (sender instanceof Player)
				{
					if (args.length < 2) sender.sendMessage(format("4", "Syntax: /terrain remove <nick> [-a]"));
					else
					{
						if (args.length == 3 && args[2].equalsIgnoreCase("-a"))
						{
							configfile = new File("plugins/TerrainClaim/terrains.yml");
							config = YamlConfiguration.loadConfiguration(configfile);
							
							List<String> tereny = (List<String>) config.get("Terrains");
							
							for (int i = 0; i < tereny.size(); i++)
							{
								if (tereny.get(i).split(";")[3].equalsIgnoreCase(sender.getName()))
								{
									String[] split = tereny.get(i).split(";");
									
									File tconf = new File("plugins/TerrainClaim/claims/" + split[0] + "/" + split[1] + "," + split[2] + ".yml");
									Functions.Remove(tconf, sender, args[1]);
								}
							}
						}
						else
						{
							Chunk ch = ((Player) sender).getLocation().getChunk();
							if (permitted(ch, (Player) sender, 2) || Perm("remove.others", sender, false, true))
							{
								File tconf = new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
								if (tconf.exists()) Functions.Remove(tconf, sender, args[1]);
								else sender.sendMessage(format("4", lang("rm-not-claimed")));
							}
							else sender.sendMessage(format("4", lang("rm-not-permitted")));
						}
					}
				}
				else sender.sendMessage(format("4", lang("info-not-claimed")));
			}
			else if (args[0].equalsIgnoreCase("info") && Perm("info", sender, true, true))
			{
				if (sender instanceof Player)
				{
					Chunk ch = ((Player) sender).getLocation().getChunk();
					
					File tconf = new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
					if (!tconf.exists()) sender.sendMessage(format("4", lang("info-not-claimed")));
					else
					{
						FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
						
						sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Information about claim: " + ChatColor.AQUA + ch.getX() + "," + ch.getZ());
						sender.sendMessage(ChatColor.DARK_AQUA + "Owner: " + ChatColor.AQUA + tconfig.getString("Owner"));
						sender.sendMessage(ChatColor.DARK_AQUA + "Name: " + ChatColor.AQUA + tconfig.getString("Name"));
						
						List<String> Allowed = (List<String>) tconfig.getList("Allowed");
						
						String disp = "";
						
						for (int i = 0; i < Allowed.size(); i++)
						{
							if (Allowed.get(i).split(",")[1].equalsIgnoreCase("2")) disp = disp + Allowed.get(i).split(",")[0] + ", ";
						}
						
						if (disp.length() > 0) disp = disp.substring(0, disp.length() - 2);
						
						sender.sendMessage(ChatColor.DARK_AQUA + "Administrators: " + ChatColor.AQUA + disp);
						
						disp = "";
						
						for (int i = 0; i < Allowed.size(); i++)
						{
							if (Allowed.get(i).split(",")[1].equalsIgnoreCase("1")) disp = disp + Allowed.get(i).split(",")[0] + ", ";
						}
						
						if (disp.length() > 0) disp = disp.substring(0, disp.length() - 2);
						
						sender.sendMessage(ChatColor.DARK_AQUA + "Members: " + ChatColor.AQUA + disp);
						
						disp = "";
						
						for (int i = 0; i < Allowed.size(); i++)
						{
							if (Allowed.get(i).split(",")[1].equalsIgnoreCase("0")) disp = disp + Allowed.get(i).split(",")[0] + ", ";
						}
						
						if (disp.length() > 0) disp = disp.substring(0, disp.length() - 2);
						
						sender.sendMessage(ChatColor.DARK_AQUA + "Helpers: " + ChatColor.AQUA + disp);
					}
				}
				else sender.sendMessage(format("4", "This command can be executed only from game level."));
			}
			else if (args[0].equalsIgnoreCase("block") && Perm("block", sender, false, true))
			{
				String target = null;
				int i = 0;
				
				if (args.length > 1) target = args[1];
				else if (sender instanceof Player) target = sender.getName();
				else sender.sendMessage(format("4", "When running from console, please provide player in second parameter."));
				
				if (args.length > 2)
				{
					try
					{
						i = Integer.valueOf(args[2]);
					}
					catch (Exception e)
					{
						sender.sendMessage(format("4", "You must provide an integer in third parameter."));
					}
				}
				else i = 1;
				
				if (target != null && i > 0)
				{
					for (Player t : getOnline())
					{
						if (t.getName().equalsIgnoreCase(target))
						{
							ItemStack bt = BlokTerenu;
							
							bt.setAmount(i);
							
							t.getInventory().addItem(bt);
							t.updateInventory();
							
							t.sendMessage(format("3", lang("block-become").replace("%amount", Integer.toString(i)).replace("%nick", sender.getName())));
							sender.sendMessage(format("3", lang("block-give").replace("%amount", Integer.toString(i)).replace("%nick", t.getName())));
						}
					}
				}
			}
			else if (args[0].equalsIgnoreCase("reload") && Perm("reload", sender, false, true))
			{
				Reload();
				sender.sendMessage(ChatColor.GREEN + "[TerrainClaim] Plugin reloaded.");
			}
			else sender.sendMessage(format("4", "Unknown subcommand. Type /terrain to get help."));
		}
		
		return true;
	}
	
	protected static Boolean Perm(String perm, CommandSender sender, Boolean user, Boolean admin)
	{
		Boolean result = sender.hasPermission("terrain." + perm);
		
		if (!result && user) result = sender.hasPermission("terrain.player");
		if (!result && admin) result = sender.hasPermission("terrain.admin");
		
		if (!result)
		{
			sender.sendMessage(format("4", "Access Denied!"));
			sender.sendMessage(format("4", "You need one of following permissions:"));
			sender.sendMessage(ChatColor.DARK_GRAY + "- terrain." + perm);
			if (user) sender.sendMessage(ChatColor.DARK_GRAY + "- terrain.player");
			if (admin) sender.sendMessage(ChatColor.DARK_GRAY + "- terrain.admin");
		}
		
		return result;
	}
	
	public static String format (String Color, String Msg)
	{
		return ChatColor.translateAlternateColorCodes('&', "&8&l[&" + Color + "&l" + Main.getInstance().config.getString("PluginDisplayName") + "&8&l] " + "&" + Color + "&l" + Msg);
	}
	
	public static Main getInstance()
    {	 
        return instance;
    }
	
	public Player[] getOnline()
    {
        try
        {
            Method method = Bukkit.class.getMethod("getOnlinePlayers");
            Object players = method.invoke(null);
            
            if (players instanceof Player[]) {
                Player[] oldPlayers = (Player[]) players;
                return oldPlayers;
             
            }
            else
            {
                Collection<Player> newPlayers = (Collection<Player>) players;
                
                Player[] online = new Player[newPlayers.size()];
                
                Object[] obj = newPlayers.toArray();
                
                int counter = 0;
                
                for (int i = 0; i < obj.length; i++)
                {
                	if (obj[i] instanceof Player)
                	{
                		String name = obj[i].toString().substring(obj[i].toString().indexOf("{"));
                		name = name.replace("{name=", "");
                		name = name.substring(0, name.length() - 1);
                		
                		online[counter] = Bukkit.getPlayer(name);
                		counter = counter + 1;
                	}
                }
                return online;
            }
         
        } 
        catch (Exception e)
        {
            System.out.println("Player online ERROR");
            System.out.println(e.toString());
            e.printStackTrace();
            
            return null;
        }
	}
	
	public static void Reload()
	{
		Functions.GenerateLang("en");
		Functions.GenerateLang("pl");
		Functions.GenerateLang("fr"); //Translation made by: Alphayt (https://dev.bukkit.org/members/Alphayt)
		
		Functions.GenerateConfig("experimental");
		
		configfile = new File("plugins/TerrainClaim/terrains.yml");
		config = YamlConfiguration.loadConfiguration(configfile);
		
		wfile = new File("plugins/TerrainClaim/worlds.yml");
		wconf = YamlConfiguration.loadConfiguration(wfile);
		
		efile = new File("plugins/TerrainClaim/experimental.yml");
		econf = YamlConfiguration.loadConfiguration(efile);
		
		langfile = new File("plugins/TerrainClaim/lang/" + config.getString("Lang") + ".yml");
		langconf = YamlConfiguration.loadConfiguration(langfile);
		
		BlokTerenu = new ItemStack(Material.getMaterial(config.getString("TerrainBlock")), 1);
		
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(BlokTerenu.getType());
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("TerrainBlockName")));
		
		List<String> desc = new ArrayList<String>();
		List<String> lore = new ArrayList<String>();
		
		desc.clear();
		desc = config.getStringList("TerrainBlockLore");
		
		for (String l:desc)
		{
			lore.add(ChatColor.translateAlternateColorCodes('&', l));
		}
		
		meta.setLore(lore);
		
		BlokTerenu.setItemMeta(meta);
	}
	
	protected static WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null;
	    }
	 
	    return (WorldGuardPlugin) plugin;
	}
	
	public static String lang(String text)
	{
		try
		{
			return ChatColor.translateAlternateColorCodes('&', langconf.getString(text));
		}
		catch (Exception e)
		{
			return ChatColor.RED + "Translation " + text + " not found in /plugins/TerrainClaim/lang/" + config.getString("Lang") + ".yml";
		}
	}
	
	public static boolean permitted(Chunk ch, Player target, int Level)
	{
		Boolean result = false;
		
		File tconf = new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
		if (!tconf.exists()) result = true;
		else
		{
			if (CheckWorld(ch.getWorld()))
			{
				FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
				
				if (tconfig.getString("Owner").equalsIgnoreCase(target.getName())) result = true;
				else if (target.hasPermission("terrain.admin") || target.hasPermission("terrain.bypass")) result = true;
				else
				{
					List<String> members = (List<String>) tconfig.get("Allowed");
					
					for (int i = 0; i < members.size(); i++)
					{
						String[] member = members.get(i).split(",");
						
						if (member[0].equalsIgnoreCase(target.getName()))
						{
							if (Integer.parseInt(member[1]) >= Level) result = true;
						}
					}
				}
			}
			else
			{
				target.sendMessage(format("3", lang("prot-err-world")));
				result = true;
			}
		}
		
		return result;
	}
	
	public static boolean CheckWorld(World world)
	{
		return ((!wconf.getBoolean("UseBlacklist") || !wconf.getStringList("BlacklistedWorlds").contains(world.getName())) && (!wconf.getBoolean("UseWhitelist") || wconf.getStringList("WhitelistedWorlds").contains(world.getName())));
	}
}

package net.zabszk.terrain;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;

public class Main extends JavaPlugin
{
	public static Plugin plugin;
	private static Main instance;
	
	private static File langfile;
	protected static FileConfiguration langconf;
	
	private static Event event;
	
	private static File configfile;
	protected static FileConfiguration config;
	
	public static final int LangVersion = 2;
	
	@Override
	public void onEnable()
	{
		plugin = this;
		instance = this;
		event = new Event();
		
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
			sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("add") + " " + lang("help-nick") + " " + lang("help-rank") + " [-a]");
			sender.sendMessage(ChatColor.GRAY + lang("help-ranks-help") + " /" + label + " " + GetAlias("ranks"));
			sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("remove") + " " + lang("help-nick") + " [-a]");
			sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("list"));
			sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("tp") + " " + lang("help-terrain-name"));
			sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("rename") + " " + lang("help-new-name"));
			sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("info"));
			
			sender.sendMessage("");
			sender.sendMessage("");
			sender.sendMessage(lang("claim-command-max").replace("%limit", Integer.toString(config.getInt("CommandClaimsLimit"))));
			sender.sendMessage("");
			
			if (config.getBoolean("AllowCommandClaiming"))
			{
				sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("claim"));
				sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("unclaim"));
				sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("settp"));
			}
			else
			{
				sender.sendMessage(ChatColor.GRAY + "/" + label + " " + GetAlias("claim"));
				sender.sendMessage(ChatColor.GRAY + "/" + label + " " + GetAlias("unclaim"));
				sender.sendMessage(ChatColor.GRAY + "/" + label + " " + GetAlias("settp"));
			}
			
			sender.sendMessage("");
			
			if (admin || sender.hasPermission("terrain.reload")) sender.sendMessage(ChatColor.GOLD + "/" + label + " reload");
			if (admin || sender.hasPermission("terrain.block")) sender.sendMessage(ChatColor.DARK_RED + "/" + label + " " + GetAlias("block") + " " + lang("help-nick-optional") + " " + lang("help-amount-optional"));
			if (admin || sender.hasPermission("terrain.list.others")) sender.sendMessage(ChatColor.DARK_RED + "/" + label + " " + GetAlias("list") + " " + lang("help-nick-optional"));
			if (admin || sender.hasPermission("terrain.tp.others")) sender.sendMessage(ChatColor.DARK_RED + "/" + label + " " + GetAlias("tp") + " " + lang("help-owner") + ":" + lang("help-nick"));
			
			sender.sendMessage("");
		}
		else
		{
			if (args.length > 0)
			{
				String alias = IsAlias(args[0]);
				if (alias.length() > 0) args[0] = alias;
			}
			
			if ((args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("ls")) && Perm("list", sender, true, true))
			{
				if (sender instanceof Player || args.length > 1)
				{
					String search = "";
					
					if (args.length > 1 && Perm("list.others", sender, false, true)) search = args[1];
					else if (args.length == 1) search = sender.getName();
					
					if (!search.equals(""))
					{
						List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
						Boolean found = false;
						
						for (int i = 0; i < tereny.size(); i++)
						{
							if (tereny.get(i).split(";")[3].equalsIgnoreCase(search))
							{
								if (!found) sender.sendMessage(format("3", lang("list")));
								String[] sp = tereny.get(i).split(";");
								
								if (sp[5].equalsIgnoreCase("B")) sender.sendMessage(ChatColor.WHITE + "- " + sp[4] + ChatColor.GREEN + " [BLOCK]");
								else if (sp[5].equalsIgnoreCase("C")) sender.sendMessage(ChatColor.WHITE + "- " + sp[4] + ChatColor.AQUA + " [COMMAND]");
								else sender.sendMessage(ChatColor.WHITE + "- " + sp[4] + ChatColor.LIGHT_PURPLE + " [OTHER]");
								
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
				sender.sendMessage(lang("help-ranks-header"));
				sender.sendMessage(lang("help-ranks-helper"));
				sender.sendMessage(lang("help-ranks-member"));
				sender.sendMessage(lang("help-ranks-admin"));
			}
			else if (args[0].equalsIgnoreCase("tp") && Perm("tp", sender, true, true))
			{
				if (sender instanceof Player)
				{
					if (args.length != 2) sender.sendMessage(format("4", "Syntax: /" + label + " " + GetAlias("tp") + " " + lang("help-terrain-name")));
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
							List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
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
							String search = args[1].replace(";", ".").replace(":", ".");
							String target = tconfig.getString("Owner");
							
							List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
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
								
								tereny = Storage.get(cfg.claims()).getStringList("Terrains");
								
								found = false;
								
								for (int i = 0; i < tereny.size(); i++)
								{
									if (!found)
									{
										String[] s = tereny.get(i).split(";");									
										
										if (s[0].equalsIgnoreCase(ch.getWorld().getName()) && s[1].equalsIgnoreCase(Integer.toString(ch.getX()))  && s[2].equalsIgnoreCase(Integer.toString(ch.getZ())))
										{
											tereny.set(i, s[0] + ";" + s[1] + ";" + s[2] + ";" + s[3] + ";" + args[1].replace(";", ".").replace(":", ".") + ";" + s[5]);
											
											sender.sendMessage(format("3", lang("rename-done")));
											
											found = true;
										}
									}
								}
								
								Storage.setclaims(tereny);
								
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
					if (args.length < 3) sender.sendMessage(format("4", "Syntax: /" + label + " " + GetAlias("add") + " " + lang("help-nick") + " " + lang("help-rank") + " [-a]"));
					else
					{
						if (args[2].equalsIgnoreCase("helper") || args[2].equalsIgnoreCase("member") || args[2].equalsIgnoreCase("admin") || args[2].equalsIgnoreCase("0") || args[2].equalsIgnoreCase("1") || args[2].equalsIgnoreCase("2"))
						{
							if (args.length == 4 && args[3].equalsIgnoreCase("-a"))
							{
								List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
								
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
						else sender.sendMessage(format("4", lang("add-wrong-rank").replace("%info", "/" + label + " " + GetAlias("ranks"))));
					}
				}
				else sender.sendMessage(format("4", "This command can be executed only from game level."));
			}
			else if ((args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("rm")) && Perm("remove", sender, true, true))
			{
				if (sender instanceof Player)
				{
					if (args.length < 2) sender.sendMessage(format("4", "Syntax: /" + label + " " + GetAlias("remove") + " " + lang("help-nick") + " [-a]"));
					else
					{
						if (args.length == 3 && args[2].equalsIgnoreCase("-a"))
						{
							List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
							
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
						
						sender.sendMessage(lang("info-about").replace("%claim", ch.getX() + "," + ch.getZ()));
						sender.sendMessage(lang("info-owner").replace("%nick", tconfig.getString("Owner")));
						sender.sendMessage(lang("info-name").replace("%name", tconfig.getString("Name")));
						
						List<String> Allowed = (List<String>) tconfig.getList("Allowed");
						
						String disp = "";
						
						for (int i = 0; i < Allowed.size(); i++)
						{
							if (Allowed.get(i).split(",")[1].equalsIgnoreCase("2")) disp = disp + Allowed.get(i).split(",")[0] + ", ";
						}
						
						if (disp.length() > 0) disp = disp.substring(0, disp.length() - 2);
						
						sender.sendMessage(lang("info-admins").replace("%nick", disp));
						
						disp = "";
						
						for (int i = 0; i < Allowed.size(); i++)
						{
							if (Allowed.get(i).split(",")[1].equalsIgnoreCase("1")) disp = disp + Allowed.get(i).split(",")[0] + ", ";
						}
						
						if (disp.length() > 0) disp = disp.substring(0, disp.length() - 2);
						
						sender.sendMessage(lang("info-members").replace("%nick", disp));
						
						disp = "";
						
						for (int i = 0; i < Allowed.size(); i++)
						{
							if (Allowed.get(i).split(",")[1].equalsIgnoreCase("0")) disp = disp + Allowed.get(i).split(",")[0] + ", ";
						}
						
						if (disp.length() > 0) disp = disp.substring(0, disp.length() - 2);
						
						sender.sendMessage(lang("info-helpers").replace("%nick", disp));
					}
				}
				else sender.sendMessage(format("4", "This command can be executed only from game level."));
			}
			else if (args[0].equalsIgnoreCase("claim") && Perm("claim", sender, true, true))
			{
				if (sender instanceof Player)
				{
					if (config.getBoolean("AllowCommandClaiming"))
					{
						Player target = (Player) sender;
						
						if (CheckWorld(target.getWorld()))
						{
							if (!config.getBoolean("CheckForWorldGuardRegions") || getWorldGuard() == null || getWorldGuard().canBuild(target, target.getLocation()))
							{
								int count = 0;
								
								List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
								Boolean found = false;
								
								for (int i = 0; i < tereny.size(); i++)
								{
									if (tereny.get(i).split(";")[3].equalsIgnoreCase(sender.getName()) && tereny.get(i).split(";")[5].equalsIgnoreCase("C")) count++;
								}
								
								if (count < config.getInt("CommandClaimsLimit") || config.getInt("CommandClaimsLimit") == -1) Functions.Claim(target, "C");
								else sender.sendMessage(format("3", lang("claim-command-limit").replace("%limit", Integer.toString(config.getInt("CommandClaimsLimit")))));
							}
						}
						else sender.sendMessage(Main.format("4", Main.lang("claim-err-world")));
					}
					else sender.sendMessage(Main.format("4", Main.lang("claim-command-disabled")));
				}
				else sender.sendMessage(format("4", "This command can be executed only from game level."));
			}
			else if (args[0].equalsIgnoreCase("unclaim"))
			{
				if (sender instanceof Player)
				{
					Location l = ((Player) sender).getLocation();
					Chunk ch = l.getChunk();
					
					File tconf = new File("plugins/TerrainClaim/claims/" + ((Player) sender).getPlayer().getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
					if (tconf.exists())
					{
						FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
						
						if (tconfig.getString("Owner").equalsIgnoreCase(sender.getName()) || Main.Perm("unclaim.others", sender, false, true))
						{
							if (tconfig.getString("Method").equalsIgnoreCase("C"))
							{
								List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
								
								String del = "";
								
								for (String t : tereny)
								{
									String[] s = t.split(";");
									
									if (s[0].equalsIgnoreCase(((Player) sender).getWorld().getName()) && s[1].equals(Integer.toString(ch.getX())) && s[2].equals(Integer.toString(ch.getZ()))) del = t;
								}
								
								if (!del.equals("")) tereny.remove(del);
								
								Storage.setclaims(tereny);
								
								tconf.delete();
								
								try
								{
									if (Storage.get(cfg.experimental()).getBoolean("PlaySound"))
									{
										((Player) sender).getWorld().playSound(l, Sound.ENTITY_GENERIC_EXPLODE, 1, 0);
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
											((Player) sender).getWorld().playEffect(l, Effect.EXPLOSION_HUGE, 5);
										}
									}
								}
								catch (Exception ex)
								{
									ex.printStackTrace();
									System.out.println(ChatColor.RED + "Disable PlayEffect in experimental config!!!");
								}
								
								sender.sendMessage(Main.format("b", Main.lang("claim-unclaimed")));
							}
							else sender.sendMessage(Main.format("4", Main.lang("unclaim-use-block")));
						}
						else sender.sendMessage(Main.format("4", Main.lang("not-owner")));
					}
					else sender.sendMessage(Main.format("4", Main.lang("unclaim-not-claimed")));
				}
				else sender.sendMessage(format("4", "This command can be executed only from game level."));
			}
			else if (args[0].equalsIgnoreCase("settp") && Perm("settp", sender, true, true))
			{
				if (sender instanceof Player)
				{
					Location l = ((Player) sender).getLocation();
					Chunk ch = l.getChunk();
					
					File tconf = new File("plugins/TerrainClaim/claims/" + ((Player) sender).getPlayer().getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
					if (tconf.exists())
					{
						FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
						
						if (tconfig.getString("Owner").equalsIgnoreCase(sender.getName()) || Main.Perm("settp.others", sender, false, true))
						{
							if (tconfig.getString("Method").equalsIgnoreCase("C"))
							{
								tconfig.set("X", l.getBlockX());
								tconfig.set("Y", l.getBlockY());
								tconfig.set("Z", l.getBlockZ());
								
								try
								{
									tconfig.save(tconf);
								}
								catch (IOException ex)
								{
									System.out.println("[TerrainClaim] Config file saving error.");
								}
								
								sender.sendMessage(Main.format("3", Main.lang("settp-done")));
							}
							else sender.sendMessage(Main.format("4", Main.lang("settp-command-only")));
						}
						else sender.sendMessage(Main.format("4", Main.lang("not-owner")));
					}
					else sender.sendMessage(Main.format("4", Main.lang("settp-not-claimed")));
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
							ItemStack bt = Functions.getTerrainBlock();
							
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
			if (config.getBoolean("ShowRequiredPermissions"))
			{
				sender.sendMessage(format("4", "You need one of following permissions:"));
				sender.sendMessage(ChatColor.DARK_GRAY + "- terrain." + perm);
				if (user) sender.sendMessage(ChatColor.DARK_GRAY + "- terrain.player");
				if (admin) sender.sendMessage(ChatColor.DARK_GRAY + "- terrain.admin");
			}
		}
		
		return result;
	}
	
	public static String format (String Color, String Msg)
	{
		return ChatColor.translateAlternateColorCodes('&', "&8&l[&" + Color + Main.getInstance().config.getString("PluginDisplayName") + "&8&l] " + "&" + Color + Msg);
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
		Functions.GenerateConfig("config");
		Functions.GenerateConfig("worlds");
		Functions.GenerateConfig("claims");
		Functions.GenerateConfig("aliases");
		Functions.GenerateConfig("experimental");
		
		Functions.MigrateConfig();
		
		File path = new File("plugins/TerrainClaim/lang/");
		
		try
		{
			if (!path.exists()) path.mkdir();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		Functions.GenerateLang("en"); //Translation made by: zabszk (https://dev.bukkit.org/members/zabszk)
		Functions.GenerateLang("pl"); //Translation made by: zabszk (https://dev.bukkit.org/members/zabszk)
		Functions.GenerateLang("fr"); //Translation made by: Alphayt (https://dev.bukkit.org/members/Alphayt)
		
		Functions.MigrateConfig();
		
		configfile = new File("plugins/TerrainClaim/config.yml");
		config = YamlConfiguration.loadConfiguration(configfile);
		
		langfile = new File("plugins/TerrainClaim/lang/" + config.getString("Lang") + ".yml");
		langconf = YamlConfiguration.loadConfiguration(langfile);
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
	
	public static String IsAlias(String arg)
	{
		FileConfiguration file = Storage.get(cfg.aliases());
		
		if (file.getString("alias-add").equalsIgnoreCase(arg)) return "add";
		if (file.getString("alias-ranks").equalsIgnoreCase(arg)) return "ranks";
		else if (file.getString("alias-remove").equalsIgnoreCase(arg)) return "remove";
		else if (file.getString("alias-list").equalsIgnoreCase(arg)) return "list";
		else if (file.getString("alias-tp").equalsIgnoreCase(arg)) return "tp";
		else if (file.getString("alias-rename").equalsIgnoreCase(arg)) return "rename";
		else if (file.getString("alias-info").equalsIgnoreCase(arg)) return "info";
		else if (file.getString("alias-claim").equalsIgnoreCase(arg)) return "claim";
		else if (file.getString("alias-unclaim").equalsIgnoreCase(arg)) return "unclaim";
		else if (file.getString("alias-settp").equalsIgnoreCase(arg)) return "settp";
		else if (file.getString("alias-block").equalsIgnoreCase(arg)) return "block";
		else return "";
	}
	
	public static String GetAlias(String subcommand)
	{
		String text = Storage.get(cfg.aliases()).getString("alias-" + subcommand);
		if (text.length() > 0) return text;
		else return subcommand;
	}
	
	public static boolean CheckWorld(World world)
	{
		FileConfiguration wconf = Storage.get(cfg.worlds());
		
		return ((!wconf.getBoolean("UseBlacklist") || !wconf.getStringList("BlacklistedWorlds").contains(world.getName())) && (!wconf.getBoolean("UseWhitelist") || wconf.getStringList("WhitelistedWorlds").contains(world.getName())));
	}
}

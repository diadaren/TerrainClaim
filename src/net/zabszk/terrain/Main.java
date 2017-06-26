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
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;

public class Main extends JavaPlugin {
	public static Plugin plugin;
	private static Main instance;
	private static Event event;
	
	public static File configfile;
	
	public static YamlConfiguration config;
	protected static YamlConfiguration langconf;
	public static YamlConfiguration flags;
	public static YamlConfiguration prefs;
	
	public static final int LangVersion = 6;
	
	public static Set<String> CommandBlacklist;
	
	@Override
	public void onEnable() {
		plugin = this;
		instance = this;
		event = new Event();
		
		Reload();
		getServer().getPluginManager().registerEvents(event, this);
		
		if (config.getBoolean("AllowMetrics")) {
			try {
			    Metrics metrics = new Metrics(this);
			    metrics.start();
			} catch (IOException e) {
				System.out.println("Metrics error!");
	            e.printStackTrace();
			}
		}
		
		Functions.ProcessClaims();
		
		System.out.println("[TerrainClaim] Plugin enabled!");
		System.out.println("TerrainClaim, version " + Bukkit.getServer().getPluginManager().getPlugin("TerrainClaim").getDescription().getVersion() + " on " + Bukkit.getBukkitVersion());
		System.out.println("[TerrainClaim] Copyright by ZABSZK, 2017");
		System.out.println("[TerrainClaim] Licensed on Mozilla Public License 2.0");
	}
	
	@Override
	public void onDisable() {
		Functions.SaveCache();
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			Boolean admin = sender.hasPermission("terrain.admin");
			
			sender.sendMessage(ChatColor.GOLD + "=====================================================");
			sender.sendMessage("");
			if (admin || sender.hasPermission("terrain.add.others.recursive")) sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("add") + " " + lang("help-nick") + " " + lang("help-rank") + ChatColor.DARK_RED + " [-a | r]");
			else sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("add") + " " + lang("help-nick") + " " + lang("help-rank") + " [-a]");
			sender.sendMessage(ChatColor.GRAY + lang("help-ranks-help") + " /" + label + " " + GetAlias("ranks"));
			if (admin || sender.hasPermission("terrain.remove.others.recursive")) sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("remove") + " " + lang("help-nick") + ChatColor.DARK_RED + " [-a | r]");
			else sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("remove") + " " + lang("help-nick") + " [-a]");
			sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("list"));
			sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("tp") + " " + lang("help-terrain-name"));
			sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("rename") + " " + lang("help-new-name"));
			sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("info"));
			sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("manage") + " " + lang("help-nick"));
			if (admin || sender.hasPermission("terrain.flag.others.recursive")) sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("flag") + " " + lang("help-flag") + ChatColor.DARK_RED + " [-a | r]");
			else sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("flag") + " " + lang("help-flag") + " [-a]");
			
			if (config.getBoolean("AllowCommandClaiming")) {
				sender.sendMessage("");
				sender.sendMessage(lang("claim-command-max").replace("%limit", Integer.toString(config.getInt("CommandClaimsLimit"))).replace("-1", lang("help-unlimited")));
				sender.sendMessage("");
				
				sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("claim"));
				sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("unclaim"));
				sender.sendMessage(ChatColor.AQUA + "/" + label + " " + GetAlias("settp"));
			}
			
			if (admin) sender.sendMessage("");
			if (admin || sender.hasPermission("terrain.reload")) sender.sendMessage(ChatColor.GOLD + "/" + label + " reload");
			if (admin || sender.hasPermission("terrain.dev")) sender.sendMessage(ChatColor.GOLD + "/" + label + " " + GetAlias("dev") + " " + lang("help-nick-optional"));
			if (admin || sender.hasPermission("terrain.block")) sender.sendMessage(ChatColor.DARK_RED + "/" + label + " " + GetAlias("block") + " " + lang("help-nick-optional") + " " + lang("help-amount-optional"));
			if (admin || sender.hasPermission("terrain.list.others")) sender.sendMessage(ChatColor.DARK_RED + "/" + label + " " + GetAlias("list") + " " + lang("help-nick-optional"));
			if (admin || sender.hasPermission("terrain.tp.others")) sender.sendMessage(ChatColor.DARK_RED + "/" + label + " " + GetAlias("tp") + " " + lang("help-owner") + ":" + lang("help-nick"));
			if (admin || sender.hasPermission("terrain.validate")) sender.sendMessage(ChatColor.DARK_RED + "/" + label + " " + GetAlias("validate"));
			
			if (langconf.getBoolean("DO-NOT-CHANGE-incomplete")) {
				sender.sendMessage("");
				sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "!" + ChatColor.DARK_GRAY + "] Current language file is incomplete.");
			}
			
			sender.sendMessage("");
			sender.sendMessage(ChatColor.DARK_GRAY + "TerrainClaim, version " + ColorizeVersionName(Bukkit.getServer().getPluginManager().getPlugin("TerrainClaim").getDescription().getVersion(), ChatColor.DARK_GRAY));
			sender.sendMessage(ChatColor.DARK_GRAY + "Copyright by ZABSZK, 2017");
			sender.sendMessage(ChatColor.DARK_GRAY + "Licensed on Mozilla Public License 2.0");
			sender.sendMessage(ChatColor.GOLD + "=====================================================");
		} else {
			if (args.length > 0) {
				String alias = IsAlias(args[0]);
				if (alias.length() > 0) args[0] = alias;
			}
			
			if ((args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("ls")) && Perm("list", sender, true, true)) {
				if (sender instanceof Player || args.length > 1) {
					String search = "";
					
					if (args.length > 1 && Perm("list.others", sender, false, true)) search = Functions.GetUUID(args[1]);
					else if (args.length == 1) search = Functions.GetUUID(sender.getName());
					
					if (!search.equals("")) {
						List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
						Boolean found = false;
						
						for (int i = 0; i < tereny.size(); i++) {
							if (tereny.get(i).split(";")[3].equalsIgnoreCase(search)) {
								if (!found) sender.sendMessage(format("3", lang("list")));
								String[] sp = tereny.get(i).split(";");
								
								if (sp[5].equalsIgnoreCase("B")) Functions.FormatListMessage(sender, sp[4], lang("list-block"), search);
								else if (sp[5].equalsIgnoreCase("C")) Functions.FormatListMessage(sender, sp[4], lang("list-command"), search);
								else Functions.FormatListMessage(sender, sp[4], lang("list-other"), search);
								
								found = true;
							}
						}
						
						if (found) {
							sender.sendMessage(lang("list-rename"));
							sender.sendMessage(lang("list-tp"));
							sender.sendMessage(lang("list-click"));
						}
						else sender.sendMessage(format("4", lang("list-empty")));
					}
				}
				else sender.sendMessage(format("4", "When running from console, please provide player in second parameter."));
			} else if (args[0].equalsIgnoreCase("ranks")) {
				sender.sendMessage(lang("help-ranks-header"));
				sender.sendMessage(lang("help-ranks-helper"));
				sender.sendMessage(lang("help-ranks-member"));
				sender.sendMessage(lang("help-ranks-admin"));
			} else if (args[0].equalsIgnoreCase("tp") && Perm("tp", sender, true, true)) {
				if (sender instanceof Player) {
					if (args.length != 2) sender.sendMessage(format("4", "Syntax: /" + label + " " + GetAlias("tp") + " " + lang("help-terrain-name")));
					else {
						String player = ((Player) sender).getUniqueId().toString();
						String search = "";
						
						if (args[1].contains(":") && Perm("tp.others", sender, false, true)) {
							player = args[1].split(":")[0];
							search = args[1].split(":")[1];
						} else if (!args[1].contains(":")) search = args[1];
						
						if (!search.equals("")) {
							List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
							Boolean found = false;
							
							for (int i = 0; i < tereny.size(); i++) {
								if (!found && tereny.get(i).split(";")[3].equalsIgnoreCase(player) && tereny.get(i).split(";")[4].equalsIgnoreCase(search)) {
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
							
							if (found) {
								sender.sendMessage(format("3", lang("tp-done")));
							} else sender.sendMessage(format("4", lang("tp-not-found")));
						}
					}
				} else sender.sendMessage(format("4", "This command can be executed only from game level."));
			} else if ((args[0].equalsIgnoreCase("rename") || args[0].equalsIgnoreCase("name")) && Perm("reame", sender, true, true)) {
				if (sender instanceof Player) {
					Chunk ch = ((Player) sender).getLocation().getChunk();
					
					File tconf = new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
					if (tconf.exists()) {
						FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
						if (tconfig.getString("Owner").equalsIgnoreCase(((Player) sender).getUniqueId().toString()) || Perm("rename.others", sender, false, true)) {
							String search = args[1].replace(";", ".").replace(":", ".");
							String target = tconfig.getString("Owner");
							
							List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
							Boolean found = false;
							
							for (int i = 0; i < tereny.size(); i++) {
								if (tereny.get(i).split(";")[3].equalsIgnoreCase(target)) {
									if (tereny.get(i).split(";")[4].equalsIgnoreCase(search)) found = true;
								}
							}
							
							if (!found) {
								tconfig.set("Name", args[1].replace(";", ".").replace(":", "."));
								
								try {
									tconfig.save(tconf);
								} catch (IOException ex) {
									System.out.println("[TerrainClaim] Config file saving error.");
								}
								
								tereny = Storage.get(cfg.claims()).getStringList("Terrains");
								
								found = false;
								
								for (int i = 0; i < tereny.size(); i++) {
									if (!found) {
										String[] s = tereny.get(i).split(";");									
										
										if (s[0].equalsIgnoreCase(ch.getWorld().getName()) && s[1].equalsIgnoreCase(Integer.toString(ch.getX()))  && s[2].equalsIgnoreCase(Integer.toString(ch.getZ()))) {
											tereny.set(i, s[0] + ";" + s[1] + ";" + s[2] + ";" + s[3] + ";" + args[1].replace(";", ".").replace(":", ".") + ";" + s[5]);
											
											sender.sendMessage(format("3", lang("rename-done")));
											
											found = true;
										}
									}
								}
								
								Storage.setclaims(tereny);
								
								try {
									config.save(configfile);
								} catch (IOException ex) {
									System.out.println("[TerrainClaim] Config file saving error.");
								}
							} else sender.sendMessage(format("4", lang("rename-exists")));
						} else sender.sendMessage(format("4", lang("rename-not-permitted")));
					} else sender.sendMessage(format("4", lang("rename-not-claimed")));
				} else sender.sendMessage(format("4", "This command can be executed only from game level."));
			} else if (args[0].equalsIgnoreCase("add") && Perm("add", sender, true, true)) {
				if (sender instanceof Player) {
					if (args.length < 3) sender.sendMessage(format("4", "Syntax: /" + label + " " + GetAlias("add") + " " + lang("help-nick") + " " + lang("help-rank") + " [-a | r]"));
					else {
						if (args[2].equalsIgnoreCase("helper") || args[2].equalsIgnoreCase("member") || args[2].equalsIgnoreCase("admin") || args[2].equalsIgnoreCase("0") || args[2].equalsIgnoreCase("1") || args[2].equalsIgnoreCase("2")) {
							if (args.length == 4 && args[3].equalsIgnoreCase("-a") && Perm("add.recursive", sender, true, true)) {
								List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains"); 
								for (int i = 0; i < tereny.size(); i++) {
									if (tereny.get(i).split(";")[3].equalsIgnoreCase(((Player) sender).getUniqueId().toString())) {
										String[] split = tereny.get(i).split(";");
										File tconf = new File("plugins/TerrainClaim/claims/" + split[0] + "/" + split[1] + "," + split[2] + ".yml");
										Functions.Add(tconf, sender, args[1], args[2]);
									}
								}
							} else if (args.length == 4 && args[3].equalsIgnoreCase("-r")) {
								if (Perm("add.others.recursive", sender, false, true)) {
									List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
									Chunk ch = ((Player) sender).getLocation().getChunk();
									File thconf = new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
									YamlConfiguration tc = YamlConfiguration.loadConfiguration(thconf);
									String uuid = tc.getString("Owner");
									
									for (int i = 0; i < tereny.size(); i++) {
										if (tereny.get(i).split(";")[3].equalsIgnoreCase(uuid)) {
											String[] split = tereny.get(i).split(";");
											File tconf = new File("plugins/TerrainClaim/claims/" + split[0] + "/" + split[1] + "," + split[2] + ".yml");
											Functions.Add(tconf, sender, args[1], args[2]);
										}
									}
								}
							} else {
								Chunk ch = ((Player) sender).getLocation().getChunk();
								if (permitted(ch, (Player) sender, 2, false) || Perm("add.others", sender, false, true)) {
									File tconf = new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
									if (tconf.exists()) Functions.Add(tconf, sender, args[1], args[2]);
									else sender.sendMessage(format("4", lang("add-not-claimed")));
								}
							}
						} else sender.sendMessage(format("4", lang("add-wrong-rank").replace("%info", "/" + label + " " + GetAlias("ranks"))));
					}
				} else sender.sendMessage(format("4", "This command can be executed only from game level."));
			} else if ((args[0].equalsIgnoreCase("del") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("rm")) && Perm("remove", sender, true, true)) {
				if (sender instanceof Player){
					if (args.length < 2) sender.sendMessage(format("4", "Syntax: /" + label + " " + GetAlias("remove") + " " + lang("help-nick") + " [-a | r]"));
					else {
						if (args.length == 3 && args[2].equalsIgnoreCase("-a") && Perm("remove.recursive", sender, true, true)) {
							List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
							
							for (int i = 0; i < tereny.size(); i++) {
								if (tereny.get(i).split(";")[3].equalsIgnoreCase(((Player) sender).getUniqueId().toString())) {
									String[] split = tereny.get(i).split(";");
									File tconf = new File("plugins/TerrainClaim/claims/" + split[0] + "/" + split[1] + "," + split[2] + ".yml");
									Functions.Remove(tconf, sender, args[1]);
								}
							}
						}
						else if (args.length == 3 && args[2].equalsIgnoreCase("-r")) {
							if (Perm("remove.others.recursive", sender, false, true)) {
								List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
								Chunk ch = ((Player) sender).getLocation().getChunk();
								
								File thconf = new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
								YamlConfiguration tc = YamlConfiguration.loadConfiguration(thconf);
								String uuid = tc.getString("Owner");
								
								for (int i = 0; i < tereny.size(); i++) {
									if (tereny.get(i).split(";")[3].equalsIgnoreCase(uuid)) {
										String[] split = tereny.get(i).split(";");
										File tconf = new File("plugins/TerrainClaim/claims/" + split[0] + "/" + split[1] + "," + split[2] + ".yml");
										Functions.Remove(tconf, sender, args[1]);
									}
								}
							}
						} else {
							Chunk ch = ((Player) sender).getLocation().getChunk();
							if (permitted(ch, (Player) sender, 2, false) || Perm("remove.others", sender, false, true)) {
								File tconf = new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
								if (tconf.exists()) Functions.Remove(tconf, sender, args[1]);
								else sender.sendMessage(format("4", lang("rm-not-claimed")));
							}
							else sender.sendMessage(format("4", lang("rm-not-permitted")));
						}
					}
				} else sender.sendMessage(format("4", lang("info-not-claimed")));
			} else if (args[0].equalsIgnoreCase("info") && Perm("info", sender, true, true)) {
				if (sender instanceof Player) {
					Chunk ch = ((Player) sender).getLocation().getChunk();
					
					File tconf = new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
					if (!tconf.exists()) sender.sendMessage(format("4", lang("info-not-claimed")));
					else {
						FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
						
						sender.sendMessage(lang("info-about").replace("%claim", ch.getX() + "," + ch.getZ()));
						sender.sendMessage(lang("info-owner").replace("%nick", Functions.GetNickname(tconfig.getString("Owner"))));
						sender.sendMessage(lang("info-name").replace("%name", tconfig.getString("Name")));
						
						List<String> Allowed = tconfig.getStringList("Allowed");
						List<String> Flags = tconfig.getStringList("Flags");
						String flg = "";
						
						for (int i = 0; i < Flags.size(); i++) {
							String flag = Flags.get(i);
							if (i > 0) flg += Main.lang("info-flags-separator");
							if (flag.startsWith("+")) flg += ChatColor.GREEN + flag;
							else if (flag.startsWith("@")) flg += ChatColor.DARK_GREEN + flag;
							else if (flag.startsWith("-")) flg += ChatColor.RED + flag;
							else if (flag.startsWith("!")) flg += ChatColor.DARK_RED + flag;
							else flg += ChatColor.LIGHT_PURPLE + flag;
						}
						
						if (Flags.size() > 0) sender.sendMessage(lang("info-flags").replace("%flags", flg));
						else sender.sendMessage(lang("info-flags").replace("%flags", ChatColor.DARK_GRAY + "(" + lang("none") + ")"));

						sender.sendMessage(lang("info-admins"));
						for (int i = 0; i < Allowed.size(); i++) {
							if (Allowed.get(i).split(",")[1].equalsIgnoreCase("2")) Functions.SendManageMessage(sender, Allowed.get(i).split(",")[0]);
						}
						
						sender.sendMessage(lang("info-members"));
						for (int i = 0; i < Allowed.size(); i++) {
							if (Allowed.get(i).split(",")[1].equalsIgnoreCase("1")) Functions.SendManageMessage(sender, Allowed.get(i).split(",")[0]);
						}

						sender.sendMessage(lang("info-helpers"));
						for (int i = 0; i < Allowed.size(); i++) {
							if (Allowed.get(i).split(",")[1].equalsIgnoreCase("0")) Functions.SendManageMessage(sender, Allowed.get(i).split(",")[0]);
						}
					}
				} else sender.sendMessage(format("4", "This command can be executed only from game level."));
			} else if (args[0].equalsIgnoreCase("claim") && Perm("claim", sender, true, true)) {
				if (sender instanceof Player) {
					if (config.getBoolean("AllowCommandClaiming")) {
						Player target = (Player) sender;
						if (CheckWorld(target.getWorld())) {
							if (!config.getBoolean("CheckForWorldGuardRegions") || getWorldGuard() == null || getWorldGuard().canBuild(target, target.getLocation())) {
								int count = 0;
								List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
								
								for (int i = 0; i < tereny.size(); i++) {
									if (tereny.get(i).split(";")[3].equalsIgnoreCase(((Player) sender).getUniqueId().toString()) && tereny.get(i).split(";")[5].equalsIgnoreCase("C")) count++;
								}
								
								if (count < config.getInt("CommandClaimsLimit") || config.getInt("CommandClaimsLimit") == -1) Functions.Claim(target, "C");
								else sender.sendMessage(format("3", lang("claim-command-limit").replace("%limit", Integer.toString(config.getInt("CommandClaimsLimit")))));
							}
						} else sender.sendMessage(Main.format("4", Main.lang("claim-err-world")));
					} else sender.sendMessage(Main.format("4", Main.lang("claim-command-disabled")));
				} else sender.sendMessage(format("4", "This command can be executed only from game level."));
			} else if (args[0].equalsIgnoreCase("unclaim")) {
				if (sender instanceof Player) {
					Location l = ((Player) sender).getLocation();
					Chunk ch = l.getChunk();
					
					File tconf = new File("plugins/TerrainClaim/claims/" + ((Player) sender).getPlayer().getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
					if (tconf.exists()) {
						FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
						
						if (tconfig.getString("Owner").equalsIgnoreCase(((Player) sender).getUniqueId().toString()) || Main.Perm("unclaim.others", sender, false, true)) {
							if (tconfig.getString("Method").equalsIgnoreCase("C")) {
								List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
								String del = "";
								
								for (String t : tereny) {
									String[] s = t.split(";");
									if (s[0].equalsIgnoreCase(((Player) sender).getWorld().getName()) && s[1].equals(Integer.toString(ch.getX())) && s[2].equals(Integer.toString(ch.getZ()))) del = t;
								}
								
								if (!del.equals("")) tereny.remove(del);
								Storage.setclaims(tereny);
								tconf.delete();
								
								try {
									if (Storage.get(cfg.experimental()).getBoolean("PlaySound")) ((Player) sender).getWorld().playSound(l, Sound.ENTITY_GENERIC_EXPLODE, 1, 0);
								} catch (Exception ex) {
									ex.printStackTrace();
									System.out.println(ChatColor.RED + "Disable PlaySound in experimental config!!!");
								}
								
								try {
									if (Storage.get(cfg.experimental()).getBoolean("PlayEffect")) {
										for (int a = 0; a < 500; a++) {
											((Player) sender).getWorld().playEffect(l, Effect.EXPLOSION_HUGE, 5);
										}
									}
								} catch (Exception ex) {
									ex.printStackTrace();
									System.out.println(ChatColor.RED + "Disable PlayEffect in experimental config!!!");
								}
								
								sender.sendMessage(Main.format("b", Main.lang("claim-unclaimed")));
							} else sender.sendMessage(Main.format("4", Main.lang("unclaim-use-block")));
						} else sender.sendMessage(Main.format("4", Main.lang("not-owner")));
					} else sender.sendMessage(Main.format("4", Main.lang("unclaim-not-claimed")));
				} else sender.sendMessage(format("4", "This command can be executed only from game level."));
			} else if (args[0].equalsIgnoreCase("settp") && Perm("settp", sender, true, true)) {
				if (sender instanceof Player) {
					Location l = ((Player) sender).getLocation();
					Chunk ch = l.getChunk();
					
					File tconf = new File("plugins/TerrainClaim/claims/" + ((Player) sender).getPlayer().getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
					if (tconf.exists()) {
						FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
						if (tconfig.getString("Owner").equalsIgnoreCase(((Player) sender).getUniqueId().toString()) || Main.Perm("settp.others", sender, false, true)) {
							if (tconfig.getString("Method").equalsIgnoreCase("C")) {
								tconfig.set("X", l.getBlockX());
								tconfig.set("Y", l.getBlockY());
								tconfig.set("Z", l.getBlockZ());
								
								try {
									tconfig.save(tconf);
								} catch (IOException ex) {
									System.out.println("[TerrainClaim] Config file saving error.");
								}
								
								sender.sendMessage(Main.format("3", Main.lang("settp-done")));
							} else sender.sendMessage(Main.format("4", Main.lang("settp-command-only")));
						} else sender.sendMessage(Main.format("4", Main.lang("not-owner")));
					} else sender.sendMessage(Main.format("4", Main.lang("settp-not-claimed")));
				} else sender.sendMessage(format("4", "This command can be executed only from game level."));
			} else if (args[0].equalsIgnoreCase("block") && Perm("block", sender, false, true)) {
				String target = null;
				int i = 0;
				
				if (args.length > 1) target = args[1];
				else if (sender instanceof Player) target = sender.getName();
				else sender.sendMessage(format("4", "When running from console, please provide player in second parameter."));
				
				if (args.length > 2) {
					try {
						i = Integer.valueOf(args[2]);
					} catch (Exception e) {
						sender.sendMessage(format("4", "You must provide an integer in third parameter."));
					}
				}
				else i = 1;
				
				if (target != null && i > 0) {
					for (Player t : getOnline()) {
						if (t.getName().equalsIgnoreCase(target)) {
							ItemStack bt = Functions.getTerrainBlock();
							bt.setAmount(i);
							
							t.getInventory().addItem(bt);
							t.updateInventory();
							
							t.sendMessage(format("3", lang("block-become").replace("%amount", Integer.toString(i)).replace("%nick", sender.getName())));
							sender.sendMessage(format("3", lang("block-give").replace("%amount", Integer.toString(i)).replace("%nick", t.getName())));
						}
					}
				}
			} else if (args[0].equalsIgnoreCase("reload") && Perm("reload", sender, false, true)) {
				Reload();
				sender.sendMessage(ChatColor.GREEN + "[TerrainClaim] Plugin reloaded.");
			} else if (args[0].equalsIgnoreCase("manage")) {
				if (sender instanceof Player && args.length == 2) {
					Chunk ch = ((Player) sender).getLocation().getChunk();
					
					File tconf = new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
					if (!tconf.exists()) sender.sendMessage(format("4", lang("info-not-claimed")));
					else {
						if (permitted(ch, (Player) sender, 2, false)) {
							FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
							
							Functions.FormatManageMessage(sender, args[1], tconfig.getString("Owner").equalsIgnoreCase(Functions.GetUUID(((Player) sender).getName())));
						}
					}
				} else sender.sendMessage(format("4", "This command can be executed only from game level."));
			} else if (args[0].equalsIgnoreCase("dev") && Perm("dev", sender, false, true)) {
				if (args.length == 2) {
					if (Bukkit.getOfflinePlayer(args[1]).isOnline()) sender = (CommandSender) Bukkit.getPlayer(args[1]);
				}
				
				sender.sendMessage(ChatColor.GOLD + "=====================================================");
				sender.sendMessage(ChatColor.GREEN + "TerrainClaim Technical Information");
				sender.sendMessage("");
				sender.sendMessage(ChatColor.GREEN + "General");
				sender.sendMessage(ChatColor.GRAY + "PluginDisplayName: " + config.getString("PluginDisplayName"));
				if (langconf.getBoolean("DO-NOT-CHANGE-incomplete")) sender.sendMessage(ChatColor.GRAY + "Lang: " + config.getString("Lang") + ChatColor.DARK_GRAY + " [" + ChatColor.DARK_RED + "! Incomplete !" + ChatColor.DARK_GRAY + "]");
				else sender.sendMessage(ChatColor.GRAY + "Lang: " + config.getString("Lang"));
				sender.sendMessage(ChatColor.GRAY + "ShowRequiredPermissions: " + (config.getBoolean("ShowRequiredPermissions")?(ChatColor.GREEN + "YES"):(ChatColor.RED + "NO")));
				sender.sendMessage(ChatColor.GRAY + "AllowMetrics: " + (config.getBoolean("AllowMetrics")?(ChatColor.GREEN + "YES"):(ChatColor.RED + "NO")));
				sender.sendMessage("");
				sender.sendMessage(ChatColor.GREEN + "Claiming");
				sender.sendMessage(ChatColor.GRAY + "Block claiming: " + (config.getBoolean("AllowBlockClaiming")?(ChatColor.GREEN + "YES"):(ChatColor.RED + "NO")));
				sender.sendMessage(ChatColor.GRAY + "Command claiming: " + (config.getBoolean("AllowCommandClaiming")?(ChatColor.GREEN + "YES"):(ChatColor.RED + "NO")));
				sender.sendMessage(ChatColor.GRAY + "Command chunk limit: " + Integer.toString(config.getInt("CommandClaimsLimit")));
				sender.sendMessage(ChatColor.GRAY + "Check for worldguard regions: " + (config.getBoolean("CheckForWorldGuardRegions")?(ChatColor.GREEN + "YES"):(ChatColor.RED + "NO")));
				sender.sendMessage("");
				sender.sendMessage(ChatColor.GREEN + "Protection");
				sender.sendMessage(ChatColor.GRAY + "Enable PvP: " + (config.getBoolean("Enable-PvP")?(ChatColor.GREEN + "YES"):(ChatColor.RED + "NO")));
				sender.sendMessage(ChatColor.GRAY + "Added vs Nonadded: " + (config.getBoolean("AddedVsNonadded")?(ChatColor.GREEN + "YES"):(ChatColor.RED + "NO")));
				sender.sendMessage(ChatColor.GRAY + "Enable PvE: " + (config.getBoolean("Enable-PvE")?(ChatColor.GREEN + "YES"):(ChatColor.RED + "NO")));
				sender.sendMessage(ChatColor.GRAY + "Enable EvE: " + (config.getBoolean("Enable-EvE")?(ChatColor.GREEN + "YES"):(ChatColor.RED + "NO")));
				sender.sendMessage(ChatColor.GRAY + "Enable Creeper: " + (config.getBoolean("Enable-Creeper")?(ChatColor.GREEN + "YES"):(ChatColor.RED + "NO")));
				sender.sendMessage("");
				sender.sendMessage(ChatColor.GREEN + "Experimental");
				sender.sendMessage(ChatColor.GRAY + "Sounds: " + ((Storage.get(cfg.experimental()).getBoolean("PlaySound"))?(ChatColor.GREEN + "YES"):(ChatColor.RED + "NO")));
				sender.sendMessage(ChatColor.GRAY + "Effects: " + ((Storage.get(cfg.experimental()).getBoolean("PlayEffect"))?(ChatColor.GREEN + "YES"):(ChatColor.RED + "NO")));
				sender.sendMessage("");
				sender.sendMessage(ChatColor.GREEN + "Worlds");
				sender.sendMessage(ChatColor.GRAY + "Blacklist: " + ((Storage.get(cfg.worlds()).getBoolean("UseBlacklist"))?(ChatColor.GREEN + "YES"):(ChatColor.RED + "NO")));
				sender.sendMessage(ChatColor.GRAY + "Whitelist: " + ((Storage.get(cfg.worlds()).getBoolean("UseWhitelist"))?(ChatColor.GREEN + "YES"):(ChatColor.RED + "NO")));
				if (langconf.getBoolean("DO-NOT-CHANGE-incomplete")) {
					sender.sendMessage("");
					sender.sendMessage(ChatColor.DARK_GRAY + "[" + ChatColor.DARK_RED + "!" + ChatColor.DARK_GRAY + "] Current language file is incomplete.");
				}
				sender.sendMessage("");
				sender.sendMessage(ChatColor.DARK_GRAY + "TerrainClaim, version " + ColorizeVersionName(Bukkit.getServer().getPluginManager().getPlugin("TerrainClaim").getDescription().getVersion(), ChatColor.DARK_GRAY));
				sender.sendMessage(ChatColor.DARK_GRAY + "Plugin is running on " + Bukkit.getBukkitVersion());
				sender.sendMessage(ChatColor.DARK_GRAY + "Copyright by ZABSZK, 2017");
				sender.sendMessage(ChatColor.DARK_GRAY + "Licensed on Mozilla Public License 2.0");
				sender.sendMessage(ChatColor.GOLD + "=====================================================");
			}
			else if (args[0].equalsIgnoreCase("flag"))  {
				if (sender instanceof Player) {
					if (args.length == 1) {
						Chunk ch = ((Player) sender).getLocation().getChunk();
						
						File tconf = new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
						if (!tconf.exists()) sender.sendMessage(format("4", lang("info-not-claimed")));
						else {
							FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
							Functions.PrintFlags(((Player) sender).getPlayer(), tconfig.getStringList("Flags"));
						}
					} else if (Perm("flag", sender, true, true)) {
						if (args.length == 4 && args[3].equalsIgnoreCase("-a") && Perm("flag.recursive", sender, true, true)) {
							List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
							
							for (int i = 0; i < tereny.size(); i++) {
								if (tereny.get(i).split(";")[3].equalsIgnoreCase(((Player) sender).getUniqueId().toString())) {
									String[] split = tereny.get(i).split(";");
									
									File tconf = new File("plugins/TerrainClaim/claims/" + split[0] + "/" + split[1] + "," + split[2] + ".yml");
									Functions.SetFlag(tconf, sender, args[1], true);
								}
							}
						} else if (args.length == 4 && args[3].equalsIgnoreCase("-r")) {
							if (Perm("flag.others.recursive", sender, false, true)) {
								List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
								Chunk ch = ((Player) sender).getLocation().getChunk();
								File thconf = new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
								YamlConfiguration tc = YamlConfiguration.loadConfiguration(thconf);
								String uuid = tc.getString("Owner");
								
								for (int i = 0; i < tereny.size(); i++) {
									if (tereny.get(i).split(";")[3].equalsIgnoreCase(uuid)) {
										String[] split = tereny.get(i).split(";");
										
										File tconf = new File("plugins/TerrainClaim/claims/" + split[0] + "/" + split[1] + "," + split[2] + ".yml");
										Functions.SetFlag(tconf, sender, args[1], true);
									}
								}
							}
						} else {
							Chunk ch = ((Player) sender).getLocation().getChunk();
							
							if (permitted(ch, (Player) sender, 2, false) || Perm("flag.others", sender, false, true)) {
								File tconf = new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
								if (tconf.exists()) Functions.SetFlag(tconf, sender, args[1], true);
								else sender.sendMessage(format("4", lang("flag-not-claimed")));
							}
						}
					}
				} else sender.sendMessage(format("4", "This command can be executed only from game level."));
			} else if (args[0].equalsIgnoreCase("validate") && Perm("validate", sender, false, true)) {
				sender.sendMessage(ChatColor.GREEN + "[TerrainClaim] Realoding plugin and validating claims, this may take a while...");
				Reload();
				sender.sendMessage(ChatColor.GREEN + "[TerrainClaim] Plugin reloaded. Validaing claims...");
				Functions.ProcessClaims();
				sender.sendMessage(ChatColor.GREEN + "[TerrainClaim] Claims validated!");
			} else sender.sendMessage(format("4", "Unknown subcommand. Type /terrain to get help."));
		}
		return true;
	}
	
	public static Boolean Perm(String perm, CommandSender sender, Boolean user, Boolean admin) {
		Boolean result = sender.hasPermission("terrain." + perm);
		
		if (!result && user) result = sender.hasPermission("terrain.player");
		if (!result && admin) result = sender.hasPermission("terrain.admin");
		
		if (!result) {
			sender.sendMessage(format("4", "Access Denied!"));
			if (config.getBoolean("ShowRequiredPermissions")) {
				sender.sendMessage(format("4", "You need one of following permissions:"));
				sender.sendMessage(ChatColor.DARK_GRAY + "- terrain." + perm);
				if (user) sender.sendMessage(ChatColor.DARK_GRAY + "- terrain.player");
				if (admin) sender.sendMessage(ChatColor.DARK_GRAY + "- terrain.admin");
			}
		}
		return result;
	}
	
	public static String format (String Color, String Msg) {
		return ChatColor.translateAlternateColorCodes('&', "&8[&" + Color + config.getString("PluginDisplayName") + "&8] " + "&" + Color + Msg);
	}
	
	public static Main getInstance() {	 
        return instance;
    }
	
	@SuppressWarnings("unchecked")
	public static Player[] getOnline() {
        try {
            Method method = Bukkit.class.getMethod("getOnlinePlayers");
            Object players = method.invoke(null);
            
            if (players instanceof Player[]) {
                Player[] oldPlayers = (Player[]) players;
                return oldPlayers;
             
            } else {
                Collection<Player> newPlayers = (Collection<Player>) players;
                Player[] online = new Player[newPlayers.size()];
                Object[] obj = newPlayers.toArray();
                int counter = 0;
                
                for (int i = 0; i < obj.length; i++) {
                	if (obj[i] instanceof Player){
                		String name = obj[i].toString().substring(obj[i].toString().indexOf("{"));
                		name = name.replace("{name=", "");
                		name = name.substring(0, name.length() - 1);
                		
                		online[counter] = Bukkit.getPlayer(name);
                		counter = counter + 1;
                	}
                }
                return online;
            }
         
        } catch (Exception e) {
            System.out.println("Player online ERROR");
            System.out.println(e.toString());
            e.printStackTrace();
            return null;
        }
	}
	
	public static void Reload() {
		Functions.GenerateConfig("config");
		Functions.GenerateConfig("worlds");
		Functions.GenerateConfig("claims");
		Functions.GenerateConfig("aliases");
		Functions.GenerateConfig("flags");
		Functions.GenerateConfig("prefs");
		Functions.GenerateConfig("protection");
		Functions.GenerateConfig("experimental");
		Functions.MigrateConfig();
		Functions.LoadCache();
		Functions.ReloadCommandBlacklist();
		
		File path = new File("plugins/TerrainClaim/lang/");
		
		try {
			if (!path.exists()) path.mkdir();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Functions.GenerateLang("en"); //Translation made by: zabszk (https://dev.bukkit.org/members/zabszk)
		Functions.GenerateLang("pl"); //Translation made by: zabszk (https://dev.bukkit.org/members/zabszk)
		Functions.GenerateLang("fr"); //Translation made by: Alphayt (https://dev.bukkit.org/members/Alphayt)
		Functions.GenerateLang("it"); //Translation made by: Parozzz (https://dev.bukkit.org/members/Parozzz)
		
		Functions.MigrateConfig();
		
		configfile = new File(cfg.config());
		config = YamlConfiguration.loadConfiguration(configfile);
		langconf = Storage.get("plugins/TerrainClaim/lang/" + config.getString("Lang") + ".yml");
		flags = Storage.get(cfg.flags());
		prefs = Storage.get(cfg.prefs());
	}
	
	public static WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null;
	    }
	 
	    return (WorldGuardPlugin) plugin;
	}
	
	public static String lang(String text)
	{
		try {
			return ChatColor.translateAlternateColorCodes('&', langconf.getString(text));
		} catch (Exception e) {
			return ChatColor.RED + "Translation " + text + " not found in /plugins/TerrainClaim/lang/" + config.getString("Lang") + ".yml";
		}
	}
	
	public static boolean permitted(Chunk ch, Player target, int Level, boolean AllowBypass) {
		Boolean result = false;
		
		File tconf = new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
		if (!tconf.exists()) result = true;
		else {
			if (CheckWorld(ch.getWorld())) {
				FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
				
				if (tconfig.getString("Owner").equalsIgnoreCase(target.getUniqueId().toString())) result = true;
				else if (AllowBypass && (target.hasPermission("terrain.admin") || target.hasPermission("terrain.bypass"))) result = true;
				else {
					List<String> members = tconfig.getStringList("Allowed");
					for (int i = 0; i < members.size(); i++) {
						String[] member = members.get(i).split(",");
						
						if (member[0].equalsIgnoreCase(target.getUniqueId().toString())) {
							if (Integer.parseInt(member[1]) >= Level) result = true;
						}
					}
				}
			} else {
				target.sendMessage(format("3", lang("prot-err-world")));
				result = true;
			}
		}
		return result;
	}
	
	public static String IsAlias(String arg) {
		FileConfiguration file = Storage.get(cfg.aliases());
		
		if (file.getString("alias-add").equalsIgnoreCase(arg)) return "add";
		else if (file.getString("alias-ranks").equalsIgnoreCase(arg)) return "ranks";
		else if (file.getString("alias-remove").equalsIgnoreCase(arg)) return "remove";
		else if (file.getString("alias-list").equalsIgnoreCase(arg)) return "list";
		else if (file.getString("alias-tp").equalsIgnoreCase(arg)) return "tp";
		else if (file.getString("alias-rename").equalsIgnoreCase(arg)) return "rename";
		else if (file.getString("alias-info").equalsIgnoreCase(arg)) return "info";
		else if (file.getString("alias-claim").equalsIgnoreCase(arg)) return "claim";
		else if (file.getString("alias-unclaim").equalsIgnoreCase(arg)) return "unclaim";
		else if (file.getString("alias-settp").equalsIgnoreCase(arg)) return "settp";
		else if (file.getString("alias-block").equalsIgnoreCase(arg)) return "block";
		else if (file.getString("alias-manage").equalsIgnoreCase(arg)) return "manage";
		else if (file.getString("alias-dev").equalsIgnoreCase(arg)) return "dev";
		else if (file.getString("alias-flag").equalsIgnoreCase(arg)) return "flag";
		else if (file.getString("alias-validate").equalsIgnoreCase(arg)) return "validate";
		else return "";
	}
	
	public static String GetAlias(String subcommand) {
		String text = Storage.get(cfg.aliases()).getString("alias-" + subcommand);
		if (text.length() > 0) return text;
		else return subcommand;
	}
	
	static String ColorizeVersionName(String version, ChatColor color) {
		version = version.replace("ALPHA", ChatColor.RED + "ALPHA" + color);
		version = version.replace("BETA", ChatColor.AQUA + "BETA" + color);
		version = version.replace("DEV", ChatColor.GREEN + "DEV" + color);
		
		return version;
	}
	
	public static boolean CheckWorld(World world) {
		FileConfiguration wconf = Storage.get(cfg.worlds());
		
		return ((!wconf.getBoolean("UseBlacklist") || !wconf.getStringList("BlacklistedWorlds").contains(world.getName())) && (!wconf.getBoolean("UseWhitelist") || wconf.getStringList("WhitelistedWorlds").contains(world.getName())));
	}
}

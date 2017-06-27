package net.zabszk.terrain;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

import mkremins.fanciful.FancyMessage;

public class Functions {
	
	public static Map<String, String> uuid = new HashMap<String, String>();
	
	public static void Add(File tconf, CommandSender sender, String target, String rnk) {
		target = GetUUID(target);
		YamlConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
		if (HasActiveFlag(tconfig, "prohibit-members-modify") && Main.Perm("prohibitbypass.members-modify", sender, false, true)) sender.sendMessage(Main.format("c", Main.lang("action-prohibited")));
		else {
			List<String> members = tconfig.getStringList("Allowed");
			int rank = -1;
			int index = -1;
			for (int i = 0; i < members.size(); i++) {
				if (rank == -1) {
					String[] member = members.get(i).split(",");
					if (member[0].equalsIgnoreCase(target)) {
						index = i;
						rank = Integer.valueOf(member[1]);
					}
				}
			}
			if (rank > -1) {
				if (Integer.toString(rank).equals(rnk.replace("helper", "0").replace("member", "1").replace("admin", "2"))) {
					sender.sendMessage(Main.format("e", Main.lang("add-fail-added").replace("%claim", tconfig.getString("Name"))));
				} else {
					members.set(index, target + "," + rnk.replace("helper", "0").replace("member", "1").replace("admin", "2"));
					tconfig.set("Allowed", members);
					try {
						tconfig.save(tconf);
					} catch (IOException ex) {
						System.out.println("[TerrainClaim] Config file saving error.");
					}
					sender.sendMessage(Main.format("3", Main.lang("add-changed").replace("%nick", GetNickname(target)).replace("%claim", tconfig.getString("Name"))));
				}
			} else {
				members.add(target + "," + rnk.replace("helper", "0").replace("member", "1").replace("admin", "2"));
				members.sort(new MembersComparator());
				tconfig.set("Allowed", members);
				try {
					tconfig.save(tconf);
				} catch (IOException ex) {
					System.out.println("[TerrainClaim] Config file saving error.");
				}
				sender.sendMessage(Main.format("3", Main.lang("add-added").replace("%nick", GetNickname(target)).replace("%claim", tconfig.getString("Name"))));
			}
		}
	}
	
	public static void Remove(File tconf, CommandSender sender, String target) {
		YamlConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
		if (HasActiveFlag(tconfig, "prohibit-members-modify") && Main.Perm("prohibitbypass.members-modify", sender, false, true)) sender.sendMessage(Main.format("c", Main.lang("action-prohibited")));
		else {
			target = GetUUID(target);
			List<String> members = tconfig.getStringList("Allowed");
			int id = -1;
			
			for (int i = 0; i < members.size(); i++) {
				if (id == -1) {
					String[] member = members.get(i).split(",");
					if (member[0].equalsIgnoreCase(target)) id = i;
				}
			}
			
			if (id == -1) {
				if (sender != null) sender.sendMessage(Main.format("e", Main.lang("rm-fail-removed").replace("%nick", target).replace("%claim", tconfig.getString("Name"))));
			}
			else {
				members.remove(id);
				tconfig.set("Allowed", members);
				try {
					tconfig.save(tconf);
				} catch (IOException ex) {
					System.out.println("[TerrainClaim] Config file saving error.");
				}
				if (sender != null) sender.sendMessage(Main.format("3", Main.lang("rm-removed").replace("%nick", target).replace("%claim", tconfig.getString("Name"))));
			}
		}
	}
	
	public static void Transfer(File tconf, CommandSender sender, String target) {
		YamlConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
		if (HasActiveFlag(tconfig, "prohibit-transfer") && Main.Perm("prohibitbypass.transfer", sender, false, true)) sender.sendMessage(Main.format("c", Main.lang("action-prohibited")));
		else {
			int count = 0;
			String uuid = GetUUID(target);
			List<String> claims = Storage.get(cfg.claims()).getStringList("Terrains");
			for (int i = 0; i < claims.size(); i++) {
				if (claims.get(i).split(";")[3].equalsIgnoreCase(uuid) && claims.get(i).split(";")[5].equalsIgnoreCase("C")) count++;
			}
			if (!tconfig.getString("Method").equalsIgnoreCase("C") || Main.config.getInt("CommandClaimsLimit") == -1 || count < Main.config.getInt("CommandClaimsLimit")) {
				String prevown = tconfig.getString("Owner");
				Remove(tconf, null, target);
				tconfig.set("Owner", uuid);
				Storage.save(tconf, tconfig);
				for (int i = 0; i < claims.size(); i++) {
					String[] s = claims.get(i).split(";");
					if (s[0].equalsIgnoreCase(tconfig.getString("world")) && s[1].equals(tconfig.getString("Chunk").split(",")[0]) && s[2].equals(tconfig.getString("Chunk").split(",")[1])) {
						claims.set(i, claims.get(i).replace(claims.get(i).split(";")[3], uuid));
						Storage.setclaims(claims);
						sender.sendMessage(Main.format("3", Main.lang("transfer-complete").replace("%name", tconfig.getString("Name")).replace("%newowner", target)));
						Bukkit.getPlayer(target).sendMessage(Main.format("3", Main.lang("transfer-new-owner").replace("%name", tconfig.getString("Name")).replace("%prevowner", GetNickname(prevown))));
						return;
					}
				}
				sender.sendMessage(Main.format("4", Main.lang("transfer-not-found").replace("%name", tconfig.getString("Name"))));
			} else sender.sendMessage(Main.format("4", Main.lang("transfer-limit")));
		}
	}
	
	public static void SetFlag(File tconf, CommandSender sender, String set, Boolean validatePermissions) {
		if (!ValidateFlagSyntax(set)) sender.sendMessage(Main.format("4", Main.lang("flag-not-valid")));
		else if (!ValidateFlag(set)) sender.sendMessage(Main.format("4", Main.lang("flag-not-found")));
		else if (Main.flags.getString(set.substring(1) + "-perm").equalsIgnoreCase("D")) sender.sendMessage(Main.format("c", Main.lang("flag-perm-class-D")));
		else if (validatePermissions && Main.flags.getString(set.substring(1) + "-perm").equalsIgnoreCase("C") && !Main.Perm("admin.flag." + set.substring(1), sender, false, true)) sender.sendMessage(Main.format("c", Main.lang("flag-perm-class-C")));
		else if (validatePermissions && Main.flags.getString(set.substring(1) + "-perm").equalsIgnoreCase("B") && !Main.Perm("restricted.flag." + set.substring(1), sender, false, true)) sender.sendMessage(Main.format("c", Main.lang("flag-perm-class-B")));
		else if (validatePermissions && !Main.Perm("flag", sender, true, true)) sender.sendMessage(Main.format("c", Main.lang("flag-perm-class-A")));
		else {
			YamlConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
			if (HasActiveFlag(tconfig, "prohibit-flags-modify") && Main.Perm("prohibitbypass.flags-modify", sender, false, true)) sender.sendMessage(Main.format("c", Main.lang("action-prohibited")));
			else {
				YamlConfiguration flconfig = Storage.get(cfg.flags());
				List<String> flags = tconfig.getStringList("Flags");
				int id = -1;
				for (int i = 0; i < flags.size(); i++) {
					if (id == -1) {
						String[] flag = flags.get(i).split(",");
						if (flag[0].substring(1).equalsIgnoreCase(set.substring(1))) id = i;
					}
				}
				if (id == -1) flags.add(set);
				else {
					if (set.startsWith("-") && flconfig.getBoolean(set.substring(1) + "-default") == false) flags.remove(id);
					else if (set.startsWith("+") && flconfig.getBoolean(set.substring(1) + "-default") == true) flags.remove(id);
					else flags.set(id, set);
				}
				flags.sort(new FlagComparator());
				tconfig.set("Flags", flags);
				try { tconfig.save(tconf); } catch (Exception e) { System.out.println("Can't save claim file!"); }
				sender.sendMessage(Main.format("3", Main.lang("flag-set").replace("%flag", set).replace("%claim", tconfig.getString("Name"))));
			}
		}
	}
	
	public static void SetFlag(File tconf, String set) {
		if (ValidateFlagSyntax(set) && ValidateFlag(set)) {
			FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
			List<String> flags = tconfig.getStringList("Flags");
			int id = -1;
			
			for (int i = 0; i < flags.size(); i++) {
				if (id == -1) {
					String[] flag = flags.get(i).split(",");
					if (flag[0].substring(1).equalsIgnoreCase(set.substring(1))) id = i;
				}
			}
			
			if (id == -1) flags.add(set);
			else flags.set(id, set);
			flags.sort(new FlagComparator());
			tconfig.set("Flags", flags);
			try { tconfig.save(tconf); } catch (Exception e) { System.out.println("Can't save claim file!"); }
		}
	}
	
	public static class FlagComparator implements Comparator<String> {
	    @Override
	    public int compare(String o1, String o2) {
	        o1 = o1.substring(1);
	        o2 = o2.substring(1);
	        return o1.compareToIgnoreCase(o2);
	    }
	}
	
	public static class MembersComparator implements Comparator<String> {
	    @Override
	    public int compare(String o1, String o2) {
	    	if (o1.split(",")[1].equals(o2.split(",")[1])) return o1.compareToIgnoreCase(o2);
	    	else return o1.split(",")[1].compareTo(o2.split(",")[2]);
	    }
	}
	
	public static boolean ValidateFlagSyntax(String flag) {
		if (flag.startsWith("+") || flag.startsWith("-") || flag.startsWith("!") || flag.startsWith("@")) return true;
		return false;
	}
	
	public static boolean ValidateFlag(String flag) {
		File conffile = new File(cfg.flags());
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(conffile);
		if (conf.getKeys(false).contains(flag.substring(1).toLowerCase() + "-desc")) return true;
		return false;
	}
	
	public static boolean HasActiveFlag (Chunk ch, String flag) {
		if (Main.flags.getString(flag + "-perm").equalsIgnoreCase("D")) return false;
		if (new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml").exists()) {
			YamlConfiguration tconf = Storage.get("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
			List<String> flags = tconf.getStringList("Flags");
			if (flags.contains("+" + flag) || flags.contains("@" + flag)) return true;
			if (flags.contains("-" + flag) || flags.contains("!" + flag)) return false;
			return Main.flags.getBoolean(flag + "-default");
		} else return false;
	}
	
	public static boolean HasActiveFlag (YamlConfiguration tconf, String flag) {
		if (Main.flags.getString(flag + "-perm").equalsIgnoreCase("D")) return false;
		List<String> flags = tconf.getStringList("Flags");
		if (flags.contains("+" + flag) || flags.contains("@" + flag)) return true;
		if (flags.contains("-" + flag) || flags.contains("!" + flag)) return false;
		return Main.flags.getBoolean(flag + "-default");
	}
	
	@SuppressWarnings("deprecation")
	public static void Claim(Player target, String type) {
		Chunk ch = target.getLocation().getChunk();
		File tconf = new File("plugins/TerrainClaim/claims/" + target.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
		if (tconf.exists()) target.sendMessage(Main.format("4", Main.lang("already-claimed")));
		else {
			try {
				if (!tconf.exists()) tconf.createNewFile();
			} catch (IOException ex) {
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
			tconfig.set("Flags", new ArrayList<String>());
			
			try {
				tconfig.save(tconf);
			} catch (IOException ex) {
				System.out.println("[TerrainClaim] Config file saving error.");
			}
			
			List<String> tereny = Storage.get(cfg.claims()).getStringList("Terrains");
			tereny.add(target.getWorld().getName() + ";" + ch.getX() + ";" + ch.getZ() + ";" + target.getUniqueId().toString() + ";" + target.getWorld().getName() + "," + ch.getX() + "," + ch.getZ() + ";" + type);
			Storage.setclaims(tereny);
				
			try {
				if (Storage.get(cfg.experimental()).getBoolean("PlaySound")) target.getWorld().playSound(target.getLocation().add(0, 1, 0), Sound.ENTITY_WITHER_AMBIENT, 1, 0);
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println(ChatColor.RED + "Disable PlaySound in experimental config!!!");
			}
			
			try {
				if (Storage.get(cfg.experimental()).getBoolean("PlayEffect")) {
					for (int a = 0; a < 500; a++) {
						target.getWorld().playEffect(target.getLocation().add(0, 1, 0), Effect.CLOUD, 5);
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				System.out.println(ChatColor.RED + "Disable PlayEffect in experimental config!!!");
			}
			target.sendMessage(Main.format("3", Main.lang("claim-done")));
		}
	}
	
	protected static void GenerateLang(String name) {
		File langfile = new File("plugins/TerrainClaim/lang/" + name + ".yml");
		if (langfile.exists()) {
			FileConfiguration langconf = YamlConfiguration.loadConfiguration(langfile);
			if (langconf.getInt("DO-NOT-CHANGE-lang-ver") != Main.LangVersion) langfile.delete();
		}
		if (!langfile.exists()) Extract("resources/" + name + ".yml", "plugins/TerrainClaim/lang/" + name + ".yml");
	}
	
	protected static void GenerateConfig(String name) {
		File cfile = new File("plugins/TerrainClaim/" + name + ".yml");
		if (!cfile.exists()) Extract("configs/" + name + ".yml", "plugins/TerrainClaim/" + name + ".yml");
	}
	
	@SuppressWarnings("deprecation")
	private static void Extract(String source, String target) {
		try {
			JarFile file = new JarFile(URLDecoder.decode(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()));
			ZipEntry entry = file.getEntry(source);
			InputStream inputStream = file.getInputStream(entry);
			
			Files.copy(inputStream, Paths.get(target));
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ItemStack getTerrainBlock() {
		ItemStack BlokTerenu = new ItemStack(Material.getMaterial(Main.config.getString("TerrainBlock")), 1);
		
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(BlokTerenu.getType());
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Main.config.getString("TerrainBlockName")));
		
		List<String> desc = new ArrayList<String>();
		List<String> lore = new ArrayList<String>();
		desc.clear();
		desc = Main.config.getStringList("TerrainBlockLore");
		
		for (String l:desc) {
			lore.add(ChatColor.translateAlternateColorCodes('&', l));
		}
		
		meta.setLore(lore);
		BlokTerenu.setItemMeta(meta);
		
		return BlokTerenu;
	}
	
	protected static void MigrateConfig() {
		if (Storage.getfile(cfg.OLDconfig()).exists()) {
			System.out.println("[TerrainClaim] Updating config version...");
			
			YamlConfiguration file = Storage.get(cfg.OLDconfig());
			YamlConfiguration c = Storage.get(cfg.config());
			List<String> tereny = file.getStringList("Terrains");
			
			System.out.println("[TerrainClaim] Updating claims...");
			
			for (int i = 0; i < tereny.size(); i++) {
				System.out.println("[TerrainClaim] Updating claims [ " + (i + 1) + "/" + tereny.size() + " ]");
				
				tereny.set(i, tereny.get(i) + ";B");
				String[] sp = tereny.get(i).split(";");
				
				File tconf = new File("plugins/TerrainClaim/claims/" + sp[0] + "/" + sp[1] + "," + sp[2] + ".yml");
				
				System.out.println("[TerrainClaim] Updating claim " + sp[0] + "/" + sp[1] + "," + sp[2]);
				
				FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
				
				tconfig.addDefault("Method", "B");
				tconfig.options().copyDefaults(true);
				
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
		
		FileConfiguration claim = Storage.get(cfg.claims());
		List<String> claims = claim.getStringList("Terrains");
		
		if (claims.size() > 0) {
			if (!claims.get(0).split(";")[3].contains("-")) {
				System.out.println("[TerrainClaim] Converting nicknames into UUIDs...");
				for (int i = 0; i < claims.size(); i++) {
					if (!claims.get(i).split(";")[3].contains("-")) {
						System.out.println("[TerrainClaim] Converting nicknames of claim [ " + (i + 1) + "/" + claims.size() + " ]");
						String[] c = claims.get(i).split(";");
						claims.set(i, c[0] + ";" + c[1] + ";" + c[2] + ";" + GetUUID(c[3]) + ";" + c[4] +";" + c[5]);
						
						FileConfiguration conf = YamlConfiguration.loadConfiguration(new File("plugins/TerrainClaim/claims/" + c[0] + "/" + c[1] + "," + c[2] + ".yml"));
						conf.set("Owner", GetUUID(conf.getString("Owner")));
						
						if (conf.getStringList("Allowed").size() > 0) {
							List<String> allowed = conf.getStringList("Allowed");
							
							for (int j = 0; j < allowed.size(); j++) {
								allowed.set(j, GetUUID(allowed.get(j).split(",")[0]) + "," + allowed.get(j).split(",")[1]);
							}
							
							conf.set("Allowed", allowed);
						}
						
						try {
							conf.save(new File("plugins/TerrainClaim/claims/" + c[0] + "/" + c[1] + "," + c[2] + ".yml"));
						} catch (IOException e) {
							System.out.println("[TerrainClaim] Can't save file: plugins/TerrainClaim/claims/" + c[0] + "/" + c[1] + "," + c[2] + ".yml");
							e.printStackTrace();
						}
					}
				}
				
				System.out.println("[TerrainClaim] Nicknames converted, saving claims list...");
				Storage.setclaims(claims);
				System.out.println("[TerrainClaim] List saved.");
				System.out.println("[TerrainClaim] All nicknames converted into UUIDs.");
			}
		}
		
		YamlConfiguration conf = Storage.get(cfg.config());
		
		conf.addDefault("AnyoneCanAttackMobs", false);
		conf.addDefault("SuppressDenyMessages", false);
		conf.addDefault("SuppressCommandDenyMessages", false);
		conf.addDefault("SuppressEnterMessages", false);
		conf.addDefault("SuppressLeaveMessages", false);
		conf.addDefault("SuppressEnterLeaveMessages", false);
		
		conf.options().copyDefaults(true);
		Storage.save(cfg.config(), conf);
		
		if (!Storage.get(cfg.aliases()).getKeys(false).contains("alias-manage")) {
			try {
			    Files.write(Paths.get(cfg.aliases()), "alias-manage: \"\"\n".getBytes(), StandardOpenOption.APPEND);
			    Files.write(Paths.get(cfg.aliases()), "alias-dev: \"\"\n".getBytes(), StandardOpenOption.APPEND);
			    System.out.println("[TerrainClaim] Updated alias config to newer version - added 2 keys (manage and dev).");
			} catch (IOException e) {
			    System.out.println("[TerrainClaim] Can't append alias file!");
			}
		}
		
		if (!Storage.get(cfg.aliases()).getKeys(false).contains("alias-flag")) {
			YamlConfiguration c = Storage.get(cfg.claims());
			List<String> tereny = c.getStringList("Terrains");
			
			for (int i = 0; i < tereny.size(); i++) {
				System.out.println("[TerrainClaim] Updating flags of claims [ " + (i + 1) + "/" + tereny.size() + " ]");
				String[] sp = tereny.get(i).split(";");
				File tconf = new File("plugins/TerrainClaim/claims/" + sp[0] + "/" + sp[1] + "," + sp[2] + ".yml");
				System.out.println("[TerrainClaim] Updating claim " + sp[0] + "/" + sp[1] + "," + sp[2]);
				FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
				tconfig.addDefault("Flags", new ArrayList<String>());
				tconfig.options().copyDefaults(true);
				
				try {
					tconfig.save(tconf);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			System.out.println("[TerrainClaim] Claims updated and saved");
			
			try {
			    Files.write(Paths.get(cfg.aliases()), "alias-flag: \"\"\n".getBytes(), StandardOpenOption.APPEND);
			    Files.write(Paths.get(cfg.aliases()), "alias-validate: \"\"\n".getBytes(), StandardOpenOption.APPEND);
			    Files.write(Paths.get(cfg.aliases()), "alias-transfer: \"\"\n".getBytes(), StandardOpenOption.APPEND);
			    System.out.println("[TerrainClaim] Updated alias config to newer version - added 3 keys (flag, validate and transfer).");
			}catch (IOException e) {
			    System.out.println("[TerrainClaim] Can't append alias file!");
			}
		}

		//TODO: Kick subcommand
	}
	
	public static void ProcessClaims() {
		YamlConfiguration c = Storage.get(cfg.claims());
		List<String> tereny = c.getStringList("Terrains");
		System.out.println("[TerrainClaim] Performing claims validation...");
		YamlConfiguration aflags = Storage.get(cfg.flags());
		
		for (int i = 0; i < tereny.size(); i++) {
			System.out.println("[TerrainClaim] Validating claims [ " + (i + 1) + "/" + tereny.size() + " ]");
			String[] sp = tereny.get(i).split(";");
			File tconf = new File("plugins/TerrainClaim/claims/" + sp[0] + "/" + sp[1] + "," + sp[2] + ".yml");
			System.out.println("[TerrainClaim] Validating claim " + sp[0] + "/" + sp[1] + "," + sp[2]);
			YamlConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
			List<String> flags = tconfig.getStringList("Flags");
			Set<String> toremove = new HashSet<String>();
			for (String flag : flags) {
				if ((flag.startsWith("+") && aflags.getBoolean(flag.substring(1) + "-default") == true) || flag.startsWith("-") && aflags.getBoolean(flag.substring(1) + "-default") == false) toremove.add(flag);
				else if (aflags.getString(flag.substring(1) + "-perm").equalsIgnoreCase("D")) toremove.add(flag);
			}
			flags.removeAll(toremove);
			flags.sort(new FlagComparator());
			tconfig.set("Flags", flags);
			List<String> members = tconfig.getStringList("Allowed");
			members.sort(new MembersComparator());
			tconfig.set("Allowed", members);
			Storage.save("plugins/TerrainClaim/claims/" + sp[0] + "/" + sp[1] + "," + sp[2] + ".yml", tconfig);
			
			try {
				tconfig.save(tconf);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("[TerrainClaim] Claims validated and saved.");
	}
	
	private static void Copy(String value, YamlConfiguration old, YamlConfiguration n) {
		n.set(value, old.get(value));
	}
	
	public static void FormatListMessage(CommandSender target, String name, String type, String owner) {
		String pref = "";
		if (!((Player) target).getUniqueId().toString().equalsIgnoreCase(owner)) pref = owner + ":";
		
		new FancyMessage(ChatColor.GRAY + "- ")
        .then(name + " " + ChatColor.translateAlternateColorCodes('&', type))
        .command("/tr tp " + pref + name)
        .tooltip(ChatColor.translateAlternateColorCodes('&', Main.lang("list-tooltip")))
        .send(target);
	}
	
	public static void SendManageMessage(CommandSender target, String nick) {
		nick = GetNickname(nick);
		
		new FancyMessage(ChatColor.AQUA + "- ")
	     .then(nick)
	     .command("/tr manage " + nick)
	     .tooltip(ChatColor.translateAlternateColorCodes('&', Main.lang("info-tooltip")))
	     .send((Player) target);
	}
	
	public static void FormatManageMessage(CommandSender target, String nick, Boolean chunkOwner) {
		target.sendMessage(Main.lang("info-clicked"));
		
		new FancyMessage("")
        .then(Main.lang("info-menu-remove"))
        .command("/tr remove " + nick)
        .tooltip(ChatColor.translateAlternateColorCodes('&', Main.lang("info-menu-tooltip")))
        .send(target);
		
		if (chunkOwner || target.hasPermission("terrain.remove.others.recursive")) {
			new FancyMessage("")
	        .then(Main.lang("info-menu-remove-all"))
	        .command("/tr remove " + nick + " -" + (chunkOwner?"a":"r"))
	        .tooltip(ChatColor.translateAlternateColorCodes('&', Main.lang("info-menu-tooltip")))
	        .send(target);
		}
		
		target.sendMessage("");
		
		new FancyMessage("")
        .then(Main.lang("info-menu-helper"))
        .command("/tr add " + nick + " 0")
        .tooltip(ChatColor.translateAlternateColorCodes('&', Main.lang("info-menu-tooltip")))
        .send(target);
		
		if (chunkOwner || target.hasPermission("terrain.add.others.recursive")) {
			new FancyMessage("")
	        .then(Main.lang("info-menu-helper-all"))
	        .command("/tr add " + nick + " 0 -" + (chunkOwner?"a":"r"))
	        .tooltip(ChatColor.translateAlternateColorCodes('&', Main.lang("info-menu-tooltip")))
	        .send(target);
		}
		
		new FancyMessage("")
        .then(Main.lang("info-menu-member"))
        .command("/tr add " + nick + " 1")
        .tooltip(ChatColor.translateAlternateColorCodes('&', Main.lang("info-menu-tooltip")))
        .send(target);
		
		if (chunkOwner || target.hasPermission("terrain.add.others.recursive")) {
			new FancyMessage("")
	        .then(Main.lang("info-menu-member-all"))
	        .command("/tr add " + nick + " 1 -" + (chunkOwner?"a":"r"))
	        .tooltip(ChatColor.translateAlternateColorCodes('&', Main.lang("info-menu-tooltip")))
	        .send(target);
		}
		
		new FancyMessage("")
        .then(Main.lang("info-menu-admin"))
        .command("/tr add " + nick + " 2")
        .tooltip(ChatColor.translateAlternateColorCodes('&', Main.lang("info-menu-tooltip")))
        .send(target);
		
		if (chunkOwner || target.hasPermission("terrain.add.others.recursive")) {
			new FancyMessage("")
	        .then(Main.lang("info-menu-admin-all"))
	        .command("/tr add " + nick + " 2 -"+ (chunkOwner?"a":"r"))
	        .tooltip(ChatColor.translateAlternateColorCodes('&', Main.lang("info-menu-tooltip")))
	        .send(target);
		}
	}
	
	public static void PrintFlags(Player p, List<String> flags) {
		YamlConfiguration flg = Storage.get(cfg.flags());
		Set<String> aflags = flg.getKeys(false);
		Boolean printdesc = !Main.prefs.getStringList("NoFlagDesc").contains(GetUUID(p));
		
		p.sendMessage("");
		p.sendMessage(Main.lang("flag-help-header"));
		p.sendMessage(Main.lang("flag-help-class-A"));
		p.sendMessage(Main.lang("flag-help-class-B"));
		p.sendMessage(Main.lang("flag-help-class-C"));
		p.sendMessage(Main.lang("flag-help-class-D"));
		p.sendMessage(Main.lang("flag-help-ast"));
		new FancyMessage("")
        .then(Main.lang("flag-printing-desc") + (printdesc?Main.lang("flag-enabled"):Main.lang("flag-disabled")))
        .command("/tr printdesc")
        .tooltip(ChatColor.translateAlternateColorCodes('&', Main.lang("flag-menu-tooltip")))
        .send(p);
		p.sendMessage("");
		
		for (String flag : aflags) {
			if (flag.endsWith("-desc")) {
				flag = flag.replace("-desc", "");
				if (flags.contains("+" + flag)) PrintFlag(p, flag, flg, true, true, false, printdesc);
				else if (flags.contains("-" + flag)) PrintFlag(p, flag, flg, false, true, false, printdesc);
				else if (flags.contains("!" + flag)) PrintFlag(p, flag, flg, false, true, true, printdesc);
				else if (flags.contains("@" + flag)) PrintFlag(p, flag, flg, true, true, true, printdesc);
				else PrintFlag(p, flag, flg, flg.getBoolean(flag.replace("-desc", "") + "-default"), false, false, printdesc);
			}
		}
		p.sendMessage("");
	}
	
	public static void PrintFlag(Player p, String flag, YamlConfiguration flags, Boolean value, Boolean isSet, Boolean forced, Boolean printdesc) {
		String msg;
		flag = flag.replace("-desc", "");
		Boolean permitted = false;
		String perm = flags.getString(flag + "-perm");
		
		if (value && isSet) msg = Main.lang("flag-enabled");
		else if (value) msg = Main.lang("flag-enabled-default");
		else if (isSet) msg = Main.lang("flag-disabled");
		else msg = Main.lang("flag-disabled-default");
		
		if (forced) msg += " " + Main.lang("flag-forced");
		if (perm.equalsIgnoreCase("A")) {
			msg += ChatColor.GRAY;
			permitted = p.hasPermission("terrain.player") || p.hasPermission("terrain.flag") || p.hasPermission("terrain.admin");
		} else if (perm.equalsIgnoreCase("B")) {
			msg += ChatColor.YELLOW;
			permitted = p.hasPermission("terrain.admin") || p.hasPermission("terrain.restricted.flag." + flag);
		}
		else if (perm.equalsIgnoreCase("C")) {
			msg += ChatColor.GOLD;
			permitted = p.hasPermission("terrain.admin") || p.hasPermission("terrain.admin.flag." + flag);
		}
		else if (perm.equalsIgnoreCase("D")) msg += ChatColor.WHITE + "" + ChatColor.STRIKETHROUGH;
		
		new FancyMessage(ChatColor.DARK_GRAY + "- ")
        .then(msg + " " + flag + (permitted?"":(ChatColor.RED + "*")) + (printdesc?(ChatColor.WHITE + " (" + flags.getString(flag + "-desc") + ")"):""))
        .command(forced?(value?("/tr flag !" + flag):("/tr flag @" + flag)):(value?("/tr flag -" + flag):("/tr flag +" + flag)))
        .tooltip(ChatColor.translateAlternateColorCodes('&', Main.lang("flag-menu-tooltip")))
        .send(p);
	}
	
	@SuppressWarnings("deprecation")
	public static String GetUUID(String nick) {
		try {
			OfflinePlayer p = Bukkit.getOfflinePlayer(nick);
			CacheUUID(p.getUniqueId().toString(), nick);
			return p.getUniqueId().toString();
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String GetUUID(Player target) {
		try {
			CacheUUID(target.getUniqueId().toString(), target.getName());
			return target.getUniqueId().toString();
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String GetUUID(CommandSender target) {
		if (target instanceof Player) {
			try {
				CacheUUID(((Player) target).getUniqueId().toString(), target.getName());
				return ((Player) target).getUniqueId().toString();
			} catch (Exception e) {
				return null;
			}
		}
		else return null;
	}
	
	public static String GetNickname(String uuid) {
		try {
			String nickname = Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
			if (nickname == null) nickname = QueryCache(uuid);
			if (nickname == null) return uuid;
			else return nickname;
		} catch (Exception e) {
			return uuid;
		}
		
	}
	
	public static String GetNickname(UUID uuid) {
		try {
			String nickname = Bukkit.getOfflinePlayer(uuid).getName();
			if (nickname == null) nickname = QueryCache(uuid.toString());
			if (nickname == null) return uuid.toString();
			else return nickname;
		} catch (Exception e) {
			return uuid.toString();
		}
	}
	
	public static void LoadCache() {
		if (!Storage.getfile(cfg.UUID()).exists()) {
			try {
				Storage.getfile(cfg.UUID()).createNewFile();
			} catch (Exception ex) {}
		}
		FileConfiguration c = Storage.get(cfg.UUID());
		for(String str : c.getKeys(true)) {
			uuid.put(str, c.getString(str));
		}
	}
	
	public static void SaveCache() {
		YamlConfiguration c = Storage.get(cfg.UUID());
		for(Entry<String, String> ui : uuid.entrySet()) {
			c.set(ui.getKey(), ui.getValue());
		}
		Storage.save(cfg.UUID(), c);
		System.out.println("[TerrainClaim] UUID cache saved.");
	}
	
	public static String QueryCache(String UUID) {
		if (uuid.containsKey(UUID)) return uuid.get(UUID);
		else return null;
	}
	
	public static void UpdateCacheFile(String UUID, String nickname) {
		YamlConfiguration c = Storage.get(cfg.UUID());
		c.set(UUID, nickname);
		Storage.save(cfg.UUID(), c);
	}
	
	public static void CacheUUID(String UUID, String nickname) {		
		if (uuid.containsKey(UUID)) {
			if (!uuid.get(UUID).equalsIgnoreCase(nickname)) uuid.replace(UUID, nickname);
		}
		else uuid.put(UUID, nickname);
		UpdateCacheFile(UUID, nickname);
	}
	
	public static void ReloadCommandBlacklist() {
		Main.CommandBlacklist = new HashSet<String>();
		List<String> ls = Storage.get(cfg.protection()).getStringList("CommandBlacklist");
		Main.CommandBlacklist.addAll(ls);
	}
}

package net.zabszk.terrain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Effect;
//import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
//import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
//import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Event  implements Listener
{
	@EventHandler (priority = EventPriority.MONITOR)
	public void onPlace (BlockPlaceEvent e)
	{	
		if (!Main.permitted(e.getBlock().getChunk(), e.getPlayer(), 1))
		{
			e.getPlayer().sendMessage(Main.format("4", Main.lang("action-blocked")));
			
			e.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onBreak (BlockBreakEvent e)
	{	
		if (!Main.permitted(e.getBlock().getChunk(), e.getPlayer(), 1))
		{
			e.getPlayer().sendMessage(Main.format("4", Main.lang("action-blocked")));
			
			e.setCancelled(true);
		}
		else
		{
			if (e.getBlock().getType() == Material.BEDROCK)
			{
				Chunk ch = e.getBlock().getLocation().getChunk();
				
				File tconf = new File("plugins/TerrainClaim/claims/" + e.getPlayer().getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
				if (tconf.exists())
				{
					FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
					
					Location l = e.getBlock().getLocation();
					
					if (l.getWorld().getName().equals(tconfig.getString("world")) && l.getBlockX() == tconfig.getInt("X") && l.getBlockY() == tconfig.getInt("Y") && l.getBlockZ() == tconfig.getInt("Z"))
					{
						e.getPlayer().sendMessage(Main.format("4", Main.lang("click-right-to-remove")));
						
						e.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent e) {
       
        if (e.getEntity() instanceof Creeper && !Main.config.getBoolean("Enable-Creeper")) {
        	Chunk ch = e.getEntity().getLocation().getChunk();
    		File tconf = new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
    		
    		if (tconf.exists()) e.setCancelled(true);
        }
    }
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onBucketFill (PlayerBucketFillEvent e)
	{	
		if (!Main.permitted(e.getBlockClicked().getChunk(), e.getPlayer(), 1))
		{
			e.getPlayer().sendMessage(Main.format("4", Main.lang("action-blocked")));
			
			e.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onBucketEmpty (PlayerBucketEmptyEvent e)
	{	
		if (!Main.permitted(e.getBlockClicked().getChunk(), e.getPlayer(), 1))
		{
			e.getPlayer().sendMessage(Main.format("4", Main.lang("action-blocked")));
			
			e.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void onMove(PlayerMoveEvent e)
	{
		if (!e.getFrom().getChunk().equals(e.getTo().getChunk()))
		{
			File fromc = new File("plugins/TerrainClaim/claims/" + e.getFrom().getWorld().getName() + "/" + e.getFrom().getChunk().getX() + "," + e.getFrom().getChunk().getZ() + ".yml");
			File toc = new File("plugins/TerrainClaim/claims/" + e.getTo().getWorld().getName() + "/" + e.getTo().getChunk().getX() + "," + e.getTo().getChunk().getZ() + ".yml");
			
			if (toc.exists() || fromc.exists())
			{
				FileConfiguration from = YamlConfiguration.loadConfiguration(fromc);
				FileConfiguration to = YamlConfiguration.loadConfiguration(toc);
				
				if (toc.exists() && fromc.exists())
				{
					if (!from.getString("Owner").equalsIgnoreCase(to.getString("Owner")))
						e.getPlayer().sendMessage(Main.format("3", Main.lang("chunk-enter").replace("%nickl", from.getString("Owner")).replace("%nickw", to.getString("Owner"))));
				}
				else
				{
					if (fromc.exists()) e.getPlayer().sendMessage(Main.format("3", Main.lang("chunk-leave").replace("%nick", from.getString("Owner"))));
					else e.getPlayer().sendMessage(Main.format("3", Main.lang("chunk-enter").replace("%nick", to.getString("Owner"))));
				}
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onEntityDamage(EntityDamageByEntityEvent e)
	{
		Chunk ch = e.getEntity().getLocation().getChunk();
		File tconf = new File("plugins/TerrainClaim/claims/" + ch.getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
		
		if (tconf.exists())
		{
			Main mn = Main.getInstance();
			
		    if (e.getDamager() instanceof Player)
		    {
		    	if (e.getEntity() instanceof Player)
		    	{
		    		if (!Main.config.getBoolean("Enable-PvP"))
		    		{
				    	if (!Main.config.getBoolean("AddedVsNonadded") || !Main.permitted(e.getEntity().getLocation().getChunk(), (Player) e.getDamager(), 1))
				    	{
				    		((Player) e.getDamager()).sendMessage(Main.format("4", Main.lang("action-blocked")));
							
							e.setCancelled(true);
				    	}
		    		}
		    	}
		    	else
		    	{
		    		if (!Main.permitted(e.getEntity().getLocation().getChunk(), (Player) e.getDamager(), 1))
			    	{
			    		((Player) e.getDamager()).sendMessage(Main.format("4", Main.lang("action-blocked")));
						
						e.setCancelled(true);
			    	}
		    	}
		    }
		    else if (e.getEntity() instanceof Player)
		    {
		    	if (!Main.config.getBoolean("Enable-PvE")) e.setCancelled(true);
		    }
		    else if (!(Main.config.getBoolean("Enable-EvE"))) e.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.MONITOR)
	public void onInteract (PlayerInteractEvent e)
	{
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK)
		{
			if (e.getPlayer().getItemInHand().getType() == Material.getMaterial(Main.config.getString("TerrainBlock")))
			{
				ItemStack i = e.getPlayer().getItemInHand();
				ItemStack b = Main.BlokTerenu;
				
				ItemMeta im = i.getItemMeta();
				ItemMeta bm = b.getItemMeta();
				
				if (im.equals(bm))
				{
					e.setCancelled(true);
					
					if (e.getBlockFace() != BlockFace.UP) e.getPlayer().sendMessage(Main.format("4", Main.lang("click-on-top")));
					else
					{
						if (!Main.config.getBoolean("CheckForWorldGuardRegions") || Main.getWorldGuard() == null || Main.getWorldGuard().canBuild(e.getPlayer(), e.getClickedBlock().getLocation()))
						{
							if (Main.CheckWorld(e.getClickedBlock().getWorld()))
							{
								if (Main.Perm("claim", (CommandSender) e.getPlayer(), true, true))
								{
									Chunk ch = e.getClickedBlock().getLocation().getChunk();
									
									File tconf = new File("plugins/TerrainClaim/claims/" + e.getPlayer().getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
									if (tconf.exists()) e.getPlayer().sendMessage(Main.format("4", Main.lang("already-claimed")));
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
										
										Location l = e.getClickedBlock().getLocation().add(0, 1, 0);
										
										tconfig.set("Name", e.getPlayer().getWorld().getName() + "," + ch.getX() + "," + ch.getZ());
										tconfig.set("Owner", e.getPlayer().getName());
										tconfig.set("Allowed", new ArrayList<String>());
										tconfig.set("world", e.getPlayer().getWorld().getName());
										tconfig.set("X", l.getBlockX());
										tconfig.set("Y", l.getBlockY());
										tconfig.set("Z", l.getBlockZ());
										tconfig.set("Chunk", ch.getX() + "," + ch.getZ());
										
										try
										{
											tconfig.save(tconf);
										}
										catch (IOException ex)
										{
											System.out.println("[TerrainClaim] Config file saving error.");
										}
										
										File configfile = new File("plugins/TerrainClaim/terrains.yml");
										FileConfiguration config = YamlConfiguration.loadConfiguration(configfile);
										
										List<String> tereny = (List<String>) config.getList("Terrains");
										
										tereny.add(e.getPlayer().getWorld().getName() + ";" + ch.getX() + ";" + ch.getZ() + ";" + e.getPlayer().getName() + ";" + e.getPlayer().getWorld().getName() + "," + ch.getX() + "," + ch.getZ());
										
										config.set("Terrains", tereny);
										
										try
										{
											config.save(configfile);
										}
										catch (IOException ex)
										{
											System.out.println("[TerrainClaim] Config file saving error.");
										}
										
										e.getPlayer().getWorld().getBlockAt(e.getClickedBlock().getLocation().add(0, 1, 0)).setType(Material.BEDROCK);
										
										//e.getPlayer().getWorld().playSound(e.getClickedBlock().getLocation().add(0, 1, 0), Sound.WITHER_IDLE, 1, 0);
										
										for (int a = 0; a < 500; a++)
										{
											e.getPlayer().getWorld().playEffect(e.getClickedBlock().getLocation().add(0, 1, 0), Effect.CLOUD, 5);
										}
																
										if (e.getPlayer().getGameMode() != GameMode.CREATIVE)
										{
											if (e.getPlayer().getItemInHand().getAmount() > 1) e.getPlayer().getItemInHand().setAmount(e.getPlayer().getItemInHand().getAmount() - 1);
											else e.getPlayer().setItemInHand(new ItemStack(Material.AIR));
										}
										
										e.getPlayer().sendMessage(Main.format("3", Main.lang("claim-done")));
									}
								}
							}
							else e.getPlayer().sendMessage(Main.format("4", Main.lang("claim-err-world")));
						}
						else e.getPlayer().sendMessage(Main.format("4", Main.lang("claim-WG")));
					}
					
					e.getPlayer().updateInventory();
				}
			}
			else if (e.getClickedBlock().getType() == Material.BEDROCK)
			{
				Chunk ch = e.getClickedBlock().getLocation().getChunk();
				
				File tconf = new File("plugins/TerrainClaim/claims/" + e.getPlayer().getWorld().getName() + "/" + ch.getX() + "," + ch.getZ() + ".yml");
				if (tconf.exists())
				{
					FileConfiguration tconfig = YamlConfiguration.loadConfiguration(tconf);
					
					Location l = e.getClickedBlock().getLocation();
					
					if (l.getWorld().getName().equals(tconfig.getString("world")) && l.getBlockX() == tconfig.getInt("X") && l.getBlockY() == tconfig.getInt("Y") && l.getBlockZ() == tconfig.getInt("Z"))
					{
						if (tconfig.getString("Owner").equalsIgnoreCase(e.getPlayer().getName()) || Main.Perm("unclaim.others", (CommandSender) e.getPlayer(), false, true))
						{
							if ((tconfig.getString("Owner").equalsIgnoreCase(e.getPlayer().getName()) && (e.getPlayer().getItemInHand() == null || e.getPlayer().getItemInHand().getTypeId() == 0)) || e.getPlayer().getItemInHand().getType() == Material.GOLD_AXE)
							{
								e.getPlayer().getWorld().getBlockAt(e.getClickedBlock().getLocation()).setType(Material.AIR);
								
								File configfile = new File("plugins/TerrainClaim/terrains.yml");
								FileConfiguration config = YamlConfiguration.loadConfiguration(configfile);
								
								List<String> tereny = (List<String>) config.getList("Terrains");
								
								String del = "";
								
								for (String t : tereny)
								{
									String[] s = t.split(";");
									
									if (s[0].equalsIgnoreCase(e.getPlayer().getWorld().getName()) && s[1].equals(Integer.toString(ch.getX())) && s[2].equals(Integer.toString(ch.getZ()))) del = t;
								}
								
								if (!del.equals("")) tereny.remove(del);
								
								config.set("Terrains", tereny);
								
								try
								{
									config.save(configfile);
								}
								catch (IOException ex)
								{
									System.out.println("[TerrainClaim] Config file saving error.");
								}
								
								tconf.delete();
								
								if (e.getPlayer().getGameMode() != GameMode.CREATIVE)
								{
									ItemStack bt = Main.BlokTerenu;
									
									bt.setAmount(1);
									
									e.getPlayer().getInventory().addItem(bt);
									e.getPlayer().updateInventory();
								}
								
								//e.getPlayer().getWorld().getBlockAt(e.getClickedBlock().getLocation()).setType(Material.AIR);

								//e.getPlayer().getWorld().playSound(e.getClickedBlock().getLocation().add(0, 1, 0), Sound.EXPLODE, 1, 0);
								
								for (int a = 0; a < 500; a++)
								{
									e.getPlayer().getWorld().playEffect(e.getClickedBlock().getLocation().add(0, 1, 0), Effect.EXPLOSION_HUGE, 5);
								}
								
								e.getPlayer().sendMessage(Main.format("b", Main.lang("claim-unclaimed")));
							}
							else
							{
								if (tconfig.getString("Owner").equalsIgnoreCase(e.getPlayer().getName())) e.getPlayer().sendMessage(Main.format("e", Main.lang("claim-use-empty-hand")));
								else e.getPlayer().sendMessage(Main.format("e", Main.lang("claim-use-golden-axe")));
							}
						} else e.getPlayer().sendMessage(Main.format("4", Main.lang("not-owner")));
					}
				}
			}
		}
		
		try
		{
			if (!Main.permitted(e.getClickedBlock().getLocation().getChunk(), e.getPlayer(), 0))
			{
				e.getPlayer().sendMessage(Main.format("4", Main.lang("action-blocked")));
				
				e.setCancelled(true);
			}
			else if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				if (!Main.permitted(e.getClickedBlock().getChunk(), e.getPlayer(), 0))
				{
					e.getPlayer().sendMessage(Main.format("4", Main.lang("action-blocked")));
					
					e.setCancelled(true);
				}
			}
		}
		catch (Exception ex) {}
	}
}

package org.eu.jezersek;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.eu.jezersek.http.WebServer;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NBTList;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class App extends JavaPlugin {
    private Database db;
    private InventoryManager inventoryManager;

    Mob minion;

    // Thread webServerThread;
    WebServer webServer;

    HashMap<String, Robot> robots = new HashMap<>();

    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("JavaScript");

    API api;

    @Override
    public void onEnable() {
        getLogger().info("Hello from jezersek.eu.org!");


        //set up web server
        webServer = new WebServer(this);
        //webServerThread = new Thread(webServer);
        //webServerThread.start();
        
        // register command: cc
        this.getCommand("cc").setExecutor(this);
        //register event listener
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        
        // register custom crafting recepie
        ItemStack robotSpawnEgg = makeRobotSpawnEgg(new ItemStack(Material.LEATHER_BOOTS),
                new ItemStack(Material.LEATHER_LEGGINGS), new ItemStack(Material.LEATHER_CHESTPLATE),
                new ItemStack(Material.LEATHER_HELMET));
        ShapelessRecipe robotRecepie = new ShapelessRecipe(new NamespacedKey(this, "codecraft"), robotSpawnEgg);
        robotRecepie.addIngredient(Material.IRON_BLOCK);
        robotRecepie.addIngredient(Material.GOLD_BLOCK);
        robotRecepie.addIngredient(new MaterialChoice(Material.LEATHER_BOOTS));
        robotRecepie.addIngredient(new MaterialChoice(Material.LEATHER_CHESTPLATE));
        robotRecepie.addIngredient(new MaterialChoice(Material.LEATHER_HELMET));
        robotRecepie.addIngredient(new MaterialChoice(Material.LEATHER_LEGGINGS));
        robotRecepie.addIngredient(Material.REPEATER);
        robotRecepie.addIngredient(Material.REDSTONE_TORCH);
        robotRecepie.addIngredient(Material.COMPARATOR);
        getServer().addRecipe(robotRecepie);

        getServer().getScheduler().scheduleSyncRepeatingTask(this, new TickHandler(this), 0L, 1L);
        
        //initialise JS engine api
        //api = new API();
        //engine.put("mc", api);

        // initialise database
        this.db = new SQLite(this);
        this.db.load();

        this.saveDefaultConfig();

        this.inventoryManager = new InventoryManager(this);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        try {
			webServer.httpServer.stop(0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		synchronized (webServer) {
			webServer.notifyAll();
		}
        //webServerThread.stop();
        getLogger().info("See you again, SpigotMC!");
    }

    // COMAND HANDLER
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // TEST command
        // just print some text to the console
        if (args[0].equals("test")) {
            sender.sendMessage(ChatColor.AQUA + "Hello CodeCraft!:)");
            TextComponent message = new TextComponent("Click me");
            message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://jezersek.eu.org"));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder("Visit jezersek.eu.org website!").create()));
            sender.spigot().sendMessage(message);
        }

        // Check that the commanc comes from player, not rcon for example
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to do that!");
            return true;
        }

        Player player = (Player) sender;

        // SUMMON MINION command
        // summons a Cow and saves its reference in this.minion
        if (args[0].equals("minion")) {
            ItemStack robotSpawnEgg = makeRobotSpawnEgg(new ItemStack(Material.LEATHER_BOOTS),
                new ItemStack(Material.LEATHER_LEGGINGS), new ItemStack(Material.LEATHER_CHESTPLATE),
                new ItemStack(Material.LEATHER_HELMET));
            player.getInventory().addItem(robotSpawnEgg);
        }

        if (args[0].equals("dig")) {
            player.getLocation().add(0, -1, 0).getBlock().breakNaturally();
        }

        if(args[0].equals("sight")){
            List<Block> sight = player.getLineOfSight(null, 3);

            Block b = player.getWorld().rayTraceBlocks(player.getEyeLocation(), player.getEyeLocation().getDirection(), 5, FluidCollisionMode.NEVER, false).getHitBlock();

            player.sendMessage(b.getType().toString());
            /*for(Block b : sight){
            }*/
        }

        if(args[0].equals("stop")){
            try {
                webServer.httpServer.stop(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
    
            synchronized (webServer) {
                webServer.notifyAll();
            }
        }

        // MOVE command
        // move minion around
        if (args[0].equals("move")) {
            Location location = minion.getLocation();
            location.setPitch(0);
            if (args[1].equals("forward")) {
                location.add(location.getDirection().normalize().multiply(1));
            } else if (args[1].equals("backward")) {
                location.add(location.getDirection().normalize().multiply(-1));
            } else if (args[1].equals("right")) {
                location.add(location.getDirection().getCrossProduct(new Vector(0, 1, 0)).normalize());
            } else if (args[1].equals("left")) {
                location.add(location.getDirection().getCrossProduct(new Vector(0, -1, 0)).normalize());
            }
            minion.teleport(location);
        }

        if (args[0].equals("run")) {
            String code = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

            Bukkit.getScheduler().runTaskAsynchronously(this, new Runnable() {
                @Override
                public void run() {
                    player.sendMessage("> " + ChatColor.DARK_GRAY + code);
                    try {
                        Object result = engine.eval(code);
                        if(result != null){
                            player.sendMessage(ChatColor.WHITE + result.toString());
                        }
                    } catch (ScriptException e) {
                        player.sendMessage(ChatColor.RED + e.toString());
                    }
                }
            });

            
        }
        
        return true;
    }


    // COMMAND TAB COMPLETE HANDLER
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args){
		if(command.getName().equalsIgnoreCase("cc")){  //your command name
            List<String> l = new ArrayList<String>(); //makes a ArrayList
        
            //define the possible possibility's for argument 1
            if (args.length==1){
                l.add("minion"); //Possibility #1
                l.add("move");
                l.add("run");
            }
            
            //define the possible possibility's for argument 2
            if(args[0].equals("move"))
            if (args.length==2){
                l.add("forward"); //Possibility #1
                l.add("backward");
                l.add("left");
                l.add("right");
            }
            return l; //returns the possibility's to the client
        }
		return null;            
    }


    // generate custom Spawn Egg

    public ItemStack makeRobotSpawnEgg(ItemStack boots, ItemStack leggings, ItemStack chestplate, ItemStack helmet){
        return makeRobotSpawnEgg(boots, leggings, chestplate, helmet, "");
    }
    public ItemStack makeRobotSpawnEgg(ItemStack boots, ItemStack leggings, ItemStack chestplate, ItemStack helmet, String entityId){
        ItemStack robotSpawnEgg = new ItemStack(Material.POLAR_BEAR_SPAWN_EGG, 1);
        
        SpawnEggMeta robotSpawnEggMeta = (SpawnEggMeta) robotSpawnEgg.getItemMeta();
        if(!entityId.equals("")) robotSpawnEggMeta.setDisplayName(ChatColor.WHITE+"Robot in an Egg");
        else robotSpawnEggMeta.setDisplayName(ChatColor.WHITE+"Robot Spawn Egg");
        LinkedList<String> lore = new LinkedList<>();
        lore.add("Programable Robot");
        lore.add("");
        lore.add("Helmet: " + ((LeatherArmorMeta) helmet.getItemMeta()).getColor().toString());
        lore.add("Chestplate: " + ((LeatherArmorMeta) chestplate.getItemMeta()).getColor().toString());
        lore.add("Leggings: " + ((LeatherArmorMeta) leggings.getItemMeta()).getColor().toString());
        lore.add("Boots: " + ((LeatherArmorMeta) boots.getItemMeta()).getColor().toString());
        robotSpawnEggMeta.setLore(lore);
        robotSpawnEgg.setItemMeta(robotSpawnEggMeta);

        // set nbt data
        NBTItem nbti = new NBTItem(robotSpawnEgg);
        NBTCompound tag = nbti.addCompound("EntityTag");
        nbti.getStringList("Tags").add("genuineRobotSpawnEgg");
        tag.setString("id", "minecraft:husk");
        tag.setString("CustomName", "\""+ChatColor.GOLD+"Robot\"");
        tag.setInteger("CustomNameVisible", 0);
        tag.setBoolean("NoAI", false);
        tag.setBoolean("PersistenceRequired", true);
        tag.setInteger("LeftHanded", 0);
        tag.setInteger("Silent", 1);
        tag.setInteger("IsBaby", 1);
        tag.setInteger("Invulnerable", 0);
        tag.setBoolean("FallFlying", true);
        tag.setIntArray("ArmorDropChances", new int[]{0,0,0,0});
        tag.setIntArray("HandDropChances", new int[]{0,0});
        //tag.setInteger("DrownedConversionTime", Integer.MAX_VALUE);
        //tag.setShort("Air", Short.MAX_VALUE); // prevent robot from shakng
        NBTList<String> tags = tag.getStringList("Tags");
        tags.add("genuineRobot");
        if(!entityId.equals(""))tags.add("oldInventoryId"+entityId);

        // set armor
        NBTCompoundList armor = tag.getCompoundList("ArmorItems");
        NBTCompound bootsNBT = armor.addCompound();
        bootsNBT.setString("id", "minecraft:leather_boots");
        bootsNBT.setInteger("Count", 1);
        bootsNBT.addCompound("tag").addCompound("display").setInteger("color", ((LeatherArmorMeta) boots.getItemMeta()).getColor().asRGB());
        NBTCompound leggingsNBT = armor.addCompound();
        leggingsNBT.setString("id", "minecraft:leather_leggings");
        leggingsNBT.setInteger("Count", 1);
        leggingsNBT.addCompound("tag").addCompound("display").setInteger("color", ((LeatherArmorMeta) leggings.getItemMeta()).getColor().asRGB());
        NBTCompound chestplateNBT = armor.addCompound();
        chestplateNBT.setString("id", "minecraft:leather_chestplate");
        chestplateNBT.setInteger("Count", 1);
        chestplateNBT.addCompound("tag").addCompound("display").setInteger("color", ((LeatherArmorMeta) chestplate.getItemMeta()).getColor().asRGB());
        NBTCompound helmetNBT = armor.addCompound();
        helmetNBT.setString("id", "minecraft:leather_helmet");
        helmetNBT.setInteger("Count", 1);
        helmetNBT.addCompound("tag").addCompound("display").setInteger("color", ((LeatherArmorMeta) helmet.getItemMeta()).getColor().asRGB());

        //set atributes (speed = 0, attack = 0)
        NBTCompoundList attributes = tag.getCompoundList("Attributes");
        NBTCompound speedNBT = attributes.addCompound();
        speedNBT.setString("Name", "generic.movementSpeed");
        speedNBT.setFloat("Base", 0.0f);

        NBTCompound attackNBT = attributes.addCompound();
        attackNBT.setString("Name", "generic.attackDamage");
        attackNBT.setInteger("Base", 0);

        NBTCompound followNBT = attributes.addCompound();
        followNBT.setString("Name", "generic.followRange");
        followNBT.setDouble("Base", 0d);

        NBTCompound reinforcementsNBT = attributes.addCompound();
        reinforcementsNBT.setString("Name", "zombie.spawnReinforcements");
        reinforcementsNBT.setInteger("Base", 0);

        //set effects
        NBTCompoundList effectsNBT = tag.getCompoundList("ActiveEffects");
        NBTCompound waterBreathing = effectsNBT.addCompound();
        waterBreathing.setInteger("Id", 13);
        waterBreathing.setInteger("Duration", Integer.MAX_VALUE);
        waterBreathing.setBoolean("ShowParticles", false);

        return nbti.getItem();
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public Robot getRobot(Zombie body){
        Robot robot = robots.getOrDefault(body.getUniqueId().toString(), null);
        if(robot == null){
            robot = new Robot(this, body);
            robots.put(body.getUniqueId().toString(), robot);
        }
        return robot;
    }

    public Robot getRobot(String id){
        Robot robot = robots.getOrDefault(id, null);

        return robot;
    }

    public void addRobot(Zombie body){
        robots.put(body.getUniqueId().toString(), new Robot(this, body));
    }

    public Database getDb() {
        return db;
    }

    public HashMap<String, Robot> getRobots(){
        return robots;
    }
}

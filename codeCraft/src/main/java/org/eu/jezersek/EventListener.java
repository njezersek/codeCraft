package org.eu.jezersek;

import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.util.Vector;

import de.tr7zw.nbtapi.NBTEntity;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NBTList;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class EventListener implements Listener {
    App plugin;

    EventListener(App plugin) {
        this.plugin = plugin;
    }

    // Book edit event
    // Make web interactable book
    @EventHandler
    public void playerEditBookEvent(PlayerEditBookEvent event) {
        if(event.isSigning()){
            BookMeta meta = event.getNewBookMeta();
            String content = String.join("",meta.getPages());
            //event.getPlayer().sendMessage(content);
            content = ChatColor.stripColor(content);
            if(content.equals("block program") || content.equals("program")){
                event.getPlayer().sendMessage("ok");
                String id = UUID.randomUUID().toString();
                plugin.getDb().setProgram(id, "", "");
                LinkedList<BaseComponent[]> pages = new LinkedList<>();
                BaseComponent[] second = new ComponentBuilder(id).create();
                
                pages.add(second);
                meta.spigot().setPages(pages);
            }
        }
    }

    // Offer link on web interactabl book use
    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent event){
        ItemStack item = event.getItem();
        if(item.getType().equals(Material.WRITTEN_BOOK)){
            BookMeta meta = (BookMeta) item.getItemMeta();
            String content = String.join("",meta.getPages());
            content = ChatColor.stripColor(content);
            String xml = plugin.getDb().getProgramXML(content);
            String js = plugin.getDb().getProgramJS(content);
            
            if(xml != null){
                event.getPlayer().sendMessage(js);
                TextComponent message = new TextComponent(ChatColor.GREEN+"[EDIT PROGRAM]");
                message.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://" + plugin.getConfig().getString("server-address") + ":" + plugin.getConfig().getInt("port") + "/?id=" + content));
                message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Edit this program in web editior.").create()));
                event.getPlayer().spigot().sendMessage(message);
                event.setCancelled(true);
            }
        }
    }

    // Get armor clor from crafting Robot Egg
    @EventHandler
    public void prepareItemCraftEvent(PrepareItemCraftEvent event) {

        CraftingInventory table = event.getInventory();
        ItemStack result = table.getResult();
        NBTItem nbti = new NBTItem(result);
        if (nbti.getStringList("Tags").contains("genuineRobotSpawnEgg")) {

            ItemStack helmet = (ItemStack) table.all(Material.LEATHER_HELMET).values().iterator().next();
            ItemStack chestplate = (ItemStack) table.all(Material.LEATHER_CHESTPLATE).values().iterator().next();
            ItemStack leggings = (ItemStack) table.all(Material.LEATHER_LEGGINGS).values().iterator().next();
            ItemStack boots = (ItemStack) table.all(Material.LEATHER_BOOTS).values().iterator().next();

            table.setResult(plugin.makeRobotSpawnEgg(boots, leggings, chestplate, helmet));
        }
    }

    // capture Robot spawn event and make some modificatioins
    @EventHandler (priority = EventPriority.HIGHEST)
    public void creatureSpawnEvent(CreatureSpawnEvent event){
        Entity entity = event.getEntity();
        NBTEntity nbte = new NBTEntity(entity);

        //Bukkit.getServer().broadcastMessage(ChatColor.AQUA+"Entity spawned "+nbte.getString("CustomName")+"reason: "+event.getSpawnReason().name());
        
        if(event.getSpawnReason().equals(SpawnReason.SPAWNER_EGG)){
            NBTList<String> tags = nbte.getStringList("Tags");
            
            if(!(entity instanceof Zombie))return;
            Zombie robotBody = (Zombie) entity; 
            if(tags.contains("genuineRobot")){
                // turn it to face the closest player
                Player result = null;
                double lastDistance = Double.MAX_VALUE;
                for(Player p : robotBody.getWorld().getPlayers()) {
                    double distance = robotBody.getLocation().distance(p.getLocation());
                    if(distance < lastDistance) {
                        lastDistance = distance;
                        result = p;
                    }
                }

                Vector direction = result.getLocation().subtract(robotBody.getLocation()).toVector();
                direction.setY(0);
                if(Math.abs(direction.getX()) > Math.abs(direction.getZ())){
                    direction.setZ(0);
                }
                else{
                    direction.setX(0);
                }

                robotBody.teleport(robotBody.getLocation().setDirection(direction));


                // Mak new Robot instance
                plugin.addRobot(robotBody);
            }

        }

    }

    /*// Prevent Robots (husks) from turning into Drowned
    @EventHandler
    public void entityAirChangeEvent(EntityAirChangeEvent event){
        Entity entity = event.getEntity();
        if(!(entity instanceof Zombie))return;
        Bukkit.getServer().broadcastMessage(ChatColor.BOLD+"Drowning prevented "+event.getAmount());
        NBTEntity nbte = new NBTEntity(entity);
        if(nbte.getStringList("Tags").contains("genuineRobot")){
            event.setCancelled(true);
        }
    }*/
    
    // When player hits a Robot, remove it and drop a Spawn Egg
    @EventHandler
    public void entityDamageByEntityEvent(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();

            Entity entity = event.getEntity();

            if(entity instanceof Zombie){
                Zombie robotBody = (Zombie) entity;

                EntityEquipment equipment = robotBody.getEquipment();
                
                
                NBTEntity nbte = new NBTEntity(entity);
                NBTList<String> tags = nbte.getStringList("Tags");
                if(!tags.contains("genuineRobot"))return; // only handle entities with tag genuineRobot

                plugin.getRobot(robotBody).stop(); // stop any program that may be still running
                
                ItemStack robotSpawnEgg = plugin.makeRobotSpawnEgg(equipment.getBoots(), equipment.getLeggings(), equipment.getChestplate(), equipment.getHelmet(), entity.getUniqueId().toString());
                entity.getWorld().dropItem(entity.getLocation(), robotSpawnEgg);
                
                entity.remove();
                
                player.sendMessage("Remove Robot");
            }
            
        }
    }

    // Prevent baby, zobmies spawning when Robot is clicket with Spawn Egg
    @EventHandler
    public void playerInteractEntityEvent(PlayerInteractEntityEvent event){
        Entity entity = event.getRightClicked();
        NBTEntity nbte = new NBTEntity(entity);
        NBTList<String> tags = nbte.getStringList("Tags");
        if(!tags.contains("genuineRobot"))return;
        event.setCancelled(true);
    }

    // Open robot inventory
    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerInteractAtEntityEvent(PlayerInteractAtEntityEvent event){
        if(event.isCancelled())return;
        if(event.getHand().equals(EquipmentSlot.OFF_HAND))return;
        
        Entity entity = event.getRightClicked();
        event.setCancelled(true);
        if(!(entity instanceof Zombie))return; // only handle events with Zombies (to reduce server lag)
        Zombie body = (Zombie) entity;
        NBTEntity nbte = new NBTEntity(entity);
        NBTList<String> tags = nbte.getStringList("Tags");
        if(!tags.contains("genuineRobot"))return; // only handle entities with tag genuineRobot

        //event.getPlayer().sendMessage("Opening Inventory of robot:"+ChatColor.AQUA+body.getUniqueId().toString());
        Robot robot = plugin.getRobot(body);
        Inventory robotInventory = robot.getInventory();
        //TmpInventoryHolder h = (TmpInventoryHolder) robotInventory.getHolder();
        //event.getPlayer().sendMessage("Opening Inventory of holder:"+ChatColor.RED+h.getEntityId());

        robot.stop(); // stop program
        if(robotInventory != null){
            // open inventory and cancle event
            event.getPlayer().openInventory(robotInventory);
        }
    }

    // Save robot inventory on close
    @EventHandler
    public void inventoryCloseEvent(InventoryCloseEvent event){
        HumanEntity player = event.getPlayer();
        //event.getPlayer().sendMessage("Close inventory event");
        InventoryHolder h = event.getInventory().getHolder();
        if(h instanceof TmpInventoryHolder){ // only handle RobotInventories
            TmpInventoryHolder holder = (TmpInventoryHolder) h;
            //event.getPlayer().sendMessage("Closing Inventory of robot:"+ChatColor.AQUA+holder.getEntityId());
            //Inventory robotsInventory = event.getInventory();
            Robot robot = plugin.getRobot(holder.getEntityId());
            
            if(robot != null){
                robot.saveInventory();
                //event.getPlayer().sendMessage("Saving Inventory of robot:"+ChatColor.AQUA+holder.getEntityId());
            }
            
            ItemStack[] contents = robot.getInventory().getContents();
            ItemStack book = null;
            //event.getPlayer().sendMessage("Inventory len:"+ChatColor.AQUA+contents.length);
            for(ItemStack item : contents){
                if(item == null)continue;
                //event.getPlayer().sendMessage(""+ChatColor.AQUA+item.getType().toString());
                if(item.getType().equals(Material.WRITABLE_BOOK) || item.getType().equals(Material.WRITTEN_BOOK)){
                    book = item;
                    break;
                }
            }

            // if there is a book in the inventory
            if(book != null){
                //event.getPlayer().sendMessage(ChatColor.DARK_BLUE+"Program found!:");
                BookMeta meta = (BookMeta) book.getItemMeta();
                String content = String.join("",meta.getPages());
                //content = content.replaceAll("§0", "");
                content = ChatColor.stripColor(content);
                //content = "mc.println(\"Hello World\");";
                String program = plugin.getDb().getProgramJS(content);
                if(program == null || program.equals(""))program = content;
                /*System.out.println("========== PROGRAM ==========");
                System.out.println(program);
                System.out.println("==========   END   ==========");*/

                robot.run(program, player);
                //event.getPlayer().sendMessage(program);
            }

        }
    }
}
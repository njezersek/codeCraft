package org.eu.jezersek;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import de.tr7zw.nbtapi.NBTEntity;
import de.tr7zw.nbtapi.NBTList;
import net.md_5.bungee.api.ChatColor;

/*
 This class represents a Robot
*/
class Robot implements InventoryHolder {
    private ScriptEngineManager manager = new ScriptEngineManager();
    private ScriptEngine engine;
    private App plugin;
    private API api;
    private Thread thread;
    private Inventory inventory;
    private Zombie body;
    private HumanEntity master;
    private Vector direction;
    private Location lastLocation;

    // create new robot with empty inventory
    Robot(App plugin, Zombie body) {
        this.plugin = plugin;
        this.body = body;

        NBTEntity nbte = new NBTEntity(body);
        NBTList<String> tags = nbte.getStringList("Tags");

        // check if tags contains old inventory id - this should be a copy of old robot
        // this tag should only be set by the Spawn Egg
        String oldInventoryId = "";
        for (int i = 0; i < tags.size(); i++) {
            String tag = tags.get(i);
            if (tag.startsWith("oldInventoryId")) {
                oldInventoryId = tag.substring("oldInventoryId".length(), tag.length());
                tags.remove(i); //
                i--;
            }
        }

        // if old robot id exists
        if (!oldInventoryId.equals("")) {
            inventory = plugin.getInventoryManager().get(oldInventoryId);
            inventory = InventorySerializator.copyInventory(inventory, "Robot's Inventory",
                    new TmpInventoryHolder(getID()));
        } else {
            inventory = plugin.getInventoryManager().get(getID(), "Robot's Inventory", 2);
        }
        setDirection();
        plugin.getInventoryManager().set(getID(), inventory);
    }

    public void run(String code, HumanEntity master) {
        setDirection();

        api = new API(this, plugin);
        engine = manager.getEngineByName("JavaScript");
        engine.put("mc", api);
        this.master = master;

        if (thread != null) {
            thread.stop();
        }

        thread = new Thread(new Runnable() {
            public void run() {
                try {
                    engine.eval(code);
                } catch (ScriptException e) {
                    master.sendMessage(
                            ChatColor.BOLD.toString() + ChatColor.RED + "ERROR: Program terminated! " + e.toString());
                }
            }
        });
        thread.start();
    }

    public void stop(){
        // stop any program that may be running
        if (thread != null) {
            thread.stop();
        }

        // empty hands
        body.getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
        body.getEquipment().setItemInOffHand(new ItemStack(Material.AIR));
    }

    public void saveInventory(){
        plugin.getInventoryManager().set(getID(), inventory);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Zombie getBody() {
        return body;
    }

    public void setBody(Zombie body) {
        this.body = body;
    }

    public HumanEntity getMaster() {
        return master;
    }

    public String getID(){
        return body.getUniqueId().toString();
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public Location getLastLocation() {
        return lastLocation.clone();
    }

    public void setDirection(Vector direction){
        direction.setY(0);
        if(Math.abs(direction.getX()) > Math.abs(direction.getZ())){
            direction.setZ(0);
        }
        else{
            direction.setX(0);
        }

        this.direction = direction;
        body.teleport(body.getLocation().setDirection(direction));
    }

    public void setDirection() {
        Vector direction = body.getLocation().getDirection();

        setDirection(direction);
    }

    public Vector getDirection() {
        if(direction != null)return direction.clone();
        return new Vector(1,-1,0);
    }
}
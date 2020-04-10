package org.eu.jezersek;

import java.util.Collection;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;

public class API{
    private Robot robot;
    private Plugin plugin;
    private Zombie body;
    private BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
    
    public boolean reportInfo = true;
    public boolean reportWarnings = true;
    private boolean reportErrors = true;

    API(Robot robot, Plugin plugin){
        this.robot = robot;
        this.body = robot.getBody();
        this.plugin = plugin;
    }

    // MOVMENT
    public void move(String direction){
        Vector d = new Vector(0,0,0);
        if(direction.equals("forward")){
            d.setX(1);
        }
        else if(direction.equals("backward")){
            d.setX(-1);
        }
        else if(direction.equals("left")){
            d.setY(-1);
        }
        else if(direction.equals("right")){
            d.setY(1);
        }

        Vector v = relativeVector(d);
        updateDirection();
        moveXY(v.getX(), v.getZ());
    }

    public void turn(String direction){
        Vector v = new Vector(1,0,0);
        if(direction.equals("right")){
            v = new Vector(0,0,1);
        }
        else if(direction.equals("left")){
            v = new Vector(0,0,-1);
        }
        else if(direction.equals("around")){
            v = new Vector(-1,0,0);
        }
        sleep(50);
        robot.setDirection(relativeVector(v));
        updateDirection();
    }

    public void jump(){
        updateDirection();
        scheduler.scheduleSyncDelayedTask(plugin, () -> {
            if(body.isOnGround()){
                Vector direction = new Vector(0,0.55,0);
                robot.getBody().setVelocity(direction);
            }
            else{
                warn("Can't jump if not on ground!");
            }
        }, 0L);
        sleep(300);
    }

    private void moveXY(double x, double y){
        Location destination = robot.getBody().getLocation();
        destination.add(x, 0, y);
        // align destination with block
        destination.setX(Math.floor(destination.getX()) + 0.5);
        destination.setZ(Math.floor(destination.getZ()) + 0.5);
        
        /*if(!destination.getBlock().isPassable()){ // block in robots path
            if(destination.getBlock().getRelative(BlockFace.UP).isPassable()){ // robot can jump on that block
                scheduler.scheduleSyncDelayedTask(plugin, () -> { 
                    Vector direction = new Vector(0,0.5,0);
                    robot.getBody().setVelocity(robot.getBody().getVelocity().add(direction));
                }, 0L);
            }
            else{ // if block in fornt of robot and one above it is unpassable 
                warn("I can't go through blocks");
                return;
            }
        }*/
        destination.setY(0);
        
        robot.setLastLocation(robot.getBody().getLocation());
        double d = robot.getLastLocation().distance(destination);
        //print("Destination: " + blockPosition(destination) + " lastLocation: " + blockPosition(robot.getLastLocation()) + "d: " + d);
        int timeOut = 0;
        
        while(d > 0.05){
            Location last = robot.getLastLocation().clone();
            last.setY(0);

            Vector direction = destination.clone().subtract(last).toVector().normalize();
            d = last.distance(destination);
            //print(timeOut + " : " + d + " - (" + direction.getX() + ", " + direction.getY() + ", " + direction.getZ() + ")" );
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                Vector velocity = robot.getBody().getVelocity();
                double currentVelecityInDirection = Math.abs(velocity.dot(direction));
                double targetVelovity = 0.07;
                double velocityIncrement = targetVelovity-currentVelecityInDirection;
                //print("velocity " + currentVelecityInDirection);
                if(velocityIncrement < 0)velocityIncrement = 0;
                direction.multiply(velocityIncrement);
                velocity.add(direction);
                robot.getBody().setVelocity(velocity);
            }, 0L);
            sleep(20);
            if(timeOut++ > 100)break;
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                robot.setLastLocation(body.getLocation());  
            }, 0L);
        }
        //center();
        
        //robot.getBody().setVelocity(new Vector(0,0,0));
    }

    private void center(){
        scheduler.scheduleSyncDelayedTask(plugin, () -> {
            Location center = body.getLocation();
            center.setX(Math.floor(center.getX())+0.5);
            center.setZ(Math.floor(center.getZ())+0.5);
            body.teleport(center);
        },0L);
    }
   
    private void tp(int x, int y, int z){
        Vector v = relativeVector(new Vector(x,y,z)).multiply(0.1);
        for(int i=0; i<10; i++){
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                body.teleport(body.getLocation().add(v));
            }, 0L);
            sleep(50);
        }
        info("Robot moved, to "+blockPosition(body.getLocation()));
    }
   
    private void setVelocity(float x, float y, float z){
        scheduler.scheduleSyncDelayedTask(plugin, () -> {
            robot.getBody().setVelocity(new Vector(x,y,z));
            robot.getBody().teleport(robot.getBody().getLocation().setDirection(new Vector(x,y,z)));
        }, 0L);
    }

    private Vector relativeVector(Vector v){
        //print("rv" + v.toString());
        //print("rvd" + robot.getDirection().toString());
        Vector up = new Vector(0,1,0);
        Vector right = robot.getDirection().crossProduct(up).normalize();
        Vector forward = robot.getDirection().normalize();

        return forward.multiply(v.getX()).add(up.multiply(v.getY())).add(right.multiply(v.getZ()));
    }

    // INVENTORY

    public void drop(int n, int slot){
        scheduler.scheduleSyncDelayedTask(plugin, () -> {
            ItemStack[] inventory = robot.getInventory().getContents();
            if(n < 0 || n >= inventory.length){
                warn("Nothing was droped. Inventory index is out of bounds!");
                return;
            }
            ItemStack item = inventory[n];
            if(item == null){
                warn("Nothing was droped.");
                return;
            }
            if(n < 1){
                warn("Nothing was dorped. Paremeter n must be greater than 0");
                return;
            }
            ItemStack clone = item.clone();
            int availableN = n;
            if(n > item.getAmount())availableN = item.getAmount();
            int amount = item.getAmount()-n;
            if(availableN > 0){
                clone.setAmount(availableN);
                body.getWorld().dropItem(body.getLocation(), clone);
                if(availableN == n)info("Droped "+availableN+" "+clone.getType().toString() +" from slot " + slot + ".");
                else if(n == Integer.MAX_VALUE)info("Droped "+availableN+" "+clone.getType().toString() +" from slot " + slot + ".");
                else warn("Droped only "+availableN+" "+clone.getType().toString() +" from slot " + slot + ".");
            }
            item.setAmount(amount);
            robot.saveInventory();
        }, 0L);
    }

    public void dropAll(int slot){
        drop(Integer.MAX_VALUE, slot);
    }

    public void setTool(int index){
        scheduler.scheduleSyncDelayedTask(plugin, () -> {
            ItemStack[] inventory = robot.getInventory().getContents();
            if(index < 0 || index >= inventory.length){
                warn("Inventory index is out of bounds! No tool was set.");
                return;
            }
            ItemStack item = inventory[index];
            if(item == null){
                //info("There is no item on slot " + index + ".");
                robot.getBody().getEquipment().setItemInMainHand(new ItemStack(Material.AIR));
            }
            robot.getBody().getEquipment().setItemInMainHand(item);
        }, 0L);
    }
    
    public void pickUp(String type, int index){
        scheduler.scheduleSyncDelayedTask(plugin, () -> {
            ItemStack[] inventory = robot.getInventory().getContents();
            if(index < 0 || index >= inventory.length){
                warn("Inventory index is out of bounds!");
                return;
            }

            Collection<Entity> items = body.getWorld().getNearbyEntities(body.getLocation(), 2, 2, 2, new Predicate<Entity>(){
                @Override
                public boolean test(Entity e) {
                    return e.getType().equals(EntityType.DROPPED_ITEM);
                }
            });

            Item item = null;
            double dmin = Double.POSITIVE_INFINITY;
            for(Entity i : items){
                Item ii = (Item)i;
                double distance = body.getLocation().distanceSquared(i.getLocation());
                if(distance < dmin){
                    if(ii.getItemStack().getType().toString().equals(type) || type.equals("*"))
                    dmin = distance;
                    item = (Item) i;
                }
            }

            if(item != null){  
                if(inventory[index] == null || inventory[index].getType().equals(Material.AIR)){ // if inventory slot is free
                    robot.getInventory().setItem(index, item.getItemStack());
                    info("Picked up "+item.getItemStack().getType().toString() + " and stored in slot " + index + ".");
                    item.remove();
                }
                else if(inventory[index].isSimilar(item.getItemStack())){ // if item type is the same
                    int spaceLeft = inventory[index].getMaxStackSize() - inventory[index].getAmount();
                    int itemsToPick = item.getItemStack().getAmount();
                    if(itemsToPick > spaceLeft)itemsToPick = spaceLeft;
                    inventory[index].setAmount(inventory[index].getAmount() + itemsToPick);
                    info("Picked up "+item.getItemStack().getType().toString() + " and stored in slot " + index + ".");
                    item.getItemStack().setAmount(item.getItemStack().getAmount() - itemsToPick);
                }
                else{
                    warn("Not enough space in inventory.");
                }
                robot.saveInventory();
            }
            else{
                warn("No items found.");
            }
        }, 0L);
    }

    public void pickUpAny(int index){
        pickUp("*", index);
    }

    public void swapInventory(int a, int b){
        scheduler.scheduleSyncDelayedTask(plugin, () -> {
            ItemStack[] inventory = robot.getInventory().getContents();
            if(a < 0 || a >= inventory.length){
                warn("First index ("+a+") is out of bounds!");
                return;
            }
            else if(b < 0 || b >= inventory.length){
                warn("Second index ("+b+") is out of bounds!");
                return;
            }
            ItemStack itemA = inventory[b];
            ItemStack itemB = inventory[a];
            robot.getInventory().setItem(a, itemA);
            robot.getInventory().setItem(b, itemB);

            robot.saveInventory();
        }, 0L);
    }

    public int inventoryIndex(String type){
        ItemStack[] inventory = robot.getInventory().getContents();
        for(int i = 0; i<inventory.length; i++){
            ItemStack item = inventory[i];
            if(item.getType().toString().equals(type)){
                return i;
            }
        }
        warn("No slot with such item found.");
        return -1;
    }

    public int firstFreeSlot(){
        ItemStack[] inventory = robot.getInventory().getContents();
        for(int i = 0; i<inventory.length; i++){
            ItemStack item = inventory[i];
            if(item == null || item.getType().equals(Material.AIR)){
                return i;
            }
        }
        warn("No empty slots found.");
        return -1;
    }


    // WORKING
    public void breakBlock(String direction){
        Vector v = direction2vect(direction);
        breakBlock(v.getX(), v.getY(), v.getZ());
    }

    private void breakBlock(double x, double y, double z){
        Vector v = relativeVector(new Vector(x,y,z));
        if(x > 1 || y > 1 || z > 1){
            warn("Can't break that block. I can only brak blocks near me.");
            return;
        }
        sleep(500);
        
        scheduler.scheduleSyncDelayedTask(plugin, () -> {
            Block block = robot.getBody().getLocation().add(v).getBlock();
            String type = block.getType().toString();
            if(block.breakNaturally()){
                info("Broken "+type+" at "+blockPosition(block));
            }
            else{
                info("Nothing was borken at "+blockPosition(block));
            }
        }, 0L);
    }

    public void place(int index, String direction, String blockDirection){
        Vector v = relativeVector(direction2vect(direction));

        scheduler.scheduleSyncDelayedTask(plugin, () -> {
            ItemStack[] inventory = robot.getInventory().getContents();
            if(index < 0 || index >= inventory.length){
                warn("Inventory slot ("+index+") is out of bounds!");
                return;
            }
            Block block = robot.getBody().getLocation().add(v).getBlock();
            if(block.getType().equals(Material.AIR)){
                if(inventory[index].getType().isBlock()){
                    block.setType(inventory[index].getType());
                    inventory[index].setAmount(inventory[index].getAmount()-1);

                    if(block.getBlockData() instanceof Directional){
                        Directional directionData = (Directional) block.getBlockData();
                        directionData.setFacing(BlockFace.valueOf(blockDirection));
                        block.setBlockData(directionData);
                        print("directional " + directionData.getFacing());
                    }

                    info("Block ("+inventory[index].getType().toString()+") was placed at " + blockPosition(block) + ".");
                }
                else{
                    warn("No blocks placed. " + inventory[index].getType().toString() + " is not a placable block.");
                }
            }
            else{
                warn("No blocks placed. No space at " + blockPosition(block) + ".");
            }
        }, 0L);
    }

    public String getBlockType(String direction){
        Vector v = relativeVector(direction2vect(direction));
        return body.getLocation().add(v).getBlock().getType().toString();
    }

    public String getBlockTypeAt(double x, double y, double z){
        Vector v = relativeVector(new Vector(x, y, z));
        return body.getLocation().add(v).getBlock().getType().toString();
    }

    public int getCoordinate(String c){
        Block l = body.getLocation().getBlock();
        if(c.equals("x")){
            return l.getX();
        }
        if(c.equals("y")){
            return l.getY();
        }
        if(c.equals("z")){
            return l.getZ();
        }
        error("Coordinate label \""+c+"\" is invalid.");
        return 0;
    }

    public String getDirection(){
        Vector d = robot.getLastLocation().getDirection();
        if(Math.abs(d.getX()) > Math.abs(d.getZ())){
            if(d.getX() > 0)return "EAST";
            else return "WEST";
        } 
        else{
            if(d.getZ() > 0)return "SOUTH";
            else return "NORTH";
        }
        
    }


    // COMUNICATION

    public void announce(String s) {
        Bukkit.getServer().broadcastMessage(s);
    }

    public void print(String s){
        robot.getMaster().sendMessage(s);
    }

    public void error(String s){
        if(reportErrors){
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                robot.getMaster().sendMessage(ChatColor.RED + "ERROR: " + s);
            }, 0L);
        }
    }

    public void warn(String s){
        if(reportWarnings){
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                robot.getMaster().sendMessage(ChatColor.YELLOW + "WARNING: " + s);
            }, 0L);
        }
    }

    public void info(String s){
        if(reportInfo){
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                robot.getMaster().sendMessage(ChatColor.BLUE + "INFO: " + s);
            }, 0L);
        }
    }


    // OTHER

    public void sleep(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }   

    public Vector direction2vect(String direction){
        Vector v = new Vector(0,0,0);
        if(direction.equals("below")){
            v.setY(-1);
        }
        else if(direction.equals("above")){
            v.setY(1);
        }
        else if(direction.equals("front")){
            v.setX(1);
        }
        else if(direction.equals("behind")){
            v.setX(-1);
        }
        else if(direction.equals("left")){
            v.setZ(-1);
        }
        else if(direction.equals("right")){
            v.setZ(1);
        }
        return v;
    }



    

    



    private String blockPosition(Location l){
        return blockPosition(l.getBlock());
    }

    private String blockPosition(Block block){
        return "["+block.getX()+","+block.getY()+","+block.getZ()+"]";
    }

    private void setAI(boolean v){
        scheduler.scheduleSyncDelayedTask(plugin, () -> {
            body.setAI(v);
        }, 0L);
    }

    public void updateDirection(){
        scheduler.scheduleSyncDelayedTask(plugin, () -> {
            body.teleport(body.getLocation().setDirection(robot.getDirection()));
        }, 0L);
    }

}
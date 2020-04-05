package org.eu.jezersek;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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

    public void announce(String s) {
        Bukkit.getServer().broadcastMessage(s);
    }

    public void print(String s){
        robot.getMaster().sendMessage(s);
    }

    public void sleep(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void move(double x, double y){
        Location destination = robot.getBody().getLocation();
        destination.add(x, 0, y);
        // align destination with block
        destination.setX(Math.floor(destination.getX()) + 0.5);
        destination.setZ(Math.floor(destination.getZ()) + 0.5);
        
        if(!destination.getBlock().isPassable()){ // block in robots path
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
        }

        destination.setY(0);
        robot.setLastLocation(robot.getBody().getLocation());
        double d = robot.getLastLocation().distance(destination);
        //print("Destination: " + blockPosition(destination) + " lastLocation: " + blockPosition(robot.getLastLocation()) + "d: " + d);
        int timeOut = 0;
        
        while(d > 0.1){
            Location last = robot.getLastLocation().clone();
            last.setY(0);

            Vector direction = destination.clone().subtract(last).toVector().normalize().multiply(0.01);
            d = last.distance(destination);
            //print(timeOut + " : " + d + " - (" + direction.getX() + ", " + direction.getY() + ", " + direction.getZ() + ")" );
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                robot.getBody().setVelocity(robot.getBody().getVelocity().add(direction));
            }, 0L);
            if(timeOut++ > 100)break;
            sleep(20);
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                robot.setLastLocation(body.getLocation());  
            }, 0L);
        }
        center();
        
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

    public void forward(){
        updateDirection();
        Vector v = relativeVector(new Vector(1,0,0));
        print(v.getX() + ", " + v.getY());
        move(v.getX(), v.getZ());
    }

    public void jump(){
        updateDirection();
        scheduler.scheduleSyncDelayedTask(plugin, () -> {
            if(body.isOnGround()){
                Vector direction = new Vector(0,0.5,0);
                robot.getBody().setVelocity(robot.getBody().getVelocity().add(direction));
            }
            else{
                warn("Can't jump if not on ground!");
            }
        }, 0L);
        sleep(50);
    }


    private void tp(int x, int y, int z){
        //setAI(true);
        Vector v = relativeVector(new Vector(x,y,z)).multiply(0.1);
        for(int i=0; i<10; i++){
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                body.teleport(body.getLocation().add(v));
            }, 0L);
            sleep(50);
        }
        info("Robot moved, to "+blockPosition(body.getLocation()));
        //setAI(false);
    }

    public void turnRight(){
        sleep(50);
        robot.setDirection(relativeVector(new Vector(0,0,1)));
        updateDirection();
    }
    
    public void turnLeft(){
        sleep(50);
        robot.setDirection(relativeVector(new Vector(0,0,-1)));
        updateDirection();
    }   
    public void breakBlock(int x, int y, int z){
        Vector v = relativeVector(new Vector(x,y,z));
        if(x > 1 || y > 1 || z > 1){
            warn("Can't break that block. I can only brak blocks near me.");
            return;
        }
        sleep(500);
        
        scheduler.scheduleSyncDelayedTask(plugin, () -> {
            Block block = robot.getBody().getLocation().add(v).getBlock();
            if(block.breakNaturally()){
                info("Broken "+block.getType().toString()+" at "+blockPosition(block));
            }
            else{
                info("Nothing was borken at "+blockPosition(block));
            }
        }, 0L);
    }

    private void setVelocity(float x, float y, float z){
        scheduler.scheduleSyncDelayedTask(plugin, () -> {
            robot.getBody().setVelocity(new Vector(x,y,z));
            robot.getBody().teleport(robot.getBody().getLocation().setDirection(new Vector(x,y,z)));
        }, 0L);
    }

    public void setTool(int index){
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

    private Vector relativeVector(Vector v){
        print("rv" + v.toString());
        print("rvd" + robot.getDirection().toString());
        Vector up = new Vector(0,1,0);
        Vector right = robot.getDirection().crossProduct(up).normalize();
        Vector forward = robot.getDirection().normalize();

        return forward.multiply(v.getX()).add(up.multiply(v.getY())).add(right.multiply(v.getZ()));
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
            print(robot.getDirection().toString());
        }, 0L);
    }

}
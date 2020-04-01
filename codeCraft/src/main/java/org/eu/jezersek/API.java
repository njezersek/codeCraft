package org.eu.jezersek;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
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

    public void move(int x, int y){
        Location destination = robot.getBody().getLocation();
        destination = destination.add(x, 0, y);
        robot.setLastLocation(robot.getBody().getLocation());
        int timeOut = 0;
        Vector direction = destination.subtract(robot.getBody().getLocation()).toVector().normalize().multiply(0.1);
        while(robot.getLastLocation().distanceSquared(destination) > 0.1*0.1){
            //robot.getBody().teleport(robot.getBody().getLocation().setDirection(direction));
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                robot.getBody().setVelocity(direction);
            }, 0L);
            if(timeOut++ > 100)break;
            sleep(200);
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                robot.setLastLocation(body.getLocation());  
            }, 0L);
            print(timeOut + " : " + robot.getLastLocation().distanceSquared(destination));
        }
        robot.getBody().setVelocity(new Vector(0,0,0));
    }

    /*public void move(int x, int y){
        Location destination = robot.getBody().getLocation();
        destination = destination.add(x, 0, y);
        robot.setLastLocation(robot.getBody().getLocation());
        int timeOut = 0;
        Vector direction = destination.subtract(robot.getBody().getLocation()).toVector().normalize().multiply(0.1);
        while(robot.getLastLocation().distanceSquared(destination) > 0.1*0.1){
            //robot.getBody().teleport(robot.getBody().getLocation().setDirection(direction));
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                robot.getBody().setVelocity(direction);
            }, 0L);
            if(timeOut++ > 100)break;
            sleep(200);
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                robot.setLastLocation(body.getLocation());  
            }, 0L);
            print(timeOut + " : " + robot.getLastLocation().distanceSquared(destination));
        }
        robot.getBody().setVelocity(new Vector(0,0,0));
    }*/

    public void tp(int x, int y, int z){
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
        setDirection(relativeVector(new Vector(0,0,1)));
    }
    
    public void turnLeft(){
        sleep(50);
        setDirection(relativeVector(new Vector(0,0,-1)));
    }

    private void setDirection(Vector direction){
        scheduler.scheduleSyncDelayedTask(plugin, () -> {
            body.teleport(body.getLocation().setDirection(direction));
        }, 0L);
    }
    
    public void breakBlock(int x, int y, int z){
        Vector v = relativeVector(new Vector(x,y,z));
        if(x > 1 || y > 1 || z > 1){
            warn("Can't break that block. I can only brak blocks near me.");
            return;
        }
        
        scheduler.scheduleSyncDelayedTask(plugin, () -> {
            Block block = robot.getBody().getLocation().add(v).getBlock();
            if(block.breakNaturally()){
                info("Broken "+block.getType().toString()+" at "+blockPosition(block));
            }
            else{
                info("Nothing was borken at "+blockPosition(block));
            }
        }, 0L);
        sleep(500);
    }

    public void setVelocity(float x, float y, float z){
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
        Vector up = new Vector(0,1,0);
        Vector right = robot.getBody().getLocation().getDirection().crossProduct(up).normalize();
        Vector forward = robot.getBody().getLocation().getDirection().normalize();

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

}
package org.eu.jezersek;

import java.util.Map.Entry;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Zombie;

public class TickHandler implements Runnable {
    App plugin;
    TickHandler(App plugin){
        this.plugin = plugin;
    }

    @Override
    public void run() {
        // remove all existing chunk tickets
        for(World w : plugin.getServer().getWorlds()){
            w.removePluginChunkTickets(plugin);
        }
        // add tickets for all chunks with robots in
        for(Entry<String, Robot> r : plugin.getRobots().entrySet()){
            Zombie body = r.getValue().getBody();
            Chunk c = body.getWorld().getChunkAt(body.getLocation());
            for(int i=-1; i<=1; i++){
                for(int j=-1; j<=1; j++){
                    Chunk cc = body.getWorld().getChunkAt(c.getX()+i, c.getZ()+j);
                    cc.addPluginChunkTicket(plugin);
                }
            }
        }
    }

}

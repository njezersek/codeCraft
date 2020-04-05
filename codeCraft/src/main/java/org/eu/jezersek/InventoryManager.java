package org.eu.jezersek;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

class InventoryManager{
    private Database db;

    HashMap<String, Inventory> inventories;

    InventoryManager(App plugin){
        inventories = new HashMap<>();
        this.db = plugin.getDb();
    }

    public Inventory get(String id){
        return get(id, "Inventory");
    }

    public Inventory get(String id, String title){
        return get(id, title, 3);
    }

    public Inventory get(String id, String title, int size){
        Inventory inventory;
        inventory = inventories.getOrDefault(id, null); // look in the local storage
        if(inventory == null)inventory = db.getInventory(id, title); // look in the db
        if(inventory == null)inventory = Bukkit.createInventory(new TmpInventoryHolder(id), 9*size, title); // create new one
        return inventory;
    }

    public void set(String id, Inventory inventory){
        inventories.put(id, inventory);
        db.setInvetory(id, inventory);
    }
}
package org.eu.jezersek;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

class TmpInventoryHolder implements InventoryHolder {
    String id;

    TmpInventoryHolder(String id){
        this.id = id;
    }

    public String getEntityId() {
        return id;
    }

    public void setEntityId(String id) {
        this.id = id;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
    
}
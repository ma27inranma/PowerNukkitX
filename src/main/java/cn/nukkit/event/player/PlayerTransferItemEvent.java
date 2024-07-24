package cn.nukkit.event.player;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;

public class PlayerTransferItemEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    
    public static HandlerList getHandlers(){
        return handlers;
    }

    private final Inventory sourceInv;
    private final Inventory targetInv;
    private final Item targetItem;
    private final Item sourceItem;
    private final int sourceSlot;
    private final int targetSlot;


    public PlayerTransferItemEvent(Player who, Inventory sourceInventroy, Inventory targetInventory, Item targetItem, Item sourceItem, int sourceSlot, int targetSlot) {
        this.player = who;
        this.sourceInv = sourceInventroy;
        this.targetInv = targetInventory;
        this.targetItem = targetItem;
        this.sourceItem = sourceItem;
        this.targetSlot = targetSlot;
        this.sourceSlot = sourceSlot;
    }

    public Item getTargetItem(){
        return this.targetItem;
    }

    public Item getSourceItem(){
        return this.sourceItem;
    }

    public int getTargetSlot(){
        return this.targetSlot;
    }

    public int getSourceSlot(){
        return this.sourceSlot;
    }

    public Inventory getSourceInventory(){
        return this.sourceInv;
    }

    public Inventory getTargetInventory(){
        return this.targetInv;
    }
}

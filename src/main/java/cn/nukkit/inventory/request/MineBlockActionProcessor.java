package cn.nukkit.inventory.request;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.inventory.HumanInventory;
import cn.nukkit.inventory.SpecialWindowId;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.InventorySlotPacket;
import cn.nukkit.network.protocol.types.inventory.FullContainerName;
import cn.nukkit.network.protocol.types.itemstack.ContainerSlotType;
import cn.nukkit.network.protocol.types.itemstack.request.action.ItemStackRequestActionType;
import cn.nukkit.network.protocol.types.itemstack.request.action.MineBlockAction;
import cn.nukkit.network.protocol.types.itemstack.response.ItemStackResponseContainer;
import cn.nukkit.network.protocol.types.itemstack.response.ItemStackResponseSlot;
import eu.okaeri.configs.schema.GenericsDeclaration;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

@Slf4j
public class MineBlockActionProcessor implements ItemStackRequestActionProcessor<MineBlockAction> {
    public static Boolean allowClientDurabilityPrediction = null;


    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.MINE_BLOCK;
    }

    @Nullable
    @Override
    public ActionResponse handle(MineBlockAction action, Player player, ItemStackRequestContext context) {
        HumanInventory inventory = player.getInventory();
        int heldItemIndex = inventory.getHeldItemIndex();
        if (heldItemIndex != action.getHotbarSlot()) {
            log.warn("The held Item Index on the server side does not match the client side!");
            return context.error();
        }

        Item itemInHand = inventory.getItemInHand();
        if (validateStackNetworkId(itemInHand.getNetId(), action.getStackNetworkId())) {
            log.warn("mismatch source stack network id!");
            return context.error();
        }

        if(allowClientDurabilityPrediction == null){
            // allowClientDurabilityPrediction = Server.getInstance().getProperties().getBoolean("allow_client_item_durability_prediction", true);
            // allowClientDurabilityPrediction = Server.getInstance().getProperties().get(ServerPropertiesKeys.ALLOW_CLIENT_ITEM_DURABILITY_PREDICTION, true);
            allowClientDurabilityPrediction = Optional.ofNullable(Server.getInstance().getSettings().gameplaySettings().get("allowClientItemDurabilityPrediction", Boolean.class)).orElse(false);
            Server.getInstance().getSettings().gameplaySettings().set("allowClientItemDurabilityPrediction", allowClientDurabilityPrediction);
            // allowClientDurabilityPrediction = true;
        }
        
        if(allowClientDurabilityPrediction){
            itemInHand.setDamage(action.getPredictedDurability());
        }else if (itemInHand.getDamage() != action.getPredictedDurability()) {
            InventorySlotPacket inventorySlotPacket = new InventorySlotPacket();
            int id = SpecialWindowId.PLAYER.getId();
            inventorySlotPacket.inventoryId = id;
            inventorySlotPacket.item = itemInHand;
            inventorySlotPacket.slot = action.getHotbarSlot();
            inventorySlotPacket.fullContainerName = new FullContainerName(
                    ContainerSlotType.HOTBAR,
                    id
            );
            player.dataPacket(inventorySlotPacket);
        }
        
        var itemStackResponseSlot =
                new ItemStackResponseContainer(
                        inventory.getSlotType(heldItemIndex),
                        Lists.newArrayList(
                                new ItemStackResponseSlot(
                                        inventory.toNetworkSlot(heldItemIndex),
                                        inventory.toNetworkSlot(heldItemIndex),
                                        itemInHand.getCount(),
                                        itemInHand.getNetId(),
                                        itemInHand.getCustomName(),
                                        itemInHand.getDamage()
                                )
                        ),
                        new FullContainerName(
                                inventory.getSlotType(heldItemIndex),
                                0   // I don't know the purpose of the dynamicId yet, this is why I leave it at 0 for the MineBlockAction
                        )
                );
        return context.success(List.of(itemStackResponseSlot));
    }
}

package cn.nukkit.inventory.request;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.config.ServerPropertiesKeys;
import cn.nukkit.inventory.HumanInventory;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.types.inventory.FullContainerName;
import cn.nukkit.network.protocol.types.itemstack.request.action.ItemStackRequestActionType;
import cn.nukkit.network.protocol.types.itemstack.request.action.MineBlockAction;
import cn.nukkit.network.protocol.types.itemstack.response.ItemStackResponseContainer;
import cn.nukkit.network.protocol.types.itemstack.response.ItemStackResponseSlot;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
            allowClientDurabilityPrediction = Server.getInstance().getProperties().get(ServerPropertiesKeys.ALLOW_CLIENT_ITEM_DURABILITY_PREDICTION, true);
        }
        
        if(allowClientDurabilityPrediction){
            itemInHand.setDamage(action.getPredictedDurability());
        }else if (itemInHand.getDamage() != action.getPredictedDurability()) {
            log.warn("Durability predicted by the client does not match that of the server client {} server {} player {} allowClientDurabilityPrediction {}", action.getPredictedDurability(), itemInHand.getDamage(), player.getName(), allowClientDurabilityPrediction);
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

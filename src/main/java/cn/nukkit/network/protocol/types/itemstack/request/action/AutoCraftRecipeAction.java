package cn.nukkit.network.protocol.types.itemstack.request.action;

import cn.nukkit.recipe.descriptor.ItemDescriptor;
import lombok.Value;

import java.util.List;

/**
 * AutoCraftRecipeStackRequestActionData is sent by the client similarly to the CraftRecipeStackRequestActionData. The
 * only difference is that the recipe is automatically created and crafted by shift clicking the recipe book.
 */
@Value
public class AutoCraftRecipeAction implements RecipeItemStackRequestAction {

    int recipeNetworkId;
    int numberOfRequestedCrafts;
    int timesCrafted;
    List<ItemDescriptor> ingredients;

    @Override
    public ItemStackRequestActionType getType() {
        return ItemStackRequestActionType.CRAFT_RECIPE_AUTO;
    }
}

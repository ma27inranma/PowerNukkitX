package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.property.enums.WoodType;
import cn.nukkit.event.block.LeavesDecayEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.ItemTool;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.utils.Hash;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static cn.nukkit.block.property.CommonBlockProperties.PERSISTENT_BIT;
import static cn.nukkit.block.property.CommonBlockProperties.UPDATE_BIT;

/**
 * @author Angelic47 (Nukkit Project)
 */
public abstract class BlockLeaves extends BlockTransparent {
    private static final BlockFace[] VISIT_ORDER = new BlockFace[]{
            BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.DOWN, BlockFace.UP
    };

    public BlockLeaves(BlockState blockState) {
        super(blockState);
    }

    @Override
    public double getHardness() {
        return 0.2;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_HOE;
    }

    public abstract WoodType getType();

    @Override
    public String getName() {
        return getType().getName() + " Leaves";
    }

    @Override
    public int getBurnChance() {
        return 30;
    }

    @Override
    public int getWaterloggingLevel() {
        return 1;
    }

    @Override
    public int getBurnAbility() {
        return 60;
    }

    @Override
    public boolean place(@NotNull Item item, @NotNull Block block, @NotNull Block target, @NotNull BlockFace face, double fx, double fy, double fz, @Nullable Player player) {
        this.setPersistent(true);
        this.getLevel().setBlock(this, this, true);
        return true;
    }

    @Override
    public Item[] getDrops(Item item) {
        if (item.isShears()) {
            return new Item[]{
                    toItem()
            };
        }

        List<Item> drops = new ArrayList<>(1);
        Enchantment fortuneEnchantment = item.getEnchantment(Enchantment.ID_FORTUNE_DIGGING);

        int fortune = fortuneEnchantment != null ? fortuneEnchantment.getLevel() : 0;
        int appleOdds;
        int stickOdds;
        int saplingOdds;
        switch (fortune) {
            case 0 -> {
                appleOdds = 200;
                stickOdds = 50;
                saplingOdds = getType() == WoodType.JUNGLE ? 40 : 20;
            }
            case 1 -> {
                appleOdds = 180;
                stickOdds = 45;
                saplingOdds = getType() == WoodType.JUNGLE ? 36 : 16;
            }
            case 2 -> {
                appleOdds = 160;
                stickOdds = 40;
                saplingOdds = getType() == WoodType.JUNGLE ? 32 : 12;
            }
            default -> {
                appleOdds = 120;
                stickOdds = 30;
                saplingOdds = getType() == WoodType.JUNGLE ? 24 : 10;
            }
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (canDropApple() && random.nextInt(appleOdds) == 0) {
            drops.add(Item.get(ItemID.APPLE));
        }
        if (random.nextInt(stickOdds) == 0) {
            drops.add(Item.get(ItemID.STICK));
        }
        if (random.nextInt(saplingOdds) == 0) {
            drops.add(toSapling());
        }

        return drops.toArray(Item.EMPTY_ARRAY);
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_RANDOM) {
            if (isCheckDecay()) {
                if (isPersistent() || findLog(this, 7, null)) {
                    setCheckDecay(false);
                    getLevel().setBlock(this, this, false, false);
                } else {
                    LeavesDecayEvent ev = new LeavesDecayEvent(this);
                    Server.getInstance().getPluginManager().callEvent(ev);
                    if (!ev.isCancelled()) {
                        getLevel().useBreakOn(this);
                    }
                }
                return type;
            }
        } else if (type == Level.BLOCK_UPDATE_NORMAL || type == Level.BLOCK_UPDATE_SCHEDULED) {
            if (!isCheckDecay()) {
                setCheckDecay(true);
                getLevel().setBlock(this, this, false, false);
            }

            // Slowly propagates the need to update instead of peaking down the TPS for huge trees
            for (BlockFace side : BlockFace.values()) {
                Block other = getSide(side);
                if (other instanceof BlockLeaves otherLeave) {
                    if (!otherLeave.isCheckDecay()) {
                        getLevel().scheduleUpdate(otherLeave, 2);
                    }
                }
            }
            return type;
        }
        return type;
    }

    private Boolean findLog(Block current, int distance, Long2LongMap visited) {
        if (visited == null) {
            visited = new Long2LongOpenHashMap();
            visited.defaultReturnValue(-1);
        }
        if (current instanceof IBlockWood || current instanceof BlockMangroveRoots) {
            return true;
        }
        if (distance == 0 || !(current instanceof BlockLeaves)) {
            return false;
        }
        long hash = Hash.hashBlock(current);
        if (visited.get(hash) >= distance) {
            return false;
        }
        visited.put(hash, distance);
        for (BlockFace face : VISIT_ORDER) {
            if (findLog(current.getSide(face), distance - 1, visited)) {
                return true;
            }
        }
        return false;
    }

    public boolean isCheckDecay() {
        return getPropertyValue(UPDATE_BIT);
    }

    public void setCheckDecay(boolean checkDecay) {
        setPropertyValue(UPDATE_BIT, checkDecay);
    }

    public boolean isPersistent() {
        return getPropertyValue(PERSISTENT_BIT);
    }

    public void setPersistent(boolean persistent) {
        setPropertyValue(PERSISTENT_BIT, persistent);
    }

    @Override
    public boolean canSilkTouch() {
        return true;
    }

    protected boolean canDropApple() {
        return getType() == WoodType.OAK;
    }

    @Override
    public boolean diffusesSkyLight() {
        return true;
    }

    @Override
    public boolean breaksWhenMoved() {
        return true;
    }

    @Override
    public boolean sticksToPiston() {
        return false;
    }

    public Item toSapling() {
        return Item.AIR;
    }
}

package cn.nukkit.blockentity;

import cn.nukkit.Player;
import cn.nukkit.block.BlockAir;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.BlockEntityDataPacket;
import cn.nukkit.network.protocol.UpdateBlockPacket;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public abstract class BlockEntitySpawnable extends BlockEntity {

    public BlockEntitySpawnable(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initBlockEntity() {
        super.initBlockEntity();
        this.spawnToAll();
    }

    public CompoundTag getSpawnCompound() {
        return new CompoundTag()
                .putString("id", namedTag.getString("id"))
                .putInt("x", getFloorX())
                .putInt("y", getFloorY())
                .putInt("z", getFloorZ());
    }

    public void spawnTo(Player player) {
        if (this.closed) {
            return;
        }

        UpdateBlockPacket refreshPacket = new UpdateBlockPacket();
        refreshPacket.blockRuntimeId = BlockAir.STATE.blockStateHash();
        refreshPacket.flags = UpdateBlockPacket.FLAG_NETWORK;
        refreshPacket.x = this.getFloorX();
        refreshPacket.y = this.getFloorY();
        refreshPacket.z = this.getFloorZ();

        UpdateBlockPacket setBlockPacket = new UpdateBlockPacket();
        setBlockPacket.blockRuntimeId = this.getBlock().getRuntimeId();
        setBlockPacket.flags = UpdateBlockPacket.FLAG_NETWORK;
        setBlockPacket.x = this.getFloorX();
        setBlockPacket.y = this.getFloorY();
        setBlockPacket.z = this.getFloorZ();

        player.dataPacket(refreshPacket);
        player.dataPacket(setBlockPacket);
        player.dataPacket(getSpawnPacket());
    }

    public BlockEntityDataPacket getSpawnPacket() {
        return getSpawnPacket(null);
    }

    public BlockEntityDataPacket getSpawnPacket(CompoundTag nbt) {
        if (nbt == null) {
            nbt = this.getSpawnCompound();
        }

        BlockEntityDataPacket pk = new BlockEntityDataPacket();
        pk.x = this.getFloorX();
        pk.y = this.getFloorY();
        pk.z = this.getFloorZ();
        pk.namedTag = nbt;

        return pk;
    }

    public void spawnToAll() {
        if (this.closed) {
            return;
        }

        for (Player player : this.getLevel().getChunkPlayers(this.chunk.getX(), this.chunk.getZ()).values()) {
            if (player.spawned) {
                this.spawnTo(player);
            }
        }
    }

    /**
     * Called when a player updates a block entity's NBT data
     * for example when writing on a sign.
     *
     * @param nbt    tag
     * @param player player
     * @return bool indication of success, will respawn the tile to the player if false.
     */
    public boolean updateCompoundTag(CompoundTag nbt, Player player) {
        return false;
    }
}

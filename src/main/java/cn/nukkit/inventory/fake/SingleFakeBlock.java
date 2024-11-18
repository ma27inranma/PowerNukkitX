package cn.nukkit.inventory.fake;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.BlockEntityDataPacket;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

import java.util.HashMap;
import java.util.HashSet;


public class SingleFakeBlock implements FakeBlock {
    protected final Block block;
    protected final String tileId;
    protected Object2ObjectArrayMap<Player, HashSet<Vector3>> lastPositions = new Object2ObjectArrayMap<>();

    public SingleFakeBlock(String blockId) {
        this.block = Block.get(blockId);
        this.tileId = "default";
    }

    public SingleFakeBlock(String blockId, String tileId) {
        this.block = Block.get(blockId);
        this.tileId = tileId;
    }

    public SingleFakeBlock(Block block, String tileId) {
        this.block = block;
        this.tileId = tileId;
    }

    @Override
    public void create(Player player) {
        create(player, "default");
    }

    @Override
    public void create(Player player, String titleName) {
        createAndGetLastPositions(player).addAll(this.getPlacePositions(player));
        lastPositions.get(player).forEach(position -> {
            UpdateBlockPacket updateBlockPacket = new UpdateBlockPacket();
            updateBlockPacket.blockRuntimeId = block.getRuntimeId();
            updateBlockPacket.flags = UpdateBlockPacket.FLAG_NETWORK;
            updateBlockPacket.x = position.getFloorX();
            updateBlockPacket.y = position.getFloorY();
            updateBlockPacket.z = position.getFloorZ();
            player.dataPacket(updateBlockPacket);

            BlockEntityDataPacket blockEntityDataPacket = new BlockEntityDataPacket();
            blockEntityDataPacket.x = position.getFloorX();
            blockEntityDataPacket.y = position.getFloorY();
            blockEntityDataPacket.z = position.getFloorZ();
            blockEntityDataPacket.namedTag = this.getBlockEntityDataAt(position, titleName);

            player.dataPacket(blockEntityDataPacket);
        });
    }

    @Override
    public void remove(Player player) {
        getLastPositions(player).forEach(position -> {
            UpdateBlockPacket packet = new UpdateBlockPacket();
            packet.blockRuntimeId = player.getLevel().getBlock(position).getRuntimeId();
            packet.flags = UpdateBlockPacket.FLAG_NETWORK;
            packet.x = position.getFloorX();
            packet.y = position.getFloorY();
            packet.z = position.getFloorZ();
            player.dataPacket(packet);
        });
        lastPositions.remove(player);
    }

    protected CompoundTag getBlockEntityDataAt(Vector3 position, String title) {
        return BlockEntity.getDefaultCompound(position, title)
                .putBoolean("isMovable", true)
                .putString("CustomName", title);
    }

    public HashSet<Vector3> createAndGetLastPositions(Player player) {
        if(!lastPositions.containsKey(player)) lastPositions.put(player, new HashSet<>());
        return lastPositions.get(player);
    }

    public HashSet<Vector3> getLastPositions(Player player) {
        return lastPositions.getOrDefault(player, new HashSet<>());
    }
}

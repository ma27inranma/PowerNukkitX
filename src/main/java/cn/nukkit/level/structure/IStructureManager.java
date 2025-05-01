package cn.nukkit.level.structure;

import java.io.IOException;
import java.nio.ByteOrder;

import cn.nukkit.level.Position;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;

public interface IStructureManager {
    public boolean place(CompoundTag structure, Position pos, boolean force);
    public default boolean place(byte[] structure, Position pos, boolean force) throws IOException {
        return place(NBTIO.read(structure, ByteOrder.LITTLE_ENDIAN), pos, force);
    }
}

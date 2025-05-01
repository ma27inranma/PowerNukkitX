package cn.nukkit.level.structure;

import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.IntTag;
import cn.nukkit.nbt.tag.ListTag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BuiltinStructureManager implements IStructureManager {
    private Level level;

    public BuiltinStructureManager(Level level){
        this.level = level;
    }

    @Override
    public boolean place(CompoundTag structure, Position pos, boolean force) {
        Position placingAt = pos.clone();
        if(placingAt.getLevel() == null){
            placingAt.setLevel(this.level);
        }

        if(!placingAt.level.equals(this.level)){
            throw new IllegalArgumentException("Level mismatch. Expected: " + this.level.getName() + ". Got: " + placingAt.getLevel().getName());
        }

        log.info("Structure: " + structure.toSNBT(2));

        if(structure.getInt("format_version") == 1){
            int sizeX = structure.getList("size", IntTag.class).get(0).getData();
            int sizeY = structure.getList("size", IntTag.class).get(1).getData();
            int sizeZ = structure.getList("size", IntTag.class).get(2).getData();

            CompoundTag data = structure.getCompound("structure");
            CompoundTag defaultPalette = data.getCompound("palette").getCompound("default");
            ListTag<CompoundTag> blockPalette = defaultPalette.getList("block_palette", CompoundTag.class);

            ListTag<IntTag> blockIndices = data.getList("block_indices", IntTag.class);

            int i = 0;
            for(int x = 0; x < sizeX; x++){
                for(int y = 0; y < sizeY; y++){
                    for(int z = 0; z < sizeZ; z++){
                        int blockIndex =  blockIndices.get(i++).getData();
                        CompoundTag thisPalette = blockPalette.get(blockIndex);
                    }
                }
            }
        }

        return true;
    }
}

package cn.nukkit.level.structure;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockStateImpl;
import cn.nukkit.block.property.CommonBlockProperties;
import cn.nukkit.block.property.type.BaseBlockPropertyType;
import cn.nukkit.block.property.type.BlockPropertyType;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntitySpawnable;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.IntTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.NumberTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.registry.Registries;

public class Structure implements IStructure {
    private CompoundTag structure;
    private byte sizeX, sizeY, sizeZ;

    // private Block[][][] blocks = new Block[64][64][64];
    // private Block[][][] secondaryBlocks = new Block[64][64][64];
    private HashMap<Integer, Block> blocks = new HashMap<>();
    private HashMap<Integer, Block> secondaryBlocks = new HashMap<>();
    private HashMap<Integer, CompoundTag> blockEntityNbts = new HashMap<>();

    public Structure(CompoundTag structure){
        this.structure = structure;

        this.parse();
    }
    
    public Structure(byte[] bytes){
        try {
            this.structure = NBTIO.read(bytes, ByteOrder.LITTLE_ENDIAN);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.parse();
    }
    
    private void parse(){
        try {
            Files.writeString(Path.of("structure.snbt"), this.structure.toSNBT(2));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(structure.getInt("format_version") == 1){
            this.sizeX = structure.getList("size", IntTag.class).get(0).getData().byteValue();
            this.sizeY = structure.getList("size", IntTag.class).get(1).getData().byteValue();
            this.sizeZ = structure.getList("size", IntTag.class).get(2).getData().byteValue();
            
            CompoundTag data = structure.getCompound("structure");
            CompoundTag defaultPalette = data.getCompound("palette").getCompound("default");
            ListTag<CompoundTag> blockPalette = defaultPalette.getList("block_palette", CompoundTag.class);
            
            ListTag<ListTag> blockIndicesLayers = data.getList("block_indices", ListTag.class);
            ListTag<IntTag> primaryBlockIndices = blockIndicesLayers.get(0);
            ListTag<IntTag> secondaryBlockIndices = blockIndicesLayers.get(1);

            CompoundTag blockPositionDataMap = defaultPalette.getCompound("block_position_data");
            
            int i = 0;
            for(int x = 0; x < sizeX; x++){
                for(int y = 0; y < sizeY; y++){
                    for(int z = 0; z < sizeZ; z++){
                        int primaryBlockIndex = primaryBlockIndices.get(i).getData();
                        if(primaryBlockIndex != -1) {
                            this.blocks.put(getBlockIndex(x, y, z), processBlock(blockPalette.get(primaryBlockIndex)));
                        }

                        if(blockPositionDataMap.contains(String.valueOf(i))){
                            CompoundTag blockPositionData = blockPositionDataMap.getCompound(String.valueOf(i));
                            CompoundTag blockEntityData = blockPositionData.getCompound("block_entity_data");

                            this.blockEntityNbts.put(getBlockIndex(x, y, z), blockEntityData);

                            System.out.println("true");
                        }

                        int secondaryBlockIndex = secondaryBlockIndices.get(i).getData();
                        if(secondaryBlockIndex != -1) {
                            this.secondaryBlocks.put(getBlockIndex(x, y, z), processBlock(blockPalette.get(secondaryBlockIndex)));
                        }

                        i++;
                    }
                }
            }
        }
    }

    private Block processBlock(CompoundTag palette) {
        String blockTypeId = palette.getString("name");
        Block block = Block.get(blockTypeId);
        
        // Block States
        CompoundTag states = palette.getCompound("states");
        states.getEntrySet().forEach(entry -> {
            String stateTypeId = entry.getKey();
            Object stateTag = entry.getValue();
            Object stateValue = null;

            if(stateTag instanceof NumberTag<?> numberTag){
                stateValue = numberTag.getData();
            }else if(stateTag instanceof StringTag stringTag){
                stateValue = stringTag.data;
            }

            System.out.println(stateTypeId + " " + stateValue);

            BaseBlockPropertyType<?> blockPropertyType = (BaseBlockPropertyType<?>) CommonBlockProperties.values().stream()
                .filter(property -> property.getName().equals(stateTypeId))
                .findFirst()
                .orElse(null);
            if(blockPropertyType == null) return;

            block.setPropertyValue(blockPropertyType.tryCreateValue(stateValue));
        });
        
        return block;
    }

    private int getBlockIndex(int x, int y, int z){
        return x << 16 | z << 8 | y;
    }

    @Override
    public Block getBlockAt(Vector3 position) {
        Block block = this.blocks.get(getBlockIndex(position.getFloorX(), position.getFloorY(), position.getFloorZ()));
        if(block == null) return Block.get(Block.AIR);

        return block;
    }

    @Override
    public void place(Location location, boolean includeBlocks, boolean includeBlockEntities, boolean includeEntities) {
        Level level = location.getLevel();
        
        for(int x = 0; x < this.sizeX; x++){
            for(int y = 0; y < this.sizeY; y++){
                for(int z = 0; z < this.sizeZ; z++){
                    Block block = this.blocks.get(getBlockIndex(x, y, z));
                    if(block == null) continue;

                    if(includeBlocks) {
                        level.setBlock(location.add(x, y, z), block);
                    }

                    CompoundTag blockEntityData = this.blockEntityNbts.get(getBlockIndex(x, y, z));
                    if(blockEntityData != null && includeBlockEntities) {
                        BlockEntity blockEntity = BlockEntity.createBlockEntity(blockEntityData.getString("id"), location.add(x, y, z), blockEntityData);

                        IChunk chunk = location.add(x, y, z).getChunk();
                        chunk.addBlockEntity(blockEntity);
                        level.addBlockEntity(blockEntity);

                        if(blockEntity instanceof BlockEntitySpawnable spawnable){
                            spawnable.spawnToAll();
                        }

                        System.out.println("spawned " + blockEntityData.getString("id") + " " + location.add(x, y, z).getLevelBlockEntity().namedTag.toSNBT(2));
                    }

                    Block secondaryBlock = this.secondaryBlocks.get(getBlockIndex(x, y, z));
                    if(secondaryBlock == null) continue;

                    if(includeBlocks) {
                        level.setBlock(location.add(x, y, z), 1, secondaryBlock);   
                    }
                }
            }
        }
    }
}

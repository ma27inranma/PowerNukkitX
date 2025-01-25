package cn.nukkit.level.format;

import java.io.IOException;
import java.util.HashSet;

import cn.nukkit.level.Level;
import cn.nukkit.level.format.leveldb.LevelDBProvider;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.nbt.tag.CompoundTag;

public class BlockEntityDeletingBugAider {
  public BlockEntityDeletingBugAider(Level level){
    this.level = level;
  }

  public Level level;

  public HashSet<BlockVector3> nullBlockEntities = new HashSet<>();

  public void resetBlockEntityCache(BlockVector3 block){
    nullBlockEntities.remove(block);
  }

  public void fix(BlockVector3 block){
    if(nullBlockEntities.contains(block)) return;
    if(getBlockEntity(block.x, block.y, block.z) == null) return;

    this.level.unloadChunk(block.getChunkX(), block.getChunkZ(), false, false);
    this.level.loadChunk(block.getChunkX(), block.getChunkZ(), true);
  }

  public CompoundTag getBlockEntity(int x, int y, int z){
    if(this.level.getProvider() instanceof LevelDBProvider ldbProvider){
      try{
        IChunk chunk_ = ldbProvider.getStorage().readChunk(x >> 4, z >> 4, ldbProvider);
        if(!(chunk_ instanceof Chunk chunk)) return null;

        for(CompoundTag blockEntity : chunk.getBlockEntityNBT()){
          if(blockEntity.getInt("x") == x && blockEntity.getInt("y") == y && blockEntity.getInt("z") == z){
            return blockEntity;
          }
        }

        nullBlockEntities.add(new BlockVector3(x, y, z));
      }catch(IOException e){
        e.printStackTrace();
      }
    }

    return null;
  }
}

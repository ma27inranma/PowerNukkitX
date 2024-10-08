package cn.nukkit.blockentity;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.BlockID;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import cn.nukkit.plugin.InternalPlugin;

/**
* @author ma27inranma
*/

public class BlockEntityBorderBlock extends BlockEntity { // FLAG::MARKER can be lag source
  public BlockEntityBorderBlock(IChunk chunk, CompoundTag nbt) {
    super(chunk, nbt);

    Server.getInstance().getScheduler().scheduleDelayedRepeatingTask(InternalPlugin.INSTANCE, () -> {
      if(!this.isValid()) return;

      sendBorderBlockToAll();
    }, 1, 20, false);
  }
  
  @Override
  public boolean isBlockEntityValid() {
    return getLevelBlock().getId() == BlockID.BORDER_BLOCK;
  }
 
  public void sendBorderBlockToAll() {
    UpdateBlockPacket packet = new UpdateBlockPacket();
    packet.flags = UpdateBlockPacket.FLAG_NETWORK;
    packet.blockRuntimeId = this.getLevelBlock().getRuntimeId();
    packet.x = (int) this.x;
    packet.y = (int) this.y;
    packet.z = (int) this.z;
    
    for(Player player : Server.getInstance().getDefaultLevel().getChunkPlayers(this.getChunkX(), this.getChunkZ()).values()){
      player.dataPacket(packet);
    }
  }
}

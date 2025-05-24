package cn.nukkit.level.structure;

import cn.nukkit.block.Block;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;

public interface IStructure {
	public Block getBlockAt(Vector3 position);
	public void place(Location location, boolean includeBlocks, boolean includeBlockEntities, boolean includeEntities);
}

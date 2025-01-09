package cn.nukkit.block;

public abstract class BlockFroglight extends BlockSolid {

    public BlockFroglight(BlockState blockState) {
        super(blockState);
    }

    @Override
    public int getLightLevel() {
        return 15;
    }

    @Override
    public double getResistance() {
        return 0.3;
    }

    @Override
    public double getHardness() {
        return 0.3; //TODO Without testing, this is just a guess
    }
}

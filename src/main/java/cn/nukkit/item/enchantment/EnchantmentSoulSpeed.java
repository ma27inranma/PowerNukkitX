package cn.nukkit.item.enchantment;


public class EnchantmentSoulSpeed extends Enchantment {


    protected EnchantmentSoulSpeed() {
        super(ID_SOUL_SPEED, "soul_speed", Rarity.VERY_RARE, EnchantmentType.ARMOR_FEET);

        this.setObtainableFromEnchantingTable(false);
    }

    @Override
    public int getMinEnchantAbility(int level) {
        return 10 * level;
    }

    @Override
    public int getMaxEnchantAbility(int level) {
        return getMinEnchantAbility(level) + 15;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}

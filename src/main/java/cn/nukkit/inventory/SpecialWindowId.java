package cn.nukkit.inventory;

public enum SpecialWindowId {
    CREATIVE(-2),
    NONE(-1),
    PLAYER(0),
    OFFHAND(119),
    ARMOR(120),
    CURSOR(124),
    ENDER_CHEST(125),
    FAKE_ENDER_CHEST(118);

    private final int id;

    SpecialWindowId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static SpecialWindowId getWindowIdById(int windowId) {
        for (SpecialWindowId value : values()) {
            if (value.getId() == windowId) {
                return value;
            }
        }
        return null;
    }
}

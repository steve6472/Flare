package steve6472.test;

import steve6472.flare.render.debug.DebugRender;

/**
 * Created by steve6472
 * Date: 9/29/2024
 * Project: Flare <br>
 */
class Rarities
{
    public static final Rarity COMMON = register(new Rarity("common", "Common", DebugRender.WHITE));
    public static final Rarity UNCOMMON = register(new Rarity("uncommon", "Uncommon", DebugRender.LIME));
    public static final Rarity RARE = register(new Rarity("rare", "Rare", DebugRender.BLUE));
    public static final Rarity EPIC = register(new Rarity("epic", "Epic", DebugRender.PURPLE));
    public static final Rarity LEGENDARY = register(new Rarity("legendary", "Legendary", DebugRender.GOLD));

    private static Rarity register(Rarity rarity)
    {
        TestRegistries.RARITY.register(rarity);
        return rarity;
    }
}

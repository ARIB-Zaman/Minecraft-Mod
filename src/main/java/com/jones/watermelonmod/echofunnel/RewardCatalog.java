package com.jones.watermelonmod.echofunnel;

import com.jones.watermelonmod.item.ModItems;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Temporary in-code exchange catalog. Replacing this with reloadable data later
 * will not affect bank storage, networking, or the UI purchasing flow.
 */
public final class RewardCatalog {
    public static final List<RewardOffer> OFFERS = List.of(
            new RewardOffer(ModItems.SILENCE_BREEZE, 4, List.of(5, 4, 1, 4)),
            new RewardOffer(Items.ENDER_PEARL, 4, List.of(2, 6, 4, 2)),
            new RewardOffer(Items.FIRE_CHARGE, 12, List.of(1, 3, 6, 5)),
            new RewardOffer(Items.GOLDEN_APPLE, 2, List.of(6, 2, 5, 3)),
            new RewardOffer(Items.SNOWBALL, 16, List.of(3, 5, 2, 6)),
            new RewardOffer(Items.EXPERIENCE_BOTTLE, 8, List.of(4, 3, 5, 4))
    );

    private RewardCatalog() {
    }
}

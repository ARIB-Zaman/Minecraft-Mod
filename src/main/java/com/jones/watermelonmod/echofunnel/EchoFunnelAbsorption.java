package com.jones.watermelonmod.echofunnel;

import com.jones.watermelonmod.item.ModItems;
import net.minecraft.world.entity.player.Player;

/**
 * Shared state query for future attack interception. Attack code depends on
 * this capability rather than on the Echo Funnel item implementation.
 */
public final class EchoFunnelAbsorption {
    private EchoFunnelAbsorption() {
    }

    public static boolean isAbsorbing(Player player) {
        return player.isUsingItem() && player.getUseItem().is(ModItems.ECHO_FUNNEL);
    }
}

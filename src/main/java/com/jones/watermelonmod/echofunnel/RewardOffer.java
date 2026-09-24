package com.jones.watermelonmod.echofunnel;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** One editable Echo Funnel exchange offer. Costs are points from banks 1–4. */
public record RewardOffer(Item item, int count, List<Integer> bankCosts) {
    public RewardOffer {
        bankCosts = List.copyOf(bankCosts);
        if (count <= 0 || bankCosts.size() != EchoFunnelBankStore.BANK_COUNT || bankCosts.stream().anyMatch(cost -> cost < 0)) {
            throw new IllegalArgumentException("Reward offers require a positive stack and four non-negative bank costs");
        }
    }

    public ItemStack createStack() {
        return new ItemStack(item, count);
    }
}

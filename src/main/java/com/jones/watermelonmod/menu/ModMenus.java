package com.jones.watermelonmod.menu;

import com.jones.watermelonmod.WatermelonMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

/** Menu types are registered independently of individual goggles effects. */
public final class ModMenus {
    public static final MenuType<WorkbenchMenu> WORKBENCH = Registry.register(
            BuiltInRegistries.MENU,
            WatermelonMod.id("workbench"),
            new MenuType<>(WorkbenchMenu::new, FeatureFlags.VANILLA_SET)
    );

    private ModMenus() { }

    public static void initialize() { }
}

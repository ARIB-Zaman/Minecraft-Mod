package com.jones.watermelonmod.client.sonar;

import java.util.Collections;
import java.util.List;

/** Client-only session state for the sonar ping: last sweep result, tick clock, and cooldown gate. */
public final class SonarState {
    public static final int COOLDOWN_TICKS = 60;

    private static List<SonarEcho> echoes = Collections.emptyList();
    private static int clientTick;
    private static int cooldownUntilTick;
    private static int lastPingTick = Integer.MIN_VALUE;

    private SonarState() {
    }

    public static void advanceTick() {
        clientTick++;
    }

    public static int clientTick() {
        return clientTick;
    }

    public static boolean readyToPing() {
        return clientTick >= cooldownUntilTick;
    }

    public static void firePing(List<SonarEcho> newEchoes) {
        echoes = newEchoes;
        cooldownUntilTick = clientTick + COOLDOWN_TICKS;
        lastPingTick = clientTick;
    }

    public static List<SonarEcho> currentEchoes() {
        return echoes;
    }

    public static int lastPingTick() {
        return lastPingTick;
    }

    public static float cooldownPercent() {
        int remaining = cooldownUntilTick - clientTick;
        return remaining <= 0 ? 0.0F : (float) remaining / COOLDOWN_TICKS;
    }
}

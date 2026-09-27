# Builds a small Veil Vault: a 5-wide x 4-tall x 9-deep room extending north
# (-Z) from wherever you're standing when you run this. Stand in an open,
# flat area before running it.

# Clear the volume first so it looks clean regardless of terrain underneath.
fill ~-2 ~-1 ~-8 ~2 ~4 ~1 minecraft:air

# Floor and ceiling.
fill ~-2 ~-1 ~-8 ~2 ~-1 ~0 minecraft:polished_blackstone
fill ~-2 ~4 ~-8 ~2 ~4 ~0 minecraft:polished_blackstone

# Side walls and back wall. The south side (z = ~1, where you're standing) is
# left open as the entrance.
fill ~-2 ~0 ~-8 ~-2 ~3 ~0 minecraft:polished_blackstone
fill ~2 ~0 ~-8 ~2 ~3 ~0 minecraft:polished_blackstone
fill ~-2 ~0 ~-8 ~2 ~3 ~-8 minecraft:polished_blackstone

# Lanterns by the entrance so the room reads clearly before the blur kicks in.
setblock ~-1 ~0 ~-1 minecraft:lantern
setblock ~1 ~0 ~-1 minecraft:lantern

# The Veil Emitter: centred so the blur greets you right at the threshold.
setblock ~0 ~0 ~-4 watermelonmod:veil_emitter

# A sign that is genuinely unreadable through the blur, and a chest of loot
# beneath it as the reward for restoring your view enough to read it.
setblock ~0 ~1 ~-7 minecraft:oak_wall_sign[facing=south]{front_text:{messages:['"VEIL VAULT"','"IF YOU CAN"','"READ THIS,"','"TAKE THE LOOT"']}}
setblock ~0 ~0 ~-6 minecraft:chest
item replace block ~0 ~0 ~-6 container.0 with watermelonmod:silence_breeze 2
item replace block ~0 ~0 ~-6 container.1 with watermelonmod:freeze_breeze 2
item replace block ~0 ~0 ~-6 container.2 with watermelonmod:damage_breeze 2
item replace block ~0 ~0 ~-6 container.4 with minecraft:golden_apple 1

tellraw @s {"text":"The Veil Vault is built. Step inside to feel the blur.","color":"light_purple"}

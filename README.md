# Realistic Item Weight - Minecraft Plugin

A lightweight and immersive Bukkit/Spigot plugin that adds a **realistic inventory weight system** to Minecraft. The more you carry, the slower you move, the less you jump, and the harder you fall. Perfect for survival servers looking for added realism and challenge.

---

##  Features

-  **Inventory-Based Weight System**  
  Every item in Minecraft has a weight value (default or configurable). Your total carried weight affects your gameplay.

-  **Player Modifiers**
  - Movement speed decreases as weight increases
  - Jump strength is reduced with heavier inventories
  - Fall damage increases and safe-fall distance decreases when overburdened

- **Fully Configurable**
  - Easily customize item weights in `config.yml`
  - Toggle the entire weight system on/off with `WeightsEnabled: true/false`

- **Real-Time Updates**
  - Weight recalculated on item pickup/drop, inventory interaction, player join/leave
  - Background updater runs every second



## Configuration

Example `config.yml`:

```yaml
WeightsEnabled: true

items:
  STONE: 0.0002
  OAK_LOG: 0.0004
  OAK_PLANK: 0.0001
  DIAMOND: 0.01
  FEATHER: 0.0001
  NETHERITE_SWORD: 0.02
  

package me.goodi.itemWeights;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.BlockState;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class WeightManager implements Listener {

    private final JavaPlugin plugin;
    private final Config config;
    private final Map<Material, Double> itemWeightCache = new EnumMap<>(Material.class);
    private final Map<UUID, Double> playerWeightCache = new HashMap<>();
    private final Set<UUID> scheduledUpdates = new HashSet<>();


    public WeightManager(JavaPlugin plugin, Config config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void initialize() {
        preloadItemWeights();
        startWeightUpdater();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }


    public void preloadItemWeights() {
        for (Material material : Material.values()) {
            if(!material.isItem()) continue;
            double weight = config.getItemWeight(material);
            itemWeightCache.put(material, weight);
        }
    }

    public double getItemWeight(Material material) {
        return itemWeightCache.getOrDefault(material, config.DEFAULT_WEIGHT);
    }

    public void updatePlayerWeight(Player player) {
        double weight = 0;

        for (ItemStack item : player.getInventory()) {
            if (item == null || item.getType() == Material.AIR) continue;

            if(item.getType().name().toUpperCase().contains("SHULKER_BOX")){

                if (item.getItemMeta() instanceof BlockStateMeta) {
                    BlockStateMeta meta = (BlockStateMeta) item.getItemMeta();
                    BlockState state = meta.getBlockState();

                    if (state instanceof ShulkerBox) {
                        ShulkerBox box = (ShulkerBox) state;
                        Inventory shulkerInv = box.getInventory();


                        for (ItemStack containedItem : shulkerInv.getContents()) {
                            if (containedItem == null || containedItem.getType() == Material.AIR) continue;
                            weight += getItemWeight(containedItem.getType()) * containedItem.getAmount();

                        }
                    }
                }
            }

            weight += getItemWeight(item.getType()) * item.getAmount();

        }

        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item == null || item.getType() == Material.AIR) continue;
            weight += getItemWeight(item.getType()) * item.getAmount();
        }

        ItemStack offhandItem = player.getInventory().getItemInOffHand();

        if(offhandItem.getType() != Material.AIR) {
            weight += getItemWeight(offhandItem.getType());
        }

        if(!config.isWeightActive()){
            playerWeightCache.put(player.getUniqueId(), 0.0);
            updatePlayerMovement(player);
            return;
        }

        playerWeightCache.put(player.getUniqueId(), weight);
        updatePlayerMovement(player);
    }


    public double getCachedPlayerWeight(Player player) {
        return playerWeightCache.getOrDefault(player.getUniqueId(), 0.0);
    }

    private void startWeightUpdater() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    updatePlayerWeight(player);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void updatePlayerMovement(Player player) {
        double weight = getCachedPlayerWeight(player);
        double jumpPenalty;
        double speedPenalty;
        double fallPenalty;
        double safeFall = 3;

        final double BASE_SPEED = 0.1;
        final double BASE_JUMP = 0.42;
        final double BASE_FALL_MULTIPLIER = 1.0;


        if(weight <= 0.07){
            speedPenalty = weight;
        }
        else{
            speedPenalty = (weight+0.07)/2;
        }



        if (weight <= 0.05) {
            jumpPenalty = weight * 1.3;
        } else if (weight <= 0.08) {
            jumpPenalty = weight * 1.6;
        } else if (weight <= 0.12) {
            jumpPenalty = weight * 2.0;
        } else if (weight <= 0.15) {
            jumpPenalty = weight * 4.0;
        } else {
            jumpPenalty = weight * Math.pow(20, 20); //like inf or sum
        }

        if (weight <= 0.02) {
            fallPenalty = 1;
        } else if (weight <= 0.04) {
            safeFall = 2.5;
            fallPenalty = 1.1;
        } else if (weight <= 0.06) {
            safeFall = 2;
            fallPenalty = 1.2;
        } else if (weight <= 0.1) {
            safeFall = 1.5;
            fallPenalty = 1.3;
        } else if (weight <= 0.14) {
            safeFall = 1;
            fallPenalty = 1.6;
        } else {
            fallPenalty = 2;
            safeFall = 0.4;
        }


        double newSpeed = Math.max(0.0, BASE_SPEED - speedPenalty);
        double newJump = Math.max(0.0, BASE_JUMP - jumpPenalty);

        double newFallDamageMultiplier = BASE_FALL_MULTIPLIER * fallPenalty;

        if (player.getAttribute(Attribute.MOVEMENT_SPEED) != null) {
            player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(newSpeed);
        }
        if (player.getAttribute(Attribute.JUMP_STRENGTH) != null) {
            player.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(newJump);
        }
        if (player.getAttribute(Attribute.FALL_DAMAGE_MULTIPLIER) != null) {
            player.getAttribute(Attribute.FALL_DAMAGE_MULTIPLIER).setBaseValue(newFallDamageMultiplier);
        }
        if (player.getAttribute(Attribute.SAFE_FALL_DISTANCE) != null) {
            player.getAttribute(Attribute.SAFE_FALL_DISTANCE).setBaseValue(safeFall);
        }
    }



    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player player) {
            runTask(player);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent e) {
        if (e.getWhoClicked() instanceof Player player) {
            runTask(player);
        }
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        runTask(player);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        runTask(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        runTask(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        playerWeightCache.remove(player.getUniqueId());
    }

    public void runTask(Player player) {
        if (!config.isWeightActive()) return;

        if (scheduledUpdates.add(player.getUniqueId())) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                updatePlayerWeight(player);
                scheduledUpdates.remove(player.getUniqueId());
            });
        }
    }
}

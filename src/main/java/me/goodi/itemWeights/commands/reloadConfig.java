package me.goodi.itemWeights.commands;

import me.goodi.itemWeights.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class reloadConfig implements CommandExecutor {

    private final Config config;

    public reloadConfig(Config config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {


        if (sender instanceof Player player) {
            if(player.hasPermission("ItemWeights.reload")){
                config.reload();
            }
        }


        return false;
    }
}

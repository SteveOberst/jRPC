package net.sxlver.jrpc.exampleplugin.conversation.model;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class PlayerDTO {

    private UUID uuid;
    private String name;

    private double health;
    private int foodLevel;
    private double walkSpeed;

    PlayerDTO(final Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.health = player.getHealth();
        this.foodLevel = player.getFoodLevel();
        this.walkSpeed = player.getWalkSpeed();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public static PlayerDTO fromPlayer(final Player player) {
        return new PlayerDTO(player);
    }
}

package com.example.scrambler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ScramblerPlugin extends JavaPlugin implements Listener {

    // Unicode chars that render in vanilla Minecraft's default font.
    private static final String[] SCRAMBLE_CHARS = {
        "▓", "▒", "░", "█", "▄", "▀", "■", "□", "▪", "▫",
        "◆", "◇", "○", "●", "◈", "◉", "◎", "◌", "◍",
        "⌁", "⌂", "⌐", "¬", "±", "÷", "×", "≈", "≠", "≡",
        "▁", "▂", "▃", "▅", "▆", "▇", "▉", "▊", "▋", "▌", "▍", "▎", "▏",
        "◢", "◣", "◤", "◥", "◾", "◽", "◼", "◻"
    };

    private final Map<UUID, String> originalNames = new HashMap<>();
    private final Random random = new Random();
    private BukkitRunnable scramblerTask;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        scramblerTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    boolean invisible = player.hasPotionEffect(PotionEffectType.INVISIBILITY);

                    if (invisible) {
                        originalNames.putIfAbsent(player.getUniqueId(), player.getName());
                        String scrambled = generateScrambledName(player.getName());
                        player.playerListName(
                            Component.text(scrambled).color(NamedTextColor.GRAY)
                        );
                    } else if (originalNames.containsKey(player.getUniqueId())) {
                        player.playerListName(
                            Component.text(player.getName()).color(NamedTextColor.WHITE)
                        );
                        originalNames.remove(player.getUniqueId());
                    }
                }
            }
        };
        // Run once every second (20 ticks). Change the last number for speed.
        scramblerTask.runTaskTimer(this, 0L, 20L);

        getLogger().info("InvisibleScrambler enabled.");
    }

    private String generateScrambledName(String originalName) {
        int length = Math.max(originalName.length(), 8);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(SCRAMBLE_CHARS[random.nextInt(SCRAMBLE_CHARS.length)]);
        }
        return sb.toString();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        originalNames.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public void onDisable() {
        if (scramblerTask != null) scramblerTask.cancel();
        for (Map.Entry<UUID, String> entry : originalNames.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                player.playerListName(Component.text(entry.getValue()));
            }
        }
        originalNames.clear();
    }
}
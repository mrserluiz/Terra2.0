package com.dfsek.terra.bukkit.util;

import com.dfsek.terra.bukkit.TerraBukkitPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

/** Console-only decoration, emitted once after successful plugin initialization. */
public final class StartupBanner {
    private StartupBanner() {}
    public static void print(TerraBukkitPlugin plugin, boolean generationEnabled, int configuredWorlds) {
        var console=Bukkit.getConsoleSender();
        console.sendMessage(Component.empty());
        line("  ████████████", NamedTextColor.GREEN, "  TERRA 2.0", NamedTextColor.GREEN);
        line("  ████████████", NamedTextColor.DARK_GREEN, "  "+plugin.getDescription().getVersion(), NamedTextColor.WHITE);
        line("  ▓▒▓▓▒▓▒▓▓▒▓▓", NamedTextColor.GOLD, "  Geracao independente por mundo", NamedTextColor.GRAY);
        line("  ▒▓▒▓▓▒▓▒▓▓▒▓", NamedTextColor.GOLD, "  "+Bukkit.getName()+" "+Bukkit.getBukkitVersion(), NamedTextColor.WHITE);
        line("  ▓▓▒▓▒▓▓▒▓▒▓▓", NamedTextColor.GOLD, "  Geracao: "+(generationEnabled?"habilitada; autorizacao por mundo":"desativada"), NamedTextColor.WHITE);
        line("  ▒▓▓▒▓▒▓▓▒▓▒▓", NamedTextColor.GOLD, "  Mundos configurados: "+configuredWorlds, NamedTextColor.WHITE);
        console.sendMessage(Component.text("  github.com/mrserluiz/Terra2.0", NamedTextColor.DARK_GRAY));
        console.sendMessage(Component.empty());
    }
    private static void line(String pixel, NamedTextColor color, String text, NamedTextColor textColor) {
        Bukkit.getConsoleSender().sendMessage(Component.text(pixel,color).append(Component.text(text,textColor)));
    }
}

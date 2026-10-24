package com.dfsek.terra.bukkit.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;

/** Requested &2■Terra&f.&#803c002&f: prefix, parsed once; diagnostic text remains literal. */
public final class DiagnosticMessages {
    private DiagnosticMessages() {}
    public static Component format(String message) {
        return Component.empty().append(Component.text("■Terra",NamedTextColor.DARK_GREEN))
            .append(Component.text(".",NamedTextColor.WHITE))
            .append(Component.text("2",TextColor.color(0x803c00)))
            .append(Component.text(": "+message,NamedTextColor.WHITE));
    }
    public static void send(CommandSender sender,String message) { sender.sendMessage(format(message)); }
}

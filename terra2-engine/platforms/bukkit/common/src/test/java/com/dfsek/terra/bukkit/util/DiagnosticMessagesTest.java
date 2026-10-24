package com.dfsek.terra.bukkit.util;
import org.junit.jupiter.api.Test;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import static org.junit.jupiter.api.Assertions.*;
class DiagnosticMessagesTest {
    @Test void prefixMatchesRequestedColorsAndBodyRemainsLiteral() {
        Component text=DiagnosticMessages.format("ID &#ff0000 &2<red>");
        assertEquals(Component.text("■Terra",NamedTextColor.DARK_GREEN),text.children().get(0));
        assertEquals(Component.text(".",NamedTextColor.WHITE),text.children().get(1));
        assertEquals(Component.text("2",TextColor.color(0x803c00)),text.children().get(2));
        assertEquals(Component.text(": ID &#ff0000 &2<red>",NamedTextColor.WHITE),text.children().get(3));
    }
}

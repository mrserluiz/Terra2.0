package com.dfsek.terra.bukkit.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommandMessagesTest {
    private Map<String, String> read(String locale) throws Exception {
        try(var reader = new InputStreamReader(getClass().getResourceAsStream("/lang/" + locale + ".json"), StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
        }
    }
    @Test void allThirteenLanguagesHaveSameKeysPlaceholdersAndStableCommands() throws Exception {
        var english = read("en_US");
        var placeholders = Pattern.compile("\\{\\d+}");
        assertEquals(13, CommandMessages.LANGUAGES.size());
        for(String locale : CommandMessages.LANGUAGES) {
            var translated = read(locale);
            assertEquals(english.keySet(), translated.keySet(), locale);
            for(String key : english.keySet()) {
                assertFalse(translated.get(key).isBlank(), locale + "/" + key);
                assertEquals(placeholders.matcher(english.get(key)).results().map(match -> match.group()).toList(),
                    placeholders.matcher(translated.get(key)).results().map(match -> match.group()).toList(), locale + "/" + key);
            }
            assertTrue(CommandMessages.text(locale, "help").contains("/terra2 lis Cpack"));
        }
        assertEquals(CommandMessages.text("pt_BR", "help"), CommandMessages.text("unknown", "help"));
        assertEquals(CommandMessages.text("en_US", "help"), CommandMessages.text("en-us", "help"));
        assertTrue(CommandMessages.text("en_US", "unlock-success", "{1}", "OVERWORLD").contains("World {1} authorized with OVERWORLD"));
    }
}

package com.dfsek.terra.bukkit.nms;

import java.nio.file.*;
import net.minecraft.nbt.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.terra2.pack.*;
import static org.junit.jupiter.api.Assertions.*;

class NativeTemplateScopeTest {
    @TempDir Path directory;
    @Test void scopesPoolsLootAndSpawnerIdsAndPreservesConnectorLabelsAndTypedNbtAcrossRestart() throws Exception {
        for(String path : new String[]{"data/test/worldgen/template_pool/rooms.json", "data/test/loot_table/chest.json", "data/test/trial_spawner/trial.json"}) {
            var file = directory.resolve(path); Files.createDirectories(file.getParent()); Files.writeString(file, "{}");
        }
        var scope = new ResourceScope(ResourceBundle.read(directory));
        var root = new CompoundTag(); root.putInt("DataVersion", 123);
        var jigsaw = new CompoundTag(); jigsaw.putString("id", "minecraft:jigsaw");
        jigsaw.putString("pool", "test:rooms"); jigsaw.putString("name", "test:rooms"); jigsaw.putString("target", "test:rooms");
        jigsaw.putShort("selection_priority", (short) 2); root.put("jigsaw", jigsaw);
        var chest = new CompoundTag(); chest.putString("LootTable", "test:chest"); chest.putLong("LootTableSeed", 42L); root.put("chest", chest);
        var trial = new CompoundTag(); trial.putString("normal_config", "test:trial"); root.put("trial", trial);
        var display = new CompoundTag(); display.putString("minecraft:item_model", "test:rooms"); root.put("components", display);
        var scoped = NativeTemplateScope.rewrite(root, scope);
        assertEquals("test:rooms", root.getCompoundOrEmpty("jigsaw").getStringOr("pool", ""));
        assertEquals(scope.privateId("test:rooms"), scoped.getCompoundOrEmpty("jigsaw").getStringOr("pool", ""));
        assertEquals("test:rooms", scoped.getCompoundOrEmpty("jigsaw").getStringOr("name", ""));
        assertEquals("test:rooms", scoped.getCompoundOrEmpty("jigsaw").getStringOr("target", ""));
        assertEquals(scope.privateId("test:chest"), scoped.getCompoundOrEmpty("chest").getStringOr("LootTable", ""));
        assertEquals(scope.privateId("test:trial"), scoped.getCompoundOrEmpty("trial").getStringOr("normal_config", ""));
        assertEquals("test:rooms", scoped.getCompoundOrEmpty("components").getStringOr("minecraft:item_model", ""));
        var file = directory.resolve("saved-template.nbt"); Files.write(file, NativeTemplateScope.write(scoped));
        var restored = NativeTemplateScope.read(Files.readAllBytes(file));
        assertEquals(scoped, restored);
        assertInstanceOf(ShortTag.class, restored.getCompoundOrEmpty("jigsaw").get("selection_priority"));
        assertInstanceOf(LongTag.class, restored.getCompoundOrEmpty("chest").get("LootTableSeed"));
    }
}

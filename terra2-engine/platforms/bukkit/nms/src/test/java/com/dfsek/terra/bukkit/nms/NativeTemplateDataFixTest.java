package com.dfsek.terra.bukkit.nms;

import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NativeTemplateDataFixTest {
    private static ListTag vector(int x, int y, int z) {
        var list = new ListTag(); list.add(IntTag.valueOf(x)); list.add(IntTag.valueOf(y)); list.add(IntTag.valueOf(z)); return list;
    }
    @Test void nativeStructureFixerMigratesHistoricalBlockNamesAndRoundTripsTemplateData() {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        var source = new CompoundTag(); source.putInt("DataVersion", 2586); source.put("size", vector(1, 1, 1));
        var state = new CompoundTag(); state.putString("Name", "minecraft:grass_path");
        var palette = new ListTag(); palette.add(state); source.put("palette", palette);
        var block = new CompoundTag(); block.put("pos", vector(0, 0, 0)); block.putInt("state", 0);
        var blocks = new ListTag(); blocks.add(block); source.put("blocks", blocks); source.put("entities", new ListTag());
        var fixed = DataFixTypes.STRUCTURE.updateToCurrentVersion(DataFixers.getDataFixer(), source.copy(), 2586);
        assertTrue(fixed.toString().contains("minecraft:dirt_path"));
        assertTrue(source.toString().contains("minecraft:grass_path"));
        var template = new StructureTemplate(); template.load(BuiltInRegistries.BLOCK, fixed);
        var saved = template.save(new CompoundTag());
        assertTrue(saved.toString().contains("minecraft:dirt_path"));
        assertEquals(1, template.getSize().getX()); assertEquals(1, template.getSize().getY()); assertEquals(1, template.getSize().getZ());
    }
}

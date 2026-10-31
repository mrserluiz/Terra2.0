package com.dfsek.terra.bukkit.nms;

/** Paper 26.3 environment colors are normalized RGB vectors; pack colors remain RGB24. */
public final class NativeColor {
    private NativeColor() {}
    public static org.joml.Vector3fc rgb(int color){return net.minecraft.util.ARGB.vector3fFromRGB24(color);}
    public static int rgb24(org.joml.Vector3fc color){return net.minecraft.util.ARGB.colorFromVector3f(color)&0xFFFFFF;}
}

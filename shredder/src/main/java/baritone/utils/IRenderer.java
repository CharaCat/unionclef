/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package baritone.utils;

import baritone.api.BaritoneAPI;
import baritone.api.Settings;
import baritone.utils.accessor.IEntityRenderDispatcher;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;

public interface IRenderer {

    IEntityRenderDispatcher renderManager = (IEntityRenderDispatcher) Minecraft.getInstance().getEntityRenderDispatcher();
    Settings settings = BaritoneAPI.getSettings();

    float[] color = new float[]{1.0F, 1.0F, 1.0F, 255.0F};

    static void glColor(Color color, float alpha) {
        float[] colorComponents = color.getColorComponents(null);
        IRenderer.color[0] = colorComponents[0];
        IRenderer.color[1] = colorComponents[1];
        IRenderer.color[2] = colorComponents[2];
        IRenderer.color[3] = alpha;
    }

    static BufferBuilder startLines(Color color, float alpha, float ignoredLineWidth, boolean ignoredDepth) {
        return startLines(color, alpha);
    }

    static BufferBuilder startLines(Color color, float ignoredLineWidth, boolean ignoredDepth) {
        return startLines(color);
    }

    static BufferBuilder startLines(Color color, float alpha) {
        glColor(color, alpha);
        return null;
    }

    static BufferBuilder startLines(Color color) {
        return startLines(color, .4f);
    }

    static void endLines(BufferBuilder bufferBuilder, boolean ignoredDepth) {
    }

    static BufferBuilder startBlockQuads() {
        return null;
    }

    static void endBuffer(BufferBuilder bufferBuilder, RenderType renderType) {
    }

    static void emitAABB(BufferBuilder bufferBuilder, PoseStack stack, AABB aabb) {
    }

    static void emitAABB(BufferBuilder bufferBuilder, PoseStack stack, AABB aabb, double expand) {
    }

    static void emitAABB(BufferBuilder bufferBuilder, PoseStack stack, AABB aabb, float lineWidth) {
    }

    static void emitAABB(BufferBuilder bufferBuilder, PoseStack stack, AABB aabb, double expand, float lineWidth) {
    }

    static void emitLine(BufferBuilder bufferBuilder, PoseStack stack, Vec3 start, Vec3 end) {
    }

    static void emitLine(BufferBuilder bufferBuilder, PoseStack stack, Vec3 start, Vec3 end, float lineWidth) {
    }

    static void emitLine(BufferBuilder bufferBuilder, PoseStack stack, double x1, double y1, double z1, double x2, double y2, double z2, float lineWidth) {
    }

    static void emitLine(BufferBuilder bufferBuilder, PoseStack stack,
                         double x1, double y1, double z1,
                         double x2, double y2, double z2,
                         double nx, double ny, double nz,
                         float lineWidth) {
    }

    static void emitLine(BufferBuilder bufferBuilder, PoseStack stack,
                         float x1, float y1, float z1,
                         float x2, float y2, float z2,
                         float nx, float ny, float nz,
                         float lineWidth) {
    }

    static void emitTexturedVertex(BufferBuilder bufferBuilder, PoseStack.Pose pose, float x, float y, float z, int color, float u, float v, float nx, float ny, float nz) {
    }

    static RenderType beaconBeam(Identifier identifier, boolean translucent) {
        return null;
    }

    static RenderType beaconBeam(Identifier identifier, boolean translucent, boolean ignoreDepth) {
        return null;
    }
}

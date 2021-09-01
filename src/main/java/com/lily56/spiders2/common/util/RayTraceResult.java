package com.lily56.spiders2.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public abstract class RayTraceResult {
    protected final Vec3d hitResult;
    /** Used to determine what sub-segment is hit */
    public int subHit = -1;

    /** Used to add extra hit info */
    public Object hitInfo = null;

    protected RayTraceResult(Vec3d hitVec) {
        this.hitResult = hitVec;
    }

    public double RayTrace(Entity rayhit) {
        double d0 = this.hitResult.x - rayhit.getX();
        double d1 = this.hitResult.y - rayhit.getY();
        double d2 = this.hitResult.z - rayhit.getZ();
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public abstract RayTraceResult.Type getType();

    /**
     * Returns the hit position of the raycast, in absolute world coordinates
     */
    public Vec3d getHitVec() {
        return this.hitResult;
    }

    public static enum Type {
        MISS,
        BLOCK,
        ENTITY;
    }
}
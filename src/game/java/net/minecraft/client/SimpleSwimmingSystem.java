package net.minecraft.client;

import net.minecraft.world.phys.Vec3;

public class SimpleSwimmingSystem {

    boolean inWater;
    boolean sprinting;

    Vec3 position = Vec3.ZERO;
    Vec3 velocity = Vec3.ZERO;

    Vec3 lookDirection = new Vec3(0.0, 0.0, 1.0);
    Vec3 movementInput = Vec3.ZERO;

    static final double BASE_WATER_ACCELERATION = 0.02;
    static final double SPRINT_WATER_ACCELERATION = 0.04;

    static final double WATER_DRAG = 0.80;
    static final double AIR_DRAG = 0.98;
    static final double GRAVITY = 0.08;

    public void tickMovement() {
        if (inWater) {
            Vec3 forward = lookDirection.normalize();
            Vec3 worldUp = new Vec3(0.0, 1.0, 0.0);

            Vec3 right = forward.cross(worldUp);
            if (right.length() < 0.0001) {
                right = new Vec3(1.0, 0.0, 0.0);
            } else {
                right = right.normalize();
            }

            Vec3 inputDir = new Vec3(
                    right.x * movementInput.x + forward.x * movementInput.z,
                    forward.y * movementInput.z,
                    right.z * movementInput.x + forward.z * movementInput.z
            );

            double accel = sprinting ? SPRINT_WATER_ACCELERATION : BASE_WATER_ACCELERATION;
            velocity = velocity.add(inputDir.scale(accel));
            velocity = velocity.scale(WATER_DRAG);
        } else {
            velocity = new Vec3(
                    velocity.x * AIR_DRAG,
                    velocity.y - GRAVITY,
                    velocity.z * AIR_DRAG
            );

            if (movementInput.length() > 0.0001) {
                Vec3 horizontal = new Vec3(movementInput.x, 0.0, movementInput.z).normalize().scale(0.1);
                velocity = velocity.add(horizontal);
            }
        }

        position = position.add(velocity);
    }

    public void setInWater(boolean inWater) {
        this.inWater = inWater;
    }

    public void setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
    }

    public void setLookDirection(Vec3 lookDirection) {
        this.lookDirection = lookDirection;
    }

    public void setMovementInput(Vec3 movementInput) {
        this.movementInput = movementInput;
    }

    public Vec3 getPosition() {
        return position;
    }

    public Vec3 getVelocity() {
        return velocity;
    }
}

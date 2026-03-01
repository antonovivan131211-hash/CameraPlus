package ru.tpcamera.camera;

public final class DynamicMotion {
	public static final DynamicMotion ZERO = new DynamicMotion(0.0D, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);

	public final double offsetX;
	public final double offsetY;
	public final double offsetZ;
	public final double distanceOffset;
	public final float yawOffset;
	public final float pitchOffset;

	public DynamicMotion(double offsetX, double offsetY, double offsetZ, double distanceOffset, float yawOffset, float pitchOffset) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
		this.distanceOffset = distanceOffset;
		this.yawOffset = yawOffset;
		this.pitchOffset = pitchOffset;
	}
}

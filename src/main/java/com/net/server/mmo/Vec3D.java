package com.net.server.mmo;

import java.util.Arrays;
import java.util.List;

public class Vec3D {
	private final Number px;
	private final Number py;
	private final Number pz;
	private final boolean useFloat;

	public static Vec3D fromIntArray(List<Integer> array) {
		if (array.size() != 3) {
			throw new IllegalArgumentException("Wrong array size. Vec3D requires an array with 3 parameters (x,y,z)");
		}
		return new Vec3D(((Integer) array.get(0)).intValue(), ((Integer) array.get(1)).intValue(),
				((Integer) array.get(2)).intValue());
	}

	public static Vec3D fromFloatArray(List<Float> array) {
		if (array.size() != 3) {
			throw new IllegalArgumentException("Wrong array size. Vec3D requires an array with 3 parameters (x,y,z)");
		}
		return new Vec3D(((Float) array.get(0)).floatValue(), ((Float) array.get(1)).floatValue(),
				((Float) array.get(2)).floatValue());
	}

	public Vec3D(int ix, int iy, int iz) {
		this.px = Integer.valueOf(ix);
		this.py = Integer.valueOf(iy);
		this.pz = Integer.valueOf(iz);
		this.useFloat = false;
	}

	public Vec3D(float fx, float fy, float fz) {
		this.px = Float.valueOf(fx);
		this.py = Float.valueOf(fy);
		this.pz = Float.valueOf(fz);
		this.useFloat = true;
	}

	public Vec3D(int ix, int iy) {
		this(ix, iy, 0);
	}

	public Vec3D(float fx, float fy) {
		this(fx, fy, 0.0F);
	}

	public boolean isFloat() {
		return this.useFloat;
	}

	public float floatX() {
		return this.px.floatValue();
	}

	public float floatY() {
		return this.py.floatValue();
	}

	public float floatZ() {
		return this.pz.floatValue();
	}

	public int intX() {
		return this.px.intValue();
	}

	public int intY() {
		return this.py.intValue();
	}

	public int intZ() {
		return this.pz.intValue();
	}

	public String toString() {
		return String.format("(%s, %s, %s)", new Object[] { this.px, this.py, this.pz });
	}

	public List<Integer> toIntArray() {
		return Arrays
				.asList(new Integer[] { Integer.valueOf(intX()), Integer.valueOf(intY()), Integer.valueOf(intZ()) });
	}

	public List<Float> toFloatArray() {
		return Arrays.asList(new Float[] { Float.valueOf(floatX()), Float.valueOf(floatY()), Float.valueOf(floatZ()) });
	}
}
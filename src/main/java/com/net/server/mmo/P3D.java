package com.net.server.mmo;

import org.apache.commons.lang.builder.HashCodeBuilder;

class P3D {
	public final int px;
	public final int py;
	public final int pz;

	public P3D(int x, int y, int z) {
		this.px = x;
		this.py = y;
		this.pz = z;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof P3D)) {
			return false;
		}
		P3D p2 = (P3D) obj;
		return (p2.px == this.px) && (p2.py == this.py) && (p2.pz == this.pz);
	}

	public int hashCode() {
		return new HashCodeBuilder(23, 31).append(this.px).append(this.py).append(this.pz).toHashCode();
	}

	public String toString() {
		return String.format("(%s, %s, %s)",
				new Object[] { Integer.valueOf(this.px), Integer.valueOf(this.py), Integer.valueOf(this.pz) });
	}
}
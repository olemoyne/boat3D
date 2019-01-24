package model.math;

import model.Area;

public class Bounds3D {
	
	private Vecteur max;
	private Vecteur min;
	
	public Bounds3D () {
	}

	public Bounds3D(Bounds3D o) {
		max = new Vecteur (o.getMax());
		min = new Vecteur (o.getMin());
	}

	public void addPoint(Vecteur v) {
		if (max == null) {
			max = v; min = v; return;
		}
		
		if (max.getX() < v.getX()) max = max.set(Axis.XAxis, v.getX());
		if (max.getY() < v.getY()) max = max.set(Axis.YAxis, v.getY());
		if (max.getZ() < v.getZ()) max = max.set(Axis.ZAxis, v.getZ());

		if (min.getX() > v.getX()) min = min.set(Axis.XAxis, v.getX());
		if (min.getY() > v.getY()) min = min.set(Axis.YAxis, v.getY());
		if (min.getZ() > v.getZ()) min = min.set(Axis.ZAxis, v.getZ());
	}

	
	public Vecteur getMax() {
		return max;
	}

	public Vecteur getMin() {
		return min;
	}

	public Bounds3D add(Area a) {
		for (Vecteur v : a.points) addPoint(v);
		return this;
	}

	public Bounds3D add(Bounds3D bounds) {
		Bounds3D bnds = new Bounds3D(this);
		bnds.addPoint(bounds.max);
		bnds.addPoint(bounds.min);
		return bnds;
	}

	public Decimal getSize(int axis) {
		return this.max.getDec(axis).minus(min.getDec(axis));
	}


}

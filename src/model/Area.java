package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import model.calcul.CalculSurface;
import model.math.Axis;
import model.math.Bounds3D;
import model.math.Decimal;
import model.math.Droite3D;
import model.math.MapDeVecteurs;
import model.math.Plan3D;
import model.math.Segment;
import model.math.Vecteur;
import model.math.transfo.Transformation;

/** 
 * Liste de points dï¿½finissant une surface
 * 
 * @author olemoyne
 *
 */
public class Area implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4445241224165587862L;
	public ArrayList<Vecteur> points;
	
	public Position situation;
	
	
	private class Point  {
		int pos;
		Vecteur inter;
	}
	
	public Area () {
		points = new ArrayList<Vecteur>();
		situation = new Position();
	}
	
	public Area(ArrayList<Vecteur> pts) {
		points = pts;
		situation = new Position();
	}

	public Area(Area surf) {
		points = new ArrayList<Vecteur>(surf.points);
		situation = new Position();
	}

	public static Vecteur getMediatrice (Vecteur pt1, Vecteur pt2) {
		Vecteur dir = pt2.minus(pt1);
		Decimal n = dir.getNorme();
		Decimal x= dir.getDecY().negate().divide(n);
		Decimal y= dir.getDecX().divide(n);
		dir = new Vecteur (x, y, dir.getDecZ());
		return dir;
	}
	
	
	private static Vecteur getResizedPoint(Vecteur last, Vecteur my, Vecteur next, Vecteur ctr, Decimal delta) {
		Vecteur dir = null;
		if (!next.equals(last)) {
			if (next.minus(my).estColineaire(my.minus(last))) {
				// Dans ce cas on prends la droite perpendiculaire
				dir = getMediatrice(last, next);

				if (!dir.getNorme().isZero()) {
					Droite3D dr = new Droite3D(dir, my);
					Vecteur p = dr.getPoint(delta.negate());
					Decimal dist = my.decDistance(ctr);
					Decimal dpt = p.decDistance(ctr);
					if (dpt.compareTo(dist) > 0) {
						p = dr.getPoint(delta);
					}
					return p;
				} else {
					System.err.println("Erreur : "+last.toString()+" "+my.toString()+" "+next.toString());
					return null;
				}
			} else {
				Vecteur v = next.minus(my.minus(last));
				Droite3D drt = new Droite3D(v.minus(my), my);
				return drt.getPoint(delta);
			}
		}		
		return null;
	}

	/**
	 * Retaille la forme en rognant la distance demandï¿½e
	 * 
	 * @param negate
	 * @return
	 */
	public Area resizeNormale(Decimal enPlus, int ax) {
		Area ret = new Area();
		if (enPlus.isZero()) return this;
		if (points.size() == 0) return this;
		Vecteur ctr = CalculSurface.getCentre(points, ax);

		ArrayList<Vecteur> pts = getUnitedPoints();
		
		Decimal delta = enPlus;//.negate();
		Vecteur last = pts.get(pts.size()-1);
		for (int pos =0; pos < pts.size(); pos ++) {
			Vecteur my = pts.get(pos);
			Vecteur next = null;
			if (pos < pts.size() - 1) next = pts.get(pos + 1);
			else next = pts.get(0);
			
			Vecteur p = getResizedPoint(last, my, next, ctr, delta);
			if (p != null) ret.points.add(p);
			if (my.getY() == -186) {
				System.out.println(last.toString()+" ; "+my.toString()+" ; "+next.toString()+" : Resized = "+p.toString()+" + "+delta.toString());
			}
			last = my;
		}
		return ret;
	}

	private ArrayList<Vecteur> getUnitedPoints() {
		ArrayList<Vecteur> ret = new ArrayList<Vecteur> ();
		Vecteur last = points.get(points.size()-1);
		for (Vecteur v : points) {
			if (!v.equals(last)) ret.add(v); 
			last = v;
		}
		return ret;
	}

	/**
	 * Retaille la forme en rognant la distance demandï¿½e
	 * 
	 * @param negate
	 * @return
	 */
	public Area resizeCentre(Decimal enPlus, int ax) {
		Area ret = new Area();
		Vecteur ctr = CalculSurface.getCentre(points, ax);
		Decimal delta = enPlus.multiply(Vecteur.METER);
		for (Vecteur v : points) {
			// Calcule la nouvelle position du vecteur
			Decimal l = v.distance(ctr);
			if (!l.isZero()) {
				Decimal coef = l.add(delta).divide(l); 
			// Applique le coeficient au point
				Decimal x = ctr.getDecX().add(v.getDecX().minus(ctr.getDecX()).multiply(coef));
				Decimal y = ctr.getDecY().add(v.getDecY().minus(ctr.getDecY()).multiply(coef));
				Decimal z = ctr.getDecZ().add(v.getDecZ().minus(ctr.getDecZ()).multiply(coef));
				Vecteur pt = new Vecteur (x, y, z);
				ret.points.add(pt);
			} else {
				ret.points.add(v);
			}
		}
		return ret;
	}

	public boolean isInside(Vecteur v) {
		if (this.size() == 0 ) return false;
		Segment seg = new Segment(v, this.points.get(0));
		Decimal angle = Decimal.ZERO;
		for (int i = 0; i< points.size(); i++) {
			Segment seg2 = new Segment(v, this.points.get(i));
			angle = angle.add(seg.getAngle(seg2));
		}
		if (angle.equals(new Decimal(360f))) return true;
		return false;
	}	
	
	public ArrayList<Segment> getSegments (){
		ArrayList<Segment> segs = new ArrayList<Segment>();
		int last = 0;//points.size()-1;
		for (int pos = 1; pos < points.size(); pos ++) {
			segs.add(new Segment(points.get(pos-1), points.get(pos)));
			last = pos;
		}
		if (points.size() > 0)
			segs.add(new Segment(points.get(last), points.get(0)));		
		return segs;
	}

	
	public int getIntersectionCount(Area other) {
		int nb = 0;
		ArrayList<Segment> mySegs = getSegments();
		for (Segment hisSeg : other.getSegments()) {
			for (Segment mySeg : mySegs) {
				Segment seg = hisSeg.intersection(mySeg);
				if (seg != null) {
					if ( (!seg.getA().equals(mySeg.getA())) && ((!seg.getB().equals(mySeg.getA())))) 
						nb ++;
				}
			}
		}
		return nb;
	}
	
	// Le premier point est le point la plus grande abcisse et la plus grande ordonnï¿½e 
	private Point getFirstPoint () {
		Point ret = new Point();
		// Point d'interconnexion entre le plan X = 0
		Plan3D pl = Plan3D.getPlan(Axis.XAxis, Decimal.ZERO);
		Decimal maxY = Decimal.MILLE.negate();
		int maxId = -1;
		Vecteur inter = null;
		int p = 1;
		for (Segment seg : getSegments()) {
			Vecteur v = pl.intersection(seg.getA(), seg.getB());
			if (v!= null) {
				if (v.getDecY().compareTo(maxY) > 0) {
					maxY = v.getDecY(); maxId = p;
					inter = v;
				}	
			}
			p++;
		}
		if (inter == null) {
			ret.inter = points.get(0);
			ret.pos = 0;
		} else {
			ret.inter = inter;
			ret.pos = maxId%points.size();
		}
		return ret;
	}

	// Eclate l'aire en liste de points equidistants Ã  partir du point le plus haut et le plus Ã  droite 
	public ArrayList<Vecteur> split(int nbPoints) {
		ArrayList<Vecteur> ret = new ArrayList<Vecteur>();
		// Calcule le point de dÃ©part
		Point pti = this.getFirstPoint();
		int firstPoint = pti.pos;
		// calcule le pÃ©rimetre
		Decimal p = getPerimetre();
		Decimal step = p.divide(new Decimal(nbPoints));
		// Ajoute le premier point
		Vecteur last = pti.inter;//points.get(firstPoint);
		System.out.println("First = "+last.toString()+ " - "+firstPoint);
		int nextId = (firstPoint + 1)%points.size();
		Vecteur next = points.get(nextId);
		ret.add(last); // Ajoute le premier point
		Decimal current = step; // position du pas en cours
		boolean finished = false;
		while (!finished) {
			// Rï¿½cupï¿½re le prochain pas
			Decimal len = last.decDistance(next);
			int comp = len.compareTo(current) ;
			if (comp > 0) { // Ecart est plus grand
				Droite3D dtr = new Droite3D(next.minus(last), last);
				// identifie le point
				Vecteur pt = dtr.getPoint(current);
				ret.add(pt);
				last = pt;
				current = step;
			}  
			if (comp == 0) { // Ecart est exact
				last = next;
				ret.add(next);
				nextId = (nextId + 1)%points.size();
				next = points.get(nextId);
				current = step; 
			}
			if (comp < 0) { // Ecart est plus petit
				last = next;
				nextId = (nextId + 1)%points.size();
				next = points.get(nextId);
				current = current.minus(len); 
			}
			if (ret.size() >= nbPoints)
				finished = true; 
		}
		return ret;
	}

	private Decimal getPerimetre() {
		Decimal p = Decimal.ZERO;
		Vecteur last = points.get(points.size()-1);
		for (Vecteur v : points) {
			p = p.add(last.decDistance(v));
			last = v;
		}
		return p;
	}
	
	public static final void main(String[] a) {
		Area a1 = new Area();
		a1.points.add(new Vecteur("0;0;1"));
		a1.points.add(new Vecteur("0;4;1"));
		a1.points.add(new Vecteur("3;4;1"));
		a1.points.add(new Vecteur("3;2;1"));
		a1.points.add(new Vecteur("3;0;1"));

		ArrayList<Vecteur> res = a1.split(10);
		if (res != null)
			System.out.println("Resultat 1 : "+res.toString());
		else System.out.println("Resultat 1 : pas d'intersection");
		
/**		// (-0.0309;-0.0249;0.001) ; (-0.0314;-0.0219;0.001) ; (-0.032;-0.0186;0.001) : Resized = (-0.0354;-0.0219;0.001)
		// Calcul de resize point avec trois points 
		Vecteur n = new Vecteur ("-0.0320;-0.0186;0.13");
		Vecteur m = new Vecteur ("-0.0314;-0.0219;0.13");
		Vecteur l = new Vecteur ("-0.0309;-0.0249;0.13");
		
		Vecteur pt = Area.getResizedPoint(l, m, n, new Decimal(-0.004d));
		if (pt != null) {
			System.out.println(l.toString()+" ; "+m.toString()+" ; "+n.toString()+" : Resized = "+pt.toString());
			System.out.println("Dist = "+pt.decDistance(m).toString());
		}

		n = new Vecteur ("-0.0326;-0.0151;0.13");
		m = new Vecteur ("-0.0320;-0.0186;0.13");
		l = new Vecteur ("-0.0314;-0.0219;0.13");
		
		pt = Area.getResizedPoint(l, m, n, new Decimal(-0.004d));
		if (pt != null) {
			System.out.println(l.toString()+" ; "+m.toString()+" ; "+n.toString()+" : Resized = "+pt.toString());
			System.out.println("Dist = "+pt.decDistance(m).toString());
		}
*/
	}

	/**
	 * Rtetourne le point de la forme le plus proche
	 * 
	 * @param pos
	 */
	
	public Vecteur getNearestPoint(Vecteur pos) {
		if (points.size() == 0) return null;
		Vecteur id = null;
		Decimal dist = Decimal.MILLE;
		
		for (int p = 0; p < points.size(); p++) {
			Vecteur pt = points.get(p);
			Decimal d = pos.distance(pt).abs();
			if (d.compareTo(dist) < 0) {
				id = pt;
				dist = d;
			}
		}
		return id;
	}

	/** Applique une transformation Ã  la surface **/
	public Area transform(Transformation trans) {
		Area ret = new Area();
		for (Vecteur pt : points) ret.points.add(trans.transforme(pt));
		return ret;
	}

	public int size() {
		return points.size();
	}

	public Vecteur lastPoint() {
		if (points.size() == 0) return null;
		return points.get(points.size() -1);
	}

	public Bounds3D getBounds() {
		Bounds3D bnds = new Bounds3D();
		bnds.add(this);
		return bnds;
	}

	/**
	 * Génération d'une Area avec les points dans l'autre sens 
	 * 
	 * @return
	 */
	public Area reverse() {
		Area ret = new Area();
		
		for (int p = points.size()-1; p >= 0; p--) {
			ret.points.add(points.get(p));
		}
		return ret;
	}

	public MapDeVecteurs getMap2(int axis, Decimal position, Decimal epaisseur, boolean b, int precision) {

		Decimal in = position.add(epaisseur.divide(Decimal.DEUX));
		Decimal out = position.minus(epaisseur.divide(Decimal.DEUX));
		Bounds3D bnds = this.getBounds();
		Decimal minY = bnds.getMin().getDecY().minus(Decimal.UN);
		Decimal maxY = bnds.getMax().getDecY().add(Decimal.UN);

		int otherAxis = Axis.XAxis;
		if (axis == Axis.XAxis) otherAxis = Axis.ZAxis;
		
		// définit le step d'avancement
		Decimal step = bnds.getMax().getDec(axis).minus(bnds.getMin().getDec(axis)).divide(new Decimal(precision));
		Decimal start = bnds.getMin().getDec(axis);
		
		Vecteur haut = bnds.getMax().setDec(Axis.YAxis, maxY);
		Vecteur bas = bnds.getMin().setDec(Axis.YAxis, minY);

		int fin = precision*2+2;
		MapDeVecteurs map = new MapDeVecteurs(fin, precision+1);

		for (int p = 0; p < precision+1; p++) {
			Decimal pos = start.add(step.multiply(new Decimal(p)));
			Segment seg = new Segment(haut.setDec(axis, pos), bas.setDec(axis, pos));
			Segment inter = CalculSurface.getIntersection(this, seg);
			if (inter != null) {
				Decimal miny = Decimal.min(inter.getA().getDecY(), inter.getB().getDecY());
				Decimal maxy = Decimal.max(inter.getA().getDecY(), inter.getB().getDecY());
				Decimal stp = maxy.minus(miny).divide(new Decimal(precision));
				for (int i = 0; i < fin; i++) {
					if (i < precision+1) {
						Vecteur yPos = new Vecteur().setDec(Axis.YAxis, miny.add(stp.multiply(new Decimal(i))));
						yPos = yPos.setDec(otherAxis, in);
						yPos = yPos.setDec(axis, pos);
						map.setPoint(i, p, yPos);
					} else {
						Vecteur yPos = new Vecteur().setDec(Axis.YAxis, miny.add(stp.multiply(new Decimal(fin - i -1))));
						yPos = yPos.setDec(otherAxis, out);
						yPos = yPos.setDec(axis, pos);
						map.setPoint(i, p, yPos);
					}
				}
			} 
		}

		return map;
	}

	
	//**  parcours les valeurs de la **/
	public MapDeVecteurs getMap(int axis, Decimal position, Decimal epaisseur, boolean b, int precision) {

		/** Recupère toutes les positions uniques **/
		ArrayList<Decimal> steps = new ArrayList<Decimal>();
		for (Vecteur v : points) {
			Decimal p = v.getDec(axis);
			if (!steps.contains(p)) steps.add(p);
		}
		/** trie les positions **/
		Decimal[] lst = new Decimal[steps.size()];
		steps.toArray(lst);
		Arrays.sort(lst);

		Decimal in = position.add(epaisseur.divide(Decimal.DEUX));
		Decimal out = position.minus(epaisseur.divide(Decimal.DEUX));
		Bounds3D bnds = this.getBounds();
		Decimal minY = bnds.getMin().getDecY().minus(Decimal.UN);
		Decimal maxY = bnds.getMax().getDecY().add(Decimal.UN);
		
		Vecteur haut = bnds.getMax().setDec(Axis.YAxis, maxY);
		Vecteur bas = bnds.getMin().setDec(Axis.YAxis, minY);

		int otherAxis = Axis.XAxis;
		if (axis == Axis.XAxis) otherAxis = Axis.ZAxis;
		
		int fin = precision*2+2;
		MapDeVecteurs map = new MapDeVecteurs(fin, lst.length);

		for (int p = 0; p < lst.length; p++) {
			Decimal pos = lst[p];
			Segment seg = new Segment(haut.setDec(axis, pos), bas.setDec(axis, pos));
			Segment inter = CalculSurface.getIntersection(this, seg);
			if (inter != null) {
				Decimal miny = Decimal.min(inter.getA().getDecY(), inter.getB().getDecY());
				Decimal maxy = Decimal.max(inter.getA().getDecY(), inter.getB().getDecY());
				Decimal stp = maxy.minus(miny).divide(new Decimal(precision));
				for (int i = 0; i < fin; i++) {
					if (i < precision+1) {
						Vecteur yPos = new Vecteur().setDec(Axis.YAxis, miny.add(stp.multiply(new Decimal(i))));
						yPos = yPos.setDec(otherAxis, in);
						yPos = yPos.setDec(axis, pos);
						map.setPoint(i, p, yPos);
					} else {
						Vecteur yPos = new Vecteur().setDec(Axis.YAxis, miny.add(stp.multiply(new Decimal(fin - i -1))));
						yPos = yPos.setDec(otherAxis, out);
						yPos = yPos.setDec(axis, pos);
						map.setPoint(i, p, yPos);
					}
				}
			} 
		}

		return map;
	}


}

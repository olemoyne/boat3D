package model;

import java.io.Serializable;
import java.util.ArrayList;

import model.calcul.CalculFormes;
import model.calcul.CalculPlan;
import model.calcul.CalculSurface;
import model.math.Axis;
import model.math.Bounds3D;
import model.math.Decimal;
import model.math.MapDeVecteurs;
import model.math.Plan3D;
import model.math.Segment;
import model.math.Vecteur;
import model.patch.CourbeParametree3D;
import model.patch.Patch;

public class Quille implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6072384585613619939L;

	/** Ã©paisseur de la quille **/
	public Decimal epaisseur;

	/** Profondeur de la quille  **/
	public Decimal profondeur;
	
	/** Nombre de points nécessaires pour dessiner la quille **/
	public int nbPoints;
	
	public Vecteur[] points;
	
	private Area surface;
	public Area surfaceZero;
	public Area surfaceEpaisseur;
	
	private int precision = 20;

	public MapDeVecteurs mapCalcul;

	private Area submerge;
	
	/** Creation de la quille **/
	public Quille() {
		epaisseur = new Decimal("0.02");
		profondeur = new Decimal("0.06");
		setup();
	}

	
	private void setup() {
		nbPoints = 4;
		points = new Vecteur[nbPoints];
		for (int p = 0; p < nbPoints; p ++) points[p] = new Vecteur();	
		precision = 20;
	}
	
	public String toString() {
		return "Quille";
	}


	public void recalcule(Patch patch, MapDeVecteurs mapAffichage, Structure structure, Decimal dessous, Decimal dessus) {
		if (points == null) setup();
		precision = 20;
		// Calcule les exteremmités
		Segment seg = CalculPlan.getExtermites(patch);
		if (seg == null) return;
		
		points[0] = seg.getA().setDec(Axis.XAxis, Decimal.ZERO);
		points[nbPoints-1] = seg.getB().setDec(Axis.XAxis, Decimal.ZERO);
		
		surface = getSurface();
				
		// Enlève la partie de la coque
		Plan3D pli = Plan3D.getPlan(Axis.XAxis, Decimal.ZERO);
		Area proj = mapAffichage.CreateProjection(pli);
		// Ajoute les éléments de la coque pour compléter le dessin
		surface = addSubMapPoints(surface, proj, dessus);

		pli = Plan3D.getPlan(Axis.XAxis, Decimal.ZERO);
		surfaceZero  = mapAffichage.CreateProjection(pli);

		pli = Plan3D.getPlan(Axis.XAxis, this.epaisseur.divide(Decimal.DEUX));
		surfaceEpaisseur  = mapAffichage.CreateProjection(pli);

		// Calcule la surface d'extrusion entre la projection et la surface
		submerge = new Area(surface);

		// Limite la surface au plafond
		Area projLimite = new Area();
		for (Vecteur pt : proj.points) {
			if (pt.getDecY().compareTo(dessus) > 0) { // point au dessus
				projLimite.points.add(pt.setDec(Axis.YAxis, dessus));
			} else {
				projLimite.points.add(pt);
			}
		}
		
		// Calcule la surface d'inclusion entre la projection et la surface
		Area inclusion = CalculFormes.getUnion(surface.transform(CalculPlan.rot), projLimite.transform(CalculPlan.rot), Decimal.ZERO);
		surface = inclusion.transform(CalculPlan.reverse);
				
		// Extrude les éléments de la structure
		surface = CalculPlan.extrudeStructure(surface, dessous, structure, epaisseur, profondeur);
		
		// Calcule la MAP affichage de la quille
		MapDeVecteurs map = new MapDeVecteurs(6, precision+1);
		Decimal in = epaisseur.divide(Decimal.DEUX);
		Decimal out = in.negate();
		Bounds3D bnds = submerge.getBounds();
		Decimal minY = bnds.getMin().getDecY().minus(Decimal.UN);
		Decimal maxY = bnds.getMax().getDecY().add(Decimal.UN);
		
		Decimal ZStep = bnds.getMax().getDecZ().minus(bnds.getMin().getDecZ()).divide(new Decimal(precision));
		for (int p = 0; p < precision+1; p++) {
			Decimal pos = ZStep.multiply(new Decimal(p));
			seg = new Segment(new Vecteur(Decimal.ZERO, maxY, pos), new Vecteur(Decimal.ZERO, minY, pos));
			Segment inter = CalculSurface.getIntersection(submerge, seg);
			if (inter != null) {
				Vecteur ctr = inter.getCenter();
				if (inter.getA().getDecY().compareTo(inter.getB().getDecY()) > 0) {
					map.setPoint(0, p, inter.getA().setDec(Axis.XAxis, in));
					map.setPoint(1, p, ctr.setDec(Axis.XAxis, in));
					map.setPoint(2, p, inter.getB().setDec(Axis.XAxis, in));
					map.setPoint(3, p, inter.getB().setDec(Axis.XAxis, out));
					map.setPoint(4, p, ctr.setDec(Axis.XAxis, out));
					map.setPoint(5, p, inter.getA().setDec(Axis.XAxis, out));
				} else {
					map.setPoint(0, p, inter.getB().setDec(Axis.XAxis, in));
					map.setPoint(1, p, ctr.setDec(Axis.XAxis, in));
					map.setPoint(2, p, inter.getA().setDec(Axis.XAxis, in));
					map.setPoint(3, p, inter.getA().setDec(Axis.XAxis, out));
					map.setPoint(4, p, ctr.setDec(Axis.XAxis, out));
					map.setPoint(5, p, inter.getB().setDec(Axis.XAxis, out));					
				}
			}
		}
		mapCalcul = map;
	}
	
	

	/**
	 * Ajoute les points du bas de la map d'affichage
	 * 
	 * @param surface2
	 * @param mapAffichage
	 * @return
	 */
	private Area addSubMapPoints(Area surf, Area subMap, Decimal dessus ) {
		Area lst = new Area();
		Bounds3D bnds = surf.getBounds().add(subMap);
		Decimal max = bnds.getMax().getDecY().add(Decimal.UN);
		Decimal min = bnds.getMin().getDecY().minus(Decimal.UN);
		// Détermine les verticaux relatifs à la surface
		for (Vecteur pt : surf.points) {
			Segment seg = new Segment(pt.setDec(Axis.YAxis, max), pt.setDec(Axis.YAxis, min));
			Segment inter =CalculSurface.getIntersection(subMap, seg);
			if (inter.getA() != null) {
				if (inter.getA().getDecY().compareTo(inter.getB().getDecY()) < 0) { // B plus haut
					lst.points.add(inter.getA());//.setDec(Axis.YAxis, dessus));
				} else {
					lst.points.add(inter.getB());//.setDec(Axis.YAxis, dessus));
				}
			}
		}
		Area ret = new Area(surf);
		// Ajoute les verticaux dans le sens inverses
		for (int p = lst.size()-1; p >= 0; p--) {
			ret.points.add(lst.points.get(p));
		}
		return ret;
	}


	/**
	 * Calcul de la surface de la quille
	 * 
	 * @return
	 */
	private Area getSurface() {
		Area ret = new Area();
		
		// Calcule les points de la quille
		int nbTirs = (nbPoints-1)/3;
		for (int p = 0; p < nbTirs; p ++) {			
			int pos = p*3;
			ArrayList<Vecteur> al = CourbeParametree3D.getCurvePoints(points[pos], points[pos+1], points[pos+2], points[pos+3], precision);
			// recopie les points du arrayList vers le tableau
			for (int i = 0; i < Math.min(al.size(), precision); i ++) {
				ret.points.add((Vecteur)al.get(i));
			}
		}
		return ret;
	}


	/** Calcule le nombre de points de la quille **/
	public boolean setNbPoints(int y) {
		if (y == nbPoints) return false;
		nbPoints = y;
		Vecteur[] inter = new Vecteur[nbPoints];
		int p = 0;
		for (; p < points.length-1; p ++) {
			if (p < nbPoints) {
				inter[p] = points[p];
			}
		}
		// Recopie les dernier point
		for (; p < nbPoints; p++) {
			inter[p] = points[points.length-1];
		}
		inter[nbPoints-1] = points[points.length-1];
		points = inter;
		return true;
	}


	/**
	 * Retourne la surface d'affichage
	 * @return
	 */
	public Area getAffichage() {
		return surface;
	}


	/**
	 * Retourne la surface de découpe
	 * @return
	 */
	public Area getSubmerge() {
		return this.submerge;
	}

	/**
	 * Positionne la valeur d'un point (l'absisse est positionnée à 0)
	 * @param x
	 * @param value
	 * @return
	 */
	public boolean setPoint(int x, Vecteur value) {
		if (x < nbPoints) {
			if (points[x] == value) return false;
			points[x] = value.setDec(Axis.XAxis, Decimal.ZERO);
			return true;
		}
		return false;
	}


	public Poids getPoids(Decimal dens) {
		Decimal srf = CalculSurface.getSurface(surface, Axis.XAxis);
		Vecteur ctr = CalculSurface.getCentre(surface, Axis.XAxis);
		Decimal frc =  srf.multiply(this.epaisseur).multiply(dens).multiply(Decimal.MILLE);
		Poids pds = new Poids("Quille", ctr, frc);
		return pds;
	}
	
}

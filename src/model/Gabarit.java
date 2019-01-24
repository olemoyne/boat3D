package model;

import java.io.Serializable;
import java.util.ArrayList;

import math.geom2d.polygon.Polygon2D;
import model.calcul.CalculArea;
import model.calcul.CalculFormes;
import model.calcul.CalculPlan;
import model.calcul.CalculSurface;
import model.calcul.CalculVolume;
import model.composants.Collision;
import model.composants.PatchVide;
import model.math.Axis;
import model.math.Bounds3D;
import model.math.Decimal;
import model.math.Plan3D;
import model.math.Vecteur;

/** Gabarit de construction **/
public class Gabarit implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Decimal position;
	
	/** Epaisseur du bois constituant le gabarit **/
	public Decimal epaisseur;
	/** Epaisseur de la bordure **/
	public Decimal bordure;
	
	/** Elements calculï¿½s **/
	public Decimal surface;
	public Decimal poids;
	
	private Area tranche;
	private Area contours;
	
	private int posGabarit;
	
	private Area encoche;
	
	private Vecteur pointBas;

	private ArrayList<Area> trous;
	
	public Gabarit (){
		position = new Decimal(1d);
		epaisseur = new Decimal(0.01d);
		bordure = new Decimal(0.0d);
		
		tranche = null;
		trous = new ArrayList<Area>();
	}
	

	public Plan3D getPlan(Decimal i) {
		Plan3D pl = new Plan3D(new Vecteur(Decimal.UN, Decimal.ZERO, position.add(i)), 
				new Vecteur(Decimal.ZERO, Decimal.ZERO, position.add(i)), new Vecteur(Decimal.ZERO, Decimal.UN, position.add(i)));
		return pl;
	}
	
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(position.toString());
		sb.append(" - ");
		sb.append(epaisseur.toString());
		return sb.toString();
	}
	
	
	/** 
	 * Recalcule les éléments du gabarits 
	 *   --> tranche : tranche de la MAP de calcul
	 *   --> surface : surface sans le bordé (sauf pour le tableau)
	 */
	public void recalcule(PatchVide cmp, ArrayList<Area> effacements, Decimal lowY) {
		getArea(cmp, effacements, lowY);
//		long haut = bds.getMax().getY() - cmp.epaisseurDeBardage.multiply(Vecteur.METER).longValue();

		setContours(CalculArea.reduceNormal(tranche, cmp.epaisseurDeBardage));
		
/**		// Positionne les éléments de la coupe avec la limite haute de la coupe
		Area a = new Area();
		for (Vecteur pt : contours.points) {
			if (pt.getY() >= haut) {
				a.points.add(pt.setDec(Axis.YAxis, highY));
			} else {
				a.points.add(pt);
			}
		}
		contours = a; **/
			
		if (posGabarit != 0) {
			if (cmp.structure.quille != null) {
				// Gestion de l'épaisseur de la quille
				encoche = CalculPlan.manageQuille(contours, cmp.structure.quille.epaisseur, cmp.structure.quille.profondeur, position);
			}
			
			Bounds3D bnds = contours.getBounds();
			
			if ((this.bordure != null) &&(!bordure.isZero())){
				Decimal high = lowY.add(cmp.structure.quille.profondeur.multiply(new Decimal(1.2d)));
				
				Area exclusion = new Area();
				exclusion.points.add(new Vecteur(bnds.getMin().getDecX().add(this.bordure), bnds.getMax().getDecY().add(Decimal.UN), Decimal.ZERO)); 
				exclusion.points.add(new Vecteur(bnds.getMin().getDecX().add(this.bordure), high, Decimal.ZERO)); 
				exclusion.points.add(new Vecteur(bnds.getMax().getDecX().minus(this.bordure), high, Decimal.ZERO)); 
				exclusion.points.add(new Vecteur(bnds.getMax().getDecX().minus(this.bordure), bnds.getMax().getDecY().add(Decimal.UN), Decimal.ZERO)); 
				Area retour = CalculFormes.getExtrusion(contours, exclusion, this.position.add(this.epaisseur.divide(Decimal.DEUX)));	

				contours = retour;
			}
			
			// Caclul des trous
			trous = getTrous(cmp.structure.poutres);
		}
		
	}
	
	
	/**
	 * renseigne la position de la position du gabarit sur le bateau
	 * 
	 * @param p
	 */
	public void setPos (int p) {
		this.posGabarit = p;
	}
	
	public int getPos () {
		return posGabarit;
	}
	
	

	/** 
	 * Retourne la surface (liste de points) corrrespondant au gabarit
	 * @param lowY 
	 * 
	 * @param bateau
	 * @param i 
	 * @param resize 
	 * @return
	 */
	public Area getArea(PatchVide cmp, ArrayList<Area> effacements, Decimal lowY) {
		Decimal i = position;
		Decimal ep = epaisseur;
		// Plan Z = position;
		Plan3D pl = Plan3D.getPlan(Axis.ZAxis, i);

		Area coupe = CalculPlan.getMaximalIntersection(cmp.mapAffichage, i, ep);
		// SUppression des zones de collisison
		if (cmp.collisions != null) {
			for (Collision coll : cmp.collisions) {
				Area extrude = coll.collision.intersectionHorizontaleZ(pl);
				if (effacements != null) effacements.add(extrude);
				Area diff = CalculFormes.getExtrusion(coupe, extrude, i);
				if (diff != null) coupe = diff;
			}
		}				
		tranche = coupe;
		pointBas = tranche.getBounds().getMin();
		return tranche;
	}

	/**
	 * Retourne l'intersection entre le gabarit et la poutre
	 * 
	 * @param ptr
	 * @return
	 */
	public Area getArea(Poutre ptr, Decimal i) {
		Area ret = new Area();
		// Parcours les deux faces d'abord

		Plan3D pl = new Plan3D(new Vecteur(Decimal.UN, Decimal.ZERO, position.add(i)), 
				new Vecteur(Decimal.ZERO, Decimal.ZERO, position), new Vecteur(Decimal.ZERO, Decimal.UN, position.add(i)));

		
		Vecteur start = ptr.getStart();
		Vecteur end = ptr.getEnd();
		
		long pStart = pl.donneCote(start);
		long pEnd = pl.donneCote(end);
		
		if (Long.signum(pStart)*Long.signum(pEnd) > 0) {
			// Sont du mÃªme cotÃ© --> Pas d'intersection
			return null;
		}
		// Ne sont pas du mÃªme cÃ´tÃ©
		
		Vecteur s = start;
		Vecteur e = new Vecteur (start.getDecX(), start.getDecY(), end.getDecZ());
		Vecteur c = pl.intersection(e,  s);
		if (c!= null) ret.points.add(c);

		s = new Vecteur (end.getDecX(), start.getDecY(), start.getDecZ());
		e = new Vecteur (end.getDecX(), start.getDecY(), end.getDecZ());
		c = pl.intersection(e,  s);
		if (c!= null) ret.points.add(c);


		s = new Vecteur (end.getDecX(), end.getDecY(), start.getDecZ());
		e = new Vecteur (end.getDecX(), end.getDecY(), end.getDecZ());
		c = pl.intersection(e,  s);
		if (c!= null) ret.points.add(c);


		s = new Vecteur (start.getDecX(), end.getDecY(), start.getDecZ());
		e = new Vecteur (start.getDecX(), end.getDecY(), end.getDecZ());
		c = pl.intersection(e,  s);
		if (c!= null) ret.points.add(c);
		
		return ret;
	}
	
	private ArrayList<Area> getTrous (ArrayList<Poutre> poutres) {
		ArrayList<Area> trous = new ArrayList<Area>();
		
		// Liste des trous
		for (Poutre ptr : poutres) {
			// DÃ©finit la section d'intersection entre la poutre et le gabarit
			Area a = getArea(ptr, Decimal.ZERO);
			if (a != null) trous.add(a);
		}
		return trous;
	}


	public Area getContours() {
		return contours;
	}


	public void setContours(Area contours) {
		this.contours = contours;
	}


	/**
	 * Calcule le poids du gabarit en fonction de sa surface
	 * 
	 * @param densiteBois
	 * @return
	 */
	public Poids getPoids(Decimal densiteBois) {
		ArrayList<Poids> pds = new ArrayList<Poids> ();
		if (this.contours.points.size() != 0) {
			Polygon2D pol = CalculSurface.getPoly(this.contours.points, Axis.ZAxis);
			Decimal vol = new Decimal(pol.area()).abs().multiply(epaisseur).multiply(densiteBois).multiply(Decimal.MILLE);
			Vecteur ctr = CalculSurface.getCentre(pol, position.add(epaisseur.divide(Decimal.DEUX)), Axis.ZAxis);
			pds.add(new Poids("Surf", ctr, vol));
			if (trous != null) {
				for (Area sub : this.trous) {
					pol = CalculSurface.getPoly(sub.points, Axis.ZAxis);
					vol = new Decimal(pol.area()).multiply(epaisseur).multiply(densiteBois).multiply(Decimal.MILLE);
					ctr = CalculSurface.getCentre(pol, position.add(epaisseur.divide(Decimal.DEUX)), Axis.ZAxis);
					pds.add(new Poids("Trou", ctr, vol.negate()));
				}
			}
			return CalculVolume.getCentreGravite("Gab", pds);
		}
		return null;
	}


	/** Liste des trous liés aux poutres **/
	public ArrayList<Area> getTrous() {
		return this.trous;
	}

	/** Encoche pour fixation sur la quille **/
	public Area getEncoche() {
		return this.encoche;
	}


	public Vecteur getPointBas() {
		return pointBas;
	}


	public Area getTranche() {
		return this.tranche;
	}


	public Area getIntersection(Plan3D pl) {
		Area a = CalculArea.intersection(getContours(), pl);
		if (a.size() != 0) {
			Area ret = new Area(a);
			for (int i = a.size()-1; i >= 0; i--) {
				ret.points.add(a.points.get(i).setDec(Axis.ZAxis, position.add(epaisseur)));				
			}
			return ret;
		} else return null;
		
	}
	
}

package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import model.composants.PatchVide;
import model.math.Bounds;
import model.math.Decimal;
import model.math.Vecteur;

public class Structure implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7416484307060964622L;
	
	/** Gabarits **/
	public ArrayList<Gabarit> gabarits;
	
	/** Poutres **/
	public ArrayList<Poutre> poutres;
	
	/** gestion de la quille **/
	public Quille quille;
	
	static public Vecteur sucre = new Vecteur ("0.03;0.009;0.02");
	
	public Structure () {

		gabarits = new ArrayList<Gabarit> ();
		poutres = new ArrayList<Poutre> ();
		
		quille = new Quille();  
	}
	
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (gabarits != null) for (Gabarit gab : gabarits) sb.append(gab.toString());
        if (poutres != null) for (Poutre poutre : poutres) sb.append(poutre.toString());
        return sb.toString();
    }

	public ArrayList<Poids> getAllPoids(PatchVide ptch, double densiteBois) {
		Decimal dens = new Decimal(densiteBois);
		ArrayList<Poids> ret = new ArrayList<Poids> ();
		for (Gabarit gab : gabarits){ // ajoute le poids des gabarits
			Poids pd = gab.getPoids(dens);
			if (pd != null) ret.add(pd);
		}
		for (Poutre ptr : poutres){ // ajoute le poids des poutres
			Decimal vol = ptr.epaisseur.multiply(ptr.hauteur).multiply(ptr.longueur).multiply(dens);
			Vecteur ctr = ptr.getStart().add(new Vecteur (Decimal.ZERO, Decimal.ZERO, ptr.longueur.divide(Decimal.DEUX)));
			ret.add(new Poids("Poutre", ctr, vol));
		}
		if (quille != null) {
			Poids pd = quille.getPoids(dens);
			if (pd != null) ret.add(pd);
		}
		
		return ret;
	}

	
	/**
	 * Recalcule les éléments de la structure 
	 * 
	 * @param patch
	 * @param mapAffichage
	 */
	public void recalcule(PatchVide patch) {
		if (patch == null) return;
		if (patch.patch.points.length == 0) return;

		// Calcule le point bas
		Decimal dessous = null;
		for (Gabarit g : gabarits) {
			if (dessous == null) dessous = g.getPointBas().getDecY();
			else dessous = Decimal.max(dessous, g.getPointBas().getDecY());	
		}

		Bounds b = Bounds.getBounds(patch.mapAffichage);
		Decimal dessus = b.getMax().getDecY().minus(patch.epaisseurDeBardage);
		
		
		// ordonne les gabarits 
		Decimal[] positions = new Decimal[gabarits.size()];
		int i = 0;
		for (Gabarit g : gabarits) {
			positions[i] = g.position;
			i ++;
		}
		Arrays.sort(positions);
		ArrayList<Gabarit> gabs = new ArrayList<Gabarit>(gabarits);
		int pos = 0;
		for (Decimal p : positions) {
			Gabarit find= null;
			i = 0;
			while ((i < gabs.size())&&(find == null)) {
				Gabarit g = gabs.get(i);
				if (g.position == p) find = g;
				i ++;
			}
			if (find != null) {
				find.setPos(pos);
				gabs.remove(find);
			}
			pos ++;
		}
		// Recalcule les données des gabarits
		for (Gabarit g : gabarits) {
			g.recalcule(patch, null, dessous);
		}
	
		// Calcul de la quille (map + surface)
		quille.recalcule(patch.patch, patch.mapAffichage, this, dessous, dessus);
	}

}

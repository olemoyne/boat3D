package view.view2D;

import java.awt.Color;

import model.Area;
import model.Gabarit;
import model.Structure;
import model.calcul.CalculArea;
import model.composants.PatchVide;
import model.math.Axis;
import model.math.Bounds;
import model.math.Bounds3D;
import model.math.Decimal;
import model.math.Plan3D;
import model.math.Vecteur;

public class PrintedBuildingZone extends PrintedPlan {

	
	Decimal zPosition;

	// Dimension des sucres de calage en position horizontale
		
	/** 
	 * Construit les Ã©lÃ©ments de la zone de construction 
	 * 
	 * **/
	public PrintedBuildingZone(PatchVide comp) {
		super("Building zone", Axis.YAxis);

		Bounds bnd = Bounds.getBounds(comp.mapAffichage);
		
		zPosition = bnd.getMax().getDecY().minus(comp.epaisseurDeBardage).minus(new Decimal("0.0001"));
		
		// Ajout la projection verticale de la MAP
		Plan3D pl = Plan3D.getPlan(Axis.YAxis, zPosition);
		Area proj = comp.mapAffichage.intersectionHorizontaleX(pl, Axis.XAxis);		
		this.bnds = proj.getBounds();

		addSurface(proj, Color.BLACK);

		// Ajoute les gabarits
		if (comp.structure.gabarits != null) {
			for (Gabarit g : comp.structure.gabarits) {
				
				proj = g.getIntersection(pl);
				if (proj != null) {
					Bounds3D b = proj.getBounds();
					bnds = bnds.add(b);
					addSurface(proj, Color.GREEN);
					// Positionne les surcres de maintient sur les gabarits
					// Récupère les extremités
					ajouteSucre(b.getMax().getDecX(), g.epaisseur, g.position, Axis.XAxis);
					ajouteSucre(b.getMin().getDecX(), g.epaisseur, g.position, Axis.XAxis);
				}
				
				
			}
		}

		// Ajoute la quille
		if (comp.structure.quille != null) {
			proj = null;
			Area a = CalculArea.intersection(comp.structure.quille.getAffichage(), pl);
			if (a.size() != 0) {
				Area ret = new Area();
				for (int i = 0; i < a.size(); i++) {
					ret.points.add(a.points.get(i).setDec(Axis.XAxis, comp.structure.quille.epaisseur.divide(Decimal.DEUX).negate()));				
				}
				for (int i = a.size()-1; i >= 0; i--) {
					ret.points.add(a.points.get(i).setDec(Axis.XAxis, comp.structure.quille.epaisseur.divide(Decimal.DEUX)));				
				}
				proj = ret;
			} 
			if (proj != null) {
				Bounds3D b = proj.getBounds();
				bnds = bnds.add(b);
				addSurface(proj, Color.BLUE);

				ajouteSucre(Decimal.ZERO, comp.structure.quille.epaisseur, b.getMax().getDecZ(), Axis.ZAxis);
				ajouteSucre(Decimal.ZERO, comp.structure.quille.epaisseur, b.getMin().getDecZ(), Axis.ZAxis);

			}
		}
	}


	/**
	 *  La zone de construction n'est pas imprimable en 3D
	 */
	public boolean is3DPrintable() {
		return false;
	}

	
	/** 
	 * ajoute un sucre de soutainement pour fixer la structure 
	 *  
	 * @param decX
	 * @param epaisseur
	 * @param position
	 * @param orientation
	 */
	private void ajouteSucre(Decimal decX, Decimal epaisseur, Decimal position, int orientation) {
		Area contours = new Area();
		Vecteur sucre = Structure.sucre;
		if (orientation == Axis.ZAxis) {
			sucre = new Vecteur(sucre.getDecZ(), sucre.getDecY(), sucre.getDecX());
		}
		contours.points.add(new Vecteur (decX.minus(sucre.getDecX().divide(Decimal.DEUX)), Decimal.ZERO, position.minus(sucre.getDecZ().divide(Decimal.DEUX))));
		contours.points.add(new Vecteur (decX.add(sucre.getDecX().divide(Decimal.DEUX)), Decimal.ZERO, position.minus(sucre.getDecZ().divide(Decimal.DEUX))));
		contours.points.add(new Vecteur (decX.add(sucre.getDecX().divide(Decimal.DEUX)), Decimal.ZERO, position.add(sucre.getDecZ().divide(Decimal.DEUX)).add(epaisseur)));
		contours.points.add(new Vecteur (decX.minus(sucre.getDecX().divide(Decimal.DEUX)), Decimal.ZERO, position.add(sucre.getDecZ().divide(Decimal.DEUX)).add(epaisseur)));
		bnds = bnds.add(contours.getBounds());
		addSurface(contours, Color.BLACK);
	}

	public Decimal getZPosition() {
		return zPosition;
	}


	public String toString() {
		return name;
	}

	public String getPlanType() {
		return "Building zone";
	}

	
	public String getStringDescr() {
		return "Building zone = "+position+" - "+zPosition;
	}
	
}

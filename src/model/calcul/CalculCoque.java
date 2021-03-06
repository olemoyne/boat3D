package model.calcul;

import java.util.ArrayList;

import math.geom2d.polygon.Polygon2D;
import model.Area;
import model.Cata;
import model.Poids;
import model.composants.Collision;
import model.composants.Composant;
import model.composants.PatchComposant;
import model.composants.PatchVide;
import model.math.Axis;
import model.math.Bounds;
import model.math.Decimal;
import model.math.MapDeVecteurs;
import model.math.Plan3D;
import model.math.Vecteur;
import model.math.transfo.Reflexion;
import model.math.transfo.Transformation;
import model.patch.Patch;

/**
 * Calcule les informations de coque
 * 
 * @author olemoyne
 *
 */
public class CalculCoque {

	/**
	 * Calcule la forme de la coque
	 * 
	 * @param patch
	 * @param precision
	 * @return
	 */
	public static MapDeVecteurs createCoque(Patch patch, int precision, boolean sym) {
		// Récupère la demiCoque
		MapDeVecteurs map = patch.getMap(precision, sym);
		if (!sym) {
			return map;
		}
		// Duplique la coque en créant un mirroir
		Reflexion ref = new Reflexion (Axis.XAxis, null);		
		MapDeVecteurs half = map.transforme(ref);
		
		MapDeVecteurs coque = map.addMap(half); 
		return coque;		
	}


	/** 
	 * Calcul des carènes de tous les composants 
	 * **/
	public static void calculeCarene (Cata bateau) {
		Plan3D surface = bateau.mer.getPlan();
		bateau.mer.carenes.clear();
        for (Composant cmp : bateau.composants) {
        	if (cmp.mapAffichage != null) {
	        	Transformation trs = cmp.situation.getTransformation(null);
	        	MapDeVecteurs mdv = cmp.mapAffichage.transforme(trs);
	        	MapDeVecteurs crn = mdv.truncate(surface);
	        	if (crn != null) bateau.mer.carenes.add(crn);
	        	
	        	if (cmp.getClass().isAssignableFrom(PatchVide.class)) {
	        		PatchVide pv = (PatchVide) cmp;
	        		if (pv.structure.quille.mapCalcul != null) {
			        	mdv = pv.structure.quille.mapCalcul.transforme(trs);
			        	crn = mdv.truncate(surface);
			        	if (crn != null) bateau.mer.carenes.add(crn);
	        		}
	        	}
        	} 
        }
	}
	
	public static Decimal calculeSurfaceCoque(MapDeVecteurs map) {
		Decimal surf = Decimal.ZERO;
		for (int x = 1; x < map.xSize(); x ++) {
			for (int y = 1; y < map.ySize(); y ++) {
				Vecteur A = map.getPoint(x-1,  y-1);
				Vecteur B= map.getPoint(x,  y-1);
				Vecteur C = map.getPoint(x,  y);
				Vecteur D = map.getPoint(x-1,  y);
				Decimal aire = Vecteur.calculeSurface(A, B, C).add(Vecteur.calculeSurface(A, C, D));
				surf = surf.add(aire);
			}
		}
		return surf;
	}
	
	/** Calcule la surface totale de la coque et multiplie par le coeficient 
	 * 
	 * @param bateau
	 * @return
	 */
	public static Poids calculePoidsCoque(MapDeVecteurs map, Decimal densiteSurfacique) {
		ArrayList<Poids> pds = new ArrayList<Poids>();
		if (map == null) return new Poids();
		// Trace tous les carrrés
		for (int x = 1; x < map.xSize(); x ++) {
			for (int y = 1; y < map.ySize(); y ++) {
				Vecteur A = map.getPoint(x-1,  y-1);
				Vecteur B= map.getPoint(x,  y-1);
				Vecteur C = map.getPoint(x,  y);
				Vecteur D = map.getPoint(x-1,  y);
				Decimal aire = Vecteur.calculeSurface(A, B, C).add(Vecteur.calculeSurface(A, C, D));
				Decimal kg = aire.multiply(densiteSurfacique).multiply(Decimal.MILLE);
				
				// Centre du poids
				Vecteur centre = A.add(B).add(C).add(D).multiply(new Decimal(0.25f));
				Poids p = new Poids ("", centre, kg);
				pds.add(p);
			}
		}
		return CalculVolume.getCentreGravite("Poids de la coque", pds);
	}
	
	
	/**
	 * Calcule la flottaison en fonction des poids et du volume de carène
	 * 
	 * @param cata
	 */
	public static void calculeFlottaison(Cata cata) {
		ArrayList<Poids> poussees = new ArrayList<Poids>(); 
		// Les carènes intègrent bien la position des composants et du bateau lui-même
        for (MapDeVecteurs cmp : cata.mer.carenes) {
        	poussees.add(CalculVolume.getPoussee(cmp));
        }
        cata.mer.pousseeArchimede = CalculVolume.getCentreGravite("Poussée", poussees);
        
        // Intégration du repositionnement du poids des composants
        Transformation mer = cata.mer.getTransformation();
		ArrayList<Poids> pds = new ArrayList<Poids>();
        for (Composant cmp : cata.composants) {
            Transformation trans = cmp.situation.getTransformation(mer);
        	pds.add(cmp.gravite.transforme(trans));
        }
		
		cata.mer.poidsTotal = CalculVolume.getCentreGravite("Poids total ", pds);
		// TODO : calcul si le bateau est stable
		
	}

	
	/** 
	 * Calcul du centre de dérive et de la surface anti-dérive
	 * 
	 * @param cata
	 */
	public static void calculeDerive(Cata cata) {
		// Création d'une projection
		cata.mer.surfaceAntiDerive = new ArrayList<Area>(); 
        for (MapDeVecteurs cmp : cata.mer.carenes) {
        	if (cmp.isValid())
        		cata.mer.surfaceAntiDerive.add(cmp.getProjection());
        }
        // Liste des surfaces liées aux quilles

        Decimal surf = Decimal.ZERO;
        for (Area a : cata.mer.surfaceAntiDerive) {
        	Polygon2D pol = CalculSurface.getPoly(a.points, Axis.XAxis);
        	surf = surf.add(new Decimal(pol.area()));
        }
		cata.mer.surfaceTotale = surf;

    	Vecteur v = CalculSurface.getCentreSurfaces(cata.mer.surfaceAntiDerive, Axis.XAxis);

       	cata.mer.centreAntiDerive = v;
	}

	
	/**
	 * Check if a map is collided with an other
	 */
	public static boolean checkCollisions(PatchComposant pc, Cata cata) {
		// Sanity check
		if (pc.mapAffichage == null) return false;
		
		// Working map : map d'affichage repositionnée
		Transformation trans = pc.situation.getTransformation(null);
		if (trans == null) return false;
		Transformation back = trans.getReverse(null);
		MapDeVecteurs myMap = pc.mapAffichage.transforme(trans);
		if (pc.collisions ==null) pc.collisions = new ArrayList<Collision>();
		pc.collisions.clear();

		Bounds bnds = Bounds.getBounds(myMap);
		for (Composant cmp : cata.composants) {
			// Ne retient que les patchs ou recopie de patch
			if ((cmp.isPatch())&&(myMap != null)&&(cmp != pc)) {
				//positionne la MAP dans le même repère que la MAP de calcul
				MapDeVecteurs hisMap = cmp.mapAffichage.transforme(cmp.situation.getTransformation(null)).transforme(back);
				MapDeVecteurs  coll =  CalculFormes.getCollision(myMap, bnds, hisMap);
				if (coll != null) {
					coll = coll.transforme(back);
					Collision c = new Collision();
					c.autre = cmp;
					c.collision = coll;
					pc.collisions.add(c);
				}
			}
		}
		return (pc.collisions.size() == 0);
	}

	/** 
	 * modifie la MAP d'affichage d'un composant pour extruder avec les composants imbriqués
	 * 
	 * @param pc
	 * @param cata
	 */
	public static void extrudeMap(PatchComposant pc, Cata cata) {
		// Sanity check
		if (pc.reduction == false) return;
		if (pc.mapAffichage == null) return;
		
		// Working map : map d'affichage repositionnée
		Transformation trans = pc.situation.getTransformation(null);
		Transformation back = trans.getReverse(null);
		MapDeVecteurs myMap = pc.mapAffichage.transforme(trans);
		
		
		int nbPoints = 80;
		MapDeVecteurs mapX = Patch.getMapDecoupe(myMap, nbPoints, Axis.XAxis);
//		MapDeVecteurs mapX = Patch.getMapDecoupe(myMap, nbPoints, Axis.XAxis);

		pc.mapAffichage = mapX.transforme(back);
/**		// Calcul les intersections
		Bounds bnds = Bounds.getBounds(myMap);
		for (Composant cmp : cata.composants) {
			// Ne retient que les patchs ou recopie de patch
			if ((cmp.isPatch())&&(myMap != null)&&(cmp != pc)) {
				//positionne la MAP dans le même repère que la MAP de calcul
				MapDeVecteurs hisMap = cmp.mapAffichage.transforme(cmp.situation.getTransformation(null)).transforme(back);
				if (cmp.getMapNonReduite() == null) {
					myMap = CalculFormes.getMapExtrusion(myMap, bnds, hisMap);
					if (myMap == null)
						System.err.println("Collision with forme "+pc.nom+" "+cmp.nom);
				}
			}
		}
		if (myMap != null) {
			pc.mapAffichage = myMap.transforme(back);
		} else {
			System.err.println("Impossible de réduire la forme "+pc.nom);
		}
			**/
	}


}

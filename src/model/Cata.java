package model;

import java.io.Serializable;
import java.util.ArrayList;

import model.calcul.CalculCoque;
import model.composants.Composant;
import model.composants.PatchComposant;
import model.composants.PatchVide;


/**
 * Modelisation simple d'un catamaran 
 *   * Design
 *   --> 16 points pour d�finir la forme de la coque
 *   --> 1 point de d�part pour le roof
 *   --> une �chelle de pr�cision pour les calculs / affichage
 *   --> maillage de points pour afficher la coque
 *   --> maillage de points pour les calculs
 *   --> liste de poids associ�s
 *   --> Position (tangage, gite et pilonnement )
 * 
 *   * Construction 
 *   --> Liste de gabarits ( )
 *   --> 4 poutres et leur position
 *   --> Dérive 
 *   
 * @author olemoyne
 *
 */
public class Cata implements Serializable{
   /**
	 * 
	 */
	private static final long serialVersionUID = -5490545959071227392L;

	public ArrayList<Composant> composants;
	
	public Habitacle habitacle;
	
	/** Flottaison **/
	public Flottaison mer;
	
	public String filename;
	
	/**
	 * Constructeur --> intialisation des listes
	 */
	public Cata (String f) {
		
		filename = f;
		
		mer = new Flottaison();

		composants = new ArrayList<Composant>(); 
		composants.add(PatchVide.getDefaultPatch(this));
		
		habitacle = new Habitacle();
		recalculeAll();
	}
	


	/**
	 * Changement de patch sans resizing
	 */
	public void recalculeAll() {
		long start = System.currentTimeMillis();
		System.out.println(start+" - Starting recalcule");
		
		// Force le reclacul sur tous les composants
		for (Composant cmp : composants) {
			cmp.boat = this;
			cmp.recalcule();
		}
		// Recalcul des réductions si nécessaire 
		for (Composant cmp : composants) {
			if (cmp.isPatch()) {
				try {
					PatchComposant pc = (PatchComposant) cmp;
					pc.collision = CalculCoque.checkCollisions(pc, this);
					pc.calculeElements();
				} catch (ClassCastException e) {
				}
			}
		}
		
		this.recalcule();
		long end = System.currentTimeMillis();
		System.out.println(end+" - Ending recalcule  - "+((end-start)/1000.0));
	}
	
	
	/**
	 * Changement de patch sans resizing
	 */
	public void recalcule() {
		CalculCoque.calculeCarene(this);
		CalculCoque.calculeFlottaison(this);
		CalculCoque.calculeDerive(this);
		
	}

	
    public String toString() {
        StringBuilder sb = new StringBuilder("Cata = ");
		for (Composant cmp : composants) {
			sb.append(cmp.toString());
	        sb.append(" ");
		}
        if (mer != null) sb.append(mer.toString());
        return sb.toString();
    }	
}

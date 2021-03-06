package appli.arbre.nodes;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import appli.arbre.DesignTreeRenderer;
import appli.arbre.TreeNodeProperty;
import appli.values.updater.ObjectUpdater;
import model.Position;
import model.composants.Collision;
import model.composants.PatchComposant;
import model.math.Decimal;
import model.math.Vecteur;
import model.patch.Patch;
import view.scene.PrintableScene;
import view.scene.PrintedMap;
import view.scene.PrintedPoint;

public class PatchPropertyTreeNode extends DesignTreeNode{

	// Données à modifier
	public Patch data;

	/**
	 * 
	 */
	private static final long serialVersionUID = 3562048409166723938L;

	public PatchPropertyTreeNode(DefaultMutableTreeNode up, Patch d) {
		super(up, "Patch");
		
		data = d;
	}
	
	
	@Override
	public Icon getImage() {
		return DesignTreeRenderer.patchImg;
	}


	/**
	 * Gestion du patch de données
	 */
	public ArrayList<TreeNodeProperty> getProperties() {
		
		ArrayList<TreeNodeProperty> ret = super.getProperties();
		ret.add(new TreeNodeProperty ("Taille X", data.x, true, ObjectUpdater.INTEGER) );
		ret.add(new TreeNodeProperty ("Taille Y", data.y, true, ObjectUpdater.INTEGER) );
		
		for (int x = 0; x < data.x ; x++) {
			for (int y = 0; y < data.y ; y++) {
				StringBuilder sb = new StringBuilder("Point (");
				sb.append(x);
				sb.append(", ");
				sb.append(y);
				sb.append(")");
			
				ret.add(new TreeNodeProperty (sb.toString(), data.points[x][y], true, ObjectUpdater.VECTEUR) );
			}
		}
			
		return ret;
	}

	
	public void updateValue (String fld, Object value) {
		if (fld.equals("Taille X")) {
			int x  = (int) Math.round(((Decimal)value).doubleValue());
			data.recalcule(x, data.y);
		}

		if (fld.equals("Taille Y")) {
			int y  = (int) Math.round(((Decimal)value).doubleValue());
			data.recalcule(data.x, y);
		}
		
		// Retrouve la position du point 
		if (fld.startsWith("Point (")) {
			int pos = fld.indexOf(", ");
			String xstr = fld.substring(7, pos);
			int x = Integer.parseInt(xstr);
			int pos2 = fld.indexOf(")");
			String ystr = fld.substring(pos+2, pos2);
			int y = Integer.parseInt(ystr);
			
			data.points[x][y] = (Vecteur)value;
		}
	}

	/** 
	 * Permet d'afficher la scene correspondant à la vue définie
	 * 
	 */
	public PrintableScene getScene () {
		PatchTreeNode father = (PatchTreeNode)this.parent;
		PatchComposant cmp = (PatchComposant)father.composant;
		PrintableScene ret = super.getScene(); 

		// Affiche la coque en gris
		ret.add(new PrintedMap (cmp.mapAffichage, "Coque", true, Color.darkGray, new Position()));					

		// Affiche les points en jaune
		for (int y = 0; y < data.y; y++) 
			for (int x = 0; x < data.x; x++) 
				ret.add(new PrintedPoint(data.points[x][y], "", Color.yellow));

		if (cmp.collisions != null) {
			// Affiche les collisions en rouge
			for (Collision col : cmp.collisions) {
				ret.add(new PrintedMap (col.collision, col.autre.nom, false, Color.red, new Position()));					
			}
		}

		return ret;
	}
	
	public int getNodeLevel() {
		return LEVEL_COMPOSANT_DESIGN;
	}


}

package appli.arbre.nodes;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.Icon;

import view.scene.PrintableScene;
import view.scene.PrintedArea;
import view.scene.PrintedMap;
import view.scene.PrintedPoint;
import appli.arbre.DesignTreeRenderer;
import appli.arbre.TreeNodeProperty;
import appli.values.updater.ObjectUpdater;
import model.Position;
import model.Quille;
import model.composants.PatchComposant;
import model.composants.PatchVide;
import model.math.Decimal;
import model.math.Vecteur;
import model.math.transfo.Translation;

public class QuilleTreeNode extends DesignTreeNode {

	PatchVide comp;
	/**
	 * 
	 */
	private static final long serialVersionUID = 4852166551916601414L;

	public QuilleTreeNode(DesignTreeNode up, PatchVide cmp) {
		super(up, "Quille");
		comp = cmp;
	}
	
	@Override
	public Icon getImage() {
		return DesignTreeRenderer.quilleImg;
	}

	/**
	 * Gestion du patch de donn√©es
	 */
	public ArrayList<TreeNodeProperty> getProperties() {
		
		ArrayList<TreeNodeProperty> ret = super.getProperties();

		if (comp != null) {
			if (comp.structure.quille == null) comp.structure.quille = new Quille(); 
			ret.add(new TreeNodeProperty ("Epaisseur de quille", comp.structure.quille.epaisseur, false, ObjectUpdater.DECIMAL) );
			ret.add(new TreeNodeProperty ("Profondeur de quille", comp.structure.quille.profondeur, false, ObjectUpdater.DECIMAL) );
			
			ret.add(new TreeNodeProperty ("Nb points", comp.structure.quille.nbPoints, true, ObjectUpdater.INTEGER) );
			
			int nb = comp.structure.quille.nbPoints;
			for (int x = 0; x < nb; x++) {
				StringBuilder sb = new StringBuilder("Point (");
				sb.append(x);
				sb.append(")");
				
				boolean edit = true;
				if ((x == 0) || (x == nb-1)) edit = false;
				
				ret.add(new TreeNodeProperty (sb.toString(), comp.structure.quille.points[x], edit, ObjectUpdater.VECTEUR) );
			}
		}	
		return ret;
	}

	/**
	 * Mise ‡ jour de la valeur
	 */
	public void updateValue (String fld, Object value) {
		super.updateValue(fld, value);

		if (fld.equals("Epaisseur de quille")) comp.structure.quille.epaisseur = (Decimal)value;
		if (fld.equals("Profondeur de quille")) comp.structure.quille.profondeur = (Decimal)value;
		
		if (fld.equals("Nb points")) {
			int y  = (int) Math.round(((Decimal)value).doubleValue());
			if (y >= 2) {
				boolean upd = comp.structure.quille.setNbPoints(y);
				if (upd) comp.structure.recalcule(comp);
			}
		}
		
		// Retrouve la position du point 
		if (fld.startsWith("Point (")) {
			int pos = fld.indexOf(")");
			String xstr = fld.substring(7, pos);
			int x = Integer.parseInt(xstr);
			
			boolean upd = comp.structure.quille.setPoint(x, (Vecteur)value);
			if (upd) comp.structure.recalcule(comp);
		}


	}

	/** 
	 * Permet d'afficher la scene correspondant √† la vue d√©finie
	 * 
	 */
	public PrintableScene getScene () {
		PatchTreeNode father = (PatchTreeNode)this.parent.getParent();
		PatchComposant cmp = (PatchComposant)father.composant;
		PrintableScene ret = super.getScene(); 

		// Affiche la coque en quadrillage
		ret.add(new PrintedMap (cmp.mapAffichage, "Coque", true, Color.darkGray, new Position()));	
		
		// Affiche la dÈrive 
		if (comp.structure.quille.getAffichage() != null) {
			Translation tr = new Translation (new Vecteur(comp.structure.quille.epaisseur.divide(Decimal.DEUX), Decimal.ZERO, Decimal.ZERO), null);
			ret.add(new PrintedArea(comp.structure.quille.getSubmerge().transform(tr), "QuilleD", true, true, Color.red, new Position()));	
	
			tr = new Translation (new Vecteur(comp.structure.quille.epaisseur.divide(Decimal.DEUX).negate(), Decimal.ZERO, Decimal.ZERO), null);
			ret.add(new PrintedArea(comp.structure.quille.getSubmerge().transform(tr).reverse(), "QuilleG", true, true, Color.red, new Position()));	
		}
		
		ret.add(new PrintedMap (comp.structure.quille.mapCalcul, "Quille", true, Color.darkGray, new Position()));	
		
	
		// Affiche les points en jaune
		for (int y = 0; y < comp.structure.quille.nbPoints; y++) 
				ret.add(new PrintedPoint(comp.structure.quille.points[y], "", Color.yellow));

		return ret;
	}
	
	public boolean requireButtons() {
		return false;
	}

}

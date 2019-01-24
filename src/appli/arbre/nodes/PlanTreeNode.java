package appli.arbre.nodes;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

import appli.arbre.DesignTreeRenderer;
import model.calcul.CalculPlan;
import model.composants.PatchComposant;
import model.composants.PatchVide;
import model.math.Plan3D;
import view.scene.PrintableScene;

public class PlanTreeNode extends DesignTreeNode {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5121177429213459976L;

	public PlanTreeNode(DefaultMutableTreeNode up) {
		super(up, "Plans");
	}

	@Override
	public Icon getImage() {
		return DesignTreeRenderer.planImg;
	}

	
	/** 
	 * Permet d'afficher la scene correspondant à la vue définie
	 * 
	 */
	public PrintableScene getScene () {
		PatchTreeNode father = (PatchTreeNode)this.parent.getParent();
		PatchComposant cmp = (PatchComposant)father.composant;
		PrintableScene ret = CalculPlan.calculePlans((PatchVide)cmp, new Plan3D());
		return ret;
	}
	

}

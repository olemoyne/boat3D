package appli.arbre;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import appli.arbre.nodes.DesignTreeNode;

public class DesignTreeRenderer extends DefaultTreeCellRenderer {
 
    /**
	 * 
	 */
	private static final long serialVersionUID = 6733855203284198025L;
	
	/** Image de bateau **/
	private static ImageIcon boatImg = new ImageIcon("imgs/boat.png"); 
	public static ImageIcon anchorImg = new ImageIcon("imgs/anchor_.png"); 
	public static ImageIcon designImg = new ImageIcon("imgs/design.png"); 
	public static ImageIcon dynaImg = new ImageIcon("imgs/dynamique.png"); 
	public static ImageIcon planImg = new ImageIcon("imgs/plans.png"); 
	public static ImageIcon poidsImg = new ImageIcon("imgs/poids-16.png"); 
	public static ImageIcon pousseeImg = new ImageIcon("imgs/poussee.png"); 
	public static ImageIcon structureImg = new ImageIcon("imgs/structure.png"); 
	public static ImageIcon voileImg = new ImageIcon("imgs/voile.png"); 
	public static ImageIcon aeodynImg = new ImageIcon("imgs/aerodyn.png"); 
	public static ImageIcon boisImg = new ImageIcon("imgs/bois.png"); 
	public static ImageIcon gabaritImg = new ImageIcon("imgs/cintre.png"); 
	public static ImageIcon collisionImg = new ImageIcon("imgs/collision.png"); 
	public static ImageIcon composantImg = new ImageIcon("imgs/composant.png"); 
	public static ImageIcon deriveImg = new ImageIcon("imgs/derive.png"); 
	public static ImageIcon habitatImg = new ImageIcon("imgs/habitat.png"); 
	public static ImageIcon patchImg = new ImageIcon("imgs/patch.png"); 
	public static ImageIcon quilleImg = new ImageIcon("imgs/quille.png"); 

	public DesignTreeRenderer() {
    }
 
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    	super.getTreeCellRendererComponent(
                            tree, value, sel,
                            expanded, leaf, row,
                            hasFocus);
    	// Racine de l'arbre
    	if (row == 0) {
    		this.setIcon(boatImg);
    	} else { // Autre lignes
    		DesignTreeNode dtn = (DesignTreeNode) value;
    		Icon img = dtn.getImage();
    		if (img != null) this.setIcon(img);
    	}
    	
        return this;
    }
 
}


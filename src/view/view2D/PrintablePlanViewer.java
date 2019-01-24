package view.view2D;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

import model.math.Vecteur;
import view.scene.PrintableObject;

public class PrintablePlanViewer extends JPanel implements MouseMotionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3990462677794567308L;

	/**
	 * Scene de visualisation d'un agbarit  
	 *    -> Dessin du gabarit en fonction des donn�es 
	 *    -> Dessin de la mer
	 *    -> Dessin des trous li�s aux poutres
	 *    
	 *    --> Possibilit� de positionner la souris sur un des �l�ments 
	 */
	
	protected PrintedPlan toPrint;
	protected Vecteur primaryPoint;
	protected Vecteur secondaryPoint;

	
	protected PrintableInformation logManager;
	
	
	/**
	 * Creation de la vue 2D
	 */
	public PrintablePlanViewer (PrintableInformation position, PrintableDetailsViewer list) {
		super();
		logManager = position;
		addMouseMotionListener(this);
		addMouseWheelListener(list);
		addKeyListener(list);
	}

	
	
	/** 
	 * Visualisation du gabarit
	 * 
	 */
	public void paintComponent(Graphics gr) {
		super.paintComponent(gr);
		if (toPrint == null) return;
		// Dessine le gabarit 
		
		toPrint.drawObject(gr, true, true);
		if (primaryPoint != null) {
			gr.setColor(Color.RED);
			toPrint.drawPoint(gr, primaryPoint);
		}
		if (secondaryPoint != null) {
			gr.setColor(Color.BLACK);
			toPrint.drawPoint(gr, secondaryPoint);
		}
	}
	

	/**
	 * Affiche le gabarit 
	 * 
	 * @param gab
	 * @param mer
	 * @param poutres
	 */
	public void setObject(PrintableObject  gab) {
		// Calcule le gabarit
		toPrint = (PrintedPlan)gab;

		if (toPrint != null) {
			Rectangle rec = this.getBounds();
	
			toPrint.setSize(new Rectangle(10, 10, rec.width-20, rec.height-20));
			logManager.mouseData = toPrint.getMouseData(null);
		}
	}
	

	
	public void setMousePosition(Point2D pt) {
		
		if (logManager != null) {
			if (toPrint != null) {
				logManager.mouseData = toPrint.getMouseData(pt);
				
				primaryPoint= toPrint.getPrimaryPosition(pt);
				secondaryPoint= toPrint.getSecondaryPosition(pt);
				
				//affiche les positions des 
			}
			
			this.logManager.show();
		}
		
		// Affiche les points
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		
	}


	@Override
	public void mouseMoved(MouseEvent e) {
		Point2D pt= e.getPoint();
		this.setMousePosition(pt);
	}



	public String getData(PrintableObject gab) {
		return ((PrintedPlan)gab).getMouseData(null);
	}

	public String getMousePosition(PrintableObject gab) {
		return ((PrintedPlan)gab).getMouseData(this.getMousePosition());
	}


}

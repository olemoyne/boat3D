package view.param;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;

import model.math.Axis;
import model.math.Bounds3D;
import model.math.Decimal;
import model.math.Vecteur;

import java.text.SimpleDateFormat;

import view.view2D.PrintedPlan;

public class ImageFileCreator {
	
	public static Decimal inch = new Decimal("0.393701");
	private static Vecteur entete = new Vecteur("0.06;0.0160;0");
	private static Vecteur entetePos = new Vecteur("0.003;0.003;0");

	public static void createFile(PrintedPlan pg, int pos, PrintingParameters params, String catafilename) throws IOException {
		/** Définition de la taille du dessin --> 20.32x27.94 cm => 8x11 ***/
		Decimal txInch = Decimal.CENT.multiply(inch);
				
		Bounds3D bnds = pg.getBounds();
		long planSizeX = Math.round(params.pixByInch*bnds.getSize(Axis.XAxis).doubleValue()*txInch.doubleValue())+PrintedPlan.xMargin*2;
		long planSizeY = Math.round(params.pixByInch*bnds.getSize(Axis.YAxis).doubleValue()*txInch.doubleValue())+PrintedPlan.yMargin*2;
		
		int imgSizeX = (int)planSizeX;
		int imgSizeY = (int)planSizeY + (int)entete.getY() + (int)entetePos.getY();
		
		
		BufferedImage image = new BufferedImage(imgSizeX, imgSizeY, BufferedImage.TYPE_INT_RGB); 
		Graphics2D gr = image.createGraphics();
		// Affiche le fond
		gr.setColor(Color.WHITE);
		gr.fillRect(0, 0, imgSizeX, imgSizeY);
		
		
		double txAff = 20.0/17.0;
		
		double cmx = (imgSizeX/(double)params.pixByInch)/txInch.doubleValue()*txAff;
		double cmy = (imgSizeY/(double)params.pixByInch)/txInch.doubleValue()*txAff;
		
		/** Affiche les données d'information dans un encart 
		 *   --> Fichier de catamaran
		 *   --> Numéro de tranche, position et épaisseur 
		 *   --> Date d'édition **/
		gr.setColor(Color.BLACK);
		gr.drawRect((int)entetePos.getX(), (int)entetePos.getY(), (int)entete.getX(), (int)entete.getY());
	    gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	    gr.setFont(new Font (Font.SANS_SERIF, Font.PLAIN, 20));
		gr.drawString("File = "+catafilename, (int)(entetePos.getY()+10), (int)(entetePos.getX()+40));
		gr.drawString(pg.getStringDescr(), (int)(entetePos.getY()+10), (int)(entetePos.getX()+70));
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"); 
		gr.drawString(sdf.format(new Date()), (int)entetePos.getY()+10, (int)(entetePos.getX()+110));
		gr.drawString("Dim = "+cmx+" "+cmy, (int)entetePos.getY()+10, (int)(entetePos.getX()+130));
		
		// Affichage de la forme
		Rectangle rec = new Rectangle (0, (int)entetePos.getY()+(int)entete.getY(), (int)planSizeX, (int)planSizeY);

		pg.setSize(rec,params.pixByInch);
		pg.drawObject(gr, true, false);

		sdf = new SimpleDateFormat("yyyyMMddHHmmss"); 
		String dte = sdf.format(new Date());

		// --> Supprime l'extention
		String cataFile = catafilename;
		int p = catafilename.lastIndexOf('.');
		if (p != -1) {
			cataFile = catafilename.substring(0, p);
		}
		
		// Stockage d'image dans le fichier
		File nomfichier = new File(params.fileName + File.separator+cataFile+"_"+pos+"."+dte+".bmp");// ou jpg 
		ImageIO.write(image, "BMP", nomfichier);//ou JPG 
	}
	
	
}

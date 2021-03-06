package view.param;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import model.Area;
import model.math.Axis;
import model.math.Decimal;
import model.math.Vecteur;
import model.math.transfo.Rotation;

import java.text.SimpleDateFormat;

import view.scene.PrintableObject;
import view.view2D.PrintedPlan;

public class ScriptFileCreator {

	public static void createFile(PrintedPlan pg, int pos, PrintingParameters params, String catafilename) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss"); 
		String dte = sdf.format(new Date());

		// --> Supprime l'extention
		String cataFile = catafilename;
		int p = catafilename.lastIndexOf('.');
		if (p != -1) {
			cataFile = catafilename.substring(0, p);
		}
		
		File nomFichier = new File(params.fileName + File.separator+cataFile+"_"+pos+"."+dte+".scr");// ou jpg 
		FileWriter write = new FileWriter(nomFichier); 
		params.fileName = nomFichier.getParent();
		
		write.write("#File = "+catafilename+"\n");
		write.write("#Tranche = "+pos+" - "+pg.position+" - "+pg.getEpaisseur()+"\n");
		sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"+"\n"); 
		write.write("#Date ="+sdf.format(new Date()));

		//Affiche les instructions
		write.write("import FreeCAD\nimport Sketcher\n\n\n");
		
		write.write("Gui.activateWorkbench(\"SketcherWorkbench\")"+"\n");

		Rotation rot = new Rotation (Axis.YAxis, new Decimal("-90"), null);
		Area toPrint = null;
		if (pg.getAxis() == Axis.ZAxis) {
			toPrint = pg.getPrintable();
		} else {
			toPrint = pg.getPrintable().transform(rot);
		}
		
		String forme = createAreaScript(toPrint, "Forme", null, pg.getAxis());  //Decimal.ZERO);
		
//		--> TODO : FreeCAD.getDocument("Sans_nom").getObject("Tranche_10").Placement = App.Placement(App.Vector(0,0,400),App.Rotation(App.Vector(0,1,0),90))
		
		write.write(forme);

		if (pg.getTrous() != null) {
			int i = 0;
			for (Area tr : pg.getTrous()) {
				
				toPrint = null;
				if (pg.getAxis() == Axis.ZAxis) {
					toPrint = tr;
				} else {
					toPrint = tr.transform(rot);
				}
				String trou = createAreaScript(toPrint, "Trou_"+i, null, pg.getAxis());
				write.write(trou);
				i++;
			}
		}

		write.close();
	}


	
	private static String getGeometryLine(Vecteur v1, Vecteur v2, Decimal Zpos, String name) {
		StringBuilder sb = new StringBuilder("App.ActiveDocument.");
		sb.append(name);
		sb.append(".addGeometry(Part.Line(App.Vector(") ;
		sb.append(v1.getDecX().multiply(Decimal.MILLE)) ;
		sb.append(", ") ;
		sb.append(v1.getDecY().multiply(Decimal.MILLE)) ;
		sb.append(", ") ;
		if (Zpos != null) sb.append(Zpos.multiply(Decimal.MILLE)) ;
		else sb.append(v1.getDecZ().multiply(Decimal.MILLE)) ;
		sb.append("),App.Vector(") ;
		sb.append(v2.getDecX().multiply(Decimal.MILLE)) ;
		sb.append(", ") ;
		sb.append(v2.getDecY().multiply(Decimal.MILLE)) ;
		sb.append(", ") ;
		if (Zpos != null) sb.append(Zpos.multiply(Decimal.MILLE)) ;
		else sb.append(v2.getDecZ().multiply(Decimal.MILLE)) ;
		sb.append(")),False)\n");
		
		return sb.toString();
	}
	

	private static String createAreaScript (Area area, String name, Decimal Zpos, int direction) {
		StringBuilder write = new StringBuilder();

		if ((area != null ) && (area.points != null) && (area.points.size()!= 0)) {
			write.append("App.activeDocument().addObject('Sketcher::SketchObject','"+name+"')"+"\n");
		
			write.append("App.activeDocument()."+name+".Placement = App.Placement(App.Vector(0.000000,0.000000,0.000000),App.Rotation(0.000000,0.000000,0.000000,1.000000))"+"\n");
			write.append("Gui.activeDocument().setEdit('"+name+"')\n");

			int pos = 1;
			for (int i = 1; i < area.points.size(); i ++) {
				if (!area.points.get(i-1).equals(area.points.get(i))) {
					write.append(getGeometryLine(area.points.get(i-1), area.points.get(i), Zpos, name));
					if (i > 1) {
						write.append("App.ActiveDocument."+name+".addConstraint(Sketcher.Constraint('Coincident',"+(pos-2)+",2,"+(pos-1)+",1))"+"\n"); 
					}
					pos ++;
				} else { System.out.println("Doulon ! "+i+" "+name); }
			}
			int i = area.points.size();
			write.append(getGeometryLine(area.points.get(i-1) ,area.points.get(0), Zpos, name));
	
			write.append("Gui.activeDocument().resetEdit()\n");
			write.append("App.activeDocument().recompute()\n");
		}
		return write.toString();

	}
	
	
	
	public static String extrudArea (String forme, String object, Decimal size, Decimal offset) {
		StringBuilder write = new StringBuilder();

		/** Activation du WorkBench qui permet de gérer l'extrusion **/
		write.append("Gui.activateWorkbench(\"PartWorkbench\")\n");
		write.append("FreeCAD.activeDocument().addObject(\"Part::Extrusion\",\""+object+"\")\n");
		write.append("FreeCAD.activeDocument()."+object+".Base = FreeCAD.activeDocument()."+forme+"\n");
		write.append("FreeCAD.activeDocument()."+object+".Dir = (0,0,"+size.multiply(Decimal.MILLE)+")\n");
		write.append("FreeCAD.activeDocument()."+object+".Solid = (True)\n");
		write.append("FreeCAD.activeDocument()."+object+".TaperAngle = (0)\n");
		write.append("FreeCAD.activeDocument()."+object+".Label = '"+object+"'\n");
		write.append("Gui.activeDocument()."+forme+".Visibility=False\n");
		
		if (offset != null) {
			write.append("FreeCAD.activeDocument()."+object+".Placement = App.Placement(App.Vector(0,0,"+offset.multiply(Decimal.MILLE)+"),App.Rotation(App.Vector(0,0,1),0))\n");
		}
		write.append("App.activeDocument().recompute()\n");

		return write.toString();
	}

	
	/**
	 *     
	 * @param toCut
	 * @param cutter
	 * @return
	 */
	public static String cutAreas(String toCut, String cutter, String result) {
		StringBuilder write = new StringBuilder();

		write.append("App.activeDocument().addObject(\"Part::Cut\",\""+result+"\")\n");
		write.append("App.activeDocument()."+result+".Base = App.activeDocument()."+toCut+"\n");
		write.append("App.activeDocument()."+result+".Tool = App.activeDocument()."+cutter+"\n");
		write.append("Gui.ActiveDocument."+result+".ShapeColor=Gui.ActiveDocument."+toCut+".ShapeColor\n");
		write.append("Gui.ActiveDocument."+result+".DisplayMode=Gui.ActiveDocument."+toCut+".DisplayMode\n");
		write.append("App.ActiveDocument.recompute()\n");

		return write.toString();
	}


	public static void createPositionFile(ArrayList<PrintableObject> allObjects, PrintingParameters params,	String catafilename) throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss"); 
		String dte = sdf.format(new Date());

		// --> Supprime l'extention
		String cataFile = catafilename;
		int p = catafilename.lastIndexOf('.');
		if (p != -1) {
			cataFile = catafilename.substring(0, p);
		}
		
		File nomFichier = new File(params.fileName + File.separator+cataFile+"."+dte+".scr");// ou jpg 
		FileWriter write = new FileWriter(nomFichier); 
		
		write.write("#File = "+catafilename+"\n");
		sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss"+"\n"); 
		write.write("#Date ="+sdf.format(new Date()));

		//Affiche les instructions
		write.write("import FreeCAD\nimport Sketcher\n\n\n");
		
		write.write("Gui.activateWorkbench(\"SketcherWorkbench\")"+"\n");

		int pos = 0;
		for (PrintableObject po : allObjects) {
			PrintedPlan pg = (PrintedPlan) po;
			if (pg.is3DPrintable()) {
				write.write("#Tranche = "+pos+" - "+pg.getZPosition()+" - "+pg.getEpaisseur()+"\n");
	
				StringBuilder formeName = new StringBuilder("Forme_");
				formeName.append(pos);
	
				Rotation rot = new Rotation (Axis.YAxis, new Decimal("-90"), null);
				Area toPrint = null;
				if (pg.getAxis() == Axis.ZAxis) {
					toPrint = pg.getPrintable();
				} else {
					toPrint = pg.getPrintable().transform(rot);
				}
	
				String forme = createAreaScript(toPrint, formeName.toString(), pg.getZPosition(), pg.getAxis());
				write.write(forme);
	
				StringBuilder objectName = new StringBuilder("Tranche_");
				objectName.append(pos);
	
				write.write(extrudArea(formeName.toString(), objectName.toString(), pg.getEpaisseur(), null));
				
				int i = 0;
				Decimal ep2 = pg.getEpaisseur().add(Decimal.DEUX.divide(Decimal.MILLE));
				StringBuilder cutName= objectName;
				StringBuilder lastName = objectName;
	
				if (pg.getTrous() != null) {
					for (Area tr : pg.getTrous()) {
						StringBuilder trouName = new StringBuilder("Trou_");
						trouName.append(pos);
						trouName.append("_");
						trouName.append(i);
		
						toPrint = null;
						if (pg.getAxis() == Axis.ZAxis) {
							toPrint = tr;
						} else {
							toPrint = tr.transform(rot);
						}
		
						
						String trou = createAreaScript(toPrint, trouName.toString(), pg.getZPosition(), pg.getAxis());
						write.write(trou);
		
						StringBuilder trou2Name = new StringBuilder(trouName);
						trou2Name.append("_extr");
		
						// Extrude le trou 
						write.write(extrudArea(trouName.toString(), trou2Name.toString(), ep2, Decimal.UN.divide(Decimal.MILLE).negate()));
		
						cutName= new StringBuilder(objectName);
						cutName.append("_");
						cutName.append(i);
		
						// Construit la tranche en effectuant les trous
						write.write(cutAreas(lastName.toString(), trou2Name.toString(), cutName.toString()) );
						lastName = new StringBuilder(cutName.toString());
						
						i++;
					}
				}
			write.write("FreeCAD.activeDocument().getObject(\""+lastName.toString()+
					 "\").Placement = App.Placement(App.Vector(0,0,"+pg.getZPosition().multiply(Decimal.MILLE)+"),App.Rotation(App.Vector(0,0,1),0))\n");
			pos ++;
			}
		}

		write.close();
	}
		
}

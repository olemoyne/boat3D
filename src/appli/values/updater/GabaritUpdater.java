package appli.values.updater;

import model.Gabarit;
import model.math.Decimal;

/** 
 * Permet de mettre à jour un vecteur
 * 
 * @author olemoyne
 *
 */
public class GabaritUpdater extends ObjectUpdater {

	DecimalField position, epaisseur, bordure;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4291603188855301380L;

	public GabaritUpdater () {
		super();
		
		addFields();
	}
	
	// Ajoute les champs de saisie
	private void addFields() {
		position = new DecimalField("Position (Z): ", "m");
		add(position);

		epaisseur = new DecimalField("Epaisseur : ", "m");
		add(epaisseur);

		bordure = new DecimalField("Bordure : ", "m");
		add(bordure);
	}

	public void setProperty (Object val) {
		Gabarit v = (Gabarit)val;
		position.setField(v.position);
		epaisseur.setField(v.epaisseur);
		if (v.bordure == null)
			bordure.setField(new Decimal());
		else bordure.setField(v.bordure);
	}
	
	public Object getData() {
		Decimal p = (Decimal) position.getValue();
		Decimal e = (Decimal) epaisseur.getValue();
		Decimal b = (Decimal) bordure.getValue();
		
		Gabarit ret = new Gabarit();
		ret.position = p;
		ret.epaisseur = e;
		ret.bordure = b;
		
		return ret;
	}
	
}

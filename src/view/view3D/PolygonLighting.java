package view.view3D;


import javax.swing.JFrame; 

import model.math.Vecteur;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator; 
 
public class PolygonLighting implements GLEventListener { 
   private float rpoly;
	
   @Override 
	
   public void display( GLAutoDrawable drawable ) {
   
      final GL2 gl = drawable.getGL().getGL2(); 
      gl.glColor3f(1f,0f,0f); //applying red
      
      // Clear The Screen And The Depth Buffer 
      gl.glClear( GL2.GL_COLOR_BUFFER_BIT |  
      GL2.GL_DEPTH_BUFFER_BIT );   
      gl.glLoadIdentity();       // Reset The View    
      gl.glRotatef( rpoly, rpoly, rpoly, 0.0f ); 
		
      this.drawObject(gl, new Vecteur("0;0;0"), new Vecteur("0.2;0.2;0.2"));
/**      gl.glBegin( GL2.GL_POLYGON ); 
      
      gl.glVertex3f( 0f,0.5f,0f ); 
      gl.glVertex3f( -0.5f,0.2f,0f ); 
      gl.glVertex3f( -0.5f,-0.2f,0f ); 
      gl.glVertex3f( 0f,-0.5f,0f ); 
      gl.glVertex3f( 0f,0.5f,0f ); 
      gl.glVertex3f( 0.5f,0.2f,0f ); 
      gl.glVertex3f( 0.5f,-0.2f,0f ); 
      gl.glVertex3f( 0f,-0.5f,0f ); 
      
      gl.glEnd(); 
**/		
      gl.glFlush(); 
      
      rpoly += 0.2f;  //assigning the angle 
      
      gl.glEnable( GL2.GL_LIGHTING );  
      gl.glEnable( GL2.GL_LIGHT0 );  
      gl.glEnable( GL2.GL_NORMALIZE );  

      // weak RED ambient 
      float[] ambientLight = { 0.1f, 0.f, 0.f,0f };  
      gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, ambientLight, 0);  

      // multicolor diffuse 
      float[] diffuseLight = { 1f,2f,1f,0f };  
      gl.glLightfv( GL2.GL_LIGHT0, GL2.GL_DIFFUSE, diffuseLight, 0 ); 
   }  
      
   @Override 
   public void dispose( GLAutoDrawable arg0 ) { 
      //method body  
   } 
  
   @Override 
   public void init( GLAutoDrawable arg0 ) { 
      // method body     
   } 
	
   @Override 
   public void reshape( GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4 ) { 
      // method body 
   } 
	
   
	public void drawCarre(GL2 gl, Vecteur a, Vecteur b, Vecteur c, Vecteur d) {
		
		gl.glBegin(GL2.GL_QUADS);		
		gl.glVertex3fv(a.getFloats(), 0);
		gl.glVertex3fv(b.getFloats(), 0);
		gl.glVertex3fv(c.getFloats(), 0);
		gl.glVertex3fv(d.getFloats(), 0);
		gl.glEnd();
			
	}

	public void drawObject(GL2 gl, Vecteur deb, Vecteur fin) {

/**	    gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, this.getColor(GL2.GL_AMBIENT), 0);
	    gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, this.getColor(GL2.GL_DIFFUSE), 0);
	    gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, this.getColor(GL2.GL_SPECULAR), 0);
	    gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, 5f);
	    gl.glShadeModel(GL2.GL_SMOOTH);
*/

	    drawCarre(gl,  // face avant
				deb, 
				new Vecteur(deb.getDecX(), fin.getDecY(), deb.getDecZ()),
				new Vecteur(fin.getDecX(), fin.getDecY(), deb.getDecZ()), 
				new Vecteur(fin.getDecX(), deb.getDecY(), deb.getDecZ()));

		drawCarre(gl,  // face arri�re 
				new Vecteur(deb.getDecX(), deb.getDecY(), fin.getDecZ()),
				new Vecteur(fin.getDecX(), deb.getDecY(), fin.getDecZ()),
				new Vecteur(fin.getDecX(), fin.getDecY(), fin.getDecZ()), 
				new Vecteur(deb.getDecX(), fin.getDecY(), fin.getDecZ()));

		drawCarre(gl, // 
				deb, new Vecteur(deb.getDecX(), fin.getDecY(), deb.getDecZ()), 
				new Vecteur(deb.getDecX(), fin.getDecY(), fin.getDecZ()),
				new Vecteur(deb.getDecX(), deb.getDecY(), fin.getDecZ()));

		drawCarre(gl, new Vecteur(fin.getDecX(), deb.getDecY(), deb.getDecZ()), 
				new Vecteur(fin.getDecX(), fin.getDecY(), deb.getDecZ()), 
				new Vecteur(fin.getDecX(), fin.getDecY(), fin.getDecZ()),
				new Vecteur(fin.getDecX(), deb.getDecY(), fin.getDecZ()));

		drawCarre(gl, new Vecteur(deb.getDecX(), deb.getDecY(), deb.getDecZ()), 
				new Vecteur(deb.getDecX(), deb.getDecY(), fin.getDecZ()), 
				new Vecteur(fin.getDecX(), deb.getDecY(), fin.getDecZ()),
				new Vecteur(fin.getDecX(), deb.getDecY(), deb.getDecZ()));

		drawCarre(gl, new Vecteur(deb.getDecX(), fin.getDecY(), deb.getDecZ()),
				new Vecteur(deb.getDecX(), fin.getDecY(), fin.getDecZ()),
				new Vecteur(fin.getDecX(), fin.getDecY(), fin.getDecZ()),
				new Vecteur(fin.getDecX(), fin.getDecY(), deb.getDecZ()));

	}

   public static void main( String[] args ) { 
   
      //getting the capabilities object of GL2 profile 
      final GLProfile profile = GLProfile.get( GLProfile.GL2 ); 
      GLCapabilities capabilities = new GLCapabilities( profile);

      // The canvas  
      final GLCanvas glcanvas = new GLCanvas( capabilities ); 
      PolygonLighting polygonlighting = new PolygonLighting(); 
      glcanvas.addGLEventListener( polygonlighting ); 
      glcanvas.setSize( 400, 400 ); 

      //creating frame 
      final JFrame frame = new JFrame (" Polygon lighting ");  

      //adding canvas to it 
      frame.getContentPane().add( glcanvas ); 
      frame.setSize( frame.getContentPane().getPreferredSize()); 
      frame.setVisible( true );  
                    
      //Instantiating and Initiating Animator 
      final FPSAnimator animator = new FPSAnimator(glcanvas, 300,true ); 
      animator.start();                     
      
   } //end of main 
	
} //end of class 
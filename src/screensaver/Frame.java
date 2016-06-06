package screensaver;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

//@SuppressWarnings("serial")
public class Frame extends JFrame implements KeyListener
{
	public static int width;
    public static int height;
    public static int centerX;
    public static int centerY;
	
	public Frame() 
	{
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        
        //assign variables for screen
		width = (int)device.getDefaultConfiguration().getBounds().getWidth();
        height = (int)device.getDefaultConfiguration().getBounds().getHeight();
        centerX = width / 2;
        centerY = height / 2;
		
        //construct KeyListener object
        addKeyListener(this);
        setFocusable(true);
        
        //construct Panel object
		add(new Panel());
		
		//set full screen
		setUndecorated(true);
        device.setFullScreenWindow(this);
		setVisible(true);	
	}
	
	//methods for keyListener (terminates program when key is pressed)
	public void keyPressed(KeyEvent e)
	{
		System.exit(0);
	}
	
	public void keyReleased(KeyEvent e) {}
	
	public void keyTyped(KeyEvent e) {}
}

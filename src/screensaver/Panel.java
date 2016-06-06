package screensaver;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.swing.JPanel;
import javax.swing.Timer;

@SuppressWarnings("serial")
public class Panel extends JPanel 
{
	
	public static BufferedImage[] images;
	public static int[] radius = {60, 90, 130, 170, 230, 290, 340, 380};
	public static int[] planX = new int[8];
	public static int[] planY = new int[8];
	public static int[] xFromCent = new int[8];
	public static Clip clip;
	private static double[] angle = new double[8];
	private static double[] triAng = new double[8];
	private static int[] change = {10, 6, 5, 4, 2, 3, 1, 1};
	private Timer timer = new Timer(100, new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			for (int i = 0; i < 8; i++)
			{
				//angle increases until 360 and then back to 0
				angle[i] += change[i];
				if (angle[i] >= 360)
					angle[i] -= 360;
			}
			//generate angles for triangle
			getTriAng();
			
			//get x and y coordinates
			getXandY();
			
			repaint();
		}
		
	});
	
	public Panel() 
	{
		//load images
		try {
			BufferedImage[] images = {ImageIO.read(new File("res/back.png")),
									  ImageIO.read(new File("res/sun.png")),
									  ImageIO.read(new File("res/img0.png")),
									  ImageIO.read(new File("res/img1.png")),
									  ImageIO.read(new File("res/img2.png")),
									  ImageIO.read(new File("res/img3.png")),
									  ImageIO.read(new File("res/img4.png")),
									  ImageIO.read(new File("res/img5.png")),
									  ImageIO.read(new File("res/img6.png")),
									  ImageIO.read(new File("res/img7.png"))};
			this.images = images;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		//open Panel
		setSize(Frame.width, Frame.height);
		setVisible(true);
		
		//start timer
		timer.start();
		
		//play music
		playMusic();
		
		//generate coordinates and find start angle for planets
		genCoord();
		findStartAngle();
	}
	
	public void paintComponent(Graphics g) 
	{
		//draw background
		g.drawImage(images[0], 0, 0, null);
		
		//draw sun
		int sunX = Frame.centerX - (images[1].getWidth() / 2);
		int sunY = Frame.centerY - (images[1].getHeight() / 2);
		g.drawImage(images[1], sunX, sunY, null);
		
		//draw ellipses
		Color ellCol = new Color(0xffffff);
		g.setColor(ellCol);
		
		for (int i = 0; i < radius.length; i++) {
			int ellX = Frame.centerX - radius[i];
			int ellY = Frame.centerY - radius[i];
			int diameter = radius[i] * 2;
			g.drawOval(ellX, ellY, diameter, diameter);
		}
		
		//draw planets
		for (int i = 0; i < 8; i++) 
			g.drawImage(images[i+2], planX[i], planY[i], null);
	}
	
	//generate x and y coordinates
	public void genCoord() 
	{
		//generate x coordinates
		for(int i = 0; i < radius.length; i++) 
		{
			//random left or right of center
			boolean plusOrMinus = Math.random() < 0.5;
			int distFromCent = (int)(Math.random() * (radius[i] + 1));
			int centerX = images[i+2].getWidth() / 2;
			
			if(plusOrMinus == true) //left of center
	            xFromCent[i] = -1 * distFromCent;
	         else 					//right of center
	        	 xFromCent[i] = distFromCent;
			int genX = Frame.centerX + xFromCent[i] - centerX;
			
			planX[i] = genX;
		}
		
		//generate y coordinates
		for(int i = 0; i < radius.length; i++) 
		{
			//Pythagorean formula
			int squareR = (int) Math.pow(radius[i] , 2);
			int squareX = (int) Math.pow(xFromCent[i], 2);
			int squared = (int) Math.sqrt(squareR - squareX);
			int centerY = images[i+2].getHeight() / 2;
			int yFromCent;
			
			//random above or below center
			boolean plusOrMinus = Math.random() > 0.5;
			
			if(plusOrMinus == true) //above center
				yFromCent = Frame.centerY - squared - centerY;
		     else 				//below center
		    	yFromCent = Frame.centerY + squared - centerY;
			
	    	planY[i] = yFromCent;
		}
	}
	
	//find starting angle from 0 degrees
	public static void findStartAngle()
	{
		for (int i = 0; i < 8; i++)
		{
			xFromCent[i] = Math.abs(xFromCent[i]);
			
			//find starting angle
			if (planX[i] == Frame.centerX) //directly above or below
			{
				if (planY[i] < Frame.centerY) //above
					angle[i] = 90.0;
				else 						  //below
					angle[i] = 270.0;
			}
			else if (planY[i] == Frame.centerY) //directly left or right
			{
				if (planX[i] < Frame.centerX) //left
					angle[i] = 180.0;
				else 						  //right
					angle[i] = 0.0;
			}
			else 							  //not directly
			{
				//calculate angle with arc cosine
				double x = xFromCent[i];
				double r = radius[i];
				double val = x / r;
				double initAng = Math.toDegrees(Math.acos(val));
				triAng[i] = initAng;
				
				if (planX[i] > Frame.centerX && planY[i] < Frame.centerY)      //quadrant 1
					angle[i] = initAng;
				else if (planX[i] < Frame.centerX && planY[i] < Frame.centerY) //quadrant 2
					angle[i] = 180 - initAng;
				else if (planX[i] < Frame.centerX && planY[i] > Frame.centerY) //quadrant 3
					angle[i] = 180 + initAng;
				else 														   //quadrant 4
					angle[i] = 360 - initAng;
			}
		}
	}
	
	//get angles for triangle (to calculate x and y coordinates)
	public void getTriAng()
	{
		for (int i = 0; i < 8; i++)
		{
			if (angle[i] > 270) 		//between 270 and 360
				triAng[i] = 360 - angle[i];
			else if (angle[i] > 180) 	//between 180 and 270
				triAng[i] = angle[i] - 180;
			else if (angle[i] > 90) 	//between 90 and 180
				triAng[i] = 180 - angle[i];
			else 						//between 0 and 90
				triAng[i] = angle[i];
		}
	}
	
	//get x and y coordinates for specified angle
	public void getXandY()
	{
		for (int i = 0; i < 8; i++)
		{
			if (angle[i] % 90 == 0 || angle[i] == 0) //directly above/below/left/right
			{
				if (angle[i] == 0)					 //directly right
				{
					planX[i] = Frame.centerX + radius[i];
					planY[i] = Frame.centerY;
				}
				else if (angle[i] == 90)			 //directly above
				{
					planX[i] = Frame.centerX;
					planY[i] = Frame.centerY - radius[i];
				}
				else if (angle[i] == 180)			 //directly left
				{
					planX[i] = Frame.centerX - radius[i];
					planY[i] = Frame.centerY;
				}
				else if (angle[i] == 270)			 //directly below
				{
					planX[i] = Frame.centerX;
					planY[i] = Frame.centerY + radius[i];
				}
			}
			else //not directly - for different quadrants
			{
				double radians = Math.toRadians(triAng[i]);
				
				//find x distance from center
				double cosine = Math.cos(radians);
				xFromCent[i] = (int) (cosine * radius[i]);
				
				//find y distance from center
				double sine = Math.sin(radians);
				int yFromCent = (int) (sine * radius[i]);
				
				if (angle[i] < 90) 				//quadrant 1 (0 to 90)
				{
					planX[i] = Frame.centerX + xFromCent[i];
					planY[i] = Frame.centerY - yFromCent;
				}
				else if (angle[i] < 180) 		//quadrant 2 (90 to 180)
				{
					planX[i] = Frame.centerX - xFromCent[i];
					planY[i] = Frame.centerY - yFromCent;
				}
				else if (angle [i] < 270) 		//quadrant 3 (180 to 270)
				{
					planX[i] = Frame.centerX - xFromCent[i];
					planY[i] = Frame.centerY + yFromCent;
				}
				else 							//quadrant 4 (270 to 360)
				{
					planX[i] = Frame.centerX + xFromCent[i];
					planY[i] = Frame.centerY + yFromCent;
				}
			}
			
			//stays on center of orbit
			int centerX = images[i+2].getWidth() / 2;
			int centerY = images[i+2].getHeight() / 2;
			planX[i] -= centerX;
			planY[i] -= centerY;
		}
	}
	
	//play music
	public void playMusic()
	{
		try 
		{
			Line.Info linfo = new Line.Info(Clip.class);
		    Line line = AudioSystem.getLine(linfo);
		    clip = (Clip) line;
			File source = new File("res/song.wav");
			AudioInputStream audio = AudioSystem.getAudioInputStream(source);
			clip.open(audio);
			clip.start();
			clip.loop(Clip.LOOP_CONTINUOUSLY);
	    } 
		catch(Exception ex) 
		{
	        System.out.println("Error with playing sound.");
	        ex.printStackTrace();
	    }
	}
}

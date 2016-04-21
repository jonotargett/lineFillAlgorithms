package lineFillAlgorithms;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.Vector;

public class Bicycle {

	public static final double maxSteeringAngle = 3.1415926535 / 4.0;		// in radians? 45 deg?
	public static final double advanceStep = 2.0;							// measured in metres?
	public static final double wheelSeparation = -30.0;						// distance the turning wheel is *INFRONT* of the focal point
	public static final double wheelWidth = 20.0;							// lateral distance between wheels
	public static final double sensorOffset = -40.0;							// distance in front of the focal point
	public static final double sensorWidth = 60.0;							// this isnt in metres. sue me
	
	private Vector<Double> steeringAngles;		// measured in radians?
	private Vector<Point2D.Double> positions;
	
	private Point2D.Double initPos;
	private Point2D.Double dir;
	
	private int step;
	
	public Bicycle() {
		steeringAngles = new Vector<Double>();
		positions = new Vector<Point2D.Double>();
		
		initPos = new Point2D.Double(100, 200);
		dir = new Point2D.Double(1, 0);
		
		step = 0;
	}
	
	public void generateSteeringAngles() {
		
		for(int i=1; i<4000; i++) {
			Double f = -1.0*Math.sin(3.1415926535 * (double)(i)/500.0) * Math.cos((double)(i)/510.0);
			
			
			// capping between allowable steering angles
			
			if(f > maxSteeringAngle)
				f = maxSteeringAngle;
			if(f < -maxSteeringAngle)
				f = -maxSteeringAngle;
			
			
			steeringAngles.addElement(f);
		}
	}
	
	
	
	
	public void generatePath() {
		positions.addElement(new Point2D.Double(initPos.x, initPos.y));
		
		Point2D.Double lastPos = new Point2D.Double(initPos.x, initPos.y);
		Point2D.Double curPos = new Point2D.Double(0, 0);
		
		for(int i=0; i<steeringAngles.size(); i++) {
			
			
			// radius of curvature based on angle of wheel
			double radius = wheelSeparation / Math.tan((double)steeringAngles.elementAt(i));
			
			// r*theta = advance step. what rotation do we go through, around this radius, at the step amount?
			double theta = advanceStep / radius;
			
			double tan = Math.tan(theta);
			double sin = Math.sin(theta);
			double cos = Math.cos(theta);
			
			double A_2 = radius;
			double O_2 = A_2 * Math.tan(theta);
			
			// hypotenuse H_1 == adjacent A_2. see diagram
			double A_1 = A_2 * Math.cos(theta);
			double O_1 = A_1 * Math.tan(theta);
			
			double delta_O = O_2 - O_1;
			double delta_A = A_2 - A_1;
			
			
			// perpendicular vector, calculated as 90 degree rotation CCW
			Point2D.Double leftDir = new Point2D.Double(-dir.y, dir.x);
			//
	
			double curPosX = lastPos.x + (dir.x * O_1 + leftDir.x * delta_A);
			double curPosY = lastPos.y + (dir.y * O_1 + leftDir.y * delta_A);
			
			curPos = new Point2D.Double(curPosX, curPosY);
			

			positions.addElement(new Point2D.Double(curPos.x, curPos.y));
			
			
			// rotate the direction vector to face the new angle
			dir = new Point2D.Double(dir.x * cos - dir.y * sin, dir.x * sin + dir.y * cos);
			
			lastPos = new Point2D.Double(curPos.x, curPos.y);
			
			
			
			// SHITTY OLD TEST SCRIPT
			//curPos = new Point2D.Double(lastPos.x + 1, lastPos.y + (double)steeringAngles.elementAt(i));
			//positions.addElement(curPos);
			//lastPos = curPos;
		}
	}
	
	
	
	
	public void draw(Graphics2D g) {
		
		
		
		double x1 = 0;
		double x2 = 0;
		double y1 = 0;
		double y2 = 0;
		
		Point2D.Double forward = new Point2D.Double(1, 0);
		Point2D.Double look =new Point2D.Double(1, 0);
		Point2D.Double left =new Point2D.Double(1, 0);
		
		for(int i=0; i<(Math.min(positions.size()-1, step)); i++) {
			
			Point2D.Double p1 = positions.elementAt(i);
			Point2D.Double p2 = positions.elementAt(i+1);
			
			x1 = p1.x;
			x2 = p2.x;
			y1 = p1.y;
			y2 = p2.y;
			
			g.setColor(Color.RED);
			g.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
			
			
			forward = new Point2D.Double(1, 0);
			look = new Point2D.Double(x2 - x1, y2 - y1);
			double len = Math.sqrt(look.x * look.x + look.y * look.y);
			look.x = look.x / len;
			look.y = look.y / len;
			
			left = new Point2D.Double(-look.y, look.x);
			
			g.setColor(Color.LIGHT_GRAY);
			g.drawLine((int)(x1-left.x*sensorWidth/2.0 + look.x*sensorOffset), 
						(int)(y1-left.y*sensorWidth/2.0+look.y*sensorOffset), 
						(int)(x1+left.x*sensorWidth/2.0+look.x*sensorOffset), 
						(int)(y1+left.y*sensorWidth/2.0+look.y*sensorOffset));
						
		}
		
		
		
		

		g.setColor(Color.BLUE);
		g.setStroke(new BasicStroke((int)wheelWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0));
		g.drawLine((int)x2, (int)y2, (int)(x2 + look.x * wheelSeparation), (int)(y2 + look.y * wheelSeparation));
		g.setColor(Color.DARK_GRAY);
		g.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0));
		g.drawLine((int)(x1-left.x*sensorWidth/2.0 + look.x*sensorOffset), 
					(int)(y1-left.y*sensorWidth/2.0+look.y*sensorOffset), 
					(int)(x1+left.x*sensorWidth/2.0+look.x*sensorOffset), 
					(int)(y1+left.y*sensorWidth/2.0+look.y*sensorOffset));

		//g.drawLine(x1, y1, x2, y2);
		//g.fillOval(x1-1, y1-1, 2, 2);
		//g.fillOval(x2-8, y2-8, 16, 16);
		
		step++;
		
	}
	
}

package lineFillAlgorithms;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.Timer;

public class DrawingPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Polygon zone;
	private Point2D.Double initPos;
	private Point2D.Double curPos;
	private Point2D.Double forwardVec;
	private Point2D.Double downVec;
	
	private final double width = 5.0;
	private final double minimumTravel = 1.0;
	private final int maxSearchForAcutes = 100;
	
	private Vector<Point2D.Double> path;
	private Vector<Polygon> convexZones; 
	
	
	private final Stroke stroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 15);
	private final Stroke zoneStroke = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 15);
	private final AlphaComposite transparent = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
	private final AlphaComposite opaque = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
	
	
	private Bicycle bc;
	private Timer t;
	
	
	public DrawingPanel() {
		// disables default layout manager
		super(null);
		
		initPos = new Point2D.Double(850, 750);
		curPos = new Point2D.Double();
		
		path = new Vector<Point2D.Double>();
		convexZones = new Vector<Polygon>();
		
		bc = new Bicycle();
		bc.generateSteeringAngles();
		bc.generatePath();
		
		zone = new Polygon();
		
		t = new Timer(50, this);
		t.start();
		
		// TURNING ORDER NOT CCW
		/*
		//zone.addPoint(200, 200);
		//zone.addPoint(600, 200);
		//zone.addPoint(600, 600);
		//zone.addPoint(200, 700);
		//zone.addPoint(100, 500);
		//zone.addPoint(250, 420);
		 */
		
		// DUNNO WHATS GOING ON HERE
		/*
		zone.addPoint(100, 500);
		zone.addPoint(200, 700);
		zone.addPoint(600, 600);
		zone.addPoint(600, 200);
		zone.addPoint(200, 200);
		zone.addPoint(400, 400);
		*/
		
		
		
		zone.addPoint(400,  400);
		zone.addPoint(420, 500);
		zone.addPoint(720,  600);
		zone.addPoint(800, 230);
		zone.addPoint(600, 250);
		zone.addPoint(610, 150);
		zone.addPoint(790, 100);
		zone.addPoint(500, 90);
		zone.addPoint(330, 330);
		
		
		/*
		zone.addPoint(400, 400);
		zone.addPoint(350, 600);
		zone.addPoint(650, 660);
		zone.addPoint(750, 450);
		zone.addPoint(500, 350);
		zone.addPoint(750, 350);
		zone.addPoint(680, 200);
		zone.addPoint(325, 220);
		*/
		
		
		
		
		
		
	}
	
	private double length(Point2D.Double p) {
		return Math.sqrt( Math.pow(p.getX(), 2) + Math.pow(p.getY(), 2) );
	}
	
	
	public void convexSplit() {
		
		// hertelMehlhornPartition
		// algorithm for splitting a concave closed polygon into convex parts.
		// https://www8.cs.umu.se/kurser/TDBA77/VT06/algorithms/BOOK/BOOK5/NODE194.HTM
		//
		// 1.	triangulate everything. attempt at ear clipping algorithm
		// 2.	decomposition of edges, where edge removal does not introduce concavity
		// 3.	exit
		//---------------------------------
		
		
		// ----------------------------------------------------------------------------
		// step 1: 
		/*
		 * From Polygon Triangulation by Daniel Taylor @ gamedev.net
		 * 
		 * create a list of the vertices (perferably in CCW order, starting anywhere)
			while true
			  for every vertex
			    let pPrev = the previous vertex in the list
			    let pCur = the current vertex;
			    let pNext = the next vertex in the list
			    if the vertex is not an interior vertex (the wedge product of (pPrev - pCur) and (pNext - pCur) <= 0, for CCW winding);
			      continue;
			    if there are any vertices in the polygon inside the triangle made by the current vertex and the two adjacent ones
			      continue;
			    create the triangle with the points pPrev, pCur, pNext, for a CCW triangle;
			    remove pCur from the list;
			  if no triangles were made in the above for loop
			    break;
		 */
		
		// ----------------------------------------------------------------------------
		
		// make working copy of zone
		Polygon work = new Polygon();
		for(int i=0; i<zone.npoints; i++) {
			work.addPoint(zone.xpoints[i], zone.ypoints[i]);
		}
		
		
		while(true) {
			boolean madeATriangle = false;
			
			// break early if its not possible to make further triangles
			if(work.npoints < 3)
				break;
			
			// for each vertex in the working set
			for(int i=0; i<work.npoints; i++) {
				
				// form triangles
				int pPrev = i-1;
				int pCur = i;
				int pNext = i + 1;
				
				if(pPrev < 0)
					pPrev = work.npoints - 1;
				if(pNext == work.npoints)
					pNext = 0;

				// calculate the determinate to find concavity/convexity
				Point2D.Double pc = new Point2D.Double(work.xpoints[pPrev] - work.xpoints[pCur],
														work.ypoints[pPrev] - work.ypoints[pCur]);
				Point2D.Double nc = new Point2D.Double(work.xpoints[pNext] - work.xpoints[pCur],
														work.ypoints[pNext] - work.ypoints[pCur]);
				
				double det = pc.x * nc.y - pc.y * nc.x;
				
				// if this is NOT an interior vertex
				if(det < 0) {	
					// then skip, move to the next vertex
					continue;
				}
				
				
				Polygon test = new Polygon();
				test.addPoint(work.xpoints[pPrev], work.ypoints[pPrev]);
				test.addPoint(work.xpoints[pCur], work.ypoints[pCur]);
				test.addPoint(work.xpoints[pNext], work.ypoints[pNext]);
				
				boolean shouldContinue = false;
				
				// test if any of the vertices in the working set lie within the new
				// polygon, created around the current vertex
				for(int j=0; j<work.npoints; j++) {
					if(j == pPrev ||
							j == pCur ||
							j == pNext) {
						continue;
					}
					
					if(test.contains(new Point2D.Double(work.xpoints[i], work.ypoints[i]))) {
						shouldContinue = true;
						break;
					}
				}
				
				// ignore this vertex if any other vertex lies within the created polygon
				if(shouldContinue)
					continue;
				
				
				// success! found a possible triangle
				convexZones.add(test);
				
				// now recreate the working set
				Polygon newWork = new Polygon();
				for(int j=0; j<work.npoints; j++) {
					if(j == pCur)
						continue;
					newWork.addPoint(work.xpoints[j], work.ypoints[j]);
				}
				work = null;
				work = newWork;
				
				
				madeATriangle = true;
				break;
			}
			
			if(madeATriangle == false)
				break;
			
		}
		
		
		
		
		// DECOMPOSITION ---------------------------------------------------------------------
		
		// iterate through the polygons, try to find a pair which share a chord
		// see if that chord can be decomposed without introducing concavity
		// TODO: introduce some minimum 'closeness' factor to decompose vertices within a certain distance of each other
		
		
		
		while(true) {
			
			boolean breaking = false;
			
			for(int i=0; i<convexZones.size(); i++) {
				
				if(breaking)
					break;
				
				for(int j=0; j<convexZones.size(); j++) {
					if(i == j)
						continue;
					
					Polygon p1 = convexZones.elementAt(i);
					Polygon p2 = convexZones.elementAt(j);
					
					int index11 = 0, index12 = 0, index21 = 0, index22 = 0;
					
					int shared = 0;
					
					for(int k=0; k<p1.npoints; k++) {
						Point2D.Double pp = new Point2D.Double(p1.xpoints[k], p1.ypoints[k]);
						
						for(int l=0; l<p2.npoints; l++) {
							Point2D.Double pc = new Point2D.Double(p2.xpoints[l], p2.ypoints[l]);
							
							if(pp.x == pc.x && pp.y == pc.y) {
								
								if(shared == 0) {
									index11 = k;
									index21 = l;
								} else {
									index12 = k;
									index22 = l;
								}
								shared++;
							}
						}
					}
					
					if(shared == 2) {
						// A CHORD IS SHARED. PRAISE THE SUN
						
						// this is where we merge them
						
						Polygon newPoly = new Polygon();
						
						boolean started = false;
						int k = 0;
						
						while(true) {
							if(k == index11)
								started = true;
							
							if(k == index12 && started)
								break;
							
							if(started) {
								newPoly.addPoint(p1.xpoints[k], p1.ypoints[k]);
							}
							
							k++;
							if(k == p1.npoints)
								k = 0;
						}
						
						started = false;
						k = 0;
						while(true) {
							if(k == index22)
								started = true;
							
							if(k == index21 && started)
								break;
							
							if(started) {
								newPoly.addPoint(p2.xpoints[k], p2.ypoints[k]);
							}
							
							k++;
							if(k == p2.npoints)
								k = 0;
						}
						
						
						// check for concavity
						// TODO: this needs to be more agressive. this is missing some obvious additions 
						// that are still convex. appears to form quads and then give up
						// TODO: needs to check for internal points
						boolean concave = false;
						
						// for each vertex in the working set
						for(int l=0; l<newPoly.npoints; l++) {
							
							// form triangles
							int pPrev = l-1;
							int pCur = l;
							int pNext = l + 1;
							
							if(pPrev < 0)
								pPrev = newPoly.npoints - 1;
							if(pNext == newPoly.npoints)
								pNext = 0;

							// calculate the determinate to find concavity/convexity
							Point2D.Double pc = new Point2D.Double(newPoly.xpoints[pPrev] - newPoly.xpoints[pCur],
																	newPoly.ypoints[pPrev] - newPoly.ypoints[pCur]);
							Point2D.Double nc = new Point2D.Double(newPoly.xpoints[pNext] - newPoly.xpoints[pCur],
																	newPoly.ypoints[pNext] - newPoly.ypoints[pCur]);
							
							double det = pc.x * nc.y - pc.y * nc.x;
							
							// if this is NOT an interior vertex
							if(det <= 0) {	
								concave = true;
								break;
							}
						}
						
						if(!concave) {
							convexZones.remove(p1);
							convexZones.remove(p2);
							convexZones.add(newPoly);
						
							
							breaking = true;
							break;
						}
					}
				}
			}
			
			if(breaking == false) {
				break;
			}
		}
		
		
	}
	

	public void zoneSearch() {
		
		Vector<Polygon> cz = new Vector<Polygon>();
		
		for(int i=0; i<convexZones.size(); i++) {
			cz.addElement(convexZones.elementAt(i));
		}

		
		path.add(new Point2D.Double(initPos.x, initPos.y));
		//lineSearch(zone);
		
		
		curPos = initPos;
		
		while(cz.size() > 0) {
		
			int polyIndex = -1;
			double minDistance = Double.MAX_VALUE;
			int index = -1;
			
			for(int i=0; i<cz.size(); i++) {
				
				Polygon p = cz.elementAt(i);
				
				for(int j=0; j<p.npoints; j++) {
					double distance = Math.sqrt( Math.pow(curPos.x - p.xpoints[j], 2) + 
													Math.pow(curPos.y - p.ypoints[j], 2) );
					
					if(distance < minDistance) {
						minDistance = distance;
						polyIndex = i;
						index = j;
					}
				}
				
				
			}
			
			
			lineSearch(cz.elementAt(polyIndex));
			cz.remove(polyIndex);
		}
		
		path.add(new Point2D.Double(100, 100));
	}
	
	
	private void lineSearch(Polygon p) {
		
		//discover the closest entry point (a polygon corner) ---------------------------
		
		double minDistance = Double.MAX_VALUE;
		int index = -1;
		
		for(int i=0; i<p.npoints; i++) {
			double distance = Math.sqrt( Math.pow(curPos.x - p.xpoints[i], 2) + 
											Math.pow(curPos.y - p.ypoints[i], 2) );
			
			if(distance < minDistance) {
				minDistance = distance;
				index = i;
			}
		}
		
		curPos.x = p.xpoints[index];
		curPos.y = p.ypoints[index];
		

		path.add(new Point2D.Double(curPos.x, curPos.y));
		
		// -----------------------------------------------------------------------------
		// get scanline vectors, ie forward, and down (next line)
		
		int nextPoint = index + 1;
		if(nextPoint >= p.npoints)
			nextPoint = 0;
		
		int prevPoint = index - 1;
		if(prevPoint < 0)
			prevPoint = p.npoints-1;
		
		// get a forward vector and normalise it
		forwardVec = new Point2D.Double(p.xpoints[nextPoint] - curPos.x, p.ypoints[nextPoint] - curPos.y);
		double length = length(forwardVec);

		
		forwardVec.x = forwardVec.x / length;
		forwardVec.y = forwardVec.y / length;

		
		downVec = new Point2D.Double(forwardVec.y, -forwardVec.x);
		
		// double check that the 'downVec' actually points into the shape, rather than outside
		
		Point2D.Double compVec = new Point2D.Double(p.xpoints[prevPoint] - curPos.x, p.ypoints[prevPoint] - curPos.y);
		
		double dotProduct = compVec.x * downVec.x + compVec.y * downVec.y;
		
		if(dotProduct < 0) {
			// then the vectors do no point in the same direction (ie totally opposed)
			downVec.x *= -1;
			downVec.y *= -1;
		}
		
		
		// -------------------------------------------------------------------------------
		//start mapping out the path;
		
		// adjust for the potential of starting on an acute angle
		int steps = 0;
		
		// travel inwards
		curPos.x += downVec.x * width;
		curPos.y += downVec.y * width;
		
		while( !p.contains(curPos.x, curPos.y) ) {
			if(steps > maxSearchForAcutes)
				break;
			
			curPos.x += forwardVec.x * minimumTravel * 1;
			curPos.y += forwardVec.y * minimumTravel * 1;
			steps++;
		}
		
		
		path.add(new Point2D.Double(curPos.x, curPos.y));
		
		int rows = 0;
		
		
		
		while( true ) {
			
			if(rows > 0) {
				// travel inwards
				curPos.x += downVec.x * width;
				curPos.y += downVec.y * width;
			}
			
			path.add(new Point2D.Double(curPos.x, curPos.y));
			
			
			// these next two lines make the 'forwards' direction alternate between rows
			int direction = (rows % 2);
			if(direction == 0) {direction = -1; }
			direction *= -1;
			
			steps = 0;
			int waste = 0;
			
			while( p.contains(curPos.x, curPos.y)) {
				curPos.x += forwardVec.x * minimumTravel * direction;
				curPos.y += forwardVec.y * minimumTravel * direction;
				steps++;
				
				waste++;
				if(waste > 1000) {
					System.out.println("Wasdasste");
				}
			}
			
			curPos.x -= forwardVec.x * minimumTravel * direction;
			curPos.y -= forwardVec.y * minimumTravel * direction;

			
			while( !p.contains(curPos.x+downVec.x*width, curPos.y+downVec.y*width) ) {
				if(steps <= 0)
					break;
				
				curPos.x -= forwardVec.x * minimumTravel * direction;
				curPos.y -= forwardVec.y * minimumTravel * direction;
				steps--;
				
				waste++;
				if(waste > 1000) {
					System.out.println("Wasteedede");
				}
			}
			
			
			path.add(new Point2D.Double(curPos.x, curPos.y));
			
			if(steps < 1) {
				break;
			}
			
			
			rows++;
			
			
		}
		
		
	}
	
	
	protected void paintComponent(Graphics gg) {
		
		super.paintComponent(gg);
		Graphics2D g = (Graphics2D)gg;
		
		/*
		
		// draw the sub-polygons output from the convex split ----------------
		
		g.setStroke(stroke);
		g.setComposite(transparent);
		
				
		for(int i=0; i<convexZones.size(); i++) {
			
			int r = i * 200;
			while(r > 255) {r -= 255;}
			int gr = i * 90;
			while(gr > 255) {gr -= 255;}
			
			g.setColor(new Color(r, gr, 60));
			//g.setColor(Color.RED);
			g.fillPolygon(convexZones.elementAt(i));

		}
		
		// draw the outer boundary --------------------------------------------
		
		
		g.setStroke(zoneStroke);
		g.setColor(Color.CYAN);
		g.setComposite(opaque);
		g.drawPolygon(zone);
		
		
		// draw the generated path --------------------------------------------
		
		g.setStroke(stroke);
		g.setColor(Color.BLACK);
		g.setComposite(opaque);
		
		for(int i=0; i<path.size()-1; i++) {
			
			Point2D.Double p1 = path.elementAt(i);
			Point2D.Double p2 = path.elementAt(i+1);
			
			g.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
			
		}

		*/
		
		
		bc.draw(g);
		
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getSource() == t) {
			this.repaint();
		}
	}
	
	
	
	
}













































/*
 * 
 * 	//discover the closest entry point (a polygon corner) ---------------------------
		
		double minDistance = Double.MAX_VALUE;
		int index = -1;
		
		for(int i=0; i<p.npoints; i++) {
			double distance = Math.sqrt( Math.pow(initPos.x - p.xpoints[i], 2) + 
											Math.pow(initPos.y - p.ypoints[i], 2) );
			
			if(distance < minDistance) {
				minDistance = distance;
				index = i;
			}
		}
		
		curPos.x = p.xpoints[index];
		curPos.y = p.ypoints[index];
		
		// -----------------------------------------------------------------------------
		// get scanline vectors, ie forward, and down (next line)
		
		int nextPoint = index + 1;
		if(nextPoint >= p.npoints)
			nextPoint = 0;
		
		int prevPoint = index - 1;
		if(prevPoint < 0)
			prevPoint = p.npoints-1;
		
		// get a forward vector and normalise it
		forwardVec = new Point2D.Double(p.xpoints[nextPoint] - curPos.x, p.ypoints[nextPoint] - curPos.y);
		double length = length(forwardVec);

		
		forwardVec.x = forwardVec.x / length;
		forwardVec.y = forwardVec.y / length;

		
		downVec = new Point2D.Double(forwardVec.y, -forwardVec.x);
		
		// double check that the 'downVec' actually points into the shape, rather than outside
		
		Point2D.Double compVec = new Point2D.Double(p.xpoints[prevPoint] - curPos.x, p.ypoints[prevPoint] - curPos.y);
		
		double dotProduct = compVec.x * downVec.x + compVec.y * downVec.y;
		
		if(dotProduct < 0) {
			// then the vectors do no point in the same direction (ie totally opposed)
			downVec.x *= -1;
			downVec.y *= -1;
		}
		
		
		// -------------------------------------------------------------------------------
		//start mapping out the path;
		
		path.add(new Point2D.Double(curPos.x, curPos.y));
		
		int rows = 0;
		
		while( rows < 40 ) {
			// travel downwards
			curPos.x += downVec.x * width;
			curPos.y += downVec.y * width;
			
			path.add(new Point2D.Double(curPos.x, curPos.y));
			
			// these next two lines make the 'forwards' direction alternate between rows
			int direction = (rows % 2);
			if(direction == 0) {direction = -1; }
			direction *= -1;
			
			int steps = 0;
			boolean begun = false;
			
			while( p.contains(curPos.x, curPos.y)) {

				curPos.x += forwardVec.x * minimumTravel * direction;
				curPos.y += forwardVec.y * minimumTravel * direction;
				steps++;
			}
			
			curPos.x -= forwardVec.x * minimumTravel * direction;
			curPos.y -= forwardVec.y * minimumTravel * direction;
			
			
			while( !p.contains(curPos.x+downVec.x*width, curPos.y+downVec.y*width) && steps > 0) {
				curPos.x -= forwardVec.x * minimumTravel * direction;
				curPos.y -= forwardVec.y * minimumTravel * direction;
				steps--;
			}
			
			if(steps < 1) {
				//break;
			}
			
			path.add(new Point2D.Double(curPos.x, curPos.y));
			
			rows++;
			
		}
		
		
		
		*/

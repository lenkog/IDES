package presentation;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;

public class Geometry {

	/**
	 * Returns the norm of the given vector.
	 * 
	 * @param vector
	 * @return norm (length) of vector
	 */
	public static double norm(Point2D.Float vector) {
		return Math.sqrt(Math.pow(vector.x, 2) + Math.pow(vector.y, 2));		
	}
	
	/**  
	 * @param p
	 * @return the unit direction vector for vector from (0,0) to v.
	 */
	public static Point2D.Float unit(Point2D.Float p){
		float n = (float)norm(p);
		Point2D.Float p1 = new Point2D.Float(p.x/n, p.y/n);
		return p1;
	}

	/**
	 * @param p1
	 * @param p2
	 * @return the unit direction vector for vector from p1 to p2 i.e. norm(p2 - p1)
	 */
	public static Point2D.Float unitDirectionVector(Point2D.Float p1, Point2D.Float p2){
		return unit(subtract(p2, p1));
	}
	
	/** 
	 * @param v vector with origin at (0,0) and given direction
	 * @return the vector perpendicular to v (rotated 90 degrees clockwise)
	 */
	public static Point2D.Float perp(Point2D.Float v){
		return new Point2D.Float(v.y, -v.x);		
	}
	
	/**
	 * @param v vector with origin at (0,0) and given direction
	 * @param r radians
	 * @return the vector resulting from rotating v by r radians
	 */
	public static Point2D.Float rotate(Point2D.Float v, double r) {
		float c = (float)Math.cos(r);
		float s = (float)Math.sin(r);
		return new Point2D.Float(v.x*c + v.y*s, v.y*c - v.x*s);	
	}
	
	/** 
	 * @param v vector with origin at (0,0) and given direction
	 * @param s the scalar 
	 * @return the result of scaling v by s
	 */
	public static Point2D.Float scale(Point2D.Float v, float s) {		
		return new Point2D.Float(Math.round(v.x * s), Math.round(v.y * s));		
	}	
		
	
	public static Point2D.Float subtract(Point2D.Float a, Point2D.Float v){
		return new Point2D.Float(a.x - v.x, a.y - v.y);		
	}

	public static Point2D.Float subtract(Point a, Point b) {		
		return new Point2D.Float(a.x - b.x, a.y - b.y);	
	}

	/** 
	 * @param a point
	 * @param v direction vector
	 * @return a new point a + v
	 */
	public static Point2D.Float add(Point2D.Float a, Point2D.Float v){
		return new Point2D.Float(a.x + v.x, a.y + v.y);		
	}
	
	/**
	 * 
	 * a � b  =  cos ? 

To use this formula with non-unit vectors: 
(1) normalize each vector, 
(2) compute the dot product, 
(3) take the arc cos to get the angle. 

	 * 
	 * @param v1
	 * @param v2
	 * @return the angle between the given vectors in radians
	 */
	public static double angleBetween(Point2D.Float v1, Point2D.Float v2){
		Point2D.Float unitV1, unitV2;
		unitV1 = unit(v1);
		unitV2 = unit(v2);
		double dot = unitV1.x * unitV2.x + unitV1.y * unitV2.y;				
		return Math.acos(dot);
	}
	
}

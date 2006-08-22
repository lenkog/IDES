package presentation.fsa;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import main.Hub;
import presentation.Geometry;
import presentation.GraphicalLayout;
import presentation.PresentationElement;
import services.cache.Cache;
import services.cache.NotInCacheException;
import services.latex.LatexManager;
import services.latex.LatexRenderException;
import services.latex.LatexUtils;
import util.BentoBox;

/**
 * @author helen bretzke
 */
@SuppressWarnings("serial")
public class GraphLabel extends GraphElement {
	// SJW - The bounds should be calculated on the fly
	// to make sure updates are observed
	// protected Rectangle bounds;
	
	protected Font font;
	protected BufferedImage rendered = null;
	
	// Added by SJW
	private int textMetricsWidth = 0;
	private int textMetricsHeight = 0;
	private static final double DBL_RENDERED_SCALE_WIDTH = 2.0;
	private static final double DBL_RENDERED_SCALE_HEIGHT = 2.25;
	private static final double DBL_NOT_RENDERED_SCALE_WIDTH = 2;
	private static final double DBL_NOT_RENDERED_SCALE_HEIGHT = 2.75;
	private static final int TEXT_MARGIN_WIDTH = 4;
	
	
	public GraphLabel(String text){
		setLayout(new GraphicalLayout(text));
		// TODO change to a dynamic value read from a config file and stored in 
		// SystemVariables? ResourceManager?
		font = new Font("times", Font.ITALIC, 12);
		
	}
	
	public GraphLabel(GraphicalLayout layout){		
		setLayout(layout);		
	}
	
	/**
	 * @param text string to display in this label
	 * @param location the x,y coordinates of the top left corner of this label 
	 */
	public GraphLabel(String text, Point2D location){
		this(text);		
		getLayout().setLocation((float)location.getX(), (float)location.getY());		
	}
	
	/**
	 * TODO decide whether the DrawingBoard is a special kind of Glyph.
	 * 
	 * @param text string to display in this label
	 * @param parent glyph in which this label is displayed
	 * @param location the x,y coordinates of the top left corner of this label
	 */
	public GraphLabel(String text, PresentationElement parent, Point2D location) {	
		this(text, location);		
		setParent(parent);
	}
	
	public void draw(Graphics g) {
		
		if(visible){
			if( (selected || highlighted) && !getText().equals("") ){ //&& parent != null && !parent.isSelected()){
				drawBorderAndTether(g);		
			}
		}
		
		if(LatexManager.isLatexEnabled())
		{
			if(!visible||"".equals(getLayout().getText()))
			{	return; }
			try
			{
				renderIfNeeded();
			}catch(LatexRenderException e)
			{
				LatexManager.handleRenderingProblem();
				return;
			}
			// SJW - Mod to fiddle with positioning
			/*
			((Graphics2D)g).drawImage(rendered, null, 
					(int)layout.getLocation().x,(int)layout.getLocation().y);
			*/
			Rectangle2D renderedBounds = bounds();
			((Graphics2D)g).drawImage(rendered, null, 
				(int)renderedBounds.getX(), (int)renderedBounds.getY());
		}
		else
		{					
			if(highlighted){
				g.setColor(getLayout().getHighlightColor());
			}else if(selected){
				g.setColor(getLayout().getSelectionColor());
			}else{
				g.setColor(getLayout().getColor());
			}
			drawText(g);				
		}		
	}

	/**
	 * Draws the text for this label in the given graphics context.
	 * 
	 * Updates bounds based on font metrics of graphics
	 * and the text to be drawn. 
	 * 
	 * @param g
	 */
	private void drawText(Graphics g) {
		//////////////////////////////////////////////////////////////
		// TODO compute bounds and drawing string with multiple lines
		String[] lines = getText().split("\n");
		if(lines.length > 1){
			// multiple line text
			
		}
		//////////////////////////////////////////////////////////////
		
		// Compute bounds
		g.setFont(font);
		FontMetrics metrics = g.getFontMetrics();
		/*
		 * SJW - Adjusted so these values are held in member variables
		int width = metrics.stringWidth( layout.getText() );
		int height = metrics.getHeight();
		*/
		updateMetrics(metrics);

		/*
		 * SJW - Call bounds() instrad to compute all this
		bounds.setSize(width, height);
		bounds.setLocation(new Point((int)(layout.getLocation().x - width/2), 
				(int)(layout.getLocation().y - height/2)));
		*/
		// Rectangle2D textBounds = bounds();
		
		int x = BentoBox.convertDoubleToInt(
			getLayout().getLocation().x - 
				(textMetricsWidth / DBL_NOT_RENDERED_SCALE_WIDTH));
		int y = BentoBox.convertDoubleToInt(
			getLayout().getLocation().y +
				(textMetricsHeight / DBL_NOT_RENDERED_SCALE_HEIGHT));			
				
		g.drawString(getLayout().getText(), x, y);
	}

	/**
	 * @return
	 */
	public String getText() {
		return getLayout().getText();
	}

	/**
	 * @param g
	 */
	private void drawBorderAndTether(Graphics g) {	
		if(getParent() != null && getParent() instanceof Node) return;
		
		if(selected){
			g.setColor(getLayout().getSelectionColor());
		}else if(highlighted){
			g.setColor(getLayout().getHighlightColor());
		}else{
			g.setColor(getLayout().getColor());
		}
		
		Stroke s = ((Graphics2D)g).getStroke();
		((Graphics2D)g).setStroke(GraphicalLayout.DASHED_STROKE);
			
		Rectangle bounds = bounds();
		((Graphics2D)g).draw(bounds);	// TODO draw border for free labels too
		
		// FIXME only show for edge labels
		// KLUGE instanceof, should have subclasses EdgeLabel and NodeLabel
		if(getParent() != null && getParent() instanceof Edge ){  // draw the tether
						
			Point2D.Double corner = nearestCorner(getParent().getLocation(), bounds);
						
//			g.drawLine((int)bounds().getX(), 
//						(int)bounds().getY(), 
//						(int)getParent().getLocation().x, 
//						(int)getParent().getLocation().y);
			g.drawLine((int)corner.x, (int)corner.y,
						(int)getParent().getLocation().x, 
						(int)getParent().getLocation().y);
		}
		((Graphics2D)g).setStroke(s);		
	}

	/**
	 *  Computes the corner of <code>rect</code> that is nearest to <code>point</code>. 
	 *  
	 *  FIXME Always returns the top-right corner.
	 */
	private Point2D.Double nearestCorner(Point2D.Float point, Rectangle rect)
	{
		Point2D.Double nearest = new Point2D.Double(0, 0);
		
		// upper left
		Point2D.Double corner = new Point2D.Double(rect.getX(), rect.getY());
		
		double distance = point.distance(corner);
		double min = distance;
		nearest.x = corner.x;
		nearest.y = corner.y;

		// bottom left
		corner.y += rect.height;
		distance = point.distance(corner);
				
		if(distance < min) {
			min = distance; 
			nearest.x = corner.x;
			nearest.y = corner.y;
		}
		
		// bottom right
		corner.x += rect.width;		
		distance = point.distance(corner);
		
		if(distance < min) {
			min = distance; 
			nearest.x = corner.x;
			nearest.y = corner.y;
		}
		
		// top right
		corner.y -= rect.height;
		distance = point.distance(corner);
		
		if(distance < min) {
			min = distance; 
			nearest.x = corner.x;
			nearest.y = corner.y;
		}
		
		return nearest;		
	}
	
	public Rectangle bounds() {
		
		Rectangle labelBounds = new Rectangle();
		
		if(getLayout().getText().length() == 0){
			labelBounds.height = 0;
			labelBounds.width = 0;
			labelBounds.x = (int)getLayout().getLocation().x;
			labelBounds.y = (int)getLayout().getLocation().y;
		}
		
		if(LatexManager.isLatexEnabled())
		{
			if(rendered!=null)
			{
				labelBounds.height=rendered.getHeight();
				labelBounds.width=rendered.getWidth();
			}
			else
			{
				// FIXME arbitrary dimensions: has to be recomputed after rendering
				// NOTE if not set to values > zero, causes an update loop because empty labels at location (0,0)
				// are given negative bounds below.
				labelBounds.height=0;
				labelBounds.width=0;
			}
			
			// SJW - Now, update the x and y based on the width and height
			labelBounds.x = BentoBox.convertDoubleToInt(
				getLayout().getLocation().x - 
					(labelBounds.width / DBL_RENDERED_SCALE_WIDTH));
			labelBounds.y = BentoBox.convertDoubleToInt(
				getLayout().getLocation().y - 
					(labelBounds.height / DBL_RENDERED_SCALE_HEIGHT));
		}
		else
		{			
			labelBounds.width = textMetricsWidth;
			labelBounds.height = textMetricsHeight;
			
			// SJW - Now, update the x and y based on the width and height
			labelBounds.x = BentoBox.convertDoubleToInt(
				getLayout().getLocation().x - 
					(labelBounds.width / DBL_NOT_RENDERED_SCALE_WIDTH));
			labelBounds.y = BentoBox.convertDoubleToInt(
				getLayout().getLocation().y - 
					(labelBounds.height / DBL_NOT_RENDERED_SCALE_HEIGHT));
		}		

		return labelBounds;				
	}
	
	public boolean intersects(Point2D p) {		
		return bounds().contains(p);
	}

	public void translate(float x, float y){
		// KLUGE label should store its offset from its parent,
		// parent should be oblivious.  No time to fix properly.
		PresentationElement parent = getParent();
		if(parent != null){			
			Point2D.Float offset = parent.getLayout().getLabelOffset();
			offset.setLocation(offset.x + x, offset.y + y);
			parent.getLayout().setLabelOffset(offset);
		}
		super.translate(x, y);
	}
	
	public void insert(PresentationElement child, long index) {}
	public void insert(PresentationElement g) {}
	public void remove(PresentationElement child) {}
	public PresentationElement child(long index) {	return null; }
	public Iterator children() { return null; }
	
	public void setText(String s){
		if(s==null)
			s="";
		if(!s.equals(getLayout().getText()))
		{
			getLayout().setText(s);
			if(LatexManager.isLatexEnabled())
				try
				{
					render();
				}catch(LatexRenderException e)
				{
					LatexManager.handleRenderingProblem();
					rendered=null;
				}
			else
				rendered=null;
		}
		setDirty(true);
		updateMetrics();
	}

	/**
	 * Renders the label using LaTeX.
	 * @throws LatexRenderException if rendering fails
	 * @see #rendered
	 * @see #renderIfNeeded()
	 */
	public void render() throws LatexRenderException
	{
		dirty=true;
		String label=getLayout().getText();
		if(label==null)
			label="";
		byte[] data=null;
		try
		{
			data=(byte[])Cache.get(getClass().getName()+label);
		}catch (NotInCacheException e)
		{
			data=LatexUtils.labelStringToImageBytes(label);
			Cache.put(getClass().getName()+label,data);
		}
		try
		{
			rendered=ImageIO.read(new ByteArrayInputStream(data));			
		}
		catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * If the label has not been rendered yet, it gets rendered using LaTeX
	 * @throws LatexRenderException if rendering fails
	 * @see #rendered
	 * @see #render()
	 */
	public void renderIfNeeded() throws LatexRenderException
	{
		if(rendered==null)
			render();
	}
	
	/**
	 * This method is responsible for creating a string that contains
	 * an appropriate (depending on the type) representation of this
	 * edge.
	 *  
	 * @param selectionBox The area being selected or considered
	 * @param exportType The export format
	 * @return String The string representation
	 * 
	 * @author Sarah-Jane Whittaker
	 */
	public String createExportString(Rectangle selectionBox, int exportType)
	{
		String exportString = "";
		GraphicalLayout labelLayout = getLayout();
		Rectangle2D b = bounds();
		Rectangle labelBounds = new Rectangle((int)b.getX(), (int)b.getY(), (int)b.getWidth(), (int)b.getHeight());
		CircleNode parentNode = null;
		GraphicalLayout nodeLayout = null;
		
		// This is taken from Mike Wood - thanks, Mike!!!
		String safeLabel = labelLayout.getText();
		safeLabel = BentoBox.replaceAll(safeLabel, "\\\\" 
			+ BentoBox.STR_ASCII_STANDARD_RETURN, "\\\\ ");
		safeLabel =  BentoBox.replaceAll(safeLabel, "\\\\ " 
			+ BentoBox.STR_ASCII_STANDARD_RETURN, "\\\\ ");
		safeLabel =  BentoBox.replaceAll(safeLabel,
				BentoBox.STR_ASCII_STANDARD_RETURN + BentoBox.STR_ASCII_STANDARD_RETURN, "\\\\ ");
		safeLabel = BentoBox.replaceAll(safeLabel, BentoBox.STR_ASCII_STANDARD_RETURN, " ");

		// Make sure this node is contained within the selection box
		if (! (selectionBox.contains(labelBounds)))
		{
			System.out.println("Label  " + labelBounds 
				+ " outside bounds " + selectionBox);
			return exportString;
		}
		
		// Adjust the bounds for PSTricks export		
		labelBounds.x = BentoBox.convertDoubleToInt(
			getLayout().getLocation().x - (getLayout().getLocation().x * 0.00001));
		labelBounds.y = BentoBox.convertDoubleToInt(
			getLayout().getLocation().y 
				+ (getLayout().getLocation().y * getLayout().getLocation().y * 0.00002));
		
		if (exportType == GraphExporter.INT_EXPORT_TYPE_PSTRICKS)
		{
			// Don't forget the font size!!!
			exportString = "  \\rput(" 
				+ (labelBounds.x - selectionBox.x) + "," 
				+ (selectionBox.y + selectionBox.height - labelBounds.y) + "){\\parbox{" 
				+ labelBounds.width + "pt}{\\fontsize{"
				+ getLatexFontSize() + "}{"
				+ BentoBox.roundDouble(getLatexFontSize() * 
					GraphExporter.DBL_PSTRICKS_FONT_BASELINE_FACTOR, 2)
				+ "} \\selectfont \\begin{center}" 
				+ safeLabel + "\\end{center}}}\n";		
		}
		else if (exportType == GraphExporter.INT_EXPORT_TYPE_EPS)
		{	
			// LENKO!!!
		}

		return exportString;
	}
	
	public void updateLayout(String text, Point2D.Float location)
	{
		setText(text);
		if(!location.equals(getLayout().getLocation()))
		{
			getLayout().setLocation(location.x,location.y);
			setDirty(true);
		}
	}
	
	/**
	 * 
	 */
	public void updateMetrics()
	{
		JFrame mainWindow = Hub.getMainWindow();
		Graphics mainGraphics = mainWindow.getGraphics();
		FontMetrics mainMetrics = mainGraphics.getFontMetrics(font);
		updateMetrics(mainMetrics);
	}
	
	/**
	 * 
	 */
	private void updateMetrics(FontMetrics metrics)
	{		
		textMetricsWidth = metrics.stringWidth( getLayout().getText() );
		textMetricsHeight = metrics.getHeight();
	}
	
	public static float getLatexFontSize()
	{
		return LatexManager.getFontSize();
	}
}
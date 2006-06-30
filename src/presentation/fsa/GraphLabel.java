package presentation.fsa;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;

import presentation.GraphicalLayout;
import presentation.PresentationElement;
import services.cache.Cache;
import services.cache.NotInCacheException;
import services.latex.LatexManager;
import services.latex.LatexRenderException;
import services.latex.LatexUtils;
import util.BentoBox;

/**
 * TODO Change so that doesn't extend label; waste of space and rounds the location to int coords.
 * 
 * @author helen
 *
 */
@SuppressWarnings("serial")
public class GraphLabel extends GraphElement {
	protected Rectangle bounds;	
	protected PresentationElement parent = null;  // either the graph, a node or an edge	
	protected Font font;
	protected BufferedImage rendered = null;
	
	// Added by SJW
	private int textMetricsWidth = 0;
	private int textMetricsHeight = 0;
	private static final double DBL_RENDERED_SCALE_WIDTH = 2.3;
	private static final double DBL_RENDERED_SCALE_HEIGHT = 2.25;
	private static final double DBL_NOT_RENDERED_SCALE_WIDTH = 2;
	private static final double DBL_NOT_RENDERED_SCALE_HEIGHT = 2.75;
	
	
	public GraphLabel(String text){
		layout = new GraphicalLayout(text);
		// TODO change to a dynamic value read from a config file and stored in 
		// SystemVariables? ResourceManager?
		font = new Font("times", Font.ITALIC, 12);
		bounds = new Rectangle();
	}
	
	public GraphLabel(GraphicalLayout layout){		
		this.layout = layout;	
		bounds = new Rectangle();
	}
	
	/**
	 * @param text string to display in this label
	 * @param location the x,y coordinates of the top left corner of this label 
	 */
	public GraphLabel(String text, Point2D location){
		this(text);		
		layout.setLocation((float)location.getX(), (float)location.getY());		
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
		this.parent = parent;
	}
	
	public void draw(Graphics g) {
		
		if(LatexManager.isLatexEnabled())
		{
			if(!visible||"".equals(layout.getText()))
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
			((Graphics2D)g).drawImage(rendered, null, 
				(int) (layout.getLocation().x - 
					(bounds.width / DBL_RENDERED_SCALE_WIDTH)),
				(int) (layout.getLocation().y - 
					(bounds.height / DBL_RENDERED_SCALE_HEIGHT)));
		}
		else
		{			
			if(visible){
				if(selected && !getText().equals("") ){ //&& parent != null && !parent.isSelected()){
					drawBorderAndTether(g);					
				}else if(highlighted){
					g.setColor(layout.getHighlightColor());
				}else{
					g.setColor(layout.getColor());
				}		
				drawText(g);				
			}
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
		textMetricsWidth = metrics.stringWidth( layout.getText() );
		textMetricsHeight = metrics.getHeight();

		/*
		 * SJW - Call bounds() instrad to compute all this
		bounds.setSize(width, height);
		bounds.setLocation(new Point((int)(layout.getLocation().x - width/2), 
				(int)(layout.getLocation().y - height/2)));
		*/
		bounds();
		
		// Location to draw string
		/*
		int x = (int)layout.getLocation().x - (width / 2);
		int y = (int)layout.getLocation().y + metrics.getAscent()/2;			
		 */
		int x = BentoBox.convertDoubleToInt(
			layout.getLocation().x - 
				(textMetricsWidth / DBL_NOT_RENDERED_SCALE_WIDTH));
		int y = BentoBox.convertDoubleToInt(
			layout.getLocation().y +
				(textMetricsHeight / DBL_NOT_RENDERED_SCALE_HEIGHT));			
		g.drawString(layout.getText(), x, y);
	}

	/**
	 * @return
	 */
	private String getText() {
		return layout.getText();
	}

	/**
	 * @param g
	 */
	private void drawBorderAndTether(Graphics g) {		
		g.setColor(layout.getSelectionColor());
		Rectangle r = bounds();
		r.width *= 1.1;
		r.height *= 1.1;
		Stroke s = ((Graphics2D)g).getStroke();
		((Graphics2D)g).setStroke(GraphicalLayout.DASHED_STROKE);
		((Graphics2D)g).draw(bounds());
		
		// FIXME not showing
//		if(parent != null ){  // draw the tether
//			g.drawLine((int)layout.getLocation().x, 
//						(int)layout.getLocation().y, 
//						(int)parent.getLayout().getLocation().x, 
//						(int)parent.getLayout().getLocation().y);
//		}
		((Graphics2D)g).setStroke(s);		
	}

	public Rectangle bounds() {
		if(LatexManager.isLatexEnabled())
		{
			if(rendered!=null)
			{
				bounds.height=rendered.getHeight();
				bounds.width=rendered.getWidth();
			}
			else
			{
				// FIXME arbitrary dimensions: has to be recomputed after rendering
				bounds.height=10;
				bounds.width=10;
			}
			
			// SJW - Now, update the x and y based on the width and height
			bounds.x = BentoBox.convertDoubleToInt(
				layout.getLocation().x - 
					(bounds.width / DBL_RENDERED_SCALE_WIDTH));
			bounds.y = BentoBox.convertDoubleToInt(
				layout.getLocation().y - 
					(bounds.height / DBL_RENDERED_SCALE_HEIGHT));	
		}
		else
		{
			// Lenko writes: TODO update bounds on label change
			// NOTE Unless we use deprecated getFontMetrics method, we
			// have to compute the bounds until via a graphics context
			// object in the draw method.
			
			// Sarah writes: OR we can compute the height and width in the
			// draw phase, store them and use them here!
			/*
			if(bounds.getWidth() == 0 || bounds.getHeight() == 0){
				Toolkit tk = Toolkit.getDefaultToolkit();
				FontMetrics metrics = tk.getFontMetrics(font);
				bounds.setSize(metrics.stringWidth(layout.getText()), metrics.getHeight() );
				bounds.setLocation(new Point((int)(layout.getLocation().x - bounds.width/2), 
											(int)(layout.getLocation().y - bounds.height/2)));			
			}
			*/
			bounds.width = textMetricsWidth;
			bounds.height = textMetricsHeight;
			
			// SJW - Now, update the x and y based on the width and height
			bounds.x = BentoBox.convertDoubleToInt(
				layout.getLocation().x - 
					(bounds.width / DBL_NOT_RENDERED_SCALE_WIDTH));
			bounds.y = BentoBox.convertDoubleToInt(
				layout.getLocation().y + 
					(bounds.height / DBL_NOT_RENDERED_SCALE_HEIGHT));	
		}		

		return bounds;				
	}
	
	public boolean intersects(Point2D p) {		
		return bounds().contains(p);
	}

	public void insert(PresentationElement child, long index) {}
	public void insert(PresentationElement g) {}
	public void remove(PresentationElement child) {}
	public PresentationElement child(int index) {	return null; }
	public Iterator children() { return null; }
	
	public PresentationElement parent() {		
		return parent;
	}

	public void setText(String s){
		if(s==null)
			s="";
		if(!s.equals(layout.getText()))
		{
			layout.setText(s);
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
		String label=layout.getText();
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
		Rectangle labelBounds = bounds();
		
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
		
		if (exportType == GraphExporter.INT_EXPORT_TYPE_PSTRICKS)
		{
			exportString = "  \\rput(" 
				+ (labelBounds.x - selectionBox.x) + "," 
				+ (selectionBox.y + selectionBox.height - labelBounds.y) + "){\\parbox{" 
				+ labelBounds.width + "pt}{\\begin{center}" 
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
		if(!location.equals(layout.getLocation()))
			layout.setLocation(location.x,location.y);
		// SJW
		bounds();
		setDirty(true);
	}
}
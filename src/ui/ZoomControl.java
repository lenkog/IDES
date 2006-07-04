/**
 * 
 */
package ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import main.Hub;

/**
 * @author lenko
 *
 */
public class ZoomControl extends JComboBox implements ActionListener {

//	/**
//	 * @param arg0
//	 */
//	public ZoomControl(ComboBoxModel arg0) {
//		super(arg0);
//
//	}
//
//	/**
//	 * @param arg0
//	 */
//	public ZoomControl(Object[] arg0) {
//		super(arg0);
//
//	}
//
//	/**
//	 * @param arg0
//	 */
//	public ZoomControl(Vector<?> arg0) {
//		super(arg0);
//
//	}

	protected int zoomValue=100;
	
	protected static final String[] presets = { "10 %", "25 %", "50 %", "100 %", "200 %" };
	
	/**
	 * 
	 */
	public ZoomControl() {
		super(presets);
		setEditable(true);
		setSelectedIndex(3);
		addActionListener(this);
		setMaximumSize(new Dimension(60,getPreferredSize().height));
		setPreferredSize(new Dimension(60,getPreferredSize().height));
	}

	public void actionPerformed(ActionEvent e)
	{
		String value=getEditor().getItem().toString();
		value=value.split(" ")[0];
		if(value.endsWith("%"))
			value=value.substring(0,value.length()-1);
		try
		{
			int newZoomValue=(int)Float.parseFloat(value);
			if(newZoomValue<=0)
				newZoomValue=100;
			commitZoom(newZoomValue);
		}catch(NumberFormatException ex){
			commitZoom(zoomValue);
		}
	}
	
	public float getZoom()
	{
		return (float)zoomValue/100F;
	}
	
	public void setZoom(float z)
	{
		if(z<0)
			z=0;
		commitZoom((int)(z*100));
	}
	
	private void commitZoom(int z)
	{
		if(z!=zoomValue)
		{
			zoomValue=z;
			((MainWindow)Hub.getMainWindow()).getDrawingBoard().update();
		}
		getEditor().setItem(""+zoomValue+" %");
	}
}
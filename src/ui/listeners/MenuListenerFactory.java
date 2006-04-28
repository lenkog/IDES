package ui.listeners;

import io.FileOperations;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;

import main.SystemVariables;
import model.fsa.FSAModel;

import ui.UIStateModel;
import ui.command.Command;
import ui.command.CommandHistory;
import ui.command.CopyCommand;
import ui.command.CutCommand;
import ui.command.DeleteCommand;

public class MenuListenerFactory {

  public static ActionListener makeCopyListener () {
	  return new ActionListener() {
		  public void actionPerformed(ActionEvent arg0) {			  			
			  Command cmd = new CopyCommand("selected item", "drawing area"); 
			  UIStateModel.instance().getCommandHistory().add(cmd);
			  cmd.execute();	  
		  }
	  };
  }

  public static ActionListener makeDeleteListener () {
	  return new ActionListener() {
		  public void actionPerformed(ActionEvent arg0) {			  	
			  Command cmd = new DeleteCommand("selected item", "drawing area"); 
			  UIStateModel.instance().getCommandHistory().add(cmd);
			  cmd.execute();	  
		  }
	  };
  }
  
  public static ActionListener makeCutListener () {
	  return new ActionListener() {
		  public void actionPerformed(ActionEvent arg0) {			  			
			  Command cmd = new CutCommand("selected item", "drawing area"); 
			  UIStateModel.instance().getCommandHistory().add(cmd);
			  cmd.execute();
		  }
	  };
  }

  /**
   * Note that file operations do not go into the command history
   * for undoing etc.
   * 
   * @return a listener for all file menu items
   */
  public static ActionListener makeFileMenuListener() {
	  return new ActionListener() {
		  public void actionPerformed(ActionEvent arg0) {			  			
			  JMenuItem item = (JMenuItem)arg0.getSource();			  
			  Container c = item.getParent();
			  JFileChooser chooser = new JFileChooser();
			  FSAModel des = null;
			  int returnVal = chooser.showOpenDialog(c);
			  if(returnVal == JFileChooser.APPROVE_OPTION) {
				  File f = chooser.getSelectedFile();	    	
				  des = FileOperations.openSystem(f);
			  	}
			  // figure out which file menu item was selected
			  //if(item.getName().equals(""))
			  // For now just open an existing system			  
			  if(des != null){
				  UIStateModel.instance().setDESModel(des);			  			  
				  // refresh the views
				  UIStateModel.instance().refresh();
			  }
		  }
	  };
	  
  }
  
}


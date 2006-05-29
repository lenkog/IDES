package ui.command;

import javax.swing.undo.UndoableEdit;

import org.pietschy.command.ActionCommand;
import org.pietschy.command.undo.UndoableActionCommand;

public class EditCommands {

	// ??? Should this set of commands share a common static context instance?
	
	/* Does copy need to be undoable?
	 * 
	 */
	public static class CopyCommand extends ActionCommand {

		private Object element; 
		private Object context;
		private Object buffer; // ???
		
		/**
		 * Creates a command that, when executed, will copy 
		 * <code>element</code> from the given context.
		 * 
		 * The given element could be a group of elements.
		 * 
		 * @param element
		 * @param context
		 */
		public CopyCommand(Object element, Object context) {
			super("copy.command");
			this.element = element;
			this.context = context;
		}
		
		public void handleExecute() {
			// TODO Auto-generated method stub
			// context.remove(element);
			System.out.println("Copy acts as its own buffer, \n but where should paste look for the buffer?");		
		}
	}

	
	public static class PasteCommand extends UndoableActionCommand {

		@Override
		protected UndoableEdit performEdit() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	
	
	public static class CutCommand extends UndoableActionCommand {

		private Object element; 
		private Object context;
		
		/**
		 * Creates a command that, when executed, will cut 
		 * <code>element</code> from the given context.
		 * 
		 * @param element
		 * @param context
		 */
		public CutCommand(Object element, Object context) {
			this.element = element;
			this.context = context;
		}

		@Override
		protected UndoableEdit performEdit() {
			// TODO Auto-generated method stub
			System.out.println("Cut " + element + " from the " + context + ".");
			return null;
		}

	}
	

	
	/**
	 * Represent a user issued command to delete an element of the graph.
	 * ??? What about deleting elements of a text label? 
	 * 
	 * @author helen bretzke
	 *
	 */
	public static class DeleteCommand extends UndoableActionCommand {
		
		private Object element;	 // TODO decide on type, GraphElement composite type?
		private Object context;  // Does this need to be stored?
		
		/**
		 * Creates a command that, when executed, will cut 
		 * <code>element</code> from the given context.
		 * 
		 * @param element
		 * @param context
		 */
		public DeleteCommand(Object element, Object context) {
			this.element = element;
			this.context = context;
		}		

		@Override
		protected UndoableEdit performEdit() {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

	
}
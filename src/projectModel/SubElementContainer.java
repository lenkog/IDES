package projectModel;

import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author Axel Gottlieb Michelsen
 * 
 * This class is the superclass of elements in an automaton (suprise suprise) At
 * the moment it holds functionality for manipulating subelements of an element,
 * e.g. observable, controllable. (Just make sure you type the names of the
 * attributes in the same every time.)
 * 
 */
public class SubElementContainer implements Cloneable{
    private Hashtable<String, SubElement> subElementList;
    private boolean initialized = false;

    /**
     * constructs a new empty subelementcontainer. (doh)
     */
    public SubElementContainer(){
    }
    
    /**
     * constructs a clone of the given subelementcontainer sec.
     * @param sec the subelementcontainer to clone.
     */
    public SubElementContainer(SubElementContainer sec){
        if(sec.initialized){
            this.initialize();
            subElementList = new Hashtable<String, SubElement>();
            Enumeration<SubElement> see = sec.getSubElements();
            while(see.hasMoreElements()){
                this.addSubElement(new SubElement(see.nextElement()));
            }
        }
    }
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public SubElementContainer clone(){
        return new SubElementContainer(this);
    }

    private void initialize(){
        initialized = true;
        subElementList = new Hashtable<String, SubElement>(5);
    }
    private void deinitialize(){
        initialized = false;
        subElementList = null;
    }
    
    /**
     * returns the subelements in this subelementcontainer.
     * @return all sublements in this subelementcontainer.
     */
    public Enumeration<SubElement> getSubElements(){
        return initialized ? subElementList.elements() : new Hashtable<String, SubElement>().elements();
    }

    /**
     * returns a subelement with the given name, or null if it is not
     * in this subelementcontainter.
     * @param aName the name of the subelement.
     * @return the subelement with the gibven name.
     */
    public SubElement getSubElement(String aName){
        return initialized ? subElementList.get(aName) : null;
    }

    /**
     * adds a subelement to the subelementcontainer.
     * If a subelement with the same name as the subelement that is about 
     * to be added allready exist in this subelementcontainer
     * the existing is overridden by the new.
     * @param s the subelement to add. 
     */
    public void addSubElement(SubElement s){
        if(!initialized) initialize();
        subElementList.put(s.getName(), s);
    }

    /**
     * removes a subelement from the subelementcontainer.
     * @param aName the name of the subelement to remove.
     */
    public void removeSubElement(String aName){
        if(initialized){
            subElementList.remove(aName);
            if(subElementList.size() == 0) deinitialize();
        }
    }

    /**
     * returns true if a subelement with the given name exist
     * in this subelementcontainer.
     * @param aName the name of the subelement.
     * @return true if this sublementcontainer contains a subelement with the given name.
     */
    public boolean hasSubElement(String aName){
        return initialized && subElementList.containsKey(aName);
    }

    /**
     * returns true if this subelementcontainer is empty.
     * @return true if this subelementcontainer is empty.
     */
    public boolean isEmpty(){
        return !initialized || subElementList.isEmpty();
    }

    /**
     * Prints this object and all subelements of this objects to the
     * printsstream as XML.
     * @param ps the printstream this object should be printet to.
     * @param indent the indentation this object should have.
     */
    public void toXML(PrintStream ps, String indent){
        if(!initialized) return;
        Enumeration<SubElement> see = subElementList.elements();
        while(see.hasMoreElements())
            see.nextElement().toXML(ps, indent);
    }
}

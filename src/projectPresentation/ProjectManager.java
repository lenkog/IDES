/**
 * 
 */
package projectPresentation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;

import projectModel.*;

/**
 * @author edlund
 * 
 */
public class ProjectManager implements ProjectPresentation {

    private Project project = null;

    private boolean unsaved = false;

    public void newProject(String name) {
        project = new Project(name);
        unsaved = true;
    }

    public boolean isProjectOpen() {
        return (project != null);
    }

    public void setProjectName(String name) {
        if (project != null) {
            project.setName(name);
        }
        unsaved = true;
    }

    public String getProjectName() {
        if (project != null) {
            return project.getName();
        }
        return null;
    }

    public String openProject(File file) {
        ProjectParser pp = new ProjectParser();

        project = pp.parse(file);
        return pp.getParsingErrors();
    }

    public String openAutomaton(File file, String name) {
        AutomatonParser ap = new AutomatonParser();
        Automaton automaton = ap.parse(file);
        automaton.setName(name);
        project.addAutomaton(automaton);
        return ap.getParsingErrors();

    }

    public String[] getAutomataNames() {
        LinkedList<Automaton> al = project.getAutomata();
        String[] sa = new String[al.size()];
        Iterator<Automaton> ai = al.iterator();
        int i = 0;
        while (ai.hasNext()) {
            Automaton a = ai.next();
            if (a != null) {
                sa[i++] = a.getName();
            } else
                i++;
        }
        return sa;
    }

    public void setAutomatonName(String oldName, String newName) {
        project.getAutomatonByName(oldName).setName(newName);
        unsaved = true;
    }

    private PrintStream getPrintStream(File file) {
        PrintStream ps = null;
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ioe) {
                System.err
                        .println("ProjectManager: unable to create file, message: "
                                + ioe.getMessage());
                return null;
            }
        }
        if (!file.isFile()) {
            System.err.println("ProjectManager: " + file.getName()
                    + " is no file. ");
            return null;
        }
        if (!file.canWrite()) {
            System.err.println("ProjectManager: can not write to file: "
                    + file.getName());
            return null;
        }
        try {
            ps = new PrintStream(file);
        } catch (FileNotFoundException fnfe) {
            System.out.println("ProjectManager: file disapeared, message: "
                    + fnfe.getMessage());
            return null;
        }
        return ps;
    }

    public void saveProject(String path) {
        File file = new File(path, project.getName() + ".xml");
        PrintStream ps = getPrintStream(file);
        if (ps == null)
            return;
        project.toXML(ps);
        Iterator<Automaton> ai = project.getAutomata().iterator();
        while (ai.hasNext()) {
            Automaton a = ai.next();
            saveAutomaton(a, path);
        }
    }

    public void saveAutomaton(Automaton a, String path) {
        File file = new File(path, a.getName() + ".xml");
        PrintStream ps = getPrintStream(file);
        if (ps == null)
            return;
        a.toXML(ps);
    }

    public void addAutomaton(String name) {
        project.addAutomaton(new Automaton(name));
        unsaved = true;
    }

    public boolean hasUnsavedData() {
        return unsaved;
    }

    public void setUnsavedData(boolean state) {
        unsaved = state;
    }

    public void deleteAutomatonByName(String name) {
        project.removeAutomaton(project.getAutomatonByName(name));
    }

    public String removeFileName(String name) {
        return ParsingToolbox.removeFileType(name);
    }
    
    public Automaton getAutomatonByName(String name){
        return project.getAutomatonByName(name);
    }
    
    
    public void accesible(String source){

        Automaton sourceAutomaton = getAutomatonByName(source);
        
        //create a queue
        LinkedList<State> searchQue = new LinkedList<State>();
        
        //create a new automaton
        Automaton result = new Automaton("Accesible(" + sourceAutomaton.getName() + ")");

        //find initial state  mark as reached, copy it to the new automaton and add it to the que
        Iterator<State> stateIterator = sourceAutomaton.getStateIterator();
        State state;
        while(stateIterator.hasNext()){
            state = stateIterator.next();
            
            if(state.getSubElement("properties").getSubElement("initial").getChars().equals("true")){
                searchQue.add(state);
                result.addState(new State(state));
                state.addSubElement(new SubElement("reached"));
            }
        }
        
        
        Iterator<Transition> transitionIterator = null;
        //loop while que not empty
        while(!searchQue.isEmpty()){
            //take head of que
            state = searchQue.removeFirst();
            transitionIterator = state.getSourceTransitionsListIterator();
            Transition transition = null;
            // loop while the state has more transitions
            while(transitionIterator.hasNext()){
                transition = transitionIterator.next();
                //if the state is not reached, mark it as reached and copy it to the new automaton, add it to the que
                if(!transition.getTarget().hasSubElement("reached")){
                    result.addState(new State(transition.getTarget()));
                    transition.getTarget().addSubElement(new SubElement("reached"));
                    searchQue.add(transition.getTarget());
                }
                
                // copy the transition
               //result.addTransition(new Transition(transition));
            }
 
        }

        
        //clean up
        stateIterator = sourceAutomaton.getStateIterator();
        while(stateIterator.hasNext()){
            stateIterator.next().removeSubElement("reached");
        }

       //whatever copied to the new automaton is the accesible version of the automaton
        
       project.addAutomaton(result);
    }
    
    
}

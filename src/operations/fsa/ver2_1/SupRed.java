package operations.fsa.ver2_1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import main.Hub;
import model.ModelManager;
import model.fsa.FSAEvent;
import model.fsa.FSAModel;
import pluggable.operation.Operation;

public class SupRed implements Operation {

	public final static String NAME="supervisor reduction (Grail)";

	/* (non-Javadoc)
	 * @see pluggable.operation.Operation#getName()
	 */
	public String getName() {
		return NAME;
	}

	/* (non-Javadoc)
	 * @see pluggable.operation.Operation#getNumberOfInputs()
	 */
	public int getNumberOfInputs() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see pluggable.operation.Operation#getTypeOfInputs()
	 */
	public Class[] getTypeOfInputs() {
		return new Class[]{FSAModel.class,FSAModel.class};
	}

	/* (non-Javadoc)
	 * @see pluggable.operation.Operation#getDescriptionOfInputs()
	 */
	public String[] getDescriptionOfInputs() {
		return new String[]{"Plant","Supervisor"};
	}

	/* (non-Javadoc)
	 * @see pluggable.operation.Operation#getNumberOfOutputs()
	 */
	public int getNumberOfOutputs() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see pluggable.operation.Operation#getTypeOfOutputs()
	 */
	public Class[] getTypeOfOutputs() {
		return new Class[]{FSAModel.class};
	}

	/* (non-Javadoc)
	 * @see pluggable.operation.Operation#getDescriptionOfOutputs()
	 */
	public String[] getDescriptionOfOutputs() {
		return new String[]{"reduced supervisor"};
	}

	/* (non-Javadoc)
	 * @see pluggable.operation.Operation#perform(java.lang.Object[])
	 */
	public Object[] perform(Object[] inputs) {
		exportGrail((FSAModel)inputs[0],new File("PLT"));
		exportGrail((FSAModel)inputs[1],new File("SUP"));
		Set<FSAEvent> unctrl=new TreeSet<FSAEvent>();
		for(FSAEvent e:((FSAModel)inputs[1]).getEventSet())
		{
			if(!e.isControllable())
			{
				unctrl.add(e);
			}
		}
		try
		{
			Process p=Runtime.getRuntime().exec("fmsupred PLT SUP");
            InputStream stdin = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(stdin);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            BufferedWriter out=new BufferedWriter(new FileWriter("RED"));
            while ( (line = br.readLine()) != null)
                out.write(line+"\n");
            out.close();
			p.waitFor();
		}catch(Exception e){e.printStackTrace();}
		FSAModel a=importGrail(new File("RED"));
		for(FSAEvent e:a.getEventSet())
		{
			if(unctrl.contains(e))
			{
				e.setControllable(false);
			}
			else
			{
				e.setControllable(true);
			}
		}
		return new Object[]{a};
	}
	
	public static void exportGrail(FSAModel a, File file)
	{
		String fileContents="";
    	for(Iterator<model.fsa.FSAState> i=a.getStateIterator();i.hasNext();)
    	{
    		model.fsa.FSAState s=i.next();
    		if(s.isInitial())
    		{
    			fileContents+="(START) |- "+s.getId()+"\n";
    		}
    		if(s.isMarked())
    		{
    			fileContents+=""+s.getId()+" -| (FINAL)\n";
    		}
    		for(Iterator<model.fsa.FSATransition> j=s.getSourceTransitionsListIterator();j.hasNext();)
    		{
    			model.fsa.FSATransition t=j.next();
    			fileContents+=""+s.getId()+" "+(t.getEvent()==null?"NULL":t.getEvent().getSymbol())+" "+t.getTarget().getId()+"\n";
    		}
    	}
    	
		FileWriter latexWriter = null;
				
		if (fileContents == null)
		{
			return;
		}
		
		try
		{
			latexWriter = new FileWriter(file);
			latexWriter.write(fileContents);
			latexWriter.close();
		}
		catch (IOException fileException)
		{
			Hub.displayAlert(Hub.string("problemLatexExport")+file.getPath());
		}
	}
	
	public static FSAModel importGrail(File file)
	{
		FSAModel a=null;
    	java.io.BufferedReader in=null;
    	try
    	{
    		in=new java.io.BufferedReader(new java.io.FileReader(file));
    		a=ModelManager.createModel(FSAModel.class,file.getName());
    		long tCount=0;
    		long eCount=0;
    		java.util.Hashtable<String,Long> events=new java.util.Hashtable<String, Long>();
    		String line;
    		while((line=in.readLine())!=null)
    		{
    			String[] parts=line.split(" ");
    			if(parts[0].startsWith("("))
    			{
    				long sId=Long.parseLong(parts[2]);
    				model.fsa.ver2_1.State s=(model.fsa.ver2_1.State)a.getState(sId);
    				if(s==null)
    				{
    					s=new model.fsa.ver2_1.State(sId);
    					a.add(s);
    				}
    				s.setInitial(true);
    			}
    			else if(parts[2].startsWith("("))
    			{	    				
    				long sId=Long.parseLong(parts[0]);
    				model.fsa.ver2_1.State s=(model.fsa.ver2_1.State)a.getState(sId);
    				if(s==null)
    				{
    					s=new model.fsa.ver2_1.State(sId);
    					a.add(s);
    				}
    				s.setMarked(true);
    			}
    			else
    			{
    				long sId1=Long.parseLong(parts[0]);
    				model.fsa.ver2_1.State s1=(model.fsa.ver2_1.State)a.getState(sId1);
    				if(s1==null)
    				{
    					s1=new model.fsa.ver2_1.State(sId1);
    					a.add(s1);
    				}
    				long sId2=Long.parseLong(parts[2]);
    				model.fsa.ver2_1.State s2=(model.fsa.ver2_1.State)a.getState(sId2);
    				if(s2==null)
    				{
    					s2=new model.fsa.ver2_1.State(sId2);
    					a.add(s2);
    				}
    				model.fsa.ver2_1.Event e=null;
    				Long eId=events.get(parts[1]);
    				if(eId==null)
    				{
    					e=new model.fsa.ver2_1.Event(eCount);
    					e.setSymbol(parts[1]);
    					e.setObservable(true);
    					e.setControllable(true);
    					eCount++;
    					a.add(e);
    					events.put(parts[1], new Long(e.getId()));
    				}
    				else
    					e=(model.fsa.ver2_1.Event)a.getEvent(eId.longValue());
    				model.fsa.ver2_1.Transition t=new model.fsa.ver2_1.Transition(tCount,s1,s2,e);
    				a.add(t);
    				tCount++;
    			}
    		}
    	}catch(java.io.IOException e)
    	{
    		Hub.displayAlert(Hub.string("cantParseImport")+file);
    	}
    	catch(RuntimeException e)
    	{
    		Hub.displayAlert(Hub.string("cantParseImport")+file);
    	}
    	finally
    	{
    		try
    		{
    			if(in!=null)
    				in.close();
    		}catch(java.io.IOException e){}
    	}
    	return a;
	}
}
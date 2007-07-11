/**
 * 
 */
package pluggable.io;
import main.Annotable;
import main.Hub;
import model.DESModel;
import model.fsa.FSAModel;
import io.fsa.ver2_1.FileOperations;
import io.fsa.ver2_1.XMLexporter;
import io.IOUtilities;
import java.io.FileOutputStream;
import io.ParsingToolbox;

import io.WrappedPrintStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import pluggable.io.IOPluginManager;
import pluggable.io.FileIOPlugin;
import io.AbstractParser;
/**
 * @author christiansilvano
 * \TODO make it thread-safe
 */
public final class IOCoordinator{
	//Singleton instance:
	private static IOCoordinator instance = null;
	XmlParser xmlParser = null;
	private IOCoordinator()
	{    
	}
	
	public static IOCoordinator getInstance()
	{
		if (instance == null)
		{
			instance = new IOCoordinator(); 
		}
		return instance;
	}
		
	public boolean save(DESModel model, File file)
	{	
		//Read the dataType from the plugin modelDescriptor
		String type = model.getModelDescriptor().getIOTypeDescription();

		//Get the plugin capable of saving a model of the type "type"
		//Currently there must be just one data saver for a model type.
		FileIOPlugin dataSaver = IOPluginManager.getInstance().getDataSaver(type);

		//Get all the plugins capable of saving the metaTags for ""type""
		//There can be several different meta savers for a specific data type.
		Set<FileIOPlugin> metaSavers = IOPluginManager.getInstance().getMetaSavers(type);
 		Iterator<FileIOPlugin> metaIt = metaSavers.iterator();

		//Open  ""file"" and start writing the header of the IDES file format
		//TODO: IMPLEMENT A WRAPPER TO THE PRINTSTREAM< OVERRIDING close()
 		//TODO: MAKE ps AN OUTPUT STREAM
 		WrappedPrintStream ps = new WrappedPrintStream(IOUtilities.getPrintStream(file));

 		
 	
        ps.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        ps.println("<model version=\"2.1\" type=\""+ type + "\" id=\""+model.getId()+"\">");
        ps.println("<data>");
	
       //Make the dataSaver plugin save the data information on the 
        //file (protect the original content)
		dataSaver.saveData(ps, model, file.getParentFile());
        //The data information is stored: 
		ps.println("</data>");
        //3 - Make the metaSavers one by one save the meta information on the file
		while(metaIt.hasNext())
		{
			FileIOPlugin plugin = metaIt.next();
			Iterator<String> tags = plugin.getMetaTags(type).iterator();
			while(tags.hasNext())
			{
				String tag = tags.next();
				ps.println("<meta tag=\""+ tag +"\" version=\"2.1\">");
				plugin.saveMeta(ps, model, type, tag);
				ps.println("</meta>");
			}
		}
        ps.println("</model>");
		ps.closeWrappedPrintStream();
        //4 - close the file.
		//5 - Return true if the operation was a success, otherwise return false.
        //TODO THROW IO EXCEPTION OR PLUGIN EXCEPTIONS IF SOMETHING HAPPENS
        //TODO SHOULD NOT RETURN ANYTHING
        return true;
	}
	
	//Get the "type" of the model in file and ask the plugin that manage this
	//kind of "type" to load the DES.
	public DESModel load(File file)
	{
		if(xmlParser == null)
		{	
			xmlParser = new XmlParser();
		}else
		{
			return null;
		}
		//TODO: Make the next line innerParser.getType(file)
		String type = xmlParser.getType(file);
		
		//System.out.println(type);
		if(type == null)
		{
			//File error, maybe the file is not an IDES file
			//THROW ERROR
			//TODO THROW AN IO EXCEPTION
			return null;
		}
		
		DESModel returnModel = null;
		//Ask the plugin manager for the correct plugin to handle file loading
		//The variable dataType is equivalent of the IOTypeDescription of the model to 
		//be loaded.			
		FileIOPlugin plugin = IOPluginManager.getInstance().getDataLoader(type);
		if(plugin == null)
		{
			//TODO THROW AN PLUGIN EXCEPTION
			return null;
		}
		xmlParser = null;
		//TODO make "file" be a wrapped file just with <data></data> 
		returnModel = plugin.loadData(file, file.getParentFile());
		//TODO: plugin.loadMeta(returnModel, wrapped file <meta></meta>);
		//Return the model loaded by the plugin
		return returnModel;
	}
	

	private class XmlParser extends AbstractParser{
		Set<String> metaData = new HashSet<String>();	
		protected static final String ATTRIBUTE_TYPE = "type", ATTRIBUTE_TAG = "tag";
		protected static final String NOTHING = "nothing";		
		private String dataType = new String();
		private Set<String> metaTags = new HashSet<String>();

		public XmlParser()
		{
			dataType = NOTHING;

			//Initialize parser:
			try {
	            xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
	            xmlReader.setContentHandler(this);
	        } catch (ParserConfigurationException pce) {
	            System.err
	                    .println("AbstractParser: could not configure parser, message: "
	                            + pce.getMessage());
	        } catch (SAXException se) {
	            System.err
	                    .println("AbstractParser: could not do something, message: "
	                            + se.getMessage());
	        }

		}
	private void parse(File file){
	        parsingErrors = "";
	        try{
	            xmlReader.parse(new InputSource(new FileInputStream(file)));
	        }
	        catch(FileNotFoundException fnfe){
	            parsingErrors += file.getName() + ": " + fnfe.getMessage() + "\n";
	        }
	        catch(IOException ioe){
	            parsingErrors += file.getName() + ": " + ioe.getMessage() + "\n";
	        }
	        catch(SAXException saxe){
	            parsingErrors += file.getName() + ": " + saxe.getMessage() + "\n";
	        }
	        catch(NullPointerException npe){
	        	parsingErrors += file.getName() + ": " + npe.getMessage() + "\n";
	        }
	        
	    }
	    
	   
	    public void startElement(String uri, String localName, String qName, Attributes atts)
	    {
	    	dataType = (atts.getValue(ATTRIBUTE_TYPE) != null ? atts.getValue(ATTRIBUTE_TYPE) :dataType);
	    	if(atts.getValue(ATTRIBUTE_TAG) != null)
	    	{
	    		metaTags.add(atts.getValue(ATTRIBUTE_TAG));
	    	}
	    }	    
	    //PARSE METHODS:
	    
	    private String getType(File file)
	    {
	    	parse(file);
	    	String returnString = dataType;
	    	metaTags.clear();
	    	dataType = NOTHING;
	    	if(returnString == NOTHING)
	    	{
	    		return null;
	    	}
	    	return returnString;
	    }
	    
	    private Set<String> getMetaTags(File file)
	    {
	    	parse(file);
	    	Set<String> returnSet = metaTags;
	    	metaTags.clear();
	    	dataType = NOTHING;
	    	if(metaTags.size() == 0)
	    	{
	    		return null;
	    	}
	    	return returnSet;
	    }
	}
	    
	    
 }	


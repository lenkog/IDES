/**
 * 
 */
package services;

import java.util.Iterator;

import main.Hub;
import model.fsa.FSAModel;

import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author Lenko Grigorov
 */
public class General {
	
	/**
	 * Get an ID string for a new automaton.
	 * @return a random string 
	 */
	public static String getRandomId()
	{
		String data=new Double(Math.random()).toString()+
			new Long(System.currentTimeMillis()).toString()+
			Hub.getWorkspace().size()+System.getProperty("user.name");
		for(Iterator<FSAModel> i=Hub.getWorkspace().getAutomata();i.hasNext();)
		{
			FSAModel a=i.next();
			data+=a.getEventCount();
			data+=a.getStateCount();
			data+=a.getTransitionCount();
		}
		return DigestUtils.md5Hex(data);
	}
}
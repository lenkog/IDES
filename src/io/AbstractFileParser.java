package io;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

/**
 * This is an abstract class for xml parsers that take a file as input.
 * @author Axel Gottlieb Michelsen
 * @author Kristian Edlund
 */
public abstract class AbstractFileParser extends AbstractParser {

    /**
     * constructs an abstract file parser.
     */
    public AbstractFileParser() {
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
    
    
//CHRISTIAN: I have removed this function from the interface, since loaders cannot have access to files. They have to 
    //have access over InputStreams.
//    /**
//     * Parses a file. Returns the coresponding object.
//     * @param file the file that needs parsing
//     * @return the corresponding object.
//     */
////    public abstract Object parse(File file);
}

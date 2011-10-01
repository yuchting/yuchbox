/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)

package org.xmlpull.v1.builder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;
import org.xmlpull.v1.builder.impl.XmlInfosetBuilderImpl;


//     private XmlInfosetBuilder builder = XmlInfosetBuilder.newInstance(); // (XmlPullParser)
//  builder.setUseAllTokens(true)  //comments, PIs, doctype

// setIgnoreComments
// setIgnoreProcessingInstructions
//  builder.setNamespaceAware(true)
//  builder.setBuildCompleteTree(true)


//  builder.setWrapCharacters(true)

// XmlDocument doc =  builder.parseUrl( url );

/**
 * By default builder is using non-validating pull parser with next() method
 * without namespaces to build tree consisting only of XmlDocument, XmlElemenet
 * and String nodes. Additional options are available to change builder behaviour
 * and to generate any deseired subset of
 * <a href="<a href="http://www.w3.org/TR/xml-infoset/">XML Information Set</a>
 *
 * @version $Revision: 1.8 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public abstract class XmlInfosetBuilder
{
    protected XmlPullParserFactory factory;
    
    /**
     * Create a new instance of the builder.
     */
    public static XmlInfosetBuilder newInstance() throws XmlBuilderException
    {
        XmlInfosetBuilder impl = new XmlInfosetBuilderImpl();
        try {
            impl.factory = XmlPullParserFactory.newInstance(
                System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
            impl.factory.setNamespaceAware(true);
            
        } catch(XmlPullParserException ex) {
            throw new XmlBuilderException("could not create XmlPull factory:"+ex, ex);
        }
        return impl;
    }
    
    public static XmlInfosetBuilder newInstance(XmlPullParserFactory factory) throws XmlBuilderException
    {
        if(factory == null) throw new IllegalArgumentException();
        XmlInfosetBuilder impl = new XmlInfosetBuilderImpl();
        impl.factory = factory;
        //try {
        impl.factory.setNamespaceAware(true);
        //} catch(XmlPullParserException ex) {
        //    throw new XmlBuilderException("could not create XmlPull factory:"+ex, ex);
        //}
        return impl;
    }
    
    
    /**
     * Method get XmlPull factory that is ued by this builder.
     */
    public XmlPullParserFactory getFactory() throws XmlBuilderException {return factory; }
    
    
    // --- create directly\
    /**
     * Create a new document.
     */
    public XmlDocument newDocument() throws XmlBuilderException {
        return newDocument(null, null, null);
    }
    
    /**
     * Create a new document with given XML prolog.
     *
     * @param    version             a  String
     * @param    standalone          a  Boolean
     * @param    characterEncoding   a  String
     *
     * @return   a XmlDocument
     *
     * @exception   XmlBuilderException
     *
     */
    public abstract XmlDocument newDocument(String version,
                                            Boolean standalone,
                                            String characterEncoding) throws XmlBuilderException;
    
    /**
     * Create XML fragment that is not associated with any document.
     *
     * @param    elementName         name of element
     *
     * @return   a XmlElement
     *
     * @exception   XmlBuilderException
     *
     */
    public abstract XmlElement newFragment(String elementName) throws XmlBuilderException;
    
    /**
     * Create XML fragment that is not associated with any document.
     *
     * @param    elementNamespace    namespace of element
     * @param    elementName         name of element
     *
     * @return   a XmlElement
     *
     * @exception   XmlBuilderException
     *
     */
    public abstract XmlElement newFragment(String elementNamespace, String elementName) throws XmlBuilderException;
    
    /**
     * Create XML fragment that is not associated with any document.
     *
     * @param    elementNamespace    a  XmlNamespace
     * @param    elementName         a  String
     *
     * @return   a XmlElement
     *
     * @exception   XmlBuilderException
     *
     */
    public abstract XmlElement newFragment(XmlNamespace elementNamespace,
                                           String elementName) throws XmlBuilderException;
    
    /**
     * Create a new namespace that is not associated with any XML document.
     *
     * @param    namespaceName       a  String
     *
     * @return   a XmlNamespace
     *
     * @exception   XmlBuilderException
     *
     */
    public abstract XmlNamespace newNamespace(String namespaceName) throws XmlBuilderException;
    
    /**
     * Create a new namespace that is not associated with any XML document.
     *
     * @param    prefix              a  String
     * @param    namespaceName       a  String
     *
     * @return   a XmlNamespace
     *
     * @exception   XmlBuilderException
     *
     */
    public abstract XmlNamespace newNamespace(String prefix, String namespaceName) throws XmlBuilderException;
    
    // --- parsing
    
    //public abstract XmlElement newFragment(String elementNamespace, String elementName, XmlNamespace[] context);
    //public abstract XmlElement parse(XmlPullParser sourceForNode,boolean addAllNamespaces);
    
    /**
     * Parse document - parser must be in START_DOCUMENT state.
     */
    public abstract XmlDocument parse(XmlPullParser sourceForDocument) throws XmlBuilderException;
    
    
    /**
     * Will convert current parser state into event rerpresenting XML infoset item: <ul>
     * <li>START_Document: XmlDocument without root element
     * <li>START_TAG: XmlElement without children
     * <li>TEXT: String or XmlCHaracters depending on builder mode
     * <li>additiona states to corresponding XML infoset items (when implemented!)
     * </ul>
     */
    public abstract Object parseItem(XmlPullParser pp) throws XmlBuilderException;
    
    /**
     * Parser must be on START_TAG and this method will convert START_TAG content into
     * XmlELement. Parser location is not chnaged.
     */
    public abstract XmlElement parseStartTag(XmlPullParser pp) throws XmlBuilderException;
    
    /**
     * Parse input stream to create XML document.
     *
     * @param    is                  an InputStream
     *
     * @return   a XmlDocument
     *
     * @exception   XmlBuilderException
     *
     */
    public XmlDocument parseInputStream(InputStream is) throws XmlBuilderException
    {
        XmlPullParser pp = null;
        try {
            pp = factory.newPullParser();
            pp.setInput(is, null);
            //set options ...
        } catch (XmlPullParserException e) {
            throw new XmlBuilderException("could not start parsing input stream", e);
        }
        return parse(pp);
    }
    
    /**
     * Parse input stream to create XML document using specified encoding.
     *
     * @param    is                  an InputStream
     * @param    encoding            a  String
     *
     * @return   a XmlDocument
     *
     * @exception   XmlBuilderException
     *
     */
    public XmlDocument parseInputStream(InputStream is, String encoding) throws XmlBuilderException
    {
        XmlPullParser pp = null;
        try {
            pp = factory.newPullParser();
            pp.setInput(is, encoding);
            //set options ...
        } catch (XmlPullParserException e) {
            throw new XmlBuilderException("could not start parsing input stream (encoding="+encoding+")", e);
        }
        return parse(pp);
    }
    
    /**
     * Parse reader to create XML document.
     *
     * @param    reader              a  Reader
     *
     * @return   a XmlDocument
     *
     * @exception   XmlBuilderException
     *
     */
    public XmlDocument parseReader(Reader reader) throws XmlBuilderException
    {
        XmlPullParser pp = null;
        try {
            pp = factory.newPullParser();
            pp.setInput(reader);
            //set options ...
        } catch (XmlPullParserException e) {
            throw new XmlBuilderException("could not start parsing input from reader", e);
        }
        return parse(pp);
    }
    
    /**
     * Parse input from URL location to create XML document.
     *
     * @param    locationUrl         a  String
     *
     * @return   a XmlDocument
     *
     * @exception   XmlBuilderException
     *
     */
    public abstract XmlDocument parseLocation(String locationUrl) throws XmlBuilderException;
    
    /**
     * Parse fragment  - parser must be on START_TAG. After parsing is on corresponding END_TAG.
     */
    public abstract XmlElement parseFragment(XmlPullParser sourceForXml) throws XmlBuilderException;
    
    
    /**
     * Parse input stream to create XML fragment.
     *
     * @param    is                  an InputStream
     *
     * @return   a XmlElement
     *
     * @exception   XmlBuilderException
     *
     */
    public XmlElement parseFragmentFromInputStream(InputStream is) throws XmlBuilderException
    {
        XmlPullParser pp = null;
        try {
            pp = factory.newPullParser();
            pp.setInput(is, null);
            //set options ...
            try {
                pp.nextTag();
            } catch (IOException e) {
                throw new XmlBuilderException(
                    "IO error when starting to parse input stream", e);
            }
        } catch (XmlPullParserException e) {
            throw new XmlBuilderException("could not start parsing input stream", e);
        }
        return parseFragment(pp);
    }
    
    /**
     * Parse input stream to create XML fragment using specified encoding.
     *
     * @param    is                  an InputStream
     * @param    encoding            a  String
     *
     * @return   a XmlElement
     *
     * @exception   XmlBuilderException
     *
     */
    public XmlElement parseFragementFromInputStream(InputStream is, String encoding) throws XmlBuilderException
    {
        XmlPullParser pp = null;
        try {
            pp = factory.newPullParser();
            pp.setInput(is, encoding);
            //set options ...
            try {
                pp.nextTag();
            } catch (IOException e) {
                throw new XmlBuilderException(
                    "IO error when starting to parse input stream (encoding="+encoding+")", e);
            }
        } catch (XmlPullParserException e) {
            throw new XmlBuilderException("could not start parsing input stream (encoding="+encoding+")", e);
        }
        return parseFragment(pp);
    }
    
    /**
     * Parse reader to create XML fragment.
     *
     * @param    reader              a  Reader
     *
     * @return   a XmlElement
     *
     * @exception   XmlBuilderException
     *
     */
    public XmlElement parseFragmentFromReader(Reader reader) throws XmlBuilderException
    {
        XmlPullParser pp = null;
        try {
            pp = factory.newPullParser();
            pp.setInput(reader);
            //set options ...
            try {
                pp.nextTag();
            } catch (IOException e) {
                throw new XmlBuilderException(
                    "IO error when starting to parse from reader", e);
            }
        } catch (XmlPullParserException e) {
            throw new XmlBuilderException("could not start parsing input from reader", e);
        }
        return parseFragment(pp);
    }
    
    /**
     * Move parser from START_TAG to the corresponding END_TAG which means
     * that XML sub tree is skipped.
     *
     * @param    pp                  a  XmlPullParser
     *
     * @exception   XmlBuilderException
     *
     */
    public void skipSubTree(XmlPullParser pp) throws XmlBuilderException
    {
        try {
            pp.require(XmlPullParser.START_TAG, null, null);
            int level = 1;
            while(level > 0) {
                int eventType = pp.next();
                if(eventType == XmlPullParser.END_TAG) {
                    --level;
                } else if(eventType == XmlPullParser.START_TAG) {
                    ++level;
                }
            }
        } catch (XmlPullParserException e) {
            throw new XmlBuilderException("could not skip subtree", e);
        } catch (IOException e) {
            throw new XmlBuilderException("IO error when skipping subtree", e);
        }
    }
    
    // --- serialization
    
    /**
     * Write XML start tag with information provided in XML element.
     *
     * @param    el                  a  XmlElement
     * @param    ser                 a  XmlSerializer
     *
     * @exception   XmlBuilderException
     *
     */
    public abstract void serializeStartTag(XmlElement el, XmlSerializer ser)
        throws XmlBuilderException;
    /**
     * Write XML end tag with information provided in XML element.
     *
     * @param    el                  a  XmlElement
     * @param    ser                 a  XmlSerializer
     *
     * @exception   XmlBuilderException
     *
     */
    public abstract void serializeEndTag(XmlElement el, XmlSerializer ser)
        throws XmlBuilderException;
    
    /**
     * Serialize XML infoset item including serializing of children.
     * If item is Collection all items in collection are serialized by
     * recursively calling this function.
     * This method  assumes that item is either interface defined in XB1 API, class String,
     * or that item implements XmlSerializable otherwise IllegalArgumentException
     * is thrown.
     */
    public abstract void serialize(Object item, XmlSerializer serializer) throws XmlBuilderException;
    //throws XmlPullParserException, IOException, IllegalArgumentException;
    
    /**
     * Serialize XML infoset item <b>without</b> serializing any of children.
     * This method  assumes that item is either interface defined in XB1 API, class String,
     * or item that implements XmlSerializable otherwise IllegalArgumentException
     * is thrown.
     */
    public abstract void serializeItem(Object item, XmlSerializer serializer) throws XmlBuilderException;
    
    /**
     * Serialize item using default UTF8 encoding.
     *
     * @see serializeItem
     */
    public void serializeToOutputStream(Object item, //XmlContainer node,
                                        OutputStream os)
        throws XmlBuilderException
        //throws XmlPullParserException, IOException, IllegalArgumentException
    {
        serializeToOutputStream(item, os, "UTF8");
    }
    
    /**
     * Serialize item to given output stream using given character encoding.
     *
     * @param    item                an Object
     * @param    os                  an OutputStream
     * @param    encoding            a  String
     *
     * @exception   XmlBuilderException
     *
     * @see serializeItem
     *
     */
    public void serializeToOutputStream(Object item, //XmlContainer node,
                                        OutputStream os,
                                        String encoding)
        throws XmlBuilderException
        //throws XmlPullParserException, IOException, IllegalArgumentException
    {
        XmlSerializer ser = null;
        try {
            ser = factory.newSerializer();
            ser.setOutput(os, encoding);
        } catch (Exception e) {
            throw new XmlBuilderException("could not serialize node to output stream"
                                              +" (encoding="+encoding+")", e);
        }
        serialize(item, ser);
        try {
            ser.flush();
        } catch (IOException e) {
            throw new XmlBuilderException("could not flush output", e);
        }
    }
    
    /**
     * Serialize item to given writer.
     *
     * @param    item                an Object
     * @param    writer              a  Writer
     *
     * @exception   XmlBuilderException
     *
     */
    public void serializeToWriter(Object item, //XmlContainer node,
                                  Writer writer)
        //throws XmlPullParserException, IOException, IllegalArgumentException
        throws XmlBuilderException
    {
        XmlSerializer ser = null;
        try {
            ser = factory.newSerializer();
            ser.setOutput(writer);
        } catch (Exception e) {
            throw new XmlBuilderException("could not serialize node to writer", e);
        }
        serialize(item, ser);
        try {
            ser.flush();
        } catch (IOException e) {
            throw new XmlBuilderException("could not flush output", e);
        }
    }
    
    /**
     * Convert item into String representing XML content.
     *
     * @param    item                an Object
     *
     * @return   a String
     *
     * @exception   XmlBuilderException
     *
     */
    public String serializeToString(Object item) //XmlContainer node)
        //throws XmlPullParserException, IOException, IllegalArgumentException
        throws XmlBuilderException
    {
        StringWriter sw = new StringWriter();
        serializeToWriter(item, sw);
        return sw.toString();
    }
    
}


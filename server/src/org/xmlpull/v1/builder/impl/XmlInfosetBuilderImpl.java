package org.xmlpull.v1.builder.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;
import org.xmlpull.v1.builder.XmlAttribute;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlComment;
import org.xmlpull.v1.builder.XmlContainer;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.XmlInfosetBuilder;
import org.xmlpull.v1.builder.XmlSerializable;
import org.xmlpull.v1.builder.XmlCharacters;

//TODO: add equals() and hashcode()
//TODO: think about how to do gernali XmlSerialziable (to simplify serialzie()
//TODO: in XmlElement use String namespaceName and do not use XmlNamespace namespace (except for getNamespace()?
//           NOTE: that XML infoset requires prefix informatio to be present even if not needed ....

/**
 * Implementation of generic buuilder that uses XmlPull API to access
 * current default XmlPullParser and XmlSerializer.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class XmlInfosetBuilderImpl extends XmlInfosetBuilder
{
    private final static String PROPERTY_XMLDECL_STANDALONE =
        "http://xmlpull.org/v1/doc/properties.html#xmldecl-standalone";
    private final static String PROPERTY_XMLDECL_VERSION =
        "http://xmlpull.org/v1/doc/properties.html#xmldecl-version";
    //private boolean useNamespaces;
    
    public XmlInfosetBuilderImpl() {}
    
    public XmlDocument newDocument(String version, Boolean standalone, String characterEncoding)
    {
        return new XmlDocumentImpl(version, standalone, characterEncoding);
    }
    
    public XmlElement newFragment(String elementName)
    {
        return new XmlElementImpl((XmlNamespace)null, elementName);
    }
    
    public XmlElement newFragment(String elementNamespaceName, String elementName)
    {
        return new XmlElementImpl(elementNamespaceName, elementName);
    }
    
    public XmlElement newFragment(XmlNamespace elementNamespace, String elementName)
    {
        return new XmlElementImpl(elementNamespace, elementName);
    }
    
    //public abstract XmlNamespace newNamespace(String namespaceName);
    //public abstract XmlNamespace newNamespace(String prefix, String namespaceName);
    
    public XmlNamespace newNamespace(String namespaceName)
    {
        return new XmlNamespaceImpl(null, namespaceName);
    }
    
    public XmlNamespace newNamespace(String prefix, String namespaceName)
    {
        return new XmlNamespaceImpl(prefix, namespaceName);
    }
    
    public XmlDocument parse(XmlPullParser pp)
    {
        XmlDocument doc = parseDocumentStart(pp);
        XmlElement root = parseFragment(pp);
        doc.setDocumentElement(root);
        //TODO parseDocumentEnd() - parse epilog with nextToken();
        return doc;
    }
    
    public Object parseItem(XmlPullParser pp)
    {
        try {
            int eventType = pp.getEventType();
            if(eventType == XmlPullParser.START_TAG) {
                return parseStartTag(pp);
            } else if(eventType == XmlPullParser.TEXT) {
                return pp.getText();
            } else if(eventType == XmlPullParser.START_DOCUMENT) {
                return parseDocumentStart(pp);
            } else {
                throw new XmlBuilderException("currently unsupported event type "
                                                  +XmlPullParser.TYPES[eventType]+pp.getPositionDescription());
            }
        } catch (XmlPullParserException e) {
            throw new XmlBuilderException("could not parse XML item" ,e);
        }
    }
    
    
    private XmlDocument parseDocumentStart(XmlPullParser pp)
        //throws XmlPullParserException, IOException
    {
        //sourceForNode.next();
        
        XmlDocument doc = null;
        try {
            if(pp.getEventType() != XmlPullParser.START_DOCUMENT) {
                throw new XmlBuilderException("parser must be positioned on beginning of document"
                                                  +" and not "+pp.getPositionDescription());
            }
            // TODO use nextToken()
            pp.next();
            String xmlDeclVersion = (String) pp.getProperty(PROPERTY_XMLDECL_VERSION);
            Boolean xmlDeclStandalone = (Boolean) pp.getProperty(PROPERTY_XMLDECL_STANDALONE);;
            String characterEncoding = pp.getInputEncoding();
            doc = new XmlDocumentImpl(xmlDeclVersion, xmlDeclStandalone, characterEncoding);
        } catch (XmlPullParserException e) {
            throw new XmlBuilderException("could not parse XML document prolog" ,e);
        } catch (IOException e) {
            throw new XmlBuilderException("could not read XML document prolog" ,e);
        }
        return doc;
    }
    
    
    
    public XmlElement parseFragment(XmlPullParser pp)
    {
        try {
            int depth  = pp.getDepth();
            int eventType = pp.getEventType();
            if(eventType != XmlPullParser.START_TAG) {
                throw new XmlBuilderException(
                    "expected parser to be on start tag and not "+XmlPullParser.TYPES[ eventType ]
                        +pp.getPositionDescription());
            }
            //int level = depth + 1;
            XmlElement curElem = parseStartTag(pp);
            while(true) {
                eventType = pp.next();
                if(eventType == XmlPullParser.START_TAG) {
                    XmlElement child = parseStartTag(pp);
                    curElem.addElement(child);
                    curElem = child;
                } else if(eventType == XmlPullParser.END_TAG) {
                    XmlContainer parent = curElem.getParent();
                    if(parent == null) {
                        if(pp.getDepth() != depth) {
                            throw new XmlBuilderException(
                                "unbalanced input"+pp.getPositionDescription());
                        }
                        return curElem;
                    }
                    curElem = (XmlElement) parent;
                    //--level;
                    //curElem = curElem.parent;
                } else if(eventType == XmlPullParser.TEXT) {
                    curElem.addChild(pp.getText());
                }
            }
        } catch (XmlPullParserException e) {
            throw new XmlBuilderException("could not build tree from XML" ,e);
        } catch (IOException e) {
            throw new XmlBuilderException("could not read XML tree content" ,e);
        }
    }
    
    public XmlElement parseStartTag(XmlPullParser pp)
    {
        try {
            //assert pp.getEventType() == XmlPullParser.START_TAG;
            if(pp.getEventType() != XmlPullParser.START_TAG) {
                throw new XmlBuilderException(
                    "parser must be on START_TAG and not "+pp.getPositionDescription());
            }
            
            String elNsPrefix = pp.getPrefix();
            XmlNamespace elementNs = new XmlNamespaceImpl(elNsPrefix, pp.getNamespace()) ;
            XmlElement el = new XmlElementImpl(elementNs, pp.getName());
            //add namespaces declarations (if any)
            for (int i = pp.getNamespaceCount(pp.getDepth()-1);
                     i < pp.getNamespaceCount(pp.getDepth()); i++)
            {
                // TODO think about changing  XmlPull to return "" in this case ... or not
                String prefix = pp.getNamespacePrefix(i);
                el.declareNamespace(prefix == null ? "" : prefix, pp.getNamespaceUri(i));
            }
            //add attributes and namespaces
            for (int i = 0; i < pp.getAttributeCount(); i++)
            {
                el.addAttribute(pp.getAttributeType(i),
                                pp.getAttributePrefix(i),
                                pp.getAttributeNamespace(i),
                                pp.getAttributeName(i),
                                pp.getAttributeValue(i),
                                pp.isAttributeDefault(i) == false);
            }
            return el;
        } catch (XmlPullParserException e) {
            throw new XmlBuilderException("could not parse XML start tag" ,e);
            //} catch (IOException e) {
            //    throw new XmlBuilderException("could not read XML start tag" ,e);
        }
    }
    
    public XmlDocument parseLocation(String locationUrl)
    {
        // TODO: if first is  "/" try opening file
        URL url = null;
        try {
            url = new URL(locationUrl);
        } catch (MalformedURLException e) {
            throw new XmlBuilderException("could not parse URL "+locationUrl, e);
        }
        try {
            return parseInputStream(url.openStream());
        } catch (IOException e) {
            throw new XmlBuilderException("could not open connection to URL "+locationUrl, e);
        }
    }
    
    public void serialize(Object item, XmlSerializer serializer)
    {
        if(item instanceof Collection) {
            Collection c = (Collection) item;
            for(Iterator i = c.iterator() ; i.hasNext(); ) {
                serialize(i.next(), serializer);
            }
            
        } else if(item instanceof XmlContainer) {
            serializeContainer((XmlContainer)item, serializer);
        } else {
            serializeItem(item, serializer);
        }
    }
    
    private void serializeContainer(XmlContainer node, XmlSerializer serializer)
    {
        if(node instanceof XmlSerializable) {
            try {
                ((XmlSerializable)node).serialize(serializer);
            } catch (IOException e) {
                throw new XmlBuilderException("could not serialize node "+node+": "+e, e);
            }
        } else if(node instanceof XmlDocument) {
            serializeDocument((XmlDocument)node, serializer);
        } else if(node instanceof XmlElement) {
            serializeFragment((XmlElement)node, serializer);
        } else {
            throw new IllegalArgumentException(
                "could not serialzie unknown XML container "+node.getClass());
        }
    }
    
    public void serializeItem(Object item, XmlSerializer ser)
    {
        try {
            if(item instanceof XmlSerializable) {
                //((XmlSerializable)item).serialize(ser);
                try {
                    ((XmlSerializable)item).serialize(ser);
                } catch (IOException e) {
                    throw new XmlBuilderException("could not serialize item "+item+": "+e, e);
                }
            } else if(item instanceof String) {
                ser.text(item.toString());
            } else if(item instanceof XmlCharacters) {
                ser.text(((XmlCharacters)item).getText());
            } else if(item instanceof XmlComment) {
                ser.comment(((XmlComment)item).getContent());
            } else {
                throw new IllegalArgumentException("could not serialize "+(item != null ? item.getClass() : item));
            }
        } catch (IOException e) {
            throw new XmlBuilderException("serializing XML start tag failed", e);
        }
    }
    
    public void serializeStartTag(XmlElement el, XmlSerializer ser) {
        try {
            //declare namespaces
            XmlNamespace elNamespace = el.getNamespace();
            String elPrefix = (elNamespace != null) ? elNamespace.getPrefix() : "";
            if(elPrefix == null) {
                elPrefix = "";
            }
            String nToDeclare = null;
            if(el.hasNamespaceDeclarations()) {
                Iterator iter = el.namespaces();
                while (iter.hasNext())
                {
                    XmlNamespace n = (XmlNamespace) iter.next();
                    String nPrefix = n.getPrefix();
                    if(!elPrefix.equals(nPrefix)) {
                        ser.setPrefix(nPrefix, n.getNamespaceName());
                    } else {
                        nToDeclare = n.getNamespaceName();
                    }
                }
            }
            // make sure that preferred element prefix is declared last so it is picked up by serializer
            if(nToDeclare != null) {
                ser.setPrefix(elPrefix, nToDeclare);
            } else {
                if(elNamespace != null) {
                    // first check that it needs to be  declared - will declaring change anything?
                    String namespaceName = elNamespace.getNamespaceName();
                    if(namespaceName == null) {
                        namespaceName = "";
                    }
                    
                    String serPrefix = null;
                    if(namespaceName.length() > 0) {
                        ser.getPrefix(namespaceName, false);
                    }
                    if(serPrefix==null){
                        serPrefix="";
                    }
                    if(serPrefix != elPrefix && !serPrefix.equals(elPrefix)){
                        // the prefix was not declared on current elment but just to enforce prefix choice ...
                        ser.setPrefix(elPrefix, namespaceName);
                    }
                }
            }
            
            ser.startTag(el.getNamespaceName(), el.getName());
            if(el.hasAttributes()) {
                Iterator iter = el.attributes();
                while (iter.hasNext())
                {
                    XmlAttribute a = (XmlAttribute) iter.next();
                    if(a instanceof XmlSerializable) {
                        ((XmlSerializable) a).serialize(ser);
                    } else {
                        ser.attribute(a.getNamespaceName(), a.getName(), a.getValue());
                    }
                }
            }
        } catch (IOException e) {
            throw new XmlBuilderException("serializing XML start tag failed", e);
        }
    }
    
    public void serializeEndTag(XmlElement el, XmlSerializer ser) {
        try {
            ser.endTag(el.getNamespaceName(), el.getName());
        } catch (IOException e) {
            throw new XmlBuilderException("serializing XML end tag failed", e);
        }
    }
    
    //TODO: would iit be simple make XmlContainer serialziable and use it to serialize ....
    //TODO        but would it be more flexible?
    private void serializeDocument(XmlDocument doc, XmlSerializer ser)
    {
        try {
            ser.startDocument(doc.getCharacterEncodingScheme(), doc.isStandalone());
        } catch (IOException e) {
            throw new XmlBuilderException("serializing XML document start failed", e);
        }
        if(doc.getDocumentElement() != null) {
            serializeFragment(doc.getDocumentElement(), ser);
        } else {
            throw new XmlBuilderException("could not serialize document without root element "+doc+": ");
        }
        try {
            ser.endDocument();
        } catch (IOException e) {
            throw new XmlBuilderException("serializing XML document end failed", e);
        }
    }
    
    private void serializeFragment(XmlElement el, XmlSerializer ser)
    {
        serializeStartTag(el, ser);
        //try {
        if(el.hasChildren()) {
            Iterator iter = el.children();
            while (iter.hasNext())
            {
                Object child = iter.next();
                if(child instanceof XmlSerializable) {
                    //((XmlSerializable)child).serialize(ser);
                    try {
                        ((XmlSerializable)child).serialize(ser);
                    } catch (IOException e) {
                        throw new XmlBuilderException("could not serialize item "+child+": "+e, e);
                    }
                    
                } else if(child instanceof XmlElement) {
                    serializeFragment((XmlElement)child, ser);
                } else {
                    serializeItem(child, ser);
                }
            }
        }
        //} catch (IOException e) {
        //    throw new XmlBuilderException("serializing XML element children failed", e);
        //}
        serializeEndTag(el, ser);
    }
}


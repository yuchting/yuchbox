package org.xmlpull.v1.builder;

/**
 * This is <b>immutable</b> value object that represents
 * <a href="http://www.w3.org/TR/xml-infoset/#infoitem.attribute">Attribute
 * Information Item</a>
 * with exception of <b>references</b> property.
 * Note: namespace and prefix properties are folded into XmlNamespace value object.
 *
 * @version $Revision: 1.6 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public interface XmlAttribute extends Cloneable
{
    /**
     * Method clone
     *
     * @return   an Object
     *
     * @exception   CloneNotSupportedException
     *
     */
    public Object clone() throws CloneNotSupportedException;

    /**
     * XML Infoset [owner element] property
     */
    public XmlElement getOwner();
    //public XmlElement setOwner(XmlElement newOwner);
    //public String getPrefix();
    
    /**
     * return XML Infoset [namespace name] property (namespaceName from getNamespace()
     * or null if attribute has no namespace
     */
    public String getNamespaceName();
    
    /**
     * Combination of XML Infoset [namespace name] and [prefix] properties
     */
    public XmlNamespace getNamespace();
    
    /**
     * XML Infoset [local name] property
     */
    public String getName();
    
    
    /**
     * XML Infoset [normalized value] property
     */
    public String getValue();
    /**
     * XML Infoset [attribute type]
     */
    public String getType();
    /**
     * XML Infoset [specified] flag
     */
    public boolean isSpecified();
}


package org.xmlpull.v1.builder;

/**
 * Represents
 * <a href="http://www.w3.org/TR/xml-infoset/#infoitem.entity.unparsed">Unparsed Entity
 * Information Item</a>
 * .
 *
 * @version $Revision: 1.3 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public interface XmlUnparsedEntity //extends XmlContainer
{
    public String getName();
    public String getSystemIdentifier();
    public String getPublicIdentifier();
    public String getDeclarationBaseUri();
    public String getNotationName();
    public XmlNotation getNotation();
}


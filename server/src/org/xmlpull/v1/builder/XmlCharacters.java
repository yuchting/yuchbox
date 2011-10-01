package org.xmlpull.v1.builder;

/**
 * Represents otrdered colection of
 * <a href="http://www.w3.org/TR/xml-infoset/#infoitem.character">Character Information Items</a>
 * where character code properties are put together into Java String.
 *
 * @version $Revision: 1.5 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public interface XmlCharacters extends XmlContained
{
    public String getText();
    public Boolean isWhitespaceContent();
    //public XmlElement getParent();

}


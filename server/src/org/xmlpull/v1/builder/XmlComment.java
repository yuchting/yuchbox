package org.xmlpull.v1.builder;

/**
 * Represents
 * <a href="http://www.w3.org/TR/xml-infoset/#infoitem.comment">Comment Information Item</a>.
 *
 *
 * @version $Revision: 1.5 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */

public interface XmlComment //extends XmlContainer
{
    /**
     *  A string representing the content of the comment.
     */
    public String getContent();

    /**
     * The document or element information item which contains this information item
     * in its [children] property.
     */
    public XmlContainer getParent();
}


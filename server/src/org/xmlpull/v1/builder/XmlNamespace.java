package org.xmlpull.v1.builder;


/**
 * Represents
 * <a href="http://www.w3.org/TR/xml-infoset/#infoitem.namespace">Namespace Information Item</a>
 * .
 *
 * @version $Revision: 1.2 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public interface XmlNamespace
{
    /**
     * Prefix can be null.
     * In this case it will be looked up from XML tree
     * and used if available
     * otherwise it will be automatically created only for serializaiton.
     * TODO: If prefix is empty string it will be used to indicate default namespace.
     */
    public String getPrefix();

    /**
     * Namespace name.
     * Never null.
     * Only allowed to be empty string if prefix is also empty string
     * (used to undeclare default namespace)
     */
    public String getNamespaceName();

}

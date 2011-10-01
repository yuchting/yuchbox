package org.xmlpull.v1.builder;

/**
 * Common abstraction to represent XML infoset item that are contained in other infoet items
 * This is useful so parent can be updated on contained items when container is cloned ...
 *
 * @version $Revision: 1.3 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */

public interface XmlContained
{
    
    //Object getPrent() -- requires covariant return to work both in
    ///   XmlElement (returns XmlContainer) and XmlComment (returns XmlContainer) ...
    public XmlContainer getParent();
    public void setParent(XmlContainer el);
    
}


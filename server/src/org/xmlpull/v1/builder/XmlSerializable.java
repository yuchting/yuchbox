package org.xmlpull.v1.builder;


import java.io.IOException;
import org.xmlpull.v1.XmlSerializer;

/**
 * This interface is used during serialization by XmlInfosetBuilder
 * for children that are not in XML infoset.
 *
 * @version $Revision: 1.4 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public interface XmlSerializable
{
    public void serialize(XmlSerializer ser) throws IOException;
}


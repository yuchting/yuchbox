package org.xmlpull.v1.builder.adapter;

//import java.util.IdentityHashMap;
import org.xmlpull.v1.builder.XmlAttribute;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.XmlElement;

/**
 * Wraps XML attribute - allows overriding and other nice things.
 */
public class XmlAttributeAdapter implements XmlAttribute
{
    private XmlAttribute target;
    
    //JDK15 covariant public XmlElementAdapter clone() throws CloneNotSupportedException;
    public Object clone() throws CloneNotSupportedException {
        XmlAttributeAdapter ela = (XmlAttributeAdapter) super.clone();
        ela.target = (XmlAttribute) target.clone();
        return ela;
    }
    
    
    public XmlAttributeAdapter(XmlAttribute target) {
        this.target = target;
    }
    
    
    public XmlElement getOwner() {
        return target.getOwner();
    }
    
    public String getNamespaceName() {
        return target.getNamespaceName();
    }
    
    public XmlNamespace getNamespace() {
        return target.getNamespace();
    }
    
    public String getName() {
        return target.getName();
    }
    
    public String getValue() {
        return target.getValue();
    }
    
    public String getType() {
        return target.getType();
    }
    
    public boolean isSpecified() {
        return target.isSpecified();
    }
    
//    public void serialize(XmlSerializer ser) throws IOException {
//      // TODO
//    }
}


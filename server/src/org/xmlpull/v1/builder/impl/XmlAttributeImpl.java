package org.xmlpull.v1.builder.impl;

import org.xmlpull.v1.builder.XmlAttribute;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlNamespace;

/**
 * Simple implementation.
 *
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class XmlAttributeImpl implements XmlAttribute
{
    private XmlElement owner_;
    private String prefix_;
    private XmlNamespace namespace_;
    private String name_;
    private String value_;
    private String type_ = "CDATA";
    private boolean default_;
    
    public Object clone() throws CloneNotSupportedException{
        XmlAttributeImpl cloned = (XmlAttributeImpl) super.clone();
        cloned.owner_ = null;
        // now do deep clone
        cloned.prefix_ = prefix_;
        cloned.namespace_ = namespace_;
        cloned.name_ = name_;
        cloned.value_ = value_;
        cloned.default_ = default_;
        return cloned;
    }
    
    
    XmlAttributeImpl(XmlElement owner, String name, String value) {
        this.owner_ = owner;
        this.name_ = name;
        if(value == null) throw new IllegalArgumentException("attribute value can not be null");
        this.value_ = value;
    }
    
    XmlAttributeImpl(XmlElement owner, XmlNamespace namespace,
                     String name, String value) {
        this(owner, name, value);
        this.namespace_ = namespace;
    }
    
    XmlAttributeImpl(XmlElement owner, String type, XmlNamespace namespace,
                     String name, String value) {
        this(owner, namespace, name, value);
        this.type_ = type;
    }
    
    XmlAttributeImpl(XmlElement owner,
                     String type,
                     XmlNamespace namespace,
                     String name,
                     String value,
                     boolean specified)
    {
        this(owner, namespace, name, value);
        if(type == null) {
            throw new IllegalArgumentException("attribute type can not be null");
        }
        //TODO: better checking for allowed attribute types
        this.type_ = type;
        this.default_ = !specified;
    }
    
    public XmlElement getOwner() {
        return owner_;
    }
    
    //public String getPrefix()
    //{
    //    return prefix_;
    //}
    
    public XmlNamespace getNamespace()
    {
        return namespace_;
    }
    
    public String getNamespaceName()
    {
        return namespace_ != null ? namespace_.getNamespaceName() : null;
    }
    
    public String getName()
    {
        return name_;
    }
    
    public String getValue()
    {
        return value_;
    }
    
    public String getType()
    {
        return type_;
    }
    
    public boolean isSpecified()
    {
        return !default_;
    }
    
    public boolean equals(Object other) {
        if(other == this) return true;
        if(other == null) return false;
        if(!(other instanceof XmlAttribute)) return false;
        XmlAttribute otherAttr = (XmlAttribute) other;
        return getNamespaceName().equals(otherAttr.getNamespaceName())
            && getName().equals(otherAttr.getName())
            && getValue().equals(otherAttr.getValue());
    }
    
    public String toString() {
        return "name=" + name_ + " value=" + value_;
    }
}


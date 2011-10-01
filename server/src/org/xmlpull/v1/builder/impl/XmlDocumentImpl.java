/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)

package org.xmlpull.v1.builder.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.builder.Iterable;
import org.xmlpull.v1.builder.XmlBuilderException;
import org.xmlpull.v1.builder.XmlComment;
import org.xmlpull.v1.builder.XmlDoctype;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.XmlNotation;
import org.xmlpull.v1.builder.XmlProcessingInstruction;

public class XmlDocumentImpl implements XmlDocument
{
    private List children = new ArrayList();
    private XmlElement root;
    private String version;
    private Boolean standalone;
    private String characterEncoding;
    
    public Object clone() throws CloneNotSupportedException{
        XmlDocumentImpl cloned = (XmlDocumentImpl) super.clone();
        // now do deep clone
        cloned.root = null;
        cloned.children = cloneList(cloned, children);
        int pos = cloned.findDocumentElement();
        if(pos >= 0) {
            cloned.root = (XmlElement) cloned.children.get(pos);
            cloned.root.setParent(cloned);
        }
        return cloned;
    }
    
    private  List cloneList(XmlDocumentImpl cloned, List list) throws CloneNotSupportedException {
        if(list == null) {
            return null;
        }
        List newList = new ArrayList(list.size());
        //JDK15 for(Object member: list) {
        for (int i = 0; i < list.size(); i++)
        {
            Object member = list.get(i);
            Object newMember; // = null;
            if(member instanceof XmlElement) {
                XmlElement el = (XmlElement) member;
                newMember = el.clone();
            } else if(member instanceof java.lang.Cloneable) {
                //use reflection to call clone() -- this is getting ugly!!!!
                // more background on this in http://www.artima.com/intv/issues3.html "The clone Dilemma"
                try {
                    newMember  = member.getClass().getMethod("clone", null).invoke(member, null);
                } catch (Exception e) {
                    throw new CloneNotSupportedException("failed to call clone() on  "+member+e);
                }
            } else {
                throw new CloneNotSupportedException("could not clone "+member+" of "
                                                         +(member != null ? member.getClass().toString() : ""));
            }
            newList.add(newMember);
        }
        return newList;
    }
    
    public XmlDocumentImpl(String version, Boolean standalone, String characterEncoding) {
        this.version = version;
        this.standalone = standalone;
        this.characterEncoding = characterEncoding;
    }
    
    
    /**
     */
    public String getVersion() {
        return version;
    }
    
    /**
     *
     */
    public Boolean isStandalone() {
        return standalone;
    }
    
    
    /**
     *
     */
    public String getCharacterEncodingScheme() {
        return characterEncoding;
    }
    
    public void setCharacterEncodingScheme(String characterEncoding)
    {
        this.characterEncoding = characterEncoding;
    }
    
    /**
     */
    public XmlProcessingInstruction newProcessingInstruction(String target, String content)
    {
        // TODO
        throw new XmlBuilderException("not implemented");
    }
    
    /**
     */
    public XmlProcessingInstruction addProcessingInstruction(String target, String content) {
        // TODO
        throw new XmlBuilderException("not implemented");
    }
    
    /**
     * An ordered list of child information items, in document order.
     * The list contains exactly one element information item.
     * The list also contains one processing instruction information item
     * for each processing instruction outside the document element,
     * and one comment information item for each comment outside the document element.
     * Processing instructions and comments within the DTD are excluded.
     * If there is a document type declaration,
     * the list also contains a document type declaration information item.
     */
    public Iterable children() {
        //throw new XmlBuilderException("not implemented");
        return new Iterable() {
            public Iterator iterator() {
                return children.iterator();
            }
        };
    }
    
    /**
     */
    public void removeAllUnparsedEntities() {
        // TODO
        throw new XmlBuilderException("not implemented");
    }
    
    /**
     */
    public void setDocumentElement(XmlElement rootElement) {
        // replace with existing root element
        int pos = findDocumentElement();
        if(pos >= 0) {
            children.set(pos, rootElement);
        } else {
            children.add(rootElement);
        }
        this.root = rootElement;
        rootElement.setParent(this);
        //TOOD: nice assertion that htere is only one XmlElement in children ...
    }
    
    private int findDocumentElement() {
        for (int i = 0; i < children.size(); i++)
        {
            Object element = children.get(i);
            if(element instanceof XmlElement) {
                return i;
            }
        }
        return -1;
    }
    
    public XmlElement requiredElement(XmlNamespace n, String name) {
        XmlElement el = element(n, name);
        if(el == null) { //TODO:CTX
            throw new XmlBuilderException("document does not contain element with name "+name
                                              +" in namespace "+n.getNamespaceName());
        }
        return el;
    }
    
    public XmlElement element(XmlNamespace n, String name) {
        return element(n, name, false);
    }
    
    public XmlElement element(XmlNamespace namespace, String name, boolean create) {
        XmlElement e = getDocumentElement();
        if(e == null) {
            return null;
        }
        String eNamespaceName = e.getNamespace() != null ? e.getNamespace().getNamespaceName() : null;
        if (namespace != null) {
            if ((name.equals(e.getName()))
                    && (eNamespaceName != null && eNamespaceName.equals(namespace.getNamespaceName())))
            {
                return e;
            }
        } else {
            if ((name.equals(e.getName()))
                    && (eNamespaceName == null))
            {
                return e;
            }
        }
        if(create) {
            return addDocumentElement(namespace, name);
        } else {
            return null;
        }
        
    }
    
    /**
     */
    public void insertChild(int pos, Object child) {
        // TODO
        throw new XmlBuilderException("not implemented");
    }
    
    /**
     */
    public XmlComment addComment(String content) {
        // TODO
        //throw new XmlBuilderException("not implemented");
        XmlComment comment = new XmlCommentImpl(this, content);
        children.add(comment);
        return comment;
    }
    
    /**
     *
     */
    public XmlDoctype newDoctype(String systemIdentifier, String publicIdentifier) {
        // TODO
        throw new XmlBuilderException("not implemented");
    }
    
    /**
     * An unordered set of unparsed entity information items,
     * one for each unparsed entity declared in the DTD.
     */
    public Iterable unparsedEntities() {
        // TODO
        throw new XmlBuilderException("not implemented");
    }
    
    /**
     *
     */
    public void removeAllChildren() {
        // TODO
        throw new XmlBuilderException("not implemented");
    }
    
    /**
     *
     */
    public XmlComment newComment(String content) {
        //throw new XmlBuilderException("not implemented");
        return new XmlCommentImpl(null, content);
    }
    
    /**
     * Method removeAllNotations
     *
     */
    public void removeAllNotations() {
        // TODO
        throw new XmlBuilderException("not implemented");
    }
    /**
     * Method addDoctype
     *
     */
    public XmlDoctype addDoctype(String systemIdentifier, String publicIdentifier) {
        // TODO
        throw new XmlBuilderException("not implemented");
    }
    
    /**
     *
     */
    public void addChild(Object child) {
        // TODO
        throw new XmlBuilderException("not implemented");
    }
    
    /**
     * Method addNotation
     *
     */
    public XmlNotation addNotation(String name, String systemIdentifier, String publicIdentifier, String declarationBaseUri) {
        // TODO
        throw new XmlBuilderException("not implemented");
    }
    
    
    //    public XmlElement newDocumentElement(String name) {
    //        // TODO
    //        return null;
    //    }
    
    /**
     *
     */
    public String getBaseUri() {
        // TODO
        throw new XmlBuilderException("not implemented");
    }
    
    /**
     * An unordered set of notation information items,
     * one for each notation declared in the DTD.
     */
    public Iterable notations() {
        // TODO
        throw new XmlBuilderException("not implemented");
    }
    
    /**
     *
     */
    public XmlElement addDocumentElement(String name) {
        return addDocumentElement(null, name);
    }
    
    public XmlElement addDocumentElement(XmlNamespace namespace, String name)
    {
        XmlElement el = new XmlElementImpl(namespace, name);
        if(getDocumentElement() != null) {
            throw new XmlBuilderException("document already has root element");
        }
        setDocumentElement(el);
        return el;
        
    }
    
    /**
     *
     */
    public boolean isAllDeclarationsProcessed() {
        // TODO
        throw new XmlBuilderException("not implemented");
    }
    
    /**
     *
     */
    public XmlElement getDocumentElement() {
        // TODO
        return root;
    }
    
}


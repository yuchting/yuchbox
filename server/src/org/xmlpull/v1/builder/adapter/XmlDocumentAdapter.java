package org.xmlpull.v1.builder.adapter;

import org.xmlpull.v1.builder.Iterable;
import org.xmlpull.v1.builder.XmlContainer;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlNamespace;
import org.xmlpull.v1.builder.XmlNotation;
import org.xmlpull.v1.builder.XmlComment;
import org.xmlpull.v1.builder.XmlProcessingInstruction;
import org.xmlpull.v1.builder.XmlDoctype;

/**
 */
public class XmlDocumentAdapter implements XmlDocument
{
    
    private XmlDocument target;
    
    //JDK15 covariant public XmlElementAdapter clone() throws CloneNotSupportedException;
    public Object clone() throws CloneNotSupportedException {
        XmlDocumentAdapter ela = (XmlDocumentAdapter) super.clone();
        ela.target = (XmlDocument) target.clone();
        return ela;
    }
    
    
    public XmlDocumentAdapter(XmlDocument target) {
        this.target = target;
        // new "wrapping" parent replaces old parent for children
            fixImportedChildParent(target.getDocumentElement());
        //target.setParent(null);
        //IdentityHashMap id = nul;
    }
    
    private void fixImportedChildParent(Object child) {
        if(child instanceof XmlElement) {
            XmlElement childEl = (XmlElement) child;
            XmlContainer childElParent = childEl.getParent();
            if(childElParent == target) {
                childEl.setParent(this);
            }
        }
    }
    
    public Iterable children() {
        return target.children();
    }
    
    public XmlElement getDocumentElement() {
        return target.getDocumentElement();
    }
    
    public XmlElement requiredElement(XmlNamespace n, String name) {
        return target.requiredElement(n, name);
    }
    
    public XmlElement element(XmlNamespace n, String name) {
        return target.element(n, name);
    }
    
    public XmlElement element(XmlNamespace n, String name, boolean create) {
        return target.element(n, name, create);
    }
    
    public Iterable notations() {
        return target.notations();
    }
    
    public Iterable unparsedEntities() {
        return target.unparsedEntities();
    }
    
    public String getBaseUri() {
        return target.getBaseUri();
    }
    
    public String getCharacterEncodingScheme() {
        return target.getCharacterEncodingScheme();
    }
    
    public void setCharacterEncodingScheme(String characterEncoding) {
        target.setCharacterEncodingScheme(characterEncoding);
    }
    
    public Boolean isStandalone() {
        return target.isStandalone();
    }
    
    public String getVersion() {
        return target.getVersion();
    }
    
    public boolean isAllDeclarationsProcessed() {
        return target.isAllDeclarationsProcessed();
    }
    
    public void setDocumentElement(XmlElement rootElement) {
        target.setDocumentElement(rootElement);
    }
    
    public void addChild(Object child) {
        target.addChild(child);
    }
    
    public void insertChild(int pos, Object child) {
        target.insertChild(pos, child);
    }
    
    public void removeAllChildren() {
        target.removeAllChildren();
    }
    
    public XmlComment newComment(String content) {
        return target.newComment(content);
    }
    
    public XmlComment addComment(String content) {
        return target.addComment(content);
    }
    
    public XmlDoctype newDoctype(String systemIdentifier, String publicIdentifier) {
        return target.newDoctype(systemIdentifier, publicIdentifier);
    }
    
    public XmlDoctype addDoctype(String systemIdentifier, String publicIdentifier) {
        return target.addDoctype(systemIdentifier, publicIdentifier);
    }
    
    public XmlElement addDocumentElement(String name) {
        return target.addDocumentElement(name);
    }
    
    public XmlElement addDocumentElement(XmlNamespace namespace, String name) {
        return target.addDocumentElement(namespace, name);
    }
    
    public XmlProcessingInstruction newProcessingInstruction(String target, String content) {
        return this.target.newProcessingInstruction(target, content);
    }
    
    public XmlProcessingInstruction addProcessingInstruction(String target, String content) {
        return this.target.addProcessingInstruction(target, content);
    }
    
    public void removeAllUnparsedEntities() {
        target.removeAllUnparsedEntities();
    }
    
    public XmlNotation addNotation(String name, String systemIdentifier,
                                   String publicIdentifier, String declarationBaseUri) {
        return target.addNotation(name, systemIdentifier, publicIdentifier, declarationBaseUri);
    }
    
    public void removeAllNotations() {
        target.removeAllNotations();
    }
    
}


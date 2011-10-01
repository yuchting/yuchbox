/* -*-             c-basic-offset: 4; indent-tabs-mode: nil; -*-  //------100-columns-wide------>|*/
// for license please see accompanying LICENSE.txt file (available also at http://www.xmlpull.org/)

package org.xmlpull.v1.builder.impl;

import org.xmlpull.v1.builder.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.xmlpull.v1.builder.Iterable;
//import org.xmlpull.v1.builder.SimpleIterator;

/**
 * This is implementation if XML Infoset Element Information Item.
 *
 * @version $Revision: 1.41 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public class XmlElementImpl implements XmlElement
{

    private XmlContainer parent;
    private XmlNamespace namespace;
    //private String prefix;
    private String name;
    private List attrs;
    private List nsList;
    //NOTE: optimize for one child: Object oneChild; or create ElementWithChild????
    private List children;


    //JDK15 covariant public XmlElement clone() throws CloneNotSupportedException
    public Object clone() throws CloneNotSupportedException
    {
        XmlElementImpl cloned = (XmlElementImpl) super.clone();
        cloned.parent = null;
        // now do deep clone
        cloned.attrs = cloneList(cloned, attrs);
        cloned.nsList = cloneList(cloned, nsList); ///TODO should duplicate ALL in-scope namespaces?
        cloned.children = cloneList(cloned, children);

        //fix parent -- if needs fixing ...
        if(cloned.children != null) {
            for (int i = 0; i < cloned.children.size(); i++)
            {
                Object member = cloned.children.get(i);
                //                if(member instanceof XmlElement) {
                //                    XmlElement el = (XmlElement) member;
                //                    if(el.getParent() == this) {
                //                        el.setParent(null);
                //                        el.setParent(cloned);
                //                    }
                //                }
                if(member instanceof XmlContained) {
                    XmlContained contained = (XmlContained ) member;
                    if(contained.getParent() == this) {
                        contained.setParent(null);
                        contained.setParent(cloned);
                    }
                }

            }
        }

        return cloned;
    }

    private List cloneList(XmlElementImpl cloned, List list) throws CloneNotSupportedException {
        if(list == null) {
            return null;
        }
        List newList = new ArrayList(list.size());
        //JDK15 for(Object member: list) {
        for (int i = 0; i < list.size(); i++)
        {
            Object member = list.get(i);
            Object newMember; // = null;
            if((member instanceof XmlNamespace) || (member instanceof String)) {
                // immutable and has no owner - no need to clone
                newMember = member;
            } else if(member instanceof XmlElement) {
                //                XmlElement el = (XmlElement) member;
                //                newMember = el.clone();
                XmlElement el = (XmlElement) member;
                newMember = el.clone();
            } else if(member instanceof XmlAttribute) {
                XmlAttribute attr = (XmlAttribute) member;
                newMember = new XmlAttributeImpl(cloned,
                                                 attr.getType(),
                                                 attr.getNamespace(),
                                                 attr.getName(),
                                                 attr.getValue(),
                                                 attr.isSpecified());
            } else if(member instanceof java.lang.Cloneable) {
                //use reflection to call clone() -- this is getting ugly!!!!
                // more background on this in http://www.artima.com/intv/issues3.html "The clone Dilemma"
                try {
                    newMember  = member.getClass().getMethod("clone", null).invoke(member, null);
                } catch (Exception e) {
                    throw new CloneNotSupportedException("failed to call clone() on  "+member+e);
                }
            } else {
                throw new CloneNotSupportedException();
            }
            newList.add(newMember);
        }
        return newList;
    }


    /**package**/ XmlElementImpl(String name) {
        this.name = name;
    }

    /**package**/ XmlElementImpl(XmlNamespace namespace, String name) {
        this.namespace = namespace;
        this.name = name;
    }

    /**package**/ XmlElementImpl(String namespaceName, String name) {
        if(namespaceName != null) {
            this.namespace = new XmlNamespaceImpl(null, namespaceName);
        }
        this.name = name;
    }


    public XmlContainer getRoot() {
        XmlContainer root = this;
        while(true) {
            if(! ( root instanceof XmlElement )) {
                break;
            }
            XmlElement el = (XmlElement) root;
            if(el.getParent() != null) {
                root= el.getParent();
            } else {
                break;
            }
        }
        return root;
    }

    //----- generic methods

    public XmlContainer getParent() {
        return parent;
    }

    public void setParent(XmlContainer parent)
    {
        if(parent != null) {
            // check that parent has me as child
            //            if(parent instanceof XmlElement) {
            //                Iterator iter = ((XmlElement)parent).children();
            //                boolean found = false;
            //                while (iter.hasNext())
            //                {
            //                    Object element = iter.next();
            //                    if(element == this) {
            //                        found = true;
            //                        break;
            //                    }
            //                }
            //                if(!found) {
            //                    throw new XmlBuilderException(
            //                        "this element must be child of parent to set its parent");
            //                }
            //            } else
            if(parent instanceof XmlDocument) {
                XmlDocument doc = (XmlDocument) parent;
                if(doc.getDocumentElement() != this) {
                    throw new XmlBuilderException(
                        "this element must be root document element to have document set as parent"
                            +" but already different element is set as root document element");
                }
            }
        }
        this.parent = parent;
    }

    public XmlNamespace getNamespace() {
        return namespace;
    }

    public String getNamespaceName()
    {
        return namespace != null ? namespace.getNamespaceName() : null;
    }

    public void setNamespace(XmlNamespace namespace) {
        this.namespace = namespace;
    }

    //    public String getPrefix() {
    //        return prefix;
    //    }
    //
    //    public void setPrefix(String prefix) {
    //        this.prefix = prefix;
    //    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString()
    {
        return "name[" + name + "]"
            + (namespace != null ? " namespace[" + namespace.getNamespaceName() + "]" : "");
    }
    public String getBaseUri() {
        // TODO
        throw new XmlBuilderException("not implemented");
    }

    public void setBaseUri(String baseUri) {
        // TODO
        throw new XmlBuilderException("not implemented");
    }


    //Attributes

    public Iterator attributes() {
        if(attrs == null) {
            return EMPTY_ITERATOR;
        }
        return attrs.iterator();
    }

    public XmlAttribute addAttribute(XmlAttribute attributeValueToAdd) {
        if(attrs == null) ensureAttributeCapacity(5);
        //TODO: check that there is no attribute duplication (including namespaces?)
        attrs.add(attributeValueToAdd);
        return attributeValueToAdd;
    }

    public XmlAttribute addAttribute(XmlNamespace namespace, String name, String value) {
        return addAttribute("CDATA", namespace, name, value, false);
    }

    public XmlAttribute addAttribute(String name, String value) {
        return addAttribute("CDATA", null, name, value, false);
    }

    public XmlAttribute addAttribute(String attributeType, XmlNamespace namespace, String name, String value) {
        return addAttribute(attributeType, namespace, name, value, false);
    }

    public XmlAttribute addAttribute(String attributeType, XmlNamespace namespace, String name,
                                     String value, boolean specified)
    {
        XmlAttribute a = new XmlAttributeImpl(this, attributeType, namespace, name, value, specified);
        return addAttribute(a);
    }

    public XmlAttribute addAttribute(String attributeType,
                                     String attributePrefix,
                                     String attributeNamespace,
                                     String attributeName,
                                     String attributeValue,
                                     boolean specified)
    {
        XmlNamespace n = newNamespace(attributePrefix, attributeNamespace);
        return addAttribute(attributeType, n, attributeName, attributeValue, specified);
    }

    public void ensureAttributeCapacity(int minCapacity) {
        if(attrs == null) {
            attrs = new ArrayList(minCapacity);
        } else {
            ((ArrayList)attrs).ensureCapacity(minCapacity);
        }
    }

    public String getAttributeValue(String attributeNamespaceName,
                                    String attributeName)
    {
        XmlAttribute xat = findAttribute(attributeNamespaceName, attributeName);
        if(xat != null) {
            return xat.getValue();
        }
        return null;
    }

    public boolean hasAttributes() {
        return attrs != null && attrs.size() > 0;
    }

    public XmlAttribute attribute(String attributeName) {
        return attribute(null, attributeName);
    }

    public XmlAttribute attribute(XmlNamespace attributeNamespace,
                                  String attributeName)
    {
        return findAttribute(
            attributeNamespace!= null ? attributeNamespace.getNamespaceName() : null,
            attributeName);
    }

    //todo: verify me
    //if you enforce that namespace "" and null are equivalent this could be
    //rewritten
    /**
     * @deprecated
     */
    public XmlAttribute findAttribute(String attributeNamespace, String attributeName) {
        if(attributeName == null) {
            throw new IllegalArgumentException("attribute name ca not ber null");
        }

        //addtional condition ---> test if there are some attrs
        if (attrs == null) {
            return null;
        }

        int length = attrs.size();

        for (int i = 0; i < length; i++)
        {
            XmlAttribute a = (XmlAttribute) attrs.get(i);
            String aName = a.getName();
            if(aName == attributeName  // fast if strings are itnerned
                   || attributeName.equals(aName))
            {
                if(attributeNamespace != null) {
                    String aNamespace = a.getNamespaceName();
                    if (attributeNamespace.equals(aNamespace)) {
                        return a;
                    } else if ((attributeNamespace == "") && (aNamespace == null)) {
                        return a;
                    }
                    //                    XmlNamespace aNamespace = a.getNamespace();
                    //                    if(aNamespace != null) {
                    //                        String aNamespaceName = aNamespace.getNamespaceName();
                    //                        if(aNamespaceName == attributeName
                    //                           || aNamespace.equals(aNamespaceName))
                    //                        {
                    //                            return a;
                    //                        }
                    //                    }
                } else {
                    if (a.getNamespace() == null)
                    {
                        return a;
                    } else {
                        if (a.getNamespace().getNamespaceName() == "") {
                            return a;
                        }
                    }
                }

            }
        }
        return null;
    }

    public void removeAllAttributes() {
        attrs = null;
    }

    public void removeAttribute(XmlAttribute attr) {
        if(attrs == null) {
            throw new XmlBuilderException("this element has no attributes to remove");
        }
        for (int i = 0; i < attrs.size(); i++)
        {
            if (attrs.get(i).equals(attr)) {
                attrs.remove(i);
                break;
            }
        }
    }


    //------------ Namespaces

    public XmlNamespace declareNamespace(String prefix, String namespaceName) {
        if(prefix == null) {
            throw new XmlBuilderException("namespace added to element must have not null prefix");
        }
        XmlNamespace n = newNamespace(prefix, namespaceName);
        return declareNamespace(n);
    }

    public XmlNamespace declareNamespace(XmlNamespace n) {
        if(n.getPrefix() == null) {
            throw new XmlBuilderException("namespace added to element must have not null prefix");
        }
        if(nsList == null) ensureNamespaceDeclarationsCapacity(5);
        //TODO  check for duplicates
        nsList.add(n);
        return n;
    }

    public boolean hasNamespaceDeclarations() {
        return nsList != null && nsList.size() >0;
    }

    public XmlNamespace lookupNamespaceByPrefix(String namespacePrefix) {
        if(namespacePrefix == null) {
            throw new IllegalArgumentException("namespace prefix can not be null");
        }
        if(hasNamespaceDeclarations()) {
            int length = nsList.size();
            for (int i = 0; i < length; i++)
            {
                XmlNamespace n = (XmlNamespace) nsList.get(i);
                if(namespacePrefix.equals(n.getPrefix()) ){
                    return n;
                }
            }
        }
        if(parent != null && parent instanceof XmlElement) {
            return ((XmlElement)parent).lookupNamespaceByPrefix(namespacePrefix);
        } else {
            return null;
        }
    }

    public XmlNamespace lookupNamespaceByName(String namespaceName) {
        if(namespaceName == null) {
            throw new IllegalArgumentException("namespace name can not ber null");
        }
        if(hasNamespaceDeclarations()) {
            int length = nsList.size();
            for (int i = 0; i < length; i++)
            {
                XmlNamespace n = (XmlNamespace) nsList.get(i);
                if(namespaceName.equals(n.getNamespaceName()) ){
                    return n;
                }
            }
        }
        if(parent != null && parent instanceof XmlElement) {
            return ((XmlElement)parent).lookupNamespaceByName(namespaceName);
        } else {
            return null;
        }
    }

    public Iterator namespaces() {
        if(nsList == null) {
            return EMPTY_ITERATOR;
        }
        return nsList.iterator();
    }

    public XmlNamespace newNamespace(String namespaceName)
    {
        return newNamespace(null, namespaceName);
    }

    public XmlNamespace newNamespace(String prefix, String namespaceName) {
        return new XmlNamespaceImpl(prefix, namespaceName);
    }

    public void ensureNamespaceDeclarationsCapacity(int minCapacity) {
        //        if(attrs == null) {
        if(nsList == null) {
            nsList = new ArrayList(minCapacity);
        } else {
            ((ArrayList)nsList).ensureCapacity(minCapacity);
        }
    }

    public void removeAllNamespaceDeclarations() {
        nsList = null;
    }


    //---------------- Elements

    public void addChild(Object child) {
        if (child == null) throw new NullPointerException();
        if(children == null) ensureChildrenCapacity(1);
        //checkChildParent(child);
        children.add(child);
        //setChildParent(child);
    }

    public void addChild(int index, Object child) {
        if(children == null) ensureChildrenCapacity(1);
        //checkChildParent(child);
        children.add(index, child);
        //setChildParent(child);
    }

    private void checkChildParent(Object child) {
        if(child instanceof XmlContainer) {
            if(child instanceof XmlElement) {
                XmlElement elChild = (XmlElement) child;
                XmlContainer childParent = elChild.getParent();
                if(childParent != null) {
                    if(childParent != parent) {
                        throw new XmlBuilderException(
                            "child must have no parent to be added to this node");
                    }
                }
            } else if(child instanceof XmlDocument) {
                throw new XmlBuilderException("docuemet can not be stored as element child");
            }
        }
    }

    private void setChildParent(Object child) {
        if(child instanceof XmlElement) {
            XmlElement elChild = (XmlElement) child;
            elChild.setParent(this);
        }
    }

    public XmlElement addElement(XmlElement element)
    {
        checkChildParent(element);
        addChild(element);
        setChildParent(element);
        return element;
    }


    public XmlElement addElement(int pos, XmlElement element) {
        checkChildParent(element);
        addChild(pos, element);
        setChildParent(element);
        return element;
    }

    public XmlElement addElement(XmlNamespace namespace, String name) {
        XmlElement el = newElement(namespace, name);
        addChild(el);
        setChildParent(el);
        return el;
    }

    public XmlElement addElement(String name) {
        return addElement(null, name);
    }

    public Iterator children() {
        if(children == null) {
            return EMPTY_ITERATOR;
        }
        return children.iterator();
    }

    public Iterable requiredElementContent() {
        if(children == null) {
            return EMPTY_ITERABLE;
        }
        return new Iterable() {
            public Iterator iterator() {
                return new RequiredElementContentIterator(children.iterator());
            }
        };
    }

    public String requiredTextContent() {
        if(children == null) {
            //            throw new XmlBuilderException("element {"+getNamespace().getNamespaceName()+"}"
            //                                              +getName()+" has no content");
            return "";
        }
        if(children.size() == 0) {
            return "";
        } else if(children.size() == 1) {
            Object child = children.get(0);
            if(child instanceof String) {
                return child.toString();
            } else if(child instanceof XmlCharacters) {
                return ((XmlCharacters)child).getText();
            } else {
                throw new XmlBuilderException("expected text content and not "
                                                  +(child != null ? child.getClass() : null)
                                                  +" with '"+child+"'");
            }
        }
        Iterator i = children();
        StringBuffer buf = new StringBuffer();
        while(i.hasNext()) {
            Object child = i.next();
            if(child instanceof String) {
                buf.append(child.toString());
            } else if(child instanceof XmlCharacters) {
                buf.append(((XmlCharacters)child).getText());
            } else {
                throw new XmlBuilderException("expected text content and not "+child.getClass()
                                                  +" with '"+child+"'");
            }
        }
        return buf.toString();
    }

    public void ensureChildrenCapacity(int minCapacity)
    {
        if(children == null) {
            children = new ArrayList(minCapacity);
        } else {
            ((ArrayList)children).ensureCapacity(minCapacity);
        }
    }

    public XmlElement element(int position) {
        if(children == null) return null;
        int length = children.size();
        int count = 0;
        if(position >= 0 && position < (length + 1)) {
            int pos = 0;
            while(pos < length) {
                Object child = children.get(pos);
                if(child instanceof XmlElement) {
                    ++count;
                    if(count == position) {
                        return (XmlElement) child;
                    }
                }
                ++pos;
            }
        } else  { //TODO:CTX
            throw new IndexOutOfBoundsException(
                "position "+position+" bigger or equal to "+length+" children");
        } //TODO:CTX
        throw new IndexOutOfBoundsException(
            "position "+position+" too big as only "+count+" element(s) available");
    }


    public XmlElement requiredElement(XmlNamespace n, String name) throws XmlBuilderException {
        XmlElement el = element(n, name);
        if(el == null) { //TODO:CTX
            throw new XmlBuilderException("could not find element with name "+name
                                              +" in namespace "+(n !=null ? n.getNamespaceName() : null));
        }
        return el;
    }

    public XmlElement element(XmlNamespace n, String name) {
        return element(n, name, false);
    }
    public XmlElement element(XmlNamespace n, String name, boolean create) {
        XmlElement e = n != null ? findElementByName(n.getNamespaceName(), name) : findElementByName(name);
        if(e != null) {
            return e;
        }
        //e =new XmlElementImpl(n, name);
        if(create) {
            return addElement(n, name);
        } else {
            return null;
        }
    }

    public Iterable elements(final XmlNamespace n, final String name) {
        return new Iterable() {
            public Iterator iterator() {
                return new ElementsSimpleIterator(n, name, children());
            }
        };
    }

    public XmlElement findElementByName(String name) {
        if(children == null) return null;
        int length = children.size();
        for (int i = 0; i < length; i++)
        {
            Object child = children.get(i);
            if(child instanceof XmlElement) {
                XmlElement childEl = (XmlElement) child;
                if(name.equals(childEl.getName())) {
                    return childEl;
                }
            }
        }
        return null;
    }

    public XmlElement findElementByName(String namespaceName, String name,
                                        XmlElement elementToStartLooking) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public XmlElement findElementByName(String name,
                                        XmlElement elementToStartLooking)
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    public XmlElement findElementByName(String namespaceName, String name) {
        if(children == null) return null;
        int length = children.size();
        for (int i = 0; i < length; i++)
        {
            Object child = children.get(i);
            if(child instanceof XmlElement) {
                XmlElement childEl = (XmlElement) child;
                XmlNamespace namespace = childEl.getNamespace();
                if (namespace != null) {
                    if ((name.equals(childEl.getName())) && (namespaceName.equals(namespace.getNamespaceName()))) {
                        return childEl;
                    }
                } else {
                    if ((name.equals(childEl.getName())) && (namespaceName == null)) {
                        return childEl;
                    }
                }
            }
        }

        return null;
    }


    public boolean hasChild(Object child) {
        if(children == null) {
            return false;
        }
        for (int i = 0; i < children.size(); i++)
        {
            if(children.get(i) == child) {
                return true;
            }
        }
        return false;
    }


    public boolean hasChildren() {
        return children != null && children.size() > 0;
    }

    public void insertChild(int pos, Object childToInsert) {
//        children.set(pos, childToInsert);
            if (children == null) ensureChildrenCapacity(1);
            children.add(pos, childToInsert);
    }

    public XmlElement newElement(String name) {
        return newElement((XmlNamespace)null, name);
    }

    public XmlElement newElement(String namespace, String name) {
        return new XmlElementImpl(namespace, name);
    }

    public XmlElement newElement(XmlNamespace namespace, String name) {
        return new XmlElementImpl(namespace, name);
    }

    public void replaceChild(Object newChild, Object oldChild) {
        if(newChild == null) {
            throw new IllegalArgumentException("new child to replace can not be null");
        }
        if(oldChild == null) {
            throw new IllegalArgumentException("old child to replace can not be null");
        }
        if(!hasChildren()) {
            throw new XmlBuilderException("no children available for replacement");
        }
        int pos = children.indexOf(oldChild);
        if(pos == -1) {
            throw new XmlBuilderException("could not find child to replace");
        }
        children.set(pos, newChild);
    }

    public void removeAllChildren() {
        children = null;
    }

    public void removeChild(Object child) {
        if(child == null) {
            throw new IllegalArgumentException("child to remove can not be null");
        }
        if(!hasChildren()) {
            throw new XmlBuilderException("no children to remove");
        }
        int pos = children.indexOf(child);
        if(pos != -1) {
            children.remove(pos);
        }
        //      int length = children.size();
        //      for (int i = 0; i < length; i++)
        //      {
        //          Object o = children.get(i);
        //          if(o == child) {
        //          }
        //      }
    }


    public void replaceChildrenWithText(String textContent) {
        removeAllChildren();
        addChild(textContent);
    }

    private static final boolean isWhiteSpace(String txt)
    {
        for (int i = 0; i < txt.length(); i++)
        {
            if ( (txt.charAt(i) != ' ') &&
                    (txt.charAt(i) != '\n') &&
                    (txt.charAt(i) != '\t') &&
                    (txt.charAt(i) != '\r'))
            {
                return false;
            }
        }

        return true;
    }

    private class ElementsSimpleIterator implements Iterator {

        private Iterator children;
        private XmlElement currentEl;

        private XmlNamespace n;

        private String name;

        ElementsSimpleIterator(final XmlNamespace n, final String name, Iterator children) {
            this.children = children;
            this.n = n;
            this.name = name;
            findNextEl();
        }

        private void findNextEl() {
            currentEl = null;
            while(children.hasNext()) {
                Object child = children.next();
                if(child instanceof XmlElement) {
                    XmlElement el = (XmlElement) child;
                    if( (name == null || el.getName() == name || name.equals(el.getName()))
                           && (n == null || el.getNamespace() == n || n.equals(el.getNamespace()))
                      )
                    {
                        currentEl = el;
                        break;
                    }
                }
            }
        }

        public boolean hasNext() {
            return currentEl != null;
        }

        public Object next() {
            if(currentEl == null) {
                throw new XmlBuilderException(
                    "this iterator has no content and next() is not allowed");
            }
            XmlElement el = currentEl;
            findNextEl();
            return el;
        }

        public void remove() {
            throw new XmlBuilderException(
                "this element iterator does nto support remove()");
        }

    }

    private static class RequiredElementContentIterator implements Iterator {

        private Iterator children;
        private XmlElement currentEl;

        RequiredElementContentIterator(Iterator children) {
            this.children = children;
            findNextEl();
        }

        private void findNextEl() {
            currentEl = null;
            while(children.hasNext()) {
                Object child = children.next();
                if(child instanceof XmlElement) {
                    currentEl = (XmlElement) child;
                    break;
                } else if(child instanceof String) {
                    String s = child.toString();
                    if(false == isWhiteSpace(s)) {
                        throw new XmlBuilderException( //TODO parent.getPositionDesc() ...
                            "only whitespace string children allowed for non mixed element content");
                    }
                } else if(child instanceof XmlCharacters) {
                    XmlCharacters xc = (XmlCharacters) child;
                    if(!Boolean.TRUE.equals(xc.isWhitespaceContent())
                           || false == isWhiteSpace(xc.getText()) )
                    {
                        throw new XmlBuilderException( //TODO parent.getPositionDesc() ...
                            "only whitespace characters children allowed for non mixed element content");
                    }

                } else {
                    throw new XmlBuilderException( //TODO parent.getPositionDesc() ...
                        "only whitespace characters and element children allowed "+
                            "for non mixed element content and not "+child.getClass());
                }
            }
        }

        public boolean hasNext() {
            return currentEl != null;
        }

        public Object next() {
            if(currentEl == null) {
                throw new XmlBuilderException(
                    "this iterator has no content and next() is not allowed");
            }
            XmlElement el = currentEl;
            findNextEl();
            return el;
        }

        public void remove() {
            throw new XmlBuilderException(
                "this iterator does nto support remove()");
        }
    }

    private static class EmptyIterator implements Iterator {
        public boolean hasNext() {
            return false;
        }

        public Object next() {
            throw new XmlBuilderException(
                "this iterator has no content and next() is not allowed");
        }

        public void remove() {
            throw new XmlBuilderException(
                "this iterator has no content and remove() is not allowed");
        }
    }

    private static final Iterator EMPTY_ITERATOR = new EmptyIterator();

    private static final Iterable EMPTY_ITERABLE = new Iterable() {

        public Iterator iterator() {
            return EMPTY_ITERATOR;
        }
    };


}




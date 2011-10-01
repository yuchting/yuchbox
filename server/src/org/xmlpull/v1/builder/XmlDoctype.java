package org.xmlpull.v1.builder;

import java.util.Iterator;

/**
 * Represents
 * <a href="http://www.w3.org/TR/xml-infoset/#infoitem.doctype">Document Type Declaration
 * Information Item</a>.
 * If the XML document has a document type declaration,
 * then the information set contains a single document type declaration information item.
 * Note that entities and notations are provided as properties of the document information item,
 * not the document type declaration information item.
 *
 * @version $Revision: 1.2 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public interface XmlDoctype extends XmlContainer
{
    /**
     * The system identifier of the external subset, as it appears in the DOCTYPE declaration,
     * without any additional URI escaping applied by the processor.
     * If there is no external subset this property has no value.
     */
    public String getSystemIdentifier();

    /**
     * The public identifier of the external subset, normalized as described in
     * <a href="http://www.w3.org/TR/REC-xml#sec-external-ent">4.2.2 External Entities [XML]</a>.
     * If there is no external subset or if it has no public identifier,
     * this property has no value.
     */
    public String getPublicIdentifier();

    /**
     * An ordered list of processing instruction information items representing processing
     * instructions appearing in the DTD, in the original document order.
     * Items from the internal DTD subset appear before those in the external subset.
     */
    public Iterator children();

    /**
     * The document information item.
     */
    public XmlDocument getParent();


    // manipulate children
    /**
     * Add to list of children (only processing instruction information items are allowed).
     */
    public XmlProcessingInstruction addProcessingInstruction(String target, String content);

    /**
     * Remove all children.
     */
    public void removeAllProcessingInstructions();
}


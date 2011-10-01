package org.xmlpull.v1.builder;

/**
 * Represents
 * <a href="http://www.w3.org/TR/xml-infoset/#infoitem.pi">Processing Instruction Information Item</a>
 * .
 * There is a processing instruction information item
 * for each processing instruction in the document.
 * The XML declaration and text declarations for external parsed entities
 * are not considered processing instructions.
 *
 * @version $Revision: 1.3 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */

public interface XmlProcessingInstruction //extends XmlContainer
{
    /**
     * A string representing the target part of the processing instruction (an XML name).
     */
    public String getTarget();

    /**
     * A string representing the content of the processing instruction,
     * excluding the target and any white space immediately following it.
     * If there is no such content, the value of this property will be an empty string.
     */
    public String getContent();

    //TODO: not clear how this should be implemented ...
    /**
     * The base URI of the PI. Note that if an infoset is serialized as an XML document,
     * it will not be possible to preserve the base URI
     * of any PI that originally appeared at the top level of an external entity,
     * since there is no syntax for PIs corresponding to the xml:base attribute on elements.
     */
    public String getBaseUri();

    //TODO: not clear how this should be implemented ...
    /**
     * The notation information item named by the target.
     * If there is no declaration for a notation with that name,
     * this property has no value. If no declaration has been read,
     * but the [all declarations processed] property of the document information item is false
     * (so there may be an unread declaration), then the value of this property is unknown.
     */
    public XmlNotation getNotation();

    /**
     * The document, element, or document type definition information item
     * which contains this information item in its [children] property.
     */
    public XmlContainer getParent();

}


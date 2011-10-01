package org.xmlpull.v1.builder;

/**
 * Represents
 * <a href="http://www.w3.org/TR/xml-infoset/#infoitem.notation">Notation Information Item</a>.
 * There is a notation information item for each notation declared in the DTD.
 *
 * @version $Revision: 1.3 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */

public interface XmlNotation //extends XmlContainer
{
    /**
     * The name of the notation.
     */
    public String getName();

    /**
     * The system identifier of the notation,
     * as it appears in the declaration of the notation,
     * without any additional URI escaping applied by the processor.
     * If no system identifier was specified, this property has no value.
     */
    public String getSystemIdentifier();

    /**
     * The public identifier of the notation, normalized as described in
     * <a href="http://www.w3.org/TR/REC-xml#dt-pubid">4.2.2 External Entities [XML]</a>.
     * If the notation has no public identifier, this property has no value.
     */
    public String getPublicIdentifier();

    //TOOD how to implement this ...
    /**
     * The base URI relative to which the system identifier should be resolved
     * (i.e. the base URI of the resource within which the notation declaration occurs).
     */
    public String getDeclarationBaseUri();

}


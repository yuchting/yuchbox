package org.xmlpull.v1.builder;

import java.util.Iterator;

//JDK15 remove and replace usage with real Iterable
/**
 * Use java.lang.Iterable instead when JDK 1.5 comes out ...*
 *
 * @version $Revision: 1.3 $
 * @author <a href="http://www.extreme.indiana.edu/~aslom/">Aleksander Slominski</a>
 */
public interface Iterable
{
    public Iterator iterator();
}


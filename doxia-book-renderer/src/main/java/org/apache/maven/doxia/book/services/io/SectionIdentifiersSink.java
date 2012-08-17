package org.apache.maven.doxia.book.services.io;

import org.apache.maven.doxia.sink.SinkEventAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Obtains Section identifiers by listening for section events from a Doxia event stream.
 */
public class SectionIdentifiersSink extends BaseIoSink
{

    private List<String> sectionIds = new ArrayList<String>();

    /**
     * Obtain the section identifiers captured by this Sink, in document order.
     *
     * @return a list of section identifiers in document order
     */
    public List<String> getSectionIds()
    {
        return sectionIds;
    }

    private void handleSectionId( SinkEventAttributes attributes, List<String> sectionIds )
    {
        String id = ( String ) attributes.getAttribute( SinkEventAttributes.ID );
        if ( id != null && id.trim().length() > 0 )
        {
            sectionIds.add( id );
        }
    }

    public void section( int level, SinkEventAttributes attributes )
    {
        handleSectionId( attributes, sectionIds );
    }

}

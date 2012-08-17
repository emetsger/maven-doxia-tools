package org.apache.maven.doxia.book.services.io;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributes;

import java.util.Enumeration;

/**
 * Accepts all Doxia events emitted between the "opening" and "closing" of a specified event.
 */
public class EventFilteringSink extends FilteringSink
{
    /** The Doxia Event that we listen for */
    private DoxiaEvent event;

    /** The SinkEventAttributes passed along with the {@link #event} */
    private SinkEventAttributes requiredAttributes;

    /**
     * Tracks the state of an event: if we have seen it, if it has been closed, and the {@link EventEntry}
     * itself.
     */
    private class EventState {
        private boolean seen = false;
        private boolean closed = false;
        private EventEntry entry = null;

        @Override
        public String toString()
        {
            return "EventState{" +
                    "closed=" + closed +
                    ", seen=" + seen +
                    ", entry=" + entry +
                    '}';
        }
    }

    /** The state for {@link #event} */
    private EventState state = new EventState();

    /**
     * Constructs a new sink, including events that occur between the start and end event (inclusive) of the
     * specified {@code event}; the other events received by this sink are ignored.
     *
     * @param delegate the {@code Sink} which receives the filtered events
     * @param event the event to match
     * @param requiredAttributes the attributes that the event must carry in order to match
     */
    public EventFilteringSink( Sink delegate, DoxiaEvent event, SinkEventAttributes requiredAttributes )
    {
        super( delegate );
        this.event = event;
        this.requiredAttributes = requiredAttributes;
    }

    @Override
    protected boolean accept( DoxiaEvent event, SinkEventAttributes eventAttributes, Object... params )
    {
        // Construct and push the EventEntry onto the stack if it is not self-closing; by default this
        // event has not been accepted.
        EventEntry entry = new EventEntry( event, params, eventAttributes, false );
        if ( !event.isSelfClosing() )
        {
            events.push( entry );
        }

        // If the event hasn't been seen, and it isn't closed ...
        if ( !state.seen && event == this.event )
        {
            if ( requiredAttributes != null )
            {
                // Insure the required attributes and values from {@link #attrs} are present with the event's attributes
                Enumeration attributeNames = requiredAttributes.getAttributeNames();
                while ( attributeNames.hasMoreElements() )
                {
                    String attrName = ( String ) attributeNames.nextElement();
                    if ( !eventAttributes.containsAttribute( attrName, requiredAttributes.getAttribute( attrName ) ) )
                    {
                        return false;
                    }
                }
            }


            // Update the state
            state.entry = entry;
            state.seen = true;

            // Accept the event
            entry.accepted = true;
            return true;
        }

        // If the event has been seen, and the event has been closed (that is, the entry has popped off the stack) ...
        if ( state.seen && events.search( state.entry ) < 0 )
        {
            // Update the state
            state.closed = true;

            // Reject the event
            entry.accepted = false;
            return false;
        }

        // If the event has been seen, and the event hasn't been closed ...
        if ( state.seen && !state.closed )
        {
            // Accept the event
            entry.accepted = true;
            return true;
        }

        // By default reject the event
        entry.accepted = false;
        return false;
    }

    @Override
    public String toString()
    {
        return "EventFilteringSink{" +
                "attrs=" + requiredAttributes +
                ", event=" + event +
                ", state=" + state +
                '}';
    }
}

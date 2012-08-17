package org.apache.maven.doxia.book.services.io;

import org.apache.maven.doxia.logging.Log;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Filters {@link DoxiaEvent}s that are passed to a delegate {@link Sink}.  Useful for decorating a {@code Sink} that
 * should only consider a portion of a Doxia event stream.  By default this implementation filters nothing, and passes
 * all events to the delegate supplied on construction.
 */
public class FilteringSink extends BaseIoSink
{
    /** Default Set of Doxia events to accept; all Doxia events are accepted. */
    private static final Set<DoxiaEvent> ACCEPT_EVENT_DEFAULT = new HashSet<DoxiaEvent>();

    static
    {
        ACCEPT_EVENT_DEFAULT.addAll( Arrays.asList( DoxiaEvent.values() ) );
    }

    /**
     * Captures the firing of a {@link DoxiaEvent} and its context. Encapsulates a {@code DoxiaEvent}, its parameters,
     * any {@link SinkEventAttributes}, and the result of the {@link FilteringSink#accept(DoxiaEvent, SinkEventAttributes, Object...) accept}
     * method.  Useful for maintaining a {@code Stack} of events handled by the {@code FilteringSink}.
     * <p/>
     * Note that only the {@code DoxiaEvent} and {@code SinkEventAttributes} are considered for
     * {@link #equals(Object) equality}.  This means that two {@code EventEntry}s will be considered equal if they
     * encapsulate the same {@code DoxiaEvent} and same attribute names.
     */
    protected class EventEntry
    {
        /** The {@code DoxiaEvent} */
        protected DoxiaEvent event;

        /** The parameters supplied to the {@code Sink} method */
        protected Object[] parameters;

        /** The attributes supplied to the {@code Sink} method */
        protected SinkEventAttributes attributes;

        /**
         * Whether or not the {@link #event} was
         * {@link #accept(DoxiaEvent, org.apache.maven.doxia.sink.SinkEventAttributes, Object...) accepted}
         */
        protected boolean accepted;

        /**
         * Constructs an EventEntry with the supplied state.
         *
         * @param event the event
         * @param parameters {@code Sink} method parameter values
         * @param attributes {@code Sink} method {@code SinkEventAttributes}
         * @param accepted whether or not the event was
         *                 {@link FilteringSink#accept(DoxiaEvent, SinkEventAttributes, Object...) accept}ed
         */
        protected EventEntry( DoxiaEvent event, Object[] parameters, SinkEventAttributes attributes, boolean accepted )
        {
            this.accepted = accepted;
            this.attributes = attributes;
            this.event = event;
            this.parameters = parameters;
        }

        /**
         * Note that only the {@code DoxiaEvent} and {@code SinkEventAttributes} are considered for equality. This means
         * that two {@code EventEntry}s will be considered equal if they encapsulate the same {@code DoxiaEvent} and
         * same attribute names.
         *
         * @param o an object
         * @return true if {@code o} equals this
         */
        @Override
        public boolean equals( Object o )
        {
            if ( this == o )
            {
                return true;
            }
            if ( o == null || getClass() != o.getClass() )
            {
                return false;
            }

            EventEntry that = ( EventEntry ) o;

            if ( attributes != null ? !attributes.equals( that.attributes ) : that.attributes != null )
            {
                return false;
            }
            if ( event != that.event )
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = event != null ? event.hashCode() : 0;
            result = 31 * result + ( attributes != null ? attributes.hashCode() : 0 );
            return result;
        }

        @Override
        public String toString()
        {
            return "EventEntry{" +
                    "accepted=" + accepted +
                    ", event=" + event +
                    ", parameters=" + ( parameters == null ? null : Arrays.asList( parameters ) ) +
                    ", attributes=" + attributes +
                    '}';
        }
    }

    /**
     * A {@link Stack} of events maintained by the {@link #accept(DoxiaEvent, SinkEventAttributes, Object...) accept}
     * method.
     */
    protected Stack<EventEntry> events = new Stack<EventEntry>();

    /** Set of {@link DoxiaEvent}s to accept, initialized to the default set */
    protected Set<DoxiaEvent> eventsToAccept = ACCEPT_EVENT_DEFAULT;

    /** The delegate Sink to forward accepted events to */
    private final Sink delegate;

    /**
     * Constructs a {@link Sink} that accepts all Doxia events.
     *
     * @param delegate the Sink to pass accepted events to
     */
    public FilteringSink( Sink delegate )
    {
        this.delegate = delegate;
    }

    /**
     * Constructs a {@link Sink} that only accepts the supplied events.
     *
     * @param delegate       the Sink to pass accepted events to
     * @param eventsToAccept the Doxia events to accept
     */
    public FilteringSink( Sink delegate, DoxiaEvent... eventsToAccept )
    {
        this( delegate );

        Set<DoxiaEvent> events = new HashSet<DoxiaEvent>();
        events.addAll( Arrays.asList( eventsToAccept ) );
        this.eventsToAccept = events;
    }

    /**
     * Determines whether or not the supplied Doxia event will be accepted.  If {@code true}, the event
     * will be passed to the delegate {@code Sink}.  If {@code false}, the event is ignored, and not passed
     * to the delegate.  By default this implementation accepts all events, passing all events to the delegate.
     * <p/>
     * The responsibilities of this method not only include simply accepting or rejecting an event, but extend to
     * maintaining a {@link Stack} of {@link #events}.  Specifically, all events that require "closing" must be pushed
     * onto the event stack, regardless of whether or not they are accepted.  This is accomplished by testing whether
     * or not the event {@link DoxiaEvent#isSelfClosing() requires closing}, and if so, constructing a new
     * {@link EventEntry} and pushing it onto the {@link #events event stack}.
     * <p/>
     * Subclasses may override this method; for example, to consider the event attributes when determining whether
     * or not to accept the DoxiaEvent.  Subclasses must properly maintain the content of the {@link #events event
     * stack} so that the {@code FilteringSink} knows when to emit the "closing" events (<em>e.g.</em>
     * {@link #anchor_()})
     *
     * @param event           the Doxia event
     * @param eventAttributes event attributes, may be {@code null}
     * @param params          parameters supplied to the event method
     * @return true if the event is to be accepted and passed to the delegate {@code Sink}
     */
    protected boolean accept( DoxiaEvent event, SinkEventAttributes eventAttributes, Object... params )
    {
        EventEntry entry = new EventEntry( event, params, eventAttributes, false );

        // if the DoxiaEvent closes itself (e.g. there's no corresponding <method>_()), don't add it to the stack
        if (! event.isSelfClosing() )
        {
            events.push( entry );
        }

        if ( eventsToAccept.contains( event ) )
        {
            entry.accepted = true;
            return true;
        }

        return false;
    }

    @Override
    public void anchor( String name )
    {
        if ( accept( DoxiaEvent.ANCHOR, null, name ) )
        {
            delegate.anchor( name );
        }
    }

    @Override
    public void anchor( String name, SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.ANCHOR, attributes, name ) )
        {
            delegate.anchor( name, attributes );
        }
    }

    @Override
    public void anchor_()
    {
        if ( events.pop().accepted )
        {
            delegate.anchor_();
        }
    }

    @Override
    public void author()
    {
        if ( accept( DoxiaEvent.AUTHOR, null, null ) )
        {
            delegate.author();
        }
    }

    @Override
    public void author( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.AUTHOR, attributes, null ) )
        {
            delegate.author( attributes );
        }
    }

    @Override
    public void author_()
    {
        if ( events.pop().accepted )
        {
            delegate.author_();
        }
    }

    @Override
    public void body()
    {
        if ( accept( DoxiaEvent.BODY, null, null ) )
        {
            delegate.body();
        }
    }

    @Override
    public void body( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.BODY, attributes, null ) )
        {
            delegate.body( attributes );
        }
    }

    @Override
    public void body_()
    {
        if ( events.pop().accepted )
        {
            delegate.body_();
        }
    }

    @Override
    public void bold()
    {
        if ( accept( DoxiaEvent.BOLD, null, null ) )
        {
            delegate.bold();
        }
    }

    @Override
    public void bold_()
    {
        if ( events.pop().accepted )
        {
            delegate.bold_();
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Closes the delegate {@link Sink}.
     */
    @Override
    public void close()
    {
        delegate.close();
    }

    @Override
    public void comment( String comment )
    {
        if ( accept( DoxiaEvent.COMMENT, null, comment ) )
        {
            delegate.comment( comment );
        }
    }

    @Override
    public void date()
    {
        if ( accept( DoxiaEvent.DATE, null, null ) )
        {
            delegate.date();
        }
    }

    @Override
    public void date( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.DATE, attributes, null ) )
        {
            delegate.date( attributes );
        }
    }

    @Override
    public void date_()
    {
        if ( events.pop().accepted )
        {
            delegate.date_();
        }
    }

    @Override
    public void definedTerm()
    {
        if ( accept( DoxiaEvent.DEFINED_TERM, null, null ) )
        {
            delegate.definedTerm();
        }
    }

    @Override
    public void definedTerm( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.DEFINED_TERM, attributes, null ) )
        {
            delegate.definedTerm( attributes );
        }
    }

    @Override
    public void definedTerm_()
    {
        if ( events.pop().accepted )
        {
            delegate.definedTerm_();
        }
    }

    @Override
    public void definition()
    {
        if ( accept( DoxiaEvent.DEFINITION, null, null ) )
        {
            delegate.definition();
        }
    }

    @Override
    public void definition( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.DEFINITION, attributes, null ) )
        {
            delegate.definition( attributes );
        }
    }

    @Override
    public void definition_()
    {
        if ( events.pop().accepted )
        {
            delegate.definition_();
        }
    }

    @Override
    public void definitionList()
    {
        if ( accept( DoxiaEvent.DEFINITION_LIST, null, null ) )
        {
            delegate.definition();
        }
    }

    @Override
    public void definitionList( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.DEFINITION_LIST, attributes, null ) )
        {
            delegate.definition( attributes );
        }
    }

    @Override
    public void definitionList_()
    {
        if ( events.pop().accepted )
        {
            delegate.definitionList_();
        }
    }

    @Override
    public void definitionListItem()
    {
        if ( accept( DoxiaEvent.DEFINITION_LIST_ITEM, null, null ) )
        {
            delegate.definitionListItem();
        }
    }

    @Override
    public void definitionListItem( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.DEFINITION_LIST_ITEM, attributes, null ) )
        {
            delegate.definitionListItem( attributes );
        }

    }

    @Override
    public void definitionListItem_()
    {
        if ( events.pop().accepted )
        {
            delegate.definitionListItem_();
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Enables logging on the delegate.
     *
     * @param log the log
     */
    @Override
    public void enableLogging( Log log )
    {
        delegate.enableLogging( log );
    }

    @Override
    public void figure()
    {
        if ( accept( DoxiaEvent.FIGURE, null, null ) )
        {
            delegate.figure();
        }
    }

    @Override
    public void figure( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.FIGURE, attributes, null ) )
        {
            delegate.figure( attributes );
        }
    }

    @Override
    public void figure_()
    {
        if ( events.pop().accepted )
        {
            delegate.figure_();
        }
    }

    @Override
    public void figureCaption()
    {
        if ( accept( DoxiaEvent.FIGURE_CAPTION, null, null ) )
        {
            delegate.figureCaption();
        }
    }

    @Override
    public void figureCaption( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.FIGURE_CAPTION, attributes, null ) )
        {
            delegate.figureCaption( attributes );
        }
    }

    @Override
    public void figureCaption_()
    {
        if ( events.pop().accepted )
        {
            delegate.figureCaption_();
        }
    }

    @Override
    public void figureGraphics( String name )
    {
        if ( accept( DoxiaEvent.FIGURE_GRAPHICS, null, name ) )
        {
            delegate.figureGraphics( name );
        }
    }

    @Override
    public void figureGraphics( String src, SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.FIGURE_GRAPHICS, attributes, src ) )
        {
            delegate.figureGraphics( src, attributes );
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Flushes the delegate.
     */
    @Override
    public void flush()
    {
        delegate.flush();
    }

    @Override
    public void head()
    {
        if ( accept( DoxiaEvent.HEAD, null, null ) )
        {
            delegate.head();
        }
    }

    @Override
    public void head( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.HEAD, attributes, null ) )
        {
            delegate.head( attributes );
        }
    }

    @Override
    public void head_()
    {
        if ( events.pop().accepted )
        {
            delegate.head_();
        }
    }

    @Override
    public void horizontalRule()
    {
        if ( accept( DoxiaEvent.HORIZONTAL_RULE, null, null ) )
        {
            delegate.horizontalRule();
        }
    }

    @Override
    public void horizontalRule( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.HORIZONTAL_RULE, attributes, null ) )
        {
            delegate.horizontalRule( attributes );
        }
    }

    @Override
    public void italic()
    {
        if ( accept( DoxiaEvent.ITALIC, null, null ) )
        {
            delegate.italic();
        }
    }

    @Override
    public void italic_()
    {
        if ( events.pop().accepted )
        {
            delegate.italic_();
        }
    }

    @Override
    public void lineBreak()
    {
        if ( accept( DoxiaEvent.LINE_BREAK, null, null ) )
        {
            delegate.lineBreak();
        }
    }

    @Override
    public void lineBreak( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.LINE_BREAK, attributes, null ) )
        {
            delegate.lineBreak( attributes );
        }
    }

    @Override
    public void link( String name )
    {
        if ( accept( DoxiaEvent.LINK, null, name ) )
        {
            delegate.link( name );
        }
    }

    @Override
    public void link( String name, SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.LINK, attributes, name ) )
        {
            delegate.link( name, attributes );
        }
    }

    @Override
    public void link_()
    {
        if ( events.pop().accepted )
        {
            delegate.link_();
        }
    }

    @Override
    public void list()
    {
        if ( accept( DoxiaEvent.LIST, null, null ) )
        {
            delegate.list();
        }
    }

    @Override
    public void list( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.LIST, attributes, null ) )
        {
            delegate.list( attributes );
        }
    }

    @Override
    public void list_()
    {
        if ( events.pop().accepted )
        {
            delegate.list_();
        }
    }

    @Override
    public void listItem()
    {
        if ( accept( DoxiaEvent.LIST_ITEM, null, null ) )
        {
            delegate.listItem();
        }
    }

    @Override
    public void listItem( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.LIST_ITEM, attributes, null ) )
        {
            delegate.listItem( attributes );
        }
    }

    @Override
    public void listItem_()
    {
        if ( events.pop().accepted )
        {
            delegate.listItem_();
        }
    }

    @Override
    public void monospaced()
    {
        if ( accept( DoxiaEvent.MONO_SPACED, null, null ) )
        {
            delegate.monospaced();
        }
    }

    @Override
    public void monospaced_()
    {
        if ( events.pop().accepted )
        {
            delegate.monospaced_();
        }
    }

    @Override
    public void nonBreakingSpace()
    {
        if ( accept( DoxiaEvent.NON_BREAKING_SPACE, null, null ) )
        {
            delegate.nonBreakingSpace();
        }
    }

    @Override
    public void numberedList( int numbering )
    {
        if ( accept( DoxiaEvent.NUMBERED_LIST, null, numbering ) )
        {
            delegate.numberedList( numbering );
        }
    }

    @Override
    public void numberedList( int numbering, SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.NUMBERED_LIST, attributes, numbering ) )
        {
            delegate.numberedList( numbering, attributes );
        }
    }

    @Override
    public void numberedList_()
    {
        if ( events.pop().accepted )
        {
            delegate.numberedList_();
        }
    }

    @Override
    public void numberedListItem()
    {
        if ( accept( DoxiaEvent.NUMBERED_LIST_ITEM, null, null ) )
        {
            delegate.numberedListItem();
        }
    }

    @Override
    public void numberedListItem( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.NUMBERED_LIST_ITEM, attributes, null ) )
        {
            delegate.numberedListItem( attributes );
        }
    }

    @Override
    public void numberedListItem_()
    {
        if ( events.pop().accepted )
        {
            delegate.numberedListItem_();
        }
    }

    @Override
    public void pageBreak()
    {
        if ( accept( DoxiaEvent.PAGE_BREAK, null, null ) )
        {
            delegate.pageBreak();
        }
    }

    @Override
    public void paragraph()
    {
        if ( accept( DoxiaEvent.PARAGRAPH, null, null ) )
        {
            delegate.paragraph();
        }
    }

    @Override
    public void paragraph( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.PARAGRAPH, attributes, null ) )
        {
            delegate.paragraph( attributes );
        }
    }

    @Override
    public void paragraph_()
    {
        if ( events.pop().accepted )
        {
            delegate.paragraph_();
        }
    }

    @Override
    public void rawText( String text )
    {
        if ( accept( DoxiaEvent.RAW_TEXT, null, text ) )
        {
            delegate.rawText( text );
        }
    }

    @Override
    public void section( int level, SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.SECTION, attributes, level ) )
        {
            delegate.section( level, attributes );
        }
    }

    @Override
    public void section1()
    {
        if ( accept( DoxiaEvent.SECTION_1, null, null ) )
        {
            delegate.section1();
        }
    }

    @Override
    public void section1_()
    {
        if ( events.pop().accepted )
        {
            delegate.section1_();
        }
    }

    @Override
    public void section2()
    {
        if ( accept( DoxiaEvent.SECTION_2, null, null ) )
        {
            delegate.section2();
        }
    }

    @Override
    public void section2_()
    {
        if ( events.pop().accepted )
        {
            delegate.section2_();
        }
    }

    @Override
    public void section3()
    {
        if ( accept( DoxiaEvent.SECTION_3, null, null ) )
        {
            delegate.section3();
        }
    }

    @Override
    public void section3_()
    {
        if ( events.pop().accepted )
        {
            delegate.section3_();
        }
    }

    @Override
    public void section4()
    {
        if ( accept( DoxiaEvent.SECTION_4, null, null ) )
        {
            delegate.section4();
        }
    }

    @Override
    public void section4_()
    {
        if ( events.pop().accepted )
        {
            delegate.section4_();
        }
    }

    @Override
    public void section5()
    {
        if ( accept( DoxiaEvent.SECTION_5, null, null ) )
        {
            delegate.section5();
        }
    }

    @Override
    public void section5_()
    {
        if ( events.pop().accepted )
        {
            delegate.section5_();
        }
    }

    @Override
    public void section_( int level )
    {
        if ( events.pop().accepted )
        {
            delegate.section_( level );
        }
    }

    @Override
    public void sectionTitle()
    {
        if ( accept( DoxiaEvent.SECTION_TITLE, null, null ) )
        {
            delegate.sectionTitle();
        }
    }

    @Override
    public void sectionTitle( int level, SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.SECTION_TITLE, attributes, level ) )
        {
            delegate.sectionTitle( level, attributes );
        }
    }

    @Override
    public void sectionTitle1()
    {
        if ( accept( DoxiaEvent.SECTION_TITLE_1, null, null ) )
        {
            delegate.sectionTitle1();
        }
    }

    @Override
    public void sectionTitle1_()
    {
        if ( events.pop().accepted )
        {
            delegate.sectionTitle1_();
        }
    }

    @Override
    public void sectionTitle2()
    {
        if ( accept( DoxiaEvent.SECTION_TITLE_2, null, null ) )
        {
            delegate.sectionTitle2();
        }
    }

    @Override
    public void sectionTitle2_()
    {
        if ( events.pop().accepted )
        {
            delegate.sectionTitle2_();
        }
    }

    @Override
    public void sectionTitle3()
    {
        if ( accept( DoxiaEvent.SECTION_TITLE_3, null, null ) )
        {
            delegate.sectionTitle4();
        }
    }

    @Override
    public void sectionTitle3_()
    {
        if ( events.pop().accepted )
        {
            delegate.sectionTitle3_();
        }
    }

    @Override
    public void sectionTitle4()
    {
        if ( accept( DoxiaEvent.SECTION_TITLE_4, null, null ) )
        {
            delegate.sectionTitle4();
        }
    }

    @Override
    public void sectionTitle4_()
    {
        if ( events.pop().accepted )
        {
            delegate.sectionTitle4_();
        }
    }

    @Override
    public void sectionTitle5()
    {
        if ( accept( DoxiaEvent.SECTION_TITLE_5, null, null ) )
        {
            delegate.sectionTitle5();
        }
    }

    @Override
    public void sectionTitle5_()
    {
        if ( events.pop().accepted )
        {
            delegate.sectionTitle5_();
        }
    }

    @Override
    public void sectionTitle_()
    {
        if ( events.pop().accepted )
        {
            delegate.sectionTitle_();
        }
    }

    @Override
    public void sectionTitle_( int level )
    {
        if ( events.pop().accepted )
        {
            delegate.sectionTitle_( level );
        }
    }

    @Override
    public void table()
    {
        if ( accept( DoxiaEvent.TABLE, null, null ) )
        {
            delegate.table();
        }
    }

    @Override
    public void table( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.TABLE, attributes ) )
        {
            delegate.table( attributes );
        }
    }

    @Override
    public void table_()
    {
        if ( events.pop().accepted )
        {
            delegate.table_();
        }
    }

    @Override
    public void tableCaption()
    {
        if ( accept( DoxiaEvent.TABLE_CAPTION, null ) )
        {
            delegate.tableCaption();
        }
    }

    @Override
    public void tableCaption( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.TABLE_CAPTION, attributes ) )
        {
            delegate.tableCaption( attributes );
        }
    }

    @Override
    public void tableCaption_()
    {
        if ( events.pop().accepted )
        {
            delegate.tableCaption_();
        }
    }

    @Override
    public void tableCell()
    {
        if ( accept( DoxiaEvent.TABLE_CELL, null, null ) )
        {
            delegate.tableCell();
        }
    }

    @Override
    public void tableCell( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.TABLE_CELL, attributes, null ) )
        {
            delegate.tableCell( attributes );
        }
    }

    @Override
    public void tableCell( String width )
    {
        if ( accept( DoxiaEvent.TABLE_CELL, null, width ) )
        {
            delegate.tableCell( width );
        }
    }

    @Override
    public void tableCell_()
    {
        if ( events.pop().accepted )
        {
            delegate.tableCell_();
        }
    }

    @Override
    public void tableHeaderCell()
    {
        if ( accept( DoxiaEvent.TABLE_HEADER_CELL, null, null ) )
        {
            delegate.tableHeaderCell();
        }
    }

    @Override
    public void tableHeaderCell( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.TABLE_HEADER_CELL, attributes, null ) )
        {
            delegate.tableHeaderCell( attributes );
        }
    }

    @Override
    public void tableHeaderCell( String width )
    {
        if ( accept( DoxiaEvent.TABLE_HEADER_CELL, null, width ) )
        {
            delegate.tableHeaderCell( width );
        }
    }

    @Override
    public void tableHeaderCell_()
    {
        if ( events.pop().accepted )
        {
            delegate.tableHeaderCell_();
        }
    }

    @Override
    public void tableRow()
    {
        if ( accept( DoxiaEvent.TABLE_ROW, null, null ) )
        {
            delegate.tableRow();
        }
    }

    @Override
    public void tableRow( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.TABLE_ROW, attributes, null ) )
        {
            delegate.tableRow( attributes );
        }
    }

    @Override
    public void tableRow_()
    {
        if ( events.pop().accepted )
        {
            delegate.tableRow_();
        }
    }

    @Override
    public void tableRows( int[] justification, boolean grid )
    {
        if ( accept( DoxiaEvent.TABLE_ROWS, null, justification, grid ) )
        {
            delegate.tableRows( justification, grid );
        }
    }

    @Override
    public void tableRows_()
    {
        if ( events.pop().accepted )
        {
            delegate.tableRows_();
        }
    }

    @Override
    public void text( String text )
    {
        if ( accept( DoxiaEvent.TEXT, null, text ) )
        {
            delegate.text( text );
        }
    }

    @Override
    public void text( String text, SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.TEXT, attributes, text ) )
        {
            delegate.text( text, attributes );
        }
    }

    @Override
    public void title()
    {
        if ( accept( DoxiaEvent.TITLE, null, null ) )
        {
            delegate.title();
        }
    }

    @Override
    public void title( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.TITLE, attributes, null ) )
        {
            delegate.title( attributes );
        }
    }

    @Override
    public void title_()
    {
        if ( events.pop().accepted )
        {
            delegate.title_();
        }
    }

    @Override
    public void unknown( String name, Object[] requiredParams, SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.UNKNOWN, attributes, name, requiredParams ) )
        {
            delegate.unknown( name, requiredParams, attributes );
        }
    }

    @Override
    public void verbatim( SinkEventAttributes attributes )
    {
        if ( accept( DoxiaEvent.VERBATIM, attributes, null ) )
        {
            delegate.verbatim( attributes );
        }
    }

    @Override
    public void verbatim( boolean boxed )
    {
        if ( accept( DoxiaEvent.VERBATIM, null, boxed ) )
        {
            delegate.verbatim( boxed );
        }
    }

    @Override
    public void verbatim_()
    {
        if ( events.pop().accepted )
        {
            delegate.verbatim_();
        }
    }

}

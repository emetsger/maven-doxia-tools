package org.apache.maven.doxia.book.services.io;

/**
 * Enumeration of Doxia Events
 */
public enum DoxiaEvent
{
    ANCHOR,
    AUTHOR,
    BODY,
    BOLD,
    COMMENT( true ),
    DATE,
    DEFINED_TERM,
    DEFINITION,
    DEFINITION_LIST,
    DEFINITION_LIST_ITEM,
    FIGURE,
    FIGURE_CAPTION,
    FIGURE_GRAPHICS,
    HEAD,
    HORIZONTAL_RULE( true ),
    ITALIC,
    LINE_BREAK( true ),
    LINK,
    LIST,
    LIST_ITEM,
    MONO_SPACED,
    NON_BREAKING_SPACE( true ),
    NUMBERED_LIST,
    NUMBERED_LIST_ITEM,
    PAGE_BREAK( true ),
    PARAGRAPH,
    RAW_TEXT( true ),
    SECTION,
    SECTION_1,
    SECTION_2,
    SECTION_3,
    SECTION_4,
    SECTION_5,
    SECTION_TITLE,
    SECTION_TITLE_1,
    SECTION_TITLE_2,
    SECTION_TITLE_3,
    SECTION_TITLE_4,
    SECTION_TITLE_5,
    TABLE,
    TABLE_CAPTION,
    TABLE_CELL,
    TABLE_HEADER_CELL,
    TABLE_ROW,
    TABLE_ROWS,
    TEXT( true ),
    TITLE,
    UNKNOWN( true ),
    VERBATIM;

    /** whether or not the DoxiaEvent needs to be explicitly closed */
    private final boolean selfClosing;

    /**
     * Constructs a DoxiaEvent that should be explicitly closed.  Equivalent to {@code DoxiaEvent( false )}.
     */
    private DoxiaEvent()
    {
        this.selfClosing = false;
    }

    /**
     * Constructs a DoxiaEvent, defining whether or not the event needs to be explicitly closed.  <em>E.g.</em>
     * {@code anchor_()}
     *
     * @param selfClosing false if the event should be explicitly closed.
     */
    private DoxiaEvent( boolean selfClosing )
    {
        this.selfClosing = selfClosing;
    }

    /**
     * Whether or not this DoxiaEvent needs to be explicitly closed by calling its "close" method: by convention,
     * methods for close events end with an underscore.  <em>E.g.</em> {@code anchor_()} closes an anchor tag.
     *
     * @return false if the event should be explicitly closed, true if it closes itself.
     */
    public boolean isSelfClosing()
    {
        return selfClosing;
    }

    @Override
    public String toString()
    {
        return this.name() + ", self closing: " + selfClosing;
    }
}

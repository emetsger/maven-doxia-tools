package org.apache.maven.doxia.book.services.io;


import org.apache.maven.doxia.logging.Log;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkEventAttributes;

/**
 * An implementation of Sink in which <em>all</em> methods are no-ops.
 *
 * @see org.apache.maven.doxia.sink.SinkAdapter
 */
public abstract class BaseIoSink implements Sink
{

    public void anchor( String name )
    {

    }

    public void head()
    {

    }

    public void head( SinkEventAttributes attributes )
    {

    }

    public void head_()
    {

    }

    public void title()
    {

    }

    public void title( SinkEventAttributes attributes )
    {

    }

    public void title_()
    {

    }

    public void author()
    {

    }

    public void author( SinkEventAttributes attributes )
    {

    }

    public void author_()
    {

    }

    public void date()
    {

    }

    public void date( SinkEventAttributes attributes )
    {

    }

    public void date_()
    {

    }

    public void body()
    {

    }

    public void body( SinkEventAttributes attributes )
    {

    }

    public void body_()
    {

    }

    public void sectionTitle()
    {

    }

    public void sectionTitle_()
    {

    }

    public void section1()
    {

    }

    public void section1_()
    {

    }

    public void sectionTitle1()
    {

    }

    public void sectionTitle1_()
    {

    }

    public void section2()
    {

    }

    public void section2_()
    {

    }

    public void sectionTitle2()
    {

    }

    public void sectionTitle2_()
    {

    }

    public void section3()
    {

    }

    public void section3_()
    {

    }

    public void sectionTitle3()
    {

    }

    public void sectionTitle3_()
    {

    }

    public void section4()
    {

    }

    public void section4_()
    {

    }

    public void sectionTitle4()
    {

    }

    public void sectionTitle4_()
    {

    }

    public void section5()
    {

    }

    public void section5_()
    {

    }

    public void sectionTitle5()
    {

    }

    public void sectionTitle5_()
    {

    }

    public void section( int level, SinkEventAttributes attributes )
    {

    }

    public void section_( int level )
    {

    }

    public void sectionTitle( int level, SinkEventAttributes attributes )
    {

    }

    public void sectionTitle_( int level )
    {

    }

    public void list()
    {

    }

    public void list( SinkEventAttributes attributes )
    {

    }

    public void list_()
    {

    }

    public void listItem()
    {

    }

    public void listItem( SinkEventAttributes attributes )
    {

    }

    public void listItem_()
    {

    }

    public void numberedList( int numbering )
    {

    }

    public void numberedList( int numbering, SinkEventAttributes attributes )
    {

    }

    public void numberedList_()
    {

    }

    public void numberedListItem()
    {

    }

    public void numberedListItem( SinkEventAttributes attributes )
    {

    }

    public void numberedListItem_()
    {

    }

    public void definitionList()
    {

    }

    public void definitionList( SinkEventAttributes attributes )
    {

    }

    public void definitionList_()
    {

    }

    public void definitionListItem()
    {

    }

    public void definitionListItem( SinkEventAttributes attributes )
    {

    }

    public void definitionListItem_()
    {

    }

    public void definition()
    {

    }

    public void definition( SinkEventAttributes attributes )
    {

    }

    public void definition_()
    {

    }

    public void definedTerm()
    {

    }

    public void definedTerm( SinkEventAttributes attributes )
    {

    }

    public void definedTerm_()
    {

    }

    public void figure()
    {

    }

    public void figure( SinkEventAttributes attributes )
    {

    }

    public void figure_()
    {

    }

    public void figureCaption()
    {

    }

    public void figureCaption( SinkEventAttributes attributes )
    {

    }

    public void figureCaption_()
    {

    }

    public void figureGraphics( String name )
    {

    }

    public void figureGraphics( String src, SinkEventAttributes attributes )
    {

    }

    public void table()
    {

    }

    public void table( SinkEventAttributes attributes )
    {

    }

    public void table_()
    {

    }

    public void tableRows( int[] justification, boolean grid )
    {

    }

    public void tableRows_()
    {

    }

    public void tableRow()
    {

    }

    public void tableRow( SinkEventAttributes attributes )
    {

    }

    public void tableRow_()
    {

    }

    public void tableCell()
    {

    }

    public void tableCell( String width )
    {

    }

    public void tableCell( SinkEventAttributes attributes )
    {

    }

    public void tableCell_()
    {

    }

    public void tableHeaderCell()
    {

    }

    public void tableHeaderCell( String width )
    {

    }

    public void tableHeaderCell( SinkEventAttributes attributes )
    {

    }

    public void tableHeaderCell_()
    {

    }

    public void tableCaption()
    {

    }

    public void tableCaption( SinkEventAttributes attributes )
    {

    }

    public void tableCaption_()
    {

    }

    public void paragraph()
    {

    }

    public void paragraph( SinkEventAttributes attributes )
    {

    }

    public void paragraph_()
    {

    }

    public void verbatim( boolean boxed )
    {

    }

    public void verbatim( SinkEventAttributes attributes )
    {

    }

    public void verbatim_()
    {

    }

    public void horizontalRule()
    {

    }

    public void horizontalRule( SinkEventAttributes attributes )
    {

    }

    public void pageBreak()
    {

    }

    public void anchor( String name, SinkEventAttributes attributes )
    {

    }

    public void anchor_()
    {

    }

    public void link( String name )
    {

    }

    public void link( String name, SinkEventAttributes attributes )
    {

    }

    public void link_()
    {

    }

    public void italic()
    {

    }

    public void italic_()
    {

    }

    public void bold()
    {

    }

    public void bold_()
    {

    }

    public void monospaced()
    {

    }

    public void monospaced_()
    {

    }

    public void lineBreak()
    {

    }

    public void lineBreak( SinkEventAttributes attributes )
    {

    }

    public void nonBreakingSpace()
    {

    }

    public void text( String text )
    {

    }

    public void text( String text, SinkEventAttributes attributes )
    {

    }

    public void rawText( String text )
    {

    }

    public void comment( String comment )
    {

    }

    public void unknown( String name, Object[] requiredParams, SinkEventAttributes attributes )
    {

    }

    public void flush()
    {

    }

    public void close()
    {

    }

    public void enableLogging( Log log )
    {

    }
}

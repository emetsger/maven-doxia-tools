package org.apache.maven.doxia.book.services.io;

import junit.framework.TestCase;
import org.apache.maven.doxia.book.context.BookContext;
import org.apache.maven.doxia.module.apt.AptParser;
import org.apache.maven.doxia.module.apt.AptSiteModule;
import org.apache.maven.doxia.module.site.SiteModule;
import org.apache.maven.doxia.module.site.manager.SiteModuleManager;
import org.apache.maven.doxia.module.xdoc.XdocParser;
import org.apache.maven.doxia.module.xdoc.XdocSiteModule;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.parser.manager.ParserManager;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;

import java.io.File;
import java.util.*;

import static org.mockito.Mockito.*;

/**
 * Unit tests against {@link DefaultBookIo}.  Note this class doesn't extend the Plexus test case, and instead mocks
 * the collaborators of {@code DefaultBookIo}.
 */
public class DefaultBookIoTest extends TestCase
{
    private SiteModuleManager siteModuleMgr = mock( SiteModuleManager.class );

    private ParserManager parserMgr = mock( ParserManager.class );

    private DefaultBookIo underTest;

    public void setUp()
    {
        underTest = new DefaultBookIo( parserMgr, siteModuleMgr );
        underTest.enableLogging( new ConsoleLogger( Logger.LEVEL_DISABLED, underTest.getClass().getName() ) );
    }

    /**
     * Insures that {@link BookContext.BookFile}s don't get populated with section identifiers when loading
     * APT files.
     *
     * @throws Exception
     */
    public void testLoadFilesApt() throws Exception
    {
        File bookDir = new File( this.getClass().getResource( "/book-1" ).getPath() );
        SiteModule aptModule = new AptSiteModule();
        Parser aptParser = new AptParser();
        BookContext book = new BookContext();

        loadFilesForBook( book, bookDir, Arrays.asList( new SiteModuleAndParser( aptParser, aptModule ) ) );

        // There won't be any sections in the BookFiles: the Apt Parser doesn't emit Section events that have
        // section identifiers
        for ( Map.Entry<String, BookContext.BookFile> entry : book.getFiles().entrySet() )
        {
            assertEquals( 0, entry.getValue().getSectionIds().size() );
        }
    }

    /**
     * Insures that {@link BookContext.BookFile}s don't get populated with section identifiers when loading
     * XDoc files that don't have {@code id} attributes on {@code &lt;section>} elements
     *
     * @throws Exception
     */
    public void testLoadFilesXdoc() throws Exception
    {
        File bookDir = new File( this.getClass().getResource( "/book-2" ).getPath() );
        SiteModule xdocModule = new XdocSiteModule();
        Parser xdocParser = new XdocParser();
        BookContext book = new BookContext();

        loadFilesForBook( book, bookDir, Arrays.asList( new SiteModuleAndParser( xdocParser, xdocModule ) ) );

        // There won't be any sections in the BookFiles: the xdoc <section>s don't have ids
        for ( Map.Entry<String, BookContext.BookFile> entry : book.getFiles().entrySet() )
        {
            assertEquals( 0, entry.getValue().getSectionIds().size() );
        }
    }

    /**
     * Insures that {@link BookContext.BookFile}s will get populated with section identifiers when loading
     * XDoc files that have {@code id} attributes on {@code &lt;section>} and {@code &lt;subsection>} elements.
     *
     * @throws Exception
     */
    public void testLoadFilesXdocWithSections() throws Exception
    {
        File bookDir = new File( this.getClass().getResource( "/book-3" ).getPath() );
        SiteModule xdocModule = new XdocSiteModule();
        Parser xdocParser = new XdocParser();
        BookContext book = new BookContext();

        loadFilesForBook( book, bookDir, Arrays.asList( new SiteModuleAndParser( xdocParser, xdocModule ) ) );

        int expectedSections = 9;
        int actualSections = 0;

        for ( Map.Entry<String, BookContext.BookFile> entry : book.getFiles().entrySet() )
        {
            actualSections += entry.getValue().getSectionIds().size();
        }

        assertEquals( expectedSections, actualSections );
    }

    /**
     * Insures that when loading files using multiple site modules, and when the file names (minus their path and
     * extension) are identical, that the expected number of {@link BookContext#getFiles() files} are in the
     * {@code BookContext}, and that the section identifiers are still properly parsed and
     * {@link BookContext.BookFile#getSectionIds() exposed}.
     *
     * @throws Exception
     */
    public void testLoadFilesMultipleSourceFormats() throws Exception
    {
        File bookDir = new File( this.getClass().getResource( "/book-4" ).getPath() );
        SiteModule xdocModule = new XdocSiteModule();
        Parser xdocParser = new XdocParser();
        BookContext book = new BookContext();

        loadFilesForBook( book, bookDir, Arrays.asList( new SiteModuleAndParser( xdocParser, xdocModule ),
                new SiteModuleAndParser( new AptParser(), new AptSiteModule() ) ) );

        int expectedSections = 9;
        int actualSections = 0;

        for ( Map.Entry<String, BookContext.BookFile> entry : book.getFiles().entrySet() )
        {
            actualSections += entry.getValue().getSectionIds().size();
        }

        assertEquals( expectedSections, actualSections );
    }

    /**
     * Loads files from {@code bookDir} into the {@code context} for the supplied {@code SiteModule}/{@code Parser}
     * pair.
     *
     * @param context the {@code BookContext} to load files into
     * @param bookDir the base directory where the book files are located
     * @param smPairs {@code SiteModule}/{@code Parser} pairs used to load and parse the book files
     * @throws Exception
     */
    private void loadFilesForBook( BookContext context, File bookDir, List<SiteModuleAndParser> smPairs )
            throws Exception
    {
        // Configure mocks
        List<SiteModule> siteModules = new ArrayList<SiteModule>();
        for ( SiteModuleAndParser smPair : smPairs )
        {
            siteModules.add( smPair.siteModule );
        }
        when( siteModuleMgr.getSiteModules() ).thenReturn( siteModules );

        for ( SiteModuleAndParser smPair : smPairs )
        {
            when( parserMgr.getParser( smPair.siteModule.getParserId() ) ).thenReturn( smPair.parser );
        }

        // Obtain the files to load from each site module
        Collection<File> files = new ArrayList<File>();
        for ( SiteModuleAndParser smPair : smPairs )
        {
            files.addAll( org.apache.commons.io.FileUtils.listFiles( bookDir,
                    new String[]{ smPair.siteModule.getExtension() }, true ) );
        }

        assertTrue( "Did not find any files to parse in " + bookDir, files.size() > 0 );
        List<File> fileList = new ArrayList<File>();
        fileList.addAll( files );

        assertTrue( context.getFiles().isEmpty() );

        // Load the files
        underTest.loadFiles( context, fileList );

        Set<String> fileNames = new HashSet<String>();
        for ( File f : fileList )
        {
            fileNames.add( f.getName().substring( 0, f.getName().lastIndexOf( "." ) ) );
        }

        assertEquals( "Expected " + fileNames.size() + " book files.", fileNames.size(), context.getFiles().size() );

        // Verify the contents of the Map

        // Each file name should be a key
        for ( File f : fileList )
        {
            String name = f.getName().substring( 0, f.getName().lastIndexOf( "." ) );
            assertTrue( "BookContext is missing file " + f.getName() + " (key: " + name + ")",
                    context.getFiles().containsKey( name ) );
        }

        // Verify mock invocation
        verify( siteModuleMgr ).getSiteModules();
        for ( SiteModuleAndParser smPair : smPairs )
        {
            verify( parserMgr, atLeastOnce() ).getParser( smPair.siteModule.getParserId() );
        }
    }

    /**
     * Represents a {@code SiteModule} and its {@code Parser}; e.g. an {@link AptSiteModule} and an {@link AptParser}.
     */
    private class SiteModuleAndParser
    {
        private SiteModule siteModule;
        private Parser parser;

        private SiteModuleAndParser( Parser parser, SiteModule siteModule )
        {
            this.parser = parser;
            this.siteModule = siteModule;
        }
    }


    
}

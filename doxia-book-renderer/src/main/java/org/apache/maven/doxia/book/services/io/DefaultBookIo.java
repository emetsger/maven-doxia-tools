package org.apache.maven.doxia.book.services.io;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.doxia.book.model.io.xpp3.BookModelXpp3Reader;
import org.apache.maven.doxia.book.model.BookModel;
import org.apache.maven.doxia.book.BookDoxiaException;
import org.apache.maven.doxia.book.context.BookContext;
import org.apache.maven.doxia.module.site.SiteModule;
import org.apache.maven.doxia.module.site.manager.SiteModuleManager;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.parser.manager.ParserManager;
import org.apache.maven.doxia.parser.manager.ParserNotFoundException;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;

/**
 * <p>DefaultBookIo class.</p>
 *
 * @author <a href="mailto:trygve.laugstol@objectware.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
@Component( role = BookIo.class )
public class DefaultBookIo
    extends AbstractLogEnabled
    implements BookIo
{
    @Requirement
    private SiteModuleManager siteModuleManager;

    @Requirement
    private ParserManager parserManager;

    // -----------------------------------------------------------------------
    // DefaultBookIo Implementation
    // -----------------------------------------------------------------------

    /** {@inheritDoc} */
    public BookModel readBook( File bookDescriptor )
        throws BookDoxiaException
    {
        Reader reader = null;
        try
        {
            reader = ReaderFactory.newXmlReader( bookDescriptor );
            return new BookModelXpp3Reader().read( reader, true );
        }
        catch ( IOException e )
        {
            throw new BookDoxiaException( "Error while reading book descriptor.", e );
        }
        catch ( XmlPullParserException e )
        {
            throw new BookDoxiaException( "Error while reading book descriptor.", e );
        }
        finally
        {
            IOUtil.close( reader );
        }
    }

    /** {@inheritDoc} */
    public void loadFiles( BookContext context, List<File> files )
    {
        // ----------------------------------------------------------------------
        // Find all the files, identify the sections in the files, and map the file names to ids (which are
        // implicit section ids)
        // ----------------------------------------------------------------------

        Collection<SiteModule> siteModules = siteModuleManager.getSiteModules();

        for ( SiteModule siteModule : siteModules )
        {
            String extension = siteModule.getExtension();

            String sourceDirectory = File.separator + siteModule.getSourceDirectory() + File.separator;

            String parserId = siteModule.getParserId();

            for ( File file : filesForModule( siteModule, files ) )
            {
                String name = file.getName();

                String path = file.getAbsolutePath();

                // The sections identified in the file
                final List<String> sectionIds = new ArrayList<String>();

                try
                {
                    // Parse the file, collecting the section events
                    Parser parser = parserManager.getParser( parserId );
                    SectionIdentifiersSink sectionSink = new SectionIdentifiersSink();
                    parser.parse( new FileReader( file ), sectionSink );
                    sectionIds.addAll( sectionSink.getSectionIds() );
                }
                catch ( Exception e )
                {
                    String msg = "Error parsing file " + file + " with parser " + parserId;
                    getLogger().error( msg, e );
                    throw new RuntimeException( msg, e );
                }

                // first check if the file path contains one of the recognized source dir identifiers
                // (there's trouble if a pathname contains 2 identifiers), then match file extensions (not unique).

                final BookContext.BookFile bookFile = new BookContext.BookFile( file, parserId );
                bookFile.setSectionIds( sectionIds );
                if ( path.indexOf( sourceDirectory ) != -1 )
                {
                    name = name.substring( 0, name.length() - extension.length() - 1 );

                    context.getFiles().put( name, bookFile);
                }
                else if ( name.endsWith( extension ) )
                {
                    name = name.substring( 0, name.length() - extension.length() - 1 );

                    // don't overwrite if it's there already
                    if ( !context.getFiles().containsKey( name ) )
                    {
                        context.getFiles().put( name, bookFile);
                    }
                }
            }
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Dumping document <-> id mapping:" );

            Map<String, BookContext.BookFile> map = new TreeMap<String, BookContext.BookFile>( context.getFiles() );

            for ( Map.Entry<String, BookContext.BookFile> entry : map.entrySet() )
            {
                BookContext.BookFile file = entry.getValue();
                for (String sectionId : file.getSectionIds() )
                {
                    getLogger().debug( " " + sectionId + "=" + file.getFile() + ", parser: " + file.getParserId() );
                }
            }
        }
    }

    /**
     * Selects files from a list of candidates that can be parsed by the supplied {@code siteModule}.  Uses the
     * site module's source directory and file extension to select files.
     *
     * @param siteModule the {@code SiteModule}
     * @param candidateFiles a List of files, some of which may be able to be parsed by {@code siteModule}
     * @return the files that can be parsed by {@code siteModule}.  May be empty, but never {@code null}.
     */
    private List<File> filesForModule( SiteModule siteModule, List<File> candidateFiles )
    {
        String sourceDir = siteModule.getSourceDirectory();
        String ext = siteModule.getExtension();

        List<File> moduleFiles = new ArrayList<File>();

        for ( File candidate : candidateFiles )
        {
            String dir = candidate.getParent();
            String name = candidate.getName();

            if ( !dir.contains( sourceDir ) )
            {
                continue;
            }

            if ( !name.endsWith( ext ) )
            {
                continue;
            }

            moduleFiles.add( candidate );
        }

        return moduleFiles;
    }
}

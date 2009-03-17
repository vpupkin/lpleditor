// 
// 
// 
// Source File Name:   APTEditorDriver.java

package yep_apt_editor.editors;
 
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.maven.doxia.logging.Log;
import org.apache.maven.doxia.module.apt.AptParser;
import org.apache.maven.doxia.module.apt.AptSink;
import org.apache.maven.doxia.module.apt.AptSinkFactory;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkFactory;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.component.repository.io.PlexusTools;
import org.codehaus.plexus.util.ReaderFactory;

public class APTEditorDriver  
{

	Logger log = Logger.getLogger(APTEditorDriver.class.getName());
	public APTEditorDriver()
    {
        pathSeparator = System.getProperty("file.separator");
        String tempDir = System.getProperty("java.io.tmpdir");
        if(tempDir.lastIndexOf(pathSeparator) != tempDir.length() - 1)
            tempDir = (new StringBuilder(String.valueOf(tempDir))).append(pathSeparator).toString();
        tempDirectory = (new StringBuilder(String.valueOf(tempDir))).append("apteditor").append(pathSeparator).toString();
        File file = new File(tempDirectory);
        if(!file.exists())
            file.mkdirs();
    }

    public String convert(String aptFilePath)
        throws Exception
    {  String htmlFileName = aptFilePath;
 		String htmlFilePath  = aptFilePath;
	 
        htmlFileName = (new StringBuilder(String.valueOf(aptFilePath.substring(aptFilePath.lastIndexOf(pathSeparator) + 1, aptFilePath.lastIndexOf("."))))).append(".html").toString();
        htmlFilePath = (new StringBuilder(String.valueOf(tempDirectory))).append(htmlFileName).toString();
        convert(new String[] {
            aptFilePath
        }, htmlFilePath);
    	
         return htmlFilePath;
 
    }

    private void convert(String[] strings, String htmlFilePath) throws PlexusContainerException, ComponentLookupException, IOException, ParseException {
    	  File userDir = new File( System.getProperty ( "user.dir" ) );
    	  File inputFile = new File( strings[0] );
    	  File outputFile = new File( htmlFilePath );
          Map context = new HashMap();
          context.put( "basedir", new File( ""+"" ).getAbsolutePath() );

          ContainerConfiguration containerConfiguration = new DefaultContainerConfiguration();
          containerConfiguration.setName( "Doxia" );
          containerConfiguration.setContext( context );

          PlexusContainer container = new DefaultPlexusContainer( containerConfiguration );    	  
//    	  PlexusContainer container= new DefaultPlexusContainer();
    	  SinkFactory sinkFactory = (SinkFactory) container.lookup( SinkFactory.ROLE, "xhtml" ); // Plexus lookup
    	  Sink sink = sinkFactory.createSink( outputFile.getParentFile(), outputFile.getName()   );
//    	  SinkFactory aptFactory = new AptSinkFactory();
//    	  
//		  OutputStream outTmp = new FileOutputStream (htmlFilePath);
//		Sink sink =  aptFactory.createSink( outTmp  );
		 /// role="org.apache.maven.doxia.parser.Parser" role-hint="apt"
    	  Parser parser = (Parser)container.lookup( Parser.ROLE, "apt" );; // Plexus lookup

    	  Reader reader = ReaderFactory.newReader( inputFile, "UTF-8" );

    	  parser. parse( reader, sink );
    	  sink.flush(); 
          //outTmp.flush();
          sink.close();
          //outTmp.close();
	}

	private String tempDirectory;
    private String pathSeparator;
	public void setTOC(boolean boolean1) {
		log.fine("setTOC("+boolean1+") called.");
	}

	public void setSectionsNumbered(boolean boolean1) {
		log.fine("setSectionsNumbered("+boolean1+") called.");	}

	public void putPI(String string, String string2, String cssPath) {
		log.fine("setSectionsNumbered("+string+","+string2+","+cssPath+") called.");	}

	public void setEncoding(String charset) {
		log.fine("setEncoding("+charset+") called.");	 
	}
}

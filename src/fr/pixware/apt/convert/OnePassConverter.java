// 
// 
// 
// Source File Name:   OnePassConverter.java

package fr.pixware.apt.convert;

import fr.pixware.apt.parse.*;
import fr.pixware.util.*;
import java.io.*;
import java.util.Vector;

// Referenced classes of package fr.pixware.apt.convert:
//            Converter, Driver

public abstract class OnePassConverter
    implements Converter
{

    public OnePassConverter(Driver driver)
    {
        source = null;
        parser = null;
        outFileName = null;
        outWriter = null;
        inExtensions = null;
        this.driver = driver;
    }

    public Driver getDriver()
    {
        return driver;
    }

    public String getConverterInfo()
    {
        return null;
    }

    public void convert(String inFileNames[], String outFileName)
        throws Exception
    {
        source = new MultiFileSource(inFileNames, driver.getEncoding());
        this.outFileName = outFileName;
        outWriter = null;
        Sink sink = createSink(outFileName);
        parser = new Parser();
        try
        {
            parser.parse(source, sink);
        }
        catch(Exception e)
        {
            throw new ParseException(e, parser.getSourceName(), parser.getSourceLineNumber());
        }
        finally
        {
            source.close();
            source = null;
            parser = null;
            destroySink(sink);
            this.outFileName = null;
        }
    }

    protected Sink createSink(String outFileName)
        throws Exception
    {
        outWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFileName), driver.getEncoding()));
        return createSink(outWriter);
    }

    protected abstract Sink createSink(Writer writer)
        throws Exception;

    protected void destroySink(Sink sink)
    {
        if(outWriter != null)
        {
            try
            {
                outWriter.close();
            }
            catch(IOException ioexception) { }
            outWriter = null;
        }
    }

    protected String getOutFileName()
    {
        return outFileName;
    }

    protected void convertGraphics(String name, String extension)
        throws Exception
    {
        convertGraphics(name, new String[] {
            extension
        });
    }

    protected String convertGraphics(String name, String extensions[])
        throws Exception
    {
        String sourceFileName = parser.getSourceName();
        String inDirName = FileUtil.fileDirName(sourceFileName);
        String outDirName = FileUtil.fileDirName(outFileName);
        String outRootName = outDirName + File.separatorChar + name + ".";
        File graphicsOutDir = (new File(outRootName)).getParentFile();
        if(graphicsOutDir != null && !graphicsOutDir.isDirectory())
            graphicsOutDir.mkdirs();
        for(int j = 0; j < extensions.length; j++)
        {
            String extension = extensions[j];
            File inFile = new File(name + "." + extension);
            if(!inFile.isAbsolute())
                inFile = new File(inDirName, name + "." + extension);
            if(inFile.isFile())
            {
                File outFile = new File(outRootName + extension);
                if(!outFile.isFile() || !inFile.getCanonicalPath().equals(outFile.getCanonicalPath()))
                    FileUtil.copyFile(inFile.getPath(), outFile.getPath());
                return extension;
            }
        }

        String inExts[] = getInExtensions();
        String inExt = null;
        for(int i = 0; i < inExts.length; i++)
        {
            String ext = inExts[i];
            File inFile = new File(name + "." + ext);
            if(!inFile.isAbsolute())
                inFile = new File(inDirName, name + "." + ext);
            if(inFile.isFile())
            {
                inExt = ext;
                for(int j = 0; j < extensions.length; j++)
                {
                    String extension = extensions[j];
                    String rule = driver.getRule(inExt, extension);
                    if(rule != null)
                    {
                        executeRule(rule, inFile.getPath(), outRootName + extension);
                        return extension;
                    }
                }

            }
        }

        String graphicsDescription = "graphics named '" + name + "'";
        if(inExt == null)
            throw new RuntimeException("cannot find " + graphicsDescription);
        else
            throw new RuntimeException("don't know how to convert " + graphicsDescription + " to a suitable format (ex. " + extensions[0] + ")");
    }

    private String[] getInExtensions()
    {
        if(inExtensions == null)
        {
            String allRules[] = driver.getAllRules();
            Vector list = new Vector();
            for(int i = 0; i < allRules.length; i += 3)
            {
                String extension = allRules[i];
                if(list.indexOf(extension) < 0)
                    list.addElement(extension);
            }

            inExtensions = new String[list.size()];
            list.copyInto(inExtensions);
        }
        return inExtensions;
    }

    private void executeRule(String rule, String srcFileName, String dstFileName)
        throws IOException, InterruptedException
    {
        String cmd = StringUtil.replaceAll(rule, "%F", srcFileName);
        cmd = StringUtil.replaceAll(cmd, "%G", dstFileName);
        PlatformUtil.shellExec(cmd, driver.isVerbose());
    }

    protected String getSourceFileName()
    {
        return parser != null ? parser.getSourceName() : null;
    }

    protected int getSourceLineNumber()
    {
        return parser != null ? parser.getSourceLineNumber() : -1;
    }

    private Driver driver;
    private MultiFileSource source;
    private Parser parser;
    private String outFileName;
    private Writer outWriter;
    private String inExtensions[];
}

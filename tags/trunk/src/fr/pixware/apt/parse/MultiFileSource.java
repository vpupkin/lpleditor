// 
// 
// 
// Source File Name:   MultiFileSource.java

package fr.pixware.apt.parse;

import java.io.*;

// Referenced classes of package fr.pixware.apt.parse:
//            ParseException, Source

public class MultiFileSource
    implements Source
{

    public MultiFileSource(String fileName)
    {
        this(new String[] {
            fileName
        });
    }

    public MultiFileSource(String fileNames[])
    {
        this(fileNames, null);
    }

    public MultiFileSource(String fileNames[], String encoding)
    {
        this.fileNames = fileNames;
        this.encoding = encoding;
        next = 0;
        reader = null;
        lineNumber = -1;
    }

    public int getLineNumber()
    {
        return lineNumber;
    }

    public String getName()
    {
        return fileNames[next <= 0 ? 0 : next - 1];
    }

    public String getNextLine()
        throws ParseException
    {
        if(reader == null)
            if(next < fileNames.length)
            {
                try
                {
                    makeReader();
                }
                catch(Exception e)
                {
                    throw new ParseException(e);
                }
                next++;
            } else
            {
                return null;
            }
        String line;
        try
        {
            line = reader.readLine();
            if(line == null)
            {
                reader.close();
                reader = null;
                line = getNextLine();
            } else
            {
                lineNumber = reader.getLineNumber();
            }
        }
        catch(IOException e)
        {
            throw new ParseException(e);
        }
        return line;
    }

    private void makeReader()
        throws FileNotFoundException, UnsupportedEncodingException
    {
        java.io.Reader fileReader;
        if(encoding == null)
            fileReader = new FileReader(fileNames[next]);
        else
            fileReader = new InputStreamReader(new FileInputStream(fileNames[next]), encoding);
        reader = new LineNumberReader(fileReader);
        lineNumber = -1;
    }

    public void close()
    {
        if(reader != null)
            try
            {
                reader.close();
            }
            catch(IOException ioexception) { }
        next = fileNames.length;
        reader = null;
        lineNumber = -1;
    }

    private String fileNames[];
    private String encoding;
    private int next;
    private LineNumberReader reader;
    private int lineNumber;
}

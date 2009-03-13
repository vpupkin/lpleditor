// 
// 
// 
// Source File Name:   ReaderSource.java

package fr.pixware.apt.parse;

import java.io.*;

// Referenced classes of package fr.pixware.apt.parse:
//            ParseException, Source

public class ReaderSource
    implements Source
{

    public ReaderSource(Reader in)
    {
        reader = new LineNumberReader(in);
        lineNumber = -1;
    }

    public String getNextLine()
        throws ParseException
    {
        if(reader == null)
            return null;
        String line;
        try
        {
            line = reader.readLine();
            if(line == null)
            {
                reader.close();
                reader = null;
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

    public String getName()
    {
        return "";
    }

    public int getLineNumber()
    {
        return lineNumber;
    }

    public void close()
    {
        if(reader != null)
            try
            {
                reader.close();
            }
            catch(IOException ioexception) { }
        reader = null;
    }

    private LineNumberReader reader;
    private int lineNumber;
}

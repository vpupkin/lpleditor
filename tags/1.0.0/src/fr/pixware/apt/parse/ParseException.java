// 
// 
// 
// Source File Name:   ParseException.java

package fr.pixware.apt.parse;

import fr.pixware.util.WrappedException;

public class ParseException extends WrappedException
{

    public ParseException()
    {
        this(null, null, null, -1);
    }

    public ParseException(String message)
    {
        this(null, message, null, -1);
    }

    public ParseException(Exception e)
    {
        this(e, null, null, -1);
    }

    public ParseException(Exception e, String message)
    {
        this(e, message, null, -1);
    }

    public ParseException(Exception e, String fileName, int lineNumber)
    {
        this(e, null, fileName, lineNumber);
    }

    public ParseException(Exception e, String message, String fileName, int lineNumber)
    {
        super(e, message);
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }

    public String getFileName()
    {
        return fileName;
    }

    public int getLineNumber()
    {
        return lineNumber;
    }

    private String fileName;
    private int lineNumber;
}

// 
// 
// 
// Source File Name:   WrappedException.java

package fr.pixware.util;


public abstract class WrappedException extends Exception
{

    public WrappedException()
    {
        this(null, null);
    }

    public WrappedException(String message)
    {
        this(null, message);
    }

    public WrappedException(Exception e)
    {
        this(e, null);
    }

    public WrappedException(Exception e, String message)
    {
        super(message != null ? message : makeMessage(e));
        rootException = e;
    }

    public Exception getRootException()
    {
        return rootException;
    }

    private static String makeMessage(Exception e)
    {
        Exception rootEx = findRootException(e);
        return rootEx != null ? rootEx.getMessage() : null;
    }

    private static Exception findRootException(Exception e)
    {
        Exception rootEx = null;
        for(; e != null; e = ((WrappedException)e).getRootException())
        {
            rootEx = e;
            if(!(e instanceof WrappedException))
                break;
        }

        return rootEx;
    }

    private Exception rootException;
}

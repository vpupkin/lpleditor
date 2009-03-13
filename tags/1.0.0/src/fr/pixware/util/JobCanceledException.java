// 
// 
// 
// Source File Name:   JobCanceledException.java

package fr.pixware.util;


public class JobCanceledException extends RuntimeException
{

    public JobCanceledException(Object source)
    {
        this.source = source;
    }

    public Object getSource()
    {
        return source;
    }

    private Object source;
}

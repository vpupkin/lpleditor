// 
// 
// 
// Source File Name:   Source.java

package fr.pixware.apt.parse;


// Referenced classes of package fr.pixware.apt.parse:
//            ParseException

public interface Source
{

    public abstract String getNextLine()
        throws ParseException;

    public abstract String getName();

    public abstract int getLineNumber();
}

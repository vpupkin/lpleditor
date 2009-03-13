// 
// 
// 
// Source File Name:   REFactory.java

package fr.pixware.util;


// Referenced classes of package fr.pixware.util:
//            RESyntaxException, RE

public interface REFactory
{

    public abstract RE createRE(String s, int i)
        throws RESyntaxException;

    public static final int CASE_INSENSITIVE = 1;
    public static final int MULTI_LINE = 2;
    public static final int SINGLE_LINE = 4;
}

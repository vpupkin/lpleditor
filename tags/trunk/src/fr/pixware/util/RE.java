// 
// 
// 
// Source File Name:   RE.java

package fr.pixware.util;


// Referenced classes of package fr.pixware.util:
//            REMatch, CharSequence

public interface RE
{

    public abstract boolean search(String s);

    public abstract REMatch[] getMatch(String s, int i);

    public abstract REMatch[] getMatch(CharSequence charsequence, int i);
}

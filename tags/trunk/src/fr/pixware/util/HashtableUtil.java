// 
// 
// 
// Source File Name:   HashtableUtil.java

package fr.pixware.util;

import java.util.Enumeration;
import java.util.Hashtable;

public class HashtableUtil
{

    public HashtableUtil()
    {
    }

    public static void putAll(Hashtable map, String pairs[])
    {
        for(int i = 0; i < pairs.length; i += 2)
            map.put(pairs[i], pairs[i + 1]);

    }

    public static String[] getAllElements(Hashtable map)
    {
        String list[] = new String[map.size()];
        Enumeration iter = map.elements();
        int i = 0;
        while(iter.hasMoreElements()) 
            list[i++] = (String)iter.nextElement();
        return list;
    }
}

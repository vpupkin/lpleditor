// 
// 
// 
// Source File Name:   REMatch.java

package fr.pixware.util;


public final class REMatch
{

    public REMatch(int startIndex, int endIndex, String text)
    {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.text = text;
    }

    public int getStartIndex()
    {
        return startIndex;
    }

    public int getEndIndex()
    {
        return endIndex;
    }

    public String getText()
    {
        return text;
    }

    private int startIndex;
    private int endIndex;
    private String text;
}

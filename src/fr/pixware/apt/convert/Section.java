// 
// 
// 
// Source File Name:   Section.java

package fr.pixware.apt.convert;


public class Section
{

    public Section(int number[], Section parent)
    {
        this(number, "", parent, null);
    }

    public Section(int number[], String title, Section parent, Section children[])
    {
        this.number = number;
        this.title = title;
        this.parent = parent;
        this.children = children;
    }

    public static String formatNumber(int number[], char separ)
    {
        StringBuffer num = new StringBuffer();
        for(int i = 0; i < 5; i++)
        {
            int j = number[i];
            if(j <= 0)
                break;
            if(i > 0)
                num.append(separ);
            num.append(j);
        }

        return num.toString();
    }

    public void setNumber(int number[])
    {
        this.number = number;
    }

    public int[] getNumber()
    {
        return number;
    }

    public String getNumberFormatted()
    {
        return getNumberFormatted('.');
    }

    public String getNumberFormatted(char separ)
    {
        return formatNumber(number, separ);
    }

    public int getLevel()
    {
        int i;
        for(i = 0; i < 5; i++)
            if(number[i] <= 0)
                break;

        return i;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getTitle()
    {
        return title;
    }

    public void setParent(Section parent)
    {
        this.parent = parent;
    }

    public Section getParent()
    {
        return parent;
    }

    public void setChildren(Section children[])
    {
        this.children = children;
    }

    public Section[] getChildren()
    {
        return children;
    }

    private int number[];
    private String title;
    private Section parent;
    private Section children[];
}

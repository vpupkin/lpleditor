// 
// 
// 
// Source File Name:   Anchor.java

package fr.pixware.apt.convert;


// Referenced classes of package fr.pixware.apt.convert:
//            Section

public class Anchor
{

    public Anchor(String text, Section section)
    {
        this.text = text;
        this.section = section;
    }

    public String getText()
    {
        return text;
    }

    public Section getSection()
    {
        return section;
    }

    private String text;
    private Section section;
}

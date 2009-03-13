// 
// 
// 
// Source File Name:   Structure.java

package fr.pixware.apt.convert;


// Referenced classes of package fr.pixware.apt.convert:
//            Section, Anchor

public interface Structure
{

    public abstract String getTitle();

    public abstract String getAuthor();

    public abstract String getDate();

    public abstract boolean hasPreSections();

    public abstract Section[] getSections();

    public abstract Section getSection(int ai[]);

    public abstract Section getSection(String s);

    public abstract Anchor getAnchor(String s);

    public abstract Anchor[] getAnchors();
}

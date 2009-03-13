// 
// 
// 
// Source File Name:   StructureSink.java

package fr.pixware.apt.convert;

import fr.pixware.apt.parse.ParseException;
import fr.pixware.apt.parse.SinkAdapter;
import java.io.File;
import java.util.*;

// Referenced classes of package fr.pixware.apt.convert:
//            Section, Anchor, Structure

public class StructureSink extends SinkAdapter
    implements Structure
{

    public StructureSink()
    {
        title = null;
        author = null;
        date = null;
        preSections = false;
        sections = new Section[0];
        sectionMap = new Hashtable();
        anchors = new Anchor[0];
        anchorMap = new Hashtable();
        buffer = new StringBuffer();
    }

    public static boolean isExternalLink(String text)
    {
        text = text.toLowerCase();
        return text.indexOf("http:/") == 0 || text.indexOf("https:/") == 0 || text.indexOf("ftp:/") == 0 || text.indexOf("mailto:") == 0 || text.indexOf("file:/") == 0 || text.indexOf(".." + File.separatorChar) == 0 || text.indexOf("." + File.separatorChar) == 0;
    }

    public static String linkToKey(String text)
    {
        int length = text.length();
        StringBuffer buffer = new StringBuffer(length);
        for(int i = 0; i < length; i++)
        {
            char c = text.charAt(i);
            if(Character.isLetterOrDigit(c))
                buffer.append(Character.toLowerCase(c));
        }

        return buffer.toString();
    }

    public String getTitle()
    {
        return title;
    }

    public String getAuthor()
    {
        return author;
    }

    public String getDate()
    {
        return date;
    }

    public boolean hasPreSections()
    {
        return preSections;
    }

    public Section[] getSections()
    {
        return sections;
    }

    public Section getSection(int number[])
    {
        Section sections[] = getSections();
        Section section = null;
        for(int i = 0; i < 5; i++)
        {
            int n = number[i] - 1;
            if(n < 0)
                break;
            if(n >= sections.length)
            {
                section = null;
                break;
            }
            section = sections[n];
            sections = section.getChildren();
        }

        return section;
    }

    public Section getSection(String title)
    {
        return (Section)sectionMap.get(linkToKey(title));
    }

    public Anchor getAnchor(String text)
    {
        return (Anchor)anchorMap.get(linkToKey(text));
    }

    public Anchor[] getAnchors()
    {
        return anchors;
    }

    public void head()
        throws ParseException
    {
        title = null;
        author = null;
        date = null;
        preSections = false;
        sections = new Section[0];
        sectionMap.clear();
        anchors = new Anchor[0];
        anchorMap.clear();
        bufferFlag = false;
        preSectionsFlag = false;
    }

    public void title()
        throws ParseException
    {
        bufferFlag = true;
        buffer.setLength(0);
    }

    public void title_()
        throws ParseException
    {
        bufferFlag = false;
        title = buffer.toString();
    }

    public void author()
        throws ParseException
    {
        bufferFlag = true;
        buffer.setLength(0);
    }

    public void author_()
        throws ParseException
    {
        bufferFlag = false;
        author = buffer.toString();
    }

    public void date()
        throws ParseException
    {
        bufferFlag = true;
        buffer.setLength(0);
    }

    public void date_()
        throws ParseException
    {
        bufferFlag = false;
        date = buffer.toString();
    }

    public void body()
        throws ParseException
    {
        preSectionsFlag = true;
        for(int i = 0; i < 5; i++)
            sectionLists[i].removeAllElements();

    }

    public void body_()
        throws ParseException
    {
        addSubSections(1);
        anchors = new Anchor[anchorMap.size()];
        Enumeration iter = anchorMap.elements();
        int i = 0;
        while(iter.hasMoreElements()) 
            anchors[i++] = (Anchor)iter.nextElement();
    }

    public void section1()
        throws ParseException
    {
        preSectionsFlag = false;
        sectionLevel = 1;
    }

    public void section1_()
        throws ParseException
    {
        addSubSections(2);
    }

    public void section2()
        throws ParseException
    {
        sectionLevel = 2;
    }

    public void section2_()
        throws ParseException
    {
        addSubSections(3);
    }

    public void section3()
        throws ParseException
    {
        sectionLevel = 3;
    }

    public void section3_()
        throws ParseException
    {
        addSubSections(4);
    }

    public void section4()
        throws ParseException
    {
        sectionLevel = 4;
    }

    public void section4_()
        throws ParseException
    {
        addSubSections(5);
    }

    public void section5()
        throws ParseException
    {
        sectionLevel = 5;
    }

    public void sectionTitle()
        throws ParseException
    {
        bufferFlag = true;
        buffer.setLength(0);
        int number[] = new int[5];
        for(int i = 0; i < 5; i++)
        {
            number[i] = sectionLists[i].size();
            if(1 + i == sectionLevel)
                number[i]++;
        }

        Section parent = null;
        if(sectionLevel > 1)
            parent = (Section)sectionLists[sectionLevel - 2].lastElement();
        sectionLists[sectionLevel - 1].addElement(new Section(number, parent));
    }

    public void sectionTitle_()
        throws ParseException
    {
        bufferFlag = false;
        String title = buffer.toString();
        Section section = (Section)sectionLists[sectionLevel - 1].lastElement();
        section.setTitle(title);
        sectionMap.put(linkToKey(title), section);
    }

    public void anchor(String name)
        throws ParseException
    {
        Section section = null;
        for(int i = 5; i >= 1; i--)
        {
            Vector list = sectionLists[i - 1];
            if(list.size() <= 0)
                continue;
            section = (Section)list.lastElement();
            break;
        }

        anchorMap.put(linkToKey(name), new Anchor(name, section));
    }

    public void lineBreak()
        throws ParseException
    {
        if(bufferFlag)
            buffer.append('\n');
    }

    public void nonBreakingSpace()
        throws ParseException
    {
        if(bufferFlag)
            buffer.append(' ');
    }

    public void text(String text)
        throws ParseException
    {
        if(bufferFlag)
            buffer.append(text);
        if(preSectionsFlag)
            preSections = true;
    }

    private void addSubSections(int level)
    {
        Vector list = sectionLists[level - 1];
        Section subSections[] = new Section[list.size()];
        list.copyInto(subSections);
        list.removeAllElements();
        if(level == 1)
        {
            sections = subSections;
        } else
        {
            Section section = (Section)sectionLists[level - 2].lastElement();
            section.setChildren(subSections);
        }
    }

    private String title;
    private String author;
    private String date;
    private boolean preSections;
    private Section sections[];
    private Hashtable sectionMap;
    private Anchor anchors[];
    private Hashtable anchorMap;
    private StringBuffer buffer;
    private boolean bufferFlag;
    private boolean preSectionsFlag;
    private int sectionLevel;
    private Vector sectionLists[] = {
        new Vector(), new Vector(), new Vector(), new Vector(), new Vector()
    };
}

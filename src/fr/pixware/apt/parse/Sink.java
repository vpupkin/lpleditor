// 
// 
// 
// Source File Name:   Sink.java

package fr.pixware.apt.parse;


// Referenced classes of package fr.pixware.apt.parse:
//            ParseException

public interface Sink
{

    public abstract void head()
        throws ParseException;

    public abstract void head_()
        throws ParseException;

    public abstract void body()
        throws ParseException;

    public abstract void body_()
        throws ParseException;

    public abstract void section1()
        throws ParseException;

    public abstract void section1_()
        throws ParseException;

    public abstract void section2()
        throws ParseException;

    public abstract void section2_()
        throws ParseException;

    public abstract void section3()
        throws ParseException;

    public abstract void section3_()
        throws ParseException;

    public abstract void section4()
        throws ParseException;

    public abstract void section4_()
        throws ParseException;

    public abstract void section5()
        throws ParseException;

    public abstract void section5_()
        throws ParseException;

    public abstract void list()
        throws ParseException;

    public abstract void list_()
        throws ParseException;

    public abstract void listItem()
        throws ParseException;

    public abstract void listItem_()
        throws ParseException;

    public abstract void numberedList(int i)
        throws ParseException;

    public abstract void numberedList_()
        throws ParseException;

    public abstract void numberedListItem()
        throws ParseException;

    public abstract void numberedListItem_()
        throws ParseException;

    public abstract void definitionList()
        throws ParseException;

    public abstract void definitionList_()
        throws ParseException;

    public abstract void definitionListItem()
        throws ParseException;

    public abstract void definitionListItem_()
        throws ParseException;

    public abstract void definition()
        throws ParseException;

    public abstract void definition_()
        throws ParseException;

    public abstract void figure()
        throws ParseException;

    public abstract void figure_()
        throws ParseException;

    public abstract void table()
        throws ParseException;

    public abstract void table_()
        throws ParseException;

    public abstract void tableRows(int ai[], boolean flag)
        throws ParseException;

    public abstract void tableRows_()
        throws ParseException;

    public abstract void tableRow()
        throws ParseException;

    public abstract void tableRow_()
        throws ParseException;

    public abstract void title()
        throws ParseException;

    public abstract void title_()
        throws ParseException;

    public abstract void author()
        throws ParseException;

    public abstract void author_()
        throws ParseException;

    public abstract void date()
        throws ParseException;

    public abstract void date_()
        throws ParseException;

    public abstract void sectionTitle()
        throws ParseException;

    public abstract void sectionTitle_()
        throws ParseException;

    public abstract void paragraph()
        throws ParseException;

    public abstract void paragraph_()
        throws ParseException;

    public abstract void verbatim(boolean flag)
        throws ParseException;

    public abstract void verbatim_()
        throws ParseException;

    public abstract void definedTerm()
        throws ParseException;

    public abstract void definedTerm_()
        throws ParseException;

    public abstract void figureCaption()
        throws ParseException;

    public abstract void figureCaption_()
        throws ParseException;

    public abstract void tableCell()
        throws ParseException;

    public abstract void tableCell_()
        throws ParseException;

    public abstract void tableCaption()
        throws ParseException;

    public abstract void tableCaption_()
        throws ParseException;

    public abstract void figureGraphics(String s)
        throws ParseException;

    public abstract void horizontalRule()
        throws ParseException;

    public abstract void pageBreak()
        throws ParseException;

    public abstract void anchor(String s)
        throws ParseException;

    public abstract void anchor_()
        throws ParseException;

    public abstract void link(String s)
        throws ParseException;

    public abstract void link_()
        throws ParseException;

    public abstract void italic()
        throws ParseException;

    public abstract void italic_()
        throws ParseException;

    public abstract void bold()
        throws ParseException;

    public abstract void bold_()
        throws ParseException;

    public abstract void monospaced()
        throws ParseException;

    public abstract void monospaced_()
        throws ParseException;

    public abstract void lineBreak()
        throws ParseException;

    public abstract void nonBreakingSpace()
        throws ParseException;

    public abstract void text(String s)
        throws ParseException;

    public static final int NUMBERING_DECIMAL = 0;
    public static final int NUMBERING_LOWER_ALPHA = 1;
    public static final int NUMBERING_UPPER_ALPHA = 2;
    public static final int NUMBERING_LOWER_ROMAN = 3;
    public static final int NUMBERING_UPPER_ROMAN = 4;
}

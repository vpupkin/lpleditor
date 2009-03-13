// 
// 
// 
// Source File Name:   DocBookSink.java

package fr.pixware.apt.convert;

import fr.pixware.apt.parse.*;
import fr.pixware.util.FileUtil;
import java.io.*;
import java.util.StringTokenizer;

// Referenced classes of package fr.pixware.apt.convert:
//            LineBreaker, StructureSink

public class DocBookSink extends SinkAdapter
{

    public DocBookSink(Writer out)
    {
        xmlMode = false;
        encoding = null;
        styleSheet = null;
        lang = null;
        publicId = null;
        systemId = null;
        this.out = new LineBreaker(out);
        setItalicElement("<emphasis>");
        setBoldElement("<emphasis role=\"bold\">");
        setMonospacedElement("<literal>");
        setHorizontalRuleElement("<!-- HR -->");
        setPageBreakElement("<!-- PB -->");
        setLineBreakElement("<!-- LB -->");
    }

    public static final String escapeSGML(String text, boolean xmlMode)
    {
        int length = text.length();
        StringBuffer buffer = new StringBuffer(length);
        for(int i = 0; i < length; i++)
        {
            char c = text.charAt(i);
            switch(c)
            {
            case 60: // '<'
                buffer.append("&lt;");
                break;

            case 62: // '>'
                buffer.append("&gt;");
                break;

            case 38: // '&'
                buffer.append("&amp;");
                break;

            case 34: // '"'
                buffer.append("&quot;");
                break;

            default:
                if(xmlMode)
                {
                    buffer.append(c);
                    break;
                }
                if(c <= '~')
                {
                    buffer.append(c);
                } else
                {
                    buffer.append("&#");
                    buffer.append(c);
                    buffer.append(';');
                }
                break;
            }
        }

        return buffer.toString();
    }

    public void setXMLMode(boolean xmlMode)
    {
        this.xmlMode = xmlMode;
    }

    public boolean isXMLMode()
    {
        return xmlMode;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setStyleSheet(String styleSheet)
    {
        this.styleSheet = styleSheet;
    }

    public String getStyleSheet()
    {
        return styleSheet;
    }

    public void setPublicId(String publicId)
    {
        this.publicId = publicId;
    }

    public String getPublicId()
    {
        return publicId;
    }

    public void setSystemId(String systemId)
    {
        this.systemId = systemId;
    }

    public String getSystemId()
    {
        return systemId;
    }

    public void setLanguage(String lang)
    {
        this.lang = lang;
    }

    public String getLanguage()
    {
        return lang;
    }

    public void setItalicElement(String italicBeginTag)
    {
        if(italicBeginTag == null)
            italicBeginTag = "";
        this.italicBeginTag = italicBeginTag;
        italicEndTag = makeEndTag(italicBeginTag);
    }

    private String makeEndTag(String beginTag)
    {
        int length = beginTag.length();
        if(length == 0)
            return "";
        if(beginTag.charAt(0) != '<' || beginTag.charAt(length - 1) != '>')
            throw new IllegalArgumentException("'" + beginTag + "', not a tag");
        StringTokenizer tokens = new StringTokenizer(beginTag, "<> \t\n\r\f");
        if(!tokens.hasMoreTokens())
            throw new IllegalArgumentException("'" + beginTag + "', invalid tag");
        else
            return "</" + tokens.nextToken() + ">";
    }

    public String getItalicElement()
    {
        return italicBeginTag;
    }

    public void setBoldElement(String boldBeginTag)
    {
        if(boldBeginTag == null)
            boldBeginTag = "";
        this.boldBeginTag = boldBeginTag;
        boldEndTag = makeEndTag(boldBeginTag);
    }

    public String getBoldElement()
    {
        return boldBeginTag;
    }

    public void setMonospacedElement(String monospacedBeginTag)
    {
        if(monospacedBeginTag == null)
            monospacedBeginTag = "";
        this.monospacedBeginTag = monospacedBeginTag;
        monospacedEndTag = makeEndTag(monospacedBeginTag);
    }

    public String getMonospacedElement()
    {
        return monospacedBeginTag;
    }

    public void setHorizontalRuleElement(String horizontalRuleElement)
    {
        this.horizontalRuleElement = horizontalRuleElement;
    }

    public String getHorizontalRuleElement()
    {
        return horizontalRuleElement;
    }

    public void setPageBreakElement(String pageBreakElement)
    {
        this.pageBreakElement = pageBreakElement;
    }

    public String getPageBreakElement()
    {
        return pageBreakElement;
    }

    public void setLineBreakElement(String lineBreakElement)
    {
        this.lineBreakElement = lineBreakElement;
    }

    public String getLineBreakElement()
    {
        return lineBreakElement;
    }

    private void resetState()
    {
        hasTitle = false;
        authorDateFlag = false;
        verbatimFlag = false;
        externalLinkFlag = false;
        graphicsFileName = null;
        tableHasCaption = false;
        savedOut = null;
        tableRows = null;
        tableHasGrid = false;
    }

    public void head()
        throws ParseException
    {
        resetState();
        if(xmlMode)
        {
            markup("<?xml version=\"1.0\"");
            if(encoding != null)
                markup(" encoding=\"" + encoding + "\"");
            markup(" ?>\n");
            if(styleSheet != null)
                markup("<?xml-stylesheet type=\"text/css\"\nhref=\"" + styleSheet + "\" ?>\n");
        }
        markup("<!DOCTYPE article PUBLIC");
        String pubId;
        if(publicId == null)
        {
            if(xmlMode)
                pubId = "-//OASIS//DTD DocBook XML V4.1.2//EN";
            else
                pubId = "-//OASIS//DTD DocBook V4.1//EN";
        } else
        {
            pubId = publicId;
        }
        markup(" \"" + pubId + "\"");
        String sysId = systemId;
        if(sysId == null && xmlMode)
            sysId = "http://www.oasis-open.org/docbook/xml/4.0/docbookx.dtd";
        if(sysId == null)
            markup(">\n");
        else
            markup("\n\"" + sysId + "\">\n");
        markup("<article");
        if(lang != null)
            markup(" lang=\"" + lang + "\"");
        markup(">\n");
    }

    public void head_()
        throws ParseException
    {
        if(hasTitle)
        {
            markup("</articleinfo>\n");
            hasTitle = false;
        }
    }

    public void title()
        throws ParseException
    {
        markup("<articleinfo>\n");
        hasTitle = true;
        markup("<title>");
    }

    public void title_()
        throws ParseException
    {
        markup("</title>\n");
    }

    public void author()
        throws ParseException
    {
        authorDateFlag = true;
        markup("<corpauthor>");
    }

    public void author_()
        throws ParseException
    {
        markup("</corpauthor>\n");
        authorDateFlag = false;
    }

    public void date()
        throws ParseException
    {
        authorDateFlag = true;
        markup("<date>");
    }

    public void date_()
        throws ParseException
    {
        markup("</date>\n");
        authorDateFlag = false;
    }

    public void body_()
        throws ParseException
    {
        markup("</article>\n");
        out.flush();
        resetState();
    }

    public void section1()
        throws ParseException
    {
        markup("<section>\n");
    }

    public void section1_()
        throws ParseException
    {
        markup("</section>\n");
    }

    public void section2()
        throws ParseException
    {
        markup("<section>\n");
    }

    public void section2_()
        throws ParseException
    {
        markup("</section>\n");
    }

    public void section3()
        throws ParseException
    {
        markup("<section>\n");
    }

    public void section3_()
        throws ParseException
    {
        markup("</section>\n");
    }

    public void section4()
        throws ParseException
    {
        markup("<section>\n");
    }

    public void section4_()
        throws ParseException
    {
        markup("</section>\n");
    }

    public void section5()
        throws ParseException
    {
        markup("<section>\n");
    }

    public void section5_()
        throws ParseException
    {
        markup("</section>\n");
    }

    public void sectionTitle()
        throws ParseException
    {
        markup("<title>");
    }

    public void sectionTitle_()
        throws ParseException
    {
        markup("</title>\n");
    }

    public void list()
        throws ParseException
    {
        markup("<itemizedlist>\n");
    }

    public void list_()
        throws ParseException
    {
        markup("</itemizedlist>\n");
    }

    public void listItem()
        throws ParseException
    {
        markup("<listitem>\n");
    }

    public void listItem_()
        throws ParseException
    {
        markup("</listitem>\n");
    }

    public void numberedList(int numbering)
        throws ParseException
    {
        String numeration;
        switch(numbering)
        {
        case 2: // '\002'
            numeration = "upperalpha";
            break;

        case 1: // '\001'
            numeration = "loweralpha";
            break;

        case 4: // '\004'
            numeration = "upperroman";
            break;

        case 3: // '\003'
            numeration = "lowerroman";
            break;

        case 0: // '\0'
        default:
            numeration = "arabic";
            break;
        }
        markup("<orderedlist numeration=\"" + numeration + "\">\n");
    }

    public void numberedList_()
        throws ParseException
    {
        markup("</orderedlist>\n");
    }

    public void numberedListItem()
        throws ParseException
    {
        markup("<listitem>\n");
    }

    public void numberedListItem_()
        throws ParseException
    {
        markup("</listitem>\n");
    }

    public void definitionList()
        throws ParseException
    {
        markup("<variablelist>\n");
    }

    public void definitionList_()
        throws ParseException
    {
        markup("</variablelist>\n");
    }

    public void definitionListItem()
        throws ParseException
    {
        markup("<varlistentry>\n");
    }

    public void definitionListItem_()
        throws ParseException
    {
        markup("</varlistentry>\n");
    }

    public void definedTerm()
        throws ParseException
    {
        markup("<term>");
    }

    public void definedTerm_()
        throws ParseException
    {
        markup("</term>\n");
    }

    public void definition()
        throws ParseException
    {
        markup("<listitem>\n");
    }

    public void definition_()
        throws ParseException
    {
        markup("</listitem>\n");
    }

    public void paragraph()
        throws ParseException
    {
        markup("<para>");
    }

    public void paragraph_()
        throws ParseException
    {
        markup("</para>\n");
    }

    public void verbatim(boolean boxed)
        throws ParseException
    {
        verbatimFlag = true;
        markup("<programlisting>");
    }

    public void verbatim_()
        throws ParseException
    {
        markup("</programlisting>\n");
        verbatimFlag = false;
    }

    public void horizontalRule()
        throws ParseException
    {
        markup(horizontalRuleElement + '\n');
    }

    public void pageBreak()
        throws ParseException
    {
        markup(horizontalRuleElement + '\n');
    }

    public void figure_()
        throws ParseException
    {
        graphicElement();
    }

    protected void graphicElement()
        throws ParseException
    {
        if(graphicsFileName != null)
        {
            String format = FileUtil.fileExtension(graphicsFileName).toUpperCase();
            if(format.length() == 0)
                format = "JPEG";
            markup("<mediaobject>\n<imageobject>\n");
            markup("<imagedata format=\"" + format + "\"\nfileref=\"" + escapeSGML(graphicsFileName, xmlMode) + '"');
            if(xmlMode)
                markup("/>\n");
            else
                markup(">\n");
            markup("</imageobject>\n</mediaobject>\n");
            graphicsFileName = null;
        }
    }

    public void figureGraphics(String name)
        throws ParseException
    {
        graphicsFileName = name + ".jpeg";
    }

    public void figureCaption()
        throws ParseException
    {
        markup("<figure>\n");
        markup("<title>");
    }

    public void figureCaption_()
        throws ParseException
    {
        markup("</title>\n");
        graphicElement();
        markup("</figure>\n");
    }

    public void table()
        throws ParseException
    {
        tableHasCaption = false;
    }

    public void table_()
        throws ParseException
    {
        if(tableHasCaption)
        {
            tableHasCaption = false;
            out.write(tableRows, true);
            markup("</table>\n");
        } else
        {
            String frame;
            int sep;
            if(tableHasGrid)
            {
                frame = "all";
                sep = 1;
            } else
            {
                frame = "none";
                sep = 0;
            }
            markup("<informaltable frame=\"" + frame + "\" rowsep=\"" + sep + "\" colsep=\"" + sep + "\">\n");
            out.write(tableRows, true);
            markup("</informaltable>\n");
        }
        tableRows = null;
        tableHasGrid = false;
    }

    public void tableRows(int justification[], boolean grid)
        throws ParseException
    {
        tableHasGrid = grid;
        out.flush();
        savedOut = out;
        out = new LineBreaker(new StringWriter());
        markup("<tgroup cols=\"" + justification.length + "\">\n");
        for(int i = 0; i < justification.length; i++)
        {
            String justif;
            switch(justification[i])
            {
            case 1: // '\001'
                justif = "left";
                break;

            case 2: // '\002'
                justif = "right";
                break;

            case 0: // '\0'
            default:
                justif = "center";
                break;
            }
            markup("<colspec align=\"" + justif + "\"");
            if(xmlMode)
                markup("/>\n");
            else
                markup(">\n");
        }

        markup("<tbody>\n");
    }

    public void tableRows_()
        throws ParseException
    {
        markup("</tbody>\n");
        markup("</tgroup>\n");
        out.flush();
        tableRows = ((StringWriter)out.getDestination()).toString();
        out = savedOut;
    }

    public void tableRow()
        throws ParseException
    {
        markup("<row>\n");
    }

    public void tableRow_()
        throws ParseException
    {
        markup("</row>\n");
    }

    public void tableCell()
        throws ParseException
    {
        markup("<entry><para>");
    }

    public void tableCell_()
        throws ParseException
    {
        markup("</para></entry>\n");
    }

    public void tableCaption()
        throws ParseException
    {
        tableHasCaption = true;
        String frame;
        int sep;
        if(tableHasGrid)
        {
            frame = "all";
            sep = 1;
        } else
        {
            frame = "none";
            sep = 0;
        }
        markup("<table frame=\"" + frame + "\" rowsep=\"" + sep + "\" colsep=\"" + sep + "\">\n");
        markup("<title>");
    }

    public void tableCaption_()
        throws ParseException
    {
        markup("</title>\n");
    }

    public void anchor(String name)
        throws ParseException
    {
        if(!authorDateFlag)
        {
            markup("<anchor id=\"a." + StructureSink.linkToKey(name) + "\"");
            if(xmlMode)
                markup("/>");
            else
                markup(">");
        }
    }

    public void link(String name)
        throws ParseException
    {
        if(StructureSink.isExternalLink(name))
        {
            externalLinkFlag = true;
            markup("<ulink url=\"" + escapeSGML(name, xmlMode) + "\">");
        } else
        {
            markup("<link linkend=\"a." + StructureSink.linkToKey(name) + "\">");
        }
    }

    public void link_()
        throws ParseException
    {
        if(externalLinkFlag)
        {
            markup("</ulink>");
            externalLinkFlag = false;
        } else
        {
            markup("</link>");
        }
    }

    public void italic()
        throws ParseException
    {
        markup(italicBeginTag);
    }

    public void italic_()
        throws ParseException
    {
        markup(italicEndTag);
    }

    public void bold()
        throws ParseException
    {
        markup(boldBeginTag);
    }

    public void bold_()
        throws ParseException
    {
        markup(boldEndTag);
    }

    public void monospaced()
        throws ParseException
    {
        if(!authorDateFlag)
            markup(monospacedBeginTag);
    }

    public void monospaced_()
        throws ParseException
    {
        if(!authorDateFlag)
            markup(monospacedEndTag);
    }

    public void lineBreak()
        throws ParseException
    {
        markup(lineBreakElement + '\n');
    }

    public void nonBreakingSpace()
        throws ParseException
    {
        markup("&nbsp;");
    }

    public void text(String text)
        throws ParseException
    {
        if(verbatimFlag)
            verbatimContent(text);
        else
            content(text);
    }

    protected void markup(String text)
        throws ParseException
    {
        out.write(text, true);
    }

    protected void content(String text)
        throws ParseException
    {
        out.write(escapeSGML(text, xmlMode), false);
    }

    protected void verbatimContent(String text)
        throws ParseException
    {
        out.write(escapeSGML(text, xmlMode), true);
    }

    public static void main(String args[])
    {
        Parser parser = new Parser();
        ReaderSource source = new ReaderSource(new InputStreamReader(System.in));
        DocBookSink sink = new DocBookSink(new OutputStreamWriter(System.out));
        try
        {
            parser.parse(source, sink);
        }
        catch(ParseException e)
        {
            System.err.println("conversion error: " + e.getMessage() + "\n(near line " + parser.getSourceLineNumber() + ")");
            System.exit(2);
        }
    }

    public static final String DEFAULT_SGML_PUBLIC_ID = "-//OASIS//DTD DocBook V4.1//EN";
    public static final String DEFAULT_XML_PUBLIC_ID = "-//OASIS//DTD DocBook XML V4.1.2//EN";
    public static final String DEFAULT_XML_SYSTEM_ID = "http://www.oasis-open.org/docbook/xml/4.0/docbookx.dtd";
    private LineBreaker out;
    private boolean xmlMode;
    private String encoding;
    private String styleSheet;
    private String lang;
    private String publicId;
    private String systemId;
    private String italicBeginTag;
    private String italicEndTag;
    private String boldBeginTag;
    private String boldEndTag;
    private String monospacedBeginTag;
    private String monospacedEndTag;
    private String horizontalRuleElement;
    private String pageBreakElement;
    private String lineBreakElement;
    protected String graphicsFileName;
    private boolean hasTitle;
    private boolean authorDateFlag;
    private boolean verbatimFlag;
    private boolean externalLinkFlag;
    private boolean tableHasCaption;
    private LineBreaker savedOut;
    private String tableRows;
    private boolean tableHasGrid;
}

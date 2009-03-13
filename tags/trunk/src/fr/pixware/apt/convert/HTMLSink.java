// 
// 
// 
// Source File Name:   HTMLSink.java

package fr.pixware.apt.convert;

import fr.pixware.apt.parse.*;
import fr.pixware.util.FileUtil;
import java.io.*;
import java.net.URL;

// Referenced classes of package fr.pixware.apt.convert:
//            LineBreaker, StructureSink

public class HTMLSink extends SinkAdapter
{

    public HTMLSink(Writer out)
    {
        xmlMode = false;
        strictMode = false;
        systemId = null;
        charset = null;
        lang = null;
        this.out = new LineBreaker(out);
    }

    public HTMLSink()
    {
        this(((Writer) (new StringWriter())));
    }

    public void setWriter(Writer writer)
        throws ParseException
    {
        out.flush();
        out = new LineBreaker(writer);
    }

    public void setXMLMode(boolean xmlMode)
    {
        this.xmlMode = xmlMode;
    }

    public boolean isXMLMode()
    {
        return xmlMode;
    }

    public void setStrictMode(boolean strictMode)
    {
        this.strictMode = strictMode;
    }

    public boolean isStrictMode()
    {
        return strictMode;
    }

    public void setSystemId(String systemId)
    {
        this.systemId = systemId;
    }

    public String getSystemId()
    {
        return systemId;
    }

    public void setCharset(String charset)
    {
        this.charset = charset;
    }

    public String getCharset()
    {
        return charset;
    }

    public void setLanguage(String lang)
    {
        this.lang = lang;
    }

    public String getLanguage()
    {
        return lang;
    }

    public static String escapeHTML(String text)
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
                buffer.append(c);
                break;
            }
        }

        return buffer.toString();
    }

    public static String fileToURL(String fileName)
    {
        File file = new File(fileName);
        String urlName;
        if(file.isAbsolute())
        {
            URL url = FileUtil.fileToURL(file);
            if(url == null)
                urlName = fileName;
            else
                urlName = url.toExternalForm();
        } else
        if(File.separatorChar != '/')
            urlName = fileName.replace(File.separatorChar, '/');
        else
            urlName = fileName;
        return encodeURL(urlName);
    }

    public static String encodeURL(String text)
    {
        StringBuffer encoded = new StringBuffer();
        int length = text.length();
        char unicode[] = new char[1];
        for(int i = 0; i < length; i++)
        {
            char c = text.charAt(i);
            switch(c)
            {
            case 33: // '!'
            case 35: // '#'
            case 36: // '$'
            case 38: // '&'
            case 39: // '\''
            case 40: // '('
            case 41: // ')'
            case 42: // '*'
            case 43: // '+'
            case 44: // ','
            case 45: // '-'
            case 46: // '.'
            case 47: // '/'
            case 58: // ':'
            case 59: // ';'
            case 61: // '='
            case 63: // '?'
            case 64: // '@'
            case 91: // '['
            case 93: // ']'
            case 95: // '_'
            case 126: // '~'
                encoded.append(c);
                break;

            case 34: // '"'
            case 37: // '%'
            case 48: // '0'
            case 49: // '1'
            case 50: // '2'
            case 51: // '3'
            case 52: // '4'
            case 53: // '5'
            case 54: // '6'
            case 55: // '7'
            case 56: // '8'
            case 57: // '9'
            case 60: // '<'
            case 62: // '>'
            case 65: // 'A'
            case 66: // 'B'
            case 67: // 'C'
            case 68: // 'D'
            case 69: // 'E'
            case 70: // 'F'
            case 71: // 'G'
            case 72: // 'H'
            case 73: // 'I'
            case 74: // 'J'
            case 75: // 'K'
            case 76: // 'L'
            case 77: // 'M'
            case 78: // 'N'
            case 79: // 'O'
            case 80: // 'P'
            case 81: // 'Q'
            case 82: // 'R'
            case 83: // 'S'
            case 84: // 'T'
            case 85: // 'U'
            case 86: // 'V'
            case 87: // 'W'
            case 88: // 'X'
            case 89: // 'Y'
            case 90: // 'Z'
            case 92: // '\\'
            case 94: // '^'
            case 96: // '`'
            case 97: // 'a'
            case 98: // 'b'
            case 99: // 'c'
            case 100: // 'd'
            case 101: // 'e'
            case 102: // 'f'
            case 103: // 'g'
            case 104: // 'h'
            case 105: // 'i'
            case 106: // 'j'
            case 107: // 'k'
            case 108: // 'l'
            case 109: // 'm'
            case 110: // 'n'
            case 111: // 'o'
            case 112: // 'p'
            case 113: // 'q'
            case 114: // 'r'
            case 115: // 's'
            case 116: // 't'
            case 117: // 'u'
            case 118: // 'v'
            case 119: // 'w'
            case 120: // 'x'
            case 121: // 'y'
            case 122: // 'z'
            case 123: // '{'
            case 124: // '|'
            case 125: // '}'
            default:
                if(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9')
                {
                    encoded.append(c);
                    break;
                }
                byte bytes[];
                try
                {
                    unicode[0] = c;
                    bytes = (new String(unicode, 0, 1)).getBytes("UTF8");
                }
                catch(UnsupportedEncodingException unsupportedencodingexception)
                {
                    bytes = new byte[0];
                }
                for(int j = 0; j < bytes.length; j++)
                {
                    String hex = Integer.toHexString(bytes[j] & 0xff);
                    encoded.append('%');
                    if(hex.length() == 1)
                        encoded.append('0');
                    encoded.append(hex);
                }

                break;
            }
        }

        return encoded.toString();
    }

    public static String encodeFragment(String text)
    {
        return encodeURL(StructureSink.linkToKey(text));
    }

    public void head()
        throws ParseException
    {
        resetState();
        pageHead(null);
        headFlag = true;
    }

    public void head_()
        throws ParseException
    {
        if(!hasTitle)
            markup("<title></title>\n");
        headFlag = false;
        markup("</head>\n");
    }

    protected void resetState()
    {
        headFlag = false;
        hasTitle = false;
        buffer = new StringBuffer();
        sectionLevel = 0;
        itemFlag = false;
        boxedFlag = false;
        verbatimFlag = false;
        cellJustif = null;
        cellCount = 0;
    }

    protected void pageHead(String css)
        throws ParseException
    {
        if(xmlMode)
        {
            markup("<?xml version=\"1.0\"");
            if(charset != null)
                markup(" encoding=\"" + charset + "\"");
            markup(" ?>\n");
            if(css != null)
                markup("<?xml-stylesheet type=\"text/css\"\nhref=\"" + encodeURL(css) + "\" ?>\n");
        }
        String sysId = systemId;
        String pubId;
        if(xmlMode)
        {
            if(strictMode)
            {
                pubId = "-//W3C//DTD XHTML 1.0 Strict//EN";
                if(sysId == null)
                    sysId = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";
            } else
            {
                pubId = "-//W3C//DTD XHTML 1.0 Transitional//EN";
                if(sysId == null)
                    sysId = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd";
            }
        } else
        if(strictMode)
            pubId = "-//W3C//DTD HTML 4.01//EN";
        else
            pubId = "-//W3C//DTD HTML 4.01 Transitional//EN";
        markup("<!DOCTYPE html PUBLIC");
        markup(" \"" + pubId + "\"");
        if(sysId == null)
            markup(">\n");
        else
            markup("\n\"" + sysId + "\">\n");
        markup("<html");
        if(lang != null)
        {
            if(xmlMode)
                markup(" xml:lang=\"" + lang + "\"");
            markup(" lang=\"" + lang + "\"");
        }
        markup(">\n");
        markup("<head>\n");
        if(charset != null)
            markup("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + charset + (xmlMode ? "\" />\n" : "\">\n"));
        if(css != null)
            markup("<link rel=\"stylesheet\" type=\"text/css\"\nhref=\"" + encodeURL(css) + (xmlMode ? "\" />\n" : "\">\n"));
    }

    public void title_()
        throws ParseException
    {
        if(buffer.length() > 0)
        {
            markup("<title>");
            content(buffer.toString());
            markup("</title>\n");
            buffer = new StringBuffer();
            hasTitle = true;
        }
    }

    public void author_()
        throws ParseException
    {
        if(buffer.length() > 0)
        {
            markup("<meta name=\"author\" content=\"");
            content(buffer.toString());
            markup(xmlMode ? "\" />\n" : "\">\n");
            buffer = new StringBuffer();
        }
    }

    public void date_()
        throws ParseException
    {
        if(buffer.length() > 0)
        {
            markup("<meta name=\"date\" content=\"");
            content(buffer.toString());
            markup(xmlMode ? "\" />\n" : "\">\n");
            buffer = new StringBuffer();
        }
    }

    public void body()
        throws ParseException
    {
        markup("<body>\n");
    }

    public void body_()
        throws ParseException
    {
        markup("</body>\n");
        markup("</html>\n");
        out.flush();
        resetState();
    }

    public void section1()
        throws ParseException
    {
        sectionLevel = 1;
    }

    public void section2()
        throws ParseException
    {
        sectionLevel = 2;
    }

    public void section3()
        throws ParseException
    {
        sectionLevel = 3;
    }

    public void section4()
        throws ParseException
    {
        sectionLevel = 4;
    }

    public void section5()
        throws ParseException
    {
        sectionLevel = 5;
    }

    public void sectionTitle()
        throws ParseException
    {
        markup("<h" + sectionLevel + ">");
    }

    public void sectionTitle_()
        throws ParseException
    {
        markup("</h" + sectionLevel + ">\n");
    }

    public void list()
        throws ParseException
    {
        markup("<ul>\n");
    }

    public void list_()
        throws ParseException
    {
        markup("</ul>");
    }

    public void listItem()
        throws ParseException
    {
        markup("<li>");
        itemFlag = true;
    }

    public void listItem_()
        throws ParseException
    {
        markup("</li>\n");
    }

    public void numberedList(int numbering)
        throws ParseException
    {
        String style;
        switch(numbering)
        {
        case 2: // '\002'
            style = "upper-alpha";
            break;

        case 1: // '\001'
            style = "lower-alpha";
            break;

        case 4: // '\004'
            style = "upper-roman";
            break;

        case 3: // '\003'
            style = "lower-roman";
            break;

        case 0: // '\0'
        default:
            style = "decimal";
            break;
        }
        markup("<ol style=\"list-style-type: " + style + "\">\n");
    }

    public void numberedList_()
        throws ParseException
    {
        markup("</ol>");
    }

    public void numberedListItem()
        throws ParseException
    {
        markup("<li>");
        itemFlag = true;
    }

    public void numberedListItem_()
        throws ParseException
    {
        markup("</li>\n");
    }

    public void definitionList()
        throws ParseException
    {
        String dl;
        if(strictMode)
            dl = "<dl>\n";
        else
        if(xmlMode)
            dl = "<dl compact=\"compact\">\n";
        else
            dl = "<dl compact>\n";
        markup(dl);
    }

    public void definitionList_()
        throws ParseException
    {
        markup("</dl>");
    }

    public void definedTerm()
        throws ParseException
    {
        markup(strictMode ? "<dt style=\"display: compact\"><b>" : "<dt><b>");
    }

    public void definedTerm_()
        throws ParseException
    {
        markup("</b></dt>\n");
    }

    public void definition()
        throws ParseException
    {
        markup("<dd>");
        itemFlag = true;
    }

    public void definition_()
        throws ParseException
    {
        markup("</dd>\n");
    }

    public void paragraph()
        throws ParseException
    {
        if(!itemFlag)
            markup("<p>");
    }

    public void paragraph_()
        throws ParseException
    {
        if(itemFlag)
            itemFlag = false;
        else
            markup("</p>");
    }

    public void verbatim(boolean boxed)
        throws ParseException
    {
        verbatimFlag = true;
        boxedFlag = boxed;
        if(boxed)
            markup("<div class=\"box\" style=\"border: 2px solid; width: 100%\">");
        markup("<pre>");
    }

    public void verbatim_()
        throws ParseException
    {
        if(boxedFlag)
            markup("</pre></div>");
        else
            markup("</pre>");
        verbatimFlag = false;
        boxedFlag = false;
    }

    public void horizontalRule()
        throws ParseException
    {
        markup(xmlMode ? "<hr />" : "<hr>");
    }

    public void figure()
        throws ParseException
    {
        markup(strictMode ? "<div class=\"center\" style=\"margin-left: auto; margin-right: auto; text-align: center\">" : "<center>");
    }

    public void figure_()
        throws ParseException
    {
        markup(strictMode ? "</div>" : "</center>");
    }

    public void figureGraphics(String name)
        throws ParseException
    {
        markup("<p><img src=\"" + fileToURL(name + ".gif") + "\"\n" + "alt=\"" + escapeHTML(name) + (xmlMode ? "\" /></p>" : "\"></p>"));
    }

    public void figureCaption()
        throws ParseException
    {
        markup("<p><i>");
    }

    public void figureCaption_()
        throws ParseException
    {
        markup("</i></p>");
    }

    public void table()
        throws ParseException
    {
        markup(strictMode ? "<div class=\"center\" style=\"margin-left: auto; margin-right: auto; text-align: center\">" : "<center>");
    }

    public void table_()
        throws ParseException
    {
        markup(strictMode ? "</div>" : "</center>");
    }

    public void tableRows(int justification[], boolean grid)
        throws ParseException
    {
        markup("<table border=\"" + (grid ? 1 : 0) + "\">\n");
        cellJustif = justification;
    }

    public void tableRows_()
        throws ParseException
    {
        markup("</table>");
    }

    public void tableRow()
        throws ParseException
    {
        markup("<tr valign=\"top\">\n");
        cellCount = 0;
    }

    public void tableRow_()
        throws ParseException
    {
        markup("</tr>\n");
        cellCount = 0;
    }

    public void tableCell()
        throws ParseException
    {
        String justif;
        switch(cellJustif[cellCount])
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
        markup("<td align=\"" + justif + "\">");
    }

    public void tableCell_()
        throws ParseException
    {
        markup("</td>\n");
        cellCount++;
    }

    public void tableCaption()
        throws ParseException
    {
        markup("<p><i>");
    }

    public void tableCaption_()
        throws ParseException
    {
        markup("</i></p>");
    }

    public void anchor(String name)
        throws ParseException
    {
        if(!headFlag)
        {
            String id = encodeFragment(name);
            markup("<a id=\"" + id + "\" name=\"" + id + "\">");
        }
    }

    public void anchor_()
        throws ParseException
    {
        if(!headFlag)
            markup("</a>");
    }

    public void link(String name)
        throws ParseException
    {
        if(!headFlag)
        {
            String href;
            if(StructureSink.isExternalLink(name))
                href = encodeURL(name);
            else
                href = "#" + encodeFragment(name);
            markup("<a href=\"" + href + "\">");
        }
    }

    public void link_()
        throws ParseException
    {
        if(!headFlag)
            markup("</a>");
    }

    public void italic()
        throws ParseException
    {
        if(!headFlag)
            markup("<i>");
    }

    public void italic_()
        throws ParseException
    {
        if(!headFlag)
            markup("</i>");
    }

    public void bold()
        throws ParseException
    {
        if(!headFlag)
            markup("<b>");
    }

    public void bold_()
        throws ParseException
    {
        if(!headFlag)
            markup("</b>");
    }

    public void monospaced()
        throws ParseException
    {
        if(!headFlag)
            markup("<tt>");
    }

    public void monospaced_()
        throws ParseException
    {
        if(!headFlag)
            markup("</tt>");
    }

    public void lineBreak()
        throws ParseException
    {
        if(headFlag)
            buffer.append('\n');
        else
            markup(xmlMode ? "<br />" : "<br>");
    }

    public void nonBreakingSpace()
        throws ParseException
    {
        if(headFlag)
            buffer.append(' ');
        else
            markup("&nbsp;");
    }

    public void text(String text)
        throws ParseException
    {
        if(headFlag)
            buffer.append(text);
        else
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
        out.write(escapeHTML(text), false);
    }

    protected void verbatimContent(String text)
        throws ParseException
    {
        out.write(escapeHTML(text), true);
    }

    public static void main(String args[])
    {
        Parser parser = new Parser();
        ReaderSource source = new ReaderSource(new InputStreamReader(System.in));
        HTMLSink sink = new HTMLSink(new OutputStreamWriter(System.out));
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

    public static final String LOOSE_SGML_PUBLIC_ID = "-//W3C//DTD HTML 4.01 Transitional//EN";
    public static final String STRICT_SGML_PUBLIC_ID = "-//W3C//DTD HTML 4.01//EN";
    public static final String LOOSE_XML_PUBLIC_ID = "-//W3C//DTD XHTML 1.0 Transitional//EN";
    public static final String STRICT_XML_PUBLIC_ID = "-//W3C//DTD XHTML 1.0 Strict//EN";
    public static final String LOOSE_XML_SYSTEM_ID = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd";
    public static final String STRICT_XML_SYSTEM_ID = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd";
    public static final String XML_NAME_SPACE = "http://www.w3.org/1999/xhtml";
    private LineBreaker out;
    protected boolean xmlMode;
    protected boolean strictMode;
    protected String systemId;
    protected String charset;
    protected String lang;
    private boolean headFlag;
    private boolean hasTitle;
    private StringBuffer buffer;
    private int sectionLevel;
    private boolean itemFlag;
    private boolean boxedFlag;
    private boolean verbatimFlag;
    private int cellJustif[];
    private int cellCount;
}

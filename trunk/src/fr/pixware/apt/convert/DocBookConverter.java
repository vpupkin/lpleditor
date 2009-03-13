// 
// 
// 
// Source File Name:   DocBookConverter.java

package fr.pixware.apt.convert;

import fr.pixware.apt.parse.ParseException;
import fr.pixware.apt.parse.Sink;
import fr.pixware.util.FileUtil;
import fr.pixware.util.HashtableUtil;
import java.io.File;
import java.io.Writer;
import java.util.Hashtable;

// Referenced classes of package fr.pixware.apt.convert:
//            TwoPassConverter, DocBookSink, OnePassConverter, Driver, 
//            AnchorNotFoundWarning, StructureSink, Structure, Section

public class DocBookConverter extends TwoPassConverter
{
    private class ConvertSink extends DocBookSink
    {

        public void head()
            throws ParseException
        {
            super.head();
            structure = getStructure();
            resetSectionNumber();
        }

        public void section1()
            throws ParseException
        {
            updateSectionNumber(1);
            super.section1();
        }

        public void section2()
            throws ParseException
        {
            updateSectionNumber(2);
            super.section2();
        }

        public void section3()
            throws ParseException
        {
            updateSectionNumber(3);
            super.section3();
        }

        public void section4()
            throws ParseException
        {
            updateSectionNumber(4);
            super.section4();
        }

        public void section5()
            throws ParseException
        {
            updateSectionNumber(5);
            super.section5();
        }

        public void sectionTitle()
            throws ParseException
        {
            String num = getSectionNumber('.');
            markup("<title id=\"s." + num + "\">");
        }

        public void link(String name)
            throws ParseException
        {
            if(StructureSink.isExternalLink(name))
            {
                super.link(name);
                return;
            }
            String idref = null;
            Anchor anchor = structure.getAnchor(name);
            if(anchor == null)
            {
                Section section = structure.getSection(name);
                if(section != null)
                    idref = "s." + section.getNumberFormatted();
            } else
            {
                idref = "a." + StructureSink.linkToKey(name);
            }
            if(idref == null)
            {
                getDriver().addWarning(new AnchorNotFoundWarning(name, getSourceFileName(), getSourceLineNumber()));
                idref = "a." + StructureSink.linkToKey(name);
            }
            markup("<link linkend=\"" + idref + "\">");
        }

        public void figureGraphics(String name)
            throws ParseException
        {
            String ext = null;
            try
            {
                ext = convertGraphics(name, DocBookConverter.supportedGraphics);
            }
            catch(Exception e)
            {
                throw new ParseException(e);
            }
            super.graphicsFileName = name + "." + ext;
        }

        private void resetSectionNumber()
        {
            for(int i = 0; i < 5; i++)
                sectionNumber[i] = 0;

        }

        private void updateSectionNumber(int sectionLevel)
        {
            int i = sectionLevel - 1;
            sectionNumber[i]++;
            for(i++; i < 5; i++)
                sectionNumber[i] = 0;

        }

        private String getSectionNumber(char separ)
        {
            return Section.formatNumber(sectionNumber, separ);
        }

        private Structure structure;
        private int sectionNumber[];

        public ConvertSink(Writer out)
        {
            super(out);
            sectionNumber = new int[5];
        }
    }


    public DocBookConverter(Driver driver)
    {
        this(driver, false);
    }

    public DocBookConverter(Driver driver, boolean xmlMode)
    {
        super(driver);
        this.xmlMode = xmlMode;
    }

    public String getConverterInfo()
    {
        StringBuffer info = new StringBuffer();
        info.append("Supported option values:\n");
        info.append("  -enc");
        for(int i = 0; i < knownEncodingList.length; i += 2)
        {
            if(i % 10 == 0 && i > 0)
                info.append("\n       ");
            else
                info.append(' ');
            info.append(knownEncodingList[i]);
        }

        info.append("\n\n");
        info.append("Supported processing instructions (PI):\n");
        info.append("  * docbook.publicId=<publicId>.\n");
        info.append("    Default: -//OASIS//DTD DocBook V4.1//EN\n");
        info.append("    in SGML mode;\n");
        info.append("    -//OASIS//DTD DocBook XML V4.1.2//EN\n");
        info.append("    in XML mode.\n");
        info.append("  * docbook.systemId=<URL>|<file>.\n");
        info.append("    Mandatory in XML for generating valid documents.\n");
        info.append("    Default: none in SGML mode;\n");
        info.append("    http://www.oasis-open.org/docbook/xml/4.0/docbookx.dtd\n");
        info.append("    in XML mode.\n");
        info.append("  * docbook.css=<file name>.\n");
        info.append("    XML mode only. Copy file named <file name> to\n");
        info.append("    the ouput directory. Add a corresponding\n");
        info.append("    xml-stylesheet PI to the generated document.\n");
        info.append("    Default: none (no style sheet).\n");
        info.append("  * docbook.italic=<elemTag>.\n");
        info.append("    Default: <emphasis>.\n");
        info.append("  * docbook.bold=<elemTag>.\n");
        info.append("    Default: <emphasis role=\"bold\">.\n");
        info.append("  * docbook.monospaced=<elemTag>.\n");
        info.append("    Default: <literal>.\n");
        info.append("  * docbook.horizontalRule=<string>.\n");
        info.append("    Default: <!-- HR -->.\n");
        info.append("  * docbook.pageBreak=<string>.\n");
        info.append("    Default: <!-- PB -->.\n");
        info.append("  * docbook.lineBreak=<string>.\n");
        info.append("    Default: <!-- LB -->.\n");
        info.append("Required image format is GIF, JPEG or EPS.\n");
        return info.toString();
    }

    protected Sink createSink(Writer out)
        throws Exception
    {
        ConvertSink sink = new ConvertSink(out);
        sink.setXMLMode(xmlMode);
        Driver driver = getDriver();
        String encoding = (String)knownEncodings.get(driver.getEncoding());
        if(encoding == null)
            throw new RuntimeException("unsupported encoding '" + encoding + "'");
        sink.setEncoding(encoding);
        sink.setLanguage(driver.getLanguage());
        String pi = driver.getPI("docbook", "publicId");
        if(pi != null)
            sink.setPublicId(pi);
        pi = driver.getPI("docbook", "systemId");
        if(pi != null)
            sink.setSystemId(pi);
        String css = driver.getPI("docbook", "css");
        if(css != null && (new File(css)).isFile())
        {
            String baseName = FileUtil.fileBaseName(css);
            try
            {
                String dst = FileUtil.fileDirName(getOutFileName()) + File.separatorChar + baseName;
                FileUtil.copyFile(css, dst);
            }
            catch(Exception exception)
            {
                css = null;
            }
            css = baseName;
        }
        if(css != null)
            sink.setStyleSheet(css);
        pi = driver.getPI("docbook", "italic");
        if(pi != null)
            sink.setItalicElement(pi);
        pi = driver.getPI("docbook", "bold");
        if(pi != null)
            sink.setBoldElement(pi);
        pi = driver.getPI("docbook", "monospaced");
        if(pi != null)
            sink.setMonospacedElement(pi);
        pi = driver.getPI("docbook", "horizontalRule");
        if(pi != null)
            sink.setHorizontalRuleElement(pi);
        pi = driver.getPI("docbook", "pageBreak");
        if(pi != null)
            sink.setPageBreakElement(pi);
        pi = driver.getPI("docbook", "lineBreak");
        if(pi != null)
            sink.setLineBreakElement(pi);
        return sink;
    }

    private static final String knownEncodingList[] = {
        "ISO8859_1", "ISO-8859-1", "ISO8859_2", "ISO-8859-2", "ISO8859_3", "ISO-8859-3", "ISO8859_4", "ISO-8859-4", "ISO8859_5", "ISO-8859-5", 
        "ISO8859_6", "ISO-8859-6", "ISO8859_7", "ISO-8859-7", "ISO8859_8", "ISO-8859-8", "ISO8859_9", "ISO-8859-9", "ISO8859_13", "ISO-8859-13", 
        "ISO8859_15_FDIS", "ISO-8859-15", "Cp1250", "Windows-1250", "Cp1251", "Windows-1251", "Cp1252", "Windows-1252", "Cp1253", "Windows-1253", 
        "MacRoman", "macintosh", "MacTurkish", "x-mac-turkish", "MacCentralEurope", "x-mac-ce", "MacCyrillic", "x-mac-cyrillic", "MacGreek", "x-mac-greek", 
        "MacArabic", "x-mac-arabic", "MacCroatian", "x-mac-croatian", "MacHebrew", "x-mac-hebrew", "MacIceland", "x-mac-iceland", "MacRomania", "x-mac-romania", 
        "MacThai", "x-mac-thai", "MacUkraine", "x-mac-ukraine", "SJIS", "Shift_JIS", "UTF8", "UTF-8", "ASCII", "US-ASCII"
    };
    private static Hashtable knownEncodings;
    private static final String supportedGraphics[] = {
        "gif", "jpeg", "jpg", "eps"
    };
    private boolean xmlMode;

    static 
    {
        knownEncodings = new Hashtable();
        HashtableUtil.putAll(knownEncodings, knownEncodingList);
    }

}

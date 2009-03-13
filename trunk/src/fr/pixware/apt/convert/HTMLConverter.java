// 
// 
// 
// Source File Name:   HTMLConverter.java

package fr.pixware.apt.convert;

import fr.pixware.apt.parse.ParseException;
import fr.pixware.apt.parse.Sink;
import fr.pixware.util.*;
import java.io.*;
import java.text.Collator;
import java.util.Hashtable;
import java.util.Locale;

// Referenced classes of package fr.pixware.apt.convert:
//            TwoPassConverter, OnePassConverter, Driver, HTMLSink, 
//            AnchorNotFoundWarning, StructureSink, Structure, Section, 
//            Anchor

public class HTMLConverter extends TwoPassConverter
{
    private class ConvertSink extends HTMLSink
    {

        public void head()
            throws ParseException
        {
            resetState();
            structure = getStructure();
            headFlag = true;
        }

        public void head_()
            throws ParseException
        {
            headFlag = false;
        }

        public void link(String name)
            throws ParseException
        {
            if(!headFlag)
            {
                String href;
                if(StructureSink.isExternalLink(name))
                {
                    href = HTMLSink.encodeURL(name);
                } else
                {
                    href = null;
                    Anchor anchor = structure.getAnchor(name);
                    if(anchor == null)
                    {
                        Section section = structure.getSection(name);
                        if(section != null)
                        {
                            href = hrefFileName(section);
                            href = HTMLSink.encodeURL(href);
                            href = href + "#id.s" + section.getNumberFormatted();
                        }
                    } else
                    {
                        Section section = anchor.getSection();
                        href = HTMLSink.encodeURL(hrefFileName(section));
                        href = href + "#" + HTMLSink.encodeFragment(name);
                    }
                    if(href == null)
                    {
                        getDriver().addWarning(new AnchorNotFoundWarning(name, getSourceFileName(), getSourceLineNumber()));
                        href = "#" + HTMLSink.encodeFragment(name);
                    }
                }
                markup("<a href=\"" + href + "\">");
            }
        }

        public void link_()
            throws ParseException
        {
            if(!headFlag)
                markup("</a>");
        }

        public void text(String text)
            throws ParseException
        {
            if(!headFlag)
                super.text(text);
        }

        public void body()
            throws ParseException
        {
            resetSectionNumber();
            firstPage();
            Driver driver = getDriver();
            if(driver.isTOC() && paging <= 0)
            {
                makeTOC();
                markup(super.xmlMode ? "<hr />\n" : "<hr>\n");
                try
                {
                    HTMLConverter.copyIcon("apt_toc.gif", outDirName + File.separatorChar + "apt_toc.gif");
                }
                catch(IOException e)
                {
                    throw new ParseException(e);
                }
            }
        }

        public void body_()
            throws ParseException
        {
            Driver driver = getDriver();
            if(driver.isIndex() && paging <= 0)
            {
                markup(super.xmlMode ? "<hr />\n" : "<hr>\n");
                makeIndex();
            }
            endLastPage();
        }

        public void section1()
            throws ParseException
        {
            updateSectionNumber(1);
            nextPage(1);
            super.section1();
        }

        public void section2()
            throws ParseException
        {
            updateSectionNumber(2);
            nextPage(2);
            super.section2();
        }

        public void section3()
            throws ParseException
        {
            updateSectionNumber(3);
            nextPage(3);
            super.section3();
        }

        public void section4()
            throws ParseException
        {
            updateSectionNumber(4);
            nextPage(4);
            super.section4();
        }

        public void section5()
            throws ParseException
        {
            updateSectionNumber(5);
            nextPage(5);
            super.section5();
        }

        public void sectionTitle()
            throws ParseException
        {
            super.sectionTitle();
            String num = getSectionNumber('.');
            boolean toc = getDriver().isTOC();
            if(toc)
            {
                String id = "id.s" + num;
                markup("<a id=\"" + id + "\" name=\"" + id + "\">");
            }
            if(numberSections)
                content(num + ' ');
            if(toc)
                markup("</a>");
        }

        public void sectionTitle_()
            throws ParseException
        {
            if(getDriver().isTOC() && paging <= 0 && sectionNumber[1] == 0)
            {
                markup("<a href=\"#id.toc\">");
                markup("<img src=\"apt_toc.gif\"" + (super.strictMode ? " style=\"border-style: none; border-width: 0\"" : " border=\"0\"") + " width=\"30\" height=\"11\" alt=\"" + HTMLSink.escapeHTML((String)HTMLConverter.knownLanguages.get(super.lang + ".toc")) + (super.xmlMode ? "\" />" : "\">"));
                markup("</a>");
            }
            super.sectionTitle_();
        }

        public void figureGraphics(String name)
            throws ParseException
        {
            String ext = null;
            try
            {
                ext = convertGraphics(name, HTMLConverter.supportedGraphics);
                markup("<p><img src=\"" + HTMLSink.fileToURL(name + "." + ext) + "\"\n" + "alt=\"" + HTMLSink.escapeHTML(name) + (super.xmlMode ? "\" /></p>" : "\"></p>"));
            }
            catch(Exception e)
            {
                //throw new ParseException(e);
            	markup("<p><img src=\"http://misfunction.appspot.com/1590_eye_red_ani.gif\"\n" + "alt=\"" + HTMLSink.escapeHTML(name) + (super.xmlMode ? "\" /></p>" : "\"></p>"));
            }
            
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

        private void firstPage()
            throws ParseException
        {
            outWriter = null;
            toolbarFirstName = null;
            toolbarFirstText = null;
            toolbarLastName = null;
            toolbarLastText = null;
            toolbarName = null;
            toolbarText = null;
            nextPage(0);
        }

        private void nextPage(int sectionLevel)
            throws ParseException
        {
            if(sectionLevel > paging)
                return;
            if(sectionLevel == 0)
            {
                Driver driver = getDriver();
                if(driver.isTOC() && paging > 0)
                {
                    setOutWriter(outputFileName("toc"));
                    beginPage("toc");
                    makeTOC();
                    endPage();
                }
            } else
            {
                endPage();
            }
            String pageName;
            if(paging > 0)
                pageName = getSectionNumber('_');
            else
                pageName = "";
            setOutWriter(outputFileName(pageName));
            beginPage(pageName);
        }

        private String outputFileName(String pageName)
        {
            String fileName = outDirName + File.separatorChar + outBaseName + pageName;
            if(outExtension != null)
                fileName = fileName + "." + outExtension;
            return fileName;
        }

        private String hrefFileName(Section section)
        {
            String fileName;
            if(paging > 0)
            {
                if(section == null)
                {
                    fileName = hrefFileName("");
                } else
                {
                    int number[] = section.getNumber();
                    int number2[] = new int[5];
                    int j;
                    for(j = 0; j < paging; j++)
                        number2[j] = number[j];

                    for(; j < 5; j++)
                        number2[j] = 0;

                    fileName = hrefFileName(Section.formatNumber(number2, '_'));
                }
            } else
            {
                fileName = "";
            }
            return fileName;
        }

        private String hrefFileName(String pageName)
        {
            String fileName = outBaseName + pageName;
            if(outExtension != null)
                fileName = fileName + "." + outExtension;
            return fileName;
        }

        private void setOutWriter(String fileName)
            throws ParseException
        {
            Writer out = null;
            if(fileName == null)
            {
                setWriter(new StringWriter());
            } else
            {
                try
                {
                    out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), encoding));
                }
                catch(Exception e)
                {
                    throw new ParseException(e);
                }
                setWriter(out);
            }
            try
            {
                if(outWriter != null)
                    outWriter.close();
            }
            catch(IOException ioexception) { }
            outWriter = out;
        }

        private void endLastPage()
            throws ParseException
        {
            endPage();
            Driver driver = getDriver();
            if(driver.isIndex() && paging > 0)
            {
                setOutWriter(outputFileName("index"));
                beginPage("index");
                makeIndex();
                endPage();
            }
            setOutWriter(null);
            if(paging > 0 && !structure.hasPreSections() && structure.getSections().length > 0)
                (new File(outFileName)).delete();
        }

        private void beginPage(String pageName)
            throws ParseException
        {
            pageHead(css);
            String title = structure.getTitle();
            if(title == null)
            {
                for(int i = 0; i < userMeta.length; i += 2)
                {
                    if(!userMeta[i].equals("title"))
                        continue;
                    title = userMeta[i + 1];
                    break;
                }

            }
            markup("<title>");
            if(title != null)
                content(title);
            markup("</title>\n");
            String author = structure.getAuthor();
            if(author != null)
            {
                markup("<meta name=\"author\" content=\"");
                content(author);
                markup(super.xmlMode ? "\" />\n" : "\">\n");
            }
            String date = structure.getDate();
            if(date != null)
            {
                markup("<meta name=\"date\" content=\"");
                content(date);
                markup(super.xmlMode ? "\" />\n" : "\">\n");
            }
            for(int i = 0; i < userMeta.length; i += 2)
            {
                String meta = userMeta[i];
                if(!meta.equals("title") && (!meta.equals("author") || author == null) && (!meta.equals("date") || date == null))
                {
                    markup("<meta name=\"" + HTMLSink.escapeHTML(meta) + "\"" + " content=\"");
                    content(userMeta[i + 1]);
                    markup(super.xmlMode ? "\" />\n" : "\">\n");
                }
            }

            markup("</head>\n");
            markup("<body>\n");
            makeToolbar(pageName);
        }

        private void endPage()
            throws ParseException
        {
            markup("</body>\n");
            markup("</html>\n");
        }

        private void makeToolbar(String pageName)
            throws ParseException
        {
            if(paging <= 0)
                return;
            String title = structure.getTitle();
            if(title == null)
                title = "";
            if(toolbarFirstName == null)
            {
                for(int i = 0; i < HTMLConverter.toolbarIcons.length; i++)
                {
                    String icon = HTMLConverter.toolbarIcons[i];
                    try
                    {
                        HTMLConverter.copyIcon(icon, outDirName + File.separatorChar + icon);
                    }
                    catch(IOException e)
                    {
                        throw new ParseException(e);
                    }
                }

                Driver driver = getDriver();
                if(driver.isTOC())
                {
                    toolbarFirstName = "toc";
                    toolbarFirstText = (String)HTMLConverter.knownLanguages.get(super.lang + ".toc");
                } else
                {
                    Section section = getFirstPagedSection();
                    if(structure.hasPreSections() || section == null)
                    {
                        toolbarFirstName = "";
                        toolbarFirstText = title;
                    } else
                    {
                        toolbarFirstName = "1";
                        toolbarFirstText = section.getTitle();
                    }
                }
                if(driver.isIndex())
                {
                    toolbarLastName = "index";
                    toolbarLastText = (String)HTMLConverter.knownLanguages.get(super.lang + ".index");
                } else
                {
                    Section section = getLastPagedSection();
                    if(section == null)
                    {
                        toolbarLastName = "";
                        toolbarLastText = title;
                    } else
                    {
                        toolbarLastName = section.getNumberFormatted('_');
                        toolbarLastText = section.getTitle();
                    }
                }
            }
            markup(super.strictMode ? "<div class=\"center\" style=\"margin-left: auto; margin-right: auto; text-align: center\">" : "<center>");
            markup("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
            markup("<tr>\n");
            if(homeURL != null)
                makeToolIcon("apt_home.gif", homeURL, homeURL, homeURLTarget);
            if(pageName.equals("toc"))
            {
                makeToolIcon("apt_nofirst.gif", "", null, null);
                makeToolIcon("apt_noprev.gif", "", null, null);
                if(paging > 1)
                    makeToolIcon("apt_noup.gif", "", null, null);
                if(structure.hasPreSections())
                {
                    makeToolIcon("apt_next.gif", title, "", null);
                } else
                {
                    Section section = getFirstPagedSection();
                    if(section == null)
                        makeToolIcon("apt_next.gif", toolbarLastText, toolbarLastName, null);
                    else
                        makeToolIcon("apt_next.gif", section.getTitle(), section.getNumberFormatted('_'), null);
                }
                makeToolIcon("apt_last.gif", toolbarLastText, toolbarLastName, null);
                toolbarName = "toc";
                toolbarText = toolbarFirstText;
            } else
            if(pageName.equals(""))
            {
                if(toolbarName == null)
                {
                    makeToolIcon("apt_nofirst.gif", "", null, null);
                    makeToolIcon("apt_noprev.gif", "", null, null);
                } else
                {
                    makeToolIcon("apt_first.gif", toolbarFirstText, toolbarFirstName, null);
                    makeToolIcon("apt_prev.gif", toolbarText, toolbarName, null);
                }
                if(paging > 1)
                    makeToolIcon("apt_noup.gif", "", null, null);
                if(pageName.equals(toolbarLastName))
                {
                    makeToolIcon("apt_nonext.gif", "", null, null);
                    makeToolIcon("apt_nolast.gif", "", null, null);
                } else
                {
                    Section section = getFirstPagedSection();
                    if(section == null)
                        makeToolIcon("apt_next.gif", toolbarLastText, toolbarLastName, null);
                    else
                        makeToolIcon("apt_next.gif", section.getTitle(), section.getNumberFormatted('_'), null);
                    makeToolIcon("apt_last.gif", toolbarLastText, toolbarLastName, null);
                }
                toolbarName = "";
                toolbarText = title;
            } else
            if(pageName.equals("index"))
            {
                makeToolIcon("apt_first.gif", toolbarFirstText, toolbarFirstName, null);
                if(toolbarName == null)
                    makeToolIcon("apt_prev.gif", toolbarFirstText, toolbarFirstName, null);
                else
                    makeToolIcon("apt_prev.gif", toolbarText, toolbarName, null);
                if(paging > 1)
                    makeToolIcon("apt_noup.gif", "", null, null);
                makeToolIcon("apt_nonext.gif", "", null, null);
                makeToolIcon("apt_nolast.gif", "", null, null);
                toolbarName = null;
                toolbarText = null;
            } else
            {
                if(toolbarName != null && toolbarName.equals("") && !structure.hasPreSections())
                    if(pageName.equals(toolbarFirstName))
                    {
                        toolbarName = null;
                        toolbarText = null;
                    } else
                    {
                        toolbarName = toolbarFirstName;
                        toolbarText = toolbarFirstText;
                    }
                if(toolbarName == null)
                {
                    makeToolIcon("apt_nofirst.gif", "", null, null);
                    makeToolIcon("apt_noprev.gif", "", null, null);
                } else
                {
                    makeToolIcon("apt_first.gif", toolbarFirstText, toolbarFirstName, null);
                    makeToolIcon("apt_prev.gif", toolbarText, toolbarName, null);
                }
                Section current = structure.getSection(sectionNumber);
                if(paging > 1)
                {
                    Section section = current.getParent();
                    if(section == null)
                        makeToolIcon("apt_noup.gif", "", null, null);
                    else
                        makeToolIcon("apt_up.gif", section.getTitle(), section.getNumberFormatted('_'), null);
                }
                if(pageName.equals(toolbarLastName))
                {
                    makeToolIcon("apt_nonext.gif", "", null, null);
                    makeToolIcon("apt_nolast.gif", "", null, null);
                } else
                {
                    Section section = getNextPagedSection(current);
                    if(section == null)
                        makeToolIcon("apt_next.gif", toolbarLastText, toolbarLastName, null);
                    else
                        makeToolIcon("apt_next.gif", section.getTitle(), section.getNumberFormatted('_'), null);
                    makeToolIcon("apt_last.gif", toolbarLastText, toolbarLastName, null);
                }
                toolbarName = pageName;
                toolbarText = current.getTitle();
            }
            markup("</tr>\n");
            markup("</table>");
            markup(super.strictMode ? "</div>\n" : "</center>\n");
        }

        private Section getFirstPagedSection()
        {
            Section section = null;
            Section sections[] = structure.getSections();
            if(sections != null && sections.length > 0)
                section = sections[0];
            return section;
        }

        private Section getLastPagedSection()
        {
            Section section = null;
            Section sections[] = structure.getSections();
            for(int level = 1; sections != null && sections.length > 0; level++)
            {
                section = sections[sections.length - 1];
                if(level == paging)
                    break;
                sections = section.getChildren();
            }

            return section;
        }

        private Section getNextPagedSection(Section section)
        {
            Section next;
            if(section.getLevel() < paging)
            {
                Section sections[] = section.getChildren();
                if(sections == null || sections.length == 0)
                    next = null;
                else
                    next = sections[0];
            } else
            {
                next = null;
            }
            if(next == null)
                next = getNextSection(section);
            return next;
        }

        private Section getNextSection(Section section)
        {
            int number[] = section.getNumber();
            int number2[] = new int[5];
            for(int i = 0; i < 5; i++)
                number2[i] = number[i];

            number2[section.getLevel() - 1]++;
            Section next = structure.getSection(number2);
            if(next != null)
                return next;
            Section parent = section.getParent();
            if(parent == null)
                return null;
            else
                return getNextSection(parent);
        }

        private void makeToolIcon(String iconName, String iconText, String pageName, String target)
            throws ParseException
        {
            markup("<td>");
            String href;
            if(pageName == null)
                href = null;
            else
            if(StructureSink.isExternalLink(pageName))
                href = pageName;
            else
                href = hrefFileName(pageName);
            if(href != null)
            {
                markup("<a href=\"" + HTMLSink.encodeURL(href) + "\"");
                if(target != null)
                    markup(" target=\"" + target + "\"");
                markup("\n>");
            }
            markup("<img src=\"" + iconName + (super.strictMode ? "\" style=\"border-style: none; border-width: 0\"" : "\" border=\"0\"") + " width=\"" + 30 + "\" height=\"" + 20 + "\"\nalt=\"");
            content(iconText);
            markup(super.xmlMode ? "\" />" : "\">");
            if(href != null)
                markup("</a>");
            markup("</td>\n");
        }

        private void makeTOC()
            throws ParseException
        {
            markup("<h1><a id=\"id.toc\" name=\"id.toc\">");
            content((String)HTMLConverter.knownLanguages.get(super.lang + ".toc"));
            markup("</a></h1>\n");
            makeSectionsTOC(structure.getSections());
        }

        private void makeSectionsTOC(Section sections[])
            throws ParseException
        {
            if(sections == null || sections.length == 0)
                return;
            markup("<dl>\n");
            for(int i = 0; i < sections.length; i++)
            {
                Section section = sections[i];
                String href = hrefFileName(section);
                href = HTMLSink.encodeURL(href);
                href = href + "#id.s" + section.getNumberFormatted();
                markup("<dd><a href=\"" + href + "\">");
                if(numberSections)
                {
                    content(section.getNumberFormatted());
                    content(" ");
                }
                content(section.getTitle());
                markup("</a>");
                makeSectionsTOC(section.getChildren());
                markup("</dd>\n");
            }

            if(sections[0].getLevel() == 1)
            {
                Driver driver = getDriver();
                if(driver.isIndex())
                {
                    String href;
                    if(paging > 0)
                    {
                        href = hrefFileName("index");
                        href = HTMLSink.encodeURL(href);
                    } else
                    {
                        href = "#id.idx";
                    }
                    markup("<dd><a href=\"" + href + "\">");
                    content((String)HTMLConverter.knownLanguages.get(super.lang + ".index"));
                    markup("</a></dd>\n");
                }
            }
            markup("</dl>");
        }

        private void makeIndex()
            throws ParseException
        {
            markup("<h1><a id=\"id.idx\" name=\"id.idx\">");
            content((String)HTMLConverter.knownLanguages.get(super.lang + ".index"));
            markup("</a></h1>\n");
            Anchor anchors[] = structure.getAnchors();
            if(anchors.length == 0)
                return;
            final Collator collator = Collator.getInstance(new Locale(super.lang, ""));
            collator.setStrength(0);
            QuickSort.sort(anchors, new fr.pixware.util.QuickSort.Compare() {

                public int compare(Object o1, Object o2)
                {
                    Anchor a1 = (Anchor)o1;
                    Anchor a2 = (Anchor)o2;
                    return collator.compare(a1.getText(), a2.getText());
                }

            }
);
            String previousText = null;
            markup("<dl>\n");
            for(int i = 0; i < anchors.length; i++)
            {
                Anchor anchor = anchors[i];
                String text = anchor.getText();
                Section section = anchor.getSection();
                boolean sameLetter = true;
                char c1 = text.charAt(0);
                if(previousText != null)
                {
                    char c2 = previousText.charAt(0);
                    if(Character.isLetter(c1) && collator.compare(String.valueOf(c1), String.valueOf(c2)) != 0)
                        sameLetter = false;
                }
                String href = HTMLSink.encodeURL(hrefFileName(section));
                href = href + "#" + HTMLSink.encodeFragment(text);
                if(!sameLetter || previousText == null)
                {
                    if(previousText != null)
                    {
                        markup("</dl>\n");
                        markup("<dl>\n");
                    }
                    markup("<dt><big><b>" + Character.toUpperCase(c1) + "</b></big></dt>\n");
                }
                markup("<dd><a href=\"" + href + "\">");
                content(text);
                markup("</a></dd>\n");
                previousText = text;
            }

            markup("</dl>\n");
        }

        private String outFileName;
        private int paging;
        private String css;
        private String homeURL;
        private String homeURLTarget;
        private String encoding;
        private boolean numberSections;
        private String userMeta[];
        private String outDirName;
        private String outBaseName;
        private String outExtension;
        private Writer outWriter;
        private String toolbarFirstName;
        private String toolbarFirstText;
        private String toolbarLastName;
        private String toolbarLastText;
        private String toolbarName;
        private String toolbarText;
        private Structure structure;
        private int sectionNumber[];
        private boolean headFlag;

        public ConvertSink(String outFileName, String charset, String language, boolean xmlMode, boolean strictMode, String systemId, 
                int paging, String css, String homeURL, String homeURLTarget)
        {
            sectionNumber = new int[5];
            super.charset = charset;
            super.lang = language;
            super.xmlMode = xmlMode;
            super.strictMode = strictMode;
            super.systemId = systemId;
            this.outFileName = outFileName;
            this.paging = paging;
            this.css = css;
            this.homeURL = homeURL;
            this.homeURLTarget = homeURLTarget;
            outDirName = FileUtil.fileDirName(outFileName);
            outBaseName = FileUtil.trimFileExtension(FileUtil.fileBaseName(outFileName));
            outExtension = FileUtil.fileExtension(outFileName);
            if(outExtension != null && outExtension.length() == 0)
                outExtension = null;
            Driver driver = getDriver();
            encoding = driver.getEncoding();
            numberSections = driver.isSectionsNumbered();
            userMeta = driver.getAllMeta();
        }
    }


    public HTMLConverter(Driver driver)
    {
        super(driver);
    }

    public String getConverterInfo()
    {
        StringBuffer info = new StringBuffer();
        info.append("Supported option values:\n");
        info.append("  -lang");
        for(int i = 0; i < knownLanguageList.length; i += 3)
        {
            if(i % 10 == 0 && i > 0)
                info.append("\n        ");
            else
                info.append(' ');
            info.append(knownLanguageList[i]);
        }

        info.append('\n');
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
        info.append("  * html.xml=yes|no. Default: no.\n");
        info.append("    Generate XHTML rather than HTML if xml=yes.\n");
        info.append("  * html.systemId=<URL>|<file>.\n");
        info.append("    Mandatory in XML for generating valid documents.\n");
        info.append("    Default: none in HTML mode;\n");
        info.append("    http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\n");
        info.append("    or\n");
        info.append("    http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\n");
        info.append("    in XML mode.\n");
        info.append("  * html.strict=yes|no. Default: no, unless xml=yes.\n");
        info.append("    Use CSS2 styles rather than deprecated HTML4\n");
        info.append("    tags or attributes if strict=yes.\n");
        info.append("  * html.paging=<level>.\n");
        info.append("    Create an HTML page for each section whose\n");
        info.append("    level is <level> (1 to 5, 0 means no paging).\n");
        info.append("    When paging is enabled, a navigation toolbar is\n");
        info.append("    automatically added at the top of generated\n");
        info.append("    HTML pages.\n");
        info.append("    Default: 0 (single-page document).\n");
        info.append("  * html.css=<file name>.\n");
        info.append("    Copy file named <file name> to ouput directory.\n");
        info.append("    Add a corresponding style sheet link to\n");
        info.append("    generated HTML pages.\n");
        info.append("    Default: none (no style sheet).\n");
        info.append("  * html.homeURL=<URL>.\n");
        info.append("    Add a home icon to navigation toolbar pointing\n");
        info.append("    to URL <URL>.\n");
        info.append("  * html.homeURLTarget=<window name>|\n");
        info.append("                       _blank|_parent|_self|_top.\n");
        info.append("    Specifies a window in which to show the homeURL\n");
        info.append("    document.\n");
        info.append("    Default: none (which implies _self).\n\n");
        info.append("Required image format is GIF, JPEG or PNG.\n");
        return info.toString();
    }

    protected Sink createSink(String outFileName)
        throws Exception
    {
        Driver driver = getDriver();
        String xml = driver.getPI("html", "xml");
        String strict = driver.getPI("html", "strict");
        boolean xmlMode;
        if(xml == null)
        {
            String extension = FileUtil.fileExtension(outFileName).toLowerCase();
            xmlMode = extension.equals("xhtml") || extension.equals("xhtm");
        } else
        {
            xmlMode = xml.length() > 0 && xml.charAt(0) == 'y';
        }
        boolean strictMode;
        if(strict == null)
            strictMode = xmlMode;
        else
            strictMode = strict.length() > 0 && strict.charAt(0) == 'y';
        String systemId = driver.getPI("html", "systemId");
        if(systemId != null)
        {
            systemId = systemId.trim();
            if(systemId.length() == 0)
                systemId = null;
        }
        String pi = driver.getPI("html", "paging");
        int paging = -1;
        if(pi == null)
        {
            paging = 0;
        } else
        {
            try
            {
                paging = Integer.parseInt(pi);
            }
            catch(NumberFormatException numberformatexception)
            {
                paging = -1;
            }
            if(paging < 0)
                paging = 0;
            else
            if(paging > 5)
                paging = 5;
        }
        String css = driver.getPI("html", "css");
        if(css != null)
        {
            css = css.trim();
            if(css.length() == 0)
                css = null;
        }
        if(css != null && (new File(css)).isFile())
        {
            String baseName = FileUtil.fileBaseName(css);
            try
            {
                String dst = FileUtil.fileDirName(outFileName) + File.separatorChar + baseName;
                FileUtil.copyFile(css, dst);
            }
            catch(Exception exception)
            {
                css = null;
            }
            css = baseName;
        }
        String homeURL = driver.getPI("html", "homeURL");
        if(homeURL != null)
        {
            homeURL = homeURL.trim();
            if(homeURL.length() == 0)
                homeURL = null;
        }
        String homeURLTarget = driver.getPI("html", "homeURLTarget");
        if(homeURLTarget != null)
        {
            homeURLTarget = homeURLTarget.trim();
            if(homeURLTarget.length() == 0)
                homeURLTarget = null;
        }
        String language = driver.getLanguage();
        String index = (String)knownLanguages.get(language + ".index");
        if(index == null)
            throw new RuntimeException("unsupported language '" + language + "'");
        String encoding = driver.getEncoding();
        String charset = (String)knownEncodings.get(encoding);
        if(charset == null)
            throw new RuntimeException("unsupported encoding '" + encoding + "'");
        else
            return new ConvertSink(outFileName, charset, language, xmlMode, strictMode, systemId, paging, css, homeURL, homeURLTarget);
    }

    protected Sink createSink(Writer out)
        throws Exception
    {
        return null;
    }

    protected void destroySink(Sink sink1)
    {
    }

    private static void copyIcon(String iconName, String outFileName)
        throws IOException
    {
        BufferedInputStream in = new BufferedInputStream((fr.pixware.apt.convert.HTMLConverter.class).getResourceAsStream(iconName));
        FileOutputStream out = new FileOutputStream(outFileName);
        byte bytes[] = new byte[8192];
        int i;
        while((i = in.read(bytes)) >= 0) 
            out.write(bytes, 0, i);
        in.close();
        out.flush();
        out.close();
    }

    private static final String knownEncodingList[] = {
        "ISO8859_1", "ISO-8859-1", "ISO8859_2", "ISO-8859-2", "ISO8859_3", "ISO-8859-3", "ISO8859_4", "ISO-8859-4", "ISO8859_5", "ISO-8859-5", 
        "ISO8859_6", "ISO-8859-6", "ISO8859_7", "ISO-8859-7", "ISO8859_8", "ISO-8859-8", "ISO8859_9", "ISO-8859-9", "ISO8859_13", "ISO-8859-13", 
        "ISO8859_15_FDIS", "ISO-8859-15", "Cp1250", "Windows-1250", "Cp1251", "Windows-1251", "Cp1252", "Windows-1252", "Cp1253", "Windows-1253", 
        "MacRoman", "x-mac-roman", "MacTurkish", "x-mac-turkish", "MacCentralEurope", "x-mac-ce", "MacCyrillic", "x-mac-cyrillic", "MacGreek", "x-mac-greek", 
        "MacArabic", "x-mac-arabic", "MacCroatian", "x-mac-croatian", "MacHebrew", "x-mac-hebrew", "MacIceland", "x-mac-iceland", "MacRomania", "x-mac-romania", 
        "MacThai", "x-mac-thai", "MacUkraine", "x-mac-ukraine", "SJIS", "Shift_JIS", "UTF8", "UTF-8","UTF-8", "UTF-8", "ASCII", "US-ASCII"
    };
    private static final String knownLanguageList[] = {
        "en", "Contents", "Index", "es", "\315ndice General", "\315ndice de Materias", "de", "Inhaltsverzeichnis", "Index", "fr", 
        "Table des Mati\350res", "Index", "it", "Indice", "Indice analitico"
    };
    private static Hashtable knownLanguages;
    private static Hashtable knownEncodings;
    private static final String supportedGraphics[] = {
        "gif", "jpeg", "jpg", "png"
    };
    private static final int TOOLBAR_ICON_WIDTH = 30;
    private static final int TOOLBAR_ICON_HEIGHT = 20;
    private static final String toolbarIcons[] = {
        "apt_home.gif", "apt_next.gif", "apt_prev.gif", "apt_up.gif", "apt_first.gif", "apt_last.gif", "apt_nonext.gif", "apt_noprev.gif", "apt_noup.gif", "apt_nofirst.gif", 
        "apt_nolast.gif"
    };

    static 
    {
        knownLanguages = new Hashtable();
        knownEncodings = new Hashtable();
        for(int i = 0; i < knownLanguageList.length; i += 3)
        {
            String lang = knownLanguageList[i];
            knownLanguages.put(lang + ".toc", knownLanguageList[i + 1]);
            knownLanguages.put(lang + ".index", knownLanguageList[i + 2]);
        }

        HashtableUtil.putAll(knownEncodings, knownEncodingList);
    }




}

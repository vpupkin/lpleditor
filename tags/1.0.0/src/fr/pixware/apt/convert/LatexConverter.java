// 
// 
// 
// Source File Name:   LatexConverter.java

package fr.pixware.apt.convert;

import fr.pixware.apt.parse.*;
import fr.pixware.util.HashtableUtil;
import java.io.Writer;
import java.util.*;

// Referenced classes of package fr.pixware.apt.convert:
//            TwoPassConverter, OnePassConverter, Driver, LatexSink, 
//            Structure

public class LatexConverter extends TwoPassConverter
{
    private class ConvertSink extends LatexSink
    {

        public void setFancyHdr(boolean fancyhdr)
        {
            this.fancyhdr = fancyhdr;
        }

        public boolean isFancyHdr()
        {
            return fancyhdr;
        }

        public void head()
            throws ParseException
        {
            useAuthorMeta = false;
            super.head();
        }

        public void title()
            throws ParseException
        {
            useAuthorMeta = true;
            super.title();
        }

        public void author()
            throws ParseException
        {
            useAuthorMeta = false;
            super.author();
        }

        public void date()
            throws ParseException
        {
            if(useAuthorMeta)
            {
                useAuthorMeta = false;
                traverseAuthorMeta();
            }
            super.date();
        }

        public void head_()
            throws ParseException
        {
            if(useAuthorMeta)
            {
                useAuthorMeta = false;
                traverseAuthorMeta();
            }
            super.head_();
        }

        private void traverseAuthorMeta()
            throws ParseException
        {
            String author = getDriver().getMeta("author");
            if(author == null)
                return;
            StringTokenizer lines = new StringTokenizer(author, "\n");
            int lineCount = lines.countTokens();
            if(lineCount > 0)
            {
                super.author();
                for(int i = 0; i < lineCount; i++)
                {
                    if(i > 0)
                        super.lineBreak();
                    super.text(lines.nextToken());
                }

                super.author_();
            }
        }

        public void body()
            throws ParseException
        {
            super.body();
            if(fancyhdr)
            {
                String title = getStructure().getTitle();
                markup("\\fancyhf{}\n");
                markup("\\renewcommand{\\footrulewidth}{0pt}\n");
                if(title == null)
                {
                    markup("\\fancyfoot[c]{\\thepage}\n");
                    markup("\\renewcommand{\\headrulewidth}{0pt}\n\n");
                } else
                {
                    markup("\\fancyhead[er,ol]{\\pitalic{" + LatexSink.escaped(title) + "}}\n");
                    markup("\\fancyhead[el,or]{\\pitalic{\\thepage}}\n");
                    markup("\\renewcommand{\\headrulewidth}{0.4pt}\n\n");
                }
            }
            if(getDriver().isTOC())
                markup("\\tableofcontents\n\n");
        }

        public void body_()
            throws ParseException
        {
            if(getDriver().isIndex())
                markup("\\include{index}\n\n");
            super.body_();
        }

        public void figureGraphics(String name)
            throws ParseException
        {
            try
            {
                convertGraphics(name, "eps");
            }
            catch(Exception e)
            {
                throw new ParseException(e);
            }
            super.figureGraphics(name);
        }

        public void anchor(String name)
            throws ParseException
        {
            traversedAnchor = name;
        }

        public void anchor_()
            throws ParseException
        {
            markup("\\panchor{");
            content(traversedAnchor);
            markup("}");
        }

        public void link(String s)
            throws ParseException
        {
        }

        public void link_()
            throws ParseException
        {
        }

        private boolean fancyhdr;
        private boolean useAuthorMeta;
        private String traversedAnchor;

        public ConvertSink(Writer out)
        {
            super(out);
            fancyhdr = false;
        }
    }


    public LatexConverter(Driver driver)
    {
        super(driver);
    }

    public String getConverterInfo()
    {
        StringBuffer info = new StringBuffer();
        info.append("Supported option values:\n");
        info.append("  -paper");
        for(int i = 0; i < knownPaperList.length; i += 2)
        {
            if(i % 10 == 0 && i > 0)
                info.append("\n         ");
            else
                info.append(' ');
            info.append(knownPaperList[i]);
        }

        info.append('\n');
        info.append("  -lang");
        for(int i = 0; i < knownLanguageList.length; i += 2)
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
        info.append("  * latex.documentclass=?[options]?class.\n");
        info.append("    Example: [11pt]report.\n");
        info.append("    Default: [P]article, P specified by -paper.\n");
        info.append("  * latex.usepackage.N=?[options]?package, N=0..10.\n");
        info.append("    Example: [french]babel.\n");
        info.append("    Default packages:\n");
        info.append("      ** a4wide if -paper a4, unless classic=yes.\n");
        info.append("      ** fancyhdr if needed, see pagestyle.\n");
        info.append("      ** [L]babel, L specified by -lang.\n");
        info.append("      ** graphicx.\n");
        info.append("      ** ifthen.\n");
        info.append("      ** times, unless classic=yes.\n");
        info.append("      ** [T1]fontenc.\n");
        info.append("      ** [E]inputenc, E specified by -enc.\n");
        info.append("  * latex.pagestyle=style.\n");
        info.append("    Default: plain if classic=yes, otherwise\n");
        info.append("    a custom style using the fancyhdr package.\n");
        info.append("  * latex.hyphenation.N=word ... word, N=0..10.\n");
        info.append("    Example: gno-mon gno-mons gno-mon-ly.\n");
        info.append("    Default: TeX hyphenation rules.\n");
        info.append("  * latex.resizegraphics=yes|no. Default: no.\n");
        info.append("  * latex.classic=yes|no. Default: no.\n\n");
        info.append("If a PI is used (ex. usepackage.3=[french]babel),\n");
        info.append("the corresponding standard option (ex. -lang en)\n");
        info.append("is ignored.\n\n");
        info.append("Required image format is EPS.\n");
        return info.toString();
    }

    protected Sink createSink(Writer out)
        throws Exception
    {
        ConvertSink sink = new ConvertSink(out);
        Driver driver = getDriver();
        StringBuffer preamble = new StringBuffer();
        String opt = driver.getPI("latex", "classic");
        boolean classic = opt != null && opt.length() > 0 && opt.charAt(0) == 'y';
        String documentClass[] = parsePI("documentclass");
        if(documentClass == null)
            documentClass = (new String[] {
                "article"
            });
        if(documentClass[0].equals("article"))
        {
            if(driver.isSectionsNumbered() || driver.isTOC())
                preamble.append("\\batchmode\n\\newcommand{\\psectioni}[1]{\\section{#1}}\n\\newcommand{\\psectionii}[1]{\\subsection{#1}}\n\\newcommand{\\psectioniii}[1]{\\subsubsection{#1}}\n\\newcommand{\\psectioniv}[1]{\\paragraph{#1}}\n\\newcommand{\\psectionv}[1]{\\subparagraph{#1}}\n");
            else
                preamble.append("\\batchmode\n\\newcommand{\\psectioni}[1]{\\section*{#1}}\n\\newcommand{\\psectionii}[1]{\\subsection*{#1}}\n\\newcommand{\\psectioniii}[1]{\\subsubsection*{#1}}\n\\newcommand{\\psectioniv}[1]{\\paragraph*{#1}}\n\\newcommand{\\psectionv}[1]{\\subparagraph*{#1}}\n");
        } else
        if(driver.isSectionsNumbered() || driver.isTOC())
            preamble.append("\\batchmode\n\\newcommand{\\psectioni}[1]{\\chapter{#1}}\n\\newcommand{\\psectionii}[1]{\\section{#1}}\n\\newcommand{\\psectioniii}[1]{\\subsection{#1}}\n\\newcommand{\\psectioniv}[1]{\\subsubsection{#1}}\n\\newcommand{\\psectionv}[1]{\\paragraph{#1}}\n");
        else
            preamble.append("\\batchmode\n\\newcommand{\\psectioni}[1]{\\chapter*{#1}}\n\\newcommand{\\psectionii}[1]{\\section*{#1}}\n\\newcommand{\\psectioniii}[1]{\\subsection*{#1}}\n\\newcommand{\\psectioniv}[1]{\\subsubsection*{#1}}\n\\newcommand{\\psectionv}[1]{\\paragraph*{#1}}\n");
        preamble.append("\\newcommand{\\ptitle}[1]{\\title{#1}}\n\\newcommand{\\pauthor}[1]{\\author{#1}}\n\\newcommand{\\pdate}[1]{\\date{#1}}\n\\newcommand{\\pmaketitle}{\\maketitle}\n\\newenvironment{plist}{\\begin{itemize}}{\\end{itemize}}\n\\newenvironment{pnumberedlist}{\\begin{enumerate}}{\\end{enumerate}}\n\\newcommand{\\pdef}[1]{\\textbf{#1}\\hfill}\n\\newenvironment{pdefinitionlist}\n{\\begin{list}{}{\\settowidth{\\labelwidth}{\\textbf{999.}}\n                \\setlength{\\leftmargin}{\\labelwidth}\n                \\addtolength{\\leftmargin}{\\labelsep}\n                \\renewcommand{\\makelabel}{\\pdef}}}\n{\\end{list}}\n\\newenvironment{pfigure}{\\begin{center}}{\\end{center}}\n\\newcommand{\\pfigurecaption}[1]{\\\\* \\vspace{\\pparskipamount}\n                                \\textit{#1}}\n\\newenvironment{ptable}{\\begin{center}}{\\end{center}}\n\\newenvironment{ptablerows}[1]{\\begin{tabular}{#1}}{\\end{tabular}}\n\\newenvironment{pcell}[1]{\\begin{tabular}[t]{#1}}{\\end{tabular}}\n\\newcommand{\\ptablecaption}[1]{\\\\* \\vspace{\\pparskipamount}\n                               \\textit{#1}}\n\\newenvironment{pverbatim}{\\begin{small}}{\\end{small}}\n\\newsavebox{\\pbox}\n\\newenvironment{pverbatimbox}\n{\\begin{lrbox}{\\pbox}\\begin{minipage}{\\linewidth}\\begin{small}}\n{\\end{small}\\end{minipage}\\end{lrbox}\\fbox{\\usebox{\\pbox}}}\n\\newcommand{\\phorizontalrule}{\\begin{center}\n                              \\rule[0.5ex]{\\linewidth}{1pt}\n                              \\end{center}}\n\\newcommand{\\panchor}[1]{\\index{#1}}\n\\newcommand{\\plink}[1]{#1}\n\\newcommand{\\pitalic}[1]{\\textit{#1}}\n\\newcommand{\\pbold}[1]{\\textbf{#1}}\n\\newcommand{\\pmonospaced}[1]{\\texttt{\\small #1}}\n");
        opt = driver.getPI("latex", "resizegraphics");
        if(opt != null && opt.charAt(0) == 'y')
            preamble.append("\\newlength{\\pboxw}\n\\newlength{\\pboxh}\n\\newlength{\\pboxd}\n\\newcommand{\\pfiguregraphics}[1]{\n\\sbox{\\pbox}{\\includegraphics{#1.eps}}\n\\settowidth{\\pboxw}{\\usebox{\\pbox}}\n\\settoheight{\\pboxh}{\\usebox{\\pbox}}\n\\settodepth{\\pboxd}{\\usebox{\\pbox}}\n\\addtolength{\\pboxh}{\\pboxd}\n\\ifthenelse{\\lengthtest{\\pboxw>\\textwidth}}{\n  \\ifthenelse{\\lengthtest{\\pboxh>0.8\\textheight}}{\n    \\includegraphics[keepaspectratio,width=\\textwidth,totalheight=0.8\\textheight]{#1.eps}\n  }{\n    \\resizebox*{\\textwidth}{!}{\\usebox{\\pbox}}\n  }\n}{\n  \\ifthenelse{\\lengthtest{\\pboxh>0.8\\textheight}}{\n    \\resizebox*{!}{0.8\\textheight}{\\usebox{\\pbox}}\n  }{\n    \\usebox{\\pbox}\n  }\n}\n}\n\n");
        else
            preamble.append("\\newcommand{\\pfiguregraphics}[1]{\\includegraphics{#1.eps}}\n\n");
        if(driver.isIndex())
            preamble.append("\\makeindex\n\n");
        String paper = driver.getPaper();
        boolean xxpaper = false;
        boolean hasOpt = false;
        preamble.append("\\documentclass");
        for(int i = 1; i < documentClass.length; i++)
        {
            if(hasOpt)
                preamble.append(',');
            else
                preamble.append('[');
            preamble.append(documentClass[i]);
            hasOpt = true;
            if(documentClass[i].indexOf("paper") > 0)
                xxpaper = true;
        }

        if(!xxpaper)
        {
            opt = (String)knownPapers.get(paper);
            if(opt == null)
                throw new RuntimeException("unsupported paper size '" + paper + "'");
            if(hasOpt)
                preamble.append(',');
            else
                preamble.append('[');
            preamble.append(opt);
            hasOpt = true;
        }
        if(hasOpt)
            preamble.append(']');
        preamble.append('{');
        preamble.append(documentClass[0]);
        preamble.append("}\n");
        boolean a4wide = false;
        boolean babel = false;
        boolean graphics = false;
        boolean ifthen = false;
        boolean fancyhdr = false;
        boolean psfont = false;
        boolean fontenc = false;
        boolean inputenc = false;
        for(int j = 0; j <= 10; j++)
        {
            String usePackage[] = parsePI("usepackage." + j);
            if(usePackage != null)
            {
                if(usePackage[0].equals("a4wide"))
                    a4wide = true;
                else
                if(usePackage[0].equals("babel"))
                    babel = true;
                else
                if(usePackage[0].equals("graphics") || usePackage[0].equals("graphicx"))
                    graphics = true;
                else
                if(usePackage[0].equals("ifthen"))
                    ifthen = true;
                else
                if(usePackage[0].equals("fancyhdr"))
                    fancyhdr = true;
                else
                if(usePackage[0].equals("fontenc"))
                    fontenc = true;
                else
                if(usePackage[0].equals("inputenc"))
                    inputenc = true;
                else
                if(knownFonts.get(usePackage[0]) != null)
                    psfont = true;
                hasOpt = false;
                preamble.append("\\usepackage");
                for(int i = 1; i < usePackage.length; i++)
                {
                    if(hasOpt)
                        preamble.append(',');
                    else
                        preamble.append('[');
                    preamble.append(usePackage[i]);
                    hasOpt = true;
                }

                if(hasOpt)
                    preamble.append(']');
                preamble.append('{');
                preamble.append(usePackage[0]);
                preamble.append("}\n");
            }
        }

        if(!a4wide && !xxpaper && paper.equals("a4") && !classic)
            preamble.append("\\usepackage{a4wide}\n");
        if(!babel)
        {
            String language = driver.getLanguage();
            opt = (String)knownLanguages.get(language);
            if(opt == null)
                throw new RuntimeException("unsupported language '" + language + "'");
            preamble.append("\\usepackage[");
            preamble.append(opt);
            preamble.append("]{babel}\n");
        }
        if(!graphics)
            preamble.append("\\usepackage{graphicx}\n");
        if(!ifthen)
            preamble.append("\\usepackage{ifthen}\n");
        if(!psfont && !classic)
            preamble.append("\\usepackage{times}\n");
        if(!fontenc)
            preamble.append("\\usepackage[T1]{fontenc}\n");
        if(!inputenc)
        {
            String encoding = driver.getEncoding();
            opt = (String)knownEncodings.get(encoding);
            if(opt == null)
                throw new RuntimeException("unsupported encoding '" + encoding + "'");
            preamble.append("\\usepackage[");
            preamble.append(opt);
            preamble.append("]{inputenc}\n");
        }
        preamble.append('\n');
        boolean hyphenation = false;
        for(int j = 0; j <= 10; j++)
        {
            opt = driver.getPI("latex", "hyphenation." + j);
            if(opt != null)
            {
                preamble.append("\\hyphenation{");
                preamble.append(opt);
                preamble.append("}\n");
                hyphenation = true;
            }
        }

        if(hyphenation)
            preamble.append('\n');
        opt = driver.getPI("latex", "pagestyle");
        if(opt == null)
            if(fancyhdr)
                opt = "fancy";
            else
            if(classic)
                opt = "plain";
        if(opt == null)
        {
            preamble.append("\\usepackage{fancyhdr}\n");
            preamble.append("\\pagestyle{fancy}\n\n");
            sink.setFancyHdr(true);
        } else
        {
            preamble.append("\\pagestyle{");
            preamble.append(opt);
            preamble.append("}\n\n");
        }
        preamble.append("\\newlength{\\pparskipamount}\n\\setlength{\\pparskipamount}{1ex}\n\n");
        if(!classic)
            preamble.append("\\setlength{\\parindent}{0pt}\n\\setlength{\\parskip}{\\pparskipamount}\n\n");
        sink.setPreamble(preamble.toString());
        return sink;
    }

    private String[] parsePI(String key)
    {
        String pi = getDriver().getPI("latex", key);
        if(pi == null)
            return null;
        Vector strings = new Vector();
        int i = pi.indexOf('[');
        int j = pi.indexOf(']');
        if(i < 0 && j < 0)
            strings.addElement(pi);
        else
        if(i == 0 && j > 0 && j + 1 < pi.length())
        {
            strings.addElement(pi.substring(j + 1));
            String options = pi.substring(1, j);
            if(options.length() > 0)
            {
                for(StringTokenizer tokens = new StringTokenizer(options); tokens.hasMoreTokens(); strings.addElement(tokens.nextToken()));
            }
        }
        if(strings.size() == 0)
        {
            return null;
        } else
        {
            String stringList[] = new String[strings.size()];
            strings.copyInto(stringList);
            return stringList;
        }
    }

    private static final String preamble0 = "\\batchmode\n\\newcommand{\\psectioni}[1]{\\section{#1}}\n\\newcommand{\\psectionii}[1]{\\subsection{#1}}\n\\newcommand{\\psectioniii}[1]{\\subsubsection{#1}}\n\\newcommand{\\psectioniv}[1]{\\paragraph{#1}}\n\\newcommand{\\psectionv}[1]{\\subparagraph{#1}}\n";
    private static final String preamble0nn = "\\batchmode\n\\newcommand{\\psectioni}[1]{\\section*{#1}}\n\\newcommand{\\psectionii}[1]{\\subsection*{#1}}\n\\newcommand{\\psectioniii}[1]{\\subsubsection*{#1}}\n\\newcommand{\\psectioniv}[1]{\\paragraph*{#1}}\n\\newcommand{\\psectionv}[1]{\\subparagraph*{#1}}\n";
    private static final String preamble0r = "\\batchmode\n\\newcommand{\\psectioni}[1]{\\chapter{#1}}\n\\newcommand{\\psectionii}[1]{\\section{#1}}\n\\newcommand{\\psectioniii}[1]{\\subsection{#1}}\n\\newcommand{\\psectioniv}[1]{\\subsubsection{#1}}\n\\newcommand{\\psectionv}[1]{\\paragraph{#1}}\n";
    private static final String preamble0rnn = "\\batchmode\n\\newcommand{\\psectioni}[1]{\\chapter*{#1}}\n\\newcommand{\\psectionii}[1]{\\section*{#1}}\n\\newcommand{\\psectioniii}[1]{\\subsection*{#1}}\n\\newcommand{\\psectioniv}[1]{\\subsubsection*{#1}}\n\\newcommand{\\psectionv}[1]{\\paragraph*{#1}}\n";
    private static final String preamble1 = "\\newcommand{\\ptitle}[1]{\\title{#1}}\n\\newcommand{\\pauthor}[1]{\\author{#1}}\n\\newcommand{\\pdate}[1]{\\date{#1}}\n\\newcommand{\\pmaketitle}{\\maketitle}\n\\newenvironment{plist}{\\begin{itemize}}{\\end{itemize}}\n\\newenvironment{pnumberedlist}{\\begin{enumerate}}{\\end{enumerate}}\n\\newcommand{\\pdef}[1]{\\textbf{#1}\\hfill}\n\\newenvironment{pdefinitionlist}\n{\\begin{list}{}{\\settowidth{\\labelwidth}{\\textbf{999.}}\n                \\setlength{\\leftmargin}{\\labelwidth}\n                \\addtolength{\\leftmargin}{\\labelsep}\n                \\renewcommand{\\makelabel}{\\pdef}}}\n{\\end{list}}\n\\newenvironment{pfigure}{\\begin{center}}{\\end{center}}\n\\newcommand{\\pfigurecaption}[1]{\\\\* \\vspace{\\pparskipamount}\n                                \\textit{#1}}\n\\newenvironment{ptable}{\\begin{center}}{\\end{center}}\n\\newenvironment{ptablerows}[1]{\\begin{tabular}{#1}}{\\end{tabular}}\n\\newenvironment{pcell}[1]{\\begin{tabular}[t]{#1}}{\\end{tabular}}\n\\newcommand{\\ptablecaption}[1]{\\\\* \\vspace{\\pparskipamount}\n                               \\textit{#1}}\n\\newenvironment{pverbatim}{\\begin{small}}{\\end{small}}\n\\newsavebox{\\pbox}\n\\newenvironment{pverbatimbox}\n{\\begin{lrbox}{\\pbox}\\begin{minipage}{\\linewidth}\\begin{small}}\n{\\end{small}\\end{minipage}\\end{lrbox}\\fbox{\\usebox{\\pbox}}}\n\\newcommand{\\phorizontalrule}{\\begin{center}\n                              \\rule[0.5ex]{\\linewidth}{1pt}\n                              \\end{center}}\n\\newcommand{\\panchor}[1]{\\index{#1}}\n\\newcommand{\\plink}[1]{#1}\n\\newcommand{\\pitalic}[1]{\\textit{#1}}\n\\newcommand{\\pbold}[1]{\\textbf{#1}}\n\\newcommand{\\pmonospaced}[1]{\\texttt{\\small #1}}\n";
    private static final String preamble2 = "\\newcommand{\\pfiguregraphics}[1]{\\includegraphics{#1.eps}}\n\n";
    private static final String preamble2f = "\\newlength{\\pboxw}\n\\newlength{\\pboxh}\n\\newlength{\\pboxd}\n\\newcommand{\\pfiguregraphics}[1]{\n\\sbox{\\pbox}{\\includegraphics{#1.eps}}\n\\settowidth{\\pboxw}{\\usebox{\\pbox}}\n\\settoheight{\\pboxh}{\\usebox{\\pbox}}\n\\settodepth{\\pboxd}{\\usebox{\\pbox}}\n\\addtolength{\\pboxh}{\\pboxd}\n\\ifthenelse{\\lengthtest{\\pboxw>\\textwidth}}{\n  \\ifthenelse{\\lengthtest{\\pboxh>0.8\\textheight}}{\n    \\includegraphics[keepaspectratio,width=\\textwidth,totalheight=0.8\\textheight]{#1.eps}\n  }{\n    \\resizebox*{\\textwidth}{!}{\\usebox{\\pbox}}\n  }\n}{\n  \\ifthenelse{\\lengthtest{\\pboxh>0.8\\textheight}}{\n    \\resizebox*{!}{0.8\\textheight}{\\usebox{\\pbox}}\n  }{\n    \\usebox{\\pbox}\n  }\n}\n}\n\n";
    private static final String preamble3a = "\\newlength{\\pparskipamount}\n\\setlength{\\pparskipamount}{1ex}\n\n";
    private static final String preamble3b = "\\setlength{\\parindent}{0pt}\n\\setlength{\\parskip}{\\pparskipamount}\n\n";
    private static final String knownPaperList[] = {
        "a4", "a4paper", "a5", "a5paper", "b5", "b5paper", "letter", "letterpaper", "legal", "legalpaper", 
        "executive", "executivepaper"
    };
    private static final String knownEncodingList[] = {
        "ASCII", "ascii", "ISO8859_1", "latin1", "ISO8859_2", "latin2", "ISO8859_3", "latin3", "ISO8859_4", "latin4", 
        "ISO8859_5", "latin5", "Cp1250", "cp1250", "Cp1252", "cp1252", "Cp437", "cp437", "Cp850", "cp850", 
        "Cp852", "cp852", "Cp865", "cp865", "MacRoman", "applemac"
    };
    private static final String knownLanguageList[] = {
        "br", "breton", "ca", "catalan", "hr", "croatian", "cs", "czech", "da", "danish", 
        "nl", "dutch", "en", "english", "eo", "esperanto", "et", "estonian", "fi", "finnish", 
        "fr", "french", "gl", "galician", "de", "german", "el", "greek", "he", "hebrew", 
        "hu", "hungarian", "ga", "irish", "it", "italian", "no", "norsk", "pl", "polish", 
        "pt", "portuguese", "ro", "romanian", "ru", "russian", "gd", "scottish", "sk", "slovak", 
        "sl", "slovene", "es", "spanish", "sv", "swedish", "tr", "turkish", "cy", "welsh"
    };
    private static final String knownFontList[] = {
        "times", "ptm", "palatino", "ppl", "newcent", "pnc", "bookman", "pbk"
    };
    private static Hashtable knownPapers;
    private static Hashtable knownLanguages;
    private static Hashtable knownFonts;
    private static Hashtable knownEncodings;

    static 
    {
        knownPapers = new Hashtable();
        knownLanguages = new Hashtable();
        knownFonts = new Hashtable();
        knownEncodings = new Hashtable();
        HashtableUtil.putAll(knownPapers, knownPaperList);
        HashtableUtil.putAll(knownLanguages, knownLanguageList);
        HashtableUtil.putAll(knownFonts, knownFontList);
        HashtableUtil.putAll(knownEncodings, knownEncodingList);
    }
}

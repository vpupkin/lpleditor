// 
// 
// 
// Source File Name:   LatexSink.java

package fr.pixware.apt.convert;

import fr.pixware.apt.parse.*;
import java.io.*;

// Referenced classes of package fr.pixware.apt.convert:
//            LineBreaker

public class LatexSink extends SinkAdapter
{

    public LatexSink(Writer out)
    {
        this(out, "\\newcommand{\\ptitle}[1]{\\title{#1}}\n\\newcommand{\\pauthor}[1]{\\author{#1}}\n\\newcommand{\\pdate}[1]{\\date{#1}}\n\\newcommand{\\pmaketitle}{\\maketitle}\n\\newcommand{\\psectioni}[1]{\\section{#1}}\n\\newcommand{\\psectionii}[1]{\\subsection{#1}}\n\\newcommand{\\psectioniii}[1]{\\subsubsection{#1}}\n\\newcommand{\\psectioniv}[1]{\\paragraph{#1}}\n\\newcommand{\\psectionv}[1]{\\subparagraph{#1}}\n\\newenvironment{plist}{\\begin{itemize}}{\\end{itemize}}\n\\newenvironment{pnumberedlist}{\\begin{enumerate}}{\\end{enumerate}}\n\\newcommand{\\pdef}[1]{\\textbf{#1}\\hfill}\n\\newenvironment{pdefinitionlist}\n{\\begin{list}{}{\\settowidth{\\labelwidth}{\\textbf{999.}}\n                \\setlength{\\leftmargin}{\\labelwidth}\n                \\addtolength{\\leftmargin}{\\labelsep}\n                \\renewcommand{\\makelabel}{\\pdef}}}\n{\\end{list}}\n\\newenvironment{pfigure}{\\begin{center}}{\\end{center}}\n\\newcommand{\\pfiguregraphics}[1]{\\includegraphics{#1.eps}}\n\\newcommand{\\pfigurecaption}[1]{\\\\ \\vspace{\\pparskipamount}\n                                \\textit{#1}}\n\\newenvironment{ptable}{\\begin{center}}{\\end{center}}\n\\newenvironment{ptablerows}[1]{\\begin{tabular}{#1}}{\\end{tabular}}\n\\newenvironment{pcell}[1]{\\begin{tabular}[t]{#1}}{\\end{tabular}}\n\\newcommand{\\ptablecaption}[1]{\\\\ \\vspace{\\pparskipamount}\n                               \\textit{#1}}\n\\newenvironment{pverbatim}{\\begin{small}}{\\end{small}}\n\\newsavebox{\\pbox}\n\\newenvironment{pverbatimbox}\n{\\begin{lrbox}{\\pbox}\\begin{minipage}{\\linewidth}\\begin{small}}\n{\\end{small}\\end{minipage}\\end{lrbox}\\fbox{\\usebox{\\pbox}}}\n\\newcommand{\\phorizontalrule}{\\begin{center}\n                              \\rule[0.5ex]{\\linewidth}{1pt}\n                              \\end{center}}\n\\newcommand{\\panchor}[1]{\\textcolor{panchorcolor}{#1}}\n\\newcommand{\\plink}[1]{\\textcolor{plinkcolor}{#1}}\n\\newcommand{\\pitalic}[1]{\\textit{#1}}\n\\newcommand{\\pbold}[1]{\\textbf{#1}}\n\\newcommand{\\pmonospaced}[1]{\\texttt{\\small #1}}\n\n\\documentclass[a4paper]{article}\n\\usepackage{a4wide}\n\\usepackage{color}\n\\usepackage{graphics}\n\\usepackage{times}\n\\usepackage[latin1]{inputenc}\n\\usepackage[T1]{fontenc}\n\n\\pagestyle{plain}\n\n\\definecolor{plinkcolor}{rgb}{0,0,0.54}\n\\definecolor{panchorcolor}{rgb}{0.54,0,0}\n\n\\newlength{\\pparskipamount}\n\\setlength{\\pparskipamount}{1ex}\n\\setlength{\\parindent}{0pt}\n\\setlength{\\parskip}{\\pparskipamount}\n\n");
    }

    public LatexSink(Writer out, String preamble)
    {
        this.out = new LineBreaker(out);
        setPreamble(preamble);
    }

    public void setPreamble(String preamble)
    {
        this.preamble = preamble;
    }

    public String getPreamble()
    {
        return preamble;
    }

    public void head()
        throws ParseException
    {
        titleFlag = false;
        numberedListNesting = 0;
        verbatimFlag = false;
        boxFlag = false;
        figureFlag = false;
        tableFlag = false;
        gridFlag = false;
        cellJustif = null;
        cellCount = 0;
        markup(preamble);
        markup("\\begin{document}\n\n");
    }

    public void body()
        throws ParseException
    {
        if(titleFlag)
        {
            titleFlag = false;
            markup("\\pmaketitle\n\n");
        }
    }

    public void body_()
        throws ParseException
    {
        markup("\\end{document}\n\n");
        out.flush();
    }

    public void section1()
        throws ParseException
    {
        markup("\\psectioni{");
    }

    public void section2()
        throws ParseException
    {
        markup("\\psectionii{");
    }

    public void section3()
        throws ParseException
    {
        markup("\\psectioniii{");
    }

    public void section4()
        throws ParseException
    {
        markup("\\psectioniv{");
    }

    public void section5()
        throws ParseException
    {
        markup("\\psectionv{");
    }

    public void list()
        throws ParseException
    {
        markup("\\begin{plist}\n\n");
    }

    public void list_()
        throws ParseException
    {
        markup("\\end{plist}\n\n");
    }

    public void listItem()
        throws ParseException
    {
        markup("\\item{} ");
    }

    public void numberedList(int numbering)
        throws ParseException
    {
        numberedListNesting++;
        String counter;
        switch(numberedListNesting)
        {
        case 1: // '\001'
            counter = "enumi";
            break;

        case 2: // '\002'
            counter = "enumii";
            break;

        case 3: // '\003'
            counter = "enumiii";
            break;

        case 4: // '\004'
        default:
            counter = "enumiv";
            break;
        }
        String style;
        switch(numbering)
        {
        case 2: // '\002'
            style = "Alph";
            break;

        case 1: // '\001'
            style = "alph";
            break;

        case 4: // '\004'
            style = "Roman";
            break;

        case 3: // '\003'
            style = "roman";
            break;

        case 0: // '\0'
        default:
            style = "arabic";
            break;
        }
        markup("\\begin{pnumberedlist}\n");
        markup("\\renewcommand{\\the" + counter + "}{\\" + style + "{" + counter + "}}\n\n");
    }

    public void numberedList_()
        throws ParseException
    {
        markup("\\end{pnumberedlist}\n\n");
        numberedListNesting--;
    }

    public void numberedListItem()
        throws ParseException
    {
        markup("\\item{} ");
    }

    public void definitionList()
        throws ParseException
    {
        markup("\\begin{pdefinitionlist}\n\n");
    }

    public void definitionList_()
        throws ParseException
    {
        markup("\\end{pdefinitionlist}\n\n");
    }

    public void figure()
        throws ParseException
    {
        figureFlag = true;
        markup("\\begin{pfigure}\n");
    }

    public void figure_()
        throws ParseException
    {
        markup("\\end{pfigure}\n\n");
        figureFlag = false;
    }

    public void table()
        throws ParseException
    {
        tableFlag = true;
        markup("\\begin{ptable}\n");
    }

    public void table_()
        throws ParseException
    {
        markup("\\end{ptable}\n\n");
        tableFlag = false;
    }

    public void tableRows(int justification[], boolean grid)
        throws ParseException
    {
        StringBuffer justif = new StringBuffer();
        for(int i = 0; i < justification.length; i++)
        {
            if(grid)
                justif.append('|');
            switch(justification[i])
            {
            case 0: // '\0'
                justif.append('c');
                break;

            case 1: // '\001'
                justif.append('l');
                break;

            case 2: // '\002'
                justif.append('r');
                break;
            }
        }

        if(grid)
            justif.append('|');
        markup("\\begin{ptablerows}{" + justif.toString() + "}\n");
        if(grid)
            markup("\\hline\n");
        gridFlag = grid;
        cellJustif = justification;
    }

    public void tableRows_()
        throws ParseException
    {
        markup("\\end{ptablerows}\n");
        gridFlag = false;
        cellJustif = null;
    }

    public void tableRow()
        throws ParseException
    {
        cellCount = 0;
    }

    public void tableRow_()
        throws ParseException
    {
        markup("\\\\\n");
        if(gridFlag)
            markup("\\hline\n");
        cellCount = 0;
    }

    public void title()
        throws ParseException
    {
        titleFlag = true;
        markup("\\ptitle{");
    }

    public void title_()
        throws ParseException
    {
        markup("}\n");
    }

    public void author()
        throws ParseException
    {
        markup("\\pauthor{");
    }

    public void author_()
        throws ParseException
    {
        markup("}\n");
    }

    public void date()
        throws ParseException
    {
        markup("\\pdate{");
    }

    public void date_()
        throws ParseException
    {
        markup("}\n");
    }

    public void sectionTitle_()
        throws ParseException
    {
        markup("}\n\n");
    }

    public void paragraph_()
        throws ParseException
    {
        markup("\n\n");
    }

    public void verbatim(boolean boxed)
        throws ParseException
    {
        if(boxed)
            markup("\\begin{pverbatimbox}\n");
        else
            markup("\\begin{pverbatim}\n");
        markup("\\begin{verbatim}\n");
        verbatimFlag = true;
        boxFlag = boxed;
    }

    public void verbatim_()
        throws ParseException
    {
        markup("\n\\end{verbatim}\n");
        if(boxFlag)
            markup("\\end{pverbatimbox}\n\n");
        else
            markup("\\end{pverbatim}\n\n");
        verbatimFlag = false;
        boxFlag = false;
    }

    public void definedTerm()
        throws ParseException
    {
        markup("\\item[\\mbox{");
    }

    public void definedTerm_()
        throws ParseException
    {
        markup("}] ");
    }

    public void figureCaption()
        throws ParseException
    {
        markup("\\pfigurecaption{");
    }

    public void figureCaption_()
        throws ParseException
    {
        markup("}\n");
    }

    public void tableCell()
        throws ParseException
    {
        if(cellCount > 0)
            markup(" &\n");
        char justif;
        switch(cellJustif[cellCount])
        {
        case 1: // '\001'
            justif = 'l';
            break;

        case 2: // '\002'
            justif = 'r';
            break;

        case 0: // '\0'
        default:
            justif = 'c';
            break;
        }
        markup("\\begin{pcell}{" + justif + "}");
    }

    public void tableCell_()
        throws ParseException
    {
        markup("\\end{pcell}");
        cellCount++;
    }

    public void tableCaption()
        throws ParseException
    {
        markup("\\ptablecaption{");
    }

    public void tableCaption_()
        throws ParseException
    {
        markup("}\n");
    }

    public void figureGraphics(String name)
        throws ParseException
    {
        markup("\\pfiguregraphics{" + name + "}\n");
    }

    public void horizontalRule()
        throws ParseException
    {
        markup("\\phorizontalrule\n\n");
    }

    public void pageBreak()
        throws ParseException
    {
        markup("\\newpage\n\n");
    }

    public void anchor(String name)
        throws ParseException
    {
        markup("\\panchor{");
    }

    public void anchor_()
        throws ParseException
    {
        markup("}");
    }

    public void link(String name)
        throws ParseException
    {
        markup("\\plink{");
    }

    public void link_()
        throws ParseException
    {
        markup("}");
    }

    public void italic()
        throws ParseException
    {
        markup("\\pitalic{");
    }

    public void italic_()
        throws ParseException
    {
        markup("}");
    }

    public void bold()
        throws ParseException
    {
        markup("\\pbold{");
    }

    public void bold_()
        throws ParseException
    {
        markup("}");
    }

    public void monospaced()
        throws ParseException
    {
        markup("\\pmonospaced{");
    }

    public void monospaced_()
        throws ParseException
    {
        markup("}");
    }

    public void lineBreak()
        throws ParseException
    {
        markup(!figureFlag && !tableFlag && !titleFlag ? "\\newline\n" : "\\\\\n");
    }

    public void nonBreakingSpace()
        throws ParseException
    {
        markup("~");
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
        out.write(escaped(text), false);
    }

    protected void verbatimContent(String text)
        throws ParseException
    {
        out.write(text, true);
    }

    protected static String escaped(String text)
    {
        int length = text.length();
        StringBuffer buffer = new StringBuffer(length);
        for(int i = 0; i < length; i++)
        {
            char c = text.charAt(i);
            switch(c)
            {
            case 45: // '-'
            case 60: // '<'
            case 62: // '>'
                buffer.append("\\symbol{" + (int)c + "}");
                break;

            case 126: // '~'
                buffer.append("\\textasciitilde ");
                break;

            case 94: // '^'
                buffer.append("\\textasciicircum ");
                break;

            case 124: // '|'
                buffer.append("\\textbar ");
                break;

            case 92: // '\\'
                buffer.append("\\textbackslash ");
                break;

            case 36: // '$'
                buffer.append("\\$");
                break;

            case 38: // '&'
                buffer.append("\\&");
                break;

            case 37: // '%'
                buffer.append("\\%");
                break;

            case 35: // '#'
                buffer.append("\\#");
                break;

            case 123: // '{'
                buffer.append("\\{");
                break;

            case 125: // '}'
                buffer.append("\\}");
                break;

            case 95: // '_'
                buffer.append("\\_");
                break;

            default:
                buffer.append(c);
                break;
            }
        }

        return buffer.toString();
    }

    public static void main(String args[])
    {
        Parser parser = new Parser();
        ReaderSource source = new ReaderSource(new InputStreamReader(System.in));
        LatexSink sink = new LatexSink(new OutputStreamWriter(System.out));
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

    private LineBreaker out;
    private String preamble;
    private boolean titleFlag;
    private int numberedListNesting;
    private boolean verbatimFlag;
    private boolean boxFlag;
    private boolean figureFlag;
    private boolean tableFlag;
    private boolean gridFlag;
    private int cellJustif[];
    private int cellCount;
    private static final String defaultPreamble = "\\newcommand{\\ptitle}[1]{\\title{#1}}\n\\newcommand{\\pauthor}[1]{\\author{#1}}\n\\newcommand{\\pdate}[1]{\\date{#1}}\n\\newcommand{\\pmaketitle}{\\maketitle}\n\\newcommand{\\psectioni}[1]{\\section{#1}}\n\\newcommand{\\psectionii}[1]{\\subsection{#1}}\n\\newcommand{\\psectioniii}[1]{\\subsubsection{#1}}\n\\newcommand{\\psectioniv}[1]{\\paragraph{#1}}\n\\newcommand{\\psectionv}[1]{\\subparagraph{#1}}\n\\newenvironment{plist}{\\begin{itemize}}{\\end{itemize}}\n\\newenvironment{pnumberedlist}{\\begin{enumerate}}{\\end{enumerate}}\n\\newcommand{\\pdef}[1]{\\textbf{#1}\\hfill}\n\\newenvironment{pdefinitionlist}\n{\\begin{list}{}{\\settowidth{\\labelwidth}{\\textbf{999.}}\n                \\setlength{\\leftmargin}{\\labelwidth}\n                \\addtolength{\\leftmargin}{\\labelsep}\n                \\renewcommand{\\makelabel}{\\pdef}}}\n{\\end{list}}\n\\newenvironment{pfigure}{\\begin{center}}{\\end{center}}\n\\newcommand{\\pfiguregraphics}[1]{\\includegraphics{#1.eps}}\n\\newcommand{\\pfigurecaption}[1]{\\\\ \\vspace{\\pparskipamount}\n                                \\textit{#1}}\n\\newenvironment{ptable}{\\begin{center}}{\\end{center}}\n\\newenvironment{ptablerows}[1]{\\begin{tabular}{#1}}{\\end{tabular}}\n\\newenvironment{pcell}[1]{\\begin{tabular}[t]{#1}}{\\end{tabular}}\n\\newcommand{\\ptablecaption}[1]{\\\\ \\vspace{\\pparskipamount}\n                               \\textit{#1}}\n\\newenvironment{pverbatim}{\\begin{small}}{\\end{small}}\n\\newsavebox{\\pbox}\n\\newenvironment{pverbatimbox}\n{\\begin{lrbox}{\\pbox}\\begin{minipage}{\\linewidth}\\begin{small}}\n{\\end{small}\\end{minipage}\\end{lrbox}\\fbox{\\usebox{\\pbox}}}\n\\newcommand{\\phorizontalrule}{\\begin{center}\n                              \\rule[0.5ex]{\\linewidth}{1pt}\n                              \\end{center}}\n\\newcommand{\\panchor}[1]{\\textcolor{panchorcolor}{#1}}\n\\newcommand{\\plink}[1]{\\textcolor{plinkcolor}{#1}}\n\\newcommand{\\pitalic}[1]{\\textit{#1}}\n\\newcommand{\\pbold}[1]{\\textbf{#1}}\n\\newcommand{\\pmonospaced}[1]{\\texttt{\\small #1}}\n\n\\documentclass[a4paper]{article}\n\\usepackage{a4wide}\n\\usepackage{color}\n\\usepackage{graphics}\n\\usepackage{times}\n\\usepackage[latin1]{inputenc}\n\\usepackage[T1]{fontenc}\n\n\\pagestyle{plain}\n\n\\definecolor{plinkcolor}{rgb}{0,0,0.54}\n\\definecolor{panchorcolor}{rgb}{0.54,0,0}\n\n\\newlength{\\pparskipamount}\n\\setlength{\\pparskipamount}{1ex}\n\\setlength{\\parindent}{0pt}\n\\setlength{\\parskip}{\\pparskipamount}\n\n";
}

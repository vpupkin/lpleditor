// 
// 
// 
// Source File Name:   RTFSink.java

package fr.pixware.apt.convert.rtf;

import fr.pixware.apt.convert.AlphaNumerals;
import fr.pixware.apt.convert.RomanNumerals;
import fr.pixware.apt.parse.*;
import java.awt.Color;
import java.io.*;
import java.util.*;

// Referenced classes of package fr.pixware.apt.convert.rtf:
//            PBMReader, WMFWriter, Font

public class RTFSink extends SinkAdapter
{
    private class Box
    {

        int width;
        int height;

        Box(int width, int height)
        {
            this.width = width;
            this.height = height;
        }
    }

    private class Item
    {

        int style;
        String text;

        Item(int style, String text)
        {
            this.style = style;
            this.text = text;
        }
    }

    private class Line
    {

        void add(Item item)
        {
            items.addElement(item);
        }

        Vector items;

        private Line()
        {
            items = new Vector();
        }

    }

    private class Cell
    {

        void add(Line line)
        {
            lines.addElement(line);
        }

        Box boundingBox()
        {
            int width = 0;
            int height = 0;
            for(int i = 0; i < lines.size(); i++)
            {
                int w = 0;
                int h = 0;
                Line line = (Line)lines.elementAt(i);
                for(int j = 0; j < line.items.size(); j++)
                {
                    Item item = (Item)line.items.elementAt(j);
                    Font font = getFont(item.style, fontSize);
                    if(font != null)
                    {
                        Font.TextExtents x = font.textExtents(item.text);
                        w += x.width;
                        if(x.height > h)
                            h = x.height;
                    }
                }

                if(w > width)
                    width = w;
                height += h;
            }

            width += 120;
            height += 40;
            width += toTwips(1.0D, 4);
            return new Box(width, height);
        }

        Vector lines;

        private Cell()
        {
            lines = new Vector();
        }

    }

    private class Row
    {

        void add(Cell cell)
        {
            cells.addElement(cell);
        }

        int height()
        {
            int height = 0;
            int numCells = cells.size();
            for(int i = 0; i < numCells; i++)
            {
                Cell cell = (Cell)cells.elementAt(i);
                Box box = cell.boundingBox();
                if(box.height > height)
                    height = box.height;
            }

            return height;
        }

        Vector cells;

        private Row()
        {
            cells = new Vector();
        }

    }

    private class Table
    {

        void add(Row row)
        {
            rows.addElement(row);
            for(int i = 0; i < numColumns; i++)
            {
                if(i >= row.cells.size())
                    break;
                Cell cell = (Cell)row.cells.elementAt(i);
                int width = cell.boundingBox().width;
                if(width > columnWidths[i])
                    columnWidths[i] = width;
            }

        }

        int width()
        {
            int width = 0;
            for(int i = 0; i < numColumns; i++)
                width += columnWidths[i];

            if(grid)
                width += (numColumns + 1) * 15;
            return width;
        }

        int numColumns;
        int columnWidths[];
        int justification[];
        boolean grid;
        Vector rows;

        Table(int justification[], boolean grid)
        {
            numColumns = justification.length;
            columnWidths = new int[numColumns];
            this.justification = justification;
            this.grid = grid;
            rows = new Vector();
        }
    }

    private class Indentation
    {

        void set(int indent)
        {
            stack.addElement(new Integer(this.indent));
            this.indent = indent;
        }

        int get()
        {
            return indent;
        }

        void restore()
        {
            if(!stack.isEmpty())
            {
                indent = ((Integer)stack.lastElement()).intValue();
                stack.removeElementAt(stack.size() - 1);
            }
        }

        void add(int indent)
        {
            set(this.indent + indent);
        }

        private int indent;
        private Vector stack;

        Indentation(int indent)
        {
            stack = new Vector();
            this.indent = indent;
        }
    }

    private class Space
    {

        void set(int space)
        {
            stack.addElement(new Integer(this.space));
            this.space = space;
            next = space;
        }

        int get()
        {
            return space;
        }

        void restore()
        {
            if(!stack.isEmpty())
            {
                space = ((Integer)stack.lastElement()).intValue();
                stack.removeElementAt(stack.size() - 1);
                next = space;
            }
        }

        void setNext(int space)
        {
            next = space;
        }

        int getNext()
        {
            int next = this.next;
            this.next = space;
            return next;
        }

        void skip()
        {
            skip(getNext());
        }

        void skip(int space)
        {
            writer.print("\\pard");
            if((space -= 10) > 0)
                writer.print("\\sb" + space);
            writer.println("\\plain\\fs1\\par");
        }

        private int space;
        private int next;
        private Vector stack;

        Space(int space)
        {
            stack = new Vector();
            this.space = space;
            next = space;
        }
    }

    private class Paragraph
    {

        void begin()
        {
            writer.print("\\pard");
            if(style > 0)
                writer.print("\\s" + style);
            switch(justification)
            {
            case 0: // '\0'
                writer.print("\\qc");
                break;

            case 2: // '\002'
                writer.print("\\qr");
                break;
            }
            if(leftIndent != 0)
                writer.print("\\li" + leftIndent);
            if(rightIndent != 0)
                writer.print("\\ri" + rightIndent);
            if(firstLineIndent != 0)
                writer.print("\\fi" + firstLineIndent);
            if(spaceBefore != 0)
                writer.print("\\sb" + spaceBefore);
            if(spaceAfter != 0)
                writer.print("\\sa" + spaceAfter);
            if(frame)
                writer.print("\\box\\brdrs\\brdrw15");
            writer.print("\\plain");
            switch(fontStyle)
            {
            case 0: // '\0'
            default:
                writer.print("\\f0");
                break;

            case 1: // '\001'
                writer.print("\\f0\\i");
                break;

            case 2: // '\002'
                writer.print("\\f0\\b");
                break;

            case 3: // '\003'
                writer.print("\\f1");
                break;
            }
            writer.println("\\fs" + 2 * fontSize);
        }

        void end()
        {
            writer.println("\\par");
        }

        int style;
        int justification;
        int leftIndent;
        int rightIndent;
        int firstLineIndent;
        int spaceBefore;
        int spaceAfter;
        boolean frame;
        int fontStyle;
        int fontSize;

        Paragraph()
        {
            style = 0;
            justification = 1;
            leftIndent = indentation.get();
            rightIndent = 0;
            firstLineIndent = 0;
            spaceBefore = space.getNext();
            spaceAfter = 0;
            frame = false;
            fontStyle = 0;
            fontSize = RTFSink.this.fontSize;
        }

        Paragraph(int style, int size)
        {
            this.style = 0;
            justification = 1;
            leftIndent = indentation.get();
            rightIndent = 0;
            firstLineIndent = 0;
            spaceBefore = space.getNext();
            spaceAfter = 0;
            frame = false;
            fontStyle = 0;
            fontSize = RTFSink.this.fontSize;
            fontStyle = style;
            fontSize = size;
        }
    }

    private class Context
    {

        void set(int context)
        {
            stack.addElement(new Integer(this.context));
            this.context = context;
        }

        void restore()
        {
            if(!stack.isEmpty())
            {
                context = ((Integer)stack.lastElement()).intValue();
                stack.removeElementAt(stack.size() - 1);
            }
        }

        int get()
        {
            return context;
        }

        private int context;
        private Vector stack;

        private Context()
        {
            context = 0;
            stack = new Vector();
        }

    }

    private class Counter
    {

        void set(int value)
        {
            this.value = value;
        }

        int get()
        {
            return value;
        }

        void increment()
        {
            increment(1);
        }

        void increment(int value)
        {
            this.value += value;
        }

        private int value;

        Counter(int value)
        {
            set(value);
        }
    }


    public RTFSink()
        throws IOException
    {
        this(((OutputStream) (System.out)));
    }

    public RTFSink(OutputStream output)
        throws IOException
    {
        this(output, null);
    }

    public RTFSink(OutputStream output, String encoding)
        throws IOException
    {
        paperWidth = 21D;
        paperHeight = 29.699999999999999D;
        topMargin = 2D;
        bottomMargin = 2D;
        leftMargin = 2D;
        rightMargin = 2D;
        fontSize = 10;
        resolution = 72;
        imageFormat = "bmp";
        imageType = "palette";
        imageDataFormat = "ascii";
        imageCompression = true;
        codePage = 1252;
        charSet = 0;
        style = 0;
        fontTable = new Hashtable();
        context = new Context();
        indentation = new Indentation(0);
        space = new Space(200);
        numbering = new Vector();
        itemNumber = new Vector();
        Font font = getFont(2, fontSize);
        if(font != null)
            listItemIndent = textWidth("-  ", font);
        stream = new BufferedOutputStream(output);
        java.io.Writer w;
        if(encoding != null)
            w = new OutputStreamWriter(stream, encoding);
        else
            w = new OutputStreamWriter(stream);
        writer = new PrintWriter(new BufferedWriter(w));
    }

    public void setPaperSize(double width, double height)
    {
        paperWidth = width;
        paperHeight = height;
    }

    public void setTopMargin(double margin)
    {
        topMargin = margin;
    }

    public void setBottomMargin(double margin)
    {
        bottomMargin = margin;
    }

    public void setLeftMargin(double margin)
    {
        leftMargin = margin;
    }

    public void setRightMargin(double margin)
    {
        rightMargin = margin;
    }

    public void setFontSize(int size)
    {
        fontSize = size;
    }

    public void setSpacing(int spacing)
    {
        space.set(20 * spacing);
    }

    public void setResolution(int resolution)
    {
        this.resolution = resolution;
    }

    public void setImageFormat(String format)
    {
        imageFormat = format;
    }

    public void setImageType(String type)
    {
        imageType = type;
    }

    public void setImageDataFormat(String format)
    {
        imageDataFormat = format;
    }

    public void setImageCompression(boolean compression)
    {
        imageCompression = compression;
    }

    public void setCodePage(int cp)
    {
        codePage = cp;
    }

    public void setCharSet(int cs)
    {
        charSet = cs;
    }

    public void head()
        throws ParseException
    {
        writer.println("{\\rtf1\\ansi\\ansicpg" + codePage + "\\deff0");
        writer.println("{\\fonttbl");
        writer.println("{\\f0\\froman\\fcharset" + charSet + " Times;}");
        writer.println("{\\f1\\fmodern\\fcharset" + charSet + " Courier;}");
        writer.println("}");
        writer.println("{\\stylesheet");
        for(int level = 1; level <= 5; level++)
        {
            writer.print("{\\s" + styleNumber(level));
            writer.print("\\outlinelevel" + level);
            writer.print(" Section Title " + level);
            writer.println(";}");
        }

        writer.println("}");
        writer.println("\\paperw" + toTwips(paperWidth, 2));
        writer.println("\\paperh" + toTwips(paperHeight, 2));
        writer.println("\\margl" + toTwips(leftMargin, 2));
        writer.println("\\margr" + toTwips(rightMargin, 2));
        writer.println("\\margt" + toTwips(topMargin, 2));
        writer.println("\\margb" + toTwips(bottomMargin, 2));
        space.set(space.get() / 2);
        space.setNext(0);
        emptyHeader = true;
    }

    public void head_()
        throws ParseException
    {
        space.restore();
        if(emptyHeader)
            space.setNext(0);
        else
            space.setNext(2 * space.get());
    }

    private int toTwips(double length, int unit)
    {
        double points;
        switch(unit)
        {
        case 1: // '\001'
            points = (length / 25.399999999999999D) * 72D;
            break;

        case 2: // '\002'
            points = (length / 2.54D) * 72D;
            break;

        case 3: // '\003'
            points = length * 72D;
            break;

        case 4: // '\004'
        default:
            points = (length / (double)resolution) * 72D;
            break;
        }
        return (int)Math.rint(points * 20D);
    }

    public void title()
        throws ParseException
    {
        Paragraph paragraph = new Paragraph(2, fontSize + 6);
        paragraph.justification = 0;
        beginParagraph(paragraph);
        emptyHeader = false;
    }

    public void title_()
        throws ParseException
    {
        endParagraph();
    }

    public void author()
        throws ParseException
    {
        Paragraph paragraph = new Paragraph(0, fontSize + 2);
        paragraph.justification = 0;
        beginParagraph(paragraph);
        emptyHeader = false;
    }

    public void author_()
        throws ParseException
    {
        endParagraph();
    }

    public void date()
        throws ParseException
    {
        Paragraph paragraph = new Paragraph(0, fontSize);
        paragraph.justification = 0;
        beginParagraph(paragraph);
        emptyHeader = false;
    }

    public void date_()
        throws ParseException
    {
        endParagraph();
    }

    public void body()
        throws ParseException
    {
    }

    public void body_()
        throws ParseException
    {
        writer.println("}");
        writer.flush();
    }

    public void section1()
        throws ParseException
    {
        sectionLevel = 1;
    }

    public void section1_()
        throws ParseException
    {
    }

    public void section2()
        throws ParseException
    {
        sectionLevel = 2;
    }

    public void section2_()
        throws ParseException
    {
    }

    public void section3()
        throws ParseException
    {
        sectionLevel = 3;
    }

    public void section3_()
        throws ParseException
    {
    }

    public void section4()
        throws ParseException
    {
        sectionLevel = 4;
    }

    public void section4_()
        throws ParseException
    {
    }

    public void section5()
        throws ParseException
    {
        sectionLevel = 5;
    }

    public void section5_()
        throws ParseException
    {
    }

    public void sectionTitle()
        throws ParseException
    {
        int style = 2;
        int size = fontSize;
        switch(sectionLevel)
        {
        case 1: // '\001'
            size = fontSize + 6;
            break;

        case 2: // '\002'
            size = fontSize + 4;
            break;

        case 3: // '\003'
            size = fontSize + 2;
            break;

        case 5: // '\005'
            style = 0;
            break;
        }
        Paragraph paragraph = new Paragraph(style, size);
        paragraph.style = styleNumber(sectionLevel);
        beginParagraph(paragraph);
    }

    public void sectionTitle_()
        throws ParseException
    {
        endParagraph();
    }

    private int styleNumber(int level)
    {
        return level;
    }

    public void list()
        throws ParseException
    {
        indentation.add(300);
        space.set(space.get() / 2);
    }

    public void list_()
        throws ParseException
    {
        indentation.restore();
        space.restore();
    }

    public void listItem()
        throws ParseException
    {
        Paragraph paragraph = new Paragraph();
        paragraph.leftIndent = indentation.get() + listItemIndent;
        paragraph.firstLineIndent = -listItemIndent;
        beginParagraph(paragraph);
        beginStyle(2);
        writer.println("-  ");
        endStyle();
        indentation.add(listItemIndent);
        space.set(space.get() / 2);
    }

    public void listItem_()
        throws ParseException
    {
        endParagraph();
        indentation.restore();
        space.restore();
    }

    public void numberedList(int numbering)
        throws ParseException
    {
        this.numbering.addElement(new Integer(numbering));
        itemNumber.addElement(new Counter(0));
        indentation.add(300);
        space.set(space.get() / 2);
    }

    public void numberedList_()
        throws ParseException
    {
        numbering.removeElementAt(numbering.size() - 1);
        itemNumber.removeElementAt(itemNumber.size() - 1);
        indentation.restore();
        space.restore();
    }

    public void numberedListItem()
        throws ParseException
    {
        ((Counter)itemNumber.lastElement()).increment();
        int indent = 0;
        String header = getItemHeader();
        Font font = getFont(3, fontSize);
        if(font != null)
            indent = textWidth(header, font);
        Paragraph paragraph = new Paragraph();
        paragraph.leftIndent = indentation.get() + indent;
        paragraph.firstLineIndent = -indent;
        beginParagraph(paragraph);
        beginStyle(3);
        writer.println(header);
        endStyle();
        indentation.add(indent);
        space.set(space.get() / 2);
    }

    public void numberedListItem_()
        throws ParseException
    {
        endParagraph();
        indentation.restore();
        space.restore();
    }

    private String getItemHeader()
    {
        int numbering = ((Integer)this.numbering.lastElement()).intValue();
        int itemNumber = ((Counter)this.itemNumber.lastElement()).get();
        StringBuffer buf = new StringBuffer();
        switch(numbering)
        {
        case 0: // '\0'
        default:
            buf.append(itemNumber);
            buf.append(". ");
            for(; buf.length() < 4; buf.append(' '));
            break;

        case 1: // '\001'
            buf.append(AlphaNumerals.toString(itemNumber, true));
            buf.append(") ");
            break;

        case 2: // '\002'
            buf.append(AlphaNumerals.toString(itemNumber, false));
            buf.append(". ");
            break;

        case 3: // '\003'
            buf.append(RomanNumerals.toString(itemNumber, true));
            buf.append(") ");
            for(; buf.length() < 6; buf.append(' '));
            break;

        case 4: // '\004'
            buf.append(RomanNumerals.toString(itemNumber, false));
            buf.append(". ");
            for(; buf.length() < 6; buf.append(' '));
            break;
        }
        return buf.toString();
    }

    public void definitionList()
        throws ParseException
    {
        int next = space.getNext();
        indentation.add(300);
        space.set(space.get() / 2);
        space.setNext(next);
    }

    public void definitionList_()
        throws ParseException
    {
        indentation.restore();
        space.restore();
    }

    public void definitionListItem()
        throws ParseException
    {
        int next = space.getNext();
        space.set(space.get() / 2);
        space.setNext(next);
    }

    public void definitionListItem_()
        throws ParseException
    {
        space.restore();
    }

    public void definedTerm()
        throws ParseException
    {
    }

    public void definedTerm_()
        throws ParseException
    {
        endParagraph();
    }

    public void definition()
        throws ParseException
    {
        int next = space.getNext();
        indentation.add(300);
        space.set(space.get() / 2);
        space.setNext(next);
    }

    public void definition_()
        throws ParseException
    {
        endParagraph();
        indentation.restore();
        space.restore();
    }

    public void table()
        throws ParseException
    {
    }

    public void table_()
        throws ParseException
    {
    }

    public void tableRows(int justification[], boolean grid)
        throws ParseException
    {
        table = new Table(justification, grid);
        context.set(2);
    }

    public void tableRows_()
        throws ParseException
    {
        boolean bb = false;
        boolean br = false;
        int offset = (pageWidth() - (table.width() + indentation.get())) / 2;
        int x0 = indentation.get() + offset;
        space.skip();
        for(int i = 0; i < table.rows.size(); i++)
        {
            Row row = (Row)table.rows.elementAt(i);
            writer.print("\\trowd");
            writer.print("\\trleft" + x0);
            writer.print("\\trgaph60");
            writer.println("\\trrh" + row.height());
            if(table.grid)
            {
                if(i == table.rows.size() - 1)
                    bb = true;
                br = false;
            }
            
            int x = x0;
            for(int j = 0; j < table.numColumns; j++)
            {
                if(table.grid)
                {
                    if(j == table.numColumns - 1)
                        br = true;
                    setBorder(true, bb, true, br);
                    x += 15;
                }
                x += table.columnWidths[j];
                writer.println("\\clvertalc\\cellx" + x);
            }

            for(int j = 0; j < table.numColumns; j++)
            {
                if(j >= row.cells.size())
                    break;
                Cell cell = (Cell)row.cells.elementAt(j);
                writer.print("\\pard\\intbl");
                setJustification(table.justification[j]);
                writer.println("\\plain\\f0\\fs" + 2 * fontSize);
                for(int k = 0; k < cell.lines.size(); k++)
                {
                    if(k > 0)
                        writer.println("\\line");
                    Line line = (Line)cell.lines.elementAt(k);
                    for(int n = 0; n < line.items.size(); n++)
                    {
                        Item item = (Item)line.items.elementAt(n);
                        writer.print("{");
                        setStyle(item.style);
                        writer.println(escape(item.text));
                        writer.println("}");
                    }

                }

                writer.println("\\cell");
            }

            writer.println("\\row");
        }

        context.restore();
    }

    private int pageWidth()
    {
        double width = paperWidth - (leftMargin + rightMargin);
        return toTwips(width, 2);
    }

    private void setBorder(boolean bt, boolean bb, boolean bl, boolean br)
    {
        if(bt)
            writer.println("\\clbrdrt\\brdrs\\brdrw15");
        if(bb)
            writer.println("\\clbrdrb\\brdrs\\brdrw15");
        if(bl)
            writer.println("\\clbrdrl\\brdrs\\brdrw15");
        if(br)
            writer.println("\\clbrdrr\\brdrs\\brdrw15");
    }

    private void setJustification(int justification)
    {
        switch(justification)
        {
        case 1: // '\001'
        default:
            writer.println("\\ql");
            break;

        case 0: // '\0'
            writer.println("\\qc");
            break;

        case 2: // '\002'
            writer.println("\\qr");
            break;
        }
    }

    private void setStyle(int style)
    {
        switch(style)
        {
        case 1: // '\001'
            writer.println("\\i");
            break;

        case 2: // '\002'
            writer.println("\\b");
            break;

        case 3: // '\003'
            writer.println("\\f1");
            break;
        }
    }

    public void tableRow()
        throws ParseException
    {
        row = new Row();
    }

    public void tableRow_()
        throws ParseException
    {
        table.add(row);
    }

    public void tableCell()
        throws ParseException
    {
        cell = new Cell();
        line = new Line();
    }

    public void tableCell_()
        throws ParseException
    {
        cell.add(line);
        row.add(cell);
    }

    public void tableCaption()
        throws ParseException
    {
        Paragraph paragraph = new Paragraph();
        paragraph.justification = 0;
        paragraph.spaceBefore /= 2;
        beginParagraph(paragraph);
    }

    public void tableCaption_()
        throws ParseException
    {
        endParagraph();
    }

    public void paragraph()
        throws ParseException
    {
        if(paragraph == null)
            beginParagraph(new Paragraph());
    }

    public void paragraph_()
        throws ParseException
    {
        endParagraph();
    }

    private void beginParagraph(Paragraph paragraph)
    {
        paragraph.begin();
        this.paragraph = paragraph;
        if(style != 0)
            beginStyle(style);
    }

    private void endParagraph()
    {
        if(paragraph != null)
        {
            if(style != 0)
                endStyle();
            paragraph.end();
            paragraph = null;
        }
    }

    public void verbatim(boolean boxed)
        throws ParseException
    {
        verbatim = new StringBuffer();
        frame = boxed;
        context.set(1);
    }

    public void verbatim_()
        throws ParseException
    {
        String text = verbatim.toString();
        Paragraph paragraph = new Paragraph();
        paragraph.fontStyle = 3;
        paragraph.frame = frame;
        beginParagraph(paragraph);
        for(StringTokenizer t = new StringTokenizer(text, "\n", true); t.hasMoreTokens();)
        {
            String s = t.nextToken();
            if(s.equals("\n") && t.hasMoreTokens())
                writer.println("\\line");
            else
                writer.println(escape(s));
        }

        endParagraph();
        context.restore();
    }

    public void figure()
        throws ParseException
    {
    }

    public void figure_()
        throws ParseException
    {
    }

    public void figureGraphics(String name)
        throws ParseException
    {
        StringBuffer buf = new StringBuffer(name);
        buf.append(".ppm");
        name = buf.toString();
        Paragraph paragraph = new Paragraph();
        paragraph.justification = 0;
        beginParagraph(paragraph);
        try
        {
            writeImage(name);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(2);
        }
        endParagraph();
    }

    private void writeImage(String source)
        throws Exception
    {
        PBMReader ppm = new PBMReader(source);
        WMFWriter.Dib dib = new WMFWriter.Dib();
        WMFWriter wmf = new WMFWriter();
        int srcWidth = ppm.width();
        int srcHeight = ppm.height();
        dib.biWidth = srcWidth;
        dib.biHeight = srcHeight;
        dib.biXPelsPerMeter = (int)(((double)resolution * 100D) / 2.54D);
        dib.biYPelsPerMeter = dib.biXPelsPerMeter;
        int bytesPerLine;
        if(imageType.equals("rgb"))
        {
            dib.biBitCount = 24;
            dib.biCompression = 0;
            bytesPerLine = 4 * ((3 * srcWidth + 3) / 4);
            dib.bitmap = new byte[srcHeight * bytesPerLine];
            byte line[] = new byte[3 * srcWidth];
            for(int i = srcHeight - 1; i >= 0; i--)
            {
                ppm.read(line, 0, line.length);
                int j = 0;
                int k = i * bytesPerLine;
                for(; j < line.length; j += 3)
                {
                    dib.bitmap[k++] = line[j + 2];
                    dib.bitmap[k++] = line[j + 1];
                    dib.bitmap[k++] = line[j];
                }

            }

        } else
        {
            dib.biBitCount = 8;
            bytesPerLine = 4 * ((srcWidth + 3) / 4);
            byte bitmap[] = new byte[srcHeight * bytesPerLine];
            Vector colors = new Vector(256);
            colors.addElement(Color.white);
            colors.addElement(Color.black);
            byte line[] = new byte[3 * srcWidth];
            for(int i = srcHeight - 1; i >= 0; i--)
            {
                ppm.read(line, 0, line.length);
                int j = 0;
                int k = i * bytesPerLine;
                while(j < line.length) 
                {
                    int r = line[j++] & 0xff;
                    int g = line[j++] & 0xff;
                    int b = line[j++] & 0xff;
                    Color color = new Color(r, g, b);
                    int index = colors.indexOf(color);
                    if(index < 0)
                        if(colors.size() < colors.capacity())
                        {
                            colors.addElement(color);
                            index = colors.size() - 1;
                        } else
                        {
                            index = 1;
                        }
                    bitmap[k++] = (byte)index;
                }
            }

            dib.biClrUsed = colors.size();
            dib.biClrImportant = dib.biClrUsed;
            dib.palette = new byte[4 * dib.biClrUsed];
            int i = 0;
            for(int j = 0; i < dib.biClrUsed; j++)
            {
                Color color = (Color)colors.elementAt(i);
                dib.palette[j++] = (byte)color.getBlue();
                dib.palette[j++] = (byte)color.getGreen();
                dib.palette[j++] = (byte)color.getRed();
                i++;
            }

            if(imageCompression)
            {
                dib.biCompression = 1;
                dib.bitmap = new byte[bitmap.length + 2 * (bitmap.length / 255 + 1)];
                dib.biSizeImage = WMFWriter.Dib.rlEncode8(bitmap, 0, bitmap.length, dib.bitmap, 0);
            } else
            {
                dib.biCompression = 0;
                dib.bitmap = bitmap;
            }
        }
        if(imageFormat.equals("wmf"))
        {
            int parameters[] = new int[1];
            parameters[0] = 1;
            WMFWriter.Record record = new WMFWriter.Record(259, parameters);
            wmf.add(record);
            parameters = new int[2];
            record = new WMFWriter.Record(523, parameters);
            wmf.add(record);
            parameters = new int[2];
            parameters[0] = srcHeight;
            parameters[1] = srcWidth;
            record = new WMFWriter.Record(524, parameters);
            wmf.add(record);
            parameters = new int[8];
            parameters[1] = 204;
            parameters[0] = 32;
            parameters[5] = srcWidth;
            parameters[4] = srcHeight;
            record = new WMFWriter.DibBitBltRecord(parameters, dib);
            wmf.add(record);
        }
        if(imageFormat.equals("wmf"))
        {
            writer.print("{\\pict\\wmetafile1");
            writer.println("\\picbmp\\picbpp" + dib.biBitCount);
        } else
        {
            writer.print("{\\pict\\dibitmap0\\wbmplanes1");
            writer.print("\\wbmbitspixel" + dib.biBitCount);
            writer.println("\\wbmwidthbytes" + bytesPerLine);
        }
        writer.print("\\picw" + srcWidth);
        writer.print("\\pich" + srcHeight);
        writer.print("\\picwgoal" + toTwips(srcWidth, 4));
        writer.println("\\pichgoal" + toTwips(srcHeight, 4));
        if(imageFormat.equals("wmf"))
        {
            if(imageDataFormat.equals("raw"))
            {
                writer.print("\\bin" + 2 * wmf.size() + " ");
                writer.flush();
                wmf.write(stream);
                stream.flush();
            } else
            {
                wmf.print(writer);
            }
        } else
        if(imageDataFormat.equals("raw"))
        {
            writer.print("\\bin" + 2 * dib.size() + " ");
            writer.flush();
            dib.write(stream);
            stream.flush();
        } else
        {
            dib.print(writer);
        }
        writer.println("}");
    }

    public void figureCaption()
        throws ParseException
    {
        Paragraph paragraph = new Paragraph();
        paragraph.justification = 0;
        paragraph.spaceBefore /= 2;
        beginParagraph(paragraph);
    }

    public void figureCaption_()
        throws ParseException
    {
        endParagraph();
    }

    public void horizontalRule()
        throws ParseException
    {
        writer.print("\\pard\\li" + indentation.get());
        int skip = space.getNext();
        if(skip > 0)
            writer.print("\\sb" + skip);
        space.setNext(skip);
        writer.print("\\brdrb\\brdrs\\brdrw15");
        writer.println("\\plain\\fs1\\par");
    }

    public void pageBreak()
        throws ParseException
    {
        writer.println("\\page");
    }

    public void anchor(String s)
        throws ParseException
    {
    }

    public void anchor_()
        throws ParseException
    {
    }

    public void link(String s)
        throws ParseException
    {
    }

    public void link_()
        throws ParseException
    {
    }

    public void italic()
        throws ParseException
    {
        beginStyle(1);
    }

    public void italic_()
        throws ParseException
    {
        endStyle();
    }

    public void bold()
        throws ParseException
    {
        beginStyle(2);
    }

    public void bold_()
        throws ParseException
    {
        endStyle();
    }

    public void monospaced()
        throws ParseException
    {
        beginStyle(3);
    }

    public void monospaced_()
        throws ParseException
    {
        endStyle();
    }

    private void beginStyle(int style)
    {
        this.style = style;
        switch(context.get())
        {
        default:
            if(paragraph != null)
                switch(style)
                {
                case 1: // '\001'
                    writer.println("{\\i");
                    break;

                case 2: // '\002'
                    writer.println("{\\b");
                    break;

                case 3: // '\003'
                    writer.println("{\\f1");
                    break;

                default:
                    writer.println("{");
                    break;
                }
            // fall through

        case 2: // '\002'
            return;
        }
    }

    private void endStyle()
    {
        style = 0;
        switch(context.get())
        {
        default:
            if(paragraph != null)
                writer.println("}");
            // fall through

        case 2: // '\002'
            return;
        }
    }

    public void lineBreak()
        throws ParseException
    {
        switch(context.get())
        {
        case 2: // '\002'
            cell.add(line);
            line = new Line();
            break;

        default:
            writer.println("\\line");
            break;
        }
    }

    public void nonBreakingSpace()
        throws ParseException
    {
        switch(context.get())
        {
        case 2: // '\002'
            line.add(new Item(style, " "));
            break;

        default:
            writer.println("\\~");
            break;
        }
    }

    public void text(String text)
        throws ParseException
    {
        switch(context.get())
        {
        case 1: // '\001'
            verbatim.append(text);
            break;

        case 2: // '\002'
            for(StringTokenizer t = new StringTokenizer(text, "\n", true); t.hasMoreTokens();)
            {
                String token = t.nextToken();
                if(token.equals("\n"))
                {
                    cell.add(line);
                    line = new Line();
                } else
                {
                    line.add(new Item(style, normalize(token)));
                }
            }

            break;

        default:
            if(paragraph == null)
                beginParagraph(new Paragraph());
            writer.println(escape(normalize(text)));
            break;
        }
    }

    private static String normalize(String s)
    {
        int length = s.length();
        StringBuffer buffer = new StringBuffer(length);
        for(int i = 0; i < length; i++)
        {
            char c = s.charAt(i);
            if(Character.isWhitespace(c))
            {
                if(buffer.length() == 0 || buffer.charAt(buffer.length() - 1) != ' ')
                    buffer.append(' ');
            } else
            {
                buffer.append(c);
            }
        }

        return buffer.toString();
    }

    private static String escape(String s)
    {
        int length = s.length();
        StringBuffer buffer = new StringBuffer(length);
        for(int i = 0; i < length; i++)
        {
            char c = s.charAt(i);
            switch(c)
            {
            case 92: // '\\'
                buffer.append("\\\\");
                break;

            case 123: // '{'
                buffer.append("\\{");
                break;

            case 125: // '}'
                buffer.append("\\}");
                break;

            default:
                buffer.append(c);
                break;
            }
        }

        return buffer.toString();
    }

    private Font getFont(int style, int size)
    {
        Font font = null;
        StringBuffer buf = new StringBuffer();
        buf.append(style);
        buf.append(size);
        String key = buf.toString();
        Object object = fontTable.get(key);
        if(object == null)
            try
            {
                font = new Font(style, size);
                fontTable.put(key, font);
            }
            catch(Exception exception) { }
        else
            font = (Font)object;
        return font;
    }

    private static int textWidth(String text, Font font)
    {
        int width = 0;
        for(StringTokenizer t = new StringTokenizer(text, "\n"); t.hasMoreTokens();)
        {
            int w = font.textExtents(t.nextToken()).width;
            if(w > width)
                width = w;
        }

        return width;
    }

    public static void main(String args[])
        throws Exception
    {
        Parser parser = new Parser();
        java.io.Reader input;
        if(args.length < 1 || args[0].equals("-"))
            input = new InputStreamReader(System.in);
        else
            input = new FileReader(args[0]);
        ReaderSource source = new ReaderSource(input);
        OutputStream output;
        if(args.length < 2 || args[1].equals("-"))
            output = System.out;
        else
            output = new FileOutputStream(args[1]);
        RTFSink sink = new RTFSink(output);
        parser.parse(source, sink);
    }

    public static final double DEFAULT_PAPER_WIDTH = 21D;
    public static final double DEFAULT_PAPER_HEIGHT = 29.699999999999999D;
    public static final double DEFAULT_TOP_MARGIN = 2D;
    public static final double DEFAULT_BOTTOM_MARGIN = 2D;
    public static final double DEFAULT_LEFT_MARGIN = 2D;
    public static final double DEFAULT_RIGHT_MARGIN = 2D;
    public static final int DEFAULT_FONT_SIZE = 10;
    public static final int DEFAULT_SPACING = 10;
    public static final int DEFAULT_RESOLUTION = 72;
    public static final String DEFAULT_IMAGE_FORMAT = "bmp";
    public static final String DEFAULT_IMAGE_TYPE = "palette";
    public static final String DEFAULT_DATA_FORMAT = "ascii";
    public static final int DEFAULT_CODE_PAGE = 1252;
    public static final int DEFAULT_CHAR_SET = 0;
    public static final String IMG_FORMAT_BMP = "bmp";
    public static final String IMG_FORMAT_WMF = "wmf";
    public static final String IMG_TYPE_PALETTE = "palette";
    public static final String IMG_TYPE_RGB = "rgb";
    public static final String IMG_DATA_ASCII = "ascii";
    public static final String IMG_DATA_RAW = "raw";
    public static final int STYLE_ROMAN = 0;
    public static final int STYLE_ITALIC = 1;
    public static final int STYLE_BOLD = 2;
    public static final int STYLE_TYPEWRITER = 3;
    private static final int CONTEXT_UNDEFINED = 0;
    private static final int CONTEXT_VERBATIM = 1;
    private static final int CONTEXT_TABLE = 2;
    private static final int UNIT_MILLIMETER = 1;
    private static final int UNIT_CENTIMETER = 2;
    private static final int UNIT_INCH = 3;
    private static final int UNIT_PIXEL = 4;
    private static final int LIST_INDENT = 300;
    private static final String LIST_ITEM_HEADER = "-  ";
    private static final int DEFINITION_INDENT = 300;
    private static final int CELL_HORIZONTAL_PAD = 60;
    private static final int CELL_VERTICAL_PAD = 20;
    private static final int BORDER_WIDTH = 15;
    private double paperWidth;
    private double paperHeight;
    private double topMargin;
    private double bottomMargin;
    private double leftMargin;
    private double rightMargin;
    private int fontSize;
    private int resolution;
    private String imageFormat;
    private String imageType;
    private String imageDataFormat;
    private boolean imageCompression;
    private int codePage;
    private int charSet;
    private Hashtable fontTable;
    private Context context;
    private Paragraph paragraph;
    private Indentation indentation;
    private Space space;
    private int listItemIndent;
    private Vector numbering;
    private Vector itemNumber;
    private int style;
    private int sectionLevel;
    private boolean emptyHeader;
    private StringBuffer verbatim;
    boolean frame;
    private Table table;
    private Row row;
    private Cell cell;
    private Line line;
    protected PrintWriter writer;
    protected OutputStream stream;





}

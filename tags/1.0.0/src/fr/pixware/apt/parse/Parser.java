// 
// 
// 
// Source File Name:   Parser.java

package fr.pixware.apt.parse;

import fr.pixware.util.StringUtil;
import java.util.StringTokenizer;

// Referenced classes of package fr.pixware.apt.parse:
//            ParseException, Sink, Source

public class Parser
{
    private class ListBreak extends Block
    {

        public void traverse()
            throws ParseException
        {
            throw new ParseException("internal error: traversing list break");
        }

        public ListBreak(int indent, String firstLine)
            throws ParseException
        {
            super(15, indent);
        }
    }

    private class PageBreak extends Block
    {

        public void traverse()
            throws ParseException
        {
            sink.pageBreak();
        }

        public PageBreak(int indent, String firstLine)
            throws ParseException
        {
            super(14, indent);
        }
    }

    private class HorizontalRule extends Block
    {

        public void traverse()
            throws ParseException
        {
            sink.horizontalRule();
        }

        public HorizontalRule(int indent, String firstLine)
            throws ParseException
        {
            super(13, indent);
        }
    }

    private class DefinitionListItem extends Block
    {

        public void traverse()
            throws ParseException
        {
            int i = skipSpaceFrom(0);
            int j = skipFromLeftToRightBracket(i);
            sink.definedTerm();
            traverseText(i + 1, j);
            sink.definedTerm_();
            j = skipSpaceFrom(j + 1);
            if(j == super.textLength)
            {
                throw new ParseException("no definition");
            } else
            {
                sink.definition();
                sink.paragraph();
                traverseText(j);
                sink.paragraph_();
                return;
            }
        }

        public DefinitionListItem(int indent, String firstLine)
            throws ParseException
        {
            super(12, indent, firstLine);
        }
    }

    private class NumberedListItem extends Block
    {

        public int getNumbering()
        {
            return numbering;
        }

        public void traverse()
            throws ParseException
        {
            sink.paragraph();
            traverseText(skipItemNumber());
            sink.paragraph_();
        }

        private int skipItemNumber()
            throws ParseException
        {
            int i = skipSpaceFrom(0);
            char prevChar = ' ';
            for(; i < super.textLength; i++)
            {
                char c = super.text.charAt(i);
                if(c == ']' && prevChar == ']')
                    break;
                prevChar = c;
            }

            if(i == super.textLength)
                throw new ParseException("missing ']]'");
            else
                return skipSpaceFrom(i + 1);
        }

        private int numbering;

        public NumberedListItem(int indent, String firstLine, int numbering)
            throws ParseException
        {
            super(11, indent, firstLine);
            this.numbering = numbering;
        }
    }

    private class ListItem extends Block
    {

        public void traverse()
            throws ParseException
        {
            sink.paragraph();
            traverseText(skipLeadingBullets());
            sink.paragraph_();
        }

        public ListItem(int indent, String firstLine)
            throws ParseException
        {
            super(10, indent, firstLine);
        }
    }

    private class Table extends Block
    {

        public void traverse()
            throws ParseException
        {
            int captionIndex = -1;
            int nextLineIndex = 0;
            int init = 2;
            int justification[] = null;
            int rows = 0;
            int columns = 0;
            StringBuffer cells[] = null;
            boolean grid = false;
            sink.table();
            while(nextLineIndex < super.textLength) 
            {
                int i = super.text.indexOf("*--", nextLineIndex);
                if(i < 0)
                {
                    captionIndex = nextLineIndex;
                    break;
                }
                i = super.text.indexOf('\n', nextLineIndex);
                String line;
                if(i < 0)
                {
                    line = super.text.substring(nextLineIndex);
                    nextLineIndex = super.textLength;
                } else
                {
                    line = super.text.substring(nextLineIndex, i);
                    nextLineIndex = i + 1;
                }
                int lineLength = line.length();
                if(line.indexOf("*--") == 0)
                {
                    if(init == 2)
                    {
                        init = 1;
                        justification = parseJustification(line, lineLength);
                        columns = justification.length;
                        cells = new StringBuffer[columns];
                        for(i = 0; i < columns; i++)
                            cells[i] = new StringBuffer();

                    } else
                    if(traverseRow(cells))
                        rows++;
                } else
                {
                    if(init == 1)
                    {
                        init = 0;
                        grid = Parser.charAt(line, lineLength, 0) == '|';
                        sink.tableRows(justification, grid);
                    }
                    line = StringUtil.replaceAll(grid ? line.substring(1) : line, "\\|", "\\174");
                    StringTokenizer cellLines = new StringTokenizer(line, "|");
                    i = 0;
                    while(cellLines.hasMoreTokens()) 
                    {
                        String cellLine = cellLines.nextToken().trim();
                        StringBuffer cell = cells[i];
                        if(cellLine.length() > 0)
                        {
                            if(cell.length() > 0)
                                cell.append("\\\n");
                            cell.append(cellLine);
                        }
                        if(++i == columns)
                            break;
                    }
                }
            }
            if(rows == 0)
                throw new ParseException("no table rows");
            sink.tableRows_();
            if(captionIndex >= 0)
            {
                sink.tableCaption();
                Parser.doTraverseText(super.text, captionIndex, super.textLength, sink);
                sink.tableCaption_();
            }
            sink.table_();
        }

        private int[] parseJustification(String line, int lineLength)
            throws ParseException
        {
            int columns = 0;
            for(int i = 2; i < lineLength;)
                switch(line.charAt(i))
                {
                case 42: // '*'
                case 43: // '+'
                case 58: // ':'
                    columns++;
                    // fall through

                default:
                    i++;
                    break;
                }

            if(columns == 0)
                throw new ParseException("no columns specified");
            int justification[] = new int[columns];
            columns = 0;
            for(int i = 2; i < lineLength; i++)
                switch(line.charAt(i))
                {
                case 42: // '*'
                    justification[columns++] = 0;
                    break;

                case 43: // '+'
                    justification[columns++] = 1;
                    break;

                case 58: // ':'
                    justification[columns++] = 2;
                    break;
                }

            return justification;
        }

        private boolean traverseRow(StringBuffer cells[])
            throws ParseException
        {
            boolean traversed = false;
            for(int i = 0; i < cells.length; i++)
            {
                if(cells[i].length() <= 0)
                    continue;
                traversed = true;
                break;
            }

            if(traversed)
            {
                sink.tableRow();
                for(int i = 0; i < cells.length; i++)
                {
                    StringBuffer cell = cells[i];
                    sink.tableCell();
                    if(cell.length() > 0)
                    {
                        Parser.doTraverseText(cell.toString(), 0, cell.length(), sink);
                        cell.setLength(0);
                    }
                    sink.tableCell_();
                }

                sink.tableRow_();
            }
            return traversed;
        }

        public Table(int indent, String firstLine)
            throws ParseException
        {
            super(9, indent, firstLine);
        }
    }

    private class Figure extends Block
    {

        public void traverse()
            throws ParseException
        {
            sink.figure();
            int i = skipFromLeftToRightBracket(0);
            sink.figureGraphics(super.text.substring(1, i));
            i = skipSpaceFrom(i + 1);
            if(i < super.textLength)
            {
                sink.figureCaption();
                traverseText(i);
                sink.figureCaption_();
            }
            sink.figure_();
        }

        public Figure(int indent, String firstLine)
            throws ParseException
        {
            super(8, indent, firstLine);
        }
    }

    private class Verbatim extends Block
    {

        public void traverse()
            throws ParseException
        {
            sink.verbatim(boxed);
            sink.text(super.text);
            sink.verbatim_();
        }

        private boolean boxed;

        public Verbatim(int indent, String firstLine)
            throws ParseException
        {
            super(7, indent, null);
            StringBuffer buffer = new StringBuffer();
            char firstChar = firstLine.charAt(0);
            boxed = firstChar == '+';
            while(line != null) 
            {
                String l = line;
                int length = l.length();
                if(Parser.charAt(l, length, 0) == firstChar && Parser.charAt(l, length, 1) == '-' && Parser.charAt(l, length, 2) == '-')
                {
                    nextLine();
                    break;
                }
                int column = 0;
                for(int i = 0; i < length; i++)
                {
                    char c = l.charAt(i);
                    if(c == '\t')
                    {
                        int prevColumn = column;
                        column = (((column + 1 + 8) - 1) / 8) * 8;
                        buffer.append(Parser.spaces, 0, column - prevColumn);
                    } else
                    {
                        column++;
                        buffer.append(c);
                    }
                }

                buffer.append('\n');
                nextLine();
            }
            super.textLength = buffer.length();
            if(super.textLength > 0)
            {
                super.textLength--;
                buffer.setLength(super.textLength);
            }
            super.text = buffer.toString();
        }
    }

    private class Paragraph extends Block
    {

        public void traverse()
            throws ParseException
        {
            sink.paragraph();
            traverseText(skipSpaceFrom(0));
            sink.paragraph_();
        }

        public Paragraph(int indent, String firstLine)
            throws ParseException
        {
            super(6, indent, firstLine);
        }
    }

    private class Section5 extends Section
    {

        public Section5(int indent, String firstLine)
            throws ParseException
        {
            super(5, indent, firstLine);
        }
    }

    private class Section4 extends Section
    {

        public Section4(int indent, String firstLine)
            throws ParseException
        {
            super(4, indent, firstLine);
        }
    }

    private class Section3 extends Section
    {

        public Section3(int indent, String firstLine)
            throws ParseException
        {
            super(3, indent, firstLine);
        }
    }

    private class Section2 extends Section
    {

        public Section2(int indent, String firstLine)
            throws ParseException
        {
            super(2, indent, firstLine);
        }
    }

    private class Section1 extends Section
    {

        public Section1(int indent, String firstLine)
            throws ParseException
        {
            super(1, indent, firstLine);
        }
    }

    private class Section extends Block
    {

        public void traverse()
            throws ParseException
        {
            sink.sectionTitle();
            traverseText(skipLeadingBullets());
            sink.sectionTitle_();
        }

        public Section(int type, int indent, String firstLine)
            throws ParseException
        {
            super(type, indent, firstLine);
        }
    }

    private class Title extends Block
    {

        public void traverse()
            throws ParseException
        {
            StringTokenizer lines = new StringTokenizer(super.text, "\n");
            int separator = -1;
            boolean firstLine = true;
            boolean title = false;
            boolean author = false;
            boolean date = false;
label0:
            while(lines.hasMoreTokens()) 
            {
                String line = lines.nextToken().trim();
                int lineLength = line.length();
                if(Parser.charAt(line, lineLength, 0) == '-' && Parser.charAt(line, lineLength, 1) == '-' && Parser.charAt(line, lineLength, 2) == '-')
                {
                    switch(separator)
                    {
                    default:
                        break;

                    case 0: // '\0'
                        if(title)
                            sink.title_();
                        else
                            throw new ParseException("missing title");
                        break;

                    case 2: // '\002'
                        break label0;

                    case 1: // '\001'
                        if(author)
                            sink.author_();
                        break;
                    }
                    separator++;
                    firstLine = true;
                } else
                {
                    if(firstLine)
                    {
                        firstLine = false;
                        switch(separator)
                        {
                        case 0: // '\0'
                            title = true;
                            sink.title();
                            break;

                        case 1: // '\001'
                            author = true;
                            sink.author();
                            break;

                        case 2: // '\002'
                            date = true;
                            sink.date();
                            break;
                        }
                    } else
                    {
                        sink.lineBreak();
                    }
                    Parser.doTraverseText(line, 0, lineLength, sink);
                }
            }
            switch(separator)
            {
            default:
                break;

            case 0: // '\0'
                if(title)
                    sink.title_();
                else
                    throw new ParseException("missing title");
                break;

            case 1: // '\001'
                if(author)
                    sink.author_();
                break;

            case 2: // '\002'
                if(date)
                    sink.date_();
                break;
            }
        }

        public Title(int indent, String firstLine)
            throws ParseException
        {
            super(0, indent, firstLine);
        }
    }

    private abstract class Block
    {

        public final int getType()
        {
            return type;
        }

        public final int getIndent()
        {
            return indent;
        }

        public abstract void traverse()
            throws ParseException;

        protected void traverseText(int begin)
            throws ParseException
        {
            traverseText(begin, text.length());
        }

        protected void traverseText(int begin, int end)
            throws ParseException
        {
            Parser.doTraverseText(text, begin, end, sink);
        }

        protected int skipLeadingBullets()
        {
            int i;
            for(i = skipSpaceFrom(0); i < textLength; i++)
                if(text.charAt(i) != '*')
                    break;

            return skipSpaceFrom(i);
        }

        protected int skipFromLeftToRightBracket(int i)
            throws ParseException
        {
            char previous = '[';
            for(i++; i < textLength; i++)
            {
                char c = text.charAt(i);
                if(c == ']' && previous != '\\')
                    break;
                previous = c;
            }

            if(i == textLength)
                throw new ParseException("missing ']'");
            else
                return i;
        }

        protected final int skipSpaceFrom(int i)
        {
            return Parser.skipSpace(text, textLength, i);
        }

        protected int type;
        protected int indent;
        protected String text;
        protected int textLength;

        public Block(int type, int indent)
            throws ParseException
        {
            this(type, indent, null);
        }

        public Block(int type, int indent, String firstLine)
            throws ParseException
        {
            this.type = type;
            this.indent = indent;
            nextLine();
            if(firstLine == null)
            {
                text = null;
                textLength = 0;
            } else
            {
                StringBuffer buffer = new StringBuffer(firstLine);
                while(line != null) 
                {
                    String l = line;
                    int length = l.length();
                    int i = 0;
                    i = Parser.skipSpace(l, length, i);
                    if(i == length || Parser.charAt(l, length, i) == '~' && Parser.charAt(l, length, i + 1) == '~')
                    {
                        nextLine();
                        break;
                    }
                    buffer.append('\n');
                    buffer.append(l);
                    nextLine();
                }
                text = buffer.toString();
                textLength = text.length();
            }
        }
    }


    public Parser()
    {
    }

    public void parse(Source source, Sink sink)
        throws ParseException
    {
        this.source = source;
        this.sink = sink;
        blockFileName = null;
        blockLineNumber = -1;
        nextLine();
        nextBlock(true);
        traverseHead();
        traverseBody();
        this.source = null;
        this.sink = null;
    }

    public String getSourceName()
    {
        return blockFileName;
    }

    public int getSourceLineNumber()
    {
        return blockLineNumber;
    }

    private void traverseHead()
        throws ParseException
    {
        sink.head();
        if(block != null && block.getType() == 0)
        {
            block.traverse();
            nextBlock();
        }
        sink.head_();
    }

    private void traverseBody()
        throws ParseException
    {
        sink.body();
        if(block != null)
            traverseSectionBlocks();
        while(block != null) 
            traverseSection(0);
        sink.body_();
    }

    private void traverseSection(int level)
        throws ParseException
    {
        if(block == null)
            return;
        int type = 1 + level;
        expectedBlock(type);
        switch(level)
        {
        case 0: // '\0'
            sink.section1();
            break;

        case 1: // '\001'
            sink.section2();
            break;

        case 2: // '\002'
            sink.section3();
            break;

        case 3: // '\003'
            sink.section4();
            break;

        case 4: // '\004'
            sink.section5();
            break;
        }
        block.traverse();
        nextBlock();
        traverseSectionBlocks();
        while(block != null) 
        {
            if(block.getType() <= type)
                break;
            traverseSection(level + 1);
        }
        switch(level)
        {
        case 0: // '\0'
            sink.section1_();
            break;

        case 1: // '\001'
            sink.section2_();
            break;

        case 2: // '\002'
            sink.section3_();
            break;

        case 3: // '\003'
            sink.section4_();
            break;

        case 4: // '\004'
            sink.section5_();
            break;
        }
    }

    private void traverseSectionBlocks()
        throws ParseException
    {
label0:
        while(block != null) 
            switch(block.getType())
            {
            default:
                break label0;

            case 6: // '\006'
            case 7: // '\007'
            case 8: // '\b'
            case 9: // '\t'
            case 13: // '\r'
            case 14: // '\016'
                block.traverse();
                nextBlock();
                break;

            case 10: // '\n'
                traverseList();
                break;

            case 11: // '\013'
                traverseNumberedList();
                break;

            case 12: // '\f'
                traverseDefinitionList();
                break;

            case 15: // '\017'
                nextBlock();
                break;
            }
    }

    private void traverseList()
        throws ParseException
    {
        if(block == null)
            return;
        expectedBlock(10);
        int listIndent = block.getIndent();
        sink.list();
        sink.listItem();
        block.traverse();
        nextBlock();
label0:
        while(block != null) 
        {
            int blockIndent = block.getIndent();
            switch(block.getType())
            {
            default:
                break label0;

            case 6: // '\006'
                if(blockIndent < listIndent)
                    break label0;
                // fall through

            case 7: // '\007'
            case 8: // '\b'
            case 9: // '\t'
            case 13: // '\r'
            case 14: // '\016'
                block.traverse();
                nextBlock();
                break;

            case 10: // '\n'
                if(blockIndent >= listIndent)
                {
                    if(blockIndent > listIndent)
                    {
                        traverseList();
                    } else
                    {
                        sink.listItem_();
                        sink.listItem();
                        block.traverse();
                        nextBlock();
                    }
                    break;
                }
                break label0;

            case 11: // '\013'
                if(blockIndent >= listIndent)
                {
                    traverseNumberedList();
                    break;
                }
                break label0;

            case 12: // '\f'
                if(blockIndent >= listIndent)
                {
                    traverseDefinitionList();
                    break;
                }
                break label0;

            case 15: // '\017'
                if(blockIndent >= listIndent)
                    nextBlock();
                break label0;
            }
        }
        sink.listItem_();
        sink.list_();
    }

    private void traverseNumberedList()
        throws ParseException
    {
        if(block == null)
            return;
        expectedBlock(11);
        int listIndent = block.getIndent();
        sink.numberedList(((NumberedListItem)block).getNumbering());
        sink.numberedListItem();
        block.traverse();
        nextBlock();
label0:
        while(block != null) 
        {
            int blockIndent = block.getIndent();
            switch(block.getType())
            {
            default:
                break label0;

            case 6: // '\006'
                if(blockIndent < listIndent)
                    break label0;
                // fall through

            case 7: // '\007'
            case 8: // '\b'
            case 9: // '\t'
            case 13: // '\r'
            case 14: // '\016'
                block.traverse();
                nextBlock();
                break;

            case 10: // '\n'
                if(blockIndent >= listIndent)
                {
                    traverseList();
                    break;
                }
                break label0;

            case 11: // '\013'
                if(blockIndent >= listIndent)
                {
                    if(blockIndent > listIndent)
                    {
                        traverseNumberedList();
                    } else
                    {
                        sink.numberedListItem_();
                        sink.numberedListItem();
                        block.traverse();
                        nextBlock();
                    }
                    break;
                }
                break label0;

            case 12: // '\f'
                if(blockIndent >= listIndent)
                {
                    traverseDefinitionList();
                    break;
                }
                break label0;

            case 15: // '\017'
                if(blockIndent >= listIndent)
                    nextBlock();
                break label0;
            }
        }
        sink.numberedListItem_();
        sink.numberedList_();
    }

    private void traverseDefinitionList()
        throws ParseException
    {
        if(block == null)
            return;
        expectedBlock(12);
        int listIndent = block.getIndent();
        sink.definitionList();
        sink.definitionListItem();
        block.traverse();
        nextBlock();
label0:
        while(block != null) 
        {
            int blockIndent = block.getIndent();
            switch(block.getType())
            {
            default:
                break label0;

            case 6: // '\006'
                if(blockIndent < listIndent)
                    break label0;
                // fall through

            case 7: // '\007'
            case 8: // '\b'
            case 9: // '\t'
            case 13: // '\r'
            case 14: // '\016'
                block.traverse();
                nextBlock();
                break;

            case 10: // '\n'
                if(blockIndent >= listIndent)
                {
                    traverseList();
                    break;
                }
                break label0;

            case 11: // '\013'
                if(blockIndent >= listIndent)
                {
                    traverseNumberedList();
                    break;
                }
                break label0;

            case 12: // '\f'
                if(blockIndent >= listIndent)
                {
                    if(blockIndent > listIndent)
                    {
                        traverseDefinitionList();
                    } else
                    {
                        sink.definition_();
                        sink.definitionListItem_();
                        sink.definitionListItem();
                        block.traverse();
                        nextBlock();
                    }
                    break;
                }
                break label0;

            case 15: // '\017'
                if(blockIndent >= listIndent)
                    nextBlock();
                break label0;
            }
        }
        sink.definition_();
        sink.definitionListItem_();
        sink.definitionList_();
    }

    private final void nextLine()
        throws ParseException
    {
        line = source.getNextLine();
    }

    private void nextBlock()
        throws ParseException
    {
        nextBlock(false);
    }

    private void nextBlock(boolean firstBlock)
        throws ParseException
    {
        int length;
        int indent;
        int i;
label0:
        do
        {
            if(line == null)
            {
                block = null;
                return;
            }
            length = line.length();
            indent = 0;
label1:
            for(i = 0; i < length; i++)
                switch(line.charAt(i))
                {
                default:
                    break label0;

                case 32: // ' '
                    indent++;
                    break;

                case 9: // '\t'
                    indent += 8;
                    break;

                case 126: // '~'
                    if(charAt(line, length, i + 1) == '~')
                    {
                        i = length;
                        break label1;
                    }
                    break label0;
                }

            if(i == length)
                nextLine();
        } while(true);
        blockFileName = source.getName();
        blockLineNumber = source.getLineNumber();
        block = null;
        switch(line.charAt(i))
        {
        case 42: // '*'
            if(indent == 0)
            {
                if(charAt(line, length, i + 1) == '-' && charAt(line, length, i + 2) == '-')
                    block = new Table(indent, line);
                else
                if(charAt(line, length, i + 1) == '*')
                {
                    if(charAt(line, length, i + 2) == '*')
                    {
                        if(charAt(line, length, i + 3) == '*')
                            block = new Section5(indent, line);
                        else
                            block = new Section4(indent, line);
                    } else
                    {
                        block = new Section3(indent, line);
                    }
                } else
                {
                    block = new Section2(indent, line);
                }
            } else
            {
                block = new ListItem(indent, line);
            }
            break;

        case 91: // '['
            if(charAt(line, length, i + 1) == ']')
                block = new ListBreak(indent, line);
            else
            if(indent == 0)
                block = new Figure(indent, line);
            else
            if(charAt(line, length, i + 1) == '[')
            {
                int numbering;
                switch(charAt(line, length, i + 2))
                {
                case 97: // 'a'
                    numbering = 1;
                    break;

                case 65: // 'A'
                    numbering = 2;
                    break;

                case 105: // 'i'
                    numbering = 3;
                    break;

                case 73: // 'I'
                    numbering = 4;
                    break;

                case 49: // '1'
                default:
                    numbering = 0;
                    break;
                }
                block = new NumberedListItem(indent, line, numbering);
            } else
            {
                block = new DefinitionListItem(indent, line);
            }
            break;

        case 45: // '-'
            if(charAt(line, length, i + 1) == '-' && charAt(line, length, i + 2) == '-')
                if(indent == 0)
                    block = new Verbatim(indent, line);
                else
                if(firstBlock)
                    block = new Title(indent, line);
            break;

        case 43: // '+'
            if(indent == 0 && charAt(line, length, i + 1) == '-' && charAt(line, length, i + 2) == '-')
                block = new Verbatim(indent, line);
            break;

        case 61: // '='
            if(indent == 0 && charAt(line, length, i + 1) == '=' && charAt(line, length, i + 2) == '=')
                block = new HorizontalRule(indent, line);
            break;

        case 12: // '\f'
            if(indent == 0)
                block = new PageBreak(indent, line);
            break;
        }
        if(block == null)
            if(indent == 0)
                block = new Section1(indent, line);
            else
                block = new Paragraph(indent, line);
    }

    private void expectedBlock(int type)
        throws ParseException
    {
        int blockType = block.getType();
        if(blockType != type)
            throw new ParseException("expected " + typeNames[type] + ", found " + typeNames[blockType]);
        else
            return;
    }

    private static final boolean isOctalChar(char c)
    {
        return c >= '0' && c <= '7';
    }

    private static final boolean isHexChar(char c)
    {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
    }

    private static final char charAt(String string, int length, int i)
    {
        return i >= length ? '\0' : string.charAt(i);
    }

    private static int skipSpace(String string, int length, int i)
    {
label0:
        while(i < length) 
            switch(string.charAt(i))
            {
            default:
                break label0;

            case 9: // '\t'
            case 32: // ' '
                i++;
                break;
            }
        return i;
    }

    private static void doTraverseText(String text, int begin, int end, Sink sink)
        throws ParseException
    {
        boolean anchor = false;
        boolean link = false;
        boolean italic = false;
        boolean bold = false;
        boolean monospaced = false;
        StringBuffer buffer = new StringBuffer(end - begin);
        for(int i = begin; i < end; i++)
        {
            char c = text.charAt(i);
            switch(c)
            {
            case 92: // '\\'
                if(i + 1 < end)
                {
                    char escaped = text.charAt(i + 1);
                    switch(escaped)
                    {
                    case 32: // ' '
                        i++;
                        flushTraversed(buffer, sink);
                        sink.nonBreakingSpace();
                        break;

                    case 10: // '\n'
                        for(i++; i + 1 < end && Character.isWhitespace(text.charAt(i + 1)); i++);
                        flushTraversed(buffer, sink);
                        sink.lineBreak();
                        break;

                    case 42: // '*'
                    case 43: // '+'
                    case 45: // '-'
                    case 60: // '<'
                    case 61: // '='
                    case 62: // '>'
                    case 91: // '['
                    case 92: // '\\'
                    case 93: // ']'
                    case 123: // '{'
                    case 124: // '|'
                    case 125: // '}'
                    case 126: // '~'
                        i++;
                        buffer.append(escaped);
                        break;

                    case 120: // 'x'
                        if(i + 3 < end && isHexChar(text.charAt(i + 2)) && isHexChar(text.charAt(i + 3)))
                        {
                            int value = 63;
                            try
                            {
                                value = Integer.parseInt(text.substring(i + 2, i + 4), 16);
                            }
                            catch(NumberFormatException numberformatexception) { }
                            i += 3;
                            buffer.append((char)value);
                        } else
                        {
                            buffer.append('\\');
                        }
                        break;

                    case 117: // 'u'
                        if(i + 5 < end && isHexChar(text.charAt(i + 2)) && isHexChar(text.charAt(i + 3)) && isHexChar(text.charAt(i + 4)) && isHexChar(text.charAt(i + 5)))
                        {
                            int value = 63;
                            try
                            {
                                value = Integer.parseInt(text.substring(i + 2, i + 6), 16);
                            }
                            catch(NumberFormatException numberformatexception1) { }
                            i += 5;
                            buffer.append((char)value);
                        } else
                        {
                            buffer.append('\\');
                        }
                        break;

                    default:
                        if(isOctalChar(escaped))
                        {
                            int octalChars = 1;
                            if(isOctalChar(charAt(text, end, i + 2)))
                            {
                                octalChars++;
                                if(isOctalChar(charAt(text, end, i + 3)))
                                    octalChars++;
                            }
                            int value = 63;
                            try
                            {
                                value = Integer.parseInt(text.substring(i + 1, i + 1 + octalChars), 8);
                            }
                            catch(NumberFormatException numberformatexception2) { }
                            i += octalChars;
                            buffer.append((char)value);
                        } else
                        {
                            buffer.append('\\');
                        }
                        break;
                    }
                } else
                {
                    buffer.append('\\');
                }
                break;

            case 123: // '{'
                if(!anchor && !link)
                {
                    if(i + 1 < end && text.charAt(i + 1) == '{')
                    {
                        i++;
                        link = true;
                        flushTraversed(buffer, sink);
                        String linkAnchor = null;
                        if(i + 1 < end && text.charAt(i + 1) == '{')
                        {
                            i++;
                            StringBuffer buf = new StringBuffer();
                            i = skipTraversedLinkAnchor(text, i + 1, end, buf);
                            linkAnchor = buf.toString();
                        }
                        if(linkAnchor == null)
                            linkAnchor = getTraversedLink(text, i + 1, end);
                        sink.link(linkAnchor);
                    } else
                    {
                        anchor = true;
                        flushTraversed(buffer, sink);
                        sink.anchor(getTraversedAnchor(text, i + 1, end));
                    }
                } else
                {
                    buffer.append(c);
                }
                break;

            case 125: // '}'
                if(link && i + 1 < end && text.charAt(i + 1) == '}')
                {
                    i++;
                    link = false;
                    flushTraversed(buffer, sink);
                    sink.link_();
                    break;
                }
                if(anchor)
                {
                    anchor = false;
                    flushTraversed(buffer, sink);
                    sink.anchor_();
                } else
                {
                    buffer.append(c);
                }
                break;

            case 60: // '<'
                if(!italic && !bold && !monospaced)
                {
                    if(i + 1 < end && text.charAt(i + 1) == '<')
                    {
                        if(i + 2 < end && text.charAt(i + 2) == '<')
                        {
                            i += 2;
                            monospaced = true;
                            flushTraversed(buffer, sink);
                            sink.monospaced();
                        } else
                        {
                            i++;
                            bold = true;
                            flushTraversed(buffer, sink);
                            sink.bold();
                        }
                    } else
                    {
                        italic = true;
                        flushTraversed(buffer, sink);
                        sink.italic();
                    }
                } else
                {
                    buffer.append(c);
                }
                break;

            case 62: // '>'
                if(monospaced && i + 2 < end && text.charAt(i + 1) == '>' && text.charAt(i + 2) == '>')
                {
                    i += 2;
                    monospaced = false;
                    flushTraversed(buffer, sink);
                    sink.monospaced_();
                    break;
                }
                if(bold && i + 1 < end && text.charAt(i + 1) == '>')
                {
                    i++;
                    bold = false;
                    flushTraversed(buffer, sink);
                    sink.bold_();
                    break;
                }
                if(italic)
                {
                    italic = false;
                    flushTraversed(buffer, sink);
                    sink.italic_();
                } else
                {
                    buffer.append(c);
                }
                break;

            default:
                if(Character.isWhitespace(c))
                {
                    buffer.append(' ');
                    for(; i + 1 < end && Character.isWhitespace(text.charAt(i + 1)); i++);
                } else
                {
                    buffer.append(c);
                }
                break;
            }
        }

        if(monospaced)
            throw new ParseException("missing '>>>'");
        if(bold)
            throw new ParseException("missing '>>'");
        if(italic)
            throw new ParseException("missing '>'");
        if(link)
            throw new ParseException("missing '}}'");
        if(anchor)
        {
            throw new ParseException("missing '}'");
        } else
        {
            flushTraversed(buffer, sink);
            return;
        }
    }

    private static final void flushTraversed(StringBuffer buffer, Sink sink)
        throws ParseException
    {
        if(buffer.length() > 0)
        {
            sink.text(buffer.toString());
            buffer.setLength(0);
        }
    }

    private static int skipTraversedLinkAnchor(String text, int begin, int end, StringBuffer linkAnchor)
        throws ParseException
    {
        int i;
label0:
        for(i = begin; i < end; i++)
        {
            char c = text.charAt(i);
            switch(c)
            {
            case 125: // '}'
                break label0;

            case 92: // '\\'
                if(i + 1 < end)
                {
                    i++;
                    linkAnchor.append(text.charAt(i));
                } else
                {
                    linkAnchor.append('\\');
                }
                break;

            default:
                linkAnchor.append(c);
                break;
            }
        }

        if(i == end)
            throw new ParseException("missing '}'");
        else
            return i;
    }

    private static String getTraversedLink(String text, int begin, int end)
        throws ParseException
    {
        char previous2 = '{';
        char previous = '{';
        int i;
        for(i = begin; i < end; i++)
        {
            char c = text.charAt(i);
            if(c == '}' && previous == '}' && previous2 != '\\')
                break;
            previous2 = previous;
            previous = c;
        }

        if(i == end)
            throw new ParseException("missing '}}'");
        else
            return doGetTraversedLink(text, begin, i - 1);
    }

    private static String getTraversedAnchor(String text, int begin, int end)
        throws ParseException
    {
        char previous = '{';
        int i;
        for(i = begin; i < end; i++)
        {
            char c = text.charAt(i);
            if(c == '}' && previous != '\\')
                break;
            previous = c;
        }

        if(i == end)
            throw new ParseException("missing '}'");
        else
            return doGetTraversedLink(text, begin, i);
    }

    private static String doGetTraversedLink(String text, int begin, int end)
        throws ParseException
    {
        final StringBuffer buffer = new StringBuffer(end - begin);
        Sink sink = new Sink() {

            public void head()
                throws ParseException
            {
            }

            public void head_()
                throws ParseException
            {
            }

            public void body()
                throws ParseException
            {
            }

            public void body_()
                throws ParseException
            {
            }

            public void section1()
                throws ParseException
            {
            }

            public void section1_()
                throws ParseException
            {
            }

            public void section2()
                throws ParseException
            {
            }

            public void section2_()
                throws ParseException
            {
            }

            public void section3()
                throws ParseException
            {
            }

            public void section3_()
                throws ParseException
            {
            }

            public void section4()
                throws ParseException
            {
            }

            public void section4_()
                throws ParseException
            {
            }

            public void section5()
                throws ParseException
            {
            }

            public void section5_()
                throws ParseException
            {
            }

            public void list()
                throws ParseException
            {
            }

            public void list_()
                throws ParseException
            {
            }

            public void listItem()
                throws ParseException
            {
            }

            public void listItem_()
                throws ParseException
            {
            }

            public void numberedList(int i)
                throws ParseException
            {
            }

            public void numberedList_()
                throws ParseException
            {
            }

            public void numberedListItem()
                throws ParseException
            {
            }

            public void numberedListItem_()
                throws ParseException
            {
            }

            public void definitionList()
                throws ParseException
            {
            }

            public void definitionList_()
                throws ParseException
            {
            }

            public void definitionListItem()
                throws ParseException
            {
            }

            public void definitionListItem_()
                throws ParseException
            {
            }

            public void definition()
                throws ParseException
            {
            }

            public void definition_()
                throws ParseException
            {
            }

            public void figure()
                throws ParseException
            {
            }

            public void figure_()
                throws ParseException
            {
            }

            public void table()
                throws ParseException
            {
            }

            public void table_()
                throws ParseException
            {
            }

            public void tableRows(int ai[], boolean flag)
                throws ParseException
            {
            }

            public void tableRows_()
                throws ParseException
            {
            }

            public void tableRow()
                throws ParseException
            {
            }

            public void tableRow_()
                throws ParseException
            {
            }

            public void title()
                throws ParseException
            {
            }

            public void title_()
                throws ParseException
            {
            }

            public void author()
                throws ParseException
            {
            }

            public void author_()
                throws ParseException
            {
            }

            public void date()
                throws ParseException
            {
            }

            public void date_()
                throws ParseException
            {
            }

            public void sectionTitle()
                throws ParseException
            {
            }

            public void sectionTitle_()
                throws ParseException
            {
            }

            public void paragraph()
                throws ParseException
            {
            }

            public void paragraph_()
                throws ParseException
            {
            }

            public void verbatim(boolean flag)
                throws ParseException
            {
            }

            public void verbatim_()
                throws ParseException
            {
            }

            public void definedTerm()
                throws ParseException
            {
            }

            public void definedTerm_()
                throws ParseException
            {
            }

            public void figureCaption()
                throws ParseException
            {
            }

            public void figureCaption_()
                throws ParseException
            {
            }

            public void tableCell()
                throws ParseException
            {
            }

            public void tableCell_()
                throws ParseException
            {
            }

            public void tableCaption()
                throws ParseException
            {
            }

            public void tableCaption_()
                throws ParseException
            {
            }

            public void figureGraphics(String s)
                throws ParseException
            {
            }

            public void horizontalRule()
                throws ParseException
            {
            }

            public void pageBreak()
                throws ParseException
            {
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
            }

            public void italic_()
                throws ParseException
            {
            }

            public void bold()
                throws ParseException
            {
            }

            public void bold_()
                throws ParseException
            {
            }

            public void monospaced()
                throws ParseException
            {
            }

            public void monospaced_()
                throws ParseException
            {
            }

            public void lineBreak()
                throws ParseException
            {
                buffer.append(' ');
            }

            public void nonBreakingSpace()
                throws ParseException
            {
                buffer.append(' ');
            }

            public void text(String text)
                throws ParseException
            {
                buffer.append(text);
            }

        }
;
        doTraverseText(text, begin, end, sink);
        return buffer.toString().trim();
    }

    public static final int JUSTIFY_CENTER = 0;
    public static final int JUSTIFY_LEFT = 1;
    public static final int JUSTIFY_RIGHT = 2;
    public static final int TAB_WIDTH = 8;
    private Source source;
    private Sink sink;
    private String line;
    private Block block;
    private String blockFileName;
    private int blockLineNumber;
    private static final int TITLE = 0;
    private static final int SECTION1 = 1;
    private static final int SECTION2 = 2;
    private static final int SECTION3 = 3;
    private static final int SECTION4 = 4;
    private static final int SECTION5 = 5;
    private static final int PARAGRAPH = 6;
    private static final int VERBATIM = 7;
    private static final int FIGURE = 8;
    private static final int TABLE = 9;
    private static final int LIST_ITEM = 10;
    private static final int NUMBERED_LIST_ITEM = 11;
    private static final int DEFINITION_LIST_ITEM = 12;
    private static final int HORIZONTAL_RULE = 13;
    private static final int PAGE_BREAK = 14;
    private static final int LIST_BREAK = 15;
    private static final String typeNames[] = {
        "TITLE", "SECTION1", "SECTION2", "SECTION3", "SECTION4", "SECTION5", "PARAGRAPH", "VERBATIM", "FIGURE", "TABLE", 
        "LIST_ITEM", "NUMBERED_LIST_ITEM", "DEFINITION_LIST_ITEM", "HORIZONTAL_RULE", "PAGE_BREAK", "LIST_BREAK"
    };
    private static final char spaces[] = {
        ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 
        ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 
        ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 
        ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 
        ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 
        ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 
        ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 
        ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', 
        ' ', ' ', ' ', ' ', ' '
    };








}

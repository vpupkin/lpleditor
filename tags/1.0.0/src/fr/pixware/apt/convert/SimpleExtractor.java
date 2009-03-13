// 
// 
// 
// Source File Name:   SimpleExtractor.java

package fr.pixware.apt.convert;

import java.io.*;

// Referenced classes of package fr.pixware.apt.convert:
//            Extractor, Driver

public class SimpleExtractor
    implements Extractor
{

    public SimpleExtractor(Driver driver, String beginComments, String commentLinePrefix, String endComments)
    {
        this(driver, new String[] {
            beginComments
        }, new String[] {
            commentLinePrefix
        }, new String[] {
            endComments
        });
    }

    public SimpleExtractor(Driver driver, String beginComments[], String commentLinePrefix[], String endComments[])
    {
        part1 = new StringBuffer();
        part2 = new StringBuffer();
        code = new StringBuffer();
        if(beginComments.length != commentLinePrefix.length || commentLinePrefix.length != endComments.length)
            throw new IllegalArgumentException("all arrays must have the same length");
        this.driver = driver;
        this.beginComments = beginComments;
        this.commentLinePrefix = commentLinePrefix;
        this.endComments = endComments;
        commentLinePrefixLength = new int[commentLinePrefix.length];
        for(int i = 0; i < commentLinePrefix.length; i++)
            commentLinePrefixLength[i] = commentLinePrefix[i].length();

    }

    public Driver getDriver()
    {
        return driver;
    }

    public String getExtractorInfo()
    {
        StringBuffer info = new StringBuffer();
        info.append("Extracts the APT document from comments resembling:\n");
        for(int i = 0; i < beginComments.length; i++)
        {
            info.append(beginComments[i] + "\n");
            info.append(commentLinePrefix[i] + " APT source\n");
            info.append(endComments[i] + "\n\n");
        }

        return info.toString();
    }

    public void extract(String inFileName, String outFileName)
        throws Exception
    {
        Reader in = new FileReader(inFileName);
        Writer out = new FileWriter(outFileName);
        try
        {
            extract(in, out);
        }
        finally
        {
            in.close();
            out.close();
        }
    }

    public void extract(Reader reader, Writer writer)
        throws IOException
    {
        LineNumberReader in = new LineNumberReader(reader);
        BufferedWriter out = new BufferedWriter(writer);
        resetState();
        String s;
        while((s = in.readLine()) != null) 
        {
            String line = unindentLine(s);
label0:
            switch(state)
            {
            case 1: // '\001'
            case 2: // '\002'
                if(line.indexOf(endComments[pattern]) == 0 && !endComments[pattern].equals(commentLinePrefix[pattern]))
                {
                    state = 3;
                    break;
                }
                if(commentLinePrefixLength[pattern] == 0)
                    line = s;
                else
                if(line.indexOf(commentLinePrefix[pattern]) == 0)
                {
                    line = line.substring(commentLinePrefixLength[pattern]);
                } else
                {
                    state = 3;
                    if(line.indexOf(endComments[pattern]) == 0)
                        break;
                }
                if(state == 1)
                {
                    int indent = line.indexOf("~~x");
                    if(indent >= 0)
                    {
                        codeAfterPart1 = true;
                        state = 2;
                    } else
                    {
                        part1.append(line);
                        part1.append('\n');
                    }
                    break;
                }
                if(state == 2)
                {
                    part2.append(line);
                    part2.append('\n');
                    break;
                }
                // fall through

            case 3: // '\003'
                if(line.length() > 0)
                    extractCode(s, in, code);
                writeParts(out);
                resetState();
                break;

            default:
                for(int i = 0; i < beginComments.length; i++)
                    if(line.indexOf(beginComments[i]) == 0)
                    {
                        if(line.trim().equals(beginComments[i]))
                        {
                            state = 1;
                        } else
                        {
                            int from = beginComments[i].length();
                            int to = line.indexOf(endComments[i], from);
                            if(to < 0)
                                to = line.length();
                            part1.append(line.substring(from, to));
                            part1.append('\n');
                            state = 3;
                        }
                        pattern = i;
                        break label0;
                    }

                break;
            }
        }
        if(state != 0)
        {
            writeParts(out);
            resetState();
        }
        out.newLine();
        out.flush();
    }

    private void resetState()
    {
        state = 0;
        pattern = -1;
        codeAfterPart1 = false;
        part1.setLength(0);
        part2.setLength(0);
        code.setLength(0);
    }

    private void writeParts(BufferedWriter out)
        throws IOException
    {
        if(codeAfterPart1)
        {
            if(part1.length() > 0)
            {
                out.newLine();
                out.write(part1.toString());
            }
            if(code.length() > 0)
            {
                out.newLine();
                out.write(code.toString());
            }
        } else
        {
            if(code.length() > 0)
            {
                out.newLine();
                out.write(code.toString());
            }
            if(part1.length() > 0)
            {
                out.newLine();
                out.write(part1.toString());
            }
        }
        if(part2.length() > 0)
        {
            out.newLine();
            out.write(part2.toString());
        }
    }

    protected void extractCode(String line, LineNumberReader in, StringBuffer code)
        throws IOException
    {
        code.append(" []\n");
        String box = isVerbatimBox() ? "+--+\n" : "---\n";
        code.append(box);
        line = expandTabs(line);
        int unindent = lineIndent(line);
        code.append(line.substring(unindent));
        code.append('\n');
        while((line = in.readLine()) != null) 
        {
            line = expandTabs(line);
            int indent = lineIndent(line);
            if(indent == line.length())
                break;
            code.append(line.substring(Math.min(unindent, indent)));
            code.append('\n');
        }
        code.append(box);
    }

    protected boolean isVerbatimBox()
    {
        return false;
    }

    protected static String expandTabs(String line)
    {
        return expandTabs(line, 8);
    }

    protected static String expandTabs(String line, int tabWidth)
    {
        StringBuffer expanded = new StringBuffer();
        int length = line.length();
        int column = 0;
        for(int i = 0; i < length; i++)
        {
            char c = line.charAt(i);
            if(c == '\t')
            {
                int j = column;
                for(column = (((column + 1 + tabWidth) - 1) / tabWidth) * tabWidth; j < column; j++)
                    expanded.append(' ');

            } else
            {
                column++;
                expanded.append(c);
            }
        }

        return expanded.toString();
    }

    protected static int lineIndent(String line)
    {
        int indent = 0;
        int length = line.length();
        for(int i = 0; i < length; i++)
        {
            if(!Character.isWhitespace(line.charAt(i)))
                break;
            indent++;
        }

        return indent;
    }

    protected static String unindentLine(String line)
    {
        int indent = lineIndent(line);
        return indent <= 0 ? line : line.substring(indent);
    }

    private Driver driver;
    private String beginComments[];
    private String commentLinePrefix[];
    private String endComments[];
    private int commentLinePrefixLength[];
    private static final int SKIP = 0;
    private static final int PART1 = 1;
    private static final int PART2 = 2;
    private static final int CODE = 3;
    private int state;
    private int pattern;
    private boolean codeAfterPart1;
    private StringBuffer part1;
    private StringBuffer part2;
    private StringBuffer code;
}

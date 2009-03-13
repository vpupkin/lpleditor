// 
// 
// 
// Source File Name:   LineBreaker.java

package fr.pixware.apt.convert;

import fr.pixware.apt.parse.ParseException;
import java.io.*;

public class LineBreaker
{

    public LineBreaker(Writer out)
    {
        this(out, 78);
    }

    public LineBreaker(Writer out, int maxLineLength)
    {
        lineLength = 0;
        word = new StringBuffer(1024);
        if(maxLineLength <= 0)
        {
            throw new IllegalArgumentException("maxLineLength<=0");
        } else
        {
            destination = out;
            this.maxLineLength = maxLineLength;
            writer = new BufferedWriter(out);
            return;
        }
    }

    public Writer getDestination()
    {
        return destination;
    }

    public void write(String text)
        throws ParseException
    {
        write(text, false);
    }

    public void write(String text, boolean preserveSpace)
        throws ParseException
    {
        try
        {
            int length = text.length();
            for(int i = 0; i < length; i++)
            {
                char c = text.charAt(i);
                switch(c)
                {
                case 32: // ' '
                    if(preserveSpace)
                        word.append(c);
                    else
                        writeWord();
                    break;

                case 10: // '\n'
                    writeWord();
                    writer.write(10);
                    lineLength = 0;
                    break;

                default:
                    word.append(c);
                    break;
                }
            }

        }
        catch(IOException e)
        {
            throw new ParseException(e);
        }
    }

    public void flush()
        throws ParseException
    {
        try
        {
            writeWord();
            writer.flush();
        }
        catch(IOException e)
        {
            throw new ParseException(e);
        }
    }

    private void writeWord()
        throws IOException
    {
        int length = word.length();
        if(length > 0)
        {
            if(lineLength > 0)
                if(lineLength + 1 + length > maxLineLength)
                {
                    writer.write(10);
                    lineLength = 0;
                } else
                {
                    writer.write(32);
                    lineLength++;
                }
            writer.write(word.toString());
            word.setLength(0);
            lineLength += length;
        }
    }

    public static final int DEFAULT_MAX_LINE_LENGTH = 78;
    private Writer destination;
    private BufferedWriter writer;
    private int maxLineLength;
    private int lineLength;
    private StringBuffer word;
}

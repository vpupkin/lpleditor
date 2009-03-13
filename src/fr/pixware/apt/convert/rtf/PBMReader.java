// 
// 
// 
// Source File Name:   PBMReader.java

package fr.pixware.apt.convert.rtf;

import java.io.*;

public class PBMReader
{
    private class HeaderReader
    {

        int read(String fileName)
            throws Exception
        {
            reader = new BufferedReader(new FileReader(fileName));
            offset = 0;
            String field = getField();
            if(field.length() != 2 || field.charAt(0) != 'P')
            {
                reader.close();
                throw new Exception("bad file format");
            }
            switch(field.charAt(1))
            {
            case 49: // '1'
            case 52: // '4'
                type = 1;
                break;

            case 50: // '2'
            case 53: // '5'
                type = 2;
                break;

            case 51: // '3'
            case 54: // '6'
                type = 3;
                break;

            default:
                reader.close();
                throw new Exception("bad file format");
            }
            if(field.charAt(1) > '3')
                binary = true;
            else
                binary = false;
            try
            {
                width = Integer.parseInt(getField());
                height = Integer.parseInt(getField());
                if(type == 1)
                    maxValue = 1;
                else
                    maxValue = Integer.parseInt(getField());
            }
            catch(NumberFormatException numberformatexception)
            {
                reader.close();
                throw new Exception("bad file format");
            }
            reader.close();
            return offset;
        }

        private String getField()
            throws IOException
        {
            StringBuffer field = new StringBuffer();
            try
            {
                char c;
                do
                    while((c = getChar()) == '#') 
                        skipComment();
                while(Character.isWhitespace(c));
                field.append(c);
                for(; !Character.isWhitespace(c = getChar()); field.append(c))
                {
                    if(c != '#')
                        continue;
                    skipComment();
                    break;
                }

            }
            catch(EOFException eofexception) { }
            return field.toString();
        }

        private char getChar()
            throws IOException, EOFException
        {
            int c = reader.read();
            if(c < 0)
            {
                throw new EOFException();
            } else
            {
                offset++;
                return (char)c;
            }
        }

        private void skipComment()
            throws IOException
        {
            try
            {
                while(getChar() != '\n') ;
            }
            catch(EOFException eofexception) { }
        }

        private Reader reader;
        private int offset;

        private HeaderReader()
        {
        }

    }


    public PBMReader(String fileName)
        throws Exception
    {
        HeaderReader header = new HeaderReader();
        int length = header.read(fileName);
        if(type != 3)
            throw new Exception("unsupported file type");
        if(!binary)
            throw new Exception("unsupported data format");
        if(maxValue > 255)
            throw new Exception("unsupported color depth");
        switch(type)
        {
        case 1: // '\001'
            bytesPerLine = (width + 7) / 8;
            break;

        case 2: // '\002'
            bytesPerLine = width;
            break;

        case 3: // '\003'
            bytesPerLine = 3 * width;
            break;
        }
        stream = new BufferedInputStream(new FileInputStream(fileName));
        skip(length);
    }

    public int type()
    {
        return type;
    }

    public int width()
    {
        return width;
    }

    public int height()
    {
        return height;
    }

    public int maxValue()
    {
        return maxValue;
    }

    public int bytesPerLine()
    {
        return bytesPerLine;
    }

    public long skip(long count)
        throws IOException
    {
        long skipped = stream.skip(count);
        if(skipped < count)
        {
            byte b[] = new byte[512];
            int n;
            for(; skipped < count; skipped += n)
            {
                int len = (int)Math.min(b.length, count - skipped);
                n = stream.read(b, 0, len);
                if(n < 0)
                    break;
            }

        }
        return skipped;
    }

    public int read(byte b[], int off, int len)
        throws IOException
    {
        int count;
        int n;
        for(count = 0; count < len; count += n)
        {
            n = stream.read(b, off + count, len - count);
            if(n < 0)
                break;
        }

        return count;
    }

    public static void main(String args[])
        throws Exception
    {
        PBMReader pbm = new PBMReader(args[0]);
    }

    public static final int TYPE_PBM = 1;
    public static final int TYPE_PGM = 2;
    public static final int TYPE_PPM = 3;
    private static final boolean TRACE = false;
    private static final String BAD_FILE_FORMAT = "bad file format";
    private static final String UNSUPPORTED_TYPE = "unsupported file type";
    private static final String UNSUPPORTED_FORMAT = "unsupported data format";
    private static final String UNSUPPORTED_DEPTH = "unsupported color depth";
    private int type;
    private boolean binary;
    private int width;
    private int height;
    private int maxValue;
    private int bytesPerLine;
    private InputStream stream;






}

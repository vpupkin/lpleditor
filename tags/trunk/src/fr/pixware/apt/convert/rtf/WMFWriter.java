// 
// 
// 
// Source File Name:   WMFWriter.java

package fr.pixware.apt.convert.rtf;

import java.io.*;
import java.util.Vector;

public class WMFWriter
{
    public static class DibBitBltRecord extends Record
    {

        public void write(OutputStream out)
            throws IOException
        {
            super.write(out);
            dib.write(out);
        }

        public void print(Writer out)
            throws IOException
        {
            super.print(out);
            dib.print(out);
        }

        public static final int P_COUNT = 8;
        public static final int P_ROP_L = 0;
        public static final int P_ROP_H = 1;
        public static final int P_YSRC = 2;
        public static final int P_XSRC = 3;
        public static final int P_HEIGHT = 4;
        public static final int P_WIDTH = 5;
        public static final int P_YDST = 6;
        public static final int P_XDST = 7;
        private Dib dib;

        public DibBitBltRecord(int parameters[], Dib dib)
        {
            super(2368, parameters);
            super.size += dib.size();
            this.dib = dib;
        }
    }

    public static class Dib
    {

        public int size()
        {
            int size = 40;
            if(palette != null)
                size += palette.length;
            if(bitmap != null)
                if(biSizeImage != 0)
                    size += biSizeImage;
                else
                    size += bitmap.length;
            return size / 2;
        }

        public void write(OutputStream out)
            throws IOException
        {
            WMFWriter.write32(40, out);
            WMFWriter.write32(biWidth, out);
            WMFWriter.write32(biHeight, out);
            WMFWriter.write16(1, out);
            WMFWriter.write16(biBitCount, out);
            WMFWriter.write32(biCompression, out);
            WMFWriter.write32(biSizeImage, out);
            WMFWriter.write32(biXPelsPerMeter, out);
            WMFWriter.write32(biYPelsPerMeter, out);
            WMFWriter.write32(biClrUsed, out);
            WMFWriter.write32(biClrImportant, out);
            if(palette != null)
                out.write(palette);
            if(bitmap != null)
                if(biSizeImage != 0)
                    out.write(bitmap, 0, biSizeImage);
                else
                    out.write(bitmap);
        }

        public void print(Writer out)
            throws IOException
        {
            String ls = System.getProperty("line.separator");
            WMFWriter.print32(40, out);
            WMFWriter.print32(biWidth, out);
            WMFWriter.print32(biHeight, out);
            WMFWriter.print16(1, out);
            WMFWriter.print16(biBitCount, out);
            out.write(ls);
            WMFWriter.print32(biCompression, out);
            WMFWriter.print32(biSizeImage, out);
            WMFWriter.print32(biXPelsPerMeter, out);
            WMFWriter.print32(biYPelsPerMeter, out);
            WMFWriter.print32(biClrUsed, out);
            WMFWriter.print32(biClrImportant, out);
            out.write(ls);
            if(palette != null)
                WMFWriter.print(palette, 0, palette.length, out, 64);
            if(bitmap != null)
            {
                int len = biSizeImage == 0 ? bitmap.length : biSizeImage;
                WMFWriter.print(bitmap, 0, len, out, 76);
            }
        }

        public static int rlEncode8(byte inBuf[], int inOff, int inLen, byte outBuf[], int outOff)
        {
            int i1 = inOff;
            int j = outOff;
            for(int n = inOff + inLen; i1 < n;)
            {
                int i2 = i1 + 1;
                int len;
                for(len = 1; i2 < n; len++)
                {
                    if(inBuf[i2] != inBuf[i2 - 1])
                        break;
                    i2++;
                }

                if(len > 1)
                {
                    for(; len > 255; len -= 255)
                    {
                        outBuf[j++] = -1;
                        outBuf[j++] = inBuf[i1];
                    }

                    if(len > 0)
                    {
                        outBuf[j++] = (byte)len;
                        outBuf[j++] = inBuf[i1];
                    }
                    i1 = i2;
                } else
                {
                    for(i2++; i2 < n;)
                    {
                        if(inBuf[i2] == inBuf[i2 - 1])
                            break;
                        i2++;
                        len++;
                    }

                    for(; len > 255; len -= 255)
                    {
                        outBuf[j++] = 0;
                        outBuf[j++] = -1;
                        for(int k = 0; k < 255; k++)
                            outBuf[j++] = inBuf[i1++];

                        outBuf[j++] = 0;
                    }

                    if(len > 2)
                    {
                        outBuf[j++] = 0;
                        outBuf[j++] = (byte)len;
                        for(int k = 0; k < len; k++)
                            outBuf[j++] = inBuf[i1++];

                        if(len % 2 != 0)
                            outBuf[j++] = 0;
                    } else
                    {
                        for(; len > 0; len--)
                        {
                            outBuf[j++] = 1;
                            outBuf[j++] = inBuf[i1++];
                        }

                    }
                }
            }

            return j - outOff;
        }

        public static final int BI_RGB = 0;
        public static final int BI_RLE8 = 1;
        public static final int BI_RLE4 = 2;
        public static final int BI_BITFIELDS = 3;
        public final int biSize = 40;
        public int biWidth;
        public int biHeight;
        public final short biPlanes = 1;
        public short biBitCount;
        public int biCompression;
        public int biSizeImage;
        public int biXPelsPerMeter;
        public int biYPelsPerMeter;
        public int biClrUsed;
        public int biClrImportant;
        public byte palette[];
        public byte bitmap[];

        public Dib()
        {
        }
    }

    public static class Record
    {

        public int size()
        {
            return size;
        }

        public void write(OutputStream out)
            throws IOException
        {
            WMFWriter.write32(size, out);
            WMFWriter.write16(function, out);
            if(parameters != null)
            {
                for(int i = 0; i < parameters.length; i++)
                    WMFWriter.write16(parameters[i], out);

            }
        }

        public void print(Writer out)
            throws IOException
        {
            WMFWriter.print32(size, out);
            WMFWriter.print16(function, out);
            if(parameters != null)
            {
                for(int i = 0; i < parameters.length; i++)
                    WMFWriter.print16(parameters[i], out);

            }
        }

        protected int size;
        private short function;
        private short parameters[];

        public Record(int function, int parameters[])
        {
            this.function = (short)function;
            if(parameters != null)
            {
                this.parameters = new short[parameters.length];
                for(int i = 0; i < parameters.length; i++)
                    this.parameters[i] = (short)parameters[i];

            }
            size = 3 + (parameters != null ? parameters.length : 0);
        }
    }


    public WMFWriter()
    {
        fileType = 2;
        headerSize = 9;
        version = 768;
        fileSize = headerSize + trailer.size();
        numOfObjects = 0;
        maxRecordSize = trailer.size();
        numOfParams = 0;
        records = new Vector();
    }

    public void add(Record record)
    {
        records.addElement(record);
        int size = record.size();
        fileSize += size;
        if(size > maxRecordSize)
            maxRecordSize = size;
    }

    public int size()
    {
        return fileSize;
    }

    public void write(String fileName)
        throws IOException
    {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fileName));
        write(((OutputStream) (out)));
        out.flush();
        out.close();
    }

    public void write(OutputStream out)
        throws IOException
    {
        write16(fileType, out);
        write16(headerSize, out);
        write16(version, out);
        write32(fileSize, out);
        write16(numOfObjects, out);
        write32(maxRecordSize, out);
        write16(numOfParams, out);
        int i = 0;
        for(int n = records.size(); i < n; i++)
        {
            Record record = (Record)records.elementAt(i);
            record.write(out);
        }

        trailer.write(out);
    }

    public static void write16(int word, OutputStream out)
        throws IOException
    {
        out.write(word);
        out.write(word >> 8);
    }

    public static void write32(int dword, OutputStream out)
        throws IOException
    {
        out.write(dword);
        out.write(dword >> 8);
        out.write(dword >> 16);
        out.write(dword >> 24);
    }

    public void print(Writer out)
        throws IOException
    {
        print16(fileType, out);
        print16(headerSize, out);
        print16(version, out);
        print32(fileSize, out);
        print16(numOfObjects, out);
        print32(maxRecordSize, out);
        print16(numOfParams, out);
        out.write(System.getProperty("line.separator"));
        int i = 0;
        for(int n = records.size(); i < n; i++)
        {
            Record record = (Record)records.elementAt(i);
            record.print(out);
        }

        trailer.print(out);
    }

    public static void print16(int word, Writer out)
        throws IOException
    {
        byte buf[] = new byte[2];
        buf[0] = (byte)word;
        buf[1] = (byte)(word >> 8);
        print(buf, 0, 2, out);
    }

    public static void print32(int dword, Writer out)
        throws IOException
    {
        byte buf[] = new byte[4];
        buf[0] = (byte)dword;
        buf[1] = (byte)(dword >> 8);
        buf[2] = (byte)(dword >> 16);
        buf[3] = (byte)(dword >> 24);
        print(buf, 0, 4, out);
    }

    public static void print(byte buf[], int off, int len, Writer out)
        throws IOException
    {
        char cbuf[] = new char[2 * len];
        int i = off;
        int j = 0;
        for(int n = off + len; i < n; i++)
        {
            int d = buf[i] >> 4 & 0xf;
            if(d < 10)
                cbuf[j++] = (char)(48 + d);
            else
                cbuf[j++] = (char)(97 + (d - 10));
            d = buf[i] & 0xf;
            if(d < 10)
                cbuf[j++] = (char)(48 + d);
            else
                cbuf[j++] = (char)(97 + (d - 10));
        }

        out.write(cbuf);
    }

    public static void print(byte buf[], int off, int len, Writer out, int lw)
        throws IOException
    {
        String ls = System.getProperty("line.separator");
        int n;
        for(int i = off; len > 0; i += n)
        {
            n = Math.min(len, lw / 2);
            print(buf, i, n, out);
            out.write(ls);
            len -= n;
        }

    }

    private static Record trailer = new Record(0, null);
    private short fileType;
    private short headerSize;
    private short version;
    private int fileSize;
    private short numOfObjects;
    private int maxRecordSize;
    private short numOfParams;
    private Vector records;

}

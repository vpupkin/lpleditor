// 
// 
// 
// Source File Name:   FileUtil.java

package fr.pixware.util;

import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.zip.GZIPInputStream;

public class FileUtil
{

    public FileUtil()
    {
    }

    public static File urlToFile(URL url)
    {
        if(!url.getProtocol().equals("file"))
            return null;
        String fileName = url.getFile();
        if(File.separatorChar != '/')
            fileName = fileName.replace('/', File.separatorChar);
        return new File(fileName);
    }

    public static File urlToFile(String urlName)
    {
        URL url;
        try
        {
            url = new URL(urlName);
        }
        catch(MalformedURLException malformedurlexception)
        {
            url = null;
        }
        return url != null ? urlToFile(url) : null;
    }

    public static String urlToFileName(String urlName)
    {
        File file = urlToFile(urlName);
        return file != null ? file.getAbsolutePath() : null;
    }

    public static URL fileToURL(File file)
    {
        String fileName = file.getAbsolutePath();
        if(File.separatorChar != '/')
            fileName = fileName.replace(File.separatorChar, '/');
        if(!fileName.startsWith("/"))
            fileName = "/" + fileName;
        if(!fileName.endsWith("/") && file.isDirectory())
            fileName = fileName + "/";
        URL url;
        try
        {
            url = new URL("file", "", -1, fileName);
        }
        catch(MalformedURLException malformedurlexception)
        {
            url = null;
        }
        return url;
    }

    public static URL fileToURL(String fileName)
    {
        return fileToURL(new File(fileName));
    }

    public static String fileToURLName(String fileName)
    {
        URL url = fileToURL(fileName);
        return url != null ? url.toExternalForm() : null;
    }

    public static String fileDirName(String fileName)
    {
        char separ = File.separatorChar;
        int slash = fileName.lastIndexOf(separ);
        if(slash < 0 && separ == '\\')
        {
            separ = '/';
            slash = fileName.lastIndexOf(separ);
        }
        String name;
        if(slash < 0)
            name = ".";
        else
        if(slash == 0)
        {
            name = File.separator;
        } else
        {
            name = fileName.substring(0, slash);
            if(separ != File.separatorChar)
                name = name.replace(separ, File.separatorChar);
        }
        return name;
    }

    public static String fileBaseName(String fileName)
    {
        int slash = fileName.lastIndexOf(File.separatorChar);
        if(slash < 0 && File.separatorChar == '\\')
            slash = fileName.lastIndexOf('/');
        String name;
        if(slash < 0)
            name = fileName;
        else
            name = fileName.substring(slash + 1);
        return name;
    }

    public static String fileExtension(String fileName)
    {
        int slash = fileName.lastIndexOf(File.separatorChar);
        if(slash < 0 && File.separatorChar == '\\')
            slash = fileName.lastIndexOf('/');
        if(slash < 0)
            slash = 0;
        else
            slash++;
        int dot = fileName.lastIndexOf('.');
        if(dot <= slash)
            return "";
        else
            return fileName.substring(dot + 1);
    }

    public static String trimFileExtension(String fileName)
    {
        int slash = fileName.lastIndexOf(File.separatorChar);
        if(slash < 0 && File.separatorChar == '\\')
            slash = fileName.lastIndexOf('/');
        if(slash < 0)
            slash = 0;
        else
            slash++;
        int dot = fileName.lastIndexOf('.');
        if(dot <= slash)
            return fileName;
        else
            return fileName.substring(0, dot);
    }

    public static void main(String args[])
    {
        for(int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            System.out.println("'" + fileDirName(arg) + "'");
            System.out.println("'" + fileBaseName(arg) + "'");
            System.out.println("'" + trimFileExtension(arg) + "'");
            System.out.println("'" + fileExtension(arg) + "'");
            System.out.println("'" + fileToURLName(arg) + "'");
            System.out.println("---");
        }

    }

    public static boolean removeFile(String fileName)
    {
        return removeFile(fileName, false);
    }

    public static boolean removeFile(String fileName, boolean force)
    {
        return removeFile(new File(fileName), force);
    }

    public static boolean removeFile(File file, boolean force)
    {
        if(file.isDirectory() && force)
            emptyDirectory(file);
        return file.delete();
    }

    public static void emptyDirectory(String dirName)
    {
        emptyDirectory(new File(dirName));
    }

    public static void emptyDirectory(File dir)
    {
        String children[] = dir.list();
        for(int i = 0; i < children.length; i++)
        {
            File child = new File(dir, children[i]);
            if(child.isDirectory())
                removeFile(child, true);
            else
                child.delete();
        }

    }

    public static void copyFile(String srcFileName, String dstFileName)
        throws IOException
    {
        FileInputStream src = new FileInputStream(srcFileName);
        FileOutputStream dst = new FileOutputStream(dstFileName);
        byte bytes[] = new byte[8192];
        int i;
        while((i = src.read(bytes)) != -1) 
            dst.write(bytes, 0, i);
        src.close();
        dst.flush();
        dst.close();
    }

    public static String loadString(String fileName)
        throws FileNotFoundException, IOException
    {
        return loadString(((InputStream) (new FileInputStream(fileName))));
    }

    public static String loadString(URL url)
        throws IOException
    {
        URLConnection connection = url.openConnection();
        connection.setDefaultUseCaches(false);
        connection.setUseCaches(false);
        connection.setIfModifiedSince(0L);
        return loadString(connection.getInputStream());
    }

    public static String loadString(InputStream stream)
        throws IOException
    {
        InputStreamReader in = new InputStreamReader(stream);
        char chars[] = new char[8192];
        StringBuffer buffer = new StringBuffer(chars.length);
        int i;
        try
        {
            while((i = in.read(chars, 0, chars.length)) != -1) 
                if(i > 0)
                    buffer.append(chars, 0, i);
        }
        finally
        {
            in.close();
        }
        return buffer.toString();
    }

    public static void saveString(String string, String fileName)
        throws IOException
    {
        saveString(string, ((OutputStream) (new FileOutputStream(fileName))));
    }

    public static void saveString(String string, OutputStream stream)
        throws IOException
    {
        OutputStreamWriter out = new OutputStreamWriter(stream);
        out.write(string, 0, string.length());
        out.flush();
        out.close();
    }

    public static byte[] loadBytes(String fileName)
        throws IOException
    {
        return loadBytes(((InputStream) (new FileInputStream(fileName))));
    }

    public static byte[] loadBytes(URL url)
        throws IOException
    {
        URLConnection connection = url.openConnection();
        connection.setDefaultUseCaches(false);
        connection.setUseCaches(false);
        connection.setIfModifiedSince(0L);
        return loadBytes(connection.getInputStream());
    }

    public static byte[] loadBytes(InputStream source)
        throws IOException
    {
        BufferedInputStream in = new BufferedInputStream(source);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte bytes[] = new byte[8192];
        int i;
        while((i = in.read(bytes)) >= 0) 
            out.write(bytes, 0, i);
        return out.toByteArray();
    }

    public static boolean isGzipped(String fileName)
        throws IOException
    {
        InputStream in = new FileInputStream(fileName);
        int magic1 = in.read();
        int magic2 = in.read();
        in.close();
        return magic1 == 31 && magic2 == 139;
    }

    public static String loadGzippedString(String fileName)
        throws IOException
    {
        return loadGzippedString(((InputStream) (new FileInputStream(fileName))), null);
    }

    public static String loadGzippedString(URL url)
        throws IOException
    {
        return loadGzippedString(url.openStream(), null);
    }

    public static String loadGzippedString(InputStream source, String encoding)
        throws IOException
    {
        if(encoding == null)
            encoding = defaultEncoding();
        Reader in = new InputStreamReader(new GZIPInputStream(source), encoding);
        char chars[] = new char[8192];
        StringBuffer buffer = new StringBuffer(chars.length);
        int i;
        try
        {
            while((i = in.read(chars, 0, chars.length)) != -1) 
                if(i > 0)
                    buffer.append(chars, 0, i);
        }
        finally
        {
            in.close();
        }
        return buffer.toString();
    }

    public static String defaultEncoding()
    {
        return platformDefaultEncoding;
    }

    public static boolean isKnownEncoding(String name)
    {
        return knownEncodings.containsKey(name);
    }

    public static final String KNOWN_ENCODINGS[] = {
        "ASCII", "Cp1252", "ISO8859_1", "UnicodeBig", "UnicodeBigUnmarked", "UnicodeLittle", "UnicodeLittleUnmarked", "UTF8", "UTF-16", "Big5", 
        "Cp037", "Cp273", "Cp277", "Cp278", "Cp280", "Cp284", "Cp285", "Cp297", "Cp420", "Cp424", 
        "Cp437", "Cp500", "Cp737", "Cp775", "Cp838", "Cp850", "Cp852", "Cp855", "Cp856", "Cp857", 
        "Cp858", "Cp860", "Cp861", "Cp862", "Cp863", "Cp864", "Cp865", "Cp866", "Cp868", "Cp869", 
        "Cp870", "Cp871", "Cp874", "Cp875", "Cp918", "Cp921", "Cp922", "Cp930", "Cp933", "Cp935", 
        "Cp937", "Cp939", "Cp942", "Cp942C", "Cp943", "Cp943C", "Cp948", "Cp949", "Cp949C", "Cp950", 
        "Cp964", "Cp970", "Cp1006", "Cp1025", "Cp1026", "Cp1046", "Cp1097", "Cp1098", "Cp1112", "Cp1122", 
        "Cp1123", "Cp1124", "Cp1140", "Cp1141", "Cp1142", "Cp1143", "Cp1144", "Cp1145", "Cp1146", "Cp1147", 
        "Cp1148", "Cp1149", "Cp1250", "Cp1251", "Cp1253", "Cp1254", "Cp1255", "Cp1256", "Cp1257", "Cp1258", 
        "Cp1381", "Cp1383", "Cp33722", "EUC_CN", "EUC_JP", "EUC_KR", "EUC_TW", "GBK", "ISO2022CN", "ISO2022CN_CNS", 
        "ISO2022CN_GB", "ISO2022JP", "ISO2022KR", "ISO8859_2", "ISO8859_3", "ISO8859_4", "ISO8859_5", "ISO8859_6", "ISO8859_7", "ISO8859_8", 
        "ISO8859_9", "ISO8859_13", "ISO8859_15_FDIS", "JIS0201", "JIS0208", "JIS0212", "JISAutoDetect", "Johab", "KOI8_R", "MS874", 
        "MS932", "MS936", "MS949", "MS950", "MacArabic", "MacCentralEurope", "MacCroatian", "MacCyrillic", "MacDingbat", "MacGreek", 
        "MacHebrew", "MacIceland", "MacRoman", "MacRomania", "MacSymbol", "MacThai", "MacTurkish", "MacUkraine", "SJIS", "TIS620"
    };
    private static Hashtable knownEncodings;
    private static String platformDefaultEncoding;

    static 
    {
        knownEncodings = new Hashtable();
        for(int i = 0; i < KNOWN_ENCODINGS.length; i++)
        {
            String name = KNOWN_ENCODINGS[i];
            knownEncodings.put(name, name);
        }

        platformDefaultEncoding = (new OutputStreamWriter(System.out)).getEncoding();
    }
}

// 
// 
// 
// Source File Name:   PlatformUtil.java

package fr.pixware.util;

import java.io.*;

public class PlatformUtil
{
    private static class InputConsumer extends Thread
    {

        public void run()
        {
            while(true) 
            {
                int count;
                try
                {
                    count = in.read(buffer);
                }
                catch(IOException ioexception)
                {
                    count = -1;
                }
                if(count < 0)
                    break;
                if(count > 0 && echo != null)
                    try
                    {
                        echo.write(buffer, 0, count);
                    }
                    catch(IOException ioexception1)
                    {
                        echo = null;
                    }
            }
        }

        private InputStream in;
        private OutputStream echo;
        private byte buffer[];

        public InputConsumer(InputStream in, OutputStream echo)
        {
            buffer = new byte[4096];
            this.in = in;
            this.echo = echo;
        }
    }


    public PlatformUtil()
    {
    }

    public static String platform()
    {
        if(File.pathSeparatorChar == ';')
            return "windows";
        else
            return "unix";
    }

    public static String homeDir()
    {
        String homeDir = System.getProperty("HOME");
        if(homeDir == null || homeDir.length() == 0)
            homeDir = System.getProperty("user.home");
        if(!(new File(homeDir)).isDirectory())
            homeDir = null;
        return homeDir;
    }

    public static String rcFileName(String appName)
    {
        String homeDir = homeDir();
        if(homeDir == null)
            return null;
        String rcFileName;
        if(platform().equals("windows"))
            rcFileName = homeDir + File.separatorChar + appName + ".ini";
        else
            rcFileName = homeDir + File.separatorChar + "." + appName;
        return rcFileName;
    }

    public static String tmpFileName()
    {
        return tmpFileName(".tmp");
    }

    public static String tmpFileName(String extension)
    {
        String baseName = Long.toString(System.currentTimeMillis(), 36) + extension;
        String tmpDir = tmpDir();
        String tmpFile;
        if(tmpDir == null)
            tmpFile = baseName;
        else
            tmpFile = (new File(tmpDir, baseName)).getPath();
        return tmpFile;
    }

    public static String tmpDir()
    {
        boolean windows = platform().equals("windows");
        String tmpDir;
        if(windows)
        {
            tmpDir = System.getProperty("TMP");
            if(tmpDir == null || tmpDir.length() == 0)
            {
                tmpDir = System.getProperty("TEMP");
                if(tmpDir == null || tmpDir.length() == 0)
                    tmpDir = "C:\\";
            }
        } else
        {
            tmpDir = "/tmp";
        }
        if(!(new File(tmpDir)).isDirectory())
            tmpDir = null;
        return tmpDir;
    }

    public static int shellExec(String command)
        throws IOException, InterruptedException
    {
        return shellExec(command, true);
    }

    public static int shellExec(String command, boolean verbose)
        throws IOException, InterruptedException
    {
        if(verbose)
            System.out.println(command);
        Process process;
        if(platform().equals("unix"))
            process = Runtime.getRuntime().exec(new String[] {
                "/bin/sh", "-c", command
            });
        else
            process = Runtime.getRuntime().exec(new String[] {
                "cmd.exe", "/c", command
            });
        InputConsumer consumer1 = new InputConsumer(process.getInputStream(), verbose ? ((OutputStream) (System.out)) : null);
        consumer1.start();
        InputConsumer consumer2 = new InputConsumer(process.getErrorStream(), verbose ? ((OutputStream) (System.err)) : null);
        consumer2.start();
        return process.waitFor();
    }

    public static String commandSeparator()
    {
        return platform().equals("unix") ? ";" : "&&";
    }
}

// 
// 
// 
// Source File Name:   Main.java

package fr.pixware.apt.parse;

import java.io.File;
import java.io.PrintStream;

// Referenced classes of package fr.pixware.apt.parse:
//            MultiFileSource, SinkAdapter, Parser

public class Main
{

    public Main()
    {
    }

    public static void main(String fileNames[])
    {
        if(fileNames.length == 0)
        {
            System.err.println("usage: java fr.pixware.apt.parse.Main apt_file ... apt_file");
            System.exit(1);
        }
        for(int i = 0; i < fileNames.length; i++)
            if(!(new File(fileNames[i])).isFile())
            {
                System.err.println("'" + fileNames[i] + "' is not a file");
                System.exit(1);
            }

        MultiFileSource source = new MultiFileSource(fileNames);
        SinkAdapter sink = new SinkAdapter();
        Parser parser = new Parser();
        try
        {
            parser.parse(source, sink);
        }
        catch(Exception e)
        {
            String msg = e.getMessage();
            if(msg == null)
                msg = e.getClass().getName();
            System.err.println("file '" + parser.getSourceName() + "', near line " + parser.getSourceLineNumber() + ": " + msg);
            System.exit(2);
        }
    }
}

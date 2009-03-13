// 
// 
// 
// Source File Name:   PSConverter.java

package fr.pixware.apt.convert;

import fr.pixware.util.*;
import java.io.File;

// Referenced classes of package fr.pixware.apt.convert:
//            Converter, Driver

public class PSConverter
    implements Converter
{

    public PSConverter(Driver driver)
    {
        this.driver = driver;
    }

    public Driver getDriver()
    {
        return driver;
    }

    public String getConverterInfo()
    {
        return "Uses LaTeX as its typesetter: requires a recent TeX\ndistribution with latex, makeindex, dvips.\n\nSupported processing instructions (PI):\n  * ps.pass1=latex to dvi command template.\n    Default: \"latex doc\"\n  * ps.pass2=dvi to ps command template.\n    Default: \"dvips -o %O doc\"\n";
    }

    public void convert(String inFileNames[], String outFileName)
        throws Exception
    {
        String pass1 = driver.getPI("ps", "pass1");
        if(pass1 == null)
            pass1 = "latex doc";
        String pass2 = driver.getPI("ps", "pass2");
        if(pass2 == null)
            pass2 = "dvips -o %O doc";
        doConvert(inFileNames, outFileName, driver, pass1, pass2, null);
    }

    public static void doConvert(String inFileNames[], String outFileName, Driver driver, String pass1, String pass2, String pass3)
        throws Exception
    {
        if(pass1 == null || (pass1 = pass1.trim()).length() == 0)
            return;
        outFileName = (new File(outFileName)).getCanonicalPath();
        String tmpDirName = PlatformUtil.tmpFileName("");
        File tmpDir = new File(tmpDirName);
        tmpDir.mkdir();
        tmpDirName = tmpDir.getCanonicalPath();
        driver.convert(inFileNames, tmpDirName + File.separatorChar + "doc.tex");
        StringBuffer cmd = new StringBuffer();
        String cmdSepar = " " + PlatformUtil.commandSeparator() + " ";
        cmd.append((PlatformUtil.platform().equals("windows") ? "cd /D " : "cd ") + tmpDirName);
        cmd.append(cmdSepar + expandVars(pass1, tmpDirName, outFileName));
        if(driver.isTOC())
            cmd.append(cmdSepar + expandVars(pass1, tmpDirName, outFileName));
        if(driver.isIndex())
        {
            FileUtil.saveString("headings_flag 1\nheading_prefix \"\\n  \\\\textbf{\"\nheading_suffix \"}\\n\"\ndelim_0 \"\\\\dotfill \"\ndelim_1 \"\\\\dotfill \"\ndelim_2 \"\\\\dotfill \"\n", tmpDirName + File.separatorChar + "index.ist");
            cmd.append(cmdSepar + "makeindex -q -s index.ist -o index.tex doc.idx");
            cmd.append(cmdSepar + expandVars(pass1, tmpDirName, outFileName));
        }
        cmd.append(cmdSepar + expandVars(pass1, tmpDirName, outFileName));
        if(pass2 != null && (pass2 = pass2.trim()).length() > 0)
        {
            cmd.append(cmdSepar + expandVars(pass2, tmpDirName, outFileName));
            if(pass3 != null && (pass3 = pass3.trim()).length() > 0)
                cmd.append(cmdSepar + expandVars(pass3, tmpDirName, outFileName));
        }
        PlatformUtil.shellExec(cmd.toString(), driver.isVerbose());
        FileUtil.removeFile(tmpDirName, true);
    }

    private static String expandVars(String pass, String tmpDirName, String outFileName)
    {
        String cmd = StringUtil.replaceAll(pass, "%T", tmpDirName);
        return StringUtil.replaceAll(cmd, "%O", outFileName);
    }

    private Driver driver;
    private static final String DEFAULT_PASS1 = "latex doc";
    private static final String DEFAULT_PASS2 = "dvips -o %O doc";
    private static final String INDEX_STYLE = "headings_flag 1\nheading_prefix \"\\n  \\\\textbf{\"\nheading_suffix \"}\\n\"\ndelim_0 \"\\\\dotfill \"\ndelim_1 \"\\\\dotfill \"\ndelim_2 \"\\\\dotfill \"\n";
}

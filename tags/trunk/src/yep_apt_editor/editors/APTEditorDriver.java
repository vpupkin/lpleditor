// 
// 
// 
// Source File Name:   APTEditorDriver.java

package yep_apt_editor.editors;

import fr.pixware.apt.convert.Driver;
import java.io.File;

public class APTEditorDriver extends Driver
{

    public APTEditorDriver()
    {
        pathSeparator = System.getProperty("file.separator");
        String tempDir = System.getProperty("java.io.tmpdir");
        if(tempDir.lastIndexOf(pathSeparator) != tempDir.length() - 1)
            tempDir = (new StringBuilder(String.valueOf(tempDir))).append(pathSeparator).toString();
        tempDirectory = (new StringBuilder(String.valueOf(tempDir))).append("apteditor").append(pathSeparator).toString();
        File file = new File(tempDirectory);
        if(!file.exists())
            file.mkdirs();
    }

    public String convert(String aptFilePath)
        throws Exception
    {
    	String htmlFilePath  = aptFilePath;
    	try{
	        String htmlFileName = (new StringBuilder(String.valueOf(aptFilePath.substring(aptFilePath.lastIndexOf(pathSeparator) + 1, aptFilePath.lastIndexOf("."))))).append(".html").toString();
	        htmlFilePath = (new StringBuilder(String.valueOf(tempDirectory))).append(htmlFileName).toString();
	        convert(new String[] {
	            aptFilePath
	        }, htmlFilePath);
    	}catch(Throwable e){
    		e.printStackTrace();
    	}
        return htmlFilePath;
    }

    private String tempDirectory;
    private String pathSeparator;
}

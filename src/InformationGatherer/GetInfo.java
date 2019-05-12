package InformationGatherer;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.filechooser.FileSystemView;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class GetInfo {

    public GetInfo() {}
    private static File sysInfo = new File("Sys.xml");

    public static File getSysInfo() {
        return sysInfo;
    }

    public static void getSystemInfo()
    {
        if(!sysInfo.exists()) {
            String command = "cmd /c start msinfo32.exe /nfo Sys.xml";
            try {
                System.out.println("Loading system info...");
                Process child = Runtime.getRuntime().exec(command);
                System.out.println("Loaded");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
            System.out.println("File already exists");

    }

    public static void readNfoFile()
    {
        try
        {
            BufferedReader bufRead = new BufferedReader(new InputStreamReader(new FileInputStream(sysInfo), "Cp1251"));
            String myLine = null;
            int counter = 0;
            while ( (myLine = bufRead.readLine()) != null)
            {
                if(counter == 50)
                    break;
                else {
                    System.out.println(myLine);
                    counter++;
                }

                // check to make sure you have valid data

            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

    }

    public static void readXmlFile()
    {
        try
        {
            File file = new File("Sys.xml");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                    .newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(file);
            String category = document.getElementsByTagName("Category").item(0).getTextContent();
//            System.out.println(category);
//            String pwd = document.getElementsByTagName("password").item(0).getTextContent();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String searchXMLByAttribute(String XMLFile)
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        while(true) {
            try {
                if (sysInfo.exists()) {
                    builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(XMLFile);
                    XPathFactory xPathfactory = XPathFactory.newInstance();
                    XPath xpath = xPathfactory.newXPath();
                    XPathExpression expr = xpath.compile("//Category[@name=\"Клавиатура\"]");
                    NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
                    return nl.item(0).getTextContent();
                }

            } catch (ParserConfigurationException | IOException | XPathExpressionException | SAXException e) {
                e.printStackTrace();
            }
        }
    }

    public static String returnKeyboardType(String globalInfo)
    {
        String[] splittedInfo = globalInfo.split("\n");
        for (int i = 0; i < splittedInfo.length - 1; i++) {
            if(splittedInfo[i].equals("Описание"))
                return splittedInfo[i+1];
        }
        return null;
    }

    public static int getScrenHeight()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return screenSize.height;
    }

    public static ArrayList<String> getDrives()
    {
        File[] paths;
        FileSystemView fsv = FileSystemView.getFileSystemView();

        // returns pathnames for files and directory
        paths = File.listRoots();
        ArrayList<String> drives = new ArrayList<>();
        // for each pathname in pathname array
        for(File path:paths)
        {
            // prints file and directory paths
            drives.add(path.toString());
        }
        return drives;
    }

    public static String getComputerName()
    {
        String computername= null;
        try {
            computername = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return computername;
    }

    public static String getCurrentUser()
    {
        return System.getProperty("user.name");
    }

    public static String getWindowsDirectory()
    {
        return System.getenv("windir");
    }

    public static String getSystem32Directory()
    {
        return System.getenv("WINDIR") + "\\System32";
    }

    public static String getCurrentDirectory()
    {
        return System.getProperty("user.dir");
    }

    public static long getTotalSpaceOfCurrentHardDrive(String dir)
    {
        return new File(dir).getTotalSpace();
    }

    public static String appendAllInfo(String computerName,
                                String currentUser,
                                String windowsDir,
                                String system32Dir,
                                String keyBoardType,
                                int screenHeight,
                                String installationDir
    )
    {
        StringBuilder systemInfo = new StringBuilder();
        systemInfo.append(computerName).append(";");
        systemInfo.append(currentUser).append(";");
        systemInfo.append(windowsDir).append(";");
        systemInfo.append(system32Dir).append(";");
        GetInfo.getSystemInfo();

        systemInfo.append(keyBoardType).append(";");
        systemInfo.append(screenHeight).append(";");
        for (String drive : GetInfo.getDrives()) {
            systemInfo.append(drive).append(";");
        }
        systemInfo.append(GetInfo.getTotalSpaceOfCurrentHardDrive(installationDir)).append(";");
        return systemInfo.toString();
    }
}

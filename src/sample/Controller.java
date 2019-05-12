package sample;

import InformationGatherer.GetInfo;
import WinRegistryActionPerfomer.WinRegistry;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Stream;

public class Controller {

    private String pubKeyFile = "C:\\Users\\galey\\Desktop\\pubKeys\\pubKey.pub";
    private String programForInstalltion = "C:\\Users\\galey\\IdeaProjects\\SecLab1";
    private String installationDir = null;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    public Button updateBtn;

    @FXML
    public Button browseBtn;

    @FXML
    public TextField textField;

    @FXML
    void initialize() {

        browseBtn.setOnAction(event ->{
            installationDir = getInstallationDirectory();

        });
        updateBtn.setOnAction(event -> doSomething());


    }

    public void doSomething() {
        String computerName = GetInfo.getComputerName();
        String currentUser = GetInfo.getCurrentUser();
        String winDir = GetInfo.getWindowsDirectory();
        String system32Dir = GetInfo.getSystem32Directory();
        int screnHeight = GetInfo.getScrenHeight();
        String keyboardInfo = GetInfo.searchXMLByAttribute("Sys.xml");
        String keyboardType = GetInfo.returnKeyboardType(keyboardInfo);
        String systemInfo = GetInfo.appendAllInfo(computerName,
                currentUser,
                winDir,
                system32Dir,
                keyboardType,
                screnHeight,
                installationDir);
        String hash = hashing(systemInfo);

        try {
            KeyPair kp = generateKeyPair();
            String signature = signStr(hash,kp.getPrivate());
            //key = раздел
            writeInRegistrySignature(signature);
            savePublicKey(kp.getPublic());
            System.out.println(signature);
            System.out.println(verify(hash,signature,loadPublicKeyFromFile(pubKeyFile)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        copyProgramToSelectedPath(installationDir);

    }

    public String hashing(String text) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(text.getBytes());
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            String hashtext = bigInt.toString(16);
            System.out.println(hashtext);
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String signStr(String textForSigning, PrivateKey privateKey) throws Exception
    {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(textForSigning.getBytes("UTF-8"));

        byte[] signature = privateSignature.sign();

        return Base64.getEncoder().encodeToString(signature);
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();

        return pair;
    }

    public static boolean verify(String plainText, String signature, PublicKey publicKey) throws Exception {
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(plainText.getBytes("UTF-8"));

        byte[] signatureBytes = Base64.getDecoder().decode(signature);

        return publicSignature.verify(signatureBytes);
    }

    public void writeInRegistrySignature(String signature)
    {

        try {
            WinRegistry.createKey(WinRegistry.HKEY_CURRENT_USER,"SOFTWARE\\Kosse");
            WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER,
                    "SOFTWARE\\Kosse",
                    "Signature",
                    signature);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void savePublicKey(PublicKey publickey)
    {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(pubKeyFile);
            out.write(publickey.getEncoded());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public PublicKey loadPublicKeyFromFile(String filePath)
    {
        Path path = Paths.get(filePath);
        byte[] bytes = new byte[0];
        try {
            bytes = Files.readAllBytes(path);
            X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(ks);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getInstallationDirectory()
    {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage stage = (Stage)anchorPane.getScene().getWindow();
        File file = directoryChooser.showDialog(stage);
        if(file != null)
        {
            System.out.println(file.getAbsolutePath());
            textField.setText(file.toString());
            return file.getAbsolutePath();
        }
        return null;
    }



    public void copyProgramToSelectedPath(String path)
    {
        File srcDir = new File(programForInstalltion);

        File destDir = new File(path);

        try {
            copyFolder(srcDir,destDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void copyFolder(File src, File dest) throws IOException{
        if(src.isDirectory()){
            if(!dest.exists()){
                dest.mkdir();
            }

            String files[] = src.list();

            for (String file : files) {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);

                copyFolder(srcFile,destFile);
            }

        } else {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            while ((length = in.read(buffer)) > 0){
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
        }
    }
}




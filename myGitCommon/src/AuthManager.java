import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by Melancias on 03/03/2017.
 */

public class AuthManager {

    private File authFile;
    private String password;
    public AuthManager(String password) {
        this.authFile = new File("./.authFile");
        this.password=password;
    }


    public static boolean userExists(String username) throws IOException {
        File authFile = new File("./.authFile");
        if(!authFile.exists())
            return false;
        BufferedReader authReader = new BufferedReader(new FileReader(authFile));

        // Read each line until credentials match
        boolean resp = false;
        for (String line; (line = authReader.readLine()) != null;){

            String[] credentials = line.split("\\:");
            if (credentials[0].equals(username))
               resp = true;
        }
        return resp;
    }

    public boolean authenticate(String username, String password,String action) throws NoSuchAlgorithmException, InvalidKeyException, IOException, ClassNotFoundException {
        integrityCheck(this.password);
        try {
            BufferedReader authReader = new BufferedReader(new FileReader(authFile));

            // Read each line until credentials match
            for (String line; (line = authReader.readLine()) != null;){
                String[] credentials = line.split("\\:");
                if (credentials[0].equals(username)) {
                    if (credentials[1].equals(password)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            // In case credentials not found, register
            if (action.equals("register"))
                return register(username, password);

        } catch (FileNotFoundException e) {
            createAuthFile();
            return register(username, password);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;

    }

    private boolean register(String username, String password){
        try {
            FileWriter authWriter = new FileWriter(authFile, true);
            String credentials = username+":"+password;
            authWriter.append(credentials);
            authWriter.write(System.lineSeparator());
            authWriter.flush();
            integrityRewrite();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private boolean createAuthFile(){
        try {
            return authFile.createNewFile();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean integrityCheck(String password) throws NoSuchAlgorithmException, InvalidKeyException, IOException, ClassNotFoundException {
        if(!new File(".authFile").exists() && !new File(".authFileHash").exists())
            return true;
        byte [] pass = password.getBytes();
        SecretKey key = new SecretKeySpec(pass, "HmacSHA256");
        Mac m;
        byte[] mac=null;
        m = Mac.getInstance("HmacSHA256");
        m.init(key);
        //get file byte stream compare digests
        Path path = Paths.get("./.authFile");
        byte[] data = Files.readAllBytes(path);
        m.update(data);
        mac = m.doFinal();
        System.out.println("Actual Hash:");
        System.out.println(new String(HexBin.encode(mac)));
        FileInputStream fis = new FileInputStream(".authFileHash");
        ObjectInputStream ois = new ObjectInputStream(fis);
        byte[] dataHash = (byte[]) ois.readObject();
        System.out.println("Saved Hash");
        System.out.println(new String(HexBin.encode(dataHash)));
        return Arrays.equals(mac, dataHash);
    }

     private void integrityRewrite() {
        try {
            File authHash = new File("./.authFileHash");
            if (!authHash.exists()) {
                authHash.createNewFile();
            }
            if (authHash.length() > 0) {
                authHash.delete();
                authHash.createNewFile();
            }
            byte[] pass = password.getBytes();
            SecretKey key = new SecretKeySpec(pass, "HmacSHA256");
            Mac m;
            byte[] mac = null;
            m = Mac.getInstance("HmacSHA256");
            m.init(key);
            Path path = Paths.get("./.authFile");
            byte[] data = Files.readAllBytes(path);
            m.update(data);
            mac = m.doFinal();
            FileOutputStream fos = new FileOutputStream(".authFileHash");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mac);
            oos.flush();
            fos.close();
        }
        catch(Exception e){
            System.out.println("Error rewriting the hash");
            e.printStackTrace();
        }
     }

}

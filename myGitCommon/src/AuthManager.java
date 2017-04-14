import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

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

    public boolean authenticate(String username, String password,String action) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
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

    private boolean integrityCheck(String password) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
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
        //Arrays.equals(correct, digest)
        System.out.println("Actual Signature:");
        System.out.println(new String(HexBin.encode(mac)));
        return true;
    }

}

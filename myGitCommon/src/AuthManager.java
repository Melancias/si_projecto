import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.naming.AuthenticationException;
import java.io.*;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

/**
 * Created by Melancias on 03/03/2017.
 */

//TODO Faz o que está aqui abaixo, escravo!

    /*
        Passos da coisa:
            carregar o ficheiro
            decifrar o gajo
            inegrityChech();
            se correr bem, fixe

            nota: fazer o passo inverso a cada registo
            nota2: remover
    */

public class AuthManager {

    private File authFile;
    private static String password;
    private static AuthManager instance = null;

    protected AuthManager(String password) {
        this.authFile = new File("./.authFile");
        this.password = password;
    }

    public static AuthManager getInstance(String password){
        if(instance == null){
            instance = new AuthManager(password);
        }

        return instance;
    }

    public static AuthManager getInstance() throws Exception {
        if (password != null){
            return instance;
        } else {
            throw new Exception("AuthManager password not initialized.");
        }
    }

    public String getPassword() {
        return password;
    }

    public  boolean userExists(String username) throws Exception {

        if (!authFile.exists())
            return false;

        BufferedReader authReader = readCipheredFile(authLineDecipher());

//        BufferedReader authReader = new BufferedReader(new FileReader(authFile));

        // Read each line until credentials match
        boolean resp = false;
        for (String line; (line = authReader.readLine()) != null; ) {
            String[] credentials = line.split("\\:");
            if (credentials[0].equals(username))
                resp = true;
        }
        return resp;
    }

    public boolean validatePassword(String nonce, String inputHashedPassword, String storedPassword ) throws NoSuchAlgorithmException, InvalidKeyException {

        SecretKey key = new SecretKeySpec(nonce.getBytes(), "HmacSHA256");

        Mac m;
        byte[] passwordBytes = storedPassword.getBytes();

        m = Mac.getInstance("HmacSHA256");
        m.init(key);
        m.update(passwordBytes);

        String hashedStoredPassword = new String(m.doFinal());

        return hashedStoredPassword.equals(inputHashedPassword);
    }

    public boolean authenticate(String username, String nonce, String password, String action) throws AuthenticationException {
        try {
            integrityCheck();
        }
        catch(AuthenticationException e){
            throw new AuthenticationException("Integrity Check failed");
        }
        try {
            BufferedReader authReader = readCipheredFile(authLineDecipher());

            // Read each line until credentials match
            for (String line; (line = authReader.readLine()) != null; ) {
                String[] credentials = line.split("\\:");

                if (credentials[0].equals(username)) {
                    if (validatePassword(nonce, password, credentials[1])) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            // In case credentials not found, register
            if (action.equals("register")){
                return register(username, password);
            }
        } catch (FileNotFoundException e) {
            try {
                createAuthFile();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            try {
                return register(username, password);
            } catch (BadPaddingException e1) {
                e1.printStackTrace();
            } catch (IllegalBlockSizeException e1) {
                e1.printStackTrace();
            }
            //return false;
        } catch (Exception e) {
            e.printStackTrace();
        }


        return false;

    }

    public boolean register(String username, String password) throws BadPaddingException, IllegalBlockSizeException {
        try {
            //BufferedReader reader = readCipheredFile(authLineDecipher(authCipher()));

            //FileWriter authWriter = new FileWriter(authFile, true);

            String credentials = username + ":" + password + "\n";

            byte[] newLine = credentials.getBytes();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            baos.write(authLineDecipher());
            baos.write(newLine);

            byte[] newFile = baos.toByteArray();


            //authWriter.append(credentials);
            //authWriter.write(System.lineSeparator());
            //authWriter.flush();

            authHashRewrite(newFile);

            authFileCipher(newFile);


        } catch (IOException e) {
            return false;
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean createAuthFile() throws Exception{
        try {
             new File(".authFileTemp").createNewFile();
             authCipher();
            new File(".authFileTemp").delete();
             return true;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean integritySharedCheck(String absolutePath, String password) throws Exception {
        try {
            if (!new File(absolutePath).exists() && !new File(absolutePath + ".hash").exists())
                return true;

            byte[] pass = password.getBytes();
            SecretKey key = new SecretKeySpec(pass, "HmacSHA256");

            Mac m;
            byte[] mac = null;

            m = Mac.getInstance("HmacSHA256");
            m.init(key);

            //get file byte stream compare digests
            Path path = Paths.get(absolutePath);
            byte[] data = Files.readAllBytes(path);
            m.update(data);
            mac = m.doFinal();

            FileInputStream fis = new FileInputStream(absolutePath + ".hash");
            ObjectInputStream ois = new ObjectInputStream(fis);

            byte[] dataHash = (byte[]) ois.readObject();

            return Arrays.equals(mac, dataHash);
        }catch (Exception e){
            throw new Exception("Failed checking integrity");
        }
    }

    public boolean integrityCheck() throws AuthenticationException {
        try{
        if (! authFile.exists() && !new File(authFile.getAbsolutePath() + ".hash").exists())
            return true;

        byte[] pass = password.getBytes();
        SecretKey key = new SecretKeySpec(pass, "HmacSHA256");

        Mac m;
        byte[] mac = null;

        m = Mac.getInstance("HmacSHA256");
        m.init(key);

        //get file byte stream compare digests

        byte[] data = authLineDecipher();
        m.update(data);
        mac = m.doFinal();

        FileInputStream fis = new FileInputStream(authFile.getAbsolutePath()+ ".hash");
        ObjectInputStream ois = new ObjectInputStream(fis);

        byte[] dataHash = (byte[]) ois.readObject();

        return Arrays.equals(mac, dataHash);}
        catch(Exception e){
            throw new AuthenticationException("Error deciphering the authentication file");
        }

    }

    public static void integritySharedRewrite(String absolutePath, String password) {
        try {
            File authHash = new File(absolutePath + ".hash");
            if (!authHash.exists()) {
                authHash.createNewFile();
            }
            if (authHash.length() > 0) {
                authHash.delete();
                authHash.createNewFile();
            }

            byte[] pass = AuthManager.password.getBytes();
            SecretKey key = new SecretKeySpec(pass, "HmacSHA256");

            Mac m;
            byte[] mac = null;
            m = Mac.getInstance("HmacSHA256");
            m.init(key);

            Path path = Paths.get(absolutePath);
            byte[] data = Files.readAllBytes(path);
            m.update(data);
            mac = m.doFinal();

            FileOutputStream fos = new FileOutputStream(absolutePath + ".hash");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mac);

            oos.flush();
            fos.close();
        } catch (Exception e) {
            System.out.println("Error rewriting the hash");
            e.printStackTrace();
        }
    }

    private void authHashRewrite(byte[] newFile) {
        try {
            File authHash = new File(this.authFile.getAbsolutePath() + ".hash");
            if (!authHash.exists()) {
                authHash.createNewFile();
            }
            if (authHash.length() > 0) {
                authHash.delete();
                authHash.createNewFile();
            }

            byte[] pass = AuthManager.password.getBytes();
            SecretKey key = new SecretKeySpec(pass, "HmacSHA256");

            Mac m;
            byte[] mac = null;
            m = Mac.getInstance("HmacSHA256");
            m.init(key);

            byte[] data = newFile;
            m.update(data);
            mac = m.doFinal();

            FileOutputStream fos = new FileOutputStream(this.authFile.getAbsolutePath() + ".hash");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(mac);
            oos.flush();
            fos.close();
        } catch (Exception e) {
            System.out.println("Error rewriting the hash");
            e.printStackTrace();
        }
    }

    private Cipher getCipherFromPassword() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
        SecretKey key = kf.generateSecret(keySpec);

        Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
        c.init(Cipher.ENCRYPT_MODE, key);
        FileOutputStream kos = null;
        try {
            kos = new FileOutputStream(".authFile.salt");
            ObjectOutputStream oos = new ObjectOutputStream(kos);
            oos.writeObject(c.getParameters().getEncoded());
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    private Cipher authFileCipher(byte[] newDeciferedFile) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {

        Cipher c = getCipherFromPassword();
        FileOutputStream fos;
        CipherOutputStream cos;

        try {
            fos = new FileOutputStream(".authFile");
            cos = new CipherOutputStream(fos, c);

            //TODO Alex, se estiveres aborrecido da vida, quase em depressão,
            //TODO sem nada para fazer, muda para sequencial. lol.
            cos.write(newDeciferedFile);

            cos.flush();
            fos.flush();
            cos.close();
            fos.close();
            cos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return c;
    }

    private void authCipher() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException, IOException {
        byte[] pass = password.getBytes();

        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
        SecretKey key = kf.generateSecret(keySpec);
        Cipher c =  Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
        c.init(Cipher.ENCRYPT_MODE, key);

        FileOutputStream kos = new FileOutputStream(".authFile.salt");
        ObjectOutputStream oos = new ObjectOutputStream(kos);
        oos.writeObject(c.getParameters().getEncoded());
        oos.close();
        FileInputStream fis;
        FileOutputStream fos;
        CipherOutputStream cos;

        try {
            fis = new FileInputStream(".authFileTemp");
            fos = new FileOutputStream(".authFile");
            cos = new CipherOutputStream(fos, c);

            byte[] b = new byte[16];
            int i = fis.read(b);
            while (i != -1) {
                cos.write(b, 0, i);
                i = fis.read(b);
            }
            cos.flush();
            fos.flush();
            cos.close();
            fos.close();
            cos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] authLineDecipher() throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException, ClassNotFoundException {
        byte[] pass = password.getBytes();

        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
        SecretKey key = kf.generateSecret(keySpec);
        FileInputStream saltreader = new FileInputStream(".authFile.salt");
        ObjectInputStream saltObject = new ObjectInputStream(saltreader);
        byte[] saltParameters = (byte[]) saltObject.readObject();
        AlgorithmParameters salt = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
        salt.init(saltParameters);
        saltObject.close();
        Cipher decifrador = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
        decifrador.init(Cipher.DECRYPT_MODE, key, salt);
        decifrador.doFinal();
        FileInputStream fis2 = new FileInputStream(".authFile");
        CipherInputStream decis = new CipherInputStream(fis2, decifrador);
        byte[] decifrado = new byte[16];
        ByteArrayOutputStream test= new ByteArrayOutputStream();
        int j = decis.read(decifrado);
        while (j != -1) {
            test.write(decifrado, 0, j);
            test.flush();
            j = decis.read(decifrado);
        }

        fis2.close();
        decis.close();
        return test.toByteArray();
    }

    public BufferedReader readCipheredFile(byte[] decipheredFile) {
        InputStream is = null;
        BufferedReader bfReader = null;
        try {
            is = new ByteArrayInputStream(decipheredFile);
            bfReader = new BufferedReader(new InputStreamReader(is));
            }
        catch(Exception e){
            e.printStackTrace();
        }
        return bfReader;
    }

    public static String generateNonce(){
        return new BigInteger(130, new SecureRandom()).toString(32);
    }


}


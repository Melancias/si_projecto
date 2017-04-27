import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

    public AuthManager(String password) {
        this.authFile = new File("./.authFile");
        this.password = password;
    }

    public static String getPassword() {
        return password;
    }

    public static boolean userExists(String username) throws IOException {
        File authFile = new File("./.authFile");
        if (!authFile.exists())
            return false;


        BufferedReader authReader = new BufferedReader(new FileReader(authFile));

        // Read each line until credentials match
        boolean resp = false;
        for (String line; (line = authReader.readLine()) != null; ) {

            String[] credentials = line.split("\\:");
            if (credentials[0].equals(username))
                resp = true;
        }
        return resp;
    }

    public boolean authenticate(String username, String password, String action) throws NoSuchAlgorithmException, InvalidKeyException, IOException, ClassNotFoundException, BadPaddingException, IllegalBlockSizeException {
        integrityCheck(authFile.getAbsolutePath(), this.password);
        try {
            BufferedReader authReader = new BufferedReader(new FileReader(authFile));

            // Read each line until credentials match
            for (String line; (line = authReader.readLine()) != null; ) {
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

    private boolean register(String username, String password) throws BadPaddingException, IllegalBlockSizeException {
        try {
            FileWriter authWriter = new FileWriter(authFile, true);

            String credentials = username + ":" + password;

            authWriter.append(credentials);
            authWriter.write(System.lineSeparator());
            authWriter.flush();

            integrityRewrite(authFile.getAbsolutePath(), getPassword());

            readCipheredFile(authLineDecipher(authCipher()));

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
        }
        return true;
    }

    private boolean createAuthFile() {
        try {
            return authFile.createNewFile();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean integrityCheck(String absolutePath, String password) throws NoSuchAlgorithmException, InvalidKeyException, IOException, ClassNotFoundException {

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

        System.out.println("Actual Hash:");
        System.out.println(new String(HexBin.encode(mac)));

        FileInputStream fis = new FileInputStream(absolutePath + ".hash");
        ObjectInputStream ois = new ObjectInputStream(fis);

        byte[] dataHash = (byte[]) ois.readObject();

        System.out.println("Saved Hash");
        System.out.println(new String(HexBin.encode(dataHash)));

        return Arrays.equals(mac, dataHash);
    }

    public static void integrityRewrite(String absolutePath, String password) {
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

    private Cipher authCipher() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, InvalidAlgorithmParameterException, BadPaddingException, IllegalBlockSizeException {
        System.out.println("lol");
        byte[] pass = password.getBytes();

        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
        SecretKey key = kf.generateSecret(keySpec);

        Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
        c.init(Cipher.ENCRYPT_MODE, key);

        FileInputStream fis;
        FileOutputStream fos;
        CipherOutputStream cos;

        try {
            fis = new FileInputStream(".authFile");
            fos = new FileOutputStream(".authFile.cif");
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
        return c;
    }

    public byte[] authLineDecipher(Cipher c) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] pass = password.getBytes();

        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
        SecretKey key = kf.generateSecret(keySpec);
        FileOutputStream fos2 = new FileOutputStream("decifrado.txt");
        FileInputStream fis2 = new FileInputStream(".authFile.cif");


        Cipher decifrador = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
        decifrador.init(Cipher.DECRYPT_MODE, key, c.getParameters());
        decifrador.doFinal();
        CipherInputStream decis = new CipherInputStream(fis2, decifrador);

        byte[] decifrado = new byte[16];
        ByteArrayOutputStream test= new ByteArrayOutputStream();
        int j = decis.read(decifrado);
        while (j != -1) {
            test.write(decifrado, 0, j);
            test.flush();
            j = decis.read(decifrado);
        }

        fos2.close();
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


}


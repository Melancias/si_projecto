import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.util.ArrayList;

/**
 * Created by pedro on 10-03-2017.
 */
public class DataTransferUtils {

    private Socket socket;
    private String user;
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;


    public DataTransferUtils(String host,int port,String user) throws IOException {
        //System.setProperty("javax.net.ssl.trustStore", "myClient.keyStore");
        //mudar para o keystore de quem tiver a usar o programa
        System.setProperty("javax.net.ssl.trustStore", "cliente.jks");
        SocketFactory sf = SSLSocketFactory.getDefault();
        try {
            socket = sf.createSocket(host, port);
            this.user = user;
            //noinspection Since15
            outStream = new ObjectOutputStream(socket.getOutputStream());
            inStream = new ObjectInputStream(socket.getInputStream());
        }
        catch(ConnectException e){
            throw new ConnectException("\nConnection refused: Host not found\nCheck your host:port paramaters");
        }
    }

    public DataTransferUtils(Socket socket) throws IOException {
        this.socket=socket;
        outStream = new ObjectOutputStream(socket.getOutputStream());
        inStream = new ObjectInputStream(socket.getInputStream());
    }

    static File generateSignature(String path, String username){
        try {
            FileInputStream kfile = null;
            byte[] data= Files.readAllBytes(Paths.get(path));
            kfile = new FileInputStream("cliente.jks");
            FileOutputStream fos = new FileOutputStream(path+".sig");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            KeyStore kstore = KeyStore.getInstance("JKS");
            kstore.load(kfile, "bolachas".toCharArray());
            PrivateKey myPrivateKey=(PrivateKey) kstore.getKey(username, "bolachas".toCharArray());
            Signature s = Signature.getInstance("SHA256withRSA");
            s.initSign(myPrivateKey);
            s.update(data);
            oos.writeObject(s.sign( ));
            fos.close();
            return new File(path+".sig");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void CipherKey(String fileName) {
        try {

            // TODO Muda isto!
            FileInputStream fileInputStream = new FileInputStream("servidor.jks");
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(fileInputStream, "bolachas".toCharArray());
            Certificate certificate = keyStore.getCertificate("servidor");

            Cipher ckey = Cipher.getInstance("RSA");
            ckey.init(Cipher.WRAP_MODE, certificate);
            FileInputStream fisKey = new FileInputStream(fileName);
            ObjectInputStream oisKey = new ObjectInputStream(fisKey);

            byte[] key = (byte[]) oisKey.readObject();

            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            byte[] cipheredKey = ckey.wrap(keySpec);

            FileOutputStream kos = new FileOutputStream(fileName + ".server");
            //ObjectOutputStream oos = new ObjectOutputStream(kos);
            kos.write(cipheredKey);
            kos.close();
            new File(fileName).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static boolean checkSignature(String path, String username){
        boolean answer=false;
        try {
            FileInputStream kfile = new FileInputStream("cliente.jks"); //keystore
            KeyStore kstore = KeyStore.getInstance("JKS");
            kstore.load(kfile, "bolachas".toCharArray()); //password
            Certificate cert = kstore.getCertificate(username);
            FileInputStream fis = new FileInputStream(path+".sig");
            ObjectInputStream ois = new ObjectInputStream(fis);
            byte[] data= Files.readAllBytes(Paths.get(path)); //não fiz verificação de erro
            byte signature[] = (byte[]) ois.readObject(); //não fiz verificação de erro
            Certificate c = cert; //obtém um certificado de alguma forma (ex., de um ficheiro)
            PublicKey pk = c.getPublicKey();
            Signature s = Signature.getInstance("SHA256withRSA");
            s.initVerify(pk);
            s.update(data);
            answer=s.verify(signature);
            if (answer)
                System.out.println("Message is valid");
            else
                System.out.println("Message was corrupted");

            fis.close();
            new File(path+".sig").delete();
            return answer;
        }
        catch(Exception e){

            e.printStackTrace();
        }
        return false;
    }

    public static File decipherKey(String fileName) {

        try {
            File file = new File(fileName + ".key.server");
            FileInputStream kos = new FileInputStream(file);
            //ObjectInputStream  bos = new ObjectInputStream(kos);

            byte[] chaveCifrada = new byte[256];
            int i = kos.read(chaveCifrada);

            // TODO Muda isto
            FileInputStream fileInputStream = new FileInputStream("servidor.jks");
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(fileInputStream, "bolachas".toCharArray());
            PrivateKey privateKey = (PrivateKey) keyStore.getKey("servidor", "bolachas".toCharArray());

            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.UNWRAP_MODE, privateKey );


            Key decipheredKey = c.unwrap(chaveCifrada, "AES", Cipher.SECRET_KEY);

            FileOutputStream fos   = new FileOutputStream(fileName + ".key");
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.write(decipheredKey.getEncoded());

            oos.flush();
            oos.close();
            fos.flush();
            fos.close();
            kos.close();


        } catch (Exception e) {
            e.printStackTrace();
        }

        return new File(fileName + ".key");
    }

    public static File generateCipherKey(String fileName) throws NoSuchAlgorithmException, IOException {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(128);
        SecretKey key = kg.generateKey();

        FileOutputStream fos = new FileOutputStream(fileName + ".key");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        byte[] keyBytes = key.getEncoded();

        oos.writeObject(keyBytes);

        oos.flush();
        oos.close();
        fos.flush();
        fos.close();

        return new File(fileName + ".key");
    }

    public static File cipherFile(File secretKeyFile, File fileToCipher) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, ClassNotFoundException, InvalidKeyException {

        FileInputStream fisKey;
        ObjectInputStream oisKey;
        FileInputStream fisFile;
        FileOutputStream fos;
        CipherOutputStream cos;

        fisKey = new FileInputStream(secretKeyFile);
        oisKey = new ObjectInputStream(fisKey);

        fisFile = new FileInputStream(fileToCipher);

        byte[] key = (byte[]) oisKey.readObject();

        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, keySpec);


        fos = new FileOutputStream(fileToCipher.getName()+".cif");
        cos = new CipherOutputStream(fos, c);

        byte[] b = new byte[16];
        int i = fisFile.read(b);
        while (i != -1) {
            cos.write(b, 0, i);
            i = fisFile.read(b);
        }
        cos.close();

        return new File(fileToCipher.getName()+".cif");
    }

    public static File decipherFile(File secretKeyFile, File fileToDecipher, long date) throws Exception {
        try {
            FileInputStream fis = new FileInputStream(secretKeyFile);

            byte[] keyEncoded = new byte[16];

            ObjectInputStream ois = new ObjectInputStream(fis);

            ois.read(keyEncoded);
            ois.close();

            SecretKeySpec keySpec2 = new SecretKeySpec(keyEncoded, "AES");
            //SecretKeySpec é subclasse de secretKey
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, keySpec2);
            FileInputStream fis2;
            FileOutputStream fos;
            fis2 = new FileInputStream(fileToDecipher);
            fos = new FileOutputStream(fileToDecipher.getAbsoluteFile() + ".temp");
            CipherInputStream cis = new CipherInputStream(fis2, c);
            byte[] b = new byte[16];
            int i = cis.read(b);
            while (i != -1) {
                fos.write(b, 0, i);
                i = cis.read(b);
            }
            fos.close();
            cis.close();
            String namefile = fileToDecipher.getName();
            fileToDecipher.delete();
            secretKeyFile.delete();
            new File(fileToDecipher.getAbsolutePath() + ".temp").renameTo(new File(fileToDecipher.getAbsolutePath()));
            new File(fileToDecipher.getAbsolutePath()).setLastModified(date);
            if(new File(fileToDecipher.getAbsolutePath()+".old").exists()){
                new File(fileToDecipher.getAbsolutePath()+".old").delete();
            }

        }
        catch(Exception e)
        {
            if(new File(fileToDecipher.getAbsolutePath()+".old").exists()){
                new File(fileToDecipher.getAbsolutePath()+".old").renameTo(new File(fileToDecipher.getAbsolutePath()));
            }
            else{
                new File(fileToDecipher.getAbsolutePath()).delete();
            }
            throw new Exception("Ciphered file is corrupted or was manipulated");
        }
        return null;
    }


    public boolean pushFile(File file) throws IOException {

        FileInputStream fileInput = null;

        try {
            fileInput = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            System.err.println("Error: File not found. Abort.");
            System.exit(-1);
        }
        long    fileLength  = file.length();
        int     sendLength  = 1024;
        int     offset      = 0;
        long lastModified=file.lastModified();
        byte[] buffer = new byte[1024];

        // Send file length
        outStream.writeObject(fileLength);
        outStream.writeObject(lastModified);

        while(offset < fileLength){

            if(offset + sendLength > fileLength){
                sendLength = (int)fileLength - offset;
            }
            // Read from File System to buffer
            fileInput.read(buffer, 0, sendLength);
            // Write from buffer to socket
            outStream.write(buffer, 0, sendLength);
            // flush the socket
            outStream.flush();

            offset += sendLength;
        }
        if (file.getName().contains(".sig")){
            System.out.println(" - Signature Sent - ");
        }
        else {
            System.out.println(" - File Sent - ");
        }

//        outStream.close();
//        inStream.close();
//        socket.close();
        
        return false;
    }

    public long pullFile(String path,String userType) throws IOException, ClassNotFoundException {

        File file = new File(path);
        int bytes;
        int received = 0;
        int chunkSize = 1024;
        byte[] buffer = new byte[1024];

        // Receive file length
        long fileLength = (Long) inStream.readObject();
        long lastModified = (Long) inStream.readObject();
        if(userType=="cliente"){
            if (new File(path).exists());
                new File(path).renameTo(new File(path+".old"));
            FileOutputStream fileOut = new FileOutputStream(file, false);

            while(received < fileLength){
                // If about to receive more than what's left
                if( (received + chunkSize) > fileLength ){
                    // Set incoming bytes to what's left
                    chunkSize = (int)fileLength - received;
                }

                // Read bytes to buffer
                bytes = inStream.read(buffer, 0, chunkSize);

                // Write from buffer to File System
                fileOut.write(buffer, 0, bytes);

                received += bytes;
                file.setLastModified(lastModified);

            }
            fileOut.flush();
            fileOut.close();
        }else if(userType=="servidor") {
            FileOutputStream fileOut = new FileOutputStream(file);

            while (received < fileLength) {
                // If about to receive more than what's left
                if ((received + chunkSize) > fileLength) {
                    // Set incoming bytes to what's left
                    chunkSize = (int) fileLength - received;
                }

                // Read bytes to buffer
                bytes = inStream.read(buffer, 0, chunkSize);

                // Write from buffer to File System
                fileOut.write(buffer, 0, bytes);

                received += bytes;
            }


            System.out.println("Finished - Sent: " + fileLength);

            // Close File Input Stream
            file.setLastModified(lastModified);
            fileOut.flush();
            fileOut.close();
        }
        return lastModified;

    }

    public Boolean authClient(String user, String pwd, String action) throws Exception {

        String nonce = AuthManager.generateNonce();

        if (action == "login") {

            SecretKey key = new SecretKeySpec(nonce.getBytes(), "HmacSHA256");

            Mac m;
            byte[] hashedPassword = null;
            byte[] passwordBytes = pwd.getBytes();

            m = Mac.getInstance("HmacSHA256");
            m.init(key);
            m.update(passwordBytes);
            hashedPassword = m.doFinal();
            nonce = new String(nonce);
            pwd = new String(hashedPassword);

        }

        try {
            outStream.writeObject(user);
            outStream.writeObject(nonce);
            outStream.writeObject(pwd);
            outStream.writeObject(action);
            outStream.flush();
            return (Boolean) inStream.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return new Boolean(false);
    }

    public String[] getCredentials() {
        String[] credns = null;
        try {
            System.out.println();
            String user = (String)inStream.readObject();
            String nonce = (String)inStream.readObject();
            String passwd = (String)inStream.readObject();
            String action = (String)inStream.readObject();
            credns = new String[]{user, nonce, passwd,action};
        } catch (Exception e) {
            System.out.println("Error getting credentials");
        }
        return credns;
    };


    public DataManifest sendManifest(String user, String repo, String action,RepoPathTypeEnum type) throws Exception {
        if (action.equals("push") || action.equals("pull/server")) {
            DataManifest d = new DataManifest(user, repo, action);
            d.whohasit=type;
            repo = "./" + repo;
            if (new File(repo).isFile()) {
                d.addFileManifestManual(repo);
            } else if (new File(repo).isDirectory()) {
                d.autoGenerateManifest(repo);
            }
            else if (new File(user+"/"+repo).isFile()){
                d.addFileManifestManual(user+"/"+repo);
            }
            else if(new File(user+"/"+repo).isDirectory()) {
                d.autoGenerateManifest(user+"/"+repo);
            }
            else{
                outStream.writeObject("ignore");
                System.out.println("Directory or File does not exist");
                return null;
            }

            outStream.writeObject(d);
            outStream.flush();
            return d;
        }
        else if (action=="pull"){
            DataManifest d = new DataManifest(user, repo, action);
            outStream.writeObject(d);
            outStream.flush();
        }
        return null;
    }



    public ArrayList<String> getFileList() throws IOException, ClassNotFoundException {
        ArrayList<String> c= (ArrayList<String>) inStream.readObject();
        for(String s: c){
            System.out.println(s);
        }
        return c;
    }

    public void sendRequestList(ArrayList<String> test) throws IOException {
        outStream.writeObject(test);
        outStream.flush();
    }

    public Object getRequest() throws IOException, ClassNotFoundException {
        return inStream.readObject();
    }

    public void sendHandshake() throws IOException {
        outStream.writeObject(new Boolean(true));
        outStream.flush();
    }

    public void share(String comando, String localUser, String repo, String userId) {
        try {
            outStream.writeObject(comando + ":" + localUser + ":" + repo + ":" + userId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void remove(String comando, String localUser, String repo, String userId) {
        try {
            outStream.writeObject(comando + ":" + localUser + ":" + repo + ":" + userId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCloseHandshake() throws IOException {
        outStream.writeObject(new Boolean(false));
        outStream.flush();
    }

    public void sendRequest(int i) throws IOException {
        outStream.writeObject(i);
        outStream.flush();
    }


    public int receiveAnswer() throws IOException, ClassNotFoundException {
        return (Integer) inStream.readObject();
    }

    public boolean clientRepoAccessCheck() throws IOException, ClassNotFoundException {
        String repo = null;
        String username = null;
        String action = null;
        try{
            repo = (String)inStream.readObject();
            username = (String)inStream.readObject();
            action = (String)inStream.readObject();
        }catch(Exception e){
            System.out.println("Nao recebeu os dados");
        }

        boolean answer;
        if(!action.equals("-pull")||!action.equals("-push")) {
            answer = RepoManager.shareCheck(repo, username);
        }
        else{answer=true;}
        outStream.writeObject(answer);
        outStream.flush();
        return answer;
    }

    public boolean checkRepoAcess(String repo, String localUser,String action) throws IOException, ClassNotFoundException {
        outStream.writeObject(repo);
        outStream.writeObject(localUser);
        outStream.writeObject(action);
        return (Boolean) inStream.readObject();
    }

    public boolean accountCheck(String user) throws IOException, ClassNotFoundException {
        outStream.writeObject(user);
        return (Boolean) inStream.readObject();
    }

    public String getAccountCheck() throws IOException, ClassNotFoundException {
        return (String) getRequest();
    }

}

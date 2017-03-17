import java.io.*;
import java.net.Socket;
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
        socket = new Socket(host, port);
        this.user=user;
        //noinspection Since15
        outStream = new ObjectOutputStream(socket.getOutputStream());
        inStream = new ObjectInputStream(socket.getInputStream());
    }

    public DataTransferUtils(Socket socket) throws IOException {
        this.socket=socket;
        outStream = new ObjectOutputStream(socket.getOutputStream());
        inStream = new ObjectInputStream(socket.getInputStream());
    }


    public boolean pushFile(File file) throws IOException {

        FileInputStream fileInput = null;

        try {
            fileInput = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            System.err.println("Ficheiro alterado em tempo de execução, abortar operação");
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
        System.out.println(" - File Sent - ");
//        outStream.close();
//        inStream.close();
//        socket.close();
        
        return false;
    }

    public void pullFile(String path,String userType) throws IOException, ClassNotFoundException {

        File file = new File(path);
        int bytes;
        int received = 0;
        int chunkSize = 1024;
        byte[] buffer = new byte[1024];

        // Receive file length
        long fileLength = (Long) inStream.readObject();
        long lastModified = (Long) inStream.readObject();
        if(userType=="cliente"){
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
            }
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

            fileOut.flush();
            fileOut.close();
            file.setLastModified(lastModified);


        }


    }

    public Boolean authClient(String user, String pwd) throws IOException {
        try {
            outStream.writeObject(user);
            outStream.writeObject(pwd);
            outStream.flush();
            return (Boolean) inStream.readObject();
        } catch (Exception e) {
            System.err.println("LOL NAO");
            System.exit(-1);
        }
        return new Boolean(false);
    }

    public String[] getCredentials() {
        String[] credns = null;
        try {
            System.out.println();
            String user = (String)inStream.readObject();
            String passwd = (String)inStream.readObject();
            credns=new String[]{user,passwd};
        } catch (Exception e) {
            System.err.println("LOL NAO");
            System.exit(-1);
        }
        return credns;
    };


    public void sendManifest(String repo, String action) throws Exception {
        DataManifest d= new DataManifest(user,repo,action);
        repo="./"+repo;
        if(new File(repo).isFile()){
            d.addFileManifestManual(repo);
        }
        else if (new File(repo).isDirectory() ){
            d.autoGenerateManifest(repo);
        }
        else{
             outStream.writeObject("ignore");
            System.out.println("Directorio ou ficheiro nao existente");
            }
        outStream.writeObject(d);
        outStream.flush();
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
}

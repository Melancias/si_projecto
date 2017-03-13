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

        byte[] buffer = new byte[1024];

        // Send file length
        outStream.writeLong(fileLength);

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

    public void pullFile(File file) throws IOException {

        int bytes;

        int received = 0;
        int chunkSize = 1024;
        byte[] buffer = new byte[1024];

        // Receive file length
        long fileLength = inStream.readLong();

        FileOutputStream fileOut = new FileOutputStream(file);

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

        System.out.println("Finished - Sent: " + fileLength);

        // Close File Input Stream
        fileOut.flush();
        fileOut.close();
    }

    public Boolean authClient(String user, String pwd) throws IOException {
        try {
            outStream.writeObject(user);
            outStream.writeObject(pwd);
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
        if(new File("./"+repo).isFile()){
            d.addFileManifestManual(repo);
        }
        else{
            d.autoGenerateManifest("./"+repo);
        }
        outStream.writeObject(d);
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
    }

    public DataManifest getManifest() throws IOException, ClassNotFoundException {
        DataManifest d = (DataManifest)inStream.readObject();
        System.out.println(d);
        return d;
    }

    public void sendHandshake() throws IOException {
        outStream.writeObject(new Boolean(true));
    }

    public void share(String share, String argumento, String arg, String arg1) {

    }

    public void remove(String remove, String argumento, String arg, String arg1) {
    }

    public void sendCloseHandshake() throws IOException {
        outStream.writeBoolean(false);
    }
}

import java.io.*;
import java.net.Socket;

/**
 * Created by Melancias on 24/02/2017.
 */
public class myGitUtil {

    private Socket socket;
    private ObjectOutputStream outStream;
    private ObjectInputStream inStream;


    public myGitUtil(String host,int port) throws IOException {
        socket = new Socket(host, port);
        //noinspection Since15
        outStream = new ObjectOutputStream(socket.getOutputStream());
         inStream = new ObjectInputStream(socket.getInputStream());
    }

    public void pushFile(String path) throws IOException {
        //noinspection Since15

        File file       = new File(path);
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
            System.out.println(offset + "/"+ sendLength +" - " + fileLength );

            // Read from File System to buffer
            fileInput.read(buffer, 0, sendLength);
            // Write from buffer to socket
            outStream.write(buffer, 0, sendLength);
            // flush the socket
            outStream.flush();

            offset += sendLength;
        }
        System.out.println(" - File Sent - ");
        outStream.close();
        inStream.close();
        socket.close();
    }

    public Boolean auth(String user, String pwd) throws IOException {
        try {
            System.out.println();
            outStream.writeObject(user);
            outStream.writeObject(pwd);
            return (Boolean) inStream.readObject();
        } catch (Exception e) {
            System.err.println("LOL NAO");
            System.exit(-1);
        }
        return new Boolean(false);
    }

    public static DataManifest scandir(){
        return null;
    }

}



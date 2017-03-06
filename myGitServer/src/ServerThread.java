import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Melancias on 03/03/2017.
 */
class ServerThread extends Thread {

    private Socket socket = null;

    ServerThread(Socket inSoc) {
        socket = inSoc;
        System.out.println("thread do server para cada cliente");
    }

    public void run(){
        try {
            //noinspection Since15
            ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

            String user = null;
            String passwd = null;

            try {
                user = (String)inStream.readObject();
                passwd = (String)inStream.readObject();
                System.out.println("thread: depois de receber a password e o user");
            }catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }
            if (user.equals("b") && passwd.equals("d")){
                outStream.writeObject(new Boolean(true));
                getManifest(outStream,inStream);
            }
            else {
                outStream.writeObject(new Boolean(false));
            }

            //este codigo apenas exemplifica a comunicacao entre o cliente e o servidor
            //nao faz qualquer tipo de autenticacao
            outStream.close();
            inStream.close();

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void receiveFile(ObjectOutputStream outStream,ObjectInputStream inStream ) throws IOException {

            int bytes;

            int received = 0;
            int chunkSize = 1024;
            byte[] buffer = new byte[1024];

            // Receive file length
            long fileLength = inStream.readLong();

            // File Input Stream - To read from the File System
            File file = new File("/Users/Melancias/test.jpg");
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
            fileOut.close();
        }

    public void getManifest(ObjectOutputStream outStream,ObjectInputStream inStream ) throws IOException, ClassNotFoundException {
        DataManifest d = (DataManifest)inStream.readObject();
        System.out.println(d);
        DataManifest.processManifest(d);

    }



}


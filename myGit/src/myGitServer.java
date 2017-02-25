import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Melancias on 25/02/2017.
 */
public class myGitServer {

    public static void main(String[] args) {
        System.out.println("servidor: main");
        myGitServer server = new myGitServer();
        server.startServer();
    }

     public void startServer (){
        ServerSocket sSoc = null;

        try {
            sSoc = new ServerSocket(23456);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }

        while(true) {
            try {
                Socket inSoc = sSoc.accept();
                ServerThread newServerThread = new ServerThread(inSoc);
                newServerThread.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
        //sSoc.close();
    }

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


                //este codigo apenas exemplifica a comunicacao entre o cliente e o servidor
                //nao faz qualquer tipo de autenticacao
                if (user.equals("b") && passwd.equals("d")){
                    outStream.writeObject(new Boolean(true));

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
                else {
                    outStream.writeObject(new Boolean(false));
                }

                outStream.close();
                inStream.close();

                socket.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;



/**
 *
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

}

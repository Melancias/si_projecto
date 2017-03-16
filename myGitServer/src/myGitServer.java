import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * Created by Melancias on 25/02/2017.
 */
public class myGitServer {

    public static void main(String[] args) {;
    int port=23456;
    try{
        port=Integer.parseInt(args[0]);
    }
    catch(Exception e){
        port=23456;
    }
        System.out.println("servidor: main");
        myGitServer server = new myGitServer();
        server.startServer(port);

    }

     public void startServer (int port){
        ServerSocket sSoc = null;

        try {
            sSoc = new ServerSocket(port);
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

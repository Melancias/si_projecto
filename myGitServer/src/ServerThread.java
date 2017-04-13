import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Melancias on 03/03/2017.
 */
class ServerThread extends Thread {

    private Socket socket = null;
    private AuthManager auth= new AuthManager();
    private DataTransferUtils util;
    ServerThread(Socket inSoc) throws IOException {
        socket = inSoc;
        util = new DataTransferUtils(socket);
        System.out.println("thread do server para cada cliente");

    }

    public void run(){
        try {
            //noinspection Since15

            if(!auth.userExists(util.getAccountCheck()))
                util.sendCloseHandshake();
            else
                util.sendHandshake();

            System.out.println("entrou no run");
            String[] crends=util.getCredentials();
            String user = crends[0];
            String passwd = crends[1];
            String action = crends[2];
            System.out.println("Connection established");
            if (auth.authenticate(user,passwd,action)){

                util.sendHandshake();
                if(!util.clientRepoAccessCheck()){
                    System.out.println("Access to repository denied");
                    interrupt();
                    this.stop();
                }
                System.out.println("Getting manifest...");

                Object request = util.getRequest();

                System.out.println("Received!");

                try {
                    DataManifest manifest = (DataManifest) request;

                    if (manifest.action.equals("push")) {
                        ArrayList<String> c = DataManifest.processManifest(manifest);
                        util.sendRequestList(c);
                        for (String s : c) {
                            System.out.println("Receiving files...");
                            if(manifest.autoGenerated){
                                if(manifest.repo.split("/").length==1)
                                    util.pullFile(manifest.user+"/"+manifest.repo + "/" + s, "servidor");

                                else
                                    util.pullFile(manifest.repo + "/" + s, "servidor");
                            }
                            else if (!manifest.autoGenerated & manifest.repo.split("/").length<3){
                                util.pullFile(manifest.user+"/"+manifest.repo,"servidor");
                            }
                            else if (!manifest.autoGenerated & manifest.repo.split("/").length<4){
                                util.pullFile(manifest.repo,"servidor");
                            }
                        }
                        util.sendRequest(0);
                    } else if (manifest.action.equals("pull")) {
                        if (RepoManager.checkRepo(manifest.repo,manifest.user) && RepoManager.isBeingShared(manifest)){
                            manifest=util.sendManifest(manifest.user,manifest.repo,"pull/server");
                            ArrayList<String> c=util.getFileList();
                            for (String s : c) {
                                System.out.println("Sending files...");
                                if(manifest.autoGenerated){
                                    if(manifest.repo.split("/").length==1)
                                        util.pushFile(new File(manifest.user+"/"+manifest.repo + "/" + s));

                                    else
                                        util.pushFile(new File(manifest.repo + "/" + s));
                                }
                                else if (!manifest.autoGenerated & manifest.repo.split("/").length<3){
                                    util.pushFile(new File(manifest.user+"/"+manifest.repo));
                                }
                                else if (!manifest.autoGenerated & manifest.repo.split("/").length<4){
                                    util.pushFile(new File(manifest.repo));
                                }
                            }
                            util.sendRequest(0);

                        }
                        else{
                            util.sendRequest(1);
                        }

                    }


                } catch (ClassCastException e) {

                    String params = (String) request;

                    String[] paramsList = params.split(":");

                    String command = paramsList[0];

                    if (command.equals("share")) {
                        try {
                            if (RepoManager.shareWith(paramsList[2], paramsList[3], paramsList[1])) {
                                util.sendRequest(0);
                                System.out.println("Shared done.");
                            }else{
                                util.sendRequest(-1);
                            }
                        } catch (Exception o) {
                            o.printStackTrace();
                        }
                    }else if(command.equals("remove")){
                        try{
                            if(RepoManager.removeAccessToUser(paramsList[2], paramsList[3], paramsList[1])) {
                                util.sendRequest(0);
                                System.out.println("Remove done.");
                            }else
                                util.sendRequest(-1);

                        }catch (Exception o){
                            o.printStackTrace();
                        }

                    }



            //este codigo apenas exemplifica a comunicacao entre o cliente e o servidor
            //nao faz qualquer tipo de autenticacao

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


            }
            //auth falhou
            else{util.sendCloseHandshake();}

            }
            catch(Exception e){
                Exception newasd=e;
                e.printStackTrace();
            }

        }

}


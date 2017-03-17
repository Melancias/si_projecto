import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Melancias on 21/02/2017.
 */

public class myGit {

    public static void main (String[] args) throws Exception {
        String argumento =args[0];
        if(argumento.equals("-init")){
            try{
                Path currentRelativePath = Paths.get("");
                String s = currentRelativePath.toAbsolutePath().toString();
                System.out.println(s);
                File f = new File(s + "/"+ args[1]);
                    if (!f.exists()) {
                        f.mkdir();
                        System.out.println("O repositorio " + args[1] + "  foi criado localmente");
                    } else
                        System.out.println("O repositorio ja existe");


            }catch (Exception e){
                System.out.println("Nao foi possivel criar o repositorio");
            }

        }
        else{
            String[] address;
            String host = null;
            int port = 0;
            String localUser=null;
            try {
                address = args[1].split(":");
                host = address[0];
                port = Integer.parseInt(address[1]);
                localUser = args[0];
            }
            catch(Exception e){
                System.out.println("Endereço ou porta incorrecto");
            }
            DataTransferUtils util = null;
            util = new DataTransferUtils(host, port, localUser);
            if(args.length < 5){
                System.out.println("O utilizador "+argumento+" autorizado");
                System.out.println("Confirmar password do utilizador " + args[0] + ": ");
                Scanner s = new Scanner(System.in);
                String pwd = s.nextLine();
                try{
                    if(pwd.equals(args[3])){
                        if(util.authClient(argumento, pwd))
                            System.out.println("O utilizador " + argumento + " foi criado");
                    }
                }catch(Exception e){
                    e.getStackTrace();
                }

            }else{
                String repo = args[5];
                if (!util.authClient(argumento, args[3])) {
                    // util.createUser(argumento, args[3]);
                    System.out.println("Authentication failed");
                    System.exit(-1);
                }
                if(args[4].equals("-push")){
                    util.sendManifest(repo,"push");
                    System.out.println("sending manifest");
                    ArrayList<String> fileList = util.getFileList();
                    try{
                        for (String file : fileList){
                            System.out.println("A enviar " + file);
                            util.pushFile(new File(repo+"/"+file));
                        }
                    }catch (Exception e){
                        System.out.println("Nao foi possivel enviar " + args[5]);
                    }


                }else if(args[4].equals("-pull")){
                    util.sendManifest(repo,"pull");
                    System.out.println("sending manifest");
                    ArrayList<String> fileList = util.getFileList();
                    try {
                        for (String file : fileList) {
                            System.out.println("A enviar " + file);
//                            util.pullFile(repo + "/" + file);
                        }
                    }catch (Exception e){
                        System.out.println("Nao foi posssivel copiar " + args[5] + "do servidor");
                    }

                }else if(args[4].equals("-share")){
                    try{
                        util.share("share", argumento, args[5], args[6]);
                    }catch (Exception e){
                        System.out.println("Ocorreu um erro");
                    }

                }else if(args[4].equals("-remove"))
                    try{
                        util.remove("remove", argumento, args[5], args[6]);
                    }catch (Exception e){
                        System.out.println("Ocorreu um erro");
                    }

            }
        }
    }

}


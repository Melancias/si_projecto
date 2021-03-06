import java.io.EOFException;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by Melancias on 21/02/2017.
 */


public class myGit {
    public static void main (String[] args) throws Exception {
        System.out.println("myGit Client");
        ArrayList<String> argsVerification=new ArrayList<String>(Arrays.asList(args));
        if( argsCheckVerification(argsVerification)) {
            System.out.println("myGit Client couldn't parse your arguments");
            System.exit(-1);
        }
        String argumento =args[0];

        if(argumento.equals("-init")){
            try{
                Path currentRelativePath = Paths.get("");
                String s = currentRelativePath.toAbsolutePath().toString();
                System.out.println(s);
                File f = new File(s + "/"+ args[1]);
                    if (!f.exists()) {
                        f.mkdir();
                        System.out.println(args[1] + " created locally.");
                    } else {
                        System.out.println(args[1]+ " already exists");
                    }

            }catch (Exception e){
                System.out.println("Error: Could not create repository");
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
                System.err.println("Error: Incorrect Host or Port");
                System.exit(-1);
            }
            DataTransferUtils util = null;
            try {
                util = new DataTransferUtils(host, port, localUser);
            } catch (javax.net.ssl.SSLException e) {
                System.err.println("Error: Incorrect Certificate.");
                System.exit(-1);
            } catch (java.net.ConnectException e){
                System.err.println("Error: Could not connect to server.");
                System.exit(-1);
            }
            boolean loginRegister=false;

            if(!util.accountCheck(args[0])){
                System.out.println("Creating account: " + argumento + " due to non-existence");
                System.out.println("Registering "+argumento);
                System.out.println("Confirm password " + args[0] + ": ");
                Scanner s = new Scanner(System.in);
                String pwd = s.nextLine();

                try{
                    if(pwd.equals(args[3])){
                        if(util.authClient(argumento, pwd,"register")) {
                            System.out.println(argumento + " registered!");
                            loginRegister = true;
                        }
                        else{
                            System.out.println(argumento + " register failed!");
                        }
                    }
                    else{
                        System.err.println("Error: Passwords don't match");
                        System.exit(-1);
                    }
                }catch(Exception e){
                    e.getStackTrace();
                    }

            }
            if (args.length>5){
                String repo = args[5];
                if (!loginRegister){
                    boolean answer= !util.authClient(argumento, args[3], "login");
                    if (answer) {
                        // util.createUser(argumento, args[3]);
                        System.err.println("Error: Authentication failed");
                        System.exit(-1);
                    }
                }
                if(!util.checkRepoAcess(repo,localUser,args[4])){
                    System.err.println("Error: You don't have access to the repository");
                    System.exit(-1);
                }
                if(args[4].equals("-push")){
                    Object answer=util.sendManifest(localUser,repo,"push",null);
                    if(answer==null){
                        System.err.println("Error: Repository doesn't exist locally");
                        System.exit(-1);
                    }
                    ArrayList<String> fileList = util.getFileList();
                    try{
                        for (String file : fileList){
                            System.out.println("Pushing: " + file);
                            if(new File(repo).isFile()){

                                File signature= DataTransferUtils.generateSignature(new File(repo).getAbsolutePath(),localUser);
                                util.pushFile(signature);
                                signature.delete();
                                File cipheredKey= DataTransferUtils.generateCipherKey(repo);
                                File cipheredFile= DataTransferUtils.cipherFile(cipheredKey,new File(repo));
                                util.pushFile(cipheredKey);
                                util.pushFile(cipheredFile);
                                cipheredFile.delete();
                                cipheredKey.delete();

                            }
                            else{
                                File signature= DataTransferUtils.generateSignature(new File(repo+"/"+file).getAbsolutePath(),localUser);
                                util.pushFile(signature);
                                signature.delete();
                                File cipheredKey= DataTransferUtils.generateCipherKey(repo+"/"+file);
                                File cipheredFile= DataTransferUtils.cipherFile(cipheredKey,new File(repo+"/"+file));
                                util.pushFile(cipheredKey);
                                util.pushFile(cipheredFile);
                                cipheredFile.delete();
                                cipheredKey.delete();
                            }

                        }
                        int r = util.receiveAnswer();
                        if(r == 0){
                            System.out.println(args[5] + " pushed");
                        }else{
                            System.out.println("Error: Push failed");
                        }

                    }catch (Exception e){
                        e.printStackTrace();
                        System.out.println("Error: Could not push " + args[5]);
                    }


                }else if(args[4].equals("-pull")){
                    //enviar manifesto vazio
                    util.sendManifest(localUser,repo,"pull",null);
                    Object request = util.getRequest();
                    DataManifest manifest=null;
                    try{
                    manifest = (DataManifest) request;}

                    catch(Exception e){
                        if (0 == (Integer) request || 1 == (Integer) request){
                            System.out.println("Error: Repository does not exist");
                            System.exit(0);
                        }
                    }
                    ArrayList<String> fileList= DataManifest.processManifest(manifest);
                    //add sig files and key files here
                    try {
                        util.sendRequestList(fileList);
                        for (String file : fileList) {
                            System.out.println("Pulling: " + file);
                            if(manifest.autoGenerated) {
                                long date=util.pullFile(manifest.repo + "/" + file, "cliente");
                                util.getRequest();
                                util.pullFile(manifest.repo + "/" + file+".sig", "cliente");
                                util.getRequest();
                                util.pullFile(manifest.repo + "/" + file+".key", "cliente");

                                DataTransferUtils.decipherFile(new File(manifest.repo + "/" + file+".key"),new File(manifest.repo + "/" + file),date);
                                if(!DataTransferUtils.checkSignature(manifest.repo + "/" + file,finduser(manifest))){
                                    throw new Exception("File signature was corrupted");

                                };
                            }
                            else {
                                long date=util.pullFile(manifest.repo, "cliente");
                                util.getRequest();
                                util.pullFile(manifest.repo+".sig", "cliente");
                                util.getRequest();
                                util.pullFile(manifest.repo + ".key", "cliente");
                                DataTransferUtils.decipherFile(new File(manifest.repo + ".key"),new File(manifest.repo), date);
                                if(!DataTransferUtils.checkSignature(manifest.repo,finduser(manifest))){
                                    System.err.println("File signature was corrupted");
                                    throw new Exception();
                                }
                            }
                        }

                        int r = util.receiveAnswer();
                        if(r == 0){
                            System.out.println("Success: " + args[5] + " received ");
                        }else{
                            System.out.println("Error: Pull failed");
                        }
                    } catch(EOFException e){
                        DataManifest.cleanup(repo);
                        System.err.println("Error: Internal Server Error Occurred.");
                    }catch (Exception e){
                        //e.printStackTrace();
                        DataManifest.cleanup(repo);
                        System.out.println("Error: Pull failed");

                    }

                }else if(args[4].equals("-share")){
                    try{
                        util.share("share", argumento, args[5], args[6]);
                        int r = util.receiveAnswer();
                        if(r == 0)
                            System.out.println("Success: " + args[5] + " shared with " + args[6]);
                        else
                            System.out.println("Error: Either " + args[6] + " already has access, is not registered, or you don't own the repository.");
                    }catch (Exception e){
                        System.out.println("Error");
                        e.printStackTrace();
                    }

                }else if(args[4].equals("-remove"))
                    try{
                        util.remove("remove", argumento, args[5], args[6]);
                        int r = util.receiveAnswer();
                        if(r == 0){
                            System.out.println("Success: Removed access to " + args[6]);
                        }else
                            System.out.println("Error: Either " + args[6] + "has no access, does not exist, or you don't own the repository.");
                    }catch (Exception e){
                        System.out.println("Error");
                        e.printStackTrace();
                    }
            }
        }
    }



    private static boolean argsCheckVerification(ArrayList<String> argsVerification){
        boolean answer=false;
        if(argsVerification.size()<2){
            return true;
        }
        if ((argsVerification.contains("-pull") || argsVerification.contains("-push") ||argsVerification.contains("-share"))|| argsVerification.contains("-remove") ){
            if (!(argsVerification.contains("-p"))) {
                System.out.println("Missing password");
                answer = true;
            }
        }
        if ((argsVerification.contains("-pull") || argsVerification.contains("-push")) && argsVerification.size()<6 ) {
            System.out.println("Incomplete arguments");
            answer = true;
        }
        if (((argsVerification.contains("-share")|| argsVerification.contains("-remove")) && argsVerification.size()<6)) {
            System.out.println("Incomplete arguments");
            answer = true;
        }
        if (argsVerification.get(1).split(":").length==1 && !argsVerification.contains("-init")){
            System.out.println("Destination port is missing");
            answer=true;
        }
        if (argsVerification.get(0).split("/").length>1  && !argsVerification.contains("-init")) {
            System.out.println("Username cannot have backslash on it");
            answer = true;
        }
        return answer;
    }

    private static String finduser(DataManifest manifest){
        String answer="";
        switch(manifest.whohasit){
            case LOCAL: answer=manifest.user;
                break;
            case REMOTE: answer=manifest.repo.split("/")[0];
                break;
            case REMOTEFILE:answer=manifest.repo.split("/")[0];
                break;
            case LOCALFILE: answer=manifest.user;
                break;
        }
        return answer;
    }
}


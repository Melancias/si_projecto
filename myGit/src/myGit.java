import java.io.IOException;

/**
 * Created by Melancias on 21/02/2017.
 */

public class myGit {

    public static void main (String[] args) throws Exception {
        String argumento =args[0];
        String user="b";
        String pwd="d";
        myGitUtil util= new myGitUtil("localhost",23456,user);
        util.auth(user,pwd);
        if (argumento.equals("-pull")) {
            System.out.println("Pull");
            util.sendManifest("testrepo","pull");
        } else if (argumento.equals("-push")) {
            System.out.println("Push");
            util.sendManifest("testrepo","push");
        } else {
            System.out.println("nada");
        }
    }

}


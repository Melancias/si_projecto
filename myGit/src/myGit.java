import java.io.IOException;

/**
 * Created by Melancias on 21/02/2017.
 */
public class myGit {

    public static void main (String[] args) throws IOException {
        String argumento =args[0];
        myGitUtil util= new myGitUtil("localhost",23456);
        util.auth("b","d");
        if (argumento.equals("-pull")) {
            System.out.println("Pull");

            util.pushFile("/Users/Melancias/mpv-shot0001.jpg");

        } else if (argumento.equals("-push")) {
            System.out.println("Push");

        } else {
            System.out.println("nada");

        }

    }
}


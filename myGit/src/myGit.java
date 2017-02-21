/**
 * Created by Melancias on 21/02/2017.
 */
public class myGit {

    public static void main (String[] args){
        String argumento =args[0];
        if (argumento.equals("-pull")) {
            System.out.println("Pull");

        } else if (argumento.equals("-push")) {
            System.out.println("Push");

        } else {
            System.out.println("nada");

        }
    }
}


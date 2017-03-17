import java.io.*;

/**
 * Created by Melancias on 03/03/2017.
 */

public class AuthManager {

    private File authFile;

    public AuthManager() {
        this.authFile = new File("./.authFile");
    }


    public boolean userExists(String username, String pwd) throws IOException {
        BufferedReader authReader = new BufferedReader(new FileReader(authFile));

        // Read each line until credentials match
        boolean resp = false;
        for (String line; (line = authReader.readLine()) != null;){

            String[] credentials = line.split("\\:");
            if (credentials[0].equals(username)) {
                if (credentials[1].equals(pwd)) {
                    resp =true;
                } else {
                    resp = false;
                }
            }
            resp = false;
        }
        return resp;
    }

    public boolean authenticate(String username, String password){

        try {
            BufferedReader authReader = new BufferedReader(new FileReader(authFile));

            // Read each line until credentials match
            for (String line; (line = authReader.readLine()) != null;){
                String[] credentials = line.split("\\:");
                if (credentials[0].equals(username)) {
                    if (credentials[1].equals(password)) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
            // In case credentials not found, register
            return register(username, password);

        } catch (FileNotFoundException e) {
            createAuthFile();
            return register(username, password);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;

    }

    private boolean register(String username, String password){
        try {
            FileWriter authWriter = new FileWriter(authFile, true);
            String credentials = username+":"+password;
            authWriter.append(credentials);
            authWriter.write(System.lineSeparator());
            authWriter.flush();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private boolean createAuthFile(){
        try {
            return authFile.createNewFile();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}

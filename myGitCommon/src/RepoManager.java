import java.io.*;

/**
 * Created by pedro on 11-03-2017.
 */
public class RepoManager {


    static boolean isBeingShared(String repoPath, String username){

        File shareFile = new File(repoPath + "/.shared");
        try {
            BufferedReader authReader = new BufferedReader(new FileReader(shareFile));

            for (String line; (line = authReader.readLine()) != null;){
                if (line.equals(username)) {
                    return true;
                }
            }

        } catch (FileNotFoundException e) {
            createShareFile(repoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    static boolean shareCheck(String repoPath, String username) throws IOException {
        String[] structure = repoPath.split("/");
        if (structure.length<2){return true;}
        File shareFile = new File(structure[0]+"/"+structure[1] + "/.shared");
        if(!shareFile.exists()){
            shareFile= new File(username+"/"+structure[0]);
        }
        try {
            BufferedReader authReader = new BufferedReader(new FileReader(shareFile));

            for (String line; (line = authReader.readLine()) != null; ) {
                if (line.equals(username)) {
                    return true;
                }
            }
        }
        catch(Exception e)
            {
                System.out.println("Erro a verificar a partilha");
                return false;
            }
        return false;
    }



     static boolean shareWith(String repoPath, String username, String owner){

        File shareFile = new File(owner+"/"+repoPath + "/.shared");

        boolean answer=false;
        try {
            BufferedReader readerBuffer = new BufferedReader(new FileReader(shareFile));
            String client = readerBuffer.readLine();
            if(client.equals(owner)){
                FileWriter shareWriter = new FileWriter(shareFile, true);

                // If already shared with user
                if(!isBeingShared(owner+"/"+repoPath, username) & AuthManager.userExists(username)){
                    shareWriter.append(username + "\n");
                    shareWriter.append(System.lineSeparator());
                    shareWriter.flush();
                    System.out.println("Partilhado com : " + username);
                    answer=true;
                }

                shareWriter.close();
            }


        } catch (IOException e) {
            System.out.println("Erro a ler .shared");
        }
        return answer;
    }


     static boolean createShareFile(String repoPath){
        File shareFile = new File(repoPath + "/.shared");

        try {
            shareFile.createNewFile();;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
     }

     static void manageVersions(File file) throws IOException {

         // If .2 exists
         if(new File(file.getAbsolutePath()+".2").exists()){

             // Delete .2
             new File(file.getAbsolutePath()+".2").delete();

             // Rename .1 to .2
             new File(file.getAbsolutePath()+".1").renameTo(new File(file.getAbsolutePath()+".2"));

             // And current version to .1
             file.renameTo(new File(file.getAbsolutePath()+".1"));

         // If only .1 exists
         } else if(new File(file.getAbsolutePath()+".1").exists()) {

             // rename .1 to .2
             new File(file.getAbsolutePath()+".1").renameTo(new File(file.getAbsolutePath()+".2"));

             // And current version to .1
             file.renameTo(new File(file.getAbsolutePath()+".1"));
         } else {
             file.renameTo(new File(file.getAbsolutePath()+".1"));
         }
     }

     static void createUserFolder(String username){
         String path = "./"+username;
         File dir    =  new File(path);
         dir.mkdir();
     }

     static void createRepo(String repo, String username) throws IOException {
         File dir    =  new File(repo);
         dir.mkdirs();
         createShareFile(repo);
         //criacao e adicao da conta
         File shareFile = new File(repo + "/.shared");
         FileWriter shareWriter = new FileWriter(shareFile, true);
         shareWriter.append(username + "\n");
         shareWriter.flush();
         shareWriter.close();

//         shareWith(repo, username, username);
     }

     static boolean checkRepo(String repo,String user){
         File test1=new File(repo);
         File test2=new File(user+"/"+repo);
        return test1.exists()||test2.exists();
     }


    static boolean removeAccessToUser(String path, String username, String client) throws IOException {
        File file    = new File(client + "/" + path+"/.shared");
        //File tempFile = new File(client + "/"+path+"/.temp_shared");
        String s = "";

        BufferedReader readerBuffer = new BufferedReader(new FileReader(file));
        String owner = readerBuffer.readLine();

        if (owner.equals(client)){
            s += owner + ";";
            String line = "";
            // Writes every line except the one to be removed
            while (( line = readerBuffer.readLine()) != null) {
                System.out.println(line);
                if (!line.equals(username)) {
                    s += line + ";";
                }
            }
            readerBuffer.close();
            BufferedWriter writerBuffer = new BufferedWriter(new FileWriter(file));

            System.out.println(s);
            for (String i : s.split(";")){
                writerBuffer.write(i);
                writerBuffer.append(System.lineSeparator());

            }
            writerBuffer.close();
            return true;
        }   else {
            return false;
        }
    }

}

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

     static boolean shareWith(String repoPath, String username, String owner){

        File shareFile = new File(repoPath + "/.shared");


        try {
            if(shareFile.length() != 0){
                BufferedReader readerBuffer = new BufferedReader(new FileReader(shareFile));
                String client = readerBuffer.readLine();

                if(client.equals(owner)){
                    FileWriter shareWriter = new FileWriter(shareFile, true);

                    // If already shared with user
                    if(!isBeingShared(repoPath, username)){
                        shareWriter.append(username + "\n");
                        shareWriter.flush();
                    }
                    shareWriter.close();
                }
            }else{
                FileWriter shareWriter = new FileWriter(shareFile, true);

                // If already shared with user
                if(!isBeingShared(repoPath, username)){
                    shareWriter.append(username + "\n");
                    shareWriter.flush();
                }
                shareWriter.close();
            }


        } catch (IOException e) {
            return false;
        }
        return true;
    }


     static boolean createShareFile(String repoPath){
        File shareFile = new File(repoPath + "/.shared");

        try {
            return shareFile.createNewFile();
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

     static void createUserFolder(String repo, String username){
         String path = "./"+username;
         File dir    =  new File(path);
         dir.mkdir();
     }

     static void createRepo(String repo, String username){
         File dir    =  new File(repo);
         dir.mkdirs();

         createShareFile(repo);
         shareWith(repo, username, username);
     }

    static boolean removeAccessToUser(String path, String username, String client) throws IOException {
        File file    = new File(path+"/.shared");
        File tempFile = new File(path+"/.temp_shared");

        BufferedReader readerBuffer = new BufferedReader(new FileReader(file));
        BufferedWriter writerBuffer = new BufferedWriter(new FileWriter(tempFile));

        String owner = readerBuffer.readLine();

        if (owner.equals(client)){

            // Dono
            writerBuffer.write( owner + System.getProperty("line.separator"));
            String line;

            // Writes every line except the one to be removed
            while ((line = readerBuffer.readLine()) != null) {
                System.out.println(line);
                if (!line.equals(username)) {
                    writerBuffer.write(line + System.getProperty("line.separator"));
                }
            }

            // Replace tempFile with original file
            tempFile.renameTo(file);
            writerBuffer.close();
            readerBuffer.close();

            return true;
        }   else {
            return false;
        }
    }

}

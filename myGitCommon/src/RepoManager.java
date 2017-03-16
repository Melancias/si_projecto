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

     static boolean shareWith(String repoPath, String username){

        File shareFile = new File(repoPath + "/.shared");

        try {
            FileWriter shareWriter = new FileWriter(shareFile, true);

            // If already shared with user
            if(!isBeingShared(repoPath, username)){
                shareWriter.append(username + "\n");
                shareWriter.flush();
            }
            shareWriter.close();
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

}

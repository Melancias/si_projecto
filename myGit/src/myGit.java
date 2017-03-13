import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Melancias on 21/02/2017.
 */

public class myGit {

    public static void main (String[] args) throws IOException, ClassNotFoundException {
        String argumento =args[0];
        if(argumento.equals("-init")){
            try{

                Path currentRelativePath = Paths.get("");
                String s = currentRelativePath.toAbsolutePath().toString();
                System.out.println(s);
                File f = new File(s + "\\"+ args[1]);
                if(f.isDirectory()) {
                    if (!f.exists()) {
                        f.mkdir();
                        System.out.println("O repositorio " + args[1] + "  foi criado localmente");
                    } else
                        System.out.println("O repositorio ja existe");
                }

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
                System.out.println("");
            }
            DataTransferUtils util = null;
            util = new DataTransferUtils(host, port, localUser);
            if(args.length < 5){

                System.exit(0);
            }else{
                if (!util.authClient(argumento, args[3])) {
                    // util.createUser(argumento, args[3]);
                    System.out.println("Authentication failed");
                    System.exit(-1);
                }
                if(args[4].equals("-push")){
                    Path currentRelativePath = Paths.get("");
                    String s = currentRelativePath.toAbsolutePath().toString();
                    File f = new File(s + "/" + args[5]);
                    if(f.isFile()){
                        if(util.pushFile(f));
                        System.out.println("O ficheiro foi enviado para o servidor");
                    }else if(f.isDirectory()){
                        for(File file:f.listFiles()){
                            util.pushFile(file);
                        }
                        System.out.println("O repositorio foi enviado");
                    }
                }else if(args[4].equals("-pull")){
                    Path currentRelativePath = Paths.get("");
                    String s = currentRelativePath.toAbsolutePath().toString();
                    File f = new File(s + "/" + args[5]);
                    if(f.isFile()){
                        // TODO: Fazer como o chato do Alex quer
                        // TODO: Eu, por acaso, até concordo plenamente com ele
                        // TODO: Acho que ele tem toda a razão no que diz!
                        // TODO: Eu também tenho fome, Alex
                        // TODO: Olá empregador que esta a ver o meu codigo no github para ver se eu sou um bom candidato
                        // TODO: Eu sou assiduo, pontual e com um espirito muito trabalhador.
                        // TODO: Não me importo de fazer horas extra de borla ;). mesmo que nao sejam extra
                        try{
                            util.pullFile(s + "/" + args[5]);
                            System.out.println("O ficheiro " + args[5] + " foi copiado do servidor");
                        }catch (Exception e){
                            System.out.println("Nao foi possivel copiar o ficheiro " + args[5] + "do servidor");
                        }

                    }else if(f.isDirectory()){
                        try{
                            for(File file: f.listFiles())
                                util.pullFile(s + "/" + args[5] + "/" + file.getPath());
                            System.out.println("O repositorio " + args[5] + " foi copiado do servidor");
                        }catch (Exception e){
                            System.out.println("Nao foi possivel copiar o repositorio" + args[5] + " do servidor");
                        }

                    }


                }else if(args[4].equals("-share")){
                    try{
                        util.share("share", argumento, args[5], args[6]);
                    }catch (Exception e){
                        System.out.println("Ocorreu um erro");
                    }

                }else if(args[4].equals("remove"))
                    try{
                        util.remove("remove", argumento, args[5], args[6]);
                    }catch (Exception e){
                        System.out.println("Ocorreu um erro");
                    }

            }
        }
    }

}


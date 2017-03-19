Para importar o projecto no eclipse, basta importar utilizando a opção Existing Projects into
Workspace, apontando o eclipse para esta pasta e com a opção Search for nested projects.

Para executar no eclipse basta pedir para fazer Run As nas classes java myGit e myGitServer com os respectivos cliente e
server.policy e o argumento "-Djava.security.manager -Djava.security.policy=<ficheiro de policy respectivo> [argumentos]"

Para executar na linha de comando, recomenda-se compilar em JAR os modulos myGit e myGitServer e executar cada um da
maneira seguinte:

"java -jar myGit.jar||myGitServer.jar -Djava.security.manager -Djava.security.policy=<ficheiro de policy respectivo> [argumentos]"

Incluido vão dois JAR precompilados para conveniencia:

out/artifacts/myGit_jar/myGit.jar
out/artifacts/myGitServer_jar/myGitServer.jar

É necessario correr o executavel do servidor num directorio diferente do cliente.
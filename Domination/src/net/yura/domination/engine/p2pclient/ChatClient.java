package net.yura.domination.engine.p2pclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import net.yura.domination.engine.OnlineRisk;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.RiskGame;

/**
 * @author Yura Mamyrin
 */
public class ChatClient implements OnlineRisk {

        private String myUID;

    	private PrintWriter outChat = null;
	private BufferedReader inChat = null;
	private Socket chatSocket = null;
	private ChatDisplayThread myReader = null;

        public ChatClient(Risk risk,String uid,String host, int port) throws Exception {
                myUID = uid;

                chatSocket = new Socket( host , port);

                // Create a PrintWriter object for socket output

                outChat = new PrintWriter(new OutputStreamWriter(chatSocket.getOutputStream(), RiskUtil.UTF_8), true);

                // Create a BufferedReader object for socket input

                inChat = new BufferedReader(new InputStreamReader(chatSocket.getInputStream(), RiskUtil.UTF_8));

                myReader = new ChatDisplayThread(risk, inChat);
                myReader.start();

                outChat.println( RiskGame.NETWORK_VERSION +" "+uid+" "+RiskGame.getDefaultMap() );
        }

        public void sendGameCommand(String gameCommand) {
            outChat.println(gameCommand);
        }

        public void sendUserCommand(String command) {
            outChat.println(myUID + " " + command);
        }

        public void closeGame() {
            try {
                    outChat.close();
                    inChat.close();

                    chatSocket.shutdownInput();
                    chatSocket.shutdownOutput();

                    chatSocket.close();
            }
            catch (IOException except) { }

            chatSocket = null;
        }

        public void playerRenamed(String oldName, String newName, String newAddress, int newType) {
            // dont care
        }
}

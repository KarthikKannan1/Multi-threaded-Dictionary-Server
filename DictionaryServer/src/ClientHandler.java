import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import org.json.JSONObject;
import java.net.Socket;
import java.util.List;
import org.json.JSONArray;

/**
 * @author [Karthik Kannan]
 * Student ID: [1739619]
 */

public class ClientHandler extends Thread {
	private Socket clientSocket;
	private Dictionary dictionary;
	
	ClientHandler (Socket clientSocket, Dictionary dictionary) {
		this.clientSocket = clientSocket;
		this.dictionary = dictionary;
	}
	
	public void run () {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
			String request;
			
			while ((request = in.readLine()) != null) {
				JSONObject json = new JSONObject(request);
				String action = json.getString("action");
				String word = json.getString("word");
				JSONObject response = new JSONObject();
				
				if (action.equals("SEARCH")) {
					List<String> meanings = dictionary.search(word);
					if (meanings == null) { 
						response.put("status", "ERROR: word not found.");
					}
					else {
						response.put("status", "SUCCESS");
						response.put("meanings", new JSONArray(meanings));
					}	
				}
				
				else if (action.equals("ADD_WORD")) {
					String meaning = json.getString("meaning");
					List<String> meanings = new java.util.ArrayList<>();
					for (String mean : meaning.split(",")) {
						meanings.add(mean.trim());
					}
					String result = dictionary.addWord(word, meanings);
				    response.put("status", result);
				}
				
				else if (action.equals("REMOVE_WORD")) {
					String result = dictionary.removeWord(word);
					response.put("status", result);
				}
				
				else if (action.equals("ADD_MEANING")) {
					String meaning = json.getString("meaning");
					String result = dictionary.addMeaning(word, meaning);
					response.put("status", result);
				}
				else if (action.equals("UPDATE_MEANING")) {
					String oldMeaning = json.getString("oldMeaning");
                    String newMeaning = json.getString("newMeaning");
                    String result = dictionary.updateMeaning(word, oldMeaning, newMeaning);
                    response.put("status", result);
                }
                else {
                    response.put("status", "ERROR");
                    response.put("message", "Unknown action");
                }
				out.println(response.toString());
			}
		}
		catch (Exception e) {
            System.err.println("Client error: " + e.getMessage());
            }
		}
	}
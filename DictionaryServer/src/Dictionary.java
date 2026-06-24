import org.json.JSONObject;
import org.json.JSONArray;
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

/**
 * @author [Karthik Kannan]
 * Student ID: [1739619]
 */


public class Dictionary {
	private HashMap<String, List<String>> dictionary = new HashMap<>(); // To store words as keys and a list of meanings as values.
	private final ReadWriteLock lock = new ReentrantReadWriteLock(); 
	private String filePath;                                            // To store the JSON file location.
	
	Dictionary (String filePath) {
		this.filePath = filePath;
	}
	
	public void loadDictionary() {
	    try {
	        BufferedReader in = new BufferedReader(new FileReader(filePath));
	        StringBuilder sb = new StringBuilder();
	        String line;
	        
	        while ((line = in.readLine()) != null) {
	            sb.append(line);                             // Creating a string with lines from the file.
	        }
	        in.close();
	        
	        JSONObject json = new JSONObject(sb.toString());  // Passing the string into a json dict format.
	        
	        for (String word : json.keySet()) {               // Extracting the word which is set as the dict key.
	            List<String> meanings = new ArrayList<>();    // List to store all the meanings.
	            JSONArray meaning = json.getJSONArray(word);  // Extracting the meaning out of the respective word.
	            for (int i = 0; i < meaning.length(); i++) {  
	                meanings.add(meaning.getString(i));       // Creating a list of meanings.
	            }
	            dictionary.put(word, meanings);               // Creating a dict with each word and its respective meaning mapped accordingly. 
	        }
	        // Optional debug line to check the contents of the dictionary 
	        // System.out.println("Words loaded: " + dictionary.keySet()); 
	    } 
	    catch (IOException e) {
	        System.err.println("Error loading the dictionary: " + e.getMessage());
	    }
	}
	
	public void saveDictionary() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
			JSONObject json = new JSONObject();
	        
	        for (String word : dictionary.keySet()) {         // Iterate through the created dictionary hashmap.
	        	List<String> meanings = dictionary.get(word); // Retrieve the meanings from the dict.
	        	json.put(word, new JSONArray(meanings));      
	        }
	        out.write(json.toString());
	        out.close();     
	    } 
		catch (IOException e) {
	        System.err.println("Error saving the dictionary: " + e.getMessage());
	    }
	}
	
	public List<String> search(String word) {
        lock.readLock().lock();                               // Acquire READ lock - multiple reads can happen concurrently
        try {
        	if (dictionary.containsKey(word)) {
        		return dictionary.get(word);
        	}
        	else {
        		return null;
        	}
        } 
        finally {
            lock.readLock().unlock();
            } 
	}
	
	public String addWord(String word, List<String> meanings) {
		lock.writeLock().lock();
		try {
			if (dictionary.containsKey(word)) {
				return "DUPLICATE";
			}
			else if (meanings.isEmpty()) {
				return "ERROR: meaning of the word has not been entered.";
			}
			else {
				dictionary.put(word, meanings);
				saveDictionary();
				return "SUCCESS";
			}
		} 
		finally {
            lock.writeLock().unlock();
            } 
	}
	
	public String removeWord(String word) {
		lock.writeLock().lock();
		try {
			if (dictionary.containsKey(word)) {
				dictionary.remove(word);
				saveDictionary();
				return "SUCCESS";
			}
			else {
				return "ERROR: word not found.";
			}
		}
		finally {
            lock.writeLock().unlock();
            }
	}
	
	public String addMeaning(String word, String meaning) {
		lock.writeLock().lock();
		try {
			if (!dictionary.containsKey(word)) {
				return "ERROR: word not found.";
			}
			List<String> meanings = dictionary.get(word);
			if (meanings.contains(meaning)) {
				return "DUPLICATE";
			}
			meanings.add(meaning);
			saveDictionary ();
			return "SUCCESS";
		}
		finally {
            lock.writeLock().unlock();
            }
	}
	
	public String updateMeaning(String word, String oldMeaning, String newMeaning) {
		lock.writeLock().lock();
		try {
			if (!dictionary.containsKey(word)) {
				return "ERROR: word not found.";
			}
			List<String> meanings = dictionary.get(word);
			if (!meanings.contains(oldMeaning)) {
				return "ERROR: the original meaning can't be found";
			}
			int index = meanings.indexOf(oldMeaning);
			meanings.set(index, newMeaning);
			saveDictionary();
			return "SUCCESS";
		}
		finally {
            lock.writeLock().unlock();
		}
	}
}
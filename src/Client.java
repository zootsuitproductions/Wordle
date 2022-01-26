import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.json.*;


public class Client {

  private static final String[] protocol = new String[]{"TLSv1.3"};
  private static final String[] suites = new String[]{"TLS_AES_128_GCM_SHA256"};

  private static String hostname;
  private static String username;
  private static int port = 27993;
  private static boolean encrypted = false;

  private static void parseArgs(String[] args) {
    switch (args.length) {
      case 2:
        hostname = args[0];
        username = args[1];
        break;
      case 3:
        hostname = args[1];
        username = args[2];
        if (args[0].equals("-s")) {
          encrypted = true;
          port = 27994;
        } else {
          System.out.println("Invalid arguments: ./client <-p port> <-s> <hostname> <Northeastern-username>");
          throw new IllegalArgumentException();
        }
        break;
      case 4:
        hostname = args[2];
        username = args[3];
        if (args[0].equals("-p")) {
          try {
            port = Integer.parseInt(args[1]);
          } catch (NumberFormatException e) {
            System.out.println("Invalid port: ./client <-p port> <-s> <hostname> <Northeastern-username>");
            throw new IllegalArgumentException();
          }
        } else {
          System.out.println("Invalid arguments: ./client <-p port> <-s> <hostname> <Northeastern-username>");
          throw new IllegalArgumentException();
        }
        break;
      case 5:
        hostname = args[3];
        username = args[4];
        if (args[0].equals("-p")) {
          try {
            port = Integer.parseInt(args[1]);
          } catch (NumberFormatException e) {
            System.out.println("Invalid port: ./client <-p port> <-s> <hostname> <Northeastern-username>");
            throw new IllegalArgumentException();
          }
          if (args[2].equals("-s")) {
            encrypted = true;
          } else {
            System.out.println("Invalid arguments: ./client <-p port> <-s> <hostname> <Northeastern-username>");
            throw new IllegalArgumentException();
          }
        } else {
          System.out.println("Invalid arguments: ./client <-p port> <-s> <hostname> <Northeastern-username>");
          throw new IllegalArgumentException();
        }
        break;
      default:
        System.out.println("Invalid arguments: ./client <-p port> <-s> <hostname> <Northeastern-username>");
        throw new IllegalArgumentException();
    }
  }

  public static void main(String[] args) {
    //String[] fakeArgs = {"-s", "proj1.3700.network", "santana.d"};

    //MAKE SURE TO REVERT BEFORE SUBMITTING

    try {
      parseArgs(args);
    } catch (IllegalArgumentException e) {
      return;
    }

    try {
      Socket socket;
      if (encrypted) {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        socket = factory.createSocket(hostname, port);
        ((SSLSocket) socket).setEnabledProtocols(protocol);
        ((SSLSocket) socket).setEnabledCipherSuites(suites);
        ((SSLSocket) socket).startHandshake();
      } else {
       socket = new Socket(hostname, port);
      }

      BufferedWriter outToServer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      outToServer.write("{\"type\": \"hello\", \"northeastern_username\": \"" + username + "\"}\n");
      outToServer.flush();

      //HANDLE ERRORS ON  THIS ONE - VERIFY LINE IS EXACT
      String in1 = inFromServer.readLine();
      JSONObject obj = new JSONObject(in1); //verification built in
      String type = obj.getString("type");

      if (type.equals("start")) {

        String id = obj.getString("id");
        WordleGame game = new WordleGame(id);

        while (!game.isOver) {
          String guess = game.makeGuess();

          outToServer.write("{\"type\": \"guess\", \"id\": \"" + id + "\", \"word\": \"" + guess + "\"}\n");
          outToServer.flush();

          String in = inFromServer.readLine();
          JSONObject obj1 = new JSONObject(in);

          game.reactToServerResponse(obj1, guess);
        }

        socket.close();
        outToServer.close();
        inFromServer.close();

      } else if (type.equals("error")) {
        System.out.println(obj.get("message"));
      } else {
        System.out.println("Invalid type received from server.");
      }

    } catch (UnknownHostException e) {
      System.out.println("Unknown host: " + e.getMessage());
    } catch (JSONException e) {
      System.out.println("Invalid response from server:" + e.getMessage());
    } catch (IOException e) {
      System.out.println(e.getMessage());
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
    }

}

  private static class WordleGame {

    private final List<String> words;
    public boolean isOver = false;

    public List<Character> lettersInWord = new ArrayList<>();
    public Map<Character, Integer> guessedLetterLocations = new HashMap<>();
    public Map<Character, Integer> halfGuessedLetterLocations = new HashMap<>();
    private final String id;

    public WordleGame(String id) {
      words = loadWords();
      this.id = id;
    }

    public void reactToServerResponse(JSONObject input, String guess) throws JSONException {
      if(input.getString("type").equals("retry")) {
        if (!input.getString("id").equals(id)) {
          throw new IllegalArgumentException("Invalid id received from server.");
        }
        JSONArray arr = input.getJSONArray("guesses");
        JSONObject listItem = arr.getJSONObject(arr.length() - 1);
        JSONArray guessInfo = listItem.getJSONArray("marks");
        String guessWord = listItem.getString("word");

        if (!guessWord.equals(guess)) {
          throw new IllegalArgumentException("Invalid guess word returned by server");
        }
        if (guessInfo.length() != 5) {
          throw new IllegalArgumentException("Invalid marks array received from server");
        }

        for (int i = 0; i < guessInfo.length(); i++) {
          int value = guessInfo.getInt(i);
          if (value == 1) {
            lettersInWord.add(guess.charAt(i));
            halfGuessedLetterLocations.put(guess.charAt(i), i);
          } else if (value == 2) {
            guessedLetterLocations.put(guess.charAt(i), i);
          }
        }
        filterWords();
      } else if (input.getString("type").equals("bye")) {
        if (!input.getString("id").equals(id)) {
          throw new IllegalArgumentException("Invalid id received from server.");
        }
        System.out.println(input.getString("flag"));
        isOver = true;
        //flag spxsidpudvzqzrxfzann
        //flag SLL: sqacrnknbqgegolqment
      } else if (input.getString("type").equals("error")) {
        throw new IllegalArgumentException(input.getString("message"));
      }
    }


    public String makeGuess() {
      String word = words.remove(0);
      return word;
    }

    public void filterWords() {
      //perfect guesses: 2
      for (Map.Entry<Character, Integer> entry : guessedLetterLocations.entrySet()) {
        for (int i = words.size() - 1; i >= 0; i -= 1) {
          if (words.get(i).charAt(entry.getValue()) != entry.getKey()) {
            words.remove(i);
          }
        }
      }


      //imperfect guesses: 1
      for (Map.Entry<Character, Integer> entry : halfGuessedLetterLocations.entrySet()) {
        for (int i = words.size() - 1; i >= 0; i -= 1) {
          if (words.get(i).charAt(entry.getValue()) == entry.getKey()) {
            words.remove(i);
          }
        }
      }

      for (int i = words.size() - 1; i >= 0; i -= 1) {

        for (char c : lettersInWord) {
          if (!words.get(i).contains(String.valueOf(c))) {
            //also remove words where it's in the right spot
            words.remove(i);
            break;
          }
        }
      }
      guessedLetterLocations.clear();
      lettersInWord.clear();
    }

    private List<String> loadWords() {
      List<String> words = new ArrayList<>();
      try {
        Scanner s = new Scanner(new File("res/project1-words.txt"));

        while (s.hasNext()) {
          words.add(s.next());
        }
        return words;

      } catch (FileNotFoundException e) {
        throw new IllegalArgumentException("Error finding file");
      }
    }

  }

}


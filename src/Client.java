import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.json.*;


public class Client {

  private static class WordleGame {

    private final List<String> words;
    public boolean isOver = false;

    public List<Character> lettersInWord = new ArrayList<>();
    public Map<Character, Integer> guessedLetterLocations = new HashMap<>();

    public Map<Character, Integer> halfGuessedLetterLocations = new HashMap<>();

    public WordleGame() {
      words = generateWords();
    }


    public String makeGuess() {
      String word = words.remove(0);
      return word;

    }

    public void filterWords() {

      for (Map.Entry<Character, Integer> entry :
          guessedLetterLocations.entrySet()) {
        for (int i = words.size() - 1; i >= 0; i -= 1) {
          if (words.get(i).charAt(entry.getValue()) != entry.getKey()) {
            words.remove(i);
          }
        }
      }

      for (Map.Entry<Character, Integer> entry :
          halfGuessedLetterLocations.entrySet()) {
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

    private List<String> generateWords() {
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

  private static final String[] protocols = new String[]{"TLSv1.3"};
  private static final String[] cipher_suites = new String[]{"TLS_AES_128_GCM_SHA256"};

  //VERIFY VALIDITY OF SERVER MESSAGESD

  public static void main(String[] args) {

    //System.out.println(args[0]);

    BufferedReader inFromServer;
    BufferedWriter outToServer;

//   // do something with the args
//    try {
//
//      Socket socket =
//
//      SSLSocketFactory factory =
//          (SSLSocketFactory) SSLSocketFactory.getDefault();
//      SSLSocket socket =
//          (SSLSocket) factory.createSocket("proj1.3700.network", 27994);
//
//      socket.setEnabledProtocols(protocols);
//      socket.setEnabledCipherSuites(cipher_suites);
//
//      socket.startHandshake();
//
//      outToServer = new BufferedWriter(
//              new OutputStreamWriter(
//                  socket.getOutputStream()));
//
////      outToServer.write("GET / HTTP/1.0\n");
////      outToServer.flush();
//
////      if (outToServer.checkError())
////        System.out.println("SSLSocketClient:  java.io.PrintWriter error");
//
//      /* read response */
//      inFromServer = new BufferedReader(
//          new InputStreamReader(
//              socket.getInputStream()));
//
//      outToServer.write("{\"type\": \"hello\", \"northeastern_username\": \"santana.d\"}\n");
//      outToServer.flush();
//
//
//      //HANDLE ERRORS ON  THIS ONE
//      JSONObject obj = new JSONObject(inFromServer.readLine());
//
//      String id = obj.getString("id");
//      String type = obj.getString("type");
//
//      if (type.equals("start")) {
//        WordleGame game = new WordleGame();
//
//        while (!game.isOver) {
//          String guess = game.makeGuess();
//
//          outToServer
//              .write("{\"type\": \"guess\", \"id\": \"" + id + "\", \"word\": \"" + guess + "\"}\n");
//          outToServer.flush();
//
//          String in = inFromServer.readLine();
//          JSONObject obj1 = new JSONObject(in);
//          System.out.println(in);
//          if (obj1.getString("type").equals("retry")) {
//            JSONArray arr = obj1.getJSONArray("guesses");
//            JSONObject listItem = arr.getJSONObject(arr.length() - 1);
//            JSONArray guessInfo = listItem.getJSONArray("marks");
//
//            for (int i = 0; i < guessInfo.length(); i++) {
//              int value = guessInfo.getInt(i);
//              if (value == 1) {
//                game.lettersInWord.add(guess.charAt(i));
//                game.halfGuessedLetterLocations.put(guess.charAt(i), i);
//              } else if (value == 2) {
//                game.guessedLetterLocations.put(guess.charAt(i), i);
//              }
//            }
//
//            game.filterWords();
//          } else if (obj1.getString("type").equals("bye")) {
//            System.out.println(guess);
//            game.isOver = true;
//            //flag spxsidpudvzqzrxfzann
//            //flag SLL: sqacrnknbqgegolqment
//          }
//        }
//        socket.close();
//        outToServer.close();
//        inFromServer.close();
//      }
//
//    } catch (Exception e) {
//      e.printStackTrace();
//    }


    try {

      Socket client = new Socket("proj1.3700.network", 27993);
      inFromServer = new BufferedReader(new InputStreamReader(client.getInputStream()));
      outToServer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

      outToServer.write("{\"type\": \"hello\", \"northeastern_username\": \"santana.d\"}\n");
      outToServer.flush();
    JSONObject obj = new JSONObject(inFromServer.readLine());

    String id = obj.getString("id");
    String type = obj.getString("type");

    if (type.equals("start")) {
      WordleGame game = new WordleGame();

      while (!game.isOver) {
        String guess = game.makeGuess();

        outToServer
            .write("{\"type\": \"guess\", \"id\": \"" + id + "\", \"word\": \"" + guess + "\"}\n");
        outToServer.flush();

        String in = inFromServer.readLine();
        JSONObject obj1 = new JSONObject(in);
        System.out.println(in);
        if (obj1.getString("type").equals("retry")) {
          JSONArray arr = obj1.getJSONArray("guesses");
          JSONObject listItem = arr.getJSONObject(arr.length() - 1);
          JSONArray guessInfo = listItem.getJSONArray("marks");

          for (int i = 0; i < guessInfo.length(); i++) {
            int value = guessInfo.getInt(i);
            if (value == 1) {
              game.lettersInWord.add(guess.charAt(i));
              game.halfGuessedLetterLocations.put(guess.charAt(i), i);
            } else if (value == 2) {
              game.guessedLetterLocations.put(guess.charAt(i), i);
            }
          }

          game.filterWords();
        } else if (obj1.getString("type").equals("bye")) {
          System.out.println(guess);
          game.isOver = true;
          //flag spxsidpudvzqzrxfzann
        }
      }
      client.close();
      outToServer.close();
      inFromServer.close();
    }

  } catch (JSONException | IOException e) {
    ///aaa
    }

}
}


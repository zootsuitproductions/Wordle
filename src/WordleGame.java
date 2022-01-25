import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class WordleGame {

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

import java.util.HashMap;
import java.util.Random;
import com.sun.tools.javac.Main;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		String window = "";
        char c;
        In in = new In(fileName);
        List probList;
        for (int i = 0; i < this.windowLength; i++) {
            c = in.readChar();
            window += c;
        }
        while (!in.isEmpty()) {
            c = in.readChar();
            if (this.CharDataMap.containsKey(window)) {
                probList = this.CharDataMap.get(window);
            }
            else {
                probList = new List();
            }
            probList.update(c);
            this.CharDataMap.put(window, probList);
            window += c;
            window = window.substring(1);
        }
        for (List probs : this.CharDataMap.values())
            calculateProbabilities(probs);
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {				
		Node current = probs.getNode();
		double numOfChr = 0;
        double p, cp = 0;
        while (current != null) {
            numOfChr += current.cp.count;
            current = current.next;
        }
        current = probs.getNode();
        while (current != null) {
            p = current.cp.count / numOfChr;
            cp += p;
            current.cp.p = p;
            current.cp.cp = cp;
            current = current.next;
        }
	}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
        Node current = probs.getNode();
        double rnd = this.randomGenerator.nextDouble();
        while (current != null) {
            if (rnd < current.cp.cp)
                return current.cp.chr;
            current = current.next;
        }
        return ' ';
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		if (!this.CharDataMap.containsKey(initialText.substring(initialText.length() - this.windowLength)) || initialText.length() < this.windowLength)
            return initialText;
        String generatedText = initialText;
        String currentWindow = initialText;
        char ch;
        for (int i = 0; i < textLength; i++) {
            ch = this.getRandomChar(this.CharDataMap.get(currentWindow));
            generatedText += ch;
            currentWindow += ch;
            currentWindow = currentWindow.substring(1);
        }
        return generatedText;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
        if (args.length > 4) {
            int windowLength = Integer.parseInt(args[0]);
            String initialText = args[1];
            int generatedTextLength = Integer.parseInt(args[2]);
            Boolean randomGeneration = args[3].equals("random");
            String fileName = args[4];
            LanguageModel lm;
            if (randomGeneration)
                lm = new LanguageModel(windowLength);
            else
                lm = new LanguageModel(windowLength, 20);
            // model training
            lm.train(fileName);
            // generaiting text + printing.
            System.out.println(lm.generate(initialText, generatedTextLength));
        }
    }

}
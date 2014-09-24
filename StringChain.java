import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;

public class StringChain {
	private final int order;
	private final HashMap<String, ProbabilityMapping> markovTable = 
		new HashMap<>();

	public StringChain(int order) {
		this.order = order;
	}

	public void addItems(Iterator<String> iterator) {
		iterator.forEachRemaining(new ItemsAdder());
	}

	public List<String> generate(int number, Random rand) {
		LinkedList<String> previous = new LinkedList<>(Collections.nCopies(
			order, ""));
		ArrayList<String> returnList = new ArrayList<>();
		for (; number > 0; number--) {
			String s = markovTable.get(String.join(" ", previous))
				.getNextStringRandomly(rand);
			previous.addFirst(s);
			previous.removeLast();
			returnList.add(s);
		}
		return returnList;
	}

	private class ItemsAdder implements Consumer<String> {
		private final LinkedList<String> previous = new LinkedList<>(
			Collections.nCopies(order, ""));
		public void accept(String s) {
			String key = String.join(" ", previous);
			if (!markovTable.containsKey(key)) {
				markovTable.put(key, new ProbabilityMapping());
			}
			markovTable.get(key).add(s);
			previous.removeLast();
			previous.addFirst(s);

		}
	}

	private class ProbabilityMapping {
		private final HashMap<String, Integer> probabilityMap = new HashMap<>();
		private int totalValues;

		public void add(String s) {
			if (probabilityMap.containsKey(s)) {
				probabilityMap.put(s, probabilityMap.get(s) + 1);
			}
			else {
				probabilityMap.put(s, 1);
			}

			totalValues += 1;
		}
		public int getNumberOfSamples() {
			return totalValues;
		}

		public String getNextStringByIndex(int index) { //0 based
			if (index >= getNumberOfSamples()) {
				throw new IllegalArgumentException();
			}
			return probabilityMap.keySet().stream()
				.flatMap(key -> 
					Collections.nCopies(probabilityMap.get(key), key).stream())
				.skip(index)
				.findFirst()
				.get(); //don't have to worry about emptys because max size is
						//already checked
		}

		public String getNextStringRandomly() {
			return getNextStringRandomly(new Random());
		}
		public String getNextStringRandomly(Random rand) {
			return getNextStringByIndex(
				rand.nextInt(getNumberOfSamples()));
		}
	}
}
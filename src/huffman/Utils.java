package huffman;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Stack;

import exceptions.ProbabilityFileException;
import exceptions.ProbabilitySumException;
import huffman.tree.HuffmanNode;
import huffman.tree.Leaf;
import huffman.tree.Node;

public class Utils {
	private static final double APROXIMATION = 0.999999;

	public static Map<BitSequence, Double> getPrioriProbabilities(File probabilitiesFile) throws Exception {
		try {
			BufferedReader br = new BufferedReader(new FileReader(probabilitiesFile));
			Map<BitSequence, Double> probabilities = new HashMap<>(Integer.parseInt(br.readLine()));
			String st;
			while ((st = br.readLine()) != null) {
				BitSequence key;
				String[] stArray = st.split("\\s+", 2);
				if(stArray[0].hashCode() == 0)
					key = new BitSequence(((byte) 0x20));
				else
					key = new BitSequence(stArray[0].getBytes());
				probabilities.put(key, Double.parseDouble(stArray[1]));
			}
			br.close();
			return probabilities;
		} catch (Exception e) {
			throw new ProbabilityFileException();
		}
	}

	public static HuffmanNode createHuffmanTree(Map<BitSequence, Double> probabilitiesDictionary)
			throws ProbabilitySumException {
		PriorityQueue<HuffmanNode> queue = new PriorityQueue<>();
		for (Map.Entry<BitSequence, Double> entry : probabilitiesDictionary.entrySet())
			queue.add(new Leaf(entry.getKey(), entry.getValue()));

		while (!queue.isEmpty()) {
			HuffmanNode t1 = queue.remove();
			HuffmanNode t2 = queue.remove();
			double sum = t1.getProbability() + t2.getProbability();
			Node node = new Node(t1, t2, sum);
			if (sum >= APROXIMATION)
				return node;
			queue.add(node);
		}
		throw new ProbabilitySumException();
	}

	public static Map<BitSequence, BitSequence> createMapping(HuffmanNode tree, int probabilitySize) {
		Map<BitSequence, BitSequence> mapping = new HashMap<>(probabilitySize);
		Stack<HuffmanNode> searchQueue = new Stack<>();
		Stack<BitSequence> createCode = new Stack<>();
		searchQueue.push(((Node) tree).getRight());
		searchQueue.push(((Node) tree).getLeft());
		createCode.push(new BitSequence().addBit(true));
		createCode.push(new BitSequence().addBit(false));
		HuffmanNode node;
		BitSequence code;
		while (!searchQueue.isEmpty()) {
			node = searchQueue.pop();
			code = createCode.pop();
			if (node instanceof Node) {
				searchQueue.push(((Node) node).getRight());
				searchQueue.push(((Node) node).getLeft());
				createCode.push(code.clone().addBit(true));
				createCode.push(code.clone().addBit(false));
			} else {
				mapping.put(((Leaf) node).getSymbol(), code);
			}
		}
		return mapping;
	}

	static void writeOutput(FileOutputStream fos, BitSequence bits) throws IOException {
		double limit = 64000000;
		int counter = 0;
		double div = Math.ceil(bits.getByteList().size()/limit);
		Iterator<Byte> it = bits.getByteList().iterator();
		for(int i = 0; i < div-1; i++) {
			byte[] buffer = new byte[(int) limit];
			for(int j = 0; j < limit; j++) {
				buffer[j] = it.next();
				counter++;
			}
			fos.write(buffer);
		}
		limit = (bits.getByteList().size() - counter);
		if(limit > 0) {
			byte[] buffer = new byte[(int) limit];
			for(int i = 0; i < limit; i++) {
				buffer[i] = it.next();
			}
			fos.write(buffer);
		}
	}

}

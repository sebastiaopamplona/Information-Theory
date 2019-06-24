package huffman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import huffman.tree.HuffmanNode;
import huffman.tree.Leaf;
import huffman.tree.Node;

public class Compressor {

	private static final int BUFFER_SIZE = 64000000;
	private static int finalJump = 0;
	/**
	 * To compress a file when the probabilities are not known a priori.
	 * 
	 * @param filePath           - file to compress
	 * @param symmbolLength      - bit length
	 * @param compressedFilePath - file to write compressed
	 */
	public static void compressFile(String filePath, int symbolLength, String compressedFilePath) {
		try {
			FileInputStream fis = new FileInputStream(filePath);
			long total = System.currentTimeMillis();
			
			long probabilities = System.currentTimeMillis();
			Map<BitSequence, Double> probabilitiesDictionary = getFileProbabilities(fis, symbolLength);
			probabilities = System.currentTimeMillis() - probabilities;
			
			long huffman = System.currentTimeMillis();
			HuffmanNode tree = Utils.createHuffmanTree(probabilitiesDictionary);
			Map<BitSequence, BitSequence> mapping = Utils.createMapping(tree, probabilitiesDictionary.size());
			huffman = System.currentTimeMillis() - huffman;
			
			long code = System.currentTimeMillis();
			fis = new FileInputStream(filePath);
			BitSequence codedFile = codeFile(fis, symbolLength, mapping);
			code = System.currentTimeMillis() - code;
			
			long headerT = System.currentTimeMillis();
			BitSequence header = generateHeader(tree, symbolLength);
			headerT = System.currentTimeMillis() - headerT;
			
			long write = System.currentTimeMillis();
			FileOutputStream fos = new FileOutputStream(compressedFilePath);
			Utils.writeOutput(fos, header);
			Utils.writeOutput(fos, codedFile);
			fos.close();
			write = System.currentTimeMillis() - write;
			
			total = System.currentTimeMillis() - total;
			System.out.printf("[COMPRESSOR] Probabilities: %.2f%s\n", ((probabilities/(total*1.0))*100), "%");
			System.out.printf("[COMPRESSOR] Huffman tree: %.2f%s\n", ((huffman/(total*1.0))*100), "%");
			System.out.printf("[COMPRESSOR] Code: %.2f%s\n", ((code/(total*1.0))*100), "%");
			System.out.printf("[COMPRESSOR] Header: %.2f%s\n", ((headerT/(total*1.0))*100), "%");
			System.out.printf("[COMPRESSOR] Write: %.2f%s\n", ((write/(total*1.0))*100), "%");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * To compress a file when the probabilities are known a priori.
	 * 
	 * @param filePath           - file to compress
	 * @param probabilitiesPath  - file containing the probabilities
	 * @param compressedFilePath - file to write compressed
	 */
	public static void compressFile(String filePath, String probabilitiesPath, String compressedFilePath) {
		try {
			FileInputStream fis = new FileInputStream(filePath);
			File probabilitiesFile = new File(probabilitiesPath);
			Map<BitSequence, Double> probabilitiesDictionary = Utils.getPrioriProbabilities(probabilitiesFile);
			HuffmanNode tree = Utils.createHuffmanTree(probabilitiesDictionary);
			Map<BitSequence, BitSequence> mapping = Utils.createMapping(tree, probabilitiesDictionary.size());
			BitSequence codedFile = codeFile(fis, 8, mapping);
			FileOutputStream fos = new FileOutputStream(compressedFilePath);
			Utils.writeOutput(fos, codedFile);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Generates probabilities for each symbol of the file.
	 * 
	 * @param bits 		   - file in byte array that we want to compress
	 * @param symbolLength - length of each symbol of the file
	 * @return the mapping of each symbol to its probability
	 * @throws Exception 
	 */
	private static Map<BitSequence, Double> getFileProbabilities(FileInputStream bits, int symbolLength) throws Exception {
		double mapSize = Math.pow(2, symbolLength);
		Map<BitSequence, Double> symbolAppearances = new HashMap<>((int) mapSize);
		BitSequence currentSymbol = new BitSequence();
		int symbolControler = 0;
		double counter = 0.0;
		int limit = bits.available();
		int bufferSize = Math.min(BUFFER_SIZE, limit);
		byte[] buff = new byte[bufferSize];
		bits.read(buff);
		
		for (int i = 0; i < limit; i++) {
			if(i%bufferSize == 0)
				bits.read(buff);
			BitSequence currentByte = new BitSequence((buff[i%bufferSize]));
			for (int j = 0; j < 8; j++) {
				currentSymbol.addBit(currentByte.isSet(j));
				symbolControler++;
				if (symbolControler == symbolLength) {
					Double nAppearances = symbolAppearances.get(currentSymbol);
					double result = nAppearances == null ? 1 : nAppearances + 1;
					symbolAppearances.put(currentSymbol, result);
					currentSymbol = new BitSequence();
					symbolControler = 0;
					counter++;
				}
			}
		}
		if (symbolControler > 1) {
			while (currentSymbol.getNumberOfBits() < symbolLength)
				currentSymbol.addBit(false);
			Double nAppearances = symbolAppearances.get(currentSymbol);
			double result = nAppearances == null ? 1 : nAppearances + 1;
			symbolAppearances.put(currentSymbol, result);
			counter++;
		}

		for (Map.Entry<BitSequence, Double> entry : symbolAppearances.entrySet()) {
			symbolAppearances.put(entry.getKey(), entry.getValue() / counter);
		}
		return symbolAppearances;
	}

	/**
	 * Changes the occurrence of each symbol with the respective code.
	 * 
	 * @param bits 		   - file in byte array that we want to compress 
	 * @param symbolLength - length of each symbol of the file
	 * @param mapping 	   - mapping of each symbol to its probability
	 * @return the coded file
	 * @throws IOException 
	 */
	private static BitSequence codeFile(FileInputStream bits, int symbolLength, Map<BitSequence, BitSequence> mapping) throws IOException {
		BitSequence output = new BitSequence();
		BitSequence currentSymbol = new BitSequence();
		int symbolControler = 0;
		int limit = bits.available();
		int bufferSize = Math.min(BUFFER_SIZE, limit);
		byte[] buff = new byte[bufferSize];
		for (int i = 0; i < limit; i++) {
			if(i%bufferSize == 0)
				bits.read(buff);
			BitSequence currentByte = new BitSequence((buff[i%bufferSize]));
			for (int j = 0; j < 8; j++) {
				currentSymbol.addBit(currentByte.isSet(j));
				symbolControler++;
				if (symbolControler == symbolLength) {
					output.concat(mapping.get(currentSymbol));
					symbolControler = 0;
					currentSymbol = new BitSequence();
				}
			}
		}
		if (symbolControler > 1) {
			while (currentSymbol.getNumberOfBits() < symbolLength)
				currentSymbol.addBit(false);
			output.concat(mapping.get(currentSymbol));
		}
		
		finalJump = output.getNumberOfBits()%8 == 0 ? 0: (8 - output.getNumberOfBits()%8);
		return output;
	}

	/**
	 * Generates the header to send with the coded file. It is only used when there aren't know probabilities a priori.
	 * 
	 * @param root - the root of the huffman tree
	 * @return the header that contains information about the tree and each symbol
	 */
	private static BitSequence generateHeader(HuffmanNode root, int symbolLength) {
		// header: <dfs size> <dfs> <symbols in dfs order>
		BitSequence dfs = new BitSequence();
		Queue<BitSequence> symbols = new LinkedList<>();

		// dfs stuff
		Stack<HuffmanNode> stack = new Stack<>();
		Map<Integer, Integer> explored = new HashMap<>();

		short dfsSize = 0;

		// <hashcode, 0: not explored 1: explored left 2: explored left and right>
		explored.put(root.hashCode(), 0);
		stack.add(root);

		while (!stack.isEmpty()) {
			HuffmanNode curr = stack.peek();
			if (!(curr instanceof Leaf)) {
				if (!explored.containsKey(curr.hashCode()))
					explored.put(curr.hashCode(), 0);

				if (explored.get(curr.hashCode()) == 0) {
					stack.push(((Node) curr).getLeft());
					explored.put(curr.hashCode(), 1);
					dfs.addBit(false); // 0
					dfsSize++;

				} else if (explored.get(curr.hashCode()) == 1) {
					stack.push(((Node) curr).getRight());
					explored.put(curr.hashCode(), 2);
					dfs.addBit(true); // 1
					dfsSize++;

				} else
					stack.pop();

			} else {
				symbols.add(((Leaf) curr).getSymbol());
				stack.pop();
			}

		} 	
		
		int headerSize = dfsSize + symbols.size() * symbols.peek().getNumberOfBits();
		System.out.println("[COMPRESSOR] Header size is " + ((int) Math.ceil(headerSize/8.0) + 3) + " bytes");
		int off = 8 - (headerSize)%8;
		if(off == 8)
			off = 0;
		
		dfs = header(symbolLength, off, dfsSize).concat(dfs);
		
		while (!symbols.isEmpty()) 
			dfs.concat(symbols.poll());
		
		return dfs;
	}
	
	private static BitSequence header(int symbolLength, int initialOff, int dfsSize) {
		String header = "";
		for (int i = 0; i < (5-Integer.toBinaryString(symbolLength).length()); i++)
			header += "0";
		header += Integer.toBinaryString(symbolLength);
		
		for (int i = 0; i < (3-Integer.toBinaryString(initialOff).length()); i++)
			header += "0";
		header += Integer.toBinaryString(initialOff);
		
		for (int i = 0; i < (3-Integer.toBinaryString(finalJump).length()); i++)
			header += "0";
		header += Integer.toBinaryString(finalJump);
		
		
		for (int i = 0; i < 13 - Integer.toBinaryString(dfsSize).length(); i++)
			header += "0";

		return new BitSequence(header + Integer.toBinaryString(dfsSize));
	}
}

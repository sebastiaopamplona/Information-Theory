package huffman;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Stack;
import huffman.tree.HuffmanNode;
import huffman.tree.Leaf;
import huffman.tree.Node;

public class Decompressor {

	private static final int SHORT_BIT_LENGTH = 16;
	private static final int SYMBOL_BIT_LENGHT = 8;
	private static int dfsLength = 0;
	private static int nSymbol = 0;
	private static int initialJump = 0;
	private static int finalJump = 0;
	private static int symbolLenght = 0;

	/**
	 * Method for when the probabilities are not known a priori. It has to build the
	 * Huffman tree, from the header of the compressed file.
	 * 
	 * @param compressedFilePath   - compressed file
	 * @param symbolLength         - symbol length
	 * @param decompressedFilePath - file to write, after decompressing
	 */
	public static void decompressFile(String compressedFilePath, String decompressedFilePath) {
		try {
			long total = System.currentTimeMillis();
			
			File compressedFile = new File(compressedFilePath);
			byte[] compressedFileBits = Files.readAllBytes(compressedFile.toPath());
			BitSequence compressed = new BitSequence();
			compressed.wrap(compressedFileBits);
			
			long huffman = System.currentTimeMillis();
			HuffmanNode root = buildHuffmanTree(compressed);
			huffman = System.currentTimeMillis() - huffman;
			
			int offset = SHORT_BIT_LENGTH + SYMBOL_BIT_LENGHT + dfsLength + nSymbol * symbolLenght + initialJump;
			long decompress = System.currentTimeMillis();
			BitSequence decompressed = decompress(compressed, root, offset);
			decompress = System.currentTimeMillis() - decompress;
			
			long write = System.currentTimeMillis();
			FileOutputStream fos = new FileOutputStream(decompressedFilePath);
			Utils.writeOutput(fos, decompressed);
			write = System.currentTimeMillis() - write;
			fos.close();
			
			total = System.currentTimeMillis() - total;
			System.out.printf("[DECOMPRESSOR] Generate huffman tree: %.2f%s\n", ((huffman/(total*1.0))*100), "%");
			System.out.printf("[DECOMPRESSOR] Decompression: %.2f%s\n", ((decompress/(total*1.0))*100), "%");
			System.out.printf("[DECOMPRESSOR] Write: %.2f%s\n", ((write/(total*1.0))*100), "%");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace(); 
		}
	}

	/**
	 * Method for when the probabilities are known a priori.
	 * 
	 * @param compressedFilePath   - compressed file
	 * @param decompressedFilePath - file to write, after decompression
	 * @param probabilitiesPath    - file with the probability distribution
	 */
	public static void decompressFile(String compressedFilePath, String decompressedFilePath, String probabilitiesPath) {
		try {
			File compressedFile = new File(compressedFilePath);
			byte[] compressedFileBits = Files.readAllBytes(compressedFile.toPath());
			BitSequence compressed = new BitSequence();
			compressed.wrap(compressedFileBits);
			File probabilitiesFile = new File(probabilitiesPath);
			Map<BitSequence, Double> probabilitiesDictionary = Utils.getPrioriProbabilities(probabilitiesFile);
			HuffmanNode root = Utils.createHuffmanTree(probabilitiesDictionary);
			BitSequence decompressed = decompress(compressed, root, 0);
			FileOutputStream fos = new FileOutputStream(decompressedFilePath);
			Utils.writeOutput(fos, decompressed);
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static HuffmanNode buildHuffmanTree(BitSequence compressed) {
		HuffmanNode root = new Node(null, null, 1.0);
		try {
			readHeader(compressed);
			Stack<HuffmanNode> stack = new Stack<>();
			stack.push(root);
			int prev = compressed.getBitValue(SHORT_BIT_LENGTH + SYMBOL_BIT_LENGHT);
			
			for (int i = SHORT_BIT_LENGTH + SYMBOL_BIT_LENGHT+1; i < SHORT_BIT_LENGTH + SYMBOL_BIT_LENGHT + dfsLength; i++) {
				int curr = compressed.getBitValue(i);
				Node head = (Node) stack.peek();

				if (curr == 1) {
					BitSequence symbol = compressed.getBitChunk((SHORT_BIT_LENGTH + SYMBOL_BIT_LENGHT + dfsLength + nSymbol * symbolLenght),
																(SHORT_BIT_LENGTH + SYMBOL_BIT_LENGHT + dfsLength + nSymbol * symbolLenght + symbolLenght));
					nSymbol++;
					
					Leaf leaf = new Leaf(symbol, 0);

					// 0 -> 1
					if (prev == 0)
						head.setLeft(leaf);
					// 1 -> 1
					else {
						head.setRight(leaf);
						stack.pop();
					}
				} else {
					Node newNode = new Node(null, null, 0);

					// 0 -> 0
					if (prev == 0) {
						head.setLeft(newNode);
						stack.push(newNode);
					}
					// 1 -> 1
					else {
						head.setRight(newNode);
						stack.pop();
						stack.push(newNode);
					}
				}

				prev = curr;
			}

			// take care of the last bit
			Node head = (Node) stack.peek();
			BitSequence symbol = compressed.getBitChunk((SHORT_BIT_LENGTH + SYMBOL_BIT_LENGHT + dfsLength + nSymbol * symbolLenght),
														(SHORT_BIT_LENGTH + SYMBOL_BIT_LENGHT + dfsLength + nSymbol * symbolLenght + symbolLenght));
			nSymbol++;
			Leaf leaf = new Leaf(symbol, 0);

			if (prev == 0) {
				head.setLeft(leaf);
			} else
				head.setRight(leaf);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return root;
	}
	
	private static BitSequence decompress(BitSequence compressed, HuffmanNode root, int start) {
		BitSequence decompressed = new BitSequence();
		Node curr = (Node) root;
		for (int i = start; i < compressed.getNumberOfBits()-finalJump; i++) {
			HuffmanNode next;
			if (!compressed.isSet(i)) {
				next = curr.getLeft();
			}
			else {
				next = curr.getRight();
			}
			// found a leaf; go back to the root of the tree
			if (next instanceof Leaf) {
				decompressed = decompressed.concat(((Leaf) next).getSymbol());
				curr = (Node) root;
			} else
				curr = (Node) next;
		}
		return decompressed;
	}
	
	private static void readHeader(BitSequence compressed) throws Exception {
		symbolLenght = Integer.parseInt(compressed.getBitChunk(0, 5).toString(), 2);
		initialJump = Integer.parseInt(compressed.getBitChunk(5, 8).toString(), 2);
		finalJump = Integer.parseInt(compressed.getBitChunk(8, 11).toString(), 2);
		dfsLength = Integer.parseInt(compressed.getBitChunk(11, SHORT_BIT_LENGTH + SYMBOL_BIT_LENGHT).toString(), 2);
		nSymbol = 0;
	}

}

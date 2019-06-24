package huffman.tree;

import huffman.BitSequence;

public class Leaf extends HuffmanNode {
	private BitSequence symbol;
	
	public Leaf(BitSequence symbol, double probability) {
		super(probability);
		this.symbol = symbol;
	}
	
	public BitSequence getSymbol() {
		return symbol;
	}
}

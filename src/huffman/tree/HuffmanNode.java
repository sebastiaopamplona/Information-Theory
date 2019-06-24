package huffman.tree;

public abstract class HuffmanNode implements Comparable<HuffmanNode>{
	private double probability;
	
	public HuffmanNode(double probability) {
		this.probability = probability;
	}
	
	public double getProbability() {
		return probability;
	}
	
	public int compareTo(HuffmanNode arg) {
		if(arg.getProbability() >= probability)
			return -1;
		else 
			return 1;
	}
}

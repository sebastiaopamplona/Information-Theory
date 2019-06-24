package huffman.tree;

public class Node extends HuffmanNode {
	private HuffmanNode left;
	private HuffmanNode right;
	
	public Node(HuffmanNode left, HuffmanNode right, double probability) {
		super(probability);
		this.left = left;
		this.right = right;
	}

	public HuffmanNode getLeft() {
		return left;
	}

	public HuffmanNode getRight() {
		return right;
	}

	public void setLeft(HuffmanNode left) {
		this.left = left;
	}

	public void setRight(HuffmanNode right) {
		this.right = right;
	}
	
	
}

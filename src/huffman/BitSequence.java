package huffman;

import java.util.LinkedList;
import java.util.List;

public class BitSequence {
	public static final byte NEW_BYTE = 0x0;
	private List<Byte> bitSequence;
	private int nBits;
	private byte[] wrapedByte;
	private boolean isWraped;

	public BitSequence() {
		bitSequence = new LinkedList<>();
		bitSequence.add(NEW_BYTE);
		nBits = 0;
	}
	
	public BitSequence(BitSequence bits) {
		bitSequence = bits.getByteList();
		nBits = bits.getNumberOfBits();
	}
	
	public BitSequence(byte[] bits) {
		bitSequence = new LinkedList<>();
		nBits = bits.length*8;
		for(int i = 0; i < bits.length; i++)
			bitSequence.add(bits[i]);
	}

	public BitSequence(byte bits) {
		bitSequence = new LinkedList<>();
		nBits = 8;
		bitSequence.add(bits);
	}
	
	public BitSequence(String binaryString) {
		bitSequence = new LinkedList<>();
		for(int i = 0; i < binaryString.length(); i++)
			addBit( Integer.parseInt(binaryString.charAt(i) + "") == 1 );
	}

	public void wrap (byte[] array) throws Exception {
		if(nBits > 0)
			throw new Exception("Cannot wrap array if BitSequence isn't empty.");
		wrapedByte = array;
		isWraped = true;
		nBits = array.length*8;
	}
	
	/**
	 * Adds a new bit to the sequence.
	 * 
	 * @param set is true if the new bit has value 1 or false otherwise.
	 */
	public BitSequence addBit(boolean set) {
		nBits++;
		double byteIndex = Math.ceil(nBits / 8.0);
		byte lastByte;

		if (bitSequence.size() == byteIndex)
			lastByte = bitSequence.remove(bitSequence.size() - 1);
		else
			lastByte = NEW_BYTE;
		if (set)
			lastByte |= (1 << 7-((nBits-1)%8));

		bitSequence.add(lastByte);
		return this;
	}
	
	public int getNumberOfBits() {
		return nBits;
	}

	public byte[] getByteArray() {
		bitSequence.toArray();
		byte[] byteArray = new byte[bitSequence.size()];
		for (int i = 0; i < bitSequence.size(); i++)
			byteArray[i] = bitSequence.get(i);

		return byteArray;
	}

	public List<Byte> getByteList() {
		return bitSequence;
	}

	public int getBitValue(int bitIndex) {
		int byteIndex = bitIndex / 8;
		if(isWraped) {
			return (byte) ((wrapedByte[byteIndex] >> 7-(bitIndex % 8)) & 1);
		}
		return (byte) ((bitSequence.get(byteIndex) >> 7-(bitIndex % 8)) & 1);
	}

	public boolean isSet(int bitIndex) {
		int byteIndex = bitIndex / 8;
		if(isWraped) {
			return (((wrapedByte[byteIndex] >> 7-(bitIndex % 8)) & 1)) == 1;
		}
		return (((bitSequence.get(byteIndex) >> 7-(bitIndex % 8)) & 1)) == 1;
	}

	public BitSequence concat(BitSequence bits) {
		for(int i = 0; i < bits.nBits; i++)
			addBit(bits.getBitValue(i) == 1);
		return this;
	}

	/**
	 * Returns a bit chunk for the bit sequence, as a BitSequence.
	 * @param from (inclusive)
	 * @param to (exclusive)
	 * @return bit chunk
	 * @throws Exception 
	 */
	public BitSequence getBitChunk(int from, int to) throws Exception {
		BitSequence chunk = new BitSequence();
		if(from < 0 || from > to || to > nBits)
			throw new Exception("Illegal arguments.");
		
		for(int i = from; i < to; i++)
			chunk.addBit(getBitValue(i) == 1);
		
		return chunk;
	}
	
	public BitSequence clone() {
		BitSequence clone = new BitSequence();
		for(int i = 0; i < nBits; i++) {
			clone.addBit( getBitValue(i) == 1 );
		}
		return clone;
	}
	
	@Override
	public int hashCode() {
		int hash = 7;
		hash = 31 * hash + bitSequence.hashCode();
		hash = 31 * hash + nBits;
		return hash;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof BitSequence)
			return hashCode() == o.hashCode();
		else
			return false;
	}
	
	@Override
	public String toString() {
		String s = "";
		for(int i = 0; i < nBits; i++)
			s += getBitValue(i);
		
		return s;
	}
	
	public boolean bitAt(int bits, int bitIndex) {
		return ((bits >> 7-(bitIndex % 8)) & 1) == 1;
	}
}

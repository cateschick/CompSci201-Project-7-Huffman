// Cate Schick
// CompSci 201
// Project 7: Huffman
// Filename: HuffProcessor.java

import java.util.*;

/**
 * Although this class has a history of several years,
 * it is starting from a blank-slate, new and clean implementation
 * as of Fall 2018.
 * <P>
 * Changes include relying solely on a tree for header information
 * and including debug and bits read/written information
 * 
 * @author Owen Astrachan
 */

public class HuffProcessor {

	public static final int BITS_PER_WORD = 8;
	public static final int BITS_PER_INT = 32;
	public static final int ALPH_SIZE = (1 << BITS_PER_WORD); 
	public static final int PSEUDO_EOF = ALPH_SIZE;
	public static final int HUFF_NUMBER = 0xface8200;
	public static final int HUFF_TREE  = HUFF_NUMBER | 1;

	private final int myDebugLevel;
	
	public static final int DEBUG_HIGH = 4;
	public static final int DEBUG_LOW = 1;
	
	public HuffProcessor() {
		this(0);
	}
	
	public HuffProcessor(int debug) {
		myDebugLevel = debug;
	}

	/**
	 * Compresses a file. Process must be reversible and loss-less.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be compressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void compress(BitInputStream in, BitOutputStream out){

		// create counts and bits
		int[] counts = new int[1 + ALPH_SIZE];
		int bits = in.readBits(BITS_PER_WORD);

		// create while loop
		while (bits != -1) {
			counts[bits] += 1;
			bits = in.readBits(BITS_PER_WORD);
		}

		// if counts is PSEUDO_EOF
		counts[PSEUDO_EOF] = 1;

		// create priority queue
		PriorityQueue<HuffNode> pq = new PriorityQueue<>();

		// iterate through priority queue
		for (int i = 0; i < counts.length; i++) {
			// if counts is greater than 0 add to priority queue
			if (counts[i] > 0) {
				pq.add(new HuffNode(i, counts[i], null, null));
			}
		}

		// while loop
		while (pq.size() > 1) {
			HuffNode left = pq.remove();
			HuffNode right = pq.remove();
			HuffNode tree = new HuffNode(0, left.myWeight + right.myWeight, left, right);
			pq.add(tree);
		}

		HuffNode root = pq.remove();

		// create array
		String[] array = new String[1 + ALPH_SIZE];
		// call helper
		buildHuffman(root, array, "");

		out.writeBits(BITS_PER_INT, HUFF_TREE);
		// call write header helper
		helper(root, out);

		in.reset();

		// while loop
		while (true)
		{
			int beep = in.readBits(BITS_PER_WORD);
			if (beep == -1) break;

			String s = array[beep];
			if (s != null)
				out.writeBits(s.length(), Integer.parseInt(s, 2));
		}

		String eof = array[PSEUDO_EOF];

		out.writeBits(eof.length(), Integer.parseInt(eof, 2));

		out.close();

	}

	private void buildHuffman(HuffNode root, String[] array, String string) {
		// if null
		if (root.myRight == null && root.myLeft == null) {
			array[root.myValue] = string;
			return;
		}
		// recursive calls
		buildHuffman(root.myLeft, array, string + "0");
		buildHuffman(root.myRight, array, string + "1");
	}

	private void helper(HuffNode root, BitOutputStream out) {
		// if not empty
		if (root.myRight != null || root.myLeft != null) {
			out.writeBits(1, 0);
			// recursive calls
			helper(root.myLeft, out);
			helper(root.myRight, out);
		}
		else {
			out.writeBits(1, 1);
			out.writeBits(1 + BITS_PER_WORD, root.myValue);
		}
	}

	/**
	 * Decompresses a file. Output file must be identical bit-by-bit to the
	 * original.
	 *
	 * @param in
	 *            Buffered bit stream of the file to be decompressed.
	 * @param out
	 *            Buffered bit stream writing to the output file.
	 */
	public void decompress(BitInputStream in, BitOutputStream out){

		int bits = in.readBits(BITS_PER_INT);

		// exception
		if (bits == -1) {
			throw new HuffException("invalid header: " + bits);
		}
		if (bits != HUFF_TREE) {
			throw new HuffException("invalid header:  "+ bits);
		}
		// call helper function
		HuffNode root = readTree(in);
		HuffNode current = root;

		// create while loop
		while (true) {

			int boop = in.readBits(1);

			// if bits = -1, throw exception
			if (boop == -1) {
				throw new HuffException("Cannot read bits");
			}

			else {
				// if bits = 0
				if (boop == 0) {
					current = current.myLeft;
				}

				else {
					current = current.myRight;
				}

				// if both are null ...
				if (current.myRight == null && current.myLeft == null) {
					// if myValue is PseudoEOF, break loop
					if (current.myValue == PSEUDO_EOF) {
						break;
					}

					// otherwise, keep going
					else {
						// write bits
						out.writeBits(BITS_PER_WORD, current.myValue);

						// set curr = node
						current = root;
					}
				}

			}
		}
		// call out.close
		out.close();
	}
	private HuffNode readTree(BitInputStream in) {
		int bit = in.readBits(1);

		if (bit == -1) {
			throw new HuffException("Failed to read bits");
		}

		if (bit == 0) {
			HuffNode left = readTree(in);
			HuffNode right = readTree(in);
			return new HuffNode(0, 0, left, right);
		}
		else {
			return new HuffNode((in.readBits(1 + BITS_PER_WORD)), 0, null, null);
		}
	}
}
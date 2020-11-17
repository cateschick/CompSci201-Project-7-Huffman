// Cate Schick
// CompSci 201
// Project 7: Huffman
// Filename: HuffProcessor.java

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

		// remove all this code when implementing compress
		while (true){
			int val = in.readBits(BITS_PER_WORD);
			if (val == -1) break;
			out.writeBits(BITS_PER_WORD, val);
		}
		out.close();
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
		if (bits != HUFF_TREE) {
			throw new HuffException("invalid magic number "+ bits);
		}
		// remove all code below this point for P7

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
			// call out.close
			out.close();
		}

	}
	public HuffNode readTree(BitInputStream in) {
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
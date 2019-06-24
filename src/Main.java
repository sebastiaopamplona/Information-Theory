import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import huffman.*;

public class Main {
	private static final String ORIGINAL_DIR_PATH = "./files/original/";
	private static final String COMPRESSED_DIR_PATH = "./files/compressed/";
	private static final String DECOMPRESSED_DIR_PATH = "./files/decompressed/";
	
	public static void main(String[] args) {
		String benchmarks = "bit_length file_extension file_size compression_time compression_rate decompression_time\n";
		try {
			FileOutputStream fos = new FileOutputStream("./files/benchmarks.txt");
			fos.write(benchmarks.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int l = 6; l <= 12; l++) {
			for(int t = 1; t <= 5; t++) 
				benchmarkWithHeader("txt", t, l);
			
			for(int t = 1; t <= 5; t++) 
				benchmarkWithoutHeader("txt", t, l);
			
			benchmarkWithHeader("gif", 1, l);
			benchmarkWithHeader("png", 6, l);
		}
		try {
			FileOutputStream fos = new FileOutputStream("./files/benchmarks.txt");
			fos.write(benchmarks.getBytes());
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void benchmarkWithHeader(String extension, int fileNumber, int bitLength) {
		System.out.println("####################################################");
		System.out.println("Benchmark " + fileNumber + "." + extension + " bits -> " + bitLength + "\n");
		
		String filePath = ORIGINAL_DIR_PATH + extension + "/" + fileNumber + "." + extension;
		String compressDestinPath = COMPRESSED_DIR_PATH + extension + "/" + fileNumber + "." + extension;
		
		long t = System.currentTimeMillis();
		Compressor.compressFile(filePath, bitLength, compressDestinPath);
		System.out.println("Compresed in " + (System.currentTimeMillis() - t) + " ms");
		t = System.currentTimeMillis();
		Decompressor.decompressFile(compressDestinPath, DECOMPRESSED_DIR_PATH + extension + "/" + fileNumber + "_" + bitLength + "." + extension);
		System.out.println("Decompresed in " + (System.currentTimeMillis() - t) + " ms");
		
		File original = new File(filePath);
		File compressed = new File(compressDestinPath);
		double rate = compressed.length()*100.0/original.length();
		
		System.out.println("[FINAL] File size ......................." + original.length() + " bytes");
		System.out.println("[FINAL] Compression size ................" + compressed.length() + " bytes ");
		System.out.println("[FINAL] Compression rate:................" + String.format("%.2f", rate) + "%");
		System.out.println("####################################################\n");
	}
	
	public static void benchmarkWithoutHeader(String extension, int fileNumber, int bitLength) {
		System.out.println("####################################################");
		System.out.println("Benchmark " + fileNumber + "." + extension + " bits -> " + bitLength + "\n");
		
		String filePath = ORIGINAL_DIR_PATH + extension + "/" + fileNumber + "." + extension;
		String compressDestinPath = COMPRESSED_DIR_PATH + extension + "/" + fileNumber + "." + extension;
		
		long t = System.currentTimeMillis();
		Compressor.compressFile(filePath, "./files/utils/probabilities_en.txt", compressDestinPath);
		System.out.println("Compresed in " + (System.currentTimeMillis() - t) + " ms");
		t = System.currentTimeMillis();
		Decompressor.decompressFile(compressDestinPath, DECOMPRESSED_DIR_PATH + extension + "/" + fileNumber + "_" + bitLength + "." + extension, "./files/utils/probabilities_en.txt");
		System.out.println("Decompresed in " + (System.currentTimeMillis() - t) + " ms");
		
		File original = new File(filePath);
		File compressed = new File(compressDestinPath);
		double rate = compressed.length()*100.0/original.length();
		
		System.out.println("[FINAL] File size ......................." + original.length() + " bytes");
		System.out.println("[FINAL] Compression size ................" + compressed.length() + " bytes ");
		System.out.println("[FINAL] Compression rate:................" + String.format("%.2f", rate) + "%");
		System.out.println("####################################################\n");
	}
}

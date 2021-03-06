// DE2A2.java CS6025 Cheng 2016
// Huffman encoder
// Usage: java DE2A2 original > encoded

import java.io.*;
import java.util.*;

class Node implements Comparable{
  Node left, right;
  int symbol;
  int frequency;
  public Node(Node l, Node r, int s, int f){
    left = l; right = r; symbol = s; frequency = f;
  }
  public int compareTo(Object obj){
   Node n = (Node)obj;
   return frequency - n.frequency;
  }
}

public class DE2A2{

  static final int numberOfSymbols = 256;
  static final int blockSize = 1024;
  int[] freq = new int[numberOfSymbols];
  Node tree = null;
  String[] codewords = new String[numberOfSymbols];
  int[][] codetree = null; // Huffman tree with actualNumberOfSymbols leaves 
  int buf = 0; int position = 0;  // used by outputbits()
  int actualNumberOfSymbols = 0;  // number of symbols with freq > 0
  int filesize = 0;

  void count(String filename){ // count symbol frequencies
    byte[] buffer = new byte[blockSize];
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(filename);
    } catch (FileNotFoundException e){
      System.err.println(filename + " not found");
      System.exit(1);
    }
    int len = 0;
    for (int i = 0; i < numberOfSymbols; i++) freq[i] = 0;
   try {
    while ((len = fis.read(buffer)) >= 0){
      for (int i = 0; i < len; i++){
       int symbol = buffer[i];
       if (symbol < 0) symbol += 256;
       freq[symbol]++;
      }
      filesize += len;
    }
    fis.close();
   } catch (IOException e){
      System.err.println("IOException");
      System.exit(1);
   }
  }

  void makeTree(){  // make Huffman prefix codeword tree
   PriorityQueue<Node> pq = new PriorityQueue<Node>();
   for (int i = 0; i < numberOfSymbols; i++) if (freq[i] > 0){
       actualNumberOfSymbols++;
       pq.add(new Node(null, null, i, freq[i]));
   }
   int nodeLabel = numberOfSymbols;
   System.out.println("Huffman mergers");
   while (pq.size() > 1){
     Node a = pq.poll(); Node b = pq.poll();  // remove two subtress
     System.out.println(nodeLabel + " " + a.symbol + " " + b.symbol);
     pq.add(new Node(a, b, nodeLabel++, a.frequency + b.frequency));  // add the merged subtree
   }
   tree = pq.poll();  // root of tree as the last single subtree
   System.out.println();
  }

  void dfs(Node n, String code){  // generate all codewords
    if (n.symbol >= numberOfSymbols){
      dfs(n.left, code + "0"); dfs(n.right, code + "1");
    }else codewords[n.symbol] = code;
  }

  void makeCodewords(){
    dfs(tree, "");
    System.out.println("Codewords");
    for (int i = 0; i < numberOfSymbols; i++) if (codewords[i] != null)
     System.out.println(i + " " + codewords[i]);
    System.out.println();
  }

 void buildTreeArray(){  // make the prefic code tree
  codetree = new int[actualNumberOfSymbols * 2 - 1][2];
  int treeSize = 1;
  for (int i = 0; i < actualNumberOfSymbols * 2 - 1; i++)
    codetree[i][0] = codetree[i][1] = 0;
  for (int i = 0; i < numberOfSymbols; i++) 
   if (codewords[i] != null){
    int len = codewords[i].length();
    int k = 0;
    for (int j = 0; j < len; j++){
      int side = codewords[i].charAt(j) - '0';
      if (codetree[k][side] <= 0) codetree[k][side] = treeSize++;
      k = codetree[k][side];
    }
    codetree[k][1] = i;
    printTreeArray();
  }
 }

 void printTreeArray(){
  System.out.println("tree array");
  for (int i = 0; i < actualNumberOfSymbols * 2 - 1; i++)
    System.out.println(codetree[i][0] + " " + codetree[i][1]);
  System.out.println();
 }
    
  void outputTree(){
    System.out.write(actualNumberOfSymbols);
    for (int i = 0; i < actualNumberOfSymbols * 2 - 1; i++){
      System.out.write(codetree[i][0]);
      System.out.write(codetree[i][1]);
    }
    for (int i = 0; i < 3; i++){
      System.out.write(filesize & 0xff);
      filesize >>= 8; 
    }
  }
    
  void encode(String filename){ // compress filename to System.out
    System.out.println("Encoding result");
    byte[] buffer = new byte[blockSize];
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(filename);
    } catch (FileNotFoundException e){
      System.err.println(filename + " not found");
      System.exit(1);
    }
    int len = 0;
   try {
    while ((len = fis.read(buffer)) >= 0){
      for (int i = 0; i < len; i++){
       int symbol = buffer[i];
       if (symbol < 0) symbol += 256;
       System.out.print(codewords[symbol]);
//       outputbits(codewords[symbol]);
      }
    }
    fis.close();
   } catch (IOException e){
      System.err.println("IOException");
      System.exit(1);
   }
//    if (position > 0){ System.out.write(buf << (8 - position)); }
//    System.out.flush();
    System.out.println();
  }

  void outputbits(String bitstring){ // output codeword
     for (int i = 0; i < bitstring.length(); i++){
      buf <<= 1;
      if (bitstring.charAt(i) == '1') buf |= 1;
      position++;
      if (position == 8){
         position = 0;
         System.out.write(buf);
         buf = 0;
      }
     }
  }
    

  public static void main(String[] args){
    if (args.length < 1){
     System.err.println("Usage: java DE2A2 original > encoded");
     return;
    }
    DE2A2 de2 = new DE2A2();
    de2.count(args[0]);
    de2.makeTree();
    de2.makeCodewords();
    de2.buildTreeArray(); 
//    de2.outputTree();
    de2.encode(args[0]); 
  }
}

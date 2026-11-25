import java.util.*;

class HuffmanNode {
    char ch;
    int freq, order;
    HuffmanNode left, right;

    HuffmanNode(char ch, int freq, int order) {
        this.ch = ch; this.freq = freq; this.order = order;
    }
    HuffmanNode(HuffmanNode l, HuffmanNode r, int order) {
        this.ch = '\0'; this.freq = l.freq + r.freq;
        this.order = order; this.left = l; this.right = r;
    }
    boolean isLeaf() { return left == null && right == null; }
}

class CompressionRecord {
    String method, original, encoded;
    int originalBits, compressedBits;

    CompressionRecord(String method, String original, String encoded,
                      int originalBits, int compressedBits) {
        this.method = method; this.original = original; this.encoded = encoded;
        this.originalBits = originalBits; this.compressedBits = compressedBits;
    }
    double savedPercent() {
        return originalBits == 0 ? 0 :
            (originalBits - compressedBits) * 100.0 / originalBits;
    }
}

class HuffmanCoder {
    HuffmanNode root;
    TreeMap<Character, Integer> freq = new TreeMap<>();
    TreeMap<Character, String> codes = new TreeMap<>();
    int order = 0;

    void build(String text) {
        freq.clear(); codes.clear(); order = 0;
        for (char c : text.toCharArray()) freq.put(c, freq.getOrDefault(c, 0) + 1);

        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>(
            Comparator.comparingInt((HuffmanNode n) -> n.freq)
                      .thenComparingInt(n -> n.order));

        for (char c : freq.keySet())
            pq.add(new HuffmanNode(c, freq.get(c), order++));

        while (pq.size() > 1) {
            HuffmanNode a = pq.poll(), b = pq.poll();
            pq.add(new HuffmanNode(a, b, order++));
        }
        root = pq.poll();
        if (root != null) buildCodes();
    }

    void buildCodes() {
        if (root.isLeaf()) { codes.put(root.ch, "0"); return; }
        Stack<Object[]> st = new Stack<>();
        st.push(new Object[]{root, ""});
        while (!st.empty()) {
            Object[] item = st.pop();
            HuffmanNode node = (HuffmanNode)item[0];
            String code = (String)item[1];
            if (node.isLeaf()) codes.put(node.ch, code);
            else {
                st.push(new Object[]{node.right, code + "1"});
                st.push(new Object[]{node.left, code + "0"});
            }
        }
    }

    String encode(String text) {
        StringBuilder bits = new StringBuilder();
        for (char c : text.toCharArray()) bits.append(codes.get(c));
        return bits.toString();
    }

    String decode(String bits) {
        if (root == null) return "";
        if (root.isLeaf()) {
            StringBuilder same = new StringBuilder();
            for (int i = 0; i < bits.length(); i++) same.append(root.ch);
            return same.toString();
        }
        StringBuilder result = new StringBuilder();
        HuffmanNode node = root;
        for (char bit : bits.toCharArray()) {
            node = (bit == '0') ? node.left : node.right;
            if (node.isLeaf()) { result.append(node.ch); node = root; }
        }
        return result.toString();
    }

    void printTables() {
        System.out.println("\nCharacter\tFrequency\tCode");
        for (char c : freq.keySet())
            System.out.println(show(c) + "\t\t" + freq.get(c) + "\t\t" + codes.get(c));
    }

    void printLevels() {
        if (root == null) return;
        Queue<HuffmanNode> q = new ArrayDeque<>();
        q.add(root);
        System.out.println("\nHuffman Tree (level order):");
        while (!q.isEmpty()) {
            int size = q.size();
            while (size-- > 0) {
                HuffmanNode n = q.poll();
                System.out.print(n.isLeaf() ? show(n.ch) + ":" + n.freq + "  " : n.freq + "  ");
                if (n.left != null) q.add(n.left);
                if (n.right != null) q.add(n.right);
            }
            System.out.println();
        }
    }

    static String show(char c) {
        return c == ' ' ? "[space]" : Character.toString(c);
    }
}

class RunLengthCoder {
    String encode(String s) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length();) {
            int j = i;
            while (j < s.length() && s.charAt(j) == s.charAt(i)) j++;
            out.append(j - i).append(s.charAt(i));
            i = j;
        }
        return out.toString();
    }

    String decode(String s) {
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            int count = 0;
            while (i < s.length() && Character.isDigit(s.charAt(i)))
                count = count * 10 + (s.charAt(i++) - '0');
            if (i < s.length()) {
                char c = s.charAt(i++);
                for (int k = 0; k < count; k++) out.append(c);
            }
        }
        return out.toString();
    }
}

public class TextCompressionSystem {
    static Scanner sc = new Scanner(System.in);
    static String text = "";
    static HuffmanCoder huffman = new HuffmanCoder();
    static RunLengthCoder rle = new RunLengthCoder();
    static Stack<CompressionRecord> history = new Stack<>();
    static String lastHuffmanBits = "", lastRleText = "";

    static int bits(String s) { return s.length() * 8; }

    static void enterText() {
        System.out.print("Enter text: ");
        text = sc.nextLine();
        if (text.isEmpty()) System.out.println("Please enter some text.");
        else System.out.println("Text saved.");
    }

    static boolean ready() {
        if (text.isEmpty()) { System.out.println("Enter text first (option 1)."); return false; }
        return true;
    }

    static void huffmanCompress() {
        if (!ready()) return;
        huffman.build(text);
        lastHuffmanBits = huffman.encode(text);
        int original = bits(text), compressed = lastHuffmanBits.length();
        System.out.println("\nEncoded bits: " + lastHuffmanBits);
        System.out.println("Original size: " + original + " bits");
        System.out.println("Compressed size: " + compressed + " bits");
        System.out.printf("Space saved: %.2f%%%n",
            (original - compressed) * 100.0 / original);
        System.out.println("Decoded text: " + huffman.decode(lastHuffmanBits));
        System.out.println("Lossless check: " + huffman.decode(lastHuffmanBits).equals(text));
        history.push(new CompressionRecord("Huffman", text, lastHuffmanBits, original, compressed));
    }

    static void huffmanDecompress() {
        if (lastHuffmanBits.isEmpty() || huffman.root == null) {
            System.out.println("Run Huffman compression first."); return;
        }
        System.out.println("Decoded text: " + huffman.decode(lastHuffmanBits));
    }

    static void showTables() {
        if (!ready()) return;
        huffman.build(text);
        huffman.printTables();
    }

    static void showTree() {
        if (!ready()) return;
        huffman.build(text);
        huffman.printLevels();
    }

    static void rleCompress() {
        if (!ready()) return;
        if (text.chars().anyMatch(Character::isDigit)) {
            System.out.println("RLE needs text without digits."); return;
        }
        lastRleText = rle.encode(text);
        int original = bits(text), compressed = bits(lastRleText);
        System.out.println("RLE output: " + lastRleText);
        System.out.println("Original size: " + original + " bits");
        System.out.println("RLE size: " + compressed + " bits");
        System.out.printf("Space saved: %.2f%%%n",
            (original - compressed) * 100.0 / original);
        System.out.println("Decoded text: " + rle.decode(lastRleText));
        history.push(new CompressionRecord("RLE", text, lastRleText, original, compressed));
    }

    static void rleDecompress() {
        if (lastRleText.isEmpty()) { System.out.println("Run RLE compression first."); return; }
        System.out.println("Decoded text: " + rle.decode(lastRleText));
    }

    static void compare() {
        if (!ready()) return;
        huffman.build(text);
        String hb = huffman.encode(text);
        int original = bits(text), hSize = hb.length();
        System.out.println("\nMethod\t\tCompressed bits\tSpace saved");
        System.out.printf("Huffman\t\t%d\t\t%.2f%%%n", hSize,
            (original - hSize) * 100.0 / original);
        if (text.chars().anyMatch(Character::isDigit)) {
            System.out.println("RLE skipped: input contains digits.");
        } else {
            String rb = rle.encode(text);
            int rSize = bits(rb);
            System.out.printf("RLE\t\t%d\t\t%.2f%%%n", rSize,
                (original - rSize) * 100.0 / original);
        }
    }

    static void latest() {
        if (history.empty()) { System.out.println("No compression done yet."); return; }
        CompressionRecord r = history.peek();
        System.out.println("\nLatest: " + r.method + " | " + r.original);
        System.out.println("Encoded: " + r.encoded);
        System.out.println("Sizes: " + r.originalBits + " -> " + r.compressedBits + " bits");
        System.out.printf("Space saved: %.2f%%%n", r.savedPercent());
    }

    public static void main(String[] args) {
        int choice;
        do {
            System.out.println("\n===== TEXT COMPRESSION SYSTEM =====");
            System.out.println("1. Enter/change text");
            System.out.println("2. Huffman compress");
            System.out.println("3. Huffman decompress");
            System.out.println("4. Show frequency/code tables");
            System.out.println("5. Show Huffman tree (BFS)");
            System.out.println("6. RLE compress");
            System.out.println("7. RLE decompress");
            System.out.println("8. Compare Huffman and RLE");
            System.out.println("9. Show latest compression");
            System.out.println("0. Exit");
            System.out.print("Choose option: ");
            while (!sc.hasNextInt()) { sc.nextLine(); System.out.print("Enter a number: "); }
            choice = sc.nextInt(); sc.nextLine();
            switch (choice) {
                case 1: enterText(); break;
                case 2: huffmanCompress(); break;
                case 3: huffmanDecompress(); break;
                case 4: showTables(); break;
                case 5: showTree(); break;
                case 6: rleCompress(); break;
                case 7: rleDecompress(); break;
                case 8: compare(); break;
                case 9: latest(); break;
                case 0: System.out.println("Thank you!"); break;
                default: System.out.println("Invalid option.");
            }
        } while (choice != 0);
    }
}

package lucene5;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
@SuppressWarnings("resource")
public class AnalyzerTest {
	public static void main(String[] args) throws IOException {
		/*String s = "中华人民共和国位于亚洲东部，太平洋西岸，[1] 是工人阶级领导的、以工农联盟为基础的人民民主专政的社会主义国家。";
		
		Analyzer analyzer = new IKAnalyzer();
		TokenStream tokenStream = analyzer.tokenStream("text", s);
		displayTokens(tokenStream);*/
		
		//System.out.println(System.getProperty("java.io.tmpdir"));
		System.out.println(1 >>> 1);
		/*int size = 0;
		int[] heap = new int[10];
		heap[0] = 88;
		heap[1] = 36;
		heap[2] = 18;
		heap[3] = 2;
		heap[4] = 0;
		heap[7] = 40;
		heap[size] = 12;
		int i = size;
	    int node = heap[i];          // save bottom node
	    int j = i >>> 1;
	    while (j > 0 && node < heap[j]) {
	      heap[i] = heap[j];       // shift parents down
	      i = j;
	      j = j >>> 1;
	    }
	    heap[i] = node;  
		System.out.println(heap[i]);*/
	}
	
	public static void displayTokens(TokenStream tokenStream) throws IOException {
		OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
		PositionIncrementAttribute positionIncrementAttribute = tokenStream.addAttribute(PositionIncrementAttribute.class);
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		TypeAttribute typeAttribute = tokenStream.addAttribute(TypeAttribute.class);
		
		tokenStream.reset();
		int position = 0;
		while (tokenStream.incrementToken()) {
			int increment = positionIncrementAttribute.getPositionIncrement();
			if(increment > 0) {
				position = position + increment;
				System.out.print(position + ":");
			}
		    int startOffset = offsetAttribute.startOffset();
		    int endOffset = offsetAttribute.endOffset();
		    String term = charTermAttribute.toString();
		    System.out.println("[" + term + "]" + ":(" + startOffset + "-->" + endOffset + "):" + typeAttribute.type());
		}
	}
}

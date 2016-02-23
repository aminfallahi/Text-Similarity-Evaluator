package textsimilarity;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;


public class TextSimilarity {
    public static double wordOrderSimilarity(String[] first, String[] second, double threshold){
        double toReturn;
        Set totalSet = new HashSet();
        Set firstSet = new HashSet();
        Set secondSet = new HashSet();
        for (String first1 : first) {
            totalSet.add(first1);
            firstSet.add(first1);
        }
        for (String second1 : second) {
            totalSet.add(second1);
            secondSet.add(second1);
        }

        String all[] = new String[totalSet.size()];
        totalSet.toArray(all);

        double order1[] = new double[all.length];
        double order2[] = new double[all.length];
        double orderDiff = 0.0;
        double orderSum  = 0.0;

        for(int i=0; i<all.length; i++){
            if(firstSet.contains(all[i]))
                order1[i]=1;
            else
                for(int j=0;j<first.length;j++)
                    if(wordSimilarity(first[j], all[i])>threshold){
                        order1[i]=1;
                        break;
                    }
            if(secondSet.contains(all[i]))
                order2[i]=1;
            else
                for(int j=0;j<second.length;j++)
                    if(wordSimilarity(second[j], all[i])>threshold){
                        order2[i]=1;
                        break;
                    }
            orderDiff += Math.pow(order1[i] - order2[i],2);
            orderSum  += Math.pow(order1[i] + order2[i],2);
        }

        double negScore = Math.sqrt(orderDiff)/Math.sqrt(orderSum);
        toReturn = (1.0-negScore);
        return toReturn;
    }
    
    public static String[] preProcess(String s){
        String[] toReturn=null;
        char badChars[] = {',', '.', ':', ';', '"', '?', '!', ']', '['};
        s = s.trim();
        s = s.toLowerCase();
        for (int j=0; j<badChars.length; j++)
            s = s.replace(badChars[j],' ');
        toReturn=s.split(" +");
        return toReturn;
    }
    
    public static double wordSimilarity(String s1, String s2){
        double toReturn=lin.calcRelatednessOfWords(s1, s2);
        if (toReturn>1)
            toReturn=1;
        return toReturn;
    }
    
    public static ILexicalDatabase wndb=new NictWordNet();
    public static RelatednessCalculator lin=new Lin(wndb);
    public static RelatednessCalculator wup=new WuPalmer(wndb);
    public static RelatednessCalculator path=new Path(wndb);
    public static RelatednessCalculator hirst=new HirstStOnge(wndb);
    public static RelatednessCalculator jiang=new JiangConrath(wndb);
    public static RelatednessCalculator lea=new LeacockChodorow(wndb);
    public static RelatednessCalculator lesk=new Lesk(wndb);
    public static RelatednessCalculator resnik=new Resnik(wndb);
    

    
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        double wosThreshold=0.5;
        BufferedReader in1 = new BufferedReader(new FileReader("/home/amin/inputs/pair3/input1.txt"));
        BufferedReader in2 = new BufferedReader(new FileReader("/home/amin/inputs/pair3/input2.txt"));
        char[] buf1=new char[10240];
        char[] buf2=new char[10240];
        in1.read(buf1);
        in2.read(buf2);
        String[] s1=preProcess(String.valueOf(buf1));
        String[] s2=preProcess(String.valueOf(buf2));
        double algorithm1Percentage=wordOrderSimilarity(s1, s2, wosThreshold)*100;
        System.out.print("Similarity percentage if we consider two documents are one sentence each: ");
        System.out.print(algorithm1Percentage);
        System.out.println("%");
        
        
        
        
        
        String[] in1Sentences=String.valueOf(buf1).split("\\.");
        String[] in2Sentences=String.valueOf(buf2).split("\\.");
        //System.out.println(in1Sentences[1]);
        String[] in1s,in2s;
        double pointSum=0.0;
        
        for (int i=0; i<Math.min(in1Sentences.length, in2Sentences.length); i++){
            in1s=preProcess(in1Sentences[i]);
            in2s=preProcess(in2Sentences[i]);
            pointSum+=wordOrderSimilarity(in1s, in2s, wosThreshold);
            //System.out.println(pointSum);
        }
        double algorithm2Percentage=pointSum/Math.min(in1Sentences.length, in2Sentences.length)*100;
        System.out.print("Similarity percentage if we compare each pair of sentences: ");
        System.out.print(algorithm2Percentage);
        System.out.println("%");
        
        
        
        
        String[] smallerTextSentences;
        String[] biggerTextSentences;
        if (in1Sentences.length>in2Sentences.length){
            smallerTextSentences=in2Sentences;
            biggerTextSentences=in1Sentences;
        }
        else{
            smallerTextSentences=in1Sentences;
            biggerTextSentences=in2Sentences;
        }
        double maxPoint=0.0;
        for (int i=0; i<Math.min(in1Sentences.length, in2Sentences.length); i++){
            in1s=preProcess(smallerTextSentences[i]);
            for (int j=0; j<Math.max(in1Sentences.length, in2Sentences.length); j++){
                in2s=preProcess(biggerTextSentences[j]);
                if (wordOrderSimilarity(in1s, in2s, wosThreshold)>maxPoint)
                    maxPoint=wordOrderSimilarity(in1s, in2s, wosThreshold);
            }
            pointSum+=maxPoint;
            maxPoint=0.0;
            //System.out.println(pointSum);
        }
        double algorithm3Percentage=pointSum/Math.max(in1Sentences.length, in2Sentences.length)*100/2;
        System.out.print("Similarity percentage if we compare each sentence of first text with it's best match from second text: ");
        System.out.print(algorithm3Percentage);
        System.out.println("%");

        
        System.out.print("Average of all the algorithms: ");
        System.out.print((algorithm1Percentage+algorithm2Percentage+algorithm3Percentage)/3);
        System.out.println("%");
        
    }
    
}

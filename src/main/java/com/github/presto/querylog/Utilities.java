package com.github.presto.querylog;
import java.util.regex.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class Utilities {
    private final Logger logger;
    enum MemoryFactor{
        B(1/1000000000.0), kB(1/1000000.0), MB(1/1000.0), GB(1.0), TB(1000.0),PB(1000000.0) ;
        private double factor;
        MemoryFactor(double f) {
            factor = f;
        }
        double getFactor() {
            return factor;
        }
    }

    enum TimeFactor{
        NS(1/1000000000.0),US(1/1000000.0),MS(1/1000.0),S(1.0),M(60.0),H(3600.0),D(86400.0);
        private double factor;
        TimeFactor(double f ){
            factor = f;
        }
        double getFactor(){
            return factor;
        }
    }

    public Utilities(final LoggerContext loggerContext){
        this.logger = loggerContext.getLogger(QueryLogListener.class.getName());
    }

//    public static void main(String[] args) {
//        Utilities obj =  new Utilities();
//        System.out.println("Normalised Memory :" + obj.normalizeMemory("2000000.5kB"));
//
//        System.out.println("Normalised Time: " + obj.normalizeTime("PT0S")); //PT1M42.115S
//    }


    public double normalizeBytes(double numberOfBytes){
        logger.warn("Number of Bytes: "+ numberOfBytes);
        return numberOfBytes* MemoryFactor.B.getFactor();
    }


    public  double normalizeMemory(String text){
        //Convert to number of GB's
        String[] splitValues = this.splitUnits(text);
        if(splitValues[0] != null && splitValues[1] != null){
            return Double.parseDouble(splitValues[0]) * MemoryFactor.valueOf(splitValues[1]).getFactor();
        }
        return 0.0;
    }

    public  double normalizeTime(String text){
        String[] splitValues = this.splitUnits(text);
        if(splitValues[0] != null && splitValues[1] != null){
            return Double.parseDouble(splitValues[0]) * TimeFactor.valueOf(splitValues[1].toUpperCase()).getFactor();
        }
        return 0.0;
    }


    public String[] splitUnits(String text){
        logger.warn("input String: "+text);
        System.out.println("input String: "+text);
        String splitValues[] = new String[2];
        Matcher m1 = Pattern.compile("([0-9]*\\.?[0-9]+)([a-zA-Z]+)").matcher(text);
        if(m1.find()){
            System.out.println("Size :"+m1.group(1)+" Type: "+m1.group(2));
            splitValues[0] = m1.group(1);
            splitValues[1] = m1.group(2);
            logger.warn(" String: "+splitValues[0]+":-"+splitValues[1]);
            System.out.println(" String: "+splitValues[0]+":-"+splitValues[1]);
            return splitValues;
        }
        splitValues[0] = null;
        splitValues[1] = null;
        return splitValues;
    }
}

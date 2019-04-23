package com.github.presto.querylog;
import java.util.regex.*;

public class Utilities {
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

    public static void main(String[] args) {
        Utilities obj =  new Utilities();
        System.out.println("Normalised Memory :" + obj.normalizeMemory("2000000.5kB"));

        System.out.println("Normalised Time: " + obj.normalizeTime("3.0h"));
    }

    enum TimeFactor{
        ns(1/1000000000.0),us(1/1000000.0),ms(1/1000.0),s(1.0),m(60.0),h(3600.0),d(86400.0);
        private double factor;
        TimeFactor(double f ){
            factor = f;
        }
        double getFactor(){
            return factor;
        }
    }

    public  double normalizeMemory(String text){
        // Convert to number of GB's
        String[] splitValues = this.splitUnits(text);
        if(splitValues[0] != null && splitValues[1] != null){
            return Double.parseDouble(splitValues[0]) * MemoryFactor.valueOf(splitValues[1]).getFactor();
        }
        return 0.0;
    }

    public  double normalizeTime(String text){
        String[] splitValues = this.splitUnits(text);
        if(splitValues[0] != null && splitValues[1] != null){
            return Double.parseDouble(splitValues[0]) * TimeFactor.valueOf(splitValues[1]).getFactor();
        }
        return 0.0;
    }


    public String[] splitUnits(String text){
        String splitValues[] = new String[2];
        Matcher m1 = Pattern.compile("([0-9]*.?[0-9]+)([a-zA-Z]+)").matcher(text);
        if(m1.find()){
            System.out.println("Size :"+m1.group(1)+" Type: "+m1.group(2));
            splitValues[0] = m1.group(1);
            splitValues[1] = m1.group(2);
            return splitValues;
        }
        splitValues[0] = null;
        splitValues[2] = null;
        return splitValues;
    }
}

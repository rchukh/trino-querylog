package com.github.presto.querylog;

import java.text.DecimalFormat;
import java.io.*;

public class Utilities {
    private String command;
    public Utilities() {
        this.command = "ec2-describe-tags --region   $(curl -s http://169.254.169.254/latest/meta-data/placement/availability-zone  | sed -e \"s/.$//\")    --filter      resource-id=$(curl --silent http://169.254.169.254/latest/meta-data/instance-id)    --filter      key=application | cut -f5";
         }

    public static void main(String[] args) {
        Utilities utilities = new Utilities();
        utilities.command =  "ls -lrt";
        System.out.println(utilities.getApplicationName());
    }

    public String normalizeBytes(double numberOfBytes){
        DecimalFormat df = new DecimalFormat("0.00000000");
        return df.format(numberOfBytes/1073741824.0);
    }


    public String getApplicationName(){
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash","-c", this.command);
        try {

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
                break;
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                return output.toString().replace("\n","");//.replace("presto.","");
            } else {
                return "presto_default";
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "presto_default";
        } catch (Exception e) {
            e.printStackTrace();
            return "presto_default";
        }
    }
}

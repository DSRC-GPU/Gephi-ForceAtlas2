
package org.gephi.toolkit.demos;

import java.io.FileNotFoundException;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        if (args.length < 2) {
            System.out.println("Usage : ");
            System.exit(-1);
        }
        
        int noOfIterations = 100;
        if (args.length > 1)
            noOfIterations = Integer.parseInt(args[1]);
        
        ForceAtlas2Printer fp = new ForceAtlas2Printer(args[0], noOfIterations);
        fp.runLayout();
        fp.exportToPdf();
    }
}

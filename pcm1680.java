/**
* PCM1680 communicator, Java version
*    - main program
*
* created  : 2014.11.16.
* modified : 2014.11.28.
* author   : rosza
* version  : 0.04
*
* RESET  = "\u001B[0m";
* BLACK  = "\u001B[30m";
* RED    = "\u001B[31m";
* GREEN  = "\u001B[32m";
* YELLOW = "\u001B[33m";
* BLUE   = "\u001B[34m";
* PURPLE = "\u001B[35m";
* CYAN   = "\u001B[36m";
* WHITE  = "\u001B[37m";
*
*/

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CDevice;
import com.pi4j.io.i2c.I2CFactory;

public class pcm1680 {
  /**
  * @param args
  */
  public static void main(String[] args) throws Exception {
    System.out.println("\nPCM1680 controller, Java version v0.04 by rosza - 2014.11.28.\n");
    System.out.println("Usage: java -classpath .:classes:/opt/pi4j/lib/'*' pcm1680 register [data]");
    System.out.println("       - where \u001B[32m\033[1mregister\033[0m\u001B[0m is a hex parameter (eg.: 0x0a, or 10)");
    System.out.println("       - \u001B[32m\033[1mdata\033[0m\u001B[0m is a binary (eg.: 0b110111), hex (eg.: 0x37) or decimal (eg.: 55) parameter\n");
    System.out.println("       If \033[1mexactly one\033[0m parameter given the program prints out the corresponding registers' value.");
    System.out.println("       If \033[1mexactly two\033[0m parameter given the program writes the second parameter (data) to");
    System.out.println("       the register corresponding to the first argument (register).");
    System.out.println("       Otherwise the program prints out the available registers and their values.");
    System.out.println("       In parenthesis you can see the combined reference to a group of registers (eg.: \"MUT8\", \"MUT7\", \"(MUTE2)\").\n");

    // get I2C bus instance
    final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);

    pcm1680core pcm = new pcm1680core(bus);

    int register = 0;
    int data = 0;

    /** if got exactly one argument, then try to read the given register */
    if(args.length == 1) {
      try {
        register = Integer.decode(args[0]);
        pcm.action(register);
      }
      catch(NumberFormatException e) {
        if(!pcm.isValidRegister(args[0])) {
          System.exit(0);
        }
        pcm.action(args[0]);
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }
    /** if got exactly two arguments, then try to write to the given register the given data */
    else if(args.length == 2) {
      try {
        if(args[1].startsWith("0b")) {
          data = Integer.parseInt(args[1].substring(2), 2);
        }
        else {
          data = Integer.decode(args[1]);
        }
      }
      catch(NumberFormatException e) {
        System.out.println(args[1] + " is not a valid number");
        System.exit(0);
      }
      catch(Exception e) {
        e.printStackTrace();
      }

      try {
        register = Integer.decode(args[0]);
        pcm.action(register, data);
      }
      catch(NumberFormatException e) {
        if(!pcm.isValidRegister(args[0])) {
          System.exit(0);
        }
        pcm.action(args[0], data);
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }
    /** otherwise read all available registers and print out their values */
    else {
      pcm.action();
    }
  }
}

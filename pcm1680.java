/**
* PCM1680 communicator, Java version
*    - main program
*
* created  : 2014.11.16.
* modified : 2014.11.26.
* author   : rosza
* version  : 0.03
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
    System.out.println("\nPCM1680 controller, Java version v0.01 by rosza - 2014.11.16.\n");
    System.out.println("Usage: java -classpath .:classes:/opt/pi4j/lib/'*' pcm1680 register [data]");
    System.out.println("       - where \u001B[32m\033[1mregister\033[0m\u001B[0m is a hex parameter (eg.: 0a)");
    System.out.println("       - \u001B[32m\033[1mdata\033[0m\u001B[0m is a decimal parameter\n");
    System.out.println("       If \033[1mexactly one\033[0m parameter given the program prints out the corresponding registers' value.");
    System.out.println("       If \033[1mexactly two\033[0m parameter given the program writes the second parameter (data) to");
    System.out.println("       the register corresponding to the first argument (register).");
    System.out.println("       Otherwise the program prints out the available registers and their values.\n");

    // get I2C bus instance
    final I2CBus bus = I2CFactory.getInstance(I2CBus.BUS_1);

    PCM1680_core pcm = new PCM1680_core(bus);

    int register = 0;
    int data = 0;

    /** if got exactly one argument, then try to read the given register */
    if(args.length == 1) {
      try {
        register = Integer.parseInt(args[0], 16);
      }
      catch(Exception e) {
        System.out.println(args[0] + " is not a hex number");
        System.exit(0);
      }

      if(pcm.isValidRegister(register)) {
        pcm.action(register);
      }
    }
    /** if got exactly two arguments, then try to write to the given register the given data */
    else if(args.length == 2) {
      try {
        register = Integer.parseInt(args[0], 16);
      }
      catch(Exception e) {
        System.out.println(args[0] + " is not a hex number");
        System.exit(0);
      }
      try {
        data = Integer.parseInt(args[1]);
      }
      catch(Exception e) {
        System.out.println(args[1] + " is not a decimal number");
        System.exit(0);
      }
      pcm.action(register, data);
    }
    /** otherwise read all available registers and print out their values */
    else {
      pcm.action();
    }
  }
}

/**
* PCM1680 communicator, Java version
* created  : 2014.11.16.
* author   : rosza
* version  : 0.02
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

    pcmFunctions pcm = new pcmFunctions(bus);

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

  static class pcmFunctions {
    private final I2CDevice device;
    private static final int READ_ALL_REG = 1000;

    pcmFunctions(I2CBus bus) throws Exception {
      device = bus.getDevice(ADDRESS);
    }

    /**
    * This method reads and prints out all available registers by invoking
    * 'action(int register)' with READ_ALL_REG parameter.
    *
    * @param 
    */
    void action() {
      action(READ_ALL_REG);
    }

    /**
    * This method reads and prints out the given or all register(s).
    * If @param register = READ_ALL_REG then it is reads all available register.
    *
    * @param register register to read from
    */
    void action(int register) {
      int v = 0;

      if(register == READ_ALL_REG) {
        for(int i = 0; i < ALL_REG.length; i++) {
          v = read(device, ALL_REG[i]);
          System.out.printf("address 0x%02X = %s (%d) - %s\n", ALL_REG[i], String.format("%8s", Integer.toString(v, 2)).replace(" ", "0"), v, ALL_REG_TXT[i]);
        }
      }
      else {
        v = read(device, register);
        System.out.printf("address 0x%02X = %s (%d) - %s\n", register, String.format("%8s", Integer.toString(v, 2)).replace(" ", "0"), v, ALL_REG_TXT[getRegPos(register)]);
      }
    }

    /**
    * This method writes data to register.
    *
    * @param register register to write to
    * @param data data to be write to register
    */
    void action(int register, int data) {
      if(isValidRegister(register) && isValidData(register, data)) {
        if(data > 127) {
          data = data - 256;
        }
        write(device, register, (byte)data);
        action(register);
      }
      else {
        System.out.printf("Some invalid data in 'action(int register = %s, int data = %d)'\n", String.format("0x%2s", Integer.toString(register, 16)).replace(" ", "0"), data);
      }
    }

    /**
    * This method checks whether the given register is valid or not.
    *
    * @param register register to validate
    *
    * @return true if the register is valid, otherwise returns false
    */
    private boolean isValidRegister(int register) {
      for(int r : ALL_REG) {
        if(register == r) {
          return true;
        }
      }

      System.out.printf("0x%02X is not a valid register! Available registers in PCM1680 are:\n", register);
      for(int i = 0; i < ALL_REG.length; i++) {
        System.out.printf("0x%02X - %s\n", ALL_REG[i], ALL_REG_TXT[i]);
      }
      System.out.println("\n");

      return false;
    }

    /**
    * This method checks whether the given data can be write to the given register.
    *
    * @param register target register
    * @param data data to validate
    *
    * @return true if the data is valid for the register, otherwise false
    */
    private boolean isValidData(int register, int data) {
      if((data == 0) & (register != 0x0e)) {
        return true;
      }
      if(data > 255) {
        return false;
      }

      switch(register) {
        /** ATT10-ATT17 (mask = 0b11111111) */
        case 0x01:
        /** ATT20-ATT27 (mask = 0b11111111) */
        case 0x02:
        /** ATT30-ATT37 (mask = 0b11111111) */
        case 0x03:
        /** ATT40-ATT47 (mask = 0b11111111) */
        case 0x04:
        /** ATT50-ATT57 (mask = 0b11111111) */
        case 0x05:
        /** ATT60-ATT67 (mask = 0b11111111) */
        case 0x06:
        /** ATT70-ATT77 (mask = 0b11111111) */
        case 0x10:
        /** ATT80-ATT87 (mask = 0b11111111) */
        case 0x11:
          return bitCompare(ATT_MASK, data);
          //break;

        /** MUT1-MUT6 (mask = 0b00111111) */
        case 0x07:
        /** DAC1-DAC6 enable (mask = 0b00111111) */
        case 0x08:
          return bitCompare(DAC_MUT_MASK[0], data);
          //break;
        /** MUT7-MUT8 (mask = 0b00000011) */
        case 0x12:
        /** DAC7-DAC8 enable (mask = 0b00000011) */
        case 0x13:
          if(data > 3) {
            return false;
          }
          return bitCompare(DAC_MUT_MASK[1], data);
          //break;

        /** FLT (mask = 0b00100000) & FMT2, FMT1, FMT0 (mask = 0b00000111) */
        case 0x09:
          if((data == 6) || (data == 7) || (data == 38) || (data == 39)) {
            return false;
          }
          return bitCompare((FLT_MASK | FMT_MASK), data);
          //break;

        /** SRST (mask = 0b10000000), ZREV (mask = b01000000), DREV (mask = 0b00100000), DMF (mask = 0b00011000), DMC (mask = 0b00000001) */
        case 0x0a:
          return bitCompare((SRST_MASK | ZREV_MASK | DREV_MASK | DMF_MASK | DMC_MASK), data);
          //break;

        /** OVER (mask = 0b10000000) */
        case 0x0c:
          return bitCompare(OVER_MASK, data);
          //break;

        /** DAMS (mask = 0b10000000), AZRO (mask = 0b01100000) */
        case 0x0d:
          return bitCompare((DAMS_MASK | AZRO_MASK), data);
          //break;

        /** ZERO (read-only register!!) */
        case 0x0e:
          System.out.println("ZERO register is read-only register!!");
          break;
      }
      return false;
    }

    /**
    * This method validates the given data with a bit mask.
    *
    * @param mask the data has to match with this mask. if the mask specifies 0
    *             in a bit, the data must not contain 1 in the same bit.
    *             if the mask specifies 1 in a bit, the data may contain 0 or 1.
    * @param data data to validate
    *
    * @return returns true if the data is valid, otherwise false
    */
    private boolean bitCompare(int mask, int data) {
      int i = 1;
      while(i < 256) {
        if((i & mask) != i) {
          if((i & data) == i) {
            return false;
          }
        }
        i = i << 1;
      }
      return true;
    }

    /**
    * This method returns the given registers' position in ALL_REG array.
    *
    * @param register register position to get
    *
    * @return the position of the register in ALL_REG array
    */
    private int getRegPos(int register) {
      for(int i = 0; i < ALL_REG.length; i++) {
        if(register == ALL_REG[i]) {
          return i;
        }
      }

      return 0;
    }

    /**
    * This method reads data from the given register.
    *
    * @param device the device object
    * @param register register to read
    *
    * @return returns the data
    */
    private int read(I2CDevice device, int register) {
      int res = 0;
      for(int i = 0; i < 3; i++) {
        try {
          res = device.read(register);
          break;
        }
        catch(Exception e) {
          // e.printStackTrace();
        }
      }

      return res;
    }

    /**
    * This method writes data to the given register.
    *
    * @param device the device object
    * @param register register to write
    * @param data data to write to register
    *
    * @return
    */
    private void write(I2CDevice device, int register, byte data) {
      for(int i = 0; i < 3; i++) {
        try {
          device.write(register, data);
          break;
        }
        catch(Exception e) {
          // e.printStackTrace();
        }
      }
    }

  /** PCM1680 address */
  private static final int ADDRESS = 0x4c;

  /**
  *   PCM1680 registers
  *
  *   x = variable bit, can be 0 or 1
  *   where z given, byte combined with other byte(s)!!
  *   where bits specified 0 or 1, should be left as is!!
  *
  *   MASK arrays are for getting only the desired bits
  */

  /** available registers */
  private static final int ALL_REG[]        = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0c, 0x0d, 0x0e, 0x10, 0x11, 0x12, 0x13};
  private static final String ALL_REG_TXT[] = {"AT17 AT16 AT15 AT14 AT13 AT12 AT11 AT10",
                                               "AT27 AT26 AT25 AT24 AT23 AT22 AT21 AT20",
                                               "AT37 AT36 AT35 AT34 AT33 AT32 AT31 AT30",
                                               "AT47 AT46 AT45 AT44 AT43 AT42 AT41 AT40",
                                               "AT57 AT56 AT55 AT54 AT53 AT52 AT51 AT50",
                                               "AT67 AT66 AT65 AT64 AT63 AT62 AT61 AT60",
                                               "RSV RSV MUT6 MUT5 MUT4 MUT3 MUT2 MUT1",
                                               "RSV RSV DAC6 DAC5 DAC4 DAC3 DAC2 DAC1",
                                               "RSV RSV FLT RSV RSV FMT2 FMT1 FMT0",
                                               "SRST ZREV DREV DMF1 DMF0 RSV RSV DMC",
                                               "OVER RSV RSV RSV RSV RSV RSV RSV",
                                               "DAMS AZRO1 AZRO0 RSV RSV RSV RSV RSV",
                                               "ZERO8 ZERO7 ZERO6 ZERO5 ZERO4 ZERO3 ZERO2 ZERO1",
                                               "AT77 AT76 AT75 AT74 AT73 AT72 AT71 AT70",
                                               "AT87 AT86 AT85 AT84 AT83 AT82 AT81 AT80",
                                               "RSV RSV RSV RSV RSV RSV MUT8 MUT7",
                                               "RSV RSV RSV RSV RSV RSV DAC8 DAC7"};

  private static final int ATT_REG[]   = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x10, 0x11};         // attenuation registers (xxxxxxxx)
  private static final int ATT_VALUE[] = new int[8];                                               // attenuation values, can be 0-255
  private static final int ATT_MASK    = 0b11111111;

  private static final int MUT_REG[]   = {0x07, 0x12};                                             // mute registers (00xxxxxx, 000000xx)
  private static final int MUT_VALUE[] = new int[2];                                               // 0 or 1 each
  //private static final int MUT_MASK[]  = {0b00111111, 0b00000011};

  private static final int DAC_REG[]   = {0x08, 0x13};                                             // DAC enable/disable registers (00xxxxxx, 000000xx)
  private static final int DAC_VALUE[] = new int[2];                                               // 0 (default) or 1 each (0 = DAC operation enabled)
  //private static final int DAC_MASK[]  = {0b00111111, 0b00000011};
  private static final int DAC_MUT_MASK[]  = {0b00111111, 0b00000011};

  private static final int FLT_REG[]   = {0x09};                                                   // Digital Filter Roll-Off (00x00zzz) (z = FMT2, FMT1, FMT0)
  private static final int FLT_VALUE[] = new int[1];                                               // 0 (default) or 1
  private static final int FLT_MASK    = 0b00100000;
  private static final int FMT_REG[]   = {0x09};                                                   // Audio Interface Data Format registers (00z00xxx) (z = FLT)
  private static final int FMT_VALUE[] = new int[1];                                               // 000, 001, 010, 011, 100, 101 (default)
  private static final int FMT_MASK    = 0b00000111;                                               // 110 & 111 Reserved!!!

  private static final int SRST_REG[]   = {0x0a};                                                  // Reset (xzzzz00z) (z = ZREV, DREV, DMF1, DMF0, DMC)
  private static final int SRST_VALUE[] = new int[1];                                              // 0 (default) or 1
  private static final int SRST_MASK    = 0b10000000;
  private static final int ZREV_REG[]   = {0x0a};                                                  // Zero-Flag Polarity Select (zxzzz00z) (z = SRST, DREV, DMF1, DMF0, DMC)
  private static final int ZREV_VALUE[] = new int[1];                                              // 0 (default) or 1
  private static final int ZREV_MASK    = 0b01000000;
  private static final int DREV_REG[]   = {0x0a};                                                  // Output Phase Select (zzxzz00z) (z = SRST, ZREV, DMF1, DMF0, DMC)
  private static final int DREV_VALUE[] = new int[1];                                              // 0 (default) or 1
  private static final int DREV_MASK    = 0b00100000;
  private static final int DMF_REG[]    = {0x0a};                                                  // Sampling Frequency Selection for the De-Emphasis Function  (zzzxx00z) (z = SRST, ZREV, DREV, DMC)
  private static final int DMF_VALUE[]  = new int[1];                                              // 00 (default), 01, 10, 11
  private static final int DMF_MASK     = 0b00011000;
  private static final int DMC_REG[]    = {0x0a};                                                  // Digital De-Emphasis All-Channel Function Control (zzzzz00x) (z = SRST, ZREV, DREV, DMF1, DMF0)
  private static final int DMC_VALUE[]  = new int[1];                                              // 0 (default) or 1
  private static final int DMC_MASK     = 0b00000001;

  private static final int OVER_REG[]   = {0x0c};                                                  // Oversampling Rate Control (x0000000)
  private static final int OVER_VALUE[] = new int[1];                                              // 0 (default) or 1
  private static final int OVER_MASK    = 0b10000000;

  private static final int DAMS_REG[]   = {0x0d};                                                   // Digital Attenuation Mode Select (xzz00000) (z = AZRO)
  private static final int DAMS_VALUE[] = new int[1];                                               // 0 (default) or 1
  private static final int DAMS_MASK    = 0b10000000;
  private static final int AZRO_REG[]   = {0x0d};                                                   // Zero-Flag Channel-Combination Select (zxx00000) (z = DAMS)
  private static final int AZRO_VALUE[] = new int[1];                                               // 00 (default), 01, 10, 11
  private static final int AZRO_MASK    = 0b01100000;

  private static final int ZERO_REG[]   = {0x0e};                                                   // Zero-Detect Status (Read-Only) (xxxxxxxx)
  private static final int ZERO_VALUE[] = new int[1];                                               // Read-Only

  }
}
/**
* PCM1680 communicator, Java version
* created  : 2014.11.14.
* author   : rosza
* version  : 0.01
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
  public static void main(String[] args) {


    final I2CBus bus;
    final I2CDevice device;

    System.out.println("PCM1680 controller, Java version v0.01");

    // get I2C bus instance

    
    try {
      bus = I2CFactory.getInstance(I2CBus.BUS_1);

      System.out.println("address = " + regs.ADDRESS);
      System.out.println("bus = " + bus);

      device = bus.getDevice(regs.ADDRESS);

      System.out.println("device = " + device);

      System.out.println("reading available registers...");
      final int MAX_ATTEMPTS = 3;
      int attempts = 0;
      int v = 0;
      for(int i = 0; i < regs.ALL_REGS.length; i++) {
        v = read(device, regs.ALL_REGS[i]);
        System.out.format("address 0x%02X = %08s\n", regs.ALL_REGS[i], Integer.toString(v, 2));
      }
    }
/*
      System.out.println("write ATT1...");
      try {
        device.write(regs.ATT_REGS[0], (byte)0b00011101);
        Thread.sleep(100);
        System.out.format("address 0x01 = %s (%d)\n", Integer.toString(regs.ATT_VALUES[0], 2), regs.ATT_VALUES[0]);
        Thread.sleep(500);
      }
      catch(IOException e) {
      }
      
      System.out.println("increment ATT1...");
      int z = regs.ATT_VALUES[0] + 10;
      System.out.println("ATT_VALUES[0] = " + regs.ATT_VALUES[0]);
      System.out.println("z = " + z);
      for(int i = regs.ATT_VALUES[0]; i < z; i++) {
        if(i < 256) {
          write(device, regs.ATT_REGS[0], (byte)i);
          Thread.sleep(100);
        }
       regs. ATT_VALUES[0] = read(device, regs.ATT_REGS[0]);
        System.out.format("%d - address 0x0%x = %s (%d)\n", i, regs.ATT_REGS[0], Integer.toString(regs.ATT_VALUES[0], 2), regs.ATT_VALUES[0]);
        Thread.sleep(100);
      }
    }*/
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  public static int read(I2CDevice device, int address) {
    int res = 0;
    for(int i = 0; i < 3; i++) {
      try {
        res = device.read(address);
        break;
      }
      catch(Exception e) {
        //e.printStackTrace();
      }
    }

    return res;
  }

  public static void write(I2CDevice device, int address, byte data) {
    for(int i = 0; i < 3; i++) {
      try {
        device.write(address, data);
        break;
      }
      catch(Exception e) {
        //e.printStackTrace();
      }
    }
  }

  private static class regs {
    static final int ADDRESS = 0x4c;            // PCM1680 address

    static final int ALL_REGS[] = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0c, 0x0d, 0x0e, 0x10, 0x11, 0x12, 0x13};       // available registers

    /**
    *   x = variable bit, can be 0 or 1
    *   where bits specified 0 or 1, should be left as is!!
    */
    static final int ATT_REGS[]   = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x10, 0x11};         // attenuation registers (xxxxxxxx)
    static final int ATT_VALUES[] = new int[8];                                               // attenuation values, can be 0-255

    static final int MUT_REGS[]   = {0x07, 0x12};                                             // mute registers (00xxxxxx, 000000xx)
    static final int MUT_VALUES[] = new int[2];                                               // 0, 1

    static final int DAC_REGS[]   = {0x08, 0x13};                                             // DAC registers (00xxxxxx, 000000xx)
    static final int DAC_VALUES[] = new int[2];                                               // 0, 1

    static final int FLT_FMT_REGS[]   = {0x09};                                               // Digital Filter Roll-Off & Audio Interface Data Format registers (00x00xxx)
    static final int FLT_FMT_VALUES[] = new int[1];                                           // 0, 1 for FLT; 000, 001, 010, 011, 100, 101 (default) for FMT
/*
    static final int SRST_ZREV_DREV_DMF_REGS[]   = {0x0a};                                               // Digital Filter Roll-Off & Audio Interface Data Format registers (00x00xxx)
    static final int SRST_ZREV_DREV_DMF_VALUES[] = new int[1];                                           // 0, 1 for FLT; 000, 001, 010, 011, 100, 101 (default) for FMT
*/
  }
}

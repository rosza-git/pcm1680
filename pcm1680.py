#
# PCM1680 communicator, python version
# created  : 2014.11.09.
# author   : rosza
# version  : 0.01

import smbus
import time

PCM_ADDRESS = 0x4c

bus = smbus.SMBus(1)

print bus.read_word_data(PCM_ADDRESS, 0x01)
print bin(bus.read_word_data(PCM_ADDRESS, 0x01))
bus.write_word_data(PCM_ADDRESS, 1, 0xC8)
print bus.read_word_data(PCM_ADDRESS, 0x01)

"""
#define ATT1 0x01       // 0x00 00 00 00 xx xx xx xx
#define ATT2 0x02       // 0x00 00 00 00 xx xx xx xx
#define ATT3 0x03       // 0x00 00 00 00 xx xx xx xx
#define ATT4 0x04       // 0x00 00 00 00 xx xx xx xx
#define ATT5 0x05       // 0x00 00 00 00 xx xx xx xx
#define ATT6 0x06       // 0x00 00 00 00 xx xx xx xx
#define ATT7 0x10       // 0x00 00 00 00 xx xx xx xx
#define ATT8 0x11       // 0x00 00 00 00 xx xx xx xx

#define MUT1 0x07       // 0x00 00 00 00 00 xx xx xx
#define MUT2 0x12       // 0x00 00 00 00 00 00 00 xx

#define DAC1 0x08       // 0x00 00 00 00 00 xx xx xx
#define DAC2 0x13       // 0x00 00 00 00 00 00 00 xx

// code from: http://stackoverflow.com/questions/15114140/writing-binary-number-system-in-c-code
#define B(x) S_to_binary_(#x)
static inline unsigned long long S_to_binary_(const char *s) {
  unsigned long long i = 0;
  while (*s) {
    i <<= 1;
    i += *s++ - '0';
  }
  return i;
}
// end code

int main(void) {
  unsigned char r8;
  int r16;
  unsigned char w8;
  int w16;
  int b;

  printf("Interfacing PCM1680\n");
  if(wiringPiSetup() == -1)
    exit(1);

  printf("wiringPiSetup() result: %d\n", wiringPiSetup());
  int pcmID = wiringPiI2CSetup(PCM_ADDRESS);
  printf("PCM ID: %d\n", pcmID);
  for(int i = 0; i < 20; i++) {
    if(i == 0 || i == 11 || i == 15)
      continue;
    r16 = wiringPiI2CReadReg16(pcmID, i);
    printf("r16 - %02X: %08X  -  %d\n", i, r16, r16);
  }

  w16 = wiringPiI2CWriteReg16(pcmID, 0x06, 255);
  printf("w16: %08X  -  %d\n", w16, w16);

  while(r16 < 254) {
    r16 = wiringPiI2CReadReg16(pcmID, 0x02);
    printf("r16: %08X  -  %d\n", r16, r16);
    delay(250);
  }

  w8 = wiringPiI2CWriteReg8(pcmID, 0x01, 128);
  printf("w8: %04X  -  %d\n", w8, w8);
  r8 = wiringPiI2CReadReg8(pcmID, 0x01);
  printf("r8: %04X  -  %d\n", r8, r8);

  b = B(0000000011111111);
  printf("binary: %d\n", b);
  return 0;
}
"""
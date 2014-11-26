/*
* PCM1680 communicator
* created  : 2014.11.09.
* author   : rosza
* version  : 0.01
*
* help: https://projects.drogon.net/raspberry-pi/wiringpi/i2c-library/
*/

#include <wiringPi.h>
#include <wiringPiI2C.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <ncurses.h>

#define PCM_ADDRESS 0x4c

#define ATT1 0x01       // 0x00 00 00 00 xx xx xx xx
#define ATT2 0x02       // 0x00 00 00 00 xx xx xx xx
#define ATT3 0x03       // 0x00 00 00 00 xx xx xx xx
#define ATT4 0x04       // 0x00 00 00 00 xx xx xx xx
#define ATT5 0x05       // 0x00 00 00 00 xx xx xx xx
#define ATT6 0x06       // 0x00 00 00 00 xx xx xx xx
#define ATT7 0x10       // 0x00 00 00 00 xx xx xx xx
#define ATT8 0x11       // 0x00 00 00 00 xx xx xx xx
#define MIN_ATT 0       // 
#define MAX_ATT 255     // 

#define MUT1 0x07       // 0x00 00 00 00 00 xx xx xx
#define MUT2 0x12       // 0x00 00 00 00 00 00 00 xx

#define DAC1 0x08       // 0x00 00 00 00 00 xx xx xx
#define DAC2 0x13       // 0x00 00 00 00 00 00 00 xx

int changeAtt(int reg, int id, int dir);

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

int main (int argsNum, char* args[]) {
  int regs[18] = {};

  if(wiringPiSetup() == -1) {
    printf("error occure in wiringPiSetup()");
    exit(1);
  }

  initscr();
  noecho();
  mvprintw(2, 2, "PCM1680 communicator - v0.02");

  int pcmID = wiringPiI2CSetup(PCM_ADDRESS);
  mvprintw(3, 2, "PCM1680 ID: 0x%02X", pcmID);
  refresh();

  int j = 0;
  for(int i = 1; i < 20; i++) {
    regs[j] = wiringPiI2CReadReg16(pcmID, j);
    j++;
  }

  mvprintw(5, 3, "ALL REGISTERS");
  for(int i = 1; i < 20; i++) {
    mvprintw(5 + i, 4, "reg %02X = %08X", i, regs[i]);
  }

  mvprintw(5, 26, "ATTENUATION");
  for(int i = 1, j = 1; i < 20; i++) {
    switch(i) {
      case ATT1:
      case ATT2:
      case ATT3:
      case ATT4:
      case ATT5:
      case ATT6:
      case ATT7:
      case ATT8:
        mvprintw(5 + j, 27, "ATT%d (reg %02X) = %08X", j, i, regs[i]);
        j++;
        break;
    }
  }
/*
  mvprintw(5, 56, "MUTE");
  for(int i = 1, j = 1; i < 20; i++) {
    switch(i) {
      case MUT1:
      case MUT2:
        mvprintw(5 + j, 57, "MUTE%d (reg %02X) = %08X", j, i, regs[i]);
        j++;
        break;
    }
  }
*/
  int c;
  int result = 0;

  do {
    c = getch();
    mvprintw(30, 5, "c = %c", c);
    switch(c) {
      case '+':
      case '-':
        result = changeAtt(ATT1, pcmID, c);
        int r = wiringPiI2CReadReg16(pcmID, ATT1);
        mvprintw(6, 27, "ATT1 (reg 01) = %08X %p", r, r);
        mvprintw(30, 5, "result = %d", result);
        break;
    }
  } while(c != 'q');

  echo();
  cbreak();
  endwin();
  return 0;
}

int changeAtt(int reg, int id, int dir) {
  int currentAtt = wiringPiI2CReadReg16(id, reg);

  switch(dir) {
    case '+':
      currentAtt++;
      break;
    case '-':
      currentAtt--;
      break;
  }

  if(currentAtt > MAX_ATT) {
    currentAtt = MAX_ATT;
  }
  else {
    currentAtt = MIN_ATT;
  }

  return wiringPiI2CWriteReg16(id, reg, currentAtt);
}

string b(int number) {
  string b = "";
  int r = 0;

  while(number > 1) {
    r = number % 2;
    number = number / 2;
    b.insert(0, r);
  }

  return b;
}
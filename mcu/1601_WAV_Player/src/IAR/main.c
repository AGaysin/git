#include "iom32a.h"
#include <string.h>
#include "pff.h"
#include "diskio.h"
#include "integer.h"








/* This is the fuse settings of this project. The fuse data will be included
in the output hex file with program code. However some old flash programmers
cannot load the fuse bits from hex file. If it is the case, remove this line
and use these values to program the fuse bits. */


#define FCC(c1,c2,c3,c4)	(((DWORD)c4<<24)+((DWORD)c3<<16)+((WORD)c2<<8)+(BYTE)c1)	/* FourCC */


/*---------------------------------------------------------*/
/* Work Area                                               */
/*---------------------------------------------------------*/
unsigned char secondTrack=0;
unsigned int secondTrackTimer=0;
unsigned char buttonEnable=1;
unsigned int buttonEnTimer=0;


volatile BYTE FifoRi, FifoWi, FifoCt;	/* FIFO controls */

BYTE Buff[64];		/* Wave output FIFO */

FATFS Fs;			/* File system object */
DIR Dir;			/* Directory object */
FILINFO Fno;		/* File information */

WORD rb;			/* Return value. Put this here to avoid avr-gcc's bug */

unsigned long int OCR1A_BACKUP;
unsigned int OCR1A_ADJUST;
void pwm_init();
void timer1_init();


// C 8-bit ??????? ???????
/*const unsigned char sinetable[256] = {
	128,131,134,137,140,143,146,149,152,156,159,162,165,168,171,174,
	176,179,182,185,188,191,193,196,199,201,204,206,209,211,213,216,
	218,220,222,224,226,228,230,232,234,236,237,239,240,242,243,245,
	246,247,248,249,250,251,252,252,253,254,254,255,255,255,255,255,
	255,255,255,255,255,255,254,254,253,252,252,251,250,249,248,247,
	246,245,243,242,240,239,237,236,234,232,230,228,226,224,222,220,
	218,216,213,211,209,206,204,201,199,196,193,191,188,185,182,179,
	176,174,171,168,165,162,159,156,152,149,146,143,140,137,134,131,
	128,124,121,118,115,112,109,106,103,99, 96, 93, 90, 87, 84, 81, 
	79, 76, 73, 70, 67, 64, 62, 59, 56, 54, 51, 49, 46, 44, 42, 39, 
	37, 35, 33, 31, 29, 27, 25, 23, 21, 19, 18, 16, 15, 13, 12, 10, 
	9, 8, 7, 6, 5, 4, 3, 3, 2, 1, 1, 0, 0, 0, 0, 0, 
	0, 0, 0, 0, 0, 0, 1, 1, 2, 3, 3, 4, 5, 6, 7, 8, 
	9, 10, 12, 13, 15, 16, 18, 19, 21, 23, 25, 27, 29, 31, 33, 35, 
	37, 39, 42, 44, 46, 49, 51, 54, 56, 59, 62, 64, 67, 70, 73, 76, 
	79, 81, 84, 87, 90, 93, 96, 99, 103,106,109,112,115,118,121,124
};*/
unsigned char i,j;

/*---------------------------------------------------------*/

static
DWORD load_header (void)	/* 0:Invalid format, 1:I/O error, >=1024:Number of samples */
{
	DWORD sz, f;
	BYTE b, al = 0;


	if (pf_read(Buff, 12, &rb)) return 1;	/* Load file header (12 bytes) */

	if (rb != 12 || LD_DWORD(Buff+8) != FCC('W','A','V','E')) return 0;

	for (;;) {
		asm("WDR");
		pf_read(Buff, 8, &rb);			/* Get Chunk ID and size */
		if (rb != 8) return 0;
		sz = LD_DWORD(&Buff[4]);		/* Chunk size */

		switch (LD_DWORD(&Buff[0])) {	/* Switch by chunk ID */
			case FCC('f','m','t',' ') :					/* 'fmt ' chunk */
			if (sz & 1) sz++;						/* Align chunk size */
			if (sz > 100 || sz < 16) return 0;		/* Check chunk size */
			pf_read(Buff, sz, &rb);					/* Get content */
			if (rb != sz) return 0;
			if (Buff[0] != 1) return 0;				/* Check coding type (LPCM) */
			b = Buff[2];
			if (b != 1 && b != 2) return 0;			/* Check channels (1/2) */
			FLAG = al = b;						/* Save channel flag */
			b = Buff[14];
			if (b != 8 && b != 16) return 0;		/* Check resolution (8/16 bit) */
			FLAG |= b;							/* Save resolution flag */
			if (b & 16) al <<= 1;
			f = LD_DWORD(&Buff[4]);					/* Check sampling freqency (8k-48k) */
			if (f < 8000 || f > 48000) return 4;
                        //pwm_init();
                        //timer1_init();
                        /* Set sampling interval */
                        TCCR1B = 0x0A; //CTC mode
                        TCNT1 = 0;
                        OCR1A = (BYTE)(16000000/ 8 / f) - 1;	
                        TIMSK |= (1 << OCIE1A);
			//OCR2 = (BYTE)(2000000 / f) - 1;		/* Set sampling interval */
                        //TCCR2 = 0x0A; //clk/8 CTC mode
                        //TIMSK = 0x80; //OCIE2
			break;

			case FCC('d','a','t','a') :		/* 'data' chunk */
			if (!al) return 0;							/* Check if format is valid */
			if (sz < 1024 || (sz & (al - 1))) return 0;	/* Check size */
			if (Fs.fptr & (al - 1)) return 0;			/* Check word alignment */
			return sz;									/* Start to play */

			case FCC('D','I','S','P') :		/* 'DISP' chunk */
			case FCC('L','I','S','T') :		/* 'LIST' chunk */
			case FCC('f','a','c','t') :		/* 'fact' chunk */
			if (sz & 1) sz++;				/* Align chunk size */
			pf_lseek(Fs.fptr + sz);			/* Skip this chunk */
			break;

			default :						/* Unknown chunk */
			return 0;
		}
	}

	return 0;
}



static
void ramp (
int dir		/* 0:Ramp-down, 1:Ramp-up */
)
{
	#if MODE != 3
	BYTE v, d, n;


	if (dir) {
		v = 0; d = 1;
		} else {
		v = 128; d = (BYTE)-1;
	}

	n = 128;
	do {
		v += d;
		OCR1A = v; OCR1B = v;
		delay_us(100);
	} while (--n);
	#else
	dir = dir ? 128 : 0;
	OCR1A = (BYTE)dir; OCR1B = (BYTE)dir;
	#endif
}



static
FRESULT play (char* dir)
{
	DWORD sz;
	FRESULT res;
	BYTE sw;
	WORD btr;



	
	res = pf_open(dir);		/* Open sound file */
        //res = pf_open("WAV/5.WAV");	
        //res = pf_open("/3.WAV");	
        //res = pf_open("/4.WAV");	
        //res = pf_open("WAV/44100.WAV");	
        //res = pf_open("WAV/22050.WAV");	
	if (res == FR_OK) {
		sz = load_header();			/* Check file format and ready to play */
		if (sz < 1024) return 255;	/* Cannot play this file */

		FifoCt = 0; FifoRi = 0; FifoWi = 0;	/* Reset audio FIFO */

                
                //Частота уже настроена на OCR1A = (BYTE)(F_CPU / 8 / f) - 1;	
                //Зайпуск таймера и настройка прерываний
                //OCR2 = 0x80;
                
                asm("sei");
                /*if (!TCCR1B || !TCCR1A)
                {
                  TCCR1A = 0xF1; //fast PWM 8 bit mode
                  TCCR1B = 0x01; //F_CLK/1 frequency rate
                  OCR1A = 0x80; //0.5 test
                  OCR1B = 0x80; //0.25 test
                  //TIMSK |= (1<<2); //TOVF1
                }*/
                
                str1play = 0;
                str2play = 0;
                PlayPtr = 0;


		pf_read(0, 512 - (Fs.fptr % 512), &rb);	/* Snip sector unaligned part */
		sz -= rb;
		sw = 1;	/* Button status flag */
		do {	/* Data transfer loop */
			asm("WDR");

			btr = (sz > 1024) ? 1024 : (WORD)sz;/* A chunk of audio data */
			res = pf_read(0, btr, &rb);	/* Forward the data into audio FIFO */
			if (rb != 1024) break;		/* Break on error or end of data */
			sz -= rb;					/* Decrease data counter */

			sw <<= 1;					/* Break on button down */
		} while ((PINB & 1) || ++sw != 1);
	}

        //if (PlayPtr == 1) while(str1index<255) asm("NOP");
        //if (PlayPtr == 2) while(str2index<255) asm("NOP");
	while (FifoCt) ;			/* Wait for audio FIFO empty */
	OCR1A = 128; OCR1B = 128;	/* Return output to center level */
        TCCR1A = 0;
        TCCR1B = 0;
        PlayPtr = 0;
        

	return res;
}

unsigned char saw=0;
/*-----------------------------------------------------------------------*/
/* Main                                                                  */

int main (void)
{
  asm("NOP");
	FRESULT res;
	char *dir;
	//BYTE org_osc = OSCCAL;


	//MCUSR = 0;
	//WDTCR = _BV(WDE) | 0b110;	/* Enable WDT reset in timeout of 1s */

	//PORTB = 0b101001;		/* Initialize port: - - H L H L L P */
	//DDRB  = 0b111110;
        

DDRB |= (1<<3);
PORTB |= (1<<3);
//output 441000 test
DDRD |= (1<<7);

//output PWM
DDRD |= (1<<5);
DDRD |= (1<<4);

DDRA &= ~(1<<3); //PA3 - input putton
PORTA |= (1<<3); //Pullup enable

//OCR2 = 44;

i=0;
j=64;
                
                
//TCCR1A = 0xF1; //fast PWM 8 bit mode
//TCCR1B = 0x01; //F_CLK/1 frequency rate
//OCR1A = 0xC0; //0.5 test
//OCR1B = 0x40; //0.25 test
//TIMSK |= (1<<2); //TOVF1
DDRD = 0xFF;
TCCR2 = 0x0F;
OCR2 = 166; //10ms
TIMSK |= (1<<OCIE2);
//TCCR1B = 0x09; //CTC mode
//TCNT1 = 0;
//OCR1A = 120;
//TIMSK |= (1 << OCIE1A);


    
asm("sei");	
for (;;) {
  if (!(PINA & (1<<3)) && buttonEnable)
  {
    
    asm("NOP");
    if (pf_mount(&Fs) == FR_OK) {	// Initialize FS
      Buff[0] = 0;
      if (!pf_open("osccal")) pf_read(Buff, 1, &rb);	// Adjust frequency 
      //OSCCAL = org_osc + Buff[0];
      
      res = pf_opendir(&Dir, dir = "SINE");	// Open sound file directory 
      if (res == FR_NO_FILE)
        res = pf_opendir(&Dir, dir = "");	// Open root directory 
      
      if (res == FR_OK) {				// Repeat in the dir 
        res = pf_readdir(&Dir, 0);			// Rewind dir
        if (res == FR_OK) {				// Play all wav files in the dir 
          asm("WDR");
          res = pf_readdir(&Dir, &Fno);		// Get a dir entry 
          if (res || !Fno.fname[0]) break;	// Break on error or end of dir
          if (!(Fno.fattrib & (AM_DIR|AM_HID)) && strstr(Fno.fname, ".WAV"))
          {
            if (secondTrack) res = play("/4.WAV");		// Play file 
            else res = play("/3.WAV");	
          }
        }
      }
    }
    
    //Таймер для воспроизведений второго трека
    if (secondTrack == 0) 
    {
      secondTrackTimer = 2000;
      secondTrack = 1;
    }
    //Запрет проигрывания мелодии в течении 20 секунд
    buttonEnable=0;
    buttonEnTimer=1000; //20sec
    
  }
  asm("WDR");
  
}

}



#pragma vector = TIMER1_COMPA_vect 
__interrupt void SoundSampleUpdate()
{
  //if (PIND&(1<<7)) PORTD &= ~(1<<7);
  //else PORTD |= (1<<7);
  
  /*if (saw==255) saw=0;
  else saw++;
  PORTD = sinetable[saw];*/
  
  if (PlayPtr == 1) 
  {
    if (str1play >= ARRAY_SIZE)
    {
      PlayPtr = 2;
      changeStream = 1;
      str2play=0;
    }
    else
    {
      PORTD = Stream1[str1play++];  //High byte
      //str1play++;                   //Low byte
      //OCR2 = Stream1[str1play++];
      //OCR0 = Stream1[str1play++];
    }
  }
  else if (PlayPtr == 2) 
  {
    if (str2play >= ARRAY_SIZE)
    {
      PlayPtr = 1;
      changeStream = 1;
      str1play=0;
    }
    else
    {
      PORTD = Stream2[str2play++];  //High byte
      //str2play++;                   //Low byte
      //OCR2 = Stream2[str2play++];
      //OCR0 = Stream2[str2play++];
    }
  }
  //TIFR |= (1<<4); //OCF1A
}


#pragma vector = TIMER1_COMPB_vect 
__interrupt void SoundSample2()
{

  asm("NOP");
}

#pragma vector = TIMER1_OVF_vect 
__interrupt void SoundSample3()
{

  asm("NOP");
  
}

#pragma vector = TIMER2_COMP_vect 
__interrupt void SoundSample4()
{

  if (buttonEnTimer)
  {
    if (buttonEnTimer--==1) buttonEnable = 1;    
  }
  
  if (secondTrackTimer)
  {
    if (secondTrackTimer-- == 1) secondTrack = 0;
  }
  asm("NOP");
}


void timer1_init()
{
    TCCR1B = 0x09; //CTC mode
    TCNT1 = 0;
    OCR1A = 10000;
    TIMSK |= (1 << OCIE1A);
}

void pwm_init()
{
    //TCCR0|=(1<<WGM00)|(1<<WGM01)|(1<<COM01)|(1<<CS00);
    TCCR2|=(1<<WGM20)|(1<<WGM21)|(1<<COM21)|(1<<CS20);
    //DDRB|=(1<<PB3);
    DDRD|=(1<<PD7);
}

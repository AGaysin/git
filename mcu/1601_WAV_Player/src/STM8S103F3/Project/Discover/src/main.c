
/* RELEASE V.1.0
DATE: 13.04.2015
FILE: STM8S103F3P6_WAV_V1.0.hex
CPU: STM8S103F3P6
DEV: AG, John
PCB: RF-1W ver. 1.0.pcb
VCC: 12V
ICC: 20-30mA

По команде воспроизведение WAV файла с карты памяти


*/


/* Includes ------------------------------------------------------------------*/
#include "stm8s.h"
#include "stm8s_tim1.h"
#include "stm8s_tim2.h"
#include "stm8s_adc1.h"
#include "stm8s_itc.h"
#include "stm8s_tim4.h"
#include "stm8s_exti.h"
#include "stm8s_spi.h"
#include "intrinsics.h" 
#include "stm8s_iwdg.h"


//#include "avr32wav.h"


/* Fat FS   ------------------------------------------------------------------*/
#include "diskio.h"
#include "pff.h"

/* C Library -----------------------------------------------------------------*/
#include <string.h>


/* Private typedef -----------------------------------------------------------*/
/* Private define ------------------------------------------------------------*/
#define MilliSec       1
#define Sec           10
/* Private macro -------------------------------------------------------------*/
/* Private variables ---------------------------------------------------------*/




/* Private function prototypes -----------------------------------------------*/
void CLK_Configuration(void);
void GPIO_Configuration(void);
void TIM1_Configuration(void);
void TIM2_Configuration(void);

void IWDG_Configuration(void);
void delay_ms (unsigned int value);

/* File system object structure */
/*---------------------------------------------------------*/
/* Work Area                                               */
/*---------------------------------------------------------*/

#define F_CPU 16000000
#define FCC(c1,c2,c3,c4)	(((DWORD)c4<<24)+((DWORD)c3<<16)+((WORD)c2<<8)+(BYTE)c1)	/* FourCC */



FATFS Fs;			/* File system object */
DIR Dir;			/* Directory object */
FILINFO Fno;		/* File information */

WORD rb;			/* Return value. Put this here to avoid avr-gcc's bug */

/*---------------------------------------------------------*/

static
DWORD load_header (void)	/* 0:Invalid format, 1:I/O error, >=1024:Number of samples */
{
	DWORD sz, f;
	BYTE b, al = 0;


	if (pf_read(Buff, 12, &rb)) return 1;	/* Load file header (12 bytes) */

	if (rb != 12 || LD_DWORD(Buff+8) != FCC('W','A','V','E')) return 0;

	for (;;) {
		//wdt_reset();
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
                        al=b;
			//GPIOR0 = al = b;	
                   
                   /* Save channel flag */
			b = Buff[14];
			if (b != 8 && b != 16) return 0;		/* Check resolution (8/16 bit) */
			//GPIOR0 |= b;							/* Save resolution flag */
			if (b & 16) al <<= 1;
			f = LD_DWORD(&Buff[4]);					/* Check sampling freqency (8k-48k) */
			if (f < 8000 || f > 48000) return 4;
			//OCR0A = (BYTE)(F_CPU / 8 / f) - 1;		/* Set sampling interval */
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
FRESULT play (
	const char *dir,	/* Directory */
	const char *fn		/* File */
)
{
	DWORD sz;
	FRESULT res;
	BYTE sw;
	WORD btr;


	//wdt_reset();

	//xsprintf((char*)Buff, PSTR("%s/%s"), dir, fn);
	//res = pf_open((char*)Buff);		// Open sound file 
        res = pf_open("WAV/5.WAV");		// Open sound file 
	if (res == FR_OK) {
		sz = load_header();			// Check file format and ready to play //
		if (sz < 1024) return 255;	// Cannot play this file //

		FifoCt = 0; FifoRi = 0; FifoWi = 0;	// Reset audio FIFO //

		/*if (!TCCR1) {				// Enable audio out if not enabled //
			PLLCSR = 0b00000110;	// Select PLL clock for TC1.ck //
			GTCCR =  0b01100000;	// Enable OC1B as PWM //
			TCCR1 = MODE ? 0b01100001 : 0b00000001;	// Start TC1 and enable OC1A as PWM if needed //
			TCCR0A = 0b00000010;	// Statr TC0 as interval timer at 2MHz //
			TCCR0B = 0b00000010;
			TIMSK = _BV(OCIE0A);
			ramp(1);
		}*/

                flag = 1;
		pf_read(Buff, 512 - (Fs.fptr % 512), &rb);	/* Snip sector unaligned part */
		sz -= rb;
		sw = 1;	/* Button status flag */
		do {	/* Data transfer loop */
			//wdt_reset();

                  flag = 1;
			btr = (sz > 1024) ? 1024 : (WORD)sz;/* A chunk of audio data */
			res = pf_read(Buff, btr, &rb);	/* Forward the data into audio FIFO */
			if (rb != 1024) break;		/* Break on error or end of data */
			sz -= rb;					/* Decrease data counter */

			//sw <<= 1;					/* Break on button down */
		} while (1); //(GPIO_ReadInputPin(GPIOB, GPIO_PIN_5) == SET) || ++sw != 1
	}

	while (FifoCt) ;			/* Wait for audio FIFO empty */
	//OCR1A = 128; OCR1B = 128;	/* Return output to center level */

	return res;
}


void main(void)
{

  FRESULT res;
  char *dir;
  
  CLK_Configuration();
  GPIO_Configuration();
  GPIO_WriteLow(GPIOC,GPIO_PIN_4);

  //TIM2_Configuration();
  //TIM1_Configuration();
  //IWDG_Configuration();
  //enableInterrupts();

  unsigned char fname[12];
  unsigned int cluster;
  char NEXT_OR_PREVIOUS = 1;
  
  

  while(1)
  {
    if (pf_mount(&Fs) == FR_OK)
    {
      //fat16_init();
      GPIO_WriteHigh(GPIOC,GPIO_PIN_4);
      
      
      //Mega32 project
      
     
      //Chan project
      res = pf_opendir(&Dir, dir = "WAV");	// Open sound file directory 
			if (res == FR_NO_FILE)
				res = pf_opendir(&Dir, dir = "");	// Open root directory 

			while (res == FR_OK) {				// Repeat in the dir 
				res = pf_readdir(&Dir, 0);			// Rewind dir 
				while (res == FR_OK) {				// Play all wav files in the dir 
					//wdt_reset();
					res = pf_readdir(&Dir, &Fno);		// Get a dir entry 
					if (res || !Fno.fname[0]) break;	// Break on error or end of dir 
					if (!(Fno.fattrib & (AM_DIR|AM_HID)) && strstr(Fno.fname, ".WAV"))
                                        {
                                          asm("NOP");
                                          res = play(dir, Fno.fname);		// Play file 
                                          asm("NOP");
                                        }
				}
			}
    }
    else GPIO_WriteLow(GPIOC,GPIO_PIN_4);
    delay_ms(500);
  }
  
  }




void CLK_Configuration(void){

  /* Fmaster = 16MHz */
  CLK_HSIPrescalerConfig(CLK_PRESCALER_HSIDIV1);

}

void GPIO_Configuration(void){
  /* GPIOD reset */
  GPIO_DeInit(GPIOD);
  GPIO_DeInit(GPIOA);
  GPIO_DeInit(GPIOB);
GPIO_DeInit(GPIOC);
  
  //PD3 (pin20) - TIM2-CH2 - PWM WAV OUTPUT
  GPIO_Init(GPIOD, GPIO_PIN_3, GPIO_MODE_OUT_PP_HIGH_FAST);
  
   
  //PC4(pin16) - LED TEST out OD
  GPIO_Init(GPIOC, GPIO_PIN_4, GPIO_MODE_OUT_PP_HIGH_FAST);
  
  //SD CS - active low, PC3 (pin13)
  GPIO_Init(GPIOC, GPIO_PIN_3, GPIO_MODE_OUT_PP_HIGH_FAST);
  
  
  //SD MISO
  GPIO_Init(GPIOC, GPIO_PIN_7, GPIO_MODE_IN_PU_NO_IT);
  //SD MOSI
  GPIO_Init(GPIOC, GPIO_PIN_6, GPIO_MODE_OUT_PP_HIGH_FAST);
  //SD SCK
  GPIO_Init(GPIOC, GPIO_PIN_5, GPIO_MODE_OUT_PP_HIGH_FAST);
  
  
}

void TIM1_Configuration(void){
  /* TIM1 Peripheral Configuration */ 
  TIM1_DeInit();

  //44100 kHz
  TIM1_TimeBaseInit(0, TIM1_COUNTERMODE_UP, 373,0); //23us
 
  /* Clear TIM4 update flag */
  TIM1_ClearFlag(TIM1_FLAG_UPDATE);
  /* Enable update interrupt */
  TIM1_ITConfig(TIM1_IT_UPDATE, ENABLE);
  
  /* TIM1 counter enable */
  TIM1_Cmd(ENABLE);
}

void TIM2_Configuration(void){
  TIM2_DeInit();
  TIM2_TimeBaseInit(TIM2_PRESCALER_1, 373);
  TIM2_OC2Init(TIM2_OCMODE_PWM2, TIM2_OUTPUTSTATE_ENABLE,0, TIM2_OCPOLARITY_LOW);
  //TIM2_ARRPreloadConfig(ENABLE);
  TIM2_OC2PreloadConfig(ENABLE);
  
  //if (uiRfidOutFrequency<100)uiRfidOutFrequency++;
  //else TIM2_DeInit();
  /* Clear TIM2 update flag */
  TIM2_ClearFlag(TIM2_FLAG_UPDATE);
  /* Enable update interrupt */
  TIM2_ITConfig(TIM2_IT_UPDATE, ENABLE);
    TIM2_Cmd(ENABLE);
}

void IWDG_Configuration(void){
  IWDG_Enable();
  IWDG_WriteAccessCmd(IWDG_WriteAccess_Enable);
  IWDG_SetPrescaler(IWDG_Prescaler_256);
  IWDG_SetReload(0xFF);
  
  IWDG_WriteAccessCmd(IWDG_WriteAccess_Disable);
}




/****************** INTERRUPTS ******************************/

#pragma vector=7 //особенность IAR - номер_вектора = номер_по_документации_ST + 2 //PC4 EXTI2 PORTC Vector=5;
__interrupt void EMMarineDataReceive(void){
}

#pragma vector=13 //особенность IAR - номер_вектора = номер_по_документации_ST + 2
__interrupt void TIM1_Update(void){
  
  /* Cleat Interrupt Pending bit */
  TIM1_ClearITPendingBit(TIM1_IT_UPDATE);
}

#pragma vector=14 //Timer2 Copmare2
__interrupt void TIM2_CC2(void){

  //TIM2_OC2Init(TIM2_OCMODE_PWM2, TIM2_OUTPUTSTATE_ENABLE, i, TIM2_OCPOLARITY_LOW);
  
  asm("NOP");
  /* Cleat Interrupt Pending bit */
  TIM2_ClearITPendingBit(TIM2_IT_CC2);
}

#pragma vector=15 //Timer2 Update/overflow
__interrupt void TIM2_UPDATE(void){
  GPIO_WriteReverse(GPIOC, GPIO_PIN_6);
  
  //TIM2_SetCompare2(sinetable[i]);
  //TIM2_SetAutoreload(i);
  asm("NOP");
  
 
  /* Cleat Interrupt Pending bit */
  TIM2_ClearITPendingBit(TIM2_IT_UPDATE);
}

#pragma vector = 24 //INTERRUPT_HANDLER(ADC1_IRQHandler, 22) ITC_IRQ_ADC1
__interrupt void ADC1_ConversationComplete(void){
  ADC1_ITConfig(ADC1_IT_EOCIE, DISABLE);
}

#pragma vector=25 //особенность IAR - номер_вектора = номер_по_документации_ST + 2
__interrupt void TIM4_ReceiveDataTimer(void){
  //Интервал времени, соотвествующий дискретезации 44100 Гц
  
  //Задаем ШИМ
  
  
  /* Cleat Interrupt Pending bit */
  TIM4_ClearITPendingBit(TIM4_IT_UPDATE);
}

#ifdef USE_FULL_ASSERT

/**
  * @brief  Reports the name of the source file and the source line number
  *   where the assert_param error has occurred.
  * @param file: pointer to the source file name
  * @param line: assert_param error line source number
  * @retval None
  */
void assert_failed(u8* file, u32 line)
{ 
  /* User can add his own implementation to report the file name and line number,
     ex: printf("Wrong parameters value: file %s on line %d\r\n", file, line) */

  /* Infinite loop */
  while (1)
  {
  }
}
#endif




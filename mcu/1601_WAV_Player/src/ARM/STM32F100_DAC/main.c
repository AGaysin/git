/** READ ME
  ******************************************************************************
  * @file    
  * @author  AG
  * @version V1.0.0
  * @date    30-July-2014
  * @brief   Main program body
  ******************************************************************************
SHIM CONTROLLER 80kHz - 2 phases
output is 160kHz 1 phase, time = 6.25us, 0..450 - setting values

TIM3: channel 3, and 4 used for PWM

  ******************************************************************************
  */ 

/* Includes ------------------------------------------------------------------*/
#include "stm32f10x.h"
#include "stm32f10x_it.h"
#include "ctype.h"
#include "pff.h"
#include "diskio.h"
#include "integer.h"

    
    
#define FCC(c1,c2,c3,c4)	(((DWORD)c4<<24)+((DWORD)c3<<16)+((WORD)c2<<8)+(BYTE)c1)	/* FourCC */

unsigned char secondTrack=0;
unsigned int secondTrackTimer=0;
unsigned char buttonEnable=1;
unsigned int buttonEnTimer=0;
unsigned int isNowPlaying=0;
unsigned char isSirenTrack=0;
uint8_t buttonOldPosition;
unsigned int CheckReadyCounter;
unsigned char isCardReady=0;
volatile BYTE FifoRi, FifoWi, FifoCt;	/* FIFO controls */

BYTE Buff[64];		/* Wave output FIFO */

FATFS Fs;			/* File system object */
DIR Dir;			/* Directory object */
FILINFO Fno;		/* File information */

WORD rb;			/* Return value. Put this here to avoid avr-gcc's bug */



/* Private functions ---------------------------------------------------------*/
TIM_TimeBaseInitTypeDef  TIM_TimeBaseStructure;
TIM_OCInitTypeDef  TIM_OCInitStructure;

/* Private function prototypes -----------------------------------------------*/
void RCC_Configuration(void);
void GPIO_Configuration(void);
void NVIC_Configuration(void);
void TIM2_Configuration(WORD interval);
void SPI_Configuration(void);

unsigned int i=0;
unsigned int j=1000;


#define PLAY_BTN        GPIO_ReadInputDataBit(GPIOA, GPIO_Pin_8)
#define TEST_BTN        GPIO_ReadInputDataBit(GPIOB, GPIO_Pin_14)


#define LED_SIRENA_ON    GPIO_ResetBits(GPIOB, GPIO_Pin_1)
#define LED_SIRENA_OFF   GPIO_SetBits(GPIOB, GPIO_Pin_1)
#define LED_PLAY_ON    GPIO_ResetBits(GPIOB, GPIO_Pin_2)        
#define LED_PLAY_OFF   GPIO_SetBits(GPIOB, GPIO_Pin_2)
#define LED_READY_ON     GPIO_ResetBits(GPIOB, GPIO_Pin_10)
#define LED_READY_OFF    GPIO_SetBits(GPIOB, GPIO_Pin_10)
#define LED_POWER_ON   GPIO_ResetBits(GPIOB, GPIO_Pin_11)
#define LED_POWER_OFF  GPIO_SetBits(GPIOB, GPIO_Pin_11)

#define SIRENA_OUT_ON   GPIO_SetBits(GPIOB, GPIO_Pin_15);LED_SIRENA_ON
#define SIRENA_OUT_OFF  GPIO_ResetBits(GPIOB, GPIO_Pin_15);LED_SIRENA_OFF


#define V1_ON           GPIO_SetBits(GPIOA, GPIO_Pin_9)
#define V2_ON           GPIO_SetBits(GPIOA, GPIO_Pin_10)
#define V3_ON           GPIO_SetBits(GPIOA, GPIO_Pin_11)
#define V4_ON           GPIO_SetBits(GPIOA, GPIO_Pin_12)

#define V1_OFF          GPIO_ResetBits(GPIOA, GPIO_Pin_9)
#define V2_OFF          GPIO_ResetBits(GPIOA, GPIO_Pin_10)
#define V3_OFF          GPIO_ResetBits(GPIOA, GPIO_Pin_11)
#define V4_OFF          GPIO_ResetBits(GPIOA, GPIO_Pin_12)

              //GPIO_ResetBits(GPIOB, GPIO_Pin_8);
              //GPIO_SetBits(GPIOB, GPIO_Pin_9);
              //GPIO_SetBits(GPIOC, GPIO_Pin_14);

#define VIDEO_PLAY_1    V1_OFF;V2_ON;V3_ON;V4_ON;  	//1
#define VIDEO_PLAY_2    V1_ON;V2_OFF;V3_ON;V4_ON; 	//2
#define VIDEO_SIRENA    V1_OFF;V2_OFF;V3_ON;V4_ON;	//3
#define VIDEO_NEXT_2    V1_ON;V2_ON;V3_OFF;V4_ON;	//4
#define VIDEO_NEXT_S    V1_OFF;V2_ON;V3_OFF;V4_ON;	//5
#define VIDEO_RELAY     V1_ON;V2_OFF;V3_OFF;V4_ON;	//6
#define VIDEO_CLR       V1_OFF;V2_OFF;V3_OFF;V4_ON;	//7


#define VIDEO_NEXT_S1   V1_OFF;V2_ON;V3_ON;V4_OFF;	//9
#define VIDEO_NEXT_S2   V1_ON;V2_OFF;V3_ON;V4_OFF;	//10
#define VIDEO_NOSD      V1_OFF;V2_OFF;V3_ON;V4_OFF;	//11

#define VIDEO_PLAY_S1   V1_ON;V2_ON;V3_OFF;V4_OFF;	//12
#define VIDEO_PLAY_S2   V1_OFF;V2_ON;V3_OFF;V4_OFF;	//13

#define MMC_POWER_ON    GPIO_ResetBits(GPIOC, GPIO_Pin_14)
#define MMC_POWER_OFF   GPIO_SetBits(GPIOC, GPIO_Pin_14)



#define SOUND_ENABLE GPIO_ResetBits(GPIOB, GPIO_Pin_12);
#define SOUND_DISABLE GPIO_SetBits(GPIOB, GPIO_Pin_12);

/* Private functions ---------------------------------------------------------*/
/*---------------------------------------------------------*/

static
DWORD load_header (void)	/* 0:Invalid format, 1:I/O error, >=1024:Number of samples */
{
	DWORD sz, f;
	BYTE b, al = 0;


	if (pf_read(Buff, 12, &rb)) return 1;	/* Load file header (12 bytes) */

	if (rb != 12 || LD_DWORD(Buff+8) != FCC('W','A','V','E')) return 0;

	for (;;) {
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
                        /* Set sampling interval */
                        
                        //OCR1A = (BYTE)(16000000/ 8 / f) - 1;	
                        //TIMSK |= (1 << OCIE1A);
                        TIM2->PSC = 0; // Настраиваем делитель что таймер тикал 100000 раз в секунду
  TIM2->ARR = (unsigned int ) (96000000 / f / al) ; // Чтоб прерывание случалось раз в 10 ms
  TIM2->DIER |= TIM_DIER_UIE; //разрешаем прерывание от таймера
  TIM2->CR1 |= TIM_CR1_CEN; // Начать отсчёт!
  NVIC_EnableIRQ(TIM2_IRQn); //Разрешение TIM6_DAC_IRQn прерывания
  
  
  
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
          //GPIO_ResetBits(GPIOB, GPIO_Pin_14);
		sz = load_header();			/* Check file format and ready to play */
		if (sz < 1024) return 255;	/* Cannot play this file */

		FifoCt = 0; FifoRi = 0; FifoWi = 0;	/* Reset audio FIFO */


                
                
                str1play = 0;
                str2play = 0;
                PlayPtr = 0;


                SOUND_ENABLE;
                LED_PLAY_ON;
		pf_read(0, 512 - (Fs.fptr % 512), &rb);	/* Snip sector unaligned part */
		sz -= rb;
		sw = 1;	/* Button status flag */
		do {	/* Data transfer loop */

			btr = (sz > 1024) ? 1024 : (WORD)sz;/* A chunk of audio data */
			res = pf_read(0, btr, &rb);	/* Forward the data into audio FIFO */
			if (rb != 1024) break;		/* Break on error or end of data */
			sz -= rb;					/* Decrease data counter */

			sw <<= 1;					/* Break on button down */
		} while (++sw != 1);
                SOUND_DISABLE;
                LED_PLAY_OFF;
	}

        //if (PlayPtr == 1) while(str1index<255) asm("NOP");
        //if (PlayPtr == 2) while(str2index<255) asm("NOP");
	while (FifoCt) ;			/* Wait for audio FIFO empty */
        //stop timer TIM2;
        TIM_DeInit(TIM2);
    
        //установить уровень звука в середину 2048d
        DAC->DHR12R1=2048;
        PlayPtr = 0;
        

	return res;
}


/* Work Area                                               */
/*---------------------------------------------------------*/

unsigned char NoFiles=0;






int main(void)
{
  asm("NOP");
  FRESULT res;
  char *dir;
  unsigned int delay_int;
  RCC_Configuration();
  GPIO_Configuration();
  NVIC_Configuration();
  SPI_Configuration();

  MMC_POWER_OFF;
  //Stop Siren
  SIRENA_OUT_OFF;
  //DAC INIT AND TIMER6
  TIM3->PSC = 2500; // Настраиваем делитель что таймер тикал 100000 раз в секунду
  TIM3->ARR = 1000; // Чтоб прерывание случалось раз в 10 ms
  TIM3->DIER |= TIM_DIER_UIE; //разрешаем прерывание от таймера
  TIM3->CR1 |= TIM_CR1_CEN; // Начать отсчёт!
  NVIC_EnableIRQ(TIM3_IRQn); //Разрешение TIM6_DAC_IRQn прерывания
  
  /* Включить DAC2 */
  DAC->CR |= DAC_CR_EN1;
  DAC->CR &= ~(DAC_CR_BOFF1);
  //DAC->CR |= DAC_CR_BOFF1;
  
  unsigned int dac_start=7;
  while(dac_start--)
  {
    if (dac_start%2) LED_POWER_OFF;
    else LED_POWER_ON;
    delay_ms(400);
  }
  dac_start=0;
  
  while(dac_start<0x800)
  {
    DAC->DHR12R1  = dac_start++;
    if ((dac_start/250)%2) LED_POWER_OFF;
    else LED_POWER_ON; //GPIO_ResetBits(GPIOB, GPIO_Pin_2);
    delay_int = 0xFF;
    while (delay_int--)
      ;
  }
  MMC_POWER_ON;
  
  //green on
  SOUND_DISABLE;
  LED_POWER_ON;
  buttonOldPosition = PLAY_BTN;
              //LED_READY_ON;
  CheckReadyCounter=0;
              LED_PLAY_OFF;
              LED_SIRENA_OFF;
for (;;) {
  

  if (CheckReadyCounter<10000) CheckReadyCounter++;
  else 
  {
    CheckReadyCounter=0;
    if (pf_mount(&Fs) == FR_OK)
    {
      if (isCardReady==0)  
      {
        secondTrack = 0;
        VIDEO_CLR;
      }
      isCardReady = 1;
      LED_READY_ON;
      
    }
    else
    {
      isCardReady = 0;
      LED_READY_OFF;
      VIDEO_NOSD;
      
    }
  }

  if (TEST_BTN==Bit_RESET)
  {
    
    if (pf_mount(&Fs) == FR_OK) {	// Initialize FS
      Buff[0] = 0;
      
      //if (!pf_open("osccal")) pf_read(Buff, 1, &rb);	// Adjust frequency 
      //OSCCAL = org_osc + Buff[0];
      res = pf_opendir(&Dir, dir = "SINE");	// Open sound file directory 
      if (res == FR_NO_FILE)
        res = pf_opendir(&Dir, dir = "");	// Open root directory 
      
      if (res == FR_OK) {				// Repeat in the dir 
        res = pf_readdir(&Dir, 0);			// Rewind dir
        if (res == FR_OK) {				// Play all wav files in the dir 

          res = pf_readdir(&Dir, &Fno);		// Get a dir entry 
          if (res || !Fno.fname[0]) break;	// Break on error or end of dir
          
          
          VIDEO_PLAY_1;
          res = play("ALARM/1.WAV");
          VIDEO_RELAY;
          delay_ms(10000);
          VIDEO_NEXT_2;
          delay_ms(10000);
          VIDEO_PLAY_2;
          res = play("ALARM/2.WAV");
          VIDEO_RELAY;
          delay_ms(10000);
          VIDEO_NEXT_S1;
          delay_ms(10000);
          VIDEO_PLAY_S1;
          res = play("ALARM/3.WAV");
          VIDEO_RELAY;
          delay_ms(10000);
          VIDEO_NEXT_S2;
          delay_ms(10000);
          VIDEO_PLAY_S2;
          SIRENA_OUT_ON;
          delay_ms(10000);
          SIRENA_OUT_OFF;
          VIDEO_NEXT_S;
          delay_ms(10000);
          VIDEO_SIRENA;
          SIRENA_OUT_ON;
          delay_ms(10000);
          SIRENA_OUT_OFF;
          VIDEO_CLR;
        }
      }
      
    }
    
  }
  
  //Пока срабатывает только по нулю
  if (buttonEnable && PLAY_BTN==Bit_RESET)
  {
    delay_ms(250);
    if (PLAY_BTN==Bit_RESET && PLAY_BTN==Bit_RESET)
    {
    
    asm("NOP");
    if (pf_mount(&Fs) == FR_OK) {	// Initialize FS
      Buff[0] = 0;
      
      //if (!pf_open("osccal")) pf_read(Buff, 1, &rb);	// Adjust frequency 
      //OSCCAL = org_osc + Buff[0];
      res = pf_opendir(&Dir, dir = "SINE");	// Open sound file directory 
      if (res == FR_NO_FILE)
        res = pf_opendir(&Dir, dir = "");	// Open root directory 
      
      if (res == FR_OK) {				// Repeat in the dir 
        res = pf_readdir(&Dir, 0);			// Rewind dir
        if (res == FR_OK) {				// Play all wav files in the dir 

          res = pf_readdir(&Dir, &Fno);		// Get a dir entry 
          if (res || !Fno.fname[0]) break;	// Break on error or end of dir
          if (1)//!(Fno.fattrib & (AM_DIR|AM_HID)) && strstr(Fno.fname, ".WAV"))
          {
            asm("NOP");
            isNowPlaying = 1;
            if (secondTrack == 0) 
            {
              //Track1
              VIDEO_PLAY_1;
              res = play("ALARM/1.WAV");
              //show RELAY untill RELAY_ON
              VIDEO_RELAY;
              while(PLAY_BTN==Bit_RESET) asm("NOP");
              //Next Play2
              VIDEO_NEXT_2;
              secondTrack = 1;
            }
            else if (secondTrack == 1) 
            {
              //Track2
              VIDEO_PLAY_2;
              res = play("ALARM/2.WAV");
              //show RELAY untill RELAY_ON
              VIDEO_RELAY;
              while(PLAY_BTN==Bit_RESET) asm("NOP");
              
              res = pf_open("ALARM/3.WAV");
              if (res == FR_OK)
              {
                //выводить NEXT_S1
                VIDEO_NEXT_S1;
                secondTrack = 2;
              }
              else
              {
                //выводить NEXT_S
                VIDEO_NEXT_S;
                secondTrack = 4;
              }
            }
            else if (secondTrack == 2) 
            {
              VIDEO_PLAY_S1;
              res = play("ALARM/3.WAV");
              //show RELAY untill RELAY_ON
              VIDEO_RELAY;
              while(PLAY_BTN==Bit_RESET) asm("NOP");
              //выводить NEXT_S2
                VIDEO_NEXT_S2;
              secondTrack = 3;
            }
            else if (secondTrack == 3)
            {
              VIDEO_PLAY_S2;
              SIRENA_OUT_ON; 
              while(PLAY_BTN==Bit_RESET) asm("NOP");
              SIRENA_OUT_OFF; 
              VIDEO_NEXT_S2;
            }
            else
            {
              //Sirena
              VIDEO_SIRENA;
              SIRENA_OUT_ON; 
              while(PLAY_BTN==Bit_RESET) asm("NOP");
              SIRENA_OUT_OFF; 
              VIDEO_NEXT_S;
            }
            secondTrackTimer = 6000;
          }
          asm("NOP");
        }
      }
      
    }
    
    if (isCardReady == 0)
    {
      VIDEO_SIRENA;
      SIRENA_OUT_ON; //GPIO_SetBits(GPIOB, GPIO_Pin_14);
      while(PLAY_BTN==Bit_RESET) asm("NOP");
      SIRENA_OUT_OFF;
      VIDEO_CLR;
      
   }
    
    
    //relay
    isNowPlaying = 0;
    buttonEnable=0;
    buttonEnTimer=1200; //1 min timeout
    buttonEnable = 1;  
    
    }

  }
  asm("NOP");
  
}
  
  
  
}
  


void RCC_Configuration(void){
  
//1. Включить HSE и подождать, пока он стабилизируется:
  //Turn ON HSE
  RCC->CR|=RCC_CR_HSEON;
  //Wait until it's stable 
  while (!(RCC->CR & RCC_CR_HSERDY));
  //PLL от HSE
  RCC->CFGR |= RCC_CFGR_MCO_HSE;
//2. Установить множитель PLL равным четырем, а делитель — двум, что даст результирующее умножение на два:
  //PLL input = HSI
  //PLL division factor = 2
  //PLL multiplication factor = 4
  RCC->CFGR |= RCC_CFGR_PLLMULL2;
//3. Включить PLL и подождать, пока она стабилизируется:
  //Turn PLL on
  RCC->CR|=RCC_CR_PLLON;
  //Wait PLL to stabilize
  while (!(RCC->CR & RCC_CR_PLLRDY));
//4. Перейти на тактирование от PLL:
  //Set PLL as SYSCLK
  RCC->CFGR|=RCC_CFGR_SW_1 | RCC_CFGR_SW_0;
  //Turn off MSI
  //RCC->CR &=~ RCC_CR_MSION;  
  
  
  //RCC->CR|=RCC_CR_HSEON; // Включить генератор HSE.
   //while (!(RCC->CR & RCC_CR_HSERDY)) {}; // Ожидание готовности HSE.
   //RCC->CFGR &=~RCC_CFGR_SW; // Очистить биты SW0, SW1.
   //RCC->CFGR |= RCC_CFGR_SW_HSE; // Выбрать HSE для тактирования SW0=1.
  
  /* PCLK1 = HCLK/4 */
  //RCC_PCLK1Config(RCC_HCLK_Div4);

  
  /*// 1. Clocking the controller from internal HSI RC (8 MHz)
  RCC_HSICmd(ENABLE);
  // wait until the HSI is ready
  while(RCC_GetFlagStatus(RCC_FLAG_HSIRDY) == RESET);
  RCC_SYSCLKConfig(RCC_SYSCLKSource_HSI);
  // 2. Enable ext. high frequency OSC
  RCC_HSEConfig(RCC_HSE_ON);
  // wait until the HSE is ready
  */
  // TIM6 clock enable  //10ms Timer
  RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM2, ENABLE); 
  RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM3, ENABLE); 
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_SPI1, ENABLE);
  
  /* GPIOC clock enable */
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOC, ENABLE);
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOB, ENABLE);
  RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA, ENABLE);
  
  
  /* Включаем ЦАП */
  RCC_APB1PeriphClockCmd(RCC_APB1Periph_DAC, ENABLE);
  /* Включаем таймер 6 */
  //RCC_APB1PeriphClockCmd(RCC_APB1Periph_TIM6,ENABLE);
}

void GPIO_Configuration(void){
  GPIO_InitTypeDef GPIO_InitStructure;
  
  /*GPIOA Configuration: USART2 RX/TX - DS18S20*/
  
  
  /*GPIOB Configuration: TIM3 channel3 and 4 */
  GPIO_InitStructure.GPIO_Pin =  GPIO_Pin_0 | GPIO_Pin_1 | GPIO_Pin_2 | GPIO_Pin_10 | GPIO_Pin_11 | GPIO_Pin_12 | GPIO_Pin_15;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(GPIOB, &GPIO_InitStructure);
  
  
  //VIDEO
  GPIO_InitStructure.GPIO_Pin =  GPIO_Pin_9 | GPIO_Pin_10 | GPIO_Pin_11 | GPIO_Pin_12;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(GPIOA, &GPIO_InitStructure);
  
  
  
  
  //button play
  GPIO_InitStructure.GPIO_Pin =  GPIO_Pin_8;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(GPIOA, &GPIO_InitStructure);
  
  //test btn
  GPIO_InitStructure.GPIO_Pin =  GPIO_Pin_14;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(GPIOB, &GPIO_InitStructure);
  
  //PA4 - DAC
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_4;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AIN;
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(GPIOA, &GPIO_InitStructure);
  
  // SPI1 SCK, MOSI
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_5 | GPIO_Pin_7;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_AF_PP;
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(GPIOA, &GPIO_InitStructure);
  // SPI MISO
  GPIO_InitStructure.GPIO_Pin = GPIO_Pin_6;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_IPU;
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(GPIOA, &GPIO_InitStructure);

//MMC POWER
  MMC_POWER_OFF;
  GPIO_InitStructure.GPIO_Pin =  GPIO_Pin_14;
  GPIO_InitStructure.GPIO_Mode = GPIO_Mode_Out_PP;
  GPIO_InitStructure.GPIO_Speed = GPIO_Speed_50MHz;
  GPIO_Init(GPIOC, &GPIO_InitStructure);

}


void SPI_Configuration(void) {
  
  SPI_InitTypeDef spi;
  SPI_StructInit(&spi);
  spi.SPI_Direction = SPI_Direction_2Lines_FullDuplex;
  spi.SPI_Mode = SPI_Mode_Master;
  spi.SPI_DataSize = SPI_DataSize_8b;
  spi.SPI_CPOL = SPI_CPOL_Low;
  spi.SPI_CPHA = SPI_CPHA_1Edge; //SPI_CPHA_2Edge
  spi.SPI_NSS = SPI_NSS_Soft;
  spi.SPI_BaudRatePrescaler = SPI_BaudRatePrescaler_2;
  spi.SPI_FirstBit = SPI_FirstBit_MSB;
  spi.SPI_CRCPolynomial = 7;
  SPI_Init(SPI1, &spi);
  SPI_Cmd(SPI1, ENABLE);
}


void NVIC_Configuration(void){
  NVIC_InitTypeDef NVIC_InitStructure;

  /* Enable the TIM2 global Interrupt */
  NVIC_InitStructure.NVIC_IRQChannel = TIM3_IRQn;
  NVIC_InitStructure.NVIC_IRQChannelPreemptionPriority = 0;
  NVIC_InitStructure.NVIC_IRQChannelSubPriority = 1;
  NVIC_InitStructure.NVIC_IRQChannelCmd = ENABLE;

  NVIC_Init(&NVIC_InitStructure);
}


#ifdef  USE_FULL_ASSERT




/**
  * @brief  Reports the name of the source file and the source line number
  *         where the assert_param error has occurred.
  * @param  file: pointer to the source file name
  * @param  line: assert_param error line source number
  * @retval None
  */
void assert_failed(uint8_t* file, uint32_t line)
{
  /* User can add his own implementation to report the file name and line number,
     ex: printf("Wrong parameters value: file %s on line %d\r\n", file, line) */

  while (1)
  {}
}

#endif

void TIM2_IRQHandler(void){
  TIM2->SR &= ~TIM_SR_UIF; //Сбрасываем флаг UIF
  GPIO_SetBits(GPIOB, GPIO_Pin_13);
  if (PlayPtr & 1) 
  {
    if (str1play >= ARRAY_SIZE)
    {
      PlayPtr = 2;
      changeStream = 1;
      str2play=0;
    }
    else
    {
      DAC->DHR12R1 = Stream1[str1play++]; 
    }
  }
  else if (PlayPtr & 2) 
  {
    if (str2play >= ARRAY_SIZE)
    {
      PlayPtr = 1;
      changeStream = 1;
      str1play=0;
    }
    else
    {
      DAC->DHR12R1 = Stream2[str2play++]; 
    }
  }
  GPIO_ResetBits(GPIOB, GPIO_Pin_13);
}


void TIM3_IRQHandler(void){
  //50ms intervals
  TIM3->SR &= ~TIM_SR_UIF; //Сбрасываем флаг UIF
  

  if (isNowPlaying)
  {
    
  }
  else
  {
  if (secondTrackTimer)
  {
    if (secondTrackTimer-- == 1) 
    {
      VIDEO_CLR;
      secondTrack = 0;
    }
  }
  }

  asm("NOP");
  
}
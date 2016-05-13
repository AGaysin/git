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
unsigned char cntPlayList=0;
uint8_t buttonOldPosition;
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
void testButtonCheck (void);
void cardReadyCheck (void);
void playButtonCheck(void);
unsigned int i=0;
unsigned int j=1000;


#define PLAY_BTN        GPIO_ReadInputDataBit(GPIOA, GPIO_Pin_8)
#define TEST_BTN        GPIO_ReadInputDataBit(GPIOB, GPIO_Pin_14)


#define LED_TEST_ON    GPIO_ResetBits(GPIOB, GPIO_Pin_1)
#define LED_TEST_OFF   GPIO_SetBits(GPIOB, GPIO_Pin_1)
#define LED_PLAY_ON    GPIO_ResetBits(GPIOB, GPIO_Pin_2)        
#define LED_PLAY_OFF   GPIO_SetBits(GPIOB, GPIO_Pin_2)
#define LED_READY_ON     GPIO_ResetBits(GPIOB, GPIO_Pin_10)
#define LED_READY_OFF    GPIO_SetBits(GPIOB, GPIO_Pin_10)
#define LED_READY_INVERT if(GPIO_ReadInputDataBit(GPIOB, GPIO_Pin_10)==Bit_SET) GPIO_ResetBits(GPIOB, GPIO_Pin_10); else GPIO_SetBits(GPIOB, GPIO_Pin_10);
#define LED_POWER_ON   GPIO_ResetBits(GPIOB, GPIO_Pin_11)
#define LED_POWER_OFF  GPIO_SetBits(GPIOB, GPIO_Pin_11)

#define SIRENA_OUT_ON   GPIO_SetBits(GPIOB, GPIO_Pin_15)
#define SIRENA_OUT_OFF  GPIO_ResetBits(GPIOB, GPIO_Pin_15)


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

#define MMC_POWER_ON    GPIO_ResetBits(GPIOC, GPIO_Pin_14)
#define MMC_POWER_OFF   GPIO_SetBits(GPIOC, GPIO_Pin_14)



#define SOUND_ENABLE GPIO_ResetBits(GPIOB, GPIO_Pin_12);
#define SOUND_DISABLE GPIO_SetBits(GPIOB, GPIO_Pin_12);


#define PLAY_STATE Bit_SET 
#define STOP_STATE Bit_RESET
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
                        TIM2->PSC = 0; // ����������� �������� ��� ������ ����� 100000 ��� � �������
  TIM2->ARR = (unsigned int ) (96000000 / f / al) ; // ���� ���������� ��������� ��� � 10 ms
  TIM2->DIER |= TIM_DIER_UIE; //��������� ���������� �� �������
  TIM2->CR1 |= TIM_CR1_CEN; // ������ ������!
  NVIC_EnableIRQ(TIM2_IRQn); //���������� TIM6_DAC_IRQn ����������
  
  
  
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
	}

        //if (PlayPtr == 1) while(str1index<255) asm("NOP");
        //if (PlayPtr == 2) while(str2index<255) asm("NOP");
	while (FifoCt) ;			/* Wait for audio FIFO empty */
        //stop timer TIM2;
        TIM_DeInit(TIM2);
    
        //���������� ������� ����� � �������� 2048d
        DAC->DHR12R1=2048;
        PlayPtr = 0;
        

	return res;
}


/* Work Area                                               */
/*---------------------------------------------------------*/

unsigned char NoFiles=0;


unsigned long ulInitCounter=0;  
unsigned char ucCardResetCheck=0;
unsigned char isCardReady=0;
unsigned char maxPlayList=0;

FRESULT res;
  char *dir;
  
int main(void)
{
  asm("NOP");
  
  unsigned int delay_int;
  RCC_Configuration();
  GPIO_Configuration();
  NVIC_Configuration();
  SPI_Configuration();

  

  
  MMC_POWER_OFF;
  //Stop Siren
  SIRENA_OUT_ON;
  //DAC INIT AND TIMER6
  TIM3->PSC = 2500; // ����������� �������� ��� ������ ����� 100000 ��� � �������
  TIM3->ARR = 1000; // ���� ���������� ��������� ��� � 10 ms
  TIM3->DIER |= TIM_DIER_UIE; //��������� ���������� �� �������
  TIM3->CR1 |= TIM_CR1_CEN; // ������ ������!
  NVIC_EnableIRQ(TIM3_IRQn); //���������� TIM6_DAC_IRQn ����������
  
  /* �������� DAC2 */
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
  LED_READY_OFF;
  LED_PLAY_OFF;
  LED_TEST_OFF;
  
  
  
  while(PLAY_BTN==PLAY_STATE || isCardReady==0)
  {
    
    cardReadyCheck();
    if (isCardReady) 
    {
      testButtonCheck();
    }
    delay_ms(100);
  }
       

for (;;) {

  if (++ulInitCounter==0xFFFF) 
  {
    ulInitCounter=0;
    cardReadyCheck();
  }
    
  if (isCardReady)
  {
    testButtonCheck();
    playButtonCheck();
    asm("NOP");
    
  }
  
}
  
}
  


void RCC_Configuration(void){
  
//1. �������� HSE � ���������, ���� �� ���������������:
  //Turn ON HSE
  RCC->CR|=RCC_CR_HSEON;
  //Wait until it's stable 
  while (!(RCC->CR & RCC_CR_HSERDY));
  //PLL �� HSE
  RCC->CFGR |= RCC_CFGR_MCO_HSE;
//2. ���������� ��������� PLL ������ �������, � �������� � ����, ��� ���� �������������� ��������� �� ���:
  //PLL input = HSI
  //PLL division factor = 2
  //PLL multiplication factor = 4
  RCC->CFGR |= RCC_CFGR_PLLMULL2;
//3. �������� PLL � ���������, ���� ��� ���������������:
  //Turn PLL on
  RCC->CR|=RCC_CR_PLLON;
  //Wait PLL to stabilize
  while (!(RCC->CR & RCC_CR_PLLRDY));
//4. ������� �� ������������ �� PLL:
  //Set PLL as SYSCLK
  RCC->CFGR|=RCC_CFGR_SW_1 | RCC_CFGR_SW_0;
  //Turn off MSI
  //RCC->CR &=~ RCC_CR_MSION;  
  
  
  //RCC->CR|=RCC_CR_HSEON; // �������� ��������� HSE.
   //while (!(RCC->CR & RCC_CR_HSERDY)) {}; // �������� ���������� HSE.
   //RCC->CFGR &=~RCC_CFGR_SW; // �������� ���� SW0, SW1.
   //RCC->CFGR |= RCC_CFGR_SW_HSE; // ������� HSE ��� ������������ SW0=1.
  
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
  
  
  /* �������� ��� */
  RCC_APB1PeriphClockCmd(RCC_APB1Periph_DAC, ENABLE);
  /* �������� ������ 6 */
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


void testButtonCheck(void)
{
  if (TEST_BTN==Bit_RESET)  {
    delay_ms(250);
    if (TEST_BTN==Bit_RESET && TEST_BTN==Bit_RESET)
    {
    if (pf_mount(&Fs) == FR_OK) {	// Initialize FS
      Buff[0] = 0;
      //if (!pf_open("osccal")) pf_read(Buff, 1, &rb);	// Adjust frequency 
      //OSCCAL = org_osc + Buff[0];
      res = pf_opendir(&Dir, dir = "WAV");	// Open sound file directory 
      if (res == FR_NO_FILE)
        res = pf_opendir(&Dir, dir = "");	// Open root directory 
      
      if (res == FR_OK) {				// Repeat in the dir 
        res = pf_readdir(&Dir, 0);			// Rewind dir
        if (res == FR_OK) {				// Play all wav files in the dir 
          LED_TEST_ON;
          LED_PLAY_ON;
          LED_READY_OFF;
          res = play("/1.WAV");
          delay_ms(2000);
          res = play("/2.WAV");
          delay_ms(2000);
          res = play("/3.WAV");
          delay_ms(2000);
          res = play("/4.WAV");
          delay_ms(2000);
          res = play("/5.WAV");
          delay_ms(2000);
          res = play("/6.WAV");
          delay_ms(2000);
          res = play("/7.WAV");
          delay_ms(2000);
          res = play("/8.WAV");
          delay_ms(2000);
          res = play("/9.WAV");
          delay_ms(2000);
          res = play("/10.WAV");
          delay_ms(2000);
          res = play("/11.WAV");
          delay_ms(2000);
          res = play("/12.WAV");
          delay_ms(2000);
          res = play("/13.WAV");
          delay_ms(2000);
          res = play("/14.WAV");
          delay_ms(2000);
          res = play("/15.WAV");
          delay_ms(2000);
          res = play("/16.WAV");
          asm("NOP");
          LED_TEST_OFF;
          LED_PLAY_OFF;
        }
      }
    }
      
    }
    
  }
}

void cardReadyCheck (void)
{
    if (pf_mount(&Fs) == FR_OK)
    {
      ucCardResetCheck = 0;
      if (isCardReady==0)
      {
        //read files
          if (pf_open("/16.WAV") == FR_OK) maxPlayList=16;
          else if (pf_open("/15.WAV") == FR_OK) maxPlayList=15;
          else if (pf_open("/14.WAV") == FR_OK) maxPlayList=14;
          else if (pf_open("/13.WAV") == FR_OK) maxPlayList=13;
          else if (pf_open("/12.WAV") == FR_OK) maxPlayList=12;
          else if (pf_open("/11.WAV") == FR_OK) maxPlayList=11;
          else if (pf_open("/10.WAV") == FR_OK) maxPlayList=10;
          else if (pf_open("/9.WAV") == FR_OK) maxPlayList=9;
          else if (pf_open("/8.WAV") == FR_OK) maxPlayList=8;
          else if (pf_open("/7.WAV") == FR_OK) maxPlayList=7;
          else if (pf_open("/6.WAV") == FR_OK) maxPlayList=6;
          else if (pf_open("/5.WAV") == FR_OK) maxPlayList=5;
          else if (pf_open("/4.WAV") == FR_OK) maxPlayList=4;
          else if (pf_open("/3.WAV") == FR_OK) maxPlayList=3;
          else if (pf_open("/2.WAV") == FR_OK) maxPlayList=2;
          else if (pf_open("/1.WAV") == FR_OK) maxPlayList=1;
        
        
        
      }
      isCardReady = 1;
      LED_READY_ON;
    }
    else 
    {
      if (++ucCardResetCheck == 0xFF)
      {
        //card power reset
        LED_READY_OFF;
        MMC_POWER_OFF;
        
        delay_ms(2000);
        MMC_POWER_ON;
        ucCardResetCheck = 0;
      }
      else
      {
        isCardReady=0;
        LED_READY_OFF;
      }
    }
}


void playButtonCheck(void)
{
    //���� ����������� ������ �� ����
  if (buttonEnable && PLAY_BTN==PLAY_STATE)  {
    delay_ms(250);
    if (PLAY_BTN==PLAY_STATE && PLAY_BTN==PLAY_STATE)
    {
    
    asm("NOP");
    if (NoFiles<10) NoFiles++;
    if (pf_mount(&Fs) == FR_OK) {	// Initialize FS
      Buff[0] = 0;
      
      //if (!pf_open("osccal")) pf_read(Buff, 1, &rb);	// Adjust frequency 
      //OSCCAL = org_osc + Buff[0];
      res = pf_opendir(&Dir, dir = "");	// Open root directory 
      
      if (res == FR_OK) {				// Repeat in the dir 
        res = pf_readdir(&Dir, 0);			// Rewind dir
        if (res == FR_OK) {				// Play all wav files in the dir 

          res = pf_readdir(&Dir, &Fno);		// Get a dir entry 
          if (1)//!(Fno.fattrib & (AM_DIR|AM_HID)) && strstr(Fno.fname, ".WAV"))
          {
            asm("NOP");
            //res = play("SINE/8.WAV");
            isNowPlaying = 1;
            NoFiles = 0;
            LED_PLAY_ON;
            LED_READY_OFF;
            if (maxPlayList)
            {
            switch(cntPlayList)
            {
            case 0:     res = play("/1.WAV");   break;
            case 1:     res = play("/2.WAV");   break;
            case 2:     res = play("/3.WAV");   break;
            case 3:     res = play("/4.WAV");   break;
            case 4:     res = play("/5.WAV");   break;
            case 5:     res = play("/6.WAV");   break;
            case 6:     res = play("/7.WAV");   break;
            case 7:     res = play("/8.WAV");   break;
            case 8:     res = play("/9.WAV");   break;
            case 9:     res = play("/10.WAV");   break;
            case 10:    res = play("/11.WAV");   break;
            case 11:    res = play("/12.WAV");   break;
            case 12:    res = play("/13.WAV");   break;
            case 13:    res = play("/14.WAV");   break;
            case 14:    res = play("/15.WAV");   break;
            case 15:    res = play("/16.WAV");   break;           
            }
            
            if ((cntPlayList+1)<maxPlayList) ++cntPlayList;
            else cntPlayList=0;
            secondTrackTimer = 1000; //1min timeout
            }
            LED_PLAY_OFF;
            delay_ms(3000);
          }
          asm("NOP");
        }
      }
      
    }
    
    
    while(PLAY_BTN==PLAY_STATE)
    {
      asm("NOP");
      testButtonCheck();
    }
    
      buttonEnable = 1;  
    
    }

  }
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
  TIM2->SR &= ~TIM_SR_UIF; //���������� ���� UIF
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
  TIM3->SR &= ~TIM_SR_UIF; //���������� ���� UIF
  
  if (isNowPlaying)
  {
    //red off
    //LED_READY_OFF;
   // GPIO_ResetBits(GPIOB, GPIO_Pin_10);
    //green blinking
    //if (GPIO_ReadOutputDataBit(GPIOB, GPIO_Pin_1) == Bit_RESET) GPIO_SetBits(GPIOB, GPIO_Pin_1);
    //else GPIO_ResetBits(GPIOB, GPIO_Pin_1);
  }
  else
  {
    //LED_READY_ON;
  

  
  if (secondTrackTimer)
  {
    if (secondTrackTimer-- == 1) 
    {
            //clear all
      V1_OFF;
      V2_OFF;
      V3_OFF;
             // GPIO_ResetBits(GPIOB, GPIO_Pin_8);
              //GPIO_ResetBits(GPIOB, GPIO_Pin_9);
              //GPIO_ResetBits(GPIOC, GPIO_Pin_14);

              
      secondTrack = 0;
      //red off
      //GPIO_ResetBits(GPIOB, GPIO_Pin_10);
      // green off
      //GPIO_ResetBits(GPIOB, GPIO_Pin_1);
    }
  }
  }
  asm("NOP");
  
}
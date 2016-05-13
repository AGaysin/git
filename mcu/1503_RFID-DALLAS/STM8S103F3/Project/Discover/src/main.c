/**
  ******************************************************************************
  * @file STM8_main.c
  * @brief RC Touch Sensing Library for STM8 CPU family.
  * Application example.
  * @author STMicroelectronics - MCD Application Team
  * @version V0.2.0
  * @date 19-DEC-2008
  ******************************************************************************
  *
  * THE PRESENT FIRMWARE WHICH IS FOR GUIDANCE ONLY AIMS AT PROVIDING CUSTOMERS
  * WITH CODING INFORMATION REGARDING THEIR PRODUCTS IN ORDER FOR THEM TO SAVE
  * TIME. AS A RESULT, STMICROELECTRONICS SHALL NOT BE HELD LIABLE FOR ANY
  * DIRECT, INDIRECT OR CONSEQUENTIAL DAMAGES WITH RESPECT TO ANY CLAIMS ARISING
  * FROM THE CONTENT OF SUCH FIRMWARE AND/OR THE USE MADE BY CUSTOMERS OF THE
  * CODING INFORMATION CONTAINED HEREIN IN CONNECTION WITH THEIR PRODUCTS.
  *
  * <h2><center>&copy; COPYRIGHT 2008 STMicroelectronics</center></h2>
  * @image html logo.bmp
  ******************************************************************************
  */


/*RELEASE V.1.2




Увеличили время определения кода 33 tRL=25us было 15us
на считывателе Z5R код 33h не успевал считываться и получалось 00h


*/


/*RELEASE V.1.1




ДЛЯ МОСТОВОЙ СХЕМЫ НА 74AC02/74AC04

Частота генерации длиться 75мс, 250мс выключает генерацию для экономи потребления
Если в поле зрения попадает карта - продливает генерацию на 75мс каждый считанные импульс


*/


/* RELEASE V.1.0
DATE: 13.04.2015
FILE: STM8S103F3P6_RFID_V1.0.hex
CPU: STM8S103F3P6
DEV: AG, John
PCB: RF-1W ver. 1.0.pcb
VCC: 12V
ICC: 20-30mA

Постоянная генерация выхода 125кГц,
Оптимизирована работа компаратора

Вначале ждет считывание RFID, проверяет проверки четности и контрольной суммы, как только считанные данные верные - начинает передачу кода в TM
При этом передача кода в TM идет на каждый запрос, пока в поле зрения есть карта.


*/


/* Includes ------------------------------------------------------------------*/
#include "stm8s.h"
#include "stm8s_tim1.h"
#include "stm8s_tim2.h"
#include "stm8s_adc1.h"
#include "stm8s_itc.h"
#include "stm8s_tim4.h"
#include "stm8s_exti.h"
#include "intrinsics.h" 
#include "stm8s_iwdg.h"



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
void TIM4_Configuration(void);

void IWDG_Configuration(void);

unsigned char CRC8( unsigned char *mas, unsigned char Len );
unsigned char RFID_crc8;
void ADC_Configuration(void);

unsigned char RFID_FindStart(void);
void RFID_GetData(void);
void RFID_Reset(void);

unsigned char OWTM_Request(void);
unsigned char OWTM_SendData(unsigned char *OW_DataBuffer);
void BuzzerChirp(void);
void BuzzerStart(void);
void BuzzerSound(unsigned int freq, unsigned int delay);


unsigned char ucDataByte, ucDataBit, ucRowParity=0;
unsigned char ucStringParity=0;
/* Private functions ---------------------------------------------------------*/

/* Global variables ----------------------------------------------------------*/

unsigned int uiRfidFreqCnt=0;
unsigned char isRfidFreqStart=0;
unsigned char isRfidFreqStop=0;
unsigned char isRfedFreqEnable=0;


unsigned char ucDataReceiveIndex=0;
unsigned char ucDataReceiveBuffer[11];

unsigned char ucOldKeyCode[11];
unsigned char ucCnt=0;
unsigned int uiKeyCodeResetTimer=0;
unsigned char ucDuration=0;
unsigned char ucDurationFlag=0;
unsigned char ucEdgeCounter=0;
unsigned char flagEnableTM_DisableRFID=0;
unsigned char ucInputLevel=0;

unsigned int uiRfidPulsesCounter=0;
unsigned char flagRfidKeyCodeReady=0;
unsigned char flagRfidKeyCodeSendOk=0;

unsigned char uiOWCountEnable=0;
unsigned int uiOWTimer;
unsigned char flagRfidPinOldState=0;



unsigned char ucTestMaxStartCounter=0;
/* Public functions ----------------------------------------------------------*/
__interrupt void EMMarineDataReceive(void);
/**
  ******************************************************************************
  * @brief Main function.
  * @par Parameters:
  * None
  * @retval void None
  * @par Required preconditions:
  * None
  ******************************************************************************
  */

/* FIRST MAKET VERSION 

#define EM_MARINE_READ_INPUT GPIO_ReadInputPin(GPIOC, GPIO_PIN_4)

#define TM_READ_INPUT GPIO_ReadInputPin(GPIOA, GPIO_PIN_3)

#define TM_PULL_DOWN GPIO_WriteHigh(GPIOC,GPIO_PIN_7);

#define TM_PULL_UP GPIO_WriteLow(GPIOC,GPIO_PIN_7);
*/



#define EM_MARINE_READ_INPUT GPIO_ReadInputPin(GPIOA, GPIO_PIN_3)

//#define TM_READ_INPUT GPIO_ReadInputPin(GPIOC, GPIO_PIN_3)

//#define TM_PULL_DOWN 

//#define TM_PULL_UP GPIO_WriteLow(GPIOC,GPIO_PIN_4);

#define TEST_ON GPIO_WriteHigh(GPIOD,GPIO_PIN_4);
#define TEST_OFF GPIO_WriteLow(GPIOD,GPIO_PIN_4);
#define TEST_REVERSE GPIO_WriteReverse(GPIOD,GPIO_PIN_4);

void main(void)
{
  /* Configures clocks */
  CLK_Configuration();

  /* Configures GPIOs */
  GPIO_Configuration();
  GPIO_WriteLow(GPIOC,GPIO_PIN_4);
  

  //PWM 125kHz clock gen
  TIM2_Configuration();
  //ADC_Configuration();
  TIM4_Configuration();
  /* Configures External Interrupts */
  //PC4 interrupt
  //EXTI_SetExtIntSensitivity(EXTI_PORT_GPIOC,EXTI_SENSITIVITY_RISE_ONLY);
  //EXTI_SetExtIntSensitivity(EXTI_PORT_GPIOA,EXTI_SENSITIVITY_RISE_FALL);
  //EXTI_SetTLISensitivity(EXTI_TLISENSITIVITY_FALL_ONLY);

  //TM Timer
  //TIM1_Configuration();
  
  

  /* enable interrupts */
  BuzzerStart();
  
  IWDG_Configuration();
  
  enableInterrupts();


  
  for (;;)
  {

    //RFID 125 kHz GENERATION START
   /*if (isRfidFreqStart) 
    {
      isRfidFreqStart = 0;
      isRfedFreqEnable = 1;
      TIM2_Configuration();
    }
    //RFID 125 kHz GENERATION STOP
    if (isRfidFreqStop)
    {
      isRfidFreqStop = 0;
      isRfedFreqEnable = 0;
      TIM2_DeInit();
    }
    
    */
    
    //Отправка данных в 1-Вайр
    if (flagRfidKeyCodeReady)
    {
      //Запрещаем прерывания
      disableInterrupts();
      //Тестовый светик, горит = читает RFID, не горит - передает TM
      //GPIO_WriteLow(GPIOB, GPIO_PIN_5);
      while(++uiRfidPulsesCounter < 1500)
      {
        IWDG_ReloadCounter(); //Wath-dog reset
        
        //Проверка импульсов на входе RFID
        if (EM_MARINE_READ_INPUT) uiRfidPulsesCounter = 0;
        
        //Ждем низкого уровня, импульса RESET
        if (!GPIO_ReadInputPin(GPIOC, GPIO_PIN_3)) 
        {
          if (OWTM_SendData(ucDataReceiveBuffer)) 
          {
            if (flagRfidKeyCodeSendOk)
            {
              flagRfidKeyCodeSendOk = 0;
              //Chirp <- WDR inside
              BuzzerChirp();
              uiRfidPulsesCounter=0;
            }
          }
        }
      }
      //Разрешение прерываний
      enableInterrupts();
      //Переключение на RFID
      flagRfidKeyCodeReady = 0;
      //Сброс параметров RFID
      RFID_Reset();
    }
    else //RFID CODE READING
    {
      //Wath-dog reset
      IWDG_ReloadCounter();
      //Тестовый светик, горит = читает RFID, не горит - передает TM
      //GPIO_WriteHigh(GPIOB, GPIO_PIN_5);
      if (EM_MARINE_READ_INPUT && !ucInputLevel)
      {
        //Продливаем тактирование RFID на 70мкс
        uiRfidFreqCnt = 1;
        //Rise edge
        ucInputLevel = 1;
        if (RFID_FindStart()) RFID_GetData();
      }
      else if (!EM_MARINE_READ_INPUT && ucInputLevel)
      {
        //Продливаем тактирование RFID на 70мкс
        uiRfidFreqCnt = 1;
        //Fall edge
        ucInputLevel = 0;
        if (RFID_FindStart()) RFID_GetData();
      }
    }
  }
}


void CLK_Configuration(void)
{

  /* Fmaster = 16MHz */
  CLK_HSIPrescalerConfig(CLK_PRESCALER_HSIDIV1);

}

void GPIO_Configuration(void)
{
  /* GPIOD reset */
  GPIO_DeInit(GPIOD);
  GPIO_DeInit(GPIOA);
  GPIO_DeInit(GPIOB);

  
  //PD3 - TIM2-CH2 - 125kHz - PWM 50%
  GPIO_Init(GPIOD, GPIO_PIN_3, GPIO_MODE_OUT_PP_HIGH_FAST);
  
  //PD2 - BUZZER output, Push-Pull
  //GPIO_Init(GPIOD, GPIO_PIN_2, GPIO_MODE_OUT_PP_LOW_FAST);
  
  /*FIRST MAKET VERSION 
  //PC4 - EM-MARINE DATA INPUT PIN, Ext. Interrupt 
  GPIO_Init(GPIOC, GPIO_PIN_4, GPIO_MODE_IN_FL_NO_IT);
  
  //PA3 - TM INPUT, 
  GPIO_Init(GPIOA, GPIO_PIN_3, GPIO_MODE_IN_FL_NO_IT);
  
  //PC7 - TM OUTPUT PushPull 
  GPIO_Init(GPIOC, GPIO_PIN_7, GPIO_MODE_OUT_PP_HIGH_FAST);
*/
  
  //PA3 - EM-MARINE DATA INPUT PIN, Ext. Interrupt 
  GPIO_Init(GPIOA, GPIO_PIN_3, GPIO_MODE_IN_FL_NO_IT);
  
  //PC3 - TM INPUT, 
  GPIO_Init(GPIOC, GPIO_PIN_3, GPIO_MODE_IN_FL_NO_IT);
  
  //PC4 - TM OUTPUT PushPull 
  GPIO_Init(GPIOC, GPIO_PIN_4, GPIO_MODE_OUT_PP_HIGH_FAST);
  
  //PD4 - LED TEST out OD
  GPIO_Init(GPIOD, GPIO_PIN_4, GPIO_MODE_OUT_PP_HIGH_FAST);
  //PB4 - LED TEST out OD
 //GPIO_Init(GPIOB, GPIO_PIN_4, GPIO_MODE_OUT_OD_LOW_FAST);
  
  
  
  //PC5 - BUZZER+ OUTPUT PushPull 
  GPIO_Init(GPIOC, GPIO_PIN_5, GPIO_MODE_OUT_PP_HIGH_FAST);
  //PC6 - BUZZER- OUTPUT PushPull 
  GPIO_Init(GPIOC, GPIO_PIN_6, GPIO_MODE_OUT_PP_HIGH_FAST);
 

}

void TIM1_Configuration(void)
{
  /* TIM1 Peripheral Configuration */ 
  TIM1_DeInit();

  /* Time Base configuration */
	/*
  TIM1_Prescaler = 0
  TIM1_CounterMode = TIM1_COUNTERMODE_UP
  TIM1_Period = 65535
  TIM1_RepetitionCounter = 0
  
	*/

  TIM1_TimeBaseInit(0, TIM1_COUNTERMODE_UP, 6000,0); //370us
  
  /* Clear TIM4 update flag */
  TIM1_ClearFlag(TIM1_FLAG_UPDATE);
  /* Enable update interrupt */
  TIM1_ITConfig(TIM1_IT_UPDATE, ENABLE);
  
  /* TIM1 counter enable */
  TIM1_Cmd(ENABLE);
}

void TIM2_Configuration(void)
{
  TIM2_DeInit();
  TIM2_TimeBaseInit(TIM2_PRESCALER_1, 127);
  TIM2_OC2Init(TIM2_OCMODE_PWM2, TIM2_OUTPUTSTATE_ENABLE,64, TIM2_OCPOLARITY_LOW);
  //TIM2_ARRPreloadConfig(ENABLE);
  //TIM2_OC2PreloadConfig(ENABLE);
  //TIM2_SetCompare2(uiRfidOutFrequency);
  //TIM2_SetAutoreload(2*uiRfidOutFrequency-1);
  //if (uiRfidOutFrequency<100)uiRfidOutFrequency++;
  //else TIM2_DeInit();
  /* Clear TIM2 update flag */
  //TIM2_ClearFlag(TIM2_FLAG_CC2);
  /* Enable update interrupt */
  //TIM2_ITConfig(TIM2_IT_CC2, ENABLE);
  TIM2_Cmd(ENABLE);
}

void ADC_Configuration(void)
{
  /*  Init GPIO for ADC2 */
  //GPIO_Init(GPIOC, GPIO_PIN_4, GPIO_MODE_IN_FL_NO_IT);
  
  /* De-Init ADC peripheral*/
  ADC1_DeInit();

  /* Init ADC2 peripheral */
  ADC1_Init(ADC1_CONVERSIONMODE_CONTINUOUS, ADC1_CHANNEL_2, ADC1_PRESSEL_FCPU_D18, \
            ADC1_EXTTRIG_TIM, DISABLE, ADC1_ALIGN_LEFT, ADC1_SCHMITTTRIG_CHANNEL2,\
            DISABLE);
  //ADC1->CSR |= 0x02;
  //ADC1->CR1 |= 0x52;
  //ADC1->CR3 |= 0x80;
    

  //ADC1->CR2 |= 0x0A;
  
  ADC1_StartConversion();

  /* Enable EOC interrupt */
  //ADC1_ITConfig(ADC1_IT_EOCIE, ENABLE);
  
  //Priority ITC_IRQ_ADC1
  //ITC_SetSoftwarePriority(ITC_IRQ_ADC1,ITC_PRIORITYLEVEL_3);

  /* Enable general interrupts */  
  //enableInterrupts();
  
  /*Start Conversion */
}


void TIM4_Configuration(void)
{
  /* TIM4 configuration:
   - TIM4CLK is set to 16 MHz, the TIM4 Prescaler is equal to 128 so the TIM1 counter
   clock used is 16 MHz / 16 = 1 000 000 Hz
  - With 1 000 000 Hz we can generate time base:
      max time base is 0.256 ms if TIM4_PERIOD = 255 --> (255 + 1) / 1000000 = 32 us
      min time base is 0.002 ms if TIM4_PERIOD = 1   --> (  1 + 1) / 1000000 = 2 us
  - In this example we need to generate a time base equal to 125us
   so div 8 TIM4_PERIOD 100us = (0.0001 * 2000000 - 1) = 99 
  so div 4 TIM4_PERIOD 10us = (0.00001 * 4000000 - 1) = 39 */

  /* Time base configuration */
  TIM4_DeInit();
  TIM4_TimeBaseInit(TIM4_PRESCALER_8, 99); //50us
  //TIM4_TimeBaseInit(TIM4_PRESCALER_4, 39); //20us
  /* Clear TIM4 update flag */
  TIM4_ClearFlag(TIM4_FLAG_UPDATE);
  /* Enable update interrupt */
  TIM4_ITConfig(TIM4_IT_UPDATE, ENABLE);
  /* Enable TIM4 */
  TIM4_Cmd(ENABLE);
}

unsigned char RFID_FindStart(void)
{
  if (ucEdgeCounter>=17) return 1;
  if (ucEdgeCounter >= ucTestMaxStartCounter) ucTestMaxStartCounter=ucEdgeCounter;
  if (ucCnt>=3 && ucCnt<7) ucDuration = 1;
  else if (ucCnt>=8 && ucCnt<12)ucDuration = 2;
  else 
  {
    RFID_Reset();
    ucDuration = 0;
  }
  ucCnt = 0;
  TIM4_SetCounter(0);
  //TEST_ON;
  if (ucEdgeCounter==0) 
  {
    //TEST_OFF;
    
    if (ucDuration==2 && !ucInputLevel) //High & Long
    {
      ucEdgeCounter=1;
      //Starts with LONG & HIGH always
      ucDurationFlag = 2;
    }
  }
  else if ((ucEdgeCounter%2))
  {
    if (ucDuration==1 && ucInputLevel) //Low & Short
    {
      ucEdgeCounter++;
    }
    else ucEdgeCounter=0;
  }
  else if (!(ucEdgeCounter%2))
  {
    if (ucDuration==1 && !ucInputLevel) //High & Short
    {
      ucEdgeCounter++;
    }
    else ucEdgeCounter=0;
  }
  return 0;
}

void RFID_GetData(void)
{
  unsigned char update=0;
  //Оценка длительности импульса
  if (ucCnt>=3 && ucCnt<7) ucDuration = 1;
  else if (ucCnt>=8 && ucCnt<12) ucDuration = 2;
  else 
  {
    RFID_Reset();
    ucDuration = 0;

  }
  ucCnt = 0;
  TIM4_SetCounter(0);

  
  //Если Long&high -> ловим спады, записываем единицу в буфер
  //Если Long&Low -> ловим фронты, записываем 0 в буфер
  if (ucDuration==2) //Long
  {
    if (ucInputLevel) ucDurationFlag = 1; //now High level -> was Low level.
    else ucDurationFlag = 2; //&was High
  }
  
  if (ucInputLevel && ucDurationFlag==1) //now High, was Low -> Rise edge ==0
  {
    update= 1;
    ucDataBit=0;
  }
  if (!ucInputLevel && ucDurationFlag==2) 
  {
    update = 1;
    ucDataBit=1;
  }
  
  if (update)
  {
    update = 0;
    if (ucDataReceiveIndex && (ucDataReceiveIndex%5)==4)
    {
      if ((ucDataReceiveIndex/5)==10)
      {
        //Full message received
        //Row Parity check
        if (ucRowParity != ucDataByte)
        {
          RFID_Reset();
          return;
        }
        else
        {
          //Флаг переключения на передачу RFID в считыватель TM
          flagRfidKeyCodeReady = 1;
          //Сброс счетсчика импульсов наличия карты RFID
          uiRfidPulsesCounter = 0;
          //Флаг для ЧИРПа при первой передачи кода ТМ
          flagRfidKeyCodeSendOk = 1;
          //TIM4_DeInit();
          //Combine Data
          ucDataReceiveBuffer[9]=(ucDataReceiveBuffer[8]<<4) + ucDataReceiveBuffer[9];//TMP1->1
          ucDataReceiveBuffer[8]=(ucDataReceiveBuffer[4]<<4) + ucDataReceiveBuffer[5];//TMP2->3
          ucDataReceiveBuffer[4]=(ucDataReceiveBuffer[2]<<4) + ucDataReceiveBuffer[3];
          ucDataReceiveBuffer[5]=(ucDataReceiveBuffer[0]<<4) + ucDataReceiveBuffer[1];
          ucDataReceiveBuffer[2]=(ucDataReceiveBuffer[6]<<4) + ucDataReceiveBuffer[7];
          ucDataReceiveBuffer[1]=ucDataReceiveBuffer[9];
          ucDataReceiveBuffer[3]=ucDataReceiveBuffer[8];
          ucDataReceiveBuffer[6]=0x00; //5
          ucDataReceiveBuffer[0]=0x01; //FAMILY-CODE
          ucDataReceiveBuffer[7]=CRC8(ucDataReceiveBuffer, 7); //CRC
          
          asm("NOP");


        }
      }
      else
      {
      //String Parity check
      if (ucStringParity != ucDataBit)
      {
        RFID_Reset();
        return;
      }
      else
      {
        ucStringParity = 0;
        //Copy data to Buffer
        ucDataReceiveBuffer[(ucDataReceiveIndex/5)]=ucDataByte;
        //Row Parity calc
        ucRowParity ^= ucDataByte;
        //Clear DataRegister
        ucDataByte = 0;
      }
      }
    }
    else
    {
      //GPIO_WriteHigh(GPIOB, GPIO_PIN_5);
      //Combine Bit to Byte
      ucDataByte <<= 1;
      ucDataByte |= ucDataBit;
      //String Parity calc
      ucStringParity ^= ucDataBit;
    }
    ucDataReceiveIndex++;
  }
}




unsigned char OWTM_SendData(unsigned char *OW_DataBuffer)
{
  unsigned int OW_Timer=0;
  unsigned char OW_DataBits=0;
  unsigned char OW_DataByte=0;
  unsigned char OW_DataBytesCnt=0;
  //Запрет прерываний
  asm("NOP");
  disableInterrupts();
  asm("NOP");
  IWDG_ReloadCounter(); //Wath-dog reset

  
  
  OW_Timer = 0;
  while (!GPIO_ReadInputPin(GPIOC, GPIO_PIN_3) && (++OW_Timer<50000)) asm("NOP");
  if (OW_Timer >= 100)
  {
    
    //Формирование импульса PRESENCE
    
    //20us delay
    
    OW_Timer=20; while(OW_Timer--) asm("NOP");
    //Роняем шину к земле
    GPIO_WriteHigh(GPIOC,GPIO_PIN_4);
    //100us delay
    OW_Timer=150; while(OW_Timer--) asm("NOP");
    //поднимаем шину
    GPIO_WriteLow(GPIOC,GPIO_PIN_4);
    
    //Ждем возвращения линии в высокий уровень
    OW_Timer=50000;
    while(!GPIO_ReadInputPin(GPIOC, GPIO_PIN_3) && OW_Timer--) asm("NOP");
    asm("NOP");
    
    //Прием данных
    asm("NOP");
    while(OW_DataBits<8)
    {
      IWDG_ReloadCounter(); //Wath-dog reset

      //ждем спадающего фронта
      OW_Timer=50000;
      while(GPIO_ReadInputPin(GPIOC, GPIO_PIN_3) && OW_Timer--) asm("NOP");
      //Задержка 25мкс
      OW_Timer=25; while(OW_Timer--) asm("NOP");
    
      //Чтание бита данных
      if (GPIO_ReadInputPin(GPIOC, GPIO_PIN_3)) OW_DataByte |= (1<<OW_DataBits);
      
      //инкремент счетсчика битов
      OW_DataBits++;
      
      //Ждем возвращения линии в высокий уровень
      OW_Timer=50000;
      while(!GPIO_ReadInputPin(GPIOC, GPIO_PIN_3) && OW_Timer--) asm("NOP");

    }
    
    
    //Анализ принятого кода
    if (OW_DataByte == 0x33)
    {
      /* КОЛЛИЗИЯ: ЗДЕСЬ МОЖЕТ ЗАВИСНУТЬ ЕСЛИ КОНТРОЛЛЕР СЧИТАЛ НЕ ВСЕ БИТЫ */
      
      //Передача кода ключа
      //1-Wire отправляет код побитно с конца
      //8 байт данных
      OW_DataBytesCnt=0;
      while(OW_DataBytesCnt<8)
      {
        //Отправка одного байта побитно
        OW_DataBits=0;
        while(OW_DataBits<8)
        {
          IWDG_ReloadCounter();                  //Wath-dog reset
          
          //Ждем спада фронта
          OW_Timer=50000;
          while(GPIO_ReadInputPin(GPIOC, GPIO_PIN_3) && OW_Timer--) asm("NOP");
          
          // если передаем единицу поднимаем уровень на 45мкс
          //если передаем ноль - удерживаем низкий уровень в течении 45мкс
          
          if (!(OW_DataBuffer[OW_DataBytesCnt] & (1<<OW_DataBits))) GPIO_WriteHigh(GPIOC,GPIO_PIN_4);
          
          //Задержка 50мкс
          OW_Timer=50; while(OW_Timer--) asm("NOP");
          
          //test
          //GPIO_WriteLow(GPIOB,GPIO_PIN_5);
          
          //Отпускаем шину вверх
          GPIO_WriteLow(GPIOC,GPIO_PIN_4);
          
          //Ждем возвращения линии в высокий уровень
          OW_Timer=50000;
          while(!GPIO_ReadInputPin(GPIOC, GPIO_PIN_3) && OW_Timer--) asm("NOP");
          OW_DataBits++;
        }
        
        OW_DataBytesCnt++;
      }
      asm("NOP"); //Передача данных завершена
      //Ждем возвращения линии в высокий уровень
      OW_Timer=50000;
      while(!GPIO_ReadInputPin(GPIOC, GPIO_PIN_3) && OW_Timer--) asm("NOP");
      return 1;
    }
    
  }

  //Ждем возвращения линии в высокий уровень
  OW_Timer=50000;
  while(!GPIO_ReadInputPin(GPIOC, GPIO_PIN_3) && OW_Timer--) asm("NOP");
  return 0;
}




void RFID_Reset(void)
{
  ucEdgeCounter=0;
  ucRowParity=0;
  ucStringParity=0;
  ucDataByte=0;
  ucDataReceiveIndex=0;
  ucCnt=0;
}


/* Подсчет CRC8 массива mas длиной Len */
unsigned char CRC8( unsigned char *mas, unsigned char Len )
{
  unsigned char i,dat,crc,fb,st_byt;
  st_byt=0; crc=0;
  do{
    dat=mas[st_byt];
    for( i=0; i<8; i++) {  // счетчик битов в байте
      fb = crc ^ dat;
      fb &= 1;
      crc >>= 1;
      dat >>= 1;
      if( fb == 1 ) crc ^= 0x8c; // полином
    }
    st_byt++;
  } while( st_byt < Len ); // счетчик байтов в массиве
  return crc;
}


void BuzzerChirp(void)
{
  unsigned int duration=0, tone=0;

  
  GPIO_WriteLow(GPIOC, GPIO_PIN_6);
  GPIO_WriteLow(GPIOC, GPIO_PIN_5);
  //F=4kHz
  duration = 400;
  while(duration--)
  {
    //Wath-dog reset
    IWDG_ReloadCounter();
    
    GPIO_WriteLow(GPIOC, GPIO_PIN_5);
    GPIO_WriteHigh(GPIOC, GPIO_PIN_6);
    tone = 150; while(tone--) asm("NOP");
    GPIO_WriteLow(GPIOC, GPIO_PIN_6);
    GPIO_WriteHigh(GPIOC, GPIO_PIN_5);
    tone = 150; while(tone--) asm("NOP");
  }
  
  
  //Off Pins
  GPIO_WriteLow(GPIOC, GPIO_PIN_6);
  GPIO_WriteLow(GPIOC, GPIO_PIN_5);

}

void BuzzerStart(void)
{

  unsigned int DelaySound;
  unsigned char ucDelaySound2;
  disableInterrupts();
  ucDelaySound2 = 40;
  while(ucDelaySound2--) 
  {
    DelaySound=50000; while (DelaySound--) ;
  }
  
//RUSSIAN GIMN!!!
  //G 0.5 3136 Hz
  BuzzerSound(3136, 10);
  DelaySound=2000; while (DelaySound--) ;
  //C 1.0 4186 Hz
  BuzzerSound(4186, 20);
  DelaySound=4000; while (DelaySound--) ;
  //G 0.5 3136 Hz
  BuzzerSound(3136, 10);
  DelaySound=2000; while (DelaySound--) ;
  //A 0.25 3440 Hz
  BuzzerSound(3440, 5);
  DelaySound=1000; while (DelaySound--) ;
  //B 0.5 3951 Hz
  BuzzerSound(3951, 10);
  DelaySound=1000; while (DelaySound--) ;
  /*
  //E 0.5 2637 Hz
  BuzzerSound(2637, 10);
  DelaySound=2000; while (DelaySound--) ;
  //E 0.5 2637 Hz
  BuzzerSound(2637, 10);
  DelaySound=2000; while (DelaySound--) ;
  //A 1.0 3440 Hz
  BuzzerSound(3440, 20);
  DelaySound=4000; while (DelaySound--) ;
  //G 0.5 3136 Hz
  BuzzerSound(3136, 10);
  DelaySound=2000; while (DelaySound--) ;
  //F 0.25 2793 Hz
  BuzzerSound(2793, 5);
  DelaySound=1000; while (DelaySound--) ;
  //G 0.5 3136 Hz
  //G 0.5 3136 Hz
  BuzzerSound(3136, 10);
  DelaySound=2000; while (DelaySound--) ;
  */
  enableInterrupts();

}


void IWDG_Configuration(void)
{
  IWDG_Enable();
  IWDG_WriteAccessCmd(IWDG_WriteAccess_Enable);
  IWDG_SetPrescaler(IWDG_Prescaler_256);
  IWDG_SetReload(0xFF);
  
  IWDG_WriteAccessCmd(IWDG_WriteAccess_Disable);
}
void BuzzerSound(unsigned int freq, unsigned int delay)
{
  unsigned int duration=0;
  unsigned long tone=0;

  //F=4kHz - 150
  //2000 - 300
  while(delay--)
  {
  duration = freq/300;
  while(duration--)
  {
    GPIO_WriteLow(GPIOC, GPIO_PIN_5);
    GPIO_WriteHigh(GPIOC, GPIO_PIN_6);
    tone = 600000 / freq; while(tone--) asm("NOP");
    GPIO_WriteLow(GPIOC, GPIO_PIN_6);
    GPIO_WriteHigh(GPIOC, GPIO_PIN_5);
    tone = 600000 / freq; while(tone--) asm("NOP");
  }
  }
  
  
  //Off Pins
  GPIO_WriteLow(GPIOC, GPIO_PIN_6);
  GPIO_WriteLow(GPIOC, GPIO_PIN_5);
}




/****************** INTERRUPTS ******************************/


#pragma vector=7 //особенность IAR - номер_вектора = номер_по_документации_ST + 2 //PC4 EXTI2 PORTC Vector=5;
__interrupt void EMMarineDataReceive(void)
{
}


#pragma vector=13 //особенность IAR - номер_вектора = номер_по_документации_ST + 2
__interrupt void TIM1_Update(void)
{
  //GPIO_WriteReverse(GPIOB, GPIO_PIN_5);
  /* Cleat Interrupt Pending bit */
  TIM1_ClearITPendingBit(TIM1_IT_UPDATE);
}

#pragma vector=14 //Timer2 Copmare2
__interrupt void TIM2_CC2(void)
{


  /* Cleat Interrupt Pending bit */
  TIM2_ClearITPendingBit(TIM2_IT_CC2);
}


#pragma vector = 24 //INTERRUPT_HANDLER(ADC1_IRQHandler, 22) ITC_IRQ_ADC1
__interrupt void ADC1_ConversationComplete(void)
{
  ADC1_ITConfig(ADC1_IT_EOCIE, DISABLE);
}


#pragma vector=25 //особенность IAR - номер_вектора = номер_по_документации_ST + 2
__interrupt void TIM4_ReceiveDataTimer(void)
{
  //Если карты нет или код "смазанный" - готовимся к считываюнию заново

  if (ucCnt<200) ucCnt++;
  //isRfidFreqChangeEnable = 1;
  
  /*if (uiRfidFreqCnt == 0) 
  {
    isRfidFreqStart = 1;
    uiRfidFreqCnt = 1;
  }
  else if (uiRfidFreqCnt>5000) uiRfidFreqCnt = 0;
  else if (++uiRfidFreqCnt==1500) isRfidFreqStop = 1;*/
  
  
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




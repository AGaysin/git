/*
 * onewire.c
 *
 *  Created on: 13.02.2012
 *      Author: di
 */

#include "onewire.h"

#ifdef OW_USART1

#undef OW_USART2
#undef OW_USART3
#undef OW_USART4

#define OW_USART 		USART1
#define OW_DMA_CH_RX 	DMA1_Channel5
#define OW_DMA_CH_TX 	DMA1_Channel4
#define OW_DMA_FLAG		DMA1_FLAG_TC5

#endif


#ifdef OW_USART2

#undef OW_USART1
#undef OW_USART3
#undef OW_USART4

#define OW_USART 		USART2
#define OW_DMA_CH_RX 	DMA1_Channel6
#define OW_DMA_CH_TX 	DMA1_Channel7
#define OW_DMA_FLAG		DMA1_FLAG_TC6

#endif


// ����� ��� ������/�������� �� 1-wire
uint8_t ow_buf[8];
uint8_t *buf1;
#define OW_0	0x00
#define OW_1	0xff
#define OW_R_1	0xff

//-----------------------------------------------------------------------------
// ������� ����������� ���� ���� � ������, ��� �������� ����� USART
// ow_byte - ����, ������� ���� �������������
// ow_bits - ������ �� �����, �������� �� ����� 8 ����
//-----------------------------------------------------------------------------
void OW_toBits(uint8_t ow_byte, uint8_t *ow_bits) {
	uint8_t i;
	for (i = 0; i < 8; i++) {
		if (ow_byte & 0x01) {
			*ow_bits = OW_1;
		} else {
			*ow_bits = OW_0;
		}
		ow_bits++;
		ow_byte = ow_byte >> 1;
	}
}

//-----------------------------------------------------------------------------
// �������� �������������� - �� ����, ��� �������� ����� USART ����� ���������� ����
// ow_bits - ������ �� �����, �������� �� ����� 8 ����
//-----------------------------------------------------------------------------
uint8_t OW_toByte(uint8_t *ow_bits) {
	uint8_t ow_byte, i;
	ow_byte = 0;
	for (i = 0; i < 8; i++) {
		ow_byte = ow_byte >> 1;
		if (*ow_bits == OW_R_1) {
			ow_byte |= 0x80;
		}
		ow_bits++;
	}

	return ow_byte;
}

//-----------------------------------------------------------------------------
// �������������� USART � DMA
//-----------------------------------------------------------------------------
uint8_t OW_Init() {
	GPIO_InitTypeDef GPIO_InitStruct;
	USART_InitTypeDef USART_InitStructure;

	if (OW_USART == USART1) {
		RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA | RCC_APB2Periph_AFIO,
				ENABLE);

		// USART TX
		GPIO_InitStruct.GPIO_Pin = GPIO_Pin_9;
		GPIO_InitStruct.GPIO_Mode = GPIO_Mode_AF_PP;
		GPIO_InitStruct.GPIO_Speed = GPIO_Speed_50MHz;

		GPIO_Init(GPIOA, &GPIO_InitStruct);

		// USART RX
		GPIO_InitStruct.GPIO_Pin = GPIO_Pin_10;
		GPIO_InitStruct.GPIO_Mode = GPIO_Mode_IN_FLOATING;
		GPIO_InitStruct.GPIO_Speed = GPIO_Speed_50MHz;

		GPIO_Init(GPIOA, &GPIO_InitStruct);

		RCC_APB2PeriphClockCmd(RCC_APB2Periph_USART1, ENABLE);

		RCC_AHBPeriphClockCmd(RCC_AHBPeriph_DMA1, ENABLE);
	}

	if (OW_USART == USART2) {
		RCC_APB2PeriphClockCmd(RCC_APB2Periph_GPIOA | RCC_APB2Periph_AFIO,
				ENABLE);

		GPIO_InitStruct.GPIO_Pin = GPIO_Pin_2;
		GPIO_InitStruct.GPIO_Mode = GPIO_Mode_AF_PP;
		GPIO_InitStruct.GPIO_Speed = GPIO_Speed_50MHz;

		GPIO_Init(GPIOA, &GPIO_InitStruct);

		GPIO_InitStruct.GPIO_Pin = GPIO_Pin_3;
		GPIO_InitStruct.GPIO_Mode = GPIO_Mode_IN_FLOATING;
		GPIO_InitStruct.GPIO_Speed = GPIO_Speed_50MHz;

		GPIO_Init(GPIOA, &GPIO_InitStruct);

		RCC_APB1PeriphClockCmd(RCC_APB1Periph_USART2, ENABLE);

		RCC_AHBPeriphClockCmd(RCC_AHBPeriph_DMA1, ENABLE);
	}

	USART_InitStructure.USART_BaudRate = 115200;
	USART_InitStructure.USART_WordLength = USART_WordLength_8b;
	USART_InitStructure.USART_StopBits = USART_StopBits_1;
	USART_InitStructure.USART_Parity = USART_Parity_No;
	USART_InitStructure.USART_HardwareFlowControl =
			USART_HardwareFlowControl_None;
	USART_InitStructure.USART_Mode = USART_Mode_Tx | USART_Mode_Rx;

	USART_Init(OW_USART, &USART_InitStructure);
	USART_Cmd(OW_USART, ENABLE);
	return OW_OK;
}

//-----------------------------------------------------------------------------
// ������������ ����� � �������� �� ������� ��������� �� ����
//-----------------------------------------------------------------------------
uint8_t OW_Reset() {
	uint8_t ow_presence;
	USART_InitTypeDef USART_InitStructure;

	USART_InitStructure.USART_BaudRate = 9600;
	USART_InitStructure.USART_WordLength = USART_WordLength_8b;
	USART_InitStructure.USART_StopBits = USART_StopBits_1;
	USART_InitStructure.USART_Parity = USART_Parity_No;
	USART_InitStructure.USART_HardwareFlowControl =
			USART_HardwareFlowControl_None;
	USART_InitStructure.USART_Mode = USART_Mode_Tx | USART_Mode_Rx;
	USART_Init(OW_USART, &USART_InitStructure);

	// ���������� 0xf0 �� �������� 9600
	USART_ClearFlag(OW_USART, USART_FLAG_TC);
	USART_SendData(OW_USART, 0xf0);
	while (USART_GetFlagStatus(OW_USART, USART_FLAG_TC) == RESET) {
#ifdef OW_GIVE_TICK_RTOS
		taskYIELD();
#endif
	}

	ow_presence = USART_ReceiveData(OW_USART);

	USART_InitStructure.USART_BaudRate = 115200;
	USART_InitStructure.USART_WordLength = USART_WordLength_8b;
	USART_InitStructure.USART_StopBits = USART_StopBits_1;
	USART_InitStructure.USART_Parity = USART_Parity_No;
	USART_InitStructure.USART_HardwareFlowControl =
			USART_HardwareFlowControl_None;
	USART_InitStructure.USART_Mode = USART_Mode_Tx | USART_Mode_Rx;
	USART_Init(OW_USART, &USART_InitStructure);

	if (ow_presence != 0xf0) {
		return OW_OK;
	}

	return OW_NO_DEVICE;
}


//-----------------------------------------------------------------------------
// ��������� ������� � ����� 1-wire
// sendReset - �������� RESET � ������ �������.
// 		OW_SEND_RESET ��� OW_NO_RESET
// command - ������ ����, ���������� � ����. ���� ����� ������ - ���������� OW_READ_SLOTH
// cLen - ����� ������ ������, ������� ���� ��������� � ����
// data - ���� ��������� ������, �� ������ �� ����� ��� ������
// dLen - ����� ������ ��� ������. ����������� �� ����� ���� �����
// readStart - � ������ ������� �������� �������� ������ (���������� � 0)
//		����� ������� OW_NO_READ, ����� ����� �� �������� data � dLen
//-----------------------------------------------------------------------------
uint8_t OW_Send(uint8_t sendReset, uint8_t *command, uint8_t cLen,
		uint8_t *data, uint8_t dLen, uint8_t readStart) {

	// ���� ��������� ����� - ���������� � ��������� �� ������� ���������
	if (sendReset == OW_SEND_RESET) {
		if (OW_Reset() == OW_NO_DEVICE) {
			return OW_NO_DEVICE;
		}
	}

	while (cLen > 0) {

		OW_toBits(*command, ow_buf);
		command++;
		cLen--;

		DMA_InitTypeDef DMA_InitStructure;

		// DMA �� ������
		DMA_DeInit(OW_DMA_CH_RX);
		DMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t) &(USART2->DR);
		DMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t) ow_buf;
		DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralSRC;
		DMA_InitStructure.DMA_BufferSize = 8;
		DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;
		DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;
		DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_Byte;
		DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_Byte;
		DMA_InitStructure.DMA_Mode = DMA_Mode_Normal;
		DMA_InitStructure.DMA_Priority = DMA_Priority_Low;
		DMA_InitStructure.DMA_M2M = DMA_M2M_Disable;
		DMA_Init(OW_DMA_CH_RX, &DMA_InitStructure);

		// DMA �� ������
		DMA_DeInit(OW_DMA_CH_TX);
		DMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t) &(USART2->DR);
		DMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t) ow_buf;
		DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralDST;
		DMA_InitStructure.DMA_BufferSize = 8;
		DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;
		DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;
		DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_Byte;
		DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_Byte;
		DMA_InitStructure.DMA_Mode = DMA_Mode_Normal;
		DMA_InitStructure.DMA_Priority = DMA_Priority_Low;
		DMA_InitStructure.DMA_M2M = DMA_M2M_Disable;
		DMA_Init(OW_DMA_CH_TX, &DMA_InitStructure);

		// ����� ����� ��������
		USART_ClearFlag(OW_USART, USART_FLAG_RXNE | USART_FLAG_TC | USART_FLAG_TXE);
		USART_DMACmd(OW_USART, USART_DMAReq_Tx | USART_DMAReq_Rx, ENABLE);
		DMA_Cmd(OW_DMA_CH_RX, ENABLE);
		DMA_Cmd(OW_DMA_CH_TX, ENABLE);

		// ����, ���� �� ������ 8 ����
		while (DMA_GetFlagStatus(OW_DMA_FLAG) == RESET){
#ifdef OW_GIVE_TICK_RTOS
			taskYIELD();
#endif
		}

		// ��������� DMA
		DMA_Cmd(OW_DMA_CH_TX, DISABLE);
		DMA_Cmd(OW_DMA_CH_RX, DISABLE);
		USART_DMACmd(OW_USART, USART_DMAReq_Tx | USART_DMAReq_Rx, DISABLE);

		// ���� ����������� ������ ����-�� ����� - ������� �� � �����
		if (readStart == 0 && dLen > 0) {
			*data = OW_toByte(ow_buf);
			data++;
			dLen--;
		} else {
			if (readStart != OW_NO_READ) {
				readStart--;
			}
		}
	}

	return OW_OK;
}
unsigned char TermReadEnable=0;

void OW_TermMain (void)
{
  
  
      if (TermReadEnable==1)
    {
      //�������� ������ � ���
      
      // ���� ��������� ����� - ���������� � ��������� �� ������� ���������
      OW_Reset();
      
      OW_toBits(0xCC, ow_buf);
      
      DMA_InitTypeDef DMA_InitStructure;
      
      // DMA �� ������
      DMA_DeInit(OW_DMA_CH_RX);
      DMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t) &(USART2->DR);
      DMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t) ow_buf;
      DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralSRC;
      DMA_InitStructure.DMA_BufferSize = 8;
      DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;
      DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;
      DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_Byte;
      DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_Byte;
      DMA_InitStructure.DMA_Mode = DMA_Mode_Normal;
      DMA_InitStructure.DMA_Priority = DMA_Priority_Low;
      DMA_InitStructure.DMA_M2M = DMA_M2M_Disable;
      DMA_Init(OW_DMA_CH_RX, &DMA_InitStructure);
      
      // DMA �� ������
      DMA_DeInit(OW_DMA_CH_TX);
      DMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t) &(USART2->DR);
      DMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t) ow_buf;
      DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralDST;
      DMA_InitStructure.DMA_BufferSize = 8;
      DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;
      DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;
      DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_Byte;
      DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_Byte;
      DMA_InitStructure.DMA_Mode = DMA_Mode_Normal;
      DMA_InitStructure.DMA_Priority = DMA_Priority_Low;
      DMA_InitStructure.DMA_M2M = DMA_M2M_Disable;
      DMA_Init(OW_DMA_CH_TX, &DMA_InitStructure);
      
      // ����� ����� ��������
      USART_ClearFlag(OW_USART, USART_FLAG_RXNE | USART_FLAG_TC | USART_FLAG_TXE);
      USART_DMACmd(OW_USART, USART_DMAReq_Tx | USART_DMAReq_Rx, ENABLE);
      DMA_Cmd(OW_DMA_CH_RX, ENABLE);
      DMA_Cmd(OW_DMA_CH_TX, ENABLE);
      
      TermReadEnable = 2;
    }
    else if (TermReadEnable==2) //�������� ������
    {
      if (DMA_GetFlagStatus(OW_DMA_FLAG) == RESET) TermReadEnable=3;
    }
    else if (TermReadEnable==3) //�������� ������
    {
      OW_toBits(0x44, ow_buf);
      
      DMA_InitTypeDef DMA_InitStructure;
      
      // DMA �� ������
      DMA_DeInit(OW_DMA_CH_RX);
      DMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t) &(USART2->DR);
      DMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t) ow_buf;
      DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralSRC;
      DMA_InitStructure.DMA_BufferSize = 8;
      DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;
      DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;
      DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_Byte;
      DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_Byte;
      DMA_InitStructure.DMA_Mode = DMA_Mode_Normal;
      DMA_InitStructure.DMA_Priority = DMA_Priority_Low;
      DMA_InitStructure.DMA_M2M = DMA_M2M_Disable;
      DMA_Init(OW_DMA_CH_RX, &DMA_InitStructure);
      
      // DMA �� ������
      DMA_DeInit(OW_DMA_CH_TX);
      DMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t) &(USART2->DR);
      DMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t) ow_buf;
      DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralDST;
      DMA_InitStructure.DMA_BufferSize = 8;
      DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;
      DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;
      DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_Byte;
      DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_Byte;
      DMA_InitStructure.DMA_Mode = DMA_Mode_Normal;
      DMA_InitStructure.DMA_Priority = DMA_Priority_Low;
      DMA_InitStructure.DMA_M2M = DMA_M2M_Disable;
      DMA_Init(OW_DMA_CH_TX, &DMA_InitStructure);
      
      // ����� ����� ��������
      USART_ClearFlag(OW_USART, USART_FLAG_RXNE | USART_FLAG_TC | USART_FLAG_TXE);
      USART_DMACmd(OW_USART, USART_DMAReq_Tx | USART_DMAReq_Rx, ENABLE);
      DMA_Cmd(OW_DMA_CH_RX, ENABLE);
      DMA_Cmd(OW_DMA_CH_TX, ENABLE);
      
      TermReadEnable=4;
    }
    else if (TermReadEnable==4) //�������� ������
    {
      if (DMA_GetFlagStatus(OW_DMA_FLAG) == RESET) TermReadEnable=5;
    }
    else if (TermReadEnable==5) //�������� ������
    {
      // ��������� DMA
      DMA_Cmd(OW_DMA_CH_TX, DISABLE);
      DMA_Cmd(OW_DMA_CH_RX, DISABLE);
      USART_DMACmd(OW_USART, USART_DMAReq_Tx | USART_DMAReq_Rx, DISABLE);
      TermReadEnable = 6; //on TIMER ++TermReadEnable;
    }
    else if (TermReadEnable==8)
    {
      //������ ������
      //OW_Send(OW_SEND_RESET, "\xcc\xbe\xff\xff", 4, buf,2, 2);

      //�������� ������ � ���
      
      // ���� ��������� ����� - ���������� � ��������� �� ������� ���������
      OW_Reset();
      
      OW_toBits(0xCC, ow_buf);
      
      DMA_InitTypeDef DMA_InitStructure;
      
      // DMA �� ������
      DMA_DeInit(OW_DMA_CH_RX);
      DMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t) &(USART2->DR);
      DMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t) ow_buf;
      DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralSRC;
      DMA_InitStructure.DMA_BufferSize = 8;
      DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;
      DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;
      DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_Byte;
      DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_Byte;
      DMA_InitStructure.DMA_Mode = DMA_Mode_Normal;
      DMA_InitStructure.DMA_Priority = DMA_Priority_Low;
      DMA_InitStructure.DMA_M2M = DMA_M2M_Disable;
      DMA_Init(OW_DMA_CH_RX, &DMA_InitStructure);
      
      // DMA �� ������
      DMA_DeInit(OW_DMA_CH_TX);
      DMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t) &(USART2->DR);
      DMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t) ow_buf;
      DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralDST;
      DMA_InitStructure.DMA_BufferSize = 8;
      DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;
      DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;
      DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_Byte;
      DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_Byte;
      DMA_InitStructure.DMA_Mode = DMA_Mode_Normal;
      DMA_InitStructure.DMA_Priority = DMA_Priority_Low;
      DMA_InitStructure.DMA_M2M = DMA_M2M_Disable;
      DMA_Init(OW_DMA_CH_TX, &DMA_InitStructure);
      
      // ����� ����� ��������
      USART_ClearFlag(OW_USART, USART_FLAG_RXNE | USART_FLAG_TC | USART_FLAG_TXE);
      USART_DMACmd(OW_USART, USART_DMAReq_Tx | USART_DMAReq_Rx, ENABLE);
      DMA_Cmd(OW_DMA_CH_RX, ENABLE);
      DMA_Cmd(OW_DMA_CH_TX, ENABLE);
      
      TermReadEnable = 9;
    }
    else if (TermReadEnable==9) //�������� ������
    {
      if (DMA_GetFlagStatus(OW_DMA_FLAG) == RESET) TermReadEnable=10;
    }
    if (TermReadEnable==10)
    {
      //�������� ������ � ���
      
      // ���� ��������� ����� - ���������� � ��������� �� ������� ���������
      OW_Reset();
      
      OW_toBits(0xBE, ow_buf);
      
      DMA_InitTypeDef DMA_InitStructure;
      
      // DMA �� ������
      DMA_DeInit(OW_DMA_CH_RX);
      DMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t) &(USART2->DR);
      DMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t) ow_buf;
      DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralSRC;
      DMA_InitStructure.DMA_BufferSize = 8;
      DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;
      DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;
      DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_Byte;
      DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_Byte;
      DMA_InitStructure.DMA_Mode = DMA_Mode_Normal;
      DMA_InitStructure.DMA_Priority = DMA_Priority_Low;
      DMA_InitStructure.DMA_M2M = DMA_M2M_Disable;
      DMA_Init(OW_DMA_CH_RX, &DMA_InitStructure);
      
      // DMA �� ������
      DMA_DeInit(OW_DMA_CH_TX);
      DMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t) &(USART2->DR);
      DMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t) ow_buf;
      DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralDST;
      DMA_InitStructure.DMA_BufferSize = 8;
      DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;
      DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;
      DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_Byte;
      DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_Byte;
      DMA_InitStructure.DMA_Mode = DMA_Mode_Normal;
      DMA_InitStructure.DMA_Priority = DMA_Priority_Low;
      DMA_InitStructure.DMA_M2M = DMA_M2M_Disable;
      DMA_Init(OW_DMA_CH_TX, &DMA_InitStructure);
      
      // ����� ����� ��������
      USART_ClearFlag(OW_USART, USART_FLAG_RXNE | USART_FLAG_TC | USART_FLAG_TXE);
      USART_DMACmd(OW_USART, USART_DMAReq_Tx | USART_DMAReq_Rx, ENABLE);
      DMA_Cmd(OW_DMA_CH_RX, ENABLE);
      DMA_Cmd(OW_DMA_CH_TX, ENABLE);
      
      TermReadEnable = 11;
    }
    else if (TermReadEnable==11) //�������� ������
    {
      if (DMA_GetFlagStatus(OW_DMA_FLAG) == RESET) TermReadEnable=12;
    }
    if (TermReadEnable==12)
    {
      //�������� ������ � ���
      
      // ���� ��������� ����� - ���������� � ��������� �� ������� ���������
      OW_Reset();
      
      OW_toBits(0xFF, ow_buf);
      
      DMA_InitTypeDef DMA_InitStructure;
      
      // DMA �� ������
      DMA_DeInit(OW_DMA_CH_RX);
      DMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t) &(USART2->DR);
      DMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t) ow_buf;
      DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralSRC;
      DMA_InitStructure.DMA_BufferSize = 8;
      DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;
      DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;
      DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_Byte;
      DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_Byte;
      DMA_InitStructure.DMA_Mode = DMA_Mode_Normal;
      DMA_InitStructure.DMA_Priority = DMA_Priority_Low;
      DMA_InitStructure.DMA_M2M = DMA_M2M_Disable;
      DMA_Init(OW_DMA_CH_RX, &DMA_InitStructure);
      
      // DMA �� ������
      DMA_DeInit(OW_DMA_CH_TX);
      DMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t) &(USART2->DR);
      DMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t) ow_buf;
      DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralDST;
      DMA_InitStructure.DMA_BufferSize = 8;
      DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;
      DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;
      DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_Byte;
      DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_Byte;
      DMA_InitStructure.DMA_Mode = DMA_Mode_Normal;
      DMA_InitStructure.DMA_Priority = DMA_Priority_Low;
      DMA_InitStructure.DMA_M2M = DMA_M2M_Disable;
      DMA_Init(OW_DMA_CH_TX, &DMA_InitStructure);
      
      // ����� ����� ��������
      USART_ClearFlag(OW_USART, USART_FLAG_RXNE | USART_FLAG_TC | USART_FLAG_TXE);
      USART_DMACmd(OW_USART, USART_DMAReq_Tx | USART_DMAReq_Rx, ENABLE);
      DMA_Cmd(OW_DMA_CH_RX, ENABLE);
      DMA_Cmd(OW_DMA_CH_TX, ENABLE);
      
      TermReadEnable = 13;
    }
    else if (TermReadEnable==13) //�������� ������
    {
      if (DMA_GetFlagStatus(OW_DMA_FLAG) == RESET) TermReadEnable=14;
    }
    if (TermReadEnable==14)
    {
      //�������� ������ � ���
      
      // ���� ��������� ����� - ���������� � ��������� �� ������� ���������
      OW_Reset();
      
      OW_toBits(0xFF, ow_buf);
      
      DMA_InitTypeDef DMA_InitStructure;
      
      // DMA �� ������
      DMA_DeInit(OW_DMA_CH_RX);
      DMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t) &(USART2->DR);
      DMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t) ow_buf;
      DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralSRC;
      DMA_InitStructure.DMA_BufferSize = 8;
      DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;
      DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;
      DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_Byte;
      DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_Byte;
      DMA_InitStructure.DMA_Mode = DMA_Mode_Normal;
      DMA_InitStructure.DMA_Priority = DMA_Priority_Low;
      DMA_InitStructure.DMA_M2M = DMA_M2M_Disable;
      DMA_Init(OW_DMA_CH_RX, &DMA_InitStructure);
      
      // DMA �� ������
      DMA_DeInit(OW_DMA_CH_TX);
      DMA_InitStructure.DMA_PeripheralBaseAddr = (uint32_t) &(USART2->DR);
      DMA_InitStructure.DMA_MemoryBaseAddr = (uint32_t) ow_buf;
      DMA_InitStructure.DMA_DIR = DMA_DIR_PeripheralDST;
      DMA_InitStructure.DMA_BufferSize = 8;
      DMA_InitStructure.DMA_PeripheralInc = DMA_PeripheralInc_Disable;
      DMA_InitStructure.DMA_MemoryInc = DMA_MemoryInc_Enable;
      DMA_InitStructure.DMA_PeripheralDataSize = DMA_PeripheralDataSize_Byte;
      DMA_InitStructure.DMA_MemoryDataSize = DMA_MemoryDataSize_Byte;
      DMA_InitStructure.DMA_Mode = DMA_Mode_Normal;
      DMA_InitStructure.DMA_Priority = DMA_Priority_Low;
      DMA_InitStructure.DMA_M2M = DMA_M2M_Disable;
      DMA_Init(OW_DMA_CH_TX, &DMA_InitStructure);
      
      // ����� ����� ��������
      USART_ClearFlag(OW_USART, USART_FLAG_RXNE | USART_FLAG_TC | USART_FLAG_TXE);
      USART_DMACmd(OW_USART, USART_DMAReq_Tx | USART_DMAReq_Rx, ENABLE);
      DMA_Cmd(OW_DMA_CH_RX, ENABLE);
      DMA_Cmd(OW_DMA_CH_TX, ENABLE);
      
      TermReadEnable = 15;
    }
    else if (TermReadEnable==15) //�������� ������
    {
      if (DMA_GetFlagStatus(OW_DMA_FLAG) == RESET) TermReadEnable=16;
    }    
    else if (TermReadEnable==16) //�������� ������
    {
      // ��������� DMA
      DMA_Cmd(OW_DMA_CH_TX, DISABLE);
      DMA_Cmd(OW_DMA_CH_RX, DISABLE);
      USART_DMACmd(OW_USART, USART_DMAReq_Tx | USART_DMAReq_Rx, DISABLE);
      
      
      //������ 2� ���� ������
      
      // ���� ����������� ������ ����-�� ����� - ������� �� � �����
      unsigned char ddLen=4;
		if (ddLen > 0) {
			*buf1 = OW_toByte(ow_buf);
			buf1++;
			ddLen--;
		}

      asm("NOP");
      TermReadEnable = 0;
    }    

}
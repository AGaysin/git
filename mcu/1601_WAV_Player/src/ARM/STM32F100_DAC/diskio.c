/*-----------------------------------------------------------------------*/
/* PFF - Low level disk control module for ATtiny85     (C)ChaN, 2009    */
/*-----------------------------------------------------------------------*/

#include "diskio.h"
#include "stm32f10x.h"

unsigned int Stream1[ARRAY_SIZE+1], Stream2[ARRAY_SIZE+1];
unsigned int PlayPtr, changeStream, interrupt;
unsigned int str1play, str2play;
unsigned int str1write, str2write;

volatile unsigned char FLAG;

/* Definitions for MMC/SDC command */
#define CMD0	(0x40+0)	/* GO_IDLE_STATE */
#define CMD1	(0x40+1)	/* SEND_OP_COND (MMC) */
#define	ACMD41	(0x40+41)	/* SEND_OP_COND (SDC) */
#define CMD8	(0x40+8)	/* SEND_IF_COND */
#define CMD9	(0x40+9)	/* SEND_CSD */
#define CMD10	(0x40+10)	/* SEND_CID */
#define CMD12	(0x40+12)	/* STOP_TRANSMISSION */
#define ACMD13	(0x40+13)	/* SD_STATUS (SDC) */
#define CMD16	(0x40+16)	/* SET_BLOCKLEN */
#define CMD17	(0x40+17)	/* READ_SINGLE_BLOCK */
#define CMD18	(0x40+18)	/* READ_MULTIPLE_BLOCK */
#define CMD23	(0x40+23)	/* SET_BLOCK_COUNT (MMC) */
#define	ACMD23	(0x40+23)	/* SET_WR_BLK_ERASE_COUNT (SDC) */
#define CMD24	(0x40+24)	/* WRITE_BLOCK */
#define CMD25	(0x40+25)	/* WRITE_MULTIPLE_BLOCK */
#define CMD55	(0x40+55)	/* APP_CMD */
#define CMD58	(0x40+58)	/* READ_OCR */


/* Port Controls  (Platform dependent) */
#define SELECT()	GPIO_ResetBits(GPIOB, GPIO_Pin_0);	/* PB3: MMC CS = L */
#define	DESELECT()	GPIO_SetBits(GPIOB, GPIO_Pin_0);	/* PB3: MMC CS = H */


/*--------------------------------------------------------------------------

   Module Private Functions

---------------------------------------------------------------------------*/

BYTE CardType;



/*-----------------------------------------------------------------------*/
/* Wait for card ready                                                   */
/*-----------------------------------------------------------------------*/

BYTE wait_ready (void)
{
	BYTE res;
	WORD tmr;


	rcv_spi();
	tmr = 2500;
	do
        {
		res = rcv_spi();
                tmr--;
        }
	while (res != 0xFF && tmr);

	return res;
}



/*-----------------------------------------------------------------------*/
/* Deselect the card and release SPI bus                                 */
/*-----------------------------------------------------------------------*/

void release_spi (void)
{
	DESELECT();
	rcv_spi();
}


/*-----------------------------------------------------------------------*/
/* Send a command packet to MMC                                          */
/*-----------------------------------------------------------------------*/

BYTE send_cmd (
	BYTE cmd,		/* Command byte */
	DWORD arg		/* Argument */
)
{
	BYTE n, res;


	if (cmd & 0x80) {	/* ACMD<n> is the command sequense of CMD55-CMD<n> */
		cmd &= 0x7F;
		res = send_cmd(CMD55, 0);
		if (res > 1) return res;
	}

	/* Select the card and wait for ready */
	DESELECT();
	SELECT();
	if (wait_ready() != 0xFF) return 0xFF;

	/* Send command packet */
	xmit_spi(cmd);						/* Start + Command index */
	xmit_spi((BYTE)(arg >> 24));		/* Argument[31..24] */
	xmit_spi((BYTE)(arg >> 16));		/* Argument[23..16] */
	xmit_spi((BYTE)(arg >> 8));			/* Argument[15..8] */
	xmit_spi((BYTE)arg);				/* Argument[7..0] */
	n = 0x01;							/* Dummy CRC + Stop */
	if (cmd == CMD0) n = 0x95;			/* Valid CRC for CMD0(0) */
	if (cmd == CMD8) n = 0x87;			/* Valid CRC for CMD8(0x1AA) */
	xmit_spi(n);

	/* Receive command response */
	n = 10;								/* Wait for a valid response in timeout of 10 attempts */
	do {
		res = rcv_spi();
	} while ((res & 0x80) && --n);

	return res;			/* Return with the response value */
}


BYTE send_acmd (
	BYTE cmd,		/* Command byte */
	DWORD arg		/* Argument */
)
{
	BYTE n, res;


	if (wait_ready() != 0xFF) return 0xFF;
        DESELECT();
	SELECT();
        
	/* Send command packet */
	xmit_spi(CMD55);						/* Start + Command index */
	xmit_spi(0);		/* Argument[31..24] */
	xmit_spi(0);		/* Argument[23..16] */
	xmit_spi(0);			/* Argument[15..8] */
	xmit_spi(0);				/* Argument[7..0] */						
	xmit_spi(0xFF);

	/* Receive command response */
	n = 10;								
        /* Wait for a valid response in timeout of 10 attempts */
        res = 0xFF;
	while ((res == 0xFF) && n--) res = rcv_spi();

	/* Select the card and wait for ready */
	DESELECT();
	SELECT();
	if (wait_ready() != 0xFF) return 0xFF;

	/* Send command packet */
	xmit_spi(cmd);						/* Start + Command index */
	xmit_spi((BYTE)(arg >> 24));		/* Argument[31..24] */
	xmit_spi((BYTE)(arg >> 16));		/* Argument[23..16] */
	xmit_spi((BYTE)(arg >> 8));			/* Argument[15..8] */
	xmit_spi((BYTE)arg);				/* Argument[7..0] */						
	xmit_spi(0xFF);

	/* Receive command response */
	n = 10;		
        res = 0xFF;
        /* Wait for a valid response in timeout of 10 attempts */
	while ((res == 0xFF) && n--) res = rcv_spi();

	return res;			/* Return with the response value */
}
/*--------------------------------------------------------------------------

   Public Functions

---------------------------------------------------------------------------*/

void delay_ms (unsigned int value){
  unsigned int ticks;
  while (value--) 
  {
    ticks=1535;
    while (ticks--)
      ;
  }
}

void delay_us(unsigned int value){
  while (value--) 
  {
    asm("NOP");
    asm("NOP");
  }
}
/*-----------------------------------------------------------------------*/
/* Initialize Disk Drive                                                 */
/*-----------------------------------------------------------------------*/

DSTATUS disk_initialize (void)
{
	BYTE n, cmd, ty, ocr[4];
	WORD tmr;


	//init_spi();		/* Initialize USI */
        DESELECT();
	for (tmr = 50; tmr; tmr--) rcv_spi();	/* Dummy clocks */
        SELECT();
        delay_ms(250);
	ty = 0;
	if (send_cmd(CMD0, 0) == 1) {			/* Enter Idle state */
		if (send_cmd(CMD8, 0x1AA) == 1) {	/* SDv2 */
			for (n = 0; n < 4; n++) ocr[n] = rcv_spi();		/* Get trailing return value of R7 resp */
			if (ocr[2] == 0x01 && ocr[3] == 0xAA) {				/* The card can work at vdd range of 2.7-3.6V */
				for (tmr = 25000; tmr && send_acmd(ACMD41, 1UL << 30); tmr--) ;	/* Wait for leaving idle state (ACMD41 with HCS bit) */
				if (tmr && send_cmd(CMD58, 0) == 0) {		/* Check CCS bit in the OCR */
					for (n = 0; n < 4; n++) ocr[n] = rcv_spi();
					ty = (ocr[0] & 0x40) ? CT_SD2 | CT_BLOCK : CT_SD2;	/* SDv2 */
				}
			}
		} else {							/* SDSC or MMC */
			if (send_cmd(ACMD41, 0) <= 1) 	{
				ty = CT_SD1; cmd = ACMD41;	/* SDv1 */
			} else {
				ty = CT_MMC; cmd = CMD1;	/* MMCv3 */
			}
			for (tmr = 25000; tmr && send_cmd(cmd, 0); tmr--) ;	/* Wait for leaving idle state */
			if (!tmr || send_cmd(CMD16, 512) != 0) {			/* Set R/W block length to 512 */
				ty = 0;
			}
		}
	}
	CardType = ty;
	release_spi();
        //init_spi_fast();
        TIM_DeInit(TIM6);
        

	return ty ? 0 : STA_NOINIT;
}



/*-----------------------------------------------------------------------*/
/* Read partial sector                                                   */
/*-----------------------------------------------------------------------*/

DRESULT disk_readp (BYTE* buff, DWORD sector, UINT ofs, UINT cnt)
{
	DRESULT res;
	BYTE rc;
	WORD tmr;

	//if (!(CardType & CT_BLOCK)) lba *= 512;		/* Convert LBA to BA if needed */

	res = RES_ERROR;
	if (send_cmd(CMD17, sector) == 0) {		/* READ_SINGLE_BLOCK */

		tmr = 1000;
		do {							/* Wait for data packet in timeout of 200ms */
			rc = rcv_spi();
		} while (rc == 0xFF && --tmr);

		if (rc == 0xFE) {
			read_blk_part(buff, ofs, cnt);
			res = RES_OK;
		}
	}

	release_spi();
	return res;
}

void read_blk_part(BYTE *buff, UINT ofs, UINT cnt)
{
  unsigned int templ;
	unsigned int num=514;
	if (buff == 0)
        {
          //fb_wave
          num -= ofs;
          while(ofs--)rcv_spi();
          num -= cnt;
          //rcv_spi();
          while(cnt) 
          {
            if (PlayPtr == 0 || PlayPtr == 2)
            {
              if (str1write >= ARRAY_SIZE)
              {
                str1write = 0;
                if (PlayPtr == 0) 
                {
                  //str1write = 0;
                  PlayPtr = 1;
                }
                else
                {
                  changeStream = 0;
                  while(!changeStream) asm("NOP");
                  changeStream = 0;
                }
              }
              else
              {
                
                templ = rcv_spi() | (rcv_spi()<<8);
                //GPIO_SetBits(GPIOB, GPIO_Pin_14);
                if (templ & 0x8000) templ -= 0x8000;
                else templ += 0x8000;
                Stream1[str1write++] = templ>>4;
                //GPIO_ResetBits(GPIOB, GPIO_Pin_14);
                if (cnt) cnt--;
                if (cnt) cnt--;
              }
            }
            else if (PlayPtr == 1)
            {
              if (str2write >= ARRAY_SIZE)
              {
                str2write = 0;
                changeStream = 0;
                while(!changeStream) asm("NOP");
                changeStream = 0;
              }
              else
              {
                //write to Stream 2
                
                templ = rcv_spi() | (rcv_spi()<<8);
                //GPIO_SetBits(GPIOB, GPIO_Pin_14);
                if (templ & 0x8000) templ -= 0x8000;
                else templ += 0x8000;
                Stream2[str2write++] = templ>>4;
                //GPIO_ResetBits(GPIOB, GPIO_Pin_14);
                if (cnt) cnt--;
                if (cnt) cnt--;
              }
            }
          }
          while(num--) rcv_spi();//test_buf[testi++]=rcv_spi();
          asm("NOP");
        }
        else
        {
          //fb_mem
          num -= ofs;
          while(ofs--)rcv_spi();
          num -= cnt;
          while(cnt--) *buff++=rcv_spi();
          while(num--) rcv_spi();
	}
	asm("NOP");
}


unsigned char rcv_spi(void){
  SPI_I2S_SendData(SPI1, 0xFF);
  while(SPI_I2S_GetFlagStatus(SPI1, SPI_I2S_FLAG_BSY) == SET) //���������� �����?
    ;
    return SPI_I2S_ReceiveData(SPI1); //������ �������� ������
}

void xmit_spi(unsigned char data){

  //while (SPI_I2S_GetFlagStatus(SPI2, SPI_I2S_FLAG_TXE) == RESET);
  SPI_I2S_SendData(SPI1, data);
  while(SPI_I2S_GetFlagStatus(SPI1, SPI_I2S_FLAG_BSY) == SET) //���������� �����?
    ;
}


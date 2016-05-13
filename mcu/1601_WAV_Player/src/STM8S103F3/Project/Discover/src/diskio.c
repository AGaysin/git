/*-----------------------------------------------------------------------*/
/* Low level disk I/O module skeleton for Petit FatFs (C)ChaN, 2014      */
/*-----------------------------------------------------------------------*/

#include "diskio.h"


/*-----------------------------------------------------------------------*/
/* Initialize Disk Drive                                                 */
/*-----------------------------------------------------------------------*/

DSTATUS disk_initialize (void)
{
	DSTATUS stat;

	// Put your code here

        BYTE n, cmd, ty, ocr[4];
	WORD tmr;


	init_spi();		/* Initialize USI */
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
        init_spi_fast();

	return ty ? 0 : STA_NOINIT;
}



/*-----------------------------------------------------------------------*/
/* Read Partial Sector                                                   */
/*-----------------------------------------------------------------------*/

DRESULT disk_readp (
	BYTE* buff,		/* Pointer to the destination object */
	DWORD sector,	/* Sector number (LBA) */
	UINT offset,	/* Offset in the sector */
	UINT count		/* Byte count (bit15:destination) */
)
{
	DRESULT res;

	// Put your code here

        BYTE rc;
	WORD tmr;

	if (!(CardType & CT_BLOCK)) lba *= 512;		/* Convert LBA to BA if needed */

	res = RES_ERROR;
	if (send_cmd(CMD17, lba) == 0) {		/* READ_SINGLE_BLOCK */

		tmr = 1000;
		do {							/* Wait for data packet in timeout of 200ms */
			rc = rcv_spi();
		} while (rc == 0xFF && --tmr);

		if (rc == 0xFE) {
			read_blk_part(dest, ofs, cnt);
			res = RES_OK;
		}
	}

	release_spi();
	return res;
}



/*-----------------------------------------------------------------------*/
/* Write Partial Sector                                                  */
/*-----------------------------------------------------------------------*/

DRESULT disk_writep (
	BYTE* buff,		/* Pointer to the data to be written, NULL:Initiate/Finalize write operation */
	DWORD sc		/* Sector number (LBA) or Number of bytes to send */
)
{
	DRESULT res;


	if (!buff) {
		if (sc) {

			// Initiate write process

		} else {

			// Finalize write process

		}
	} else {

		// Send data to the disk

	}

	return res;
}

void init_spi(void){
  
  SPI_DeInit();
  SPI_Init(SPI_FIRSTBIT_MSB, 
           SPI_BAUDRATEPRESCALER_32, 
           SPI_MODE_MASTER, 
           SPI_CLOCKPOLARITY_HIGH, 
           SPI_CLOCKPHASE_2EDGE, 
           SPI_DATADIRECTION_2LINES_FULLDUPLEX, 
           SPI_NSS_SOFT, 
           0x00);
  SPI_Cmd(ENABLE);
  
}

void init_spi_fast(void){
  
  SPI_DeInit();
  SPI_Init(SPI_FIRSTBIT_MSB, 
           SPI_BAUDRATEPRESCALER_2, 
           SPI_MODE_MASTER, 
           SPI_CLOCKPOLARITY_HIGH, 
           SPI_CLOCKPHASE_2EDGE, 
           SPI_DATADIRECTION_2LINES_FULLDUPLEX, 
           SPI_NSS_SOFT, 
           0x00);
  SPI_Cmd(ENABLE);
  
}

BYTE rcv_spi(void) {	
  /* Ждём очистки регистра DR */
  while ((SPI->SR & (u8)SPI_FLAG_TXE) == RESET) { ; }
 
  /* Послать байт */
  SPI->DR = 0xFF; 
 
  /* Ждём приёма байта */
  while ((SPI->SR & (u8)SPI_FLAG_RXNE) == RESET) { ; }
 
  /* Возвратить принятый байт */
  return (u8)SPI->DR; 
  
}


void xmit_spi(BYTE data) {
  //Ждем когда будет Tx Buffer Empty
  while(!(SPI->SR & (1<<1)))
    ;
  SPI->DR = data;
  while(!(SPI->SR & (1<<1)))
    ;
}

void read_blk_part(char *dest, WORD ofs, WORD cnt)
{
  unsigned int i=0,j=0;
  unsigned char buffer[512];
  for (i=0;i<ofs; i++) buffer[j++]=rcv_spi();
  for (i=0;i<cnt; i++) 
  {
    buffer[j] = rcv_spi();
    *dest++ = buffer[j];
    j++;
  }
  for (i=0; (i+cnt+ofs)<514; i++) buffer[j++]=rcv_spi();

  asm("NOP");
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
    ticks=1575;
    while (ticks--)
      ;
  }
}

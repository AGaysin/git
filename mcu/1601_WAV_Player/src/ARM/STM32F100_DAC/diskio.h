/*-----------------------------------------------------------------------
/  PFF - Low level disk interface modlue include file    (C)ChaN, 2014
/-----------------------------------------------------------------------*/
#define ARRAY_SIZE 256

extern unsigned int Stream1[ARRAY_SIZE+1], Stream2[ARRAY_SIZE+1];
extern unsigned int PlayPtr, changeStream, interrupt;
extern unsigned int str1play, str2play;


#ifndef _DISKIO_DEFINED
#define _DISKIO_DEFINED

#ifdef __cplusplus
extern "C" {
	#endif

	#include "integer.h"


	/* Status of Disk Functions */
	typedef BYTE	DSTATUS;


	/* Results of Disk Functions */
	typedef enum {
		RES_OK = 0,		/* 0: Function succeeded */
		RES_ERROR,		/* 1: Disk error */
		RES_NOTRDY,		/* 2: Not ready */
		RES_PARERR		/* 3: Invalid parameter */
	} DRESULT;





	/*---------------------------------------*/

	/* SPI control functions (defined in asmfunc.S) */
	void xmit_spi (BYTE);
	BYTE rcv_spi (void);
	void read_blk_part(BYTE *buff, UINT ofs, UINT cnt);
	BYTE send_acmd (BYTE cmd,DWORD arg);
	BYTE send_cmd (BYTE cmd,DWORD arg);
	BYTE wait_ready (void);
	void release_spi (void);
	void delay_ms (unsigned int value);
        void delay_us(unsigned int value);
	
	
	extern volatile unsigned char FLAG;


        
        



	/* Prototypes for disk control functions */

	DSTATUS disk_initialize (void);
	DRESULT disk_readp (BYTE* buff, DWORD sector, UINT offser, UINT count);

	#define STA_NOINIT		0x01	/* Drive not initialized */
	#define STA_NODISK		0x02	/* No medium in the drive */

	/* Card type flags (CardType) */
	#define CT_MMC				0x01	/* MMC ver 3 */
	#define CT_SD1				0x02	/* SD ver 1 */
	#define CT_SD2				0x04	/* SD ver 2 */
	#define CT_SDC				(CT_SD1|CT_SD2)	/* SD */
	#define CT_BLOCK			0x08	/* Block addressing */

	#ifdef __cplusplus
}
#endif

#endif	/* _DISKIO_DEFINED */

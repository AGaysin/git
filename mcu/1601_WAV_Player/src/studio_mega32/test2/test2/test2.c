#include<avr/io.h>
#define F_CPU 8000000
#include <util/delay.h>
//#include <avr/interrupt.h>
//#include <string.h>

#define SECONDS_TO_FORWARD 15
#define RS PD6
#define EN PD5
#define LCD_NIBBLE PORTC
#define LINE1 cmd(0x80)
#define LINE2 cmd(0xc0)
#define LEFT_SWITCH  PIND&(1<<2)
#define RIGHT_SWITCH PIND&(1<<3)
#define SWITCH_EVENT PIND&((1<<3)|(1<<2))
#define CS 4
#define RC5_LED PB0


#define SELECT()	PORTB &= ~(1<<4);	/* PB3: MMC CS = L */
#define	DESELECT()	PORTB = (1<<4);	/* PB3: MMC CS = H */


#define STA_NOINIT		0x01	/* Drive not initialized */
#define STA_NODISK		0x02	/* No medium in the drive */


unsigned char CardType;
/* Card type flags (CardType) */
#define CT_MMC				0x01	/* MMC ver 3 */
#define CT_SD1				0x02	/* SD ver 1 */
#define CT_SD2				0x04	/* SD ver 2 */
#define CT_SDC				(CT_SD1|CT_SD2)	/* SD */
#define CT_BLOCK			0x08	/* Block addressing */




unsigned char readdata;
unsigned int count;
unsigned long int arg = 0;
unsigned char mmc_buf[512];
unsigned char mmc_buf0[512];
unsigned char mmc_buf1[512];
unsigned int fat_start, dir_start, data_start;
unsigned char sect_per_clust;
volatile unsigned char BUF1_EMPTY, BUF0_EMPTY = 0;
unsigned long int OCR1A_BACKUP;
register int ISR_i asm("r2");
register char STEREO asm("r4");
register char TOGGLE_BUFFER asm("r5");
unsigned long int bitrate;
unsigned int RC5_DATA;
char RC5_FLAG;
unsigned int OCR1A_ADJUST;
unsigned int STARTING_CLUSTER;

void LCD_STROBE(void);
void data(unsigned char);
void cmd(unsigned char);
void clear(void);
void lcd_init();
void string(char *, char);
void spi_init();
void spi_write(char);
unsigned char spi_read();


unsigned char send_acmd(unsigned char cmd, unsigned long int arg, unsigned char crc);
unsigned char send_cmd(unsigned char cmd, unsigned long int arg, unsigned char crc);
unsigned char wait_ready (void);
void release_spi (void);


char mmc_init();
void mmc_read_sector(unsigned long int);
void fat16_init();
void print_num(unsigned long int, char);
unsigned int scan_root_dir(unsigned char *,char [], char);
void play_cluster(unsigned int);
unsigned int find_next_cluster(unsigned int);
void pwm_init();
void mmc_read_double_buffer(unsigned long int, unsigned char []);

char check_bitrate_and_stereo(unsigned int);
void INT2_init();
unsigned int forward_seconds(unsigned int);
void pull_up_enable();
void init_RC5_valid_indicator_LED();


void main()
{
	unsigned char fname[12];
	unsigned int cluster;
	char NEXT_OR_PREVIOUS = 1;
	
	_delay_ms(50);
	spi_init();
	//lcd_init();
	pull_up_enable();
	DDRB |= (1<<3) | (1<<1) | (1<<0);
	PORTB &= ~3;
	PORTB |= (1<<3);
	while(mmc_init());
	pwm_init();
	fat16_init();
	while(1)
	{
		asm("NOP");
		_delay_ms(500);
	}
	
	//INT2_init();
	//init_RC5_valid_indicator_LED();
	while(1)
	{

		delay_ms(500);
	}
}





unsigned int forward_seconds(unsigned int cluster)
{
	cli();
	unsigned long int clusters_to_forward;
	clusters_to_forward = ((bitrate / 512) * SECONDS_TO_FORWARD) / sect_per_clust;
	while(clusters_to_forward) {
		cluster = find_next_cluster(cluster);
		if(cluster == 0xffff) break;
		clusters_to_forward--;
	}
	sei();
	return cluster;
}



char check_bitrate_and_stereo(unsigned int cluster)
{
	int i;
	mmc_read_sector(((unsigned long int)(cluster -2) * sect_per_clust) + data_start);
	if(mmc_buf[34] != 8) return 1;
	for (i = 31; i > 27; i--) {
		bitrate <<= 8;
		bitrate |= mmc_buf[i];
	}
	STEREO = mmc_buf[22] - 1;
	print_num(bitrate,2);
	OCR1A_BACKUP = ((F_CPU *(STEREO + 1))/bitrate);
	OCR1A = OCR1A_ADJUST = OCR1A_BACKUP;
	return 0;
}

unsigned int find_next_cluster(unsigned int cluster)
{
	static unsigned int cluster_index_in_buff;
	static unsigned int return_cluster;
	if((return_cluster + 1) == cluster) {
		if(cluster_index_in_buff+=2 < 512) {
			return_cluster += 1;
			return return_cluster;
			} else {
			cluster_index_in_buff-=2;
		}
	}
	cluster_index_in_buff = (2 * (cluster % 256));
	mmc_read_sector(fat_start + cluster/256);
	return_cluster = ((mmc_buf[cluster_index_in_buff + 1] << 8) + mmc_buf[cluster_index_in_buff]);
}



void mmc_read_double_buffer(unsigned long int sector, unsigned char a[])
{
	int i;
	sector *= 512;
	command(17, sector, 0xff);
	while (spi_read() != 0);
	while (spi_read() != 0xfe);
	for(i = 0; i < 512; i++)
	a[i] = spi_read();
	spi_write(0xff);
	spi_write(0xff);
}

void play_cluster(unsigned int cluster)
{
	unsigned long int sector;
	int i, j;
	sector = ((unsigned long int)(cluster -2) * sect_per_clust);
	sector += data_start;
	for(i = 0; i < sect_per_clust; i++) {
		while((!BUF1_EMPTY) && (!BUF0_EMPTY));
		if(BUF0_EMPTY) {
			mmc_read_double_buffer(sector, mmc_buf0);
			BUF0_EMPTY = 0;
			} else if(BUF1_EMPTY) {
		mmc_read_double_buffer(sector, mmc_buf1); BUF1_EMPTY = 0;}
		sector += 1;
	}
}



unsigned int scan_root_dir(unsigned char *FILE_EXTENSION, char FNAME[], char UP_DOWN)
{
	while(1) {
		unsigned int i;
		static unsigned char read_end = 0;
		static int base_count = -32, sect_plus = 0;
		if(UP_DOWN == 1) {
			base_count += 32;
			if(base_count == 512) {base_count = 0; sect_plus += 1;};
			} else {
			base_count -= 32;
			if(base_count == -32) {base_count = (512 - 32); sect_plus -= 1;}
			if(sect_plus < 0) {sect_plus = 0; base_count = 0;}
		}
		while(1) {
			mmc_read_sector(dir_start + sect_plus);
			while(base_count < 512) {
				if(mmc_buf[base_count] == 0) { read_end = 1; break;}
				if ((mmc_buf[1] != 0) && (mmc_buf[base_count + 2] != 0) && (mmc_buf[base_count] != 0xe5) && (mmc_buf[base_count] != 0x00) && ((mmc_buf[base_count + 11] & 0b00011110) == 0) && (strncmp(mmc_buf + base_count + 8, FILE_EXTENSION, 3) == 0)) {
					for(i = 0; i < 11; i++)
					FNAME[i] = mmc_buf[base_count + i];
					FNAME[11] = 0;
					return (STARTING_CLUSTER = (unsigned int)((mmc_buf[27 + base_count] << 8) + mmc_buf[26 + base_count]));
				}
				if(UP_DOWN) base_count += 32;
				else base_count -= 32;
			}
			base_count = 0;
			sect_plus++;
			if(read_end) { base_count = -32; sect_plus = 0; read_end = 0; return 0;}
		}
	}
}

void print_num(unsigned long int i, char line)
{
	char u = 0;
	unsigned char lcd_buf[16];
	if(line == 1) cmd(0x80);
	else cmd(0xc0);
	while(i) {
		lcd_buf[u++] = (i % 10 + '0');
		i /= 10;
	}
	while(u) data(lcd_buf[--u]);
}

void fat16_init()            //BOOT SECTOR SCANNING//
{
	mmc_read_sector(0);
	clear();
	LINE1;
	if((mmc_buf[0x36] == 'F') && (mmc_buf[0x39] == '1') && (mmc_buf[0x3a] == '6'))
	string("FAT16 DETECTED",1);
	else {
		string("NOT A FAT16",1);
		while(1);
	}
	_delay_ms(500);
	fat_start = mmc_buf[0x0e];
	dir_start = (fat_start + (((mmc_buf[0x17] << 8) + mmc_buf[0x16]) * 2));
	data_start = (dir_start + ((((mmc_buf[0x12] << 8) + (mmc_buf[0x11])) * 32) / 512));
	sect_per_clust = mmc_buf[0x0d];
}

void mmc_read_sector(unsigned long int sector)
{
	int i;
	
	sector *= 512;
	command(17, sector, 0xff);
	while (spi_read() != 0);
	while (spi_read() != 0xfe);
	for(i = 0; i < 512; i++)
	mmc_buf[i] = spi_read();
	spi_write(0xff);
	spi_write(0xff);
}

void release_spi (void)
{
	DESELECT();
	spi_read();
}

char mmc_init()
{
	int u = 0;
	
	unsigned char n, cmd, ty, ocr[4];
	unsigned int tmr;


	spi_init();		/* Initialize USI */
	DESELECT();
	for (tmr = 50; tmr; tmr--) spi_read();	/* Dummy clocks */
	SELECT();
	_delay_ms(250);
	ty = 0;
	if (send_cmd(0, 0, 1) == 1) {
		/* Enter Idle state */
		if (send_cmd(8, 0x1AA, 1) == 1) {
			/* SDv2 */
			for (n = 0; n < 4; n++) ocr[n] = spi_read();
			/* Get trailing return value of R7 resp */
			if (ocr[2] == 0x01 && ocr[3] == 0xAA) {
				/* The card can work at vdd range of 2.7-3.6V */
				for (tmr = 25000; tmr && send_acmd(41, 1UL << 30, 1); tmr--) ;	/* Wait for leaving idle state (ACMD41 with HCS bit) */
				if (tmr && send_cmd(58, 0, 1) == 0) {
					// Check CCS bit in the OCR
					for (n = 0; n < 4; n++) ocr[n] = spi_read();
					ty = (ocr[0] & 0x40) ? CT_SD2 | CT_BLOCK : CT_SD2;
					// SDv2
				}
			}
			} else {
			// SDSC or MMC
			if (send_acmd(41, 0, 1) <= 1) 	{
				ty = CT_SD1;
				// SDv1
				for (tmr = 25000; tmr && send_acmd(41, 0, 1); tmr--) ;
				} else {
				ty = CT_MMC;
				// MMCv3
				for (tmr = 25000; tmr && send_cmd(1, 0, 1); tmr--) ;
			}
			
			// Set R/W block length to 512
			if (!tmr || send_cmd(16, 512, 1) != 0) {
				ty = 0;
			}
		}
	}
	CardType = ty;
	release_spi();
	
	
	
	
	
	PORTB |= 1<<CS;
	for (u = 0; u < 50; u++) {
		spi_write(0xff);
	}
	PORTB &= ~(1<<CS);
	_delay_ms(1);
	command(0, 0, 0x95);
	count = 0;
	while ((spi_read() != 1) && (count < 1000))
	count++;
	if (count >= 1000) {
		string("CARD ERROR-CMD0 ",1);
		_delay_ms(500);
		return 1;
	}
	command(1, 0, 0xff);
	count = 0;
	while ((spi_read() != 0) && (count < 1000)) {
		command(1, 0, 0xff);
		count++;
	}
	if (count >= 1000) {
		string("CARD ERROR-CMD1 ",1);
		_delay_ms(500);
		return 1;
	}
	command(16, 512, 0xff);
	count = 0;
	while ((spi_read() != 0) && (count < 1000))
	count++;
	if (count >= 1000) {
		string("CARD ERROR-CMD16",1);
		_delay_ms(500);
		return 1;
	}
	string("MMC INITIALIZED!",1);
	_delay_ms(500);
	SPCR &= ~(1<<SPR1); //increase SPI clock from f/32 to f/2
	return 0;
}

unsigned char wait_ready (void)
{
	unsigned char res;
	unsigned int tmr;


	spi_read();
	tmr = 2500;
	do
	res = spi_read();
	while (res != 0xFF && tmr);

	return res;
}

unsigned char send_cmd(unsigned char cmd, unsigned long int arg, unsigned char crc)
{
	unsigned char n, res;

	
	DESELECT();
	SELECT();
	if (wait_ready() != 0xFF) return 0xFF;

	
	spi_write(0x40 | cmd);
	spi_write((unsigned char)(arg >> 24));
	spi_write((unsigned char)(arg >> 16));
	spi_write((unsigned char)(arg >> 8));
	spi_write((unsigned char)arg);
	spi_write(crc);

	
	n = 10;
	do {
		res = spi_read();
	} while ((res & 0x80) && --n);

	return res;
}

unsigned char send_acmd(unsigned char cmd, unsigned long int arg, unsigned char crc)
{
	unsigned char n, res;
	
	//CMD55
	DESELECT();
	SELECT();
	if (wait_ready() != 0xFF) return 0xFF;

	
	spi_write(0x50 | 55);
	spi_write(0);
	spi_write(0);
	spi_write(0);
	spi_write(0);
	spi_write(1);

	
	n = 10;
	do {
		res = spi_read();
	} while ((res & 0x80) && --n);

	if (res>1) return res;


	//CMD
	DESELECT();
	SELECT();
	if (wait_ready() != 0xFF) return 0xFF;

	
	spi_write(0x40 | cmd);
	spi_write((unsigned char)(arg >> 24));
	spi_write((unsigned char)(arg >> 16));
	spi_write((unsigned char)(arg >> 8));
	spi_write((unsigned char)arg);
	spi_write(crc);

	
	n = 10;
	do {
		res = spi_read();
	} while ((res & 0x80) && --n);

	return res;
}

unsigned char spi_read()
{
	SPDR = 0xff;
	while(!(SPSR & (1<<SPIF)));
	return SPDR;
}

void spi_write(char cData)
{
	SPDR = cData;
	while(!(SPSR & (1<<SPIF)));
}

void spi_init()
{
	DDRB |= (1<<5)|(1<<7)|(1<<4);
	SPCR = (1<<SPE)|(1<<MSTR)|(1<<CPOL)|(1<<CPHA)|(1<<SPR1);
	SPSR = 1;
}

void LCD_STROBE(void)
{
	PORTD |= (1 << EN);
	_delay_us(1);
	PORTD &= ~(1 << EN);
}

void data(unsigned char c)
{
	PORTD |= (1 << RS);
	_delay_us(50);
	LCD_NIBBLE = (c >> 4);
	LCD_STROBE();
	LCD_NIBBLE = (c);
	LCD_STROBE();
}

void cmd(unsigned char c)
{
	PORTD &= ~(1 << RS);
	_delay_us(50);
	LCD_NIBBLE = (c >> 4);
	LCD_STROBE();
	LCD_NIBBLE = (c);
	LCD_STROBE();
}

void clear(void)
{
	cmd(0x01);
	_delay_ms(5);
}

void lcd_init()
{
	DDRC = 0x0f;
	DDRD |= (1 << RS)|(1 << EN);
	_delay_ms(15);
	cmd(0x30);
	_delay_ms(1);
	cmd(0x30);
	_delay_us(100);
	cmd(0x30);
	cmd(0x28);
	cmd(0x28);
	cmd(0x0c);
	clear();
	cmd(0x6);
}

void string(char *p, char line)
{
	if(line == 1) LINE1;
	else LINE2;
	while(*p) data(*p++);
}

void pwm_init()
{
	TCCR0|=(1<<WGM00)|(1<<WGM01)|(1<<COM01)|(1<<CS00);
	TCCR2|=(1<<WGM20)|(1<<WGM21)|(1<<COM21)|(1<<CS20);
	//DDRB|=(1<<PB3);
	DDRD|=(1<<PD7);
}

void pull_up_enable()
{
	PORTB |= (1<<PB2);
}
void init_RC5_valid_indicator_LED()
{
	DDRB |= (1<<RC5_LED);
}

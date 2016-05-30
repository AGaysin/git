/*******************************************************************************
File:			Main.c

				Main Video Overlay function library.

Functions:	extern int		main(void)
				SIGNAL(SIG_OVERFLOW0)
				SIGNAL(SIG_INTERRUPT0)
				SIGNAL(SIG_INTERRUPT1)

Created:		1.00	12/12/04	GND	Gary Dion

Revisions:	1.01	12/15/04	GND	Improved screen font and added background

Copyright(c)	2004, Gary N. Dion (me@garydion.com). All rights reserved.
					This software is available only for non-commercial amateur radio
					or educational applications.  ALL other uses are prohibited.
					This software may be modified only if the resulting code be
					made available publicly and the original author(s) given credit.

*******************************************************************************/

/* OS headers */
#include <avr/interrupt.h>
#include <avr/sleep.h>
#include <avr/signal.h>
#include <avr/io.h>

/* General purpose include files */
#include "StdDefines.h"

/* App required include files. */
#include "Main.h"

#define WatchdogReset() asm("wdr")	/* Macro substitution to kick the dog		*/
#define Wait() while(!(SPSR & (1<<SPIF)))

/* Static functions and variables */

unsigned int line;						/* State of Delay function						*/
unsigned char print_line;				/* State of Delay function						*/
unsigned char maindelay;				/* State of mainDelay function				*/
unsigned int ltemp;						/* State of Delay function						*/
unsigned int ntemp;						/* State of Delay function						*/


unsigned char sleep_cmd=0;

unsigned char ltrs_inv[189];
/*									' '  A   B   C   D   E   F   G   H   I   J   K   L   M   N   O   P   Q   R   S   T   U   V   W   X   Y   Z  */
unsigned char ltrs[189] =		{255,231,131,195,135,129,129,195,189,131,129,189,191,125,125,195,131,195,131,195,131,189,189,125,125,125,  1,
									255,219,189,189,187,191,191,189,189,239,247,187,191, 57, 61,189,189,189,189,189,239,189,189,125,187,187,251,
									255,189,189,191,189,191,191,191,189,239,247,183,191, 85, 93,189,189,189,189,191,239,189,189,109,215,215,247,
									255,189,131,191,189,131,131,177,129,239,247,143,191,109,109,189,131,189,131,195,239,189,219,109,239,239,239,
									255,129,189,191,189,191,191,189,189,239,247,183,191,125,117,189,191,179,183,253,239,189,219,109,215,239,223,
									255,189,189,189,187,191,191,189,189,239,183,187,191,125,121,189,191,185,187,189,239,189,219,147,187,239,191,
									255,189,131,195,135,129,191,195,189,131,207,189,129,125,125,195,191,205,189,195,239,195,231,147,125,239,  1};

unsigned char nums_inv[98];
/*									-   .   /   0   1   2   3   4   5   6   7   8   9   :	!  */
unsigned char nums[98] = {
	255,255,0x7F,131,239,131,131,227,  1,131,  1,131,131,255,
	255,255,0x3F,125,207,125,125,219,127,127,253,125,125,239,
	255,255,0x1F,125,239,253,253,187,127,127,251,125,125,255,
	131,255,0x0F,125,239,243,227,123,131,  3,247,131,129,255,
	255,255,0x1F,125,239,207,253,  1,253,125,239,125,251,255,
	255,231,0x3F,125,239,191,125,251,125,125,239,125,247,239,
	255,231,0x7F,131,199,  1,131,251,131,131,239,131,207,255};
								  
		
unsigned char miganie_enable;

unsigned char sirena_en=0;
unsigned char play1_en=0;
unsigned char play2_en=0;
unsigned char relay=0;
unsigned char next2=0;
unsigned char nextS=0;
unsigned char play_s1=0, play_s2=0, next_s1=0, next_s2=0, nosdcard=0;


#define led_on PORTD &= ~(1<<4);
#define led_off PORTD |= (1<<4);

#define cam_on	PORTA |= (1<<0);
#define cam_off	PORTA &= ~(1<<0);
#define black_on PORTD |= (1<<5);
#define black_off PORTD &= ~(1<<5);
unsigned int temp_miganie=0;

unsigned char read_reg;
/*$FUNCTION$*******************************************************************/
extern int		main(void)
/*******************************************************************************
* ABSTRACT:	Main program entry function.
*
* INPUT:		None
* OUTPUT:	None
*/
{
	
	
	unsigned char temp;
	
	for (temp=0; temp<188; temp++) ltrs_inv[temp]=0xFF - ltrs[temp];
	//for (temp=0; temp<97; temp++) nums_inv[temp]=0xFF - nums[temp];
	//for (temp=0; temp<97; temp++) nums_inv[temp]=0xFF - nums[temp];
//	static unsigned short loop;			/* Generic loop variable					*/

/*Initialize serial communication functions */

/* PORT D - unused right now	*/
	PORTD = 0x00;							/* Initial state is both LEDs off			*/
	DDRD  = 0x00;							/* Data direction register for port D		*/
DDRD |= (1<<4);
DDRD |= (1<<5);//black
/* Initialize the Analog Comparator */
	//SFIOR = 0;								/* Select AIN1 as neg. input					*/
	//ACSR = (1<<ACBG) | (1<<ACIE);		/* Select Bandgap for pos. input				*/
DDRA |= (1<<0);
PORTA |= (1<<7);
//PORTA |= (1<<4) | (1<<5) | (1<<6);


/*	Initialize the 8-bit Timer0 to clock at 1.8432 MHz */
	TCCR0 = 0x01; 							/* Timer0 clock prescale of 8					*/

/* Use the 16-bit Timer1 to measure frequency; set it to clock at 1.8432 MHz	*/
	TCCR1B = 0x02;							/* Timer2 clock prescale of 8					*/

/*	Initialize the 8-bit Timer2 to clock at 1.8432 MHz */
	TCCR2 = 0x07; 							/* Timer2 clock prescale of 1024				*/

/* Initialize the Serial Peripheral Interface											*/
	PORTB = 0x00;
	DDRB = 0xFF;							/* Initial state is both LEDs off			*/
	




	SPCR = /* (1<<SPIE) | */ (1<<SPE) | (1<<MSTR) | (1<<CPHA);
	SPSR = 1;

/* Enable Timer interrupts	*/
//	TIMSK = 1<<TOIE0;

/* Enable the watchdog timer */ 
	MCUCR	= (1<<ISC00) + (1<<ISC11);	/* Set interrupt on falling edge 			*/
	GICR	= (1<<INT0) + (1<<INT1);	/* Enable interrupt for interrupt0			*/

/* Enable the watchdog timer */
	WDTCR	= (1<<WDTOE) | (1<<WDE);		/* Wake-up the watchdog register				*/
	WDTCR	= (1<<WDE) | 7;				/* Enable and timeout around 2.1s			*/





/* Enable interrupts */
	sei();



/* Reset watchdog */
	WatchdogReset();
cam_off;

	set_sleep_mode(SLEEP_MODE_IDLE);

/* Initialization complete - system ready.  Run program loop indefinitely. */
	while (TRUE)
	{
		if (sleep_cmd)
		{
			
			
			sleep_cmd=0;
			if (temp_miganie==1600) temp_miganie=0;
			
			//Реле замкнуто
			read_reg = PINA>>1;
			read_reg &= 0x0F;
			
			play1_en=0;
			relay=0;
			next2=0;
			play2_en=0;
			nextS=0;
			sirena_en=0;
			play_s1 = 0;
			play_s2 = 0;
			next_s1 = 0;
			next_s2 = 0;
			nosdcard=0;
					
					//read_reg=1;
					
			switch (read_reg)
			{//(temp_miganie/25)%2;
				case 0:
					break;
				case 1:
					play1_en=1;
					break;
				case 2:
					play2_en=1;
					break;
				case 3:
					sirena_en=(temp_miganie/25)%2;
					break;
				case 4:
					next2=(temp_miganie/25)%2;
					break;
				case 5:
					nextS=(temp_miganie/25)%2;
					break;
				case 6:
					relay=(temp_miganie/25)%2;
					break;
				case 9:
					next_s1 = (temp_miganie/25)%2;
				break;
				case 10:
					next_s2 = (temp_miganie/25)%2;
				break;
				case 11:
					nosdcard=(temp_miganie/25)%2;
				break;
				case 12:
					play_s1 = 1;
				break;
				case 13:
					play_s2 = (temp_miganie/25)%2;
				break;
				
				case 7:
				default:
					
					break;
			}
			
			//sleep_mode();
		}
		sleep_mode();
		WatchdogReset();
	}

	return(1);
}	/* main */

void delay(char del)
{
	while(del--) asm("NOP");
	
	
}
unsigned char temp1;
/*$FUNCTION$*******************************************************************/
ISR(TIMER0_OVF_vect)
{

	TIMSK &= ~(1<<TOIE0);				/* Disable the timer0 interrupt				*/
	SPSR = 0;
	cam_on;
	black_off;
	asm("NOP");

if (sirena_en)//!(PINA & (1<<6)))
{	
	if ((line > 270) && (line < 298))
	{
		delay(65);
		SPSR = 0;
		ltemp = ((line - 271)/4) * 27 - 64;
		ntemp = ((line - 271)/4)  * 14 - 45;
		black_on;
		cam_off;
		SPDR = ltrs_inv['S' + ltemp];		Wait();
		SPDR = ltrs_inv['I' + ltemp];		Wait();
		SPDR = ltrs_inv['R' + ltemp];		Wait();
		SPDR = ltrs_inv['E' + ltemp];		Wait();
		SPDR = ltrs_inv['N' + ltemp];		Wait();
		SPDR = ltrs_inv['A' + ltemp];		Wait();
		//SPDR = 0xFF-(nums['1' + ntemp]);		Wait();
		asm("NOP");
		asm("NOP");
		asm("NOP");
		asm("NOP");
		asm("NOP");
		asm("NOP");
		asm("NOP");
		asm("NOP");
		cam_on;
		black_off;
	}
	if ((line > 265) && (line < 271))
	{
		delay(65);
		SPSR = 0;
		asm("NOP");
		delay(5);
		black_on;
		cam_off;
		delay(65);
		cam_on;
		black_off;
	}
	if ((line > 297) && (line < 303))
	{
		delay(65);
		delay(3);
		black_on;
		cam_off;
		delay(65);
		cam_on;
		black_off;
	}

}

if (play_s1)//!(PINA & (1<<6)))
{	
	if ((line > 270) && (line < 298))
	{
		delay(55);
		SPSR = 0;
		ltemp = ((line - 271)/4) * 27 - 64;
		ntemp = ((line - 271)/4)  * 14 - 45;
		black_on;
		cam_off;
		SPDR = ltrs_inv['S' + ltemp];		Wait();
		SPDR = ltrs_inv['I' + ltemp];		Wait();
		SPDR = ltrs_inv['R' + ltemp];		Wait();
		SPDR = ltrs_inv['E' + ltemp];		Wait();
		SPDR = ltrs_inv['N' + ltemp];		Wait();
		SPDR = ltrs_inv['A' + ltemp];		Wait();
		SPDR = 0xFF-(nums['1' + ntemp]);		Wait();
		asm("NOP");
		asm("NOP");
		asm("NOP");
		asm("NOP");
		asm("NOP");
		asm("NOP");
		asm("NOP");
		cam_on;
		black_off;
	}
	if ((line > 265) && (line < 271))
	{
		delay(55);
		SPSR = 0;
		asm("NOP");
		delay(5);
		black_on;
		cam_off;
		delay(76);
		cam_on;
		black_off;
	}
	if ((line > 297) && (line < 303))
	{
		delay(55);
		delay(3);
		black_on;
		cam_off;
		delay(76);
		cam_on;
		black_off;
	}

}

if (play_s2)//!(PINA & (1<<6)))
{	
	if ((line > 270) && (line < 298))
	{
		delay(55);
		SPSR = 0;
		ltemp = ((line - 271)/4) * 27 - 64;
		ntemp = ((line - 271)/4)  * 14 - 45;
		black_on;
		cam_off;
		SPDR = ltrs_inv['S' + ltemp];		Wait();
		SPDR = ltrs_inv['I' + ltemp];		Wait();
		SPDR = ltrs_inv['R' + ltemp];		Wait();
		SPDR = ltrs_inv['E' + ltemp];		Wait();
		SPDR = ltrs_inv['N' + ltemp];		Wait();
		SPDR = ltrs_inv['A' + ltemp];		Wait();
		SPDR = 0xFF-(nums['2' + ntemp]);		Wait();
		asm("NOP");
		asm("NOP");
		asm("NOP");
		asm("NOP");
		asm("NOP");
		asm("NOP");
		asm("NOP");
		cam_on;
		black_off;
	}
	if ((line > 265) && (line < 271))
	{
		delay(55);
		SPSR = 0;
		asm("NOP");
		delay(5);
		black_on;
		cam_off;
		delay(76);
		cam_on;
		black_off;
	}
	if ((line > 297) && (line < 303))
	{
		delay(55);
		delay(3);
		black_on;
		cam_off;
		delay(76);
		cam_on;
		black_off;
	}

}



if (play2_en)//!(PINA & (1<<5)))
{	
	if ((line > 270) && (line < 298))
	{
		delay(75);
		SPSR = 0;
		ltemp = ((line - 271)/4) * 27 - 64;
		ntemp = ((line - 271)/4)  * 14 - 45;
		black_on;
		cam_off;
		SPDR = ltrs_inv['P' + ltemp];		Wait();
		SPDR = ltrs_inv['L' + ltemp];		Wait();
		SPDR = ltrs_inv['A' + ltemp];		Wait();
		SPDR = ltrs_inv['Y' + ltemp];		Wait();
		SPDR = 0xFF-(nums['2' + ntemp]);		Wait();
		asm("NOP");
		asm("NOP");
		asm("NOP");
		cam_on;
		black_off;
	}
	if ((line > 265) && (line < 271))
	{
		delay(75);
		SPSR = 0;
		asm("NOP");
		delay(5);
		black_on;
		cam_off;
		delay(53);
		cam_on;
		black_off;
	}
	if ((line > 297) && (line < 303))
	{
		delay(75);
		delay(3);
		black_on;
		cam_off;
		delay(53);
		cam_on;
		black_off;
	}
}


if (play1_en)//!(PINA & (1<<4)))
{	
	if ((line > 270) && (line < 298))
	{
		delay(75);
		SPSR = 0;
		ltemp = ((line - 271)/4) * 27 - 64;
		ntemp = ((line - 271)/4)  * 14 - 45;
		black_on;
		cam_off;
		SPDR = ltrs_inv['P' + ltemp];		Wait();
		SPDR = ltrs_inv['L' + ltemp];		Wait();
		SPDR = ltrs_inv['A' + ltemp];		Wait();
		SPDR = ltrs_inv['Y' + ltemp];		Wait();
		SPDR = 0xFF-(nums['1' + ntemp]);		Wait();
		asm("NOP");
		asm("NOP");
		asm("NOP");
		cam_on;
		black_off;
	}
	if ((line > 265) && (line < 271))
	{
		delay(75);
		SPSR = 0;
		asm("NOP");
		delay(5);
		black_on;
		cam_off;
		delay(53);
		cam_on;
		black_off;
	}
	if ((line > 297) && (line < 303))
	{
		delay(75);
		delay(3);
		black_on;
		cam_off;
		delay(53);
		cam_on;
		black_off;
	}

}


if (relay)//!(PINA & (1<<4)))
{	
	if ((line > 270) && (line < 298))
	{
		delay(75);
		SPSR = 0;
		ltemp = ((line - 271)/4) * 27 - 64;
		ntemp = ((line - 271)/4)  * 14 - 45;
		black_on;
		cam_off;
		SPDR = ltrs_inv['R' + ltemp];		Wait();
		SPDR = ltrs_inv['E' + ltemp];		Wait();
		SPDR = ltrs_inv['L' + ltemp];		Wait();
		SPDR = ltrs_inv['A' + ltemp];		Wait();
		SPDR = ltrs_inv['Y' + ltemp];		Wait();
		asm("NOP");
		asm("NOP");
		asm("NOP");
		asm("NOP");
		cam_on;
		black_off;
	}
	if ((line > 265) && (line < 271))
	{
		delay(75);
		SPSR = 0;
		asm("NOP");
		delay(5);
		black_on;
		cam_off;
		delay(53);
		cam_on;
		black_off;
	}
	if ((line > 297) && (line < 303))
	{
		delay(75);
		delay(3);
		black_on;
		cam_off;
		delay(53);
		cam_on;
		black_off;
	}
}

if (next2)//!(PINA & (1<<4)))
{	
	if ((line > 270) && (line < 298))
	{
		delay(95);
		SPSR = 0;
		ltemp = ((line - 271)/4) * 27 - 64;
		ntemp = ((line - 271)/4)  * 14 - 45;
		black_on;
		cam_off;
		SPDR = 0xFF-(nums['/' + ntemp]);		Wait();
		SPDR = 0xFF-(nums['/' + ntemp]);		Wait();
		SPDR = 0xFF-(nums['2' + ntemp]);		Wait();
		asm("NOP");
		asm("NOP");
		asm("NOP");
		asm("NOP");
		asm("NOP");
		cam_on;
		black_off;
	}
	if ((line > 265) && (line < 271))
	{
		delay(95);
		SPSR = 0;
		asm("NOP");
		delay(5);
		black_on;
		cam_off;
		delay(32);
		cam_on;
		black_off;
	}
	if ((line > 297) && (line < 303))
	{
		delay(95);
		delay(3);
		black_on;
		cam_off;
		delay(32);
		cam_on;
		black_off;
	}
}

if (nextS)//!(PINA & (1<<4)))
{	
	if ((line > 270) && (line < 298))
	{
		delay(95);
		SPSR = 0;
		ltemp = ((line - 271)/4) * 27 - 64;
		ntemp = ((line - 271)/4)  * 14 - 45;
		black_on;
		cam_off;
		SPDR = 0xFF-(nums['/' + ntemp]);		Wait();
		SPDR = 0xFF-(nums['/' + ntemp]);		Wait();
		SPDR = ltrs_inv['S' + ltemp];		Wait();
		asm("NOP");
		asm("NOP");
		cam_on;
		black_off;
	}
	if ((line > 265) && (line < 271))
	{
		delay(95);
		SPSR = 0;
		asm("NOP");
		delay(5);
		black_on;
		cam_off;
		delay(31);
		cam_on;
		black_off;
	}
	if ((line > 297) && (line < 303))
	{
		delay(95);
		delay(3);
		black_on;
		cam_off;
		delay(31);
		cam_on;
		black_off;
	}
}

if (next_s1)//!(PINA & (1<<4)))
{	
	if ((line > 270) && (line < 298))
	{
		delay(75);
		SPSR = 0;
		ltemp = ((line - 271)/4) * 27 - 64;
		ntemp = ((line - 271)/4)  * 14 - 45;
		black_on;
		cam_off;
		SPDR = 0xFF-(nums['/' + ntemp]);		Wait();
		SPDR = 0xFF-(nums['/' + ntemp]);		Wait();
		SPDR = ltrs_inv['S' + ltemp];		Wait();
		SPDR = 0xFF-(nums['1' + ntemp]);		Wait();
		asm("NOP");
		cam_on;
		black_off;
	}
	if ((line > 265) && (line < 271))
	{
		delay(75);
		SPSR = 0;
		asm("NOP");
		delay(5);
		black_on;
		cam_off;
		delay(42);
		cam_on;
		black_off;
	}
	if ((line > 297) && (line < 303))
	{
		delay(75);
		delay(3);
		black_on;
		cam_off;
		delay(42);
		cam_on;
		black_off;
	}
}

if (next_s2)//!(PINA & (1<<4)))
{	
	if ((line > 270) && (line < 298))
	{
		delay(75);
		SPSR = 0;
		ltemp = ((line - 271)/4) * 27 - 64;
		ntemp = ((line - 271)/4)  * 14 - 45;
		black_on;
		cam_off;
		SPDR = 0xFF-(nums['/' + ntemp]);		Wait();
		SPDR = 0xFF-(nums['/' + ntemp]);		Wait();
		SPDR = ltrs_inv['S' + ltemp];		Wait();
		SPDR = 0xFF-(nums['2' + ntemp]);		Wait();
		asm("NOP");
		cam_on;
		black_off;
	}
	if ((line > 265) && (line < 271))
	{
		delay(75);
		SPSR = 0;
		asm("NOP");
		delay(5);
		black_on;
		cam_off;
		delay(42);
		cam_on;
		black_off;
	}
	if ((line > 297) && (line < 303))
	{
		delay(75);
		delay(3);
		black_on;
		cam_off;
		delay(42);
		cam_on;
		black_off;
	}
}

if (nosdcard)//!(PINA & (1<<5)))
{	
	if ((line > 270) && (line < 298))
	{
		delay(80);
		SPSR = 0;
		ltemp = ((line - 271)/4) * 27 - 64;
		ntemp = ((line - 271)/4)  * 14 - 45;
		black_on;
		cam_off;
		SPDR = ltrs_inv['N' + ltemp];		Wait();
		SPDR = ltrs_inv['O' + ltemp];		Wait();
		SPDR = 0x00; Wait();
		SPDR = ltrs_inv['S' + ltemp];		Wait();
		SPDR = ltrs_inv['D' + ltemp];		Wait();
		asm("NOP");
		asm("NOP");
		asm("NOP");
		asm("NOP");
		cam_on;
		black_off;
	}
	if ((line > 265) && (line < 271))
	{
		delay(80);
		SPSR = 0;
		asm("NOP");
		delay(5);
		black_on;
		cam_off;
		delay(51);
		cam_on;
		black_off;
	}
	if ((line > 297) && (line < 303))
	{
		delay(80);
		delay(3);
		black_on;
		cam_off;
		delay(51);
		cam_on;
		black_off;
	}
}

}


/*$FUNCTION$*******************************************************************/
ISR(INT0_vect)
/*******************************************************************************
* ABSTRACT:	Reset line timer at the left side of the screen.
*
* INPUT:		None
* OUTPUT:	None
*/
{
	
	if (!(PIND & (1<<2))) 
	{
		line++;
		TCNT0 = 75;							/* Set timeout period for the first line	*/
		TIFR |= 1<<TOV0;						/* Clear any potential interrupt				*/
		TIMSK |= 1<<TOIE0;					/* Enable the timer0 interrupt				*/
	}
	

}	/* SIGNAL(SIG_COMPARATOR) */


/*$FUNCTION$*******************************************************************/
ISR(INT1_vect)
/*******************************************************************************
* ABSTRACT:	Reset line at the top of the screen.
*
* INPUT:		None
* OUTPUT:	None
*/
{
		line = 0;
		sleep_cmd=1;
		temp_miganie++;
}	/* SIGNAL(SIG_COMPARATOR) */

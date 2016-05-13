/*
Copyright (c) 2009, Nigel Batten.
Contactable at <firstname>.<lastname>@mail.com

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

	1.	Redistributions of source code must retain the above copyright
		notice, this list of conditions and the following disclaimer.
	2.	Redistributions in binary form must reproduce the above copyright
		notice, this list of conditions and the following disclaimer in the
		documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
COPYRIGHT HOLDER BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
OF SUCH DAMAGE.

===============================================
*/

#include <avr/io.h>
#include <avr/sleep.h>
#include "video.h"

sync_handler *g_sync_handler = sync_handler_display;

void sync_handler_display( void )
{
	g_char_poll();
	g_char_process();
	sleep_cpu(); // will wake up at the right point to start drawing.
}


void simple_sync( uint8_t count )
{
	while( count-- )
	{
		g_char_poll();
		g_char_process();
		// sleep can't be used here - I have to wait until the next sync pulse has started...
		while(!( PINB & (1<< SIG_SYNC_PIN ))){}; // wait until we're not in SYNC
		while( PINB & (1<< SIG_SYNC_PIN )){}; // wait until sync is set (at start of next scanline)
	}
}

void sync_handler_field_header( void )
{
/*
	One whole (blank) scanline (just with a normal HSync)
		PAL:
		This routine will run for 8 scanlines, with the following half-scanline pattern:
		6 short pulses
		5 broad pulses
		5 short pulses
		
		NTSC:
		This routine will run for 9 scanlines, with the following half-scanline pattern:
		6 short pulses
		6 broad pulses
		6 short pulses
	One whole (blank) scanline (with just normal HSync)
*/

uint8_t IS_PAL = g_framecount & (1<<OPT_PAL) ;

	// Sneak in a loop if the output is to be suppressed...
	if (g_options & (1<<OPT_SUPPRESSOUTPUT))
	{
		// turn off the OC1A output
		TCCR1A &= ~(1<<COM1A1)|(1<< COM1A0) ;
		SYNC_ON; // pull the sync output to 0v
		

		while ( g_options & (1<<OPT_SUPPRESSOUTPUT))
		{
			// this loop will continually poll and process serial characters
			// until the SUPPRESS OUTPUT is disabled again.
			g_char_poll();
			g_char_process();
			sleep_cpu();
		}
		
		// switch the OC1A output
		TCCR1A |= ((1<<COM1A1)|(1<< COM1A0)) ;
	}


	OCR1A = CLOCK_SHORTSYNC ; // this will take effect after the next timer rollover.
	simple_sync(1);
	
	// we're in the first shortsync pulse line. Make it H/2
	ICR1 = (IS_PAL)? CLOCK_HALFSCANLINE_PAL : CLOCK_HALFSCANLINE_NTSC ;
	simple_sync(5); // yes 5 (because OCR1A needs changing on the line BEFORE)

	// we're now at the start of the last shortsync halfline.
	// set up the (buffered) compare match point for the next (longsync pulse) halfline...
	OCR1A = (IS_PAL) ? CLOCK_LONGSYNC_PAL : CLOCK_LONGSYNC_NTSC ;
	simple_sync( (IS_PAL) ? 5 : 6 ) ;
	
	// we're now at the start of the last longsync halfline.
	// set up the (buffered) compare match point for the next (shortsync pulse) halfline...
	OCR1A = CLOCK_SHORTSYNC ;
	simple_sync( (IS_PAL) ? 5 : 6 ) ;
	
	// we're now at the start of the last shortsync halfline.
	// set up the (buffered) compare match points for the next (normal HSync) scanline...
	OCR1A = CLOCK_HSYNC ;
	g_current_scanline = (IS_PAL) ? SCANLINE_FIRST_DISPLAY_PAL - 1 : SCANLINE_FIRST_DISPLAY_NTSC - 1 ;
	g_sync_handler = sync_handler_display ;
	g_render_handler = render_handler_nondisplay ;
	simple_sync( 1 );
	
	// we're now at the start of the trailing normal HSYNC line.
	// ensure that the scanline is changed to be a whole width...
	ICR1 = (IS_PAL) ? CLOCK_SCANLINE_PAL : CLOCK_SCANLINE_NTSC ;

	g_char_poll();
	g_char_process();
	sleep_cpu(); // will wake up at the right point to start drawing.

}

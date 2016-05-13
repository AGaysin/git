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
#include "video.h"

// the following define is to restrict the number of columns to display.
// (it can help whilst debugging - reducing the number of columns to display
// means that debug code can then be added without running out of time)
// NOTE: it does not affect the fontram render function, which still has to
//       be 'truncated' by hand for debugging.
#define COL_COUNT_VISIBLE (COL_COUNT)

	   render_handler render_handler_nondisplay ;
static render_handler render_handler_font ;
static render_handler render_handler_fontwide ;
static render_handler render_handler_fontblank ;
#ifdef RAMFONT
// special versions of the above that draw the top 64 characters from RAM...
static render_handler render_handler_fontram ;
static render_handler render_handler_fontwideram ;
#endif

render_handler *g_render_handler = render_handler_nondisplay ;

static void scanline_end( void ) ;

static uint8_t s_DisplayRow = 0;
static uint8_t s_RowSlice = 0;			// The slice of the row that is being rendered (1-FONT_CHAR_HEIGHT)
static uint8_t s_FontPtrHi = 0;			// The font-slice that should be used to render this scanline.
static uint8_t s_HighlightColumn = 0;	// Which character should be inverted (unusual ordering here)
static uint8_t s_CurrentRowInfo = 0;	// row attributes for the character row currently being rendered.
static uint8_t s_CursorDelta;			// see render_handler_nondisplay for how this is used.
static uint8_t s_RowsToComplete;		// the number of rows still to complete in this field...
#define s_inverse_mask	TWAR


// variables used to store pre-calculated values
static render_handler *s_precalc_render_handler ; // the next row's render handler
static uint8_t s_precalc_CurrentRowInfo ;	// the next row's RowInfo

#ifdef RAMFONT
// Extra variables used if the 64 ram-based characters are available
static uint8_t *s_RamFontPtr ; // the font-slice that should be used to render ram characters.
static uint8_t *s_DisplayPtr ; // pointer to the start of the currently displayed row.
static uint8_t s_precalc_HighlightColumn;// the next row's highlight column
static uint8_t s_precalc_FontPtrHi = 0;
static uint8_t *s_precalc_RamFontPtr ;
static uint8_t *s_precalc_DisplayPtr ; // pointer to the start of the currently displayed row.
#endif


// lookup table for which routine should render which sort of row...
static render_handler * PROGMEM gf_render_lookup[] =
{
// these match with all the potential ROWINFO values...
	render_handler_fontblank,	// 0000 - non-visible normal
	render_handler_font,		// 0001 - visible, normal
	render_handler_fontblank,	// 0010 - non-visible double height (top)
	render_handler_font,		// 0011 - visible double height (top)
	render_handler_fontblank,	// 0100 - non-visible, normal (DOUBLE_HEIGHT_BOTTOM ignored)
	render_handler_font,		// 0101 - visible, normal (DOUBLE_HEIGHT_BOTTOM ignored)
	render_handler_fontblank,	// 0110 - non-visible, double height (bottom)
	render_handler_font,		// 0111 - visible, double height (bottom)
	render_handler_fontblank,	// 1000 - non-visible double width
	render_handler_fontwide,	// 1001 - visible, double width
	render_handler_fontblank,	// 1010 - non-visible double width, double height (top)
	render_handler_fontwide,	// 1011 - visible double width, double height (top)
	render_handler_fontblank,	// 1100 - non-visible, double width (DOUBLE_HEIGHT_BOTTOM ignored)
	render_handler_fontwide,	// 1101 - visible, double width (DOUBLE_HEIGHT_BOTTOM ignored)
	render_handler_fontblank,	// 1110 - non-visible, double width, double height (bottom)
	render_handler_fontwide		// 1111 - visible, double width, double height (bottom)
};

#ifdef RAMFONT
static render_handler * PROGMEM gf_render_lookup_ram[] =
{
// these are the routines to call if you the top 64 characters rendered from RAM
	render_handler_fontblank,	// 0000 - non-visible normal
	render_handler_fontram,		// 0001 - visible, normal
	render_handler_fontblank,	// 0010 - non-visible double height (top)
	render_handler_fontram,		// 0011 - visible double height (top)
	render_handler_fontblank,	// 0100 - non-visible, normal (DOUBLE_HEIGHT_BOTTOM ignored)
	render_handler_fontram,		// 0101 - visible, normal (DOUBLE_HEIGHT_BOTTOM ignored)
	render_handler_fontblank,	// 0110 - non-visible, double height (bottom)
	render_handler_fontram,		// 0111 - visible, double height (bottom)
	render_handler_fontblank,	// 1000 - non-visible double width
	render_handler_fontwideram,	// 1001 - visible, double width
	render_handler_fontblank,	// 1010 - non-visible double width, double height (top)
	render_handler_fontwideram,	// 1011 - visible double width, double height (top)
	render_handler_fontblank,	// 1100 - non-visible, double width (DOUBLE_HEIGHT_BOTTOM ignored)
	render_handler_fontwideram,	// 1101 - visible, double width (DOUBLE_HEIGHT_BOTTOM ignored)
	render_handler_fontblank,	// 1110 - non-visible, double width, double height (bottom)
	render_handler_fontwideram	// 1111 - visible, double width, double height (bottom)
};
#endif


#ifdef RAMFONT
#define GET_RENDER_HANDLER( ROWINFO ) ((render_handler *)(((ROWINFO & 0b11110000) == 0) ? pgm_read_word( &gf_render_lookup_ram[ ROWINFO & 0b1111] ) : pgm_read_word( &gf_render_lookup[ ROWINFO & 0b1111 ] )))
#else
#define GET_RENDER_HANDLER( ROWINFO ) ((render_handler *)pgm_read_word( &gf_render_lookup[ ROWINFO & 0b1111 ] ))
#endif


void render_handler_nondisplay( void )
{
uint16_t first_textsafe ;

	if ((g_current_scanline >> 8) == 0)
	{

		if (g_framecount & (1<<OPT_PAL))
		{
			first_textsafe = SCANLINE_FIRST_TEXTSAFE_PAL ;
		}
		else
		{
			first_textsafe = SCANLINE_FIRST_TEXTSAFE_NTSC ;
		}

		if (g_current_scanline == (first_textsafe-1))
		{
			// This line isn't actually rendered (unless, in future, inversed screen/rows are implemented, in which case a white line
			// above the top stripe of font data will be required)
			// it's the line before the first font stripe that's displayed.
			// Some pre-calculations are carried out here, the remainder in _setup_next_row().
			s_DisplayRow = g_row_scroll_offset - 1; // it will be incremented to the first-row in _setup_next_row()
			s_RowsToComplete = ROW_COUNT + 1 ; // it will be decremented in _setup_next_row()
			s_RowSlice = (FONT_CHAR_HEIGHT - 1); // triggers _setup_next_row() to re-set stuff for this first row.
			
			// configure the inverting mask...
			s_inverse_mask = (g_framecount & (1<<OPT_INVERSE) ) ? 0xFF : 0x00 ;
			
			// increment the framecount (leaving the top 3 bits alone)
			uint8_t temp = g_framecount & 0b00011111 ;
			temp += 1 ;
			temp = temp & 0b00011111 ;
			g_framecount = (g_framecount & 0b11100000) | temp ;
			return ;
		}

		if (g_current_scanline == first_textsafe)
		{
			
			// do the pre-calculations that would normally done in previous scanlines
			uint8_t rowinfo = g_rowinfo[ g_row_scroll_offset ] ;
			s_precalc_CurrentRowInfo = rowinfo ;
			s_precalc_render_handler = GET_RENDER_HANDLER( rowinfo ) ;
			
#ifdef RAMFONT
			if (	(rowinfo & ((1<<ROWINFO_DOUBLE_HEIGHT) | (1<<ROWINFO_DOUBLE_HEIGHT_BOTTOM)))
					== ((1<<ROWINFO_DOUBLE_HEIGHT) | (1<<ROWINFO_DOUBLE_HEIGHT_BOTTOM)))
			{
				s_precalc_RamFontPtr = g_userfont[4] + 63 ; // slice 4 with the bottom 2 bits set.
			}
			else
			{
				s_precalc_RamFontPtr = g_userfont[0] + 63 ; // slice 0 with the bottom 2 bits set.
			}
#endif
			
			
			// cursor delta affects is how the cursor is displayed.
			// the value is calculated here once per screen, when there's plenty of free time.
			// if the '
			// a delta of 0 means 'block cursor'
			// a delta of (COL_COUNT+1) means 'underscore cursor'
			// a delta of 2 * (COL_COUNT+1) means 'no cursor'.
			s_CursorDelta= 0 ;
			if (g_options & (1<<OPT_HIDECURSOR)) s_CursorDelta+= (2 * (COL_COUNT_VISIBLE + 1)) ;
			if (!(g_options & (1<<OPT_BLOCKCURSOR))) s_CursorDelta+= (COL_COUNT_VISIBLE + 1) ;
			
			SPCR = (1<<SPE)|(1<<MSTR)|(1<<CPHA); // enable SPI output (with phase alteration)
			

			// if HIDEDISPLAY is active, trick the setup next row into thinking it's finished
			// drawing everything (in which case, this routine will be repeatedly called until the end of the field).
			if (g_options & (1<<OPT_HIDEDISPLAY)) s_RowsToComplete = 1 ;
			
			scanline_end();
			return ;
		}
	}
	
uint16_t last_display ;
	if (g_framecount & (1<<OPT_PAL))
	{
		last_display = SCANLINE_LAST_DISPLAY_PAL ;
	}
	else
	{
		last_display = SCANLINE_LAST_DISPLAY_NTSC ;
	}

	if (g_current_scanline == last_display)
	{
		g_sync_handler = sync_handler_field_header ;
	}
}

/*
scanline_end() is called at the end of each rendered scanline (e.g. scanlines with pixels in).

On most scanline ends (the first 8 slices of a font), all that needs to be done is bump
the font pointer to the next slice.
On the end of the 9th slice though, lots of things change - everything needs setting up
for the next row of characters. It might need a different font, render routine, cursor details etc.

Because there's just too much to do at the end of the 9th slice, the work is brought forwards into
earlier slices and stored.
It's a classic trade-off between speed (there's not a lot of time at the end of a scanline) and
memory requirements for pre-calculations (There is NO space left in the 1kb SRAM parts - fortunately
there's a fair amount spare on the 2kb SRAM parts)

*/
static void scanline_end()
{
uint8_t rowslice = s_RowSlice ;

	// Test for rowslice 8 (the 9th slice) - it's the tightest route through the code
	// so a special case is made (it saves it having to go through the switch statement)
	
	
	if (rowslice == (FONT_CHAR_HEIGHT - 1))
	{
		uint8_t rows_to_complete = s_RowsToComplete ;
		rows_to_complete-- ;

		SPSR = (1<< SPI2X); // as fast as possible - F_CPU / 2
		PIXEL_DISABLE;
		// we have just drawn the last of FONT_CHAR_HEIGHT rows in this row.
		// set-up the next row...
		if (rows_to_complete)
		{
			s_RowsToComplete = rows_to_complete ;
			// there are still more rows to be drawn...
			// move onto the next character row...
			uint8_t t = s_DisplayRow + 1 ;
			if ( t >= ROW_COUNT ) t = 0;
			s_DisplayRow = t ;
			s_RowSlice = 0;
			
			
			uint8_t rowinfo = s_precalc_CurrentRowInfo ;
			s_CurrentRowInfo = rowinfo ;
			
			
#ifdef RAMFONT
			s_FontPtrHi = s_precalc_FontPtrHi ;
			s_RamFontPtr = s_precalc_RamFontPtr ;
			s_DisplayPtr = s_precalc_DisplayPtr ;
#else
			uint8_t fontptrhi = ((uint16_t)f_font >> 8);
	#if FONTBANK_COUNT > 1		
			uint8_t fontbank = (rowinfo & 0b11110000) >> 4 ;
			fontbank = fontbank * FONT_CHAR_HEIGHT ;
			fontptrhi += fontbank ;
	#endif
			if (	(rowinfo & ((1<<ROWINFO_DOUBLE_HEIGHT) | (1<<ROWINFO_DOUBLE_HEIGHT_BOTTOM)))
					== ((1<<ROWINFO_DOUBLE_HEIGHT) | (1<<ROWINFO_DOUBLE_HEIGHT_BOTTOM)))
			{
				fontptrhi += 4 ;
			}
			s_FontPtrHi = fontptrhi;
#endif
			
			
			// now set the render handler and rowinfo for the next row.
			// these were calculated in the previous scanline(s) to save some precious cycles.
			// (or in render_handler_firstline)
			
			g_render_handler = s_precalc_render_handler ;

#ifdef RAMFONT
			s_HighlightColumn = s_precalc_HighlightColumn ;
#else
			// Set the Highlight column if a cursor is required.
			s_HighlightColumn = 0; // default
			if (
				(s_DisplayRow == g_current_row)
					&&
				(g_framecount & 0b10000)
				)
			{
				s_HighlightColumn = (COL_COUNT_VISIBLE - g_current_col) - (s_CursorDelta) ;
			}
#endif
		}
		else
		{
			// We've drawn enough rows - nothing more to display!
			g_render_handler = render_handler_nondisplay ;
			return ;
		}
		
	}
	else
	{
		uint8_t rowinfo = s_CurrentRowInfo ;
		uint8_t inc = 1 ;
		CYCLES(2);
		SPSR = (1<< SPI2X); // as fast as possible - F_CPU / 2
		PIXEL_DISABLE;

		if (rowinfo & (1<<ROWINFO_DOUBLE_HEIGHT))
		{
			if (rowslice & 0b1) inc++ ;
			if (!(rowinfo & (1<<ROWINFO_DOUBLE_HEIGHT_BOTTOM))) inc++ ;
		}
		
		if (inc & 0b1)
		{
			s_FontPtrHi += 1 ; // point to the next block of 256 character bitslices
#ifdef RAMFONT		
			s_RamFontPtr += 64 ;		
#endif
		}
		
		switch( rowslice )
		{
			case 0 :
			case 1 :
			case 2 :
#ifdef RAMFONT
				{
					// pre-calculate the display pointer for the next row...
					uint8_t nextrow = s_DisplayRow + 1 ;
					if (nextrow >= ROW_COUNT) nextrow = 0 ;
					s_precalc_DisplayPtr = &(g_display[ nextrow ][0]) ;
				}
#endif
				break;
			case 3 :
				{
					// pre-calculate the rowinfo to use for the next character row...
					
					uint8_t nextrow = s_DisplayRow + 1 ;
					if (nextrow >= ROW_COUNT) nextrow = 0 ;
					uint8_t rowinfo = g_rowinfo[ nextrow ] ;
					s_precalc_CurrentRowInfo = rowinfo ;
				}
				break ;
			case 4 :
				{
					uint8_t rowinfo = s_precalc_CurrentRowInfo ;
					s_precalc_render_handler = GET_RENDER_HANDLER( rowinfo ) ;
				}
				break ;
			case 5 :
#ifdef RAMFONT
				{
					// Pre-calculate the FontPtrHi
					uint8_t rowinfo = s_precalc_CurrentRowInfo ;
					uint8_t fontptrhi = ((uint16_t)f_font >> 8);
					uint8_t fontbank = (rowinfo & 0b11110000) >> 4 ;
					fontbank = fontbank * FONT_CHAR_HEIGHT ;
					fontptrhi += fontbank ;

					if (	(rowinfo & ((1<<ROWINFO_DOUBLE_HEIGHT) | (1<<ROWINFO_DOUBLE_HEIGHT_BOTTOM)))
							== ((1<<ROWINFO_DOUBLE_HEIGHT) | (1<<ROWINFO_DOUBLE_HEIGHT_BOTTOM)))
					{
						fontptrhi += 4 ;
						s_precalc_RamFontPtr = g_userfont[4] + 63 ; // slice 0 with the bottom 2 bits set
					}
					else
					{
						s_precalc_RamFontPtr = g_userfont[0] + 63 ; // slice 4 with the bottom 2 bits set
					}
					s_precalc_FontPtrHi = fontptrhi;
				}
#endif
				break;
			case 6 :
#ifdef RAMFONT
				{
					// Pre-calculate the highlight column to use...

					// Set the Highlight column if a cursor is required.
					s_precalc_HighlightColumn = 0; // default
					uint8_t nextrow = s_DisplayRow + 1 ;
					if (nextrow >= ROW_COUNT ) nextrow = 0;
					
					if (
						(nextrow == g_current_row)
							&&
						(g_framecount & 0b10000)
						)
					{
						s_precalc_HighlightColumn = (COL_COUNT_VISIBLE - g_current_col) - (s_CursorDelta) ;
					}
				}
#endif
				break;
			case 7:
				{
					// handle the cursor - the next line is the last slice...
					if (s_HighlightColumn > COL_COUNT_VISIBLE)
					{ // if it's not already being displayed, modify the value to see if it should be...
						s_HighlightColumn += (COL_COUNT_VISIBLE + 1) ;
					}
					
				}
				break ;
		}
		rowslice++;
		s_RowSlice = rowslice ;

	}

}



static void render_handler_fontblank( void )
{ // 221c to here (from scanline start) == 13.8us
uint8_t i = COL_COUNT_VISIBLE ;

	// font display line where no characters are visible
	// Note that the cursor will never be displayed in a non-visible line
	// (as the line will have been normalised beforehand).
	SPCR = 0; //1c
	SPCR = (1<<SPE)|(1<<MSTR)|(1<<CPHA); //2c

	_delay_cycles(10);
	uint8_t invertmask = s_inverse_mask ;
	while(i > 0)
	{
		_delay_cycles(9);
		if (invertmask)
		{
			_delay_cycles(1) ;
		}
		else
		{
			PIXEL_DISABLE ;
			_delay_cycles(1);
		}
		SPDR = invertmask ;
		PIXEL_ENABLE ;
		i--;	
	}
	_delay_cycles(1) ;
	scanline_end();
}

#ifdef RAMFONT
static void render_handler_fontram( void )
{
uint8_t *char_ptr = s_DisplayPtr ;
uint8_t invertmask = s_inverse_mask ;

	CYCLES(1);

// macro to render a single character in the display.
// This macro will need calling 38 times (!)
#define RENDERCHAR( this, next, skipline_loopstart, skipline_loopend )\
					"col_" this ":		; unrolled loop for column " this "...						\n\t"\
					"	ld		r28, %a[char_ptr]+   ; (+2c)   straight into y-lo					\n\t"\
					"   cpi     r28, 0xc0            ; (+1c)   look at top 2 bits					\n\t"\
					"   brlo	col_" this "_ROMCHAR ; (+1/2c) top 2 bits are not set, jump...		\n\t"\
					"col_" this "_RAMCHAR:			 ; (=4c to here)								\n\t"\
					"   and		r28,r25		     	 ; (1c) set the appropriate top bits			\n\t"\
					"	ld		__tmp_reg__,Y        ; (2c) read byte from RAM						\n\t"\
					"	rjmp	col_" this "_render	 ; (2c)											\n\t"\
					"col_" this "_ROMCHAR:			 ; (=5c to here)								\n\t"\
					"	mov		r30, r28			 ; (1c) move character into z-lo				\n\t"\
					"	lpm		__tmp_reg__,Z		 ; (3c) read byte from ROM						\n\t"\
					"col_" this "_render:			 ; (=9c to here)								\n\t"\
					"eor		__tmp_reg__,%[invertmask]  ; EOR with invert mask \n\t"\
					"	cpi		r19, (38-" this ")	 ; (1c)											\n\t"\
					"	brne	.+2 		         ; (1/2c) invert if this is the current cursor position	\n\t"\
					"	com		__tmp_reg__			 ; (1c)											\n\t"\
skipline_loopstart	"	sbrs	r24, 0	             ; (1c) skip disable pixel for white 9th pixel	\n\t"\
skipline_loopstart	"	out		%[DDR],	r23			 ; (1c)											\n\t"\
					"	out		%[_SPDR], __tmp_reg__; (1c)											\n\t"\
					"	out		%[DDR], r22	         ; (1c) switch MOSI pin to output				\n\t"\
skipline_loopend	"	mov		r24, __tmp_reg__	 ; (1c)											\n\t"

asm("\n\t"
		// initialise registers
		// r19: which character position to invert (for cursor)
		// r20: temp storage for y_lo
		// r21: temp storage for y_hi
		
		// r22: DDRB setting for "enable pixel output"
		// r23: DDRB setting for "disable pixel output"
		// r24: bit-pattern of previous character. This is stored so that the 9th bit can duplicate the 8th bit.
		// r25: top-bit mask for the lo-byte of RAM font lookup table
		// X  : (r26,r27) address of next character to output
		// Y is trashed and restored.
		// r28: (y-lo) lo-byte of RAM font lookup table (top 2 bits are kept, meaning a 64-character lookup)
		// r29: (y-hi) hi-byte of RAM font lookup table (256-byte aligned.)
		//
		// r30: (z-lo) lo-byte of FLASH font lookup table (e.g. the character to lookup).
		// r31: (z-hi) hi-byte of FLASH font lookup table (256-byte aligned - determines which slice)
		// first set-up various registers...
		"	lds		r19, s_HighlightColumn	\n\t"
		"	ldi		r22, %[enable_pixel]			\n\t"
		"	ldi		r23, %[disable_pixel]			\n\t"
		"	movw	r20, r28 ; store y-lo + y_hi	\n\t"
		"	lds		r25, s_RamFontPtr	; (2c)		\n\t"
		"	lds		r29, s_RamFontPtr+1	; (2c)		\n\t"
		"	lds		r31, s_FontPtrHi	; (2C)		\n\t"

		RENDERCHAR( "0", "1", ";", "" )
		RENDERCHAR( "1", "2", "", "" )
		RENDERCHAR( "2", "3", "", "" )
		RENDERCHAR( "3", "4", "", "" )
		RENDERCHAR( "4", "5", "", "" )
		RENDERCHAR( "5", "6", "", "" )
		RENDERCHAR( "6", "7", "", "" )
		RENDERCHAR( "7", "8", "", "" )
		RENDERCHAR( "8", "9", "", "" )
		RENDERCHAR( "9", "10", "", "" )
		RENDERCHAR( "10", "11", "", "" )
		RENDERCHAR( "11", "12", "", "")
		RENDERCHAR( "12", "13", "", "" )
		RENDERCHAR( "13", "14", "", "" )
		RENDERCHAR( "14", "15", "", "" )
		RENDERCHAR( "15", "16", "", "" )
		RENDERCHAR( "16", "17", "", "" )
		RENDERCHAR( "17", "18", "", "" )
		RENDERCHAR( "18", "19", "", "" )
		RENDERCHAR( "19", "20", "", "" )
		RENDERCHAR( "20", "21", "", "" )
		RENDERCHAR( "21", "22", "", "" )
		RENDERCHAR( "22", "23", "", "" )
		RENDERCHAR( "23", "24", "", "" )
		RENDERCHAR( "24", "25", "", "" )
		RENDERCHAR( "25", "26", "", "" )
		RENDERCHAR( "26", "27", "", "" )
		RENDERCHAR( "27", "28", "", "" )
		RENDERCHAR( "28", "29", "", "" )
		RENDERCHAR( "29", "30", "", "" )
		RENDERCHAR( "30", "31", "", "")
		RENDERCHAR( "31", "32", "", "" )
		RENDERCHAR( "32", "33", "", "" )
		RENDERCHAR( "33", "34", "", "" )
		RENDERCHAR( "34", "35", "", "" )
		RENDERCHAR( "35", "36", "", "" )
		RENDERCHAR( "36", "37", "", "" )
		RENDERCHAR( "37", "38", "", ";" ) // the semicolon comments out some assembly for the last iteration of the loop

		"col_38:										\n\t"
		"renderfontend:								\n\t"
		"	movw		r28, r20	; restore y-lo + y_hi ;	\n\t"
		
		:
		:
		[char_ptr]		"x" (char_ptr),
		[visible_column_count] "M" (COL_COUNT_VISIBLE),
		[enable_pixel]  "M" ((1<<SIG_PIXEL_PIN)|(1<<SIG_SYNC_PIN)),
		[disable_pixel] "M" ((0<<SIG_PIXEL_PIN)|(1<<SIG_SYNC_PIN)),
		[DDR]			"I" (_SFR_IO_ADDR(DDRB)),
		[_SPDR]			"I" (_SFR_IO_ADDR(SPDR)),
		[invertmask]		"a"	(invertmask)
		:
		"r19","r20","r21","r22","r23","r24","r25","r30","r31"
	);
	
	CYCLES(2);
	scanline_end();
}
#endif

static void render_handler_font( void )
{
uint8_t *char_ptr = &(g_display[ s_DisplayRow ][0]) ;
uint8_t invertmask = s_inverse_mask ;

	asm("\n\t"
			// initialise registers
			// r21: which character position to invert (for cursor)
			// r22: DDRB setting for "enable pixel output"
			// r23: DDRB setting for "disable pixel output"
			// r24: bit-pattern of previous character
			//	    This is stored so that the 9th bit can duplicate the 8th bit.
			// r25: count of characters left to display
			// X  : (r26,r27) address of next character to output
			// r30: (z-lo) lo-byte of font lookup table (e.g. the character to lookup).
			// r31: (z-hi) hi-byte of font lookup table (256-byte aligned - determines which slice)
			"	lds		r21, s_HighlightColumn	\n\t"
			"	ldi		r22, %[enable_pixel]			\n\t"
			"	ldi		r23, %[disable_pixel]			\n\t"
			"	ldi		r24, 0x00						\n\t"
			"	ldi		r25, %[visible_column_count]	\n\t"
			"	lds		r31, s_FontPtrHi			\n\t"
			"loop:										\n\t"
			"	ld		r30, %a[char_ptr]+   ; straight into z-lo\n\t"
			"	lpm		__tmp_reg__,Z					\n\t"
			"	eor		__tmp_reg__,%[invert_mask]		\n\t"
			"	cp		r21, r25						\n\t"
			"	brne	.+2 		         ; invert if this is the current cursor position\n\t"
			"	com		__tmp_reg__						\n\t"
			"	sbrs	r24, 0	             ; skip turning off the pixel output if we want pixel 9 to be white\n\t"
			"	out		%[DDR],	r23						\n\t"
			"	mov		r24, __tmp_reg__				\n\t"
			"	out		%[_SPDR], __tmp_reg__			\n\t"
			"	out		%[DDR], r22	        ; switch MOSI pin to output\n\t"
			"	nop														\n\t"
			"	subi	r25, 0x01						\n\t"
			"	brne	loop							\n\t"
			:
			:
			[char_ptr]		"x" (char_ptr),
			[visible_column_count] "M" (COL_COUNT_VISIBLE),
			[enable_pixel]  "M" ((1<<SIG_PIXEL_PIN)|(1<<SIG_SYNC_PIN)),
			[disable_pixel] "M" ((0<<SIG_PIXEL_PIN)|(1<<SIG_SYNC_PIN)),
			[DDR]			"I" (_SFR_IO_ADDR(DDRB)),
			[_SPDR]			"I" (_SFR_IO_ADDR(SPDR)),
			[invert_mask]		"r" (invertmask)
			:
			"r21","r22","r23","r24","r25","r30","r31"
		);

	scanline_end();
}


#ifdef RAMFONT
static void render_handler_fontwideram( void )
{
uint8_t *char_ptr = s_DisplayPtr ;
uint8_t invertmask = s_inverse_mask ;

	SPSR = 0;// turn off the SPI speed-doubling.

	asm("\n\t"
			// initialise registers

			// r18: count of characters left to display
			// r19: which character position to invert (for cursor)
			// r20: temp storage for y_lo
			// r21: temp storage for y_hi
			// r22: DDRB setting for "enable pixel output"
			// r23: DDRB setting for "disable pixel output"
			// r24: bit-pattern of previous character (for 9th bit storeage)
			// r25: top-bit mask for the lo-byte of RAM font lookup table
			// X  : lo:hi (r26,r27) address of next character to output
			// Y  : lo:hi (r28,r29) address of RAM lookup
			// Z  : lo:hi (r30,r31) address of ROM lookup
			"	lds		r19, s_HighlightColumn														\n\t"
			"	movw	r20, r28			; store y-lo + y_hi										\n\t"
			"	ld		r28, %a[char_ptr]+  ; (+2c) read character into y-lo						\n\t"
			"   cpi     r28, 0xc0           ; (+1c) Is the pixel pattern in RAM? (top 2 bits set)	\n\t"
			"   brsh	WIDE_first_read_pixel_pattern_RAM	; (+1/2c) Jump if RAM based.					\n\t"
			
			"WIDE_first_read_pixel_pattern_ROM:	; (=1c to here)											\n\t"
			"	lds		r31, s_FontPtrHi	; (2c)													\n\t"
			"	mov		r30, r28			; (1c) move the character to lookup into z-lo			\n\t"
			"	lpm		__tmp_reg__,Z		; (3c) read byte from ROM								\n\t"
			"	rjmp	WIDE_first_render	; (2c)													\n\t"
			" ; 9c this path																		\n\t"
			
			
			"WIDE_first_read_pixel_pattern_RAM:	; (=2c to here )										\n\t"
			"	lds		r25, s_RamFontPtr	; (2c)													\n\t"
			"	lds		r29, s_RamFontPtr+1	; (2c)													\n\t"
			"   and		r28,r25		     	; (1c) set the appropriate top bits						\n\t"
			"	ld		__tmp_reg__,Y       ; (2c) read byte from RAM								\n\t"
			" ; 9c this path																		\n\t"

			"WIDE_first_render:"
			"	cpi		r19, %[initial_highlight_comparison]	; (1c) is the cursor on this first one?				\n\t"
			"	brne	.+2 		        ; (1/2c) skip inversion if not required					\n\t"
			"	com		__tmp_reg__			; (1c) invert											\n\t"
			" eor		__tmp_reg__, %[invertmask]		; (1c) invert mask \n\t"
			"	ldi		r22, %[enable_pixel]														\n\t"
			"	out		%[_SPDR], __tmp_reg__; (1c)	output byte to SPI hardware						\n\t"
			"	rjmp	.+0					; (2c)													\n\t"
			"	out		%[DDR], r22	        ; (1c) switch MOSI pin to output						\n\t"
			"	mov		r24, __tmp_reg__	; (1c) store pixel pattern (for next 9th pixel testing)	\n\t"

			// now there's a bit of time, initialise the remainder of the variables...
			"	lds		r31, s_FontPtrHi	; (2c)													\n\t"
			"	subi	r19, %[highlight_adjustment]	; (1c)										\n\t"
			"	lds		r25, s_RamFontPtr	; (2c)													\n\t"
			"	lds		r29, s_RamFontPtr+1	; (2c)													\n\t"
			"	ldi		r18, %[visible_column_count]-1	; (1c)										\n\t"
			"	ldi		r23, %[disable_pixel]														\n\t"
			"	nop								;	(1c)													\n\t"
			"	rjmp	.+0					; (2c)													\n\t"
			"	rjmp	.+0					; (2c)													\n\t"
			"	rjmp	WIDE_loop			; (2c)													\n\t"



			
			"WIDE_loop:"
			"	ld		r28, %a[char_ptr]+  ; (+2c) read character into y-lo						\n\t"
			"   cpi     r28, 0xc0           ; (+1c) Is the pixel pattern in RAM? (top 2 bits set)	\n\t"
			"   brsh	WIDE_read_pixel_pattern_RAM	; (+1/2c) Jump if RAM based.					\n\t"
			
			"WIDE_read_pixel_pattern_ROM:	; (=4c to here from loop)								\n\t"
			"	mov		r30, r28			; (1c) move the character to lookup into z-lo			\n\t"
			"	lpm		__tmp_reg__,Z		; (3c) read byte from ROM								\n\t"
			"	rjmp	WIDE_render			; (2c)													\n\t"
			
			
			"WIDE_read_pixel_pattern_RAM:	; (=5c to here from loop)								\n\t"
			"   and		r28,r25		     	; (1c) set the appropriate top bits						\n\t"
			"	ld		__tmp_reg__,Y       ; (2c) read byte from RAM								\n\t"
			"	rjmp	WIDE_render			; (2c) [required to balance cycles]						\n\t"

			"WIDE_render:"
			"	cp		r19, r18			; (1c) should it be inverted (e.g. for cursor)?			\n\t"
			"	brne	.+2 		        ; (1/2c) skip inversion if not required					\n\t"
			"	com		__tmp_reg__			; (1c) invert											\n\t"
			"	eor		__tmp_reg__, %[invertmask]	; (1c) invert mask		\n\t"
			"	sbrs	r24, 0	            ; (1c) If 9th pixel white, skip disable pixel output	\n\t"
			"	out		%[DDR],	r23			; (1c) Disable Pixel output								\n\t"
			"	out		%[_SPDR], __tmp_reg__; (1c)	output byte to SPI hardware						\n\t"
			"	out		%[DDR], r22	        ; (1c) switch MOSI pin to output						\n\t"
			"	mov		r24, __tmp_reg__	; (1c) store pixel pattern (for next 9th pixel testing)	\n\t"
			"	subi	r18, 0x01			; (1c) decrement loop									\n\t"
			"	breq	WIDE_endloop		; (1c) [2c branch to end loop]							\n\t"
			
			"	nop								;	(1c)													\n\t"
			"	rjmp	.+0					; (2c)													\n\t"
			"	rjmp	.+0					; (2c)													\n\t"
			"	rjmp	.+0					; (2c)													\n\t"
			"	rjmp	.+0					; (2c)													\n\t"
			"	rjmp	.+0					; (2c)													\n\t"
			"	rjmp	.+0					; (2c)													\n\t"
			"	rjmp	WIDE_loop			; (2c)													\n\t"

			"WIDE_endloop:"
			"	movw		r28, r20		; restore y-lo + y_hi									\n\t"
			
			:
			:
			[char_ptr]		"x" (char_ptr),
			[visible_column_count] "M" (COL_COUNT_VISIBLE >> 1),
			[enable_pixel]  "M" ((1<<SIG_PIXEL_PIN)|(1<<SIG_SYNC_PIN)),
			[disable_pixel] "M" ((0<<SIG_PIXEL_PIN)|(1<<SIG_SYNC_PIN)),
			[initial_highlight_comparison]	"M"	(COL_COUNT_VISIBLE),
			[highlight_adjustment] "M" ((COL_COUNT_VISIBLE >> 1) + 0),
			[DDR]			"I" (_SFR_IO_ADDR(DDRB)),
			[_SPDR]			"I" (_SFR_IO_ADDR(SPDR)),
			[invertmask]		"a"	(invertmask)
			:
			"r18","r19","r20","r21","r22","r23","r24","r25","r30","r31"
		);
	_delay_cycles(16);
	scanline_end();
}
#endif


static void render_handler_fontwide( void )
{
uint8_t byteout;
uint8_t i = (COL_COUNT_VISIBLE >> 1) - 1; // only display half the number of characters
uint8_t *char_ptr = &(g_display[ s_DisplayRow ][0]) ;
uint8_t *font_ptr = (uint8_t*)(s_FontPtrHi << 8) ;
uint8_t lastbyteout = 0;
uint8_t invcount = s_HighlightColumn;
uint8_t c;
uint8_t invertmask = s_inverse_mask ;

	SPSR = 0;// turn off the SPI speed-doubling.
	c = *char_ptr++;
	asm (	"movw r30, %1    \n\t"
			"add  r30, %2    \n\t"
			"lpm  %0 , z     \n\t"
			: "=a" (byteout)
			: "r" (font_ptr),
			  "r" (c)
			: "r30","r31" ) ;
	if (invcount == COL_COUNT_VISIBLE) byteout = (uint8_t)~byteout ;
	byteout = byteout ^ invertmask ;
	SPDR = byteout ;

	uint8_t temp = (1<<SIG_PIXEL_PIN)|(1<<SIG_SYNC_PIN);
	asm(""::"r" (temp)); // force a preload of temp.
	DDRB = temp ;

	_delay_cycles(0);
	invcount -= (COL_COUNT_VISIBLE >> 1) + 1;

	while(i > 0)
	{
		lastbyteout = byteout ;
		_delay_cycles(7);
		i--;

		c = *char_ptr++ ;
		// the following code is equivalent to byteout = pgm_read_byte( font_ptr + c ),
		// except that it knows that font_ptr will be 256-byte aligned, hence a single add
		// is required.
		asm (	"movw r30, %1    \n\t"
				"add  r30, %2    \n\t"
				"lpm  %0 , z     \n\t"
				: "=r" (byteout)
				: "r" (font_ptr),
				  "r" (c)
				: "r30","r31" ) ;
		if (invcount==i) byteout = (uint8_t)~byteout;
		if (!(lastbyteout & 0b1)) PIXEL_DISABLE ;
		byteout = byteout ^ invertmask ;
		_delay_cycles(2);
		SPDR = byteout;
		PIXEL_ENABLE;
		_delay_cycles(1);
	}
	_delay_cycles(16);
	scanline_end();
	
}


#include "avr32wav.h"
#include "diskio.h"
#include "string.h"

#define SECONDS_TO_FORWARD 15
unsigned char readdata;
unsigned int count;
unsigned long int arg = 0;
unsigned char mmc_buf[512];
unsigned int fat_start, dir_start, data_start;
unsigned char sect_per_clust;
volatile unsigned char BUF1_EMPTY, BUF0_EMPTY = 0;
unsigned long int OCR1A_BACKUP;
unsigned long int bitrate;
unsigned int OCR1A_ADJUST;
unsigned int STARTING_CLUSTER;
#define F_CPU 16000000
unsigned char mmc_buf0[4];
unsigned char mmc_buf1[4];
volatile unsigned char STEREO;

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
    //OCR1A_BACKUP = ((F_CPU *(STEREO + 1))/bitrate);
    //OCR1A = OCR1A_ADJUST = OCR1A_BACKUP;
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
                if ((mmc_buf[1] != 0) && (mmc_buf[base_count + 2] != 0) && (mmc_buf[base_count] != 0xe5) && (mmc_buf[base_count] != 0x00) && ((mmc_buf[base_count + 11] & 0x1E) == 0) && (strncmp(mmc_buf + base_count + 8, FILE_EXTENSION, 3) == 0)) {
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


void fat16_init()            //BOOT SECTOR SCANNING//
{
    mmc_read_sector(0);
    clear();
    if((mmc_buf[0x36] == 'F') && (mmc_buf[0x39] == '1') && (mmc_buf[0x3a] == '6')) asm("NOP");
    else {
        while(1);
    }
    delay_ms(500);
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



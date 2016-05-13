unsigned int forward_seconds(unsigned int cluster);

char check_bitrate_and_stereo(unsigned int cluster);

unsigned int find_next_cluster(unsigned int cluster);

void mmc_read_double_buffer(unsigned long int sector, unsigned char a[]);


void play_cluster(unsigned int cluster);

unsigned int scan_root_dir(unsigned char *FILE_EXTENSION, char FNAME[], char UP_DOWN);


void fat16_init();

void mmc_read_sector(unsigned long int sector);


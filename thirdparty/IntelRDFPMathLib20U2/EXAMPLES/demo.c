#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <bid_conf.h>
#include <bid_functions.h>

int main(int argc, char *argv[]) {
	BID_UINT64 x;
	BID_UINT64 y;
	BID_UINT64 z;
	char *op;
	char buff[4096];

    bid64_add(0x2feb29430a256d21ULL, 0x7c00000000000000ULL);

    if(argc!=4) {
		fprintf(stderr, "Usage: <A> <op> <B>\n");
		return -1;
	}

	x = bid64_from_string(argv[1]);
	op = argv[2];	
	y = bid64_from_string(argv[3]);

	if (!strcmp(op, "+"))
		z = bid64_add(x, y);
	else if (!strcmp(op, "-"))
		z = bid64_sub(x, y);
	else if (!strcmp(op, "*"))
		z = bid64_mul(x, y);
	else if (!strcmp(op, "/"))
		z = bid64_div(x, y);
	else {
		fprintf(stderr, "Unsupported operation '%s'\n", op);
		return -1;
	}

	bid64_to_string(buff, x);
	printf("%s(=%llu)", buff, x);

	printf(" %s ", op);

	bid64_to_string(buff, y);
	printf("%s(=%llu)", buff, y);

	printf(" = ");

	bid64_to_string(buff, z);
	printf("%s(=%llu)\n", buff, z);

	return 0;
}

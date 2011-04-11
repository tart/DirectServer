#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <time.h>

#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>

#include "sendrecvloop.h"

#define LOCKNAMESIZE 8
#define SENDBUF_SIZE 256
#define RECVBUF_SIZE 256

int createAndConnectSocket(const char *address, const char *port);
int doGetLocks(const char *address, const char *port);
void generateLockName(char *buf, int len);

int main(int argc, char *argv[])
{
	int testResult;

	if (argc != 3) {
		printf("Usage:\n\t%s <address> <port>\n\n", argv[0]);
		return EXIT_SUCCESS;
	}

	srand(time(NULL));

	testResult = doGetLocks(argv[1], argv[2]);

	if (testResult) {
		printf("Test failed (%i).\n", testResult);
		return EXIT_FAILURE;
	} else {
		printf("Test succeeded.\n");
		return EXIT_SUCCESS;
	}
}

int createAndConnectSocket(const char *address, const char *port)
{
	struct addrinfo addrInfoHints;
	struct addrinfo *addrInfoResult, *ai;

	int sock;
	int ret;

	memset(&addrInfoHints, 0, sizeof(addrInfoHints));
	addrInfoHints.ai_family   = AF_UNSPEC;   //IPv4 or IPv6
	addrInfoHints.ai_socktype = SOCK_STREAM; //TCP

	ret = getaddrinfo(address, port, &addrInfoHints, &addrInfoResult);
	if (ret != 0) {
		fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(ret));
		return -1;
	}

	for (ai = addrInfoResult; ai != NULL; ai = ai->ai_next) {
		sock = socket(ai->ai_family, ai->ai_socktype, ai->ai_protocol);

		if (sock == -1)
			continue;

		if (connect(sock, ai->ai_addr, ai->ai_addrlen) != -1) //success
			break;

		close(sock);
	}

	freeaddrinfo(addrInfoResult);

	if (ai == NULL) {
		fprintf(stderr, "Unable to connect.\n");
		return -1;
	}

	return sock;
}

int doGetLocks(const char *address, const char *port)
{
	int sock;
	unsigned char sbuf[SENDBUF_SIZE];
	unsigned char rbuf[RECVBUF_SIZE];
	int i;

	sock = createAndConnectSocket(address, port);

	if (sock == -1) {
		return 2;
	}

	int lockCount = 1000;
	char lockName[LOCKNAMESIZE+1] = {0};
	printf("Acquiring %i locks...\n", lockCount);

	for (i=0; i<lockCount; ++i) {
		generateLockName(lockName, LOCKNAMESIZE);

		sbuf[0] = 'L';
		sbuf[1] = 'L';
		sbuf[2] = LOCKNAMESIZE;
		memcpy(sbuf+3, lockName, LOCKNAMESIZE);

		if (!sendloop(sock, sbuf, 3+LOCKNAMESIZE, 0)) {
			fprintf(stderr, "Unable to send to socket.\n");
			return -1;
		}

		if (!recvloop(sock, rbuf, 1, 0)) {
			fprintf(stderr, "Unable to recv from socket.\n");
			return -2;
		}

		int acquired;

		if (rbuf[0] == 'T') {
			acquired = 1;
		} else if (rbuf[0] == 'F') {
			acquired = 0;
		} else {
			fprintf(stderr, "Invalid response from server.\n");
			return -40;
		}

		printf("Lock \"%s\": %s\n", lockName, (acquired ? "acquired" : "denied"));
	}

	printf("%i locks processed.\n", lockCount);

	fgetc(stdin);

	close(sock);

	return 0;
}

void generateLockName(char *buf, int len)
{
	int i;

	for (i=0; i<len; ++i) {
		buf[i] = (rand() % 26) + 'a';
	}
}

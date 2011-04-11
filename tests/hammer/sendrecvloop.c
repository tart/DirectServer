#include "sendrecvloop.h"

int sendloop(int sockfd, const void *buf, size_t len, int flags)
{
	ssize_t sendlen;

	while (len > 0) {
		sendlen = send(sockfd, buf, len, flags);

		if (sendlen == -1) {
			return 0;
		}

		len -= sendlen;
		buf += sendlen;
	}

	return 1;
}

int recvloop(int sockfd, void *buf, size_t len, int flags)
{
	ssize_t recvlen;

	while (len > 0) {
		recvlen = recv(sockfd, buf, len, flags);

		if (recvlen == -1) {
			return 0;
		}

		len -= recvlen;
		buf += recvlen;
	}

	return 1;
}

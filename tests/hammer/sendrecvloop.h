#ifndef SENDRECVLOOP_H_
#define SENDRECVLOOP_H_

#include <unistd.h>
#include <sys/socket.h>

int sendloop(int sockfd, const void *buf, size_t len, int flags);
int recvloop(int sockfd, void *buf, size_t len, int flags);

#endif /* SENDRECVLOOP_H_ */

/* This is the server side code.
 * This code is not part of the Android app
 * but it should be compiled and run in another machine
 * that acts like a server
 * In my project this program is running in a raspberry pi.
 */
#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>

int main(int argc, char**argv)
{
    int sockfd,n;
    struct sockaddr_in servaddr,cliaddr;
    socklen_t len;
    char mesg[1000];
    int returnv;

    sockfd=socket(AF_INET,SOCK_DGRAM,0);

    bzero(&servaddr,sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr=htonl(INADDR_ANY);

    // use port #3444
    servaddr.sin_port=htons(3444);
    bind(sockfd,(struct sockaddr *)&servaddr,sizeof(servaddr));
    FILE *fp;
    for (;;)
        for (;;)
        {
            len = sizeof(cliaddr);
            n = recvfrom(sockfd,mesg,1000,0,(struct sockaddr *)&cliaddr,&len);
            printf("-------------------------------------------------------\n");
            mesg[n] = 0;
            printf("%s",mesg);
            fp=fopen("log.txt", "a");
            fprintf(fp, "-------------------------------------------------------\n");

            fprintf(fp, mesg);
            fclose(fp);

        }
}


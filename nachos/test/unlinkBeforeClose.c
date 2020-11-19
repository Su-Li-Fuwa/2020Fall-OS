/* Test for using create */

#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  int i=1, j=1, cpid, status;
  char buffer[100]="Hello World!!!!\0", tmp[100]={};
  if (argc != 3) {
    printf("Usage: unlinkBeforeClose <src><dst>\n");
    return -1;
  }
  i = creat(argv[1]);
  printf("%d\n",i);
  j = write(i, &buffer, strlen(buffer));
  printf("%d\n",j);
  close(i);
  i = open(argv[1]);
  j = read(i, &tmp, 100);
  printf("%d\n",j);
  printf("%s\n", tmp);
  unlink(argv[1]);
  cpid = exec("cp.coff", 3, argv);
  join(cpid, &status);
  return 0;
}

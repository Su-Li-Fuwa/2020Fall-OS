/* Test for using create */

#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  int i=1, j=1, mPid = 0,cPid, status;
  char buffer[100]="Hello World!!!!\0", tmp[100]={};
  if (argc != 3) {
    printf("Usage: twoProcessesOpen <name><new string>\n");
    return -1;
  }
  i = creat(argv[1]);
  j = write(i, &buffer, strlen(buffer));
  close(i);
  i = open(argv[1]);
  cPid = exec("writeAndClose.coff", 3, argv);
  join(cPid, &status);
  j = read(i, &tmp, 100);
  printf("First\t %s\n", tmp);
  close(i);
  i = open(argv[1]);
  j = read(i, &tmp, 100);
  printf("Second\t %s\n", tmp);
  close(i);
  return 0;
}

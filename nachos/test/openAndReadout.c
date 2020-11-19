/* Test for using create */

#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  int i=1, j=1;
  char buffer[100]={};
  if (argc != 2) {
    printf("Usage: openAndRead <dst>\n");
    return -1;
  }
  i = open(argv[1]);
  printf("%d\n",i);
  j = read(i, &buffer, 10);
  printf("%d\n", j);
  printf("%s\n", buffer);
  return 0;
}

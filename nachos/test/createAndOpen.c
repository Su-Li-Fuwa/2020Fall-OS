/* Test for using create */

#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  int i=1, j=1;
  if (argc != 2) {
    printf("Usage: createAndOpen <dst>\n");
    return -1;
  }
  i = creat(argv[1]);
  printf("%d\n",i);
  j = open(argv[1]);
  printf("%d\n",j);
  return 0;
}

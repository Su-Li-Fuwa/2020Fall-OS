/* Test for using create */

#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  int i, j;
  i = open(argv[1]);
  j = write(i, argv[2], strlen((const char*)argv[2]));
  //j = write(i, argv[2], 7);
  close(i);
  return 0;
}

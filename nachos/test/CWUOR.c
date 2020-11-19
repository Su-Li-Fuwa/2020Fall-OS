/* Test for using create */

#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  int i=1, j=1;
  char buffer[100]="Hello World!";
  if (argc != 2) {
    printf("Usage: createAndOpen <dst>\n");
    return -1;
  }
  i = creat(argv[1]);
  printf("%d\n",i);
  j = write(i, &buffer, 100);
  printf("%d\n",j);
  j = read(i, &buffer, 100);
  printf("%d\n",j);
  printf("%s\n", buffer);
  
  return 0;
}

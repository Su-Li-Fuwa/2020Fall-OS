/* Test for using create */

#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  int i=1, j=1;
  char* name;
  while(j != -1){
    // Pass the argv[0] end with .coff, does not matter the functionality.
    j = creat("asss");
    printf("Create Done with descriptor %d\n", j);
    //getchar();
    i += 1;
  }
  printf("%d\n",i);
  return 0;
}

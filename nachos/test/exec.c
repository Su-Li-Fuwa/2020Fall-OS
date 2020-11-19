/* Test for using exec */

#include "stdio.h"
#include "stdlib.h"

int main(int argc, char** argv)
{
  int i, status;
  // printf("arg %s\n", argv[1]);
  // Pass the argv[0] end with .coff, does not matter the functionality.
  i = exec(argv[1],argc-1, argv + 1);
  join(i, &status);
  printf("Exec Done with status %d\n", status);
  return 0;
}

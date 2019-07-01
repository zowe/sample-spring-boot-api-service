#include "wtoexec.h"                                                                  
#include "wto.h"                                                                      
                                                                                      
#include <stdlib.h>                                                                   
#include <stdio.h>                                                                    
                                                                                      
/**                                                                                   
 * Force the compiler and assembler to treat this function as a                       
 * "main" to aquire stack space.  For production, create your                         
 * own or use your site's PROLOG & EPILOG.                                            
 */                                                                                   
#pragma prolog(WTOEXE, "&CCN_MAIN SETB 1 \n MYPROLOG")                                
                                                                                      
int WTOEXE(int *number, const char *string)                                   
{                                                                                     
    WTO_BUF buf = {0};                                                                
    buf.len = sprintf(buf.msg, "Number was: '%d'; String was: '%s'", *number, string);
    return wto(&buf);                                                                 
}                                                                                     
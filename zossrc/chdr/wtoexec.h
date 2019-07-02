#ifndef WTOEXEC_H                                                                           
#define WTOEXEC_H                                                                           
                                                                                            
/**                                                                                         
 * C/C++ interlanguage - "OS" meaning to call non-Language Environment conforming assembler.
 */                                                                                         
#if defined(__cplusplus) && (defined(__IBMCPP__) || defined(__IBMC__))                      
extern "OS"                                                                                 
{                                                                                           
#elif defined(__cplusplus)                                                                  
extern "C"                                                                                  
{                                                                                           
#endif                                                                                      
                                                                                            
int WTOEXE(int *, const char *);                                                            
                                                                                            
#if defined(__cplusplus)                                                                    
}                                                                                           
#endif                                                                                      
                                                                                            
#endif                                                                                      
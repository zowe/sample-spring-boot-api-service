#ifndef WTO_H                                                             
#define WTO_H                                                             
                                                                          
#if defined(__IBM_METAL__)                                                
                                                                          
#define WTO_MODEL(wtom)                                         \         
    __asm(                                                      \         
        "*                                                  \n" \         
        " WTO TEXT=,"                                           \         
        "ROUTCDE=(11),"                                         \         
        "DESC=(6),"                                             \         
        "MF=L                                               \n" \         
        "*                                                    " \         
        : "DS"(wtom));                                                    
#else                                                                     
#define WTO_MODEL(wtom)                                                   
#endif                                                                    
                                                                          
WTO_MODEL(wtoModel); // make this copy in static storage                  
                                                                          
#if defined(__IBM_METAL__)                                                
#define WTO(buf, plist, rc)                                     \         
    __asm(                                                      \         
        "*                                                  \n" \         
        " SLGR  0,0       Save RC                           \n" \         
        "*                                                  \n" \         
        " WTO TEXT=(%2),"                                       \         
        "MF=(E,%0)                                          \n" \         
        "*                                                  \n" \         
        " ST    15,%1     Save RC                           \n" \         
        "*                                                    " \         
        : "+m"(plist),                                          \         
          "=m"(rc)                                              \         
        : "r"(buf)                                              \         
        : "r0", "r1", "r14", "r15");                                      
#else                                                                     
#define WTO(buf, plist, rc)                                               
#endif                                                                    
#define MAX_WTO_TEXT 126                                                  
                                                                          
typedef struct                                                            
{                                                                         
    short int len;                                                        
    char msg[MAX_WTO_TEXT];                                               
} WTO_BUF;                                                                
                                                                          
static int wto(WTO_BUF *buf)                                              
{                                                                         
    int rc = 0;                                                           
                                                                          
    WTO_MODEL(dsaWtoModel); // stack var                                  
    dsaWtoModel = wtoModel; // copy model                                 
    WTO(buf, dsaWtoModel, rc);                                            
                                                                          
    return rc;                                                            
}                                                                         
             
#endif       
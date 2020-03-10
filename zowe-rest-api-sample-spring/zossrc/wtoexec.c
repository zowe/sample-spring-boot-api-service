/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 * 
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 * 
 * Copyright Contributors to the Zowe Project.
 */
#include "wtoexec.h"
#include "wto.h"

#include <stdlib.h>
#include <stdio.h>

/**
 * Metal C implementation running with z/OS linkage conventions and called
 * via JNI - C++ layer.
 * 
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
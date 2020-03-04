#ifndef WTOEXEC_H
#define WTOEXEC_H

/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 * 
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 * 
 * Copyright Contributors to the Zowe Project.
 */

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
#ifndef WTO_H
#define WTO_H

/**
 * Assembler and C function wrappers to call WTO
 */

/*
 * This program and the accompanying materials are made available and may be used, at your option, under either:
 * * Eclipse Public License v2.0, available at https://www.eclipse.org/legal/epl-v20.html, OR
 * * Apache License, version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 * 
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 * 
 * Copyright Contributors to the Zowe Project.
 */

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
#define WTO_MODEL(wtom) void *wtom;  // bogus definition to prevent warnings in IDEs
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

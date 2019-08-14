# makefile boilerplate to build the JNI implementation (C++), metal C code, and
# bind into a "shared object" called at REST API runtime via the /wto endpoint
#
# This program and the accompanying materials are made available under the terms of the
# Eclipse Public License v2.0 which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-v20.html
#
# SPDX-License-Identifier: EPL-2.0
#
# Copyright Contributors to the Zowe Project.

CXX = xlC
CC = xlc
ASMFLAGS = -mrent

MTL_OPTS = metal,\
 langlvl(extended),\
 sscom,\
 nolongname,\
 inline,\
 genasm,\
 inlrpt,\
 csect,\
 nose,\
 lp64,\
 list,\
 warn64,\
 optimize(2),\
 list,\
 showinc,\
 showmacro,\
 source,\
 aggregate

MTL_FLAGS = -S -W "c,$(MTL_OPTS)"

MACLIBS = -ISYS1.MACLIB \
 -ICBC.SCCNSAM

MTL_HEADERS = -I/usr/include/metal

DLL_CPP_FLAGS = -W "c,lp64,langlvl(extended),dll,XPLINK,exportall" \
 -qsearch=/sys/java64bt/v8r0m0/usr/lpp/java/J8.0_64/include \
 -qsource \
 -c

DLL_BND_FLAGS = -W "l,lp64,dll,dynam=DLL,XPLINK,map,list" \
 -qsource

ASM = as
ASM_FLAGS = -mrent

libwtojni.so: wtojni.o wtoexec.o
	$(CXX) $(DLL_BND_FLAGS) -o $@ $^ > $*.bind.lst
	extattr +p $@

wtojni.o: wtojni.cpp
	$(CXX) $(DLL_CPP_FLAGS) -qlist=$*.cpp.lst -o $@ $^

wtoexec.o: wtoexec.s
	$(ASM) $(ASM_FLAGS) -a=$*.asm.lst $(MACLIBS) -o $@ $^

wtoexec.s: wtoexec.c
	$(CC) $(MTL_FLAGS) -qlist=$*.mtl.lst $(MTL_HEADERS) -o $@ $^

libsecur.so: secur.o jnitools.o
	$(CXX) $(DLL_BND_FLAGS) -o $@ $^ > $*.bind.lst
	extattr +p $@

secur.o: secur.cpp
	$(CXX) $(DLL_CPP_FLAGS) -qlist=$*.cpp.lst -o $@ $^

jnitools.o: jnitools.cpp
	$(CXX) $(DLL_CPP_FLAGS) -qlist=$*.cpp.lst -o $@ $^

clean:
	rm *.o
	rm *.lst
	rm *.x
	rm *.so
	rm wtoexec.s

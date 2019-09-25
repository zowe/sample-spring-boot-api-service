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

CXX=xlc++
CC=xlc
ASM=as

MTL_OPTS=metal,\
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

MTL_FLAGS=-S -W "c,$(MTL_OPTS)"

MACLIBS=-ISYS1.MACLIB \
 -ICBC.SCCNSAM

MTL_HEADERS=-I/usr/include/metal

JAVA_HOME := /usr/lpp/java/J8.0_64

DLL_CPP_FLAGS=-W "c,lp64,langlvl(extended),dll,xplink,exportall"\
 -qsearch=$(JAVA_HOME)/include \
 -qsource \
 -g \
 -c

DLL_BND_FLAGS=-W "l,lp64,dll,dynam=dll,xplink,map,list" \
 -g \
 -qsource

ASM_FLAGS=-mrent

SIDEDECKPATH = $(JAVA_HOME)/bin/j9vm
SIDEDECK = libjvm

PREFIX := "../lib/"

all: libzowe-sample.so

install:
	mkdir -p $(PREFIX)
	cp -vp *.so $(PREFIX)
	extattr +p $(PREFIX)/*.so
	ls -E $(PREFIX)

libzowe-sample.so: wtojni.o wtoexec.o
	$(CXX) $(DLL_BND_FLAGS) -o $@ $^ > $*.bind.lst
	extattr +p $@

wtojni.o: wtojni.cpp
	$(CXX) $(DLL_CPP_FLAGS) -qlist=$*.cpp.lst -o $@ $^

wtoexec.o: wtoexec.s
	$(ASM) $(ASM_FLAGS) -a=$*.asm.lst $(MACLIBS) -o $@ $^

wtoexec.s: wtoexec.c
	$(CC) $(MTL_FLAGS) -qlist=$*.mtl.lst $(MTL_HEADERS) -o $@ $^

clean:
	rm *.o
	rm *.lst
	rm *.x
	rm *.so
	rm *.dbg
	rm wtoexec.s

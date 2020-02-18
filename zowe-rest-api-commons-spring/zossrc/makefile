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

DLL_CPP_FLAGS=-W "c,lp64,langlvl(extended),dll,xplink,exportall" \
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

LIB_SECUR = "libzowe-commons-secur.so"

all: $(LIB_SECUR)

install:
	mkdir -p $(PREFIX)
	cp -vp *.so $(PREFIX)
	ls -E $(PREFIX)

$(LIB_SECUR): secur.o jnitools.o
	$(CXX) $(DLL_BND_FLAGS) -o $@ $(SIDEDECKPATH)/$(SIDEDECK).x jnitools.o $^ > $*.bind.lst
	extattr +p $@
	pax -wvf $@.pax $@

secur.o: secur.c
	$(CC) $(DLL_CPP_FLAGS) -qlist=$*.cpp.lst -o $@ $^

jnitools.o: jnitools.c
	$(CC) $(DLL_CPP_FLAGS) -qlist=$*.cpp.lst -o $@ $^

clean:
	rm *.o
	rm *.lst
	rm *.x
	rm *.so
	rm *.dbg
	rm *.s

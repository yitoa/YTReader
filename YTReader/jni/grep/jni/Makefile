srcdir=.

CC=gcc

DEFS=-DGREP  -DSTDC_HEADERS=1 -DHAVE_STRING_H=1 -DHAVE_SYS_PARAM_H=1 -DHAVE_UNISTD_H=1 -DHAVE_ALLOCA_H=1 -DHAVE_GETPAGESIZE=1 -DHAVE_MEMCHR=1 -DHAVE_STRERROR=1 -DHAVE_VALLOC=1 -DHAVE_WORKING_MMAP=1

CFLAGS=-O
LDFLAGS=$(CFLAGS)

SRCS=grep.c getopt.c regex.c dfa.c kwset.c obstack.c search.c
OBJS=grep.o getopt.o regex.o dfa.o kwset.o obstack.o search.o

.c.o:
	$(CC) $(CFLAGS) $(DEFS) -I$(srcdir) -c $<

all: grep 

grep: $(OBJS)
	$(CC) $(LDFLAGS) -o grep $(OBJS)

clean:
	rm -f grep *.o
dfa.o search.o: dfa.h
grep.o search.o: grep.h
kwset.o search.o: kwset.h
kwset.o obstack.o: obstack.h
regex.o search.o: regex.h

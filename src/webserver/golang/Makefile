run: clean simple_server
	./simple_server
 
include $(GOROOT)/src/Make.inc

TARG = simple_server
GOFILES = \
	simple_server.go \
	webclient.go \
	database.go \
	messages.go \
	election.go

include $(GOROOT)/src/Make.cmd

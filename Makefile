# Assignment 1
JAR=hw1.jar

# Main target
all: compile

# OS X
ifeq "$(shell uname)" "Darwin"
LIBS=-cp ".:jogl-macosx/*"
JFLG=-d bin/ -source 1.7
# Linux/Unix/Solaris
else
LIBS=-cp ".:jogl-linux/*"
JFLG=-d bin/ -source 1.7
endif

compile:
	if [ ! -d "bin/" ]; then \
		mkdir "bin/"; \
	fi
	javac $(LIBS) $(JFLG) src/**/*.java
	jar vcmf manifest.txt $(JAR) -C bin/ . 

# Cleaning
clean:
	rm -rf bin/*
	rm $(JAR)

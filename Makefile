# Makefile for assignment 2 Java - Parallel Programming
# Author: Ian Wilkes

JAVAC= javac

all: CoinFlipper

CoinFlipper:
	$(JAVAC) CoinFlipper.java

clean:
	rm -rf *.class

JFLAGS = -g
JC = javac
JVM= java 
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	keywordcounter.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class		
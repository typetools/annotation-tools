# Put user-specific changes in your own Makefile.user.
# Make will silently continue if that file does not exist.
-include Makefile.user


PNGFILES = corresp.png dataflow.png nocast.png typecast.png

all: ${EPSFILES} ${PDFFILES} ${PNGFILES} png-copy svg-copy

png-copy: ${PNGFILES}
	cp -pf ${PNGFILES} ..

svg-copy:
	cp -pf *.svg ..

%.png : %.dot
	dot -Tpng $< > `basename $< .dot`.png

clean:
	@\rm -f *.eps
	@\rm -f *.pdf
	@\rm -f *.png

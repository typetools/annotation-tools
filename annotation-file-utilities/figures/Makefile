# Put user-specific changes in your own Makefile.user.
# Make will silently continue if that file does not exist.
-include Makefile.user

DIA?=dia

EPSFILES = scene-lib-type-hierarchy.eps
PDFFILES = scene-lib-type-hierarchy.pdf
PNGFILES = scene-lib-type-hierarchy.png tool-relations.png \
           corresp.png dataflow.png nocast.png typecast.png

all: ${EPSFILES} ${PDFFILES} ${PNGFILES} png-copy

png-copy: ${PNGFILES}
	cp -pf ${PNGFILES} ..

%.eps : %.dia
	$(DIA) -t eps $<

%.pdf : %.eps
	epstopdf $<

scene-lib-type-hierarchy.png : scene-lib-type-hierarchy.dia
	$(DIA) -t png scene-lib-type-hierarchy.dia

tool-relations.png : tool-relations.dia
	$(DIA) -t png tool-relations.dia

%.png : %.dot
	dot -Tpng $< > `basename $< .dot`.png

clean:
	@\rm -f *.eps
	@\rm -f *.pdf
	@\rm -f *.png

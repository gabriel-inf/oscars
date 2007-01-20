
## Generic rules for tex stuff
##
## Borrowed from Mark Allman (mallman@grc.nasa.gov)

.PHONY: html

TEX        := latex
DVI2PS     := dvips
DISTILL    := ps2pdf14

# All needed input files.

LOCAL_BIB  := paper.bib

BASE       := gridnets
TEX_FILES  := $(BASE).tex
#FIG_FILES  := figures/*.ps
#TAB_FILES  := tables/*.tex

# All output files.

BASE_DVI   := $(BASE).dvi
BASE_PS    := $(BASE).ps
BASE_PDF   := $(BASE).pdf

# General rules.

%.dvi : %.tex

default: $(BASE_DVI)

ps: $(BASE_PS)

pdf: $(BASE_PDF)

everything: clobber full final

full: $(BASE_DVI) bib full2

final: $(BASE_DVI) $(BASE_PS) $(BASE_PDF)

$(BASE_PS): $(BASE_DVI)
	$(DVI2PS) -o $(BASE_PS) -Ppdf -Pcmz -Pamz -t letter -D 600 -G0 $(BASE_DVI)

$(BASE_PDF): $(BASE_PS)
	$(DISTILL) $(BASE_PS)

$(BASE_DVI): $(TEX_FILES) $(FIG_FILES) $(TAB_FILES)
	latex $(BASE)

bib2:
	sh -c "grep citation $(BASE).aux; true" > CITES
	if [ -s CITES ]; then \
		bibtex $(BASE); \
	fi
	rm -f CITES

full2: bib2
	latex $(BASE)
	latex $(BASE)

bib: 
	if [ -h $(LOCAL_BIB) ];	then \
		$(MAKE); \
		$(MAKE) full2; \
		rm $(LOCAL_BIB); \
		bibgen $(BASE); \
	else \
		if [ -f $(BIB_FILE) ]; then \
			rm $(LOCAL_BIB); \
			ln -s $(BIB_FILE) $(LOCAL_BIB); \
			$(MAKE) bib; \
		fi \
	fi

#html: everything
#	rm -rf emodel
#	latex2html -math -split 0 -show_section_numbers -local_icons emodel.tex
#	latex2html -math -split 0 -show_section_numbers -local_icons emodel.tex

#	cat history/index.html |./tweak_affiliations > tmp.tmp.tmp
#	mv tmp.tmp.tmp history/index.html
#	cp history/index.html history/history.html

# Housekeeping.

clean:
	rm -f *.aux *.dvi *.log *.bbl *.blg
	rm -f .cmaker.tmp*
	rm -f CITES

clobber: clean
	rm -f $(BASE_PS) $(BASE_PDF)
	rm -f *ps *epsi
#	rm -rf emodel
#	rm -rf emodel.ent

distclean: clobber


8 symbols used in the dashboard of the game:


Bin: U+1F5D1    \ud83d\uddd1



# generate a font file with ONLY the chars we need

pyftsubset NotoSansSymbols-Regular.ttf \
  --unicodes=U+2691 \ 
  --output-file=flag-subset.ttf --no-hinting --desubroutinize

pyftsubset NotoSansSymbols2-Regular.ttf \
  --unicodes=U+1F0AD,U+1F0A0 \
  --output-file=symbols-subset.ttf --no-hinting --desubroutinize

pyftsubset NotoSansKR-Regular.ttf \
  --unicodes=U+C6C3 \                         
  --output-file=hangul-subset.ttf --no-hinting --desubroutinize

pyftsubset NotoEmoji-Regular.ttf \
  --unicodes=U+1F9D1,U+1F916,U+1F480 \
  --output-file=emoji-subset.ttf --no-hinting --desubroutinize

pyftsubset Symbola-Regular.ttf \
  --unicodes=U+1F5D1 \
  --output-file=bin-subset.ttf --no-hinting --desubroutinize


# now we want to scale them all to be the same upem


PYBIN="$(brew --prefix fonttools)/libexec/bin/python3"                                                        

"$PYBIN" -c "
from fontTools.ttLib import TTFont
from fontTools.ttLib.scaleUpem import scale_upem

for path in ['flag-subset.ttf', 'emoji-subset.ttf', 'bin-subset.ttf']:
    font = TTFont(path)
    scale_upem(font, 1000)
    font.save(path.replace('.ttf', '-1000upm.ttf'))
"

# merge all 4 font files into 1 (--verbose after merge for more info)

fonttools merge flag-subset-1000upm.ttf symbols-subset.ttf hangul-subset.ttf emoji-subset-1000upm.ttf bin-subset-1000upm.ttf \
  --drop-tables=vhea,vmtx \
  --output-file=guaranteed.ttf


# clean up all notdef glyphs, as each font has its own one at the start, but we only need 1 in our final font

pyftsubset guaranteed.ttf \
  --unicodes=U+2691,U+1F0AD,U+1F0A0,U+C6C3,U+1F9D1,U+1F916,U+1F480,U+1F5D1 \
  --output-file=guaranteed-final.ttf --no-hinting --desubroutinize
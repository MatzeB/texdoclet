TexDoclet 0.9.3 (2009-12-17)
============================

* Add support for enums

TexDoclet 0.9.2 (2009-12-14)
============================

* Improve preamble.tex (\newpage instead of \pagebreak to avoid unnecessary
  stretching; create a minipage around texdocparameters in tabular environemnt
  to have linebreaking working)
* Support for {@inheritDoc}

TexDoclet 0.9.1 (2009-12-10)
============================

* Always create UTF-8 encoded output and adapt preamble.tex to show how
  to proces this in latex.
* Small improvements to preamble.tex
* Changed usage of texdocmethod and texdocconstructor macros to have
  different parameters for modifiers and return type.

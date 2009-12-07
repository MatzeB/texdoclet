TexDoclet
=========

This is a javadoc doclet - a plugin for the [javadoc tool](http://java.sun.com/j2se/javadoc/). This doclet produces output suitable to
be included into LaTeX documents.
This version is based on [TexDoclet](https://texdoclet.dev.java.net/) with big
parts of the code rewritten and changed to support a separation from content
and layout. The sourcecode is also cleaner than the original.

Features
--------

* Commonly used html tags are converted into latex equivalents.
* Layout is defined by a set of macros outside the tool. This allows you to
  easily adapt to your own style/document without the need to touch the tools
  output.

Usage
-----

1. Produce the latex documentation by calling the doclet. Usually this works
   like this on the commandline:
	javadoc -docletpath texdoclet.jar -doclet org.wonderly.doclets.TexDoclet my.cool.package
   This should produce a file docs.tex
2. You may test the generated output by copying preamble.tex into the current
   directory and compiling it with pdflatex (preamble.tex includes docs.tex)
3. Copy the definitions inside preamble.tex into the preamble of your own latex
   document. Adapt the macros to your style and language needs.
   Use \input{docs.tex} inside your document to include the generated
   documentation.

Author/Contact
--------------

Matthias Braun <matthias.braun@kit.edu>

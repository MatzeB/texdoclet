TexDoclet
=========

This is a javadoc doclet - a plugin for the [javadoc tool](http://java.sun.com/j2se/javadoc/). This doclet produces output suitable to
be included into LaTeX documents.
This version is based on [TexDoclet](https://texdoclet.dev.java.net/) from Gregg Wonderly with the changes made and
applied to by [Matthias Braun](https://github.com/MatzeB). See his github project for details.

My fork adapts the doclet to meet the requirements of our project [lambda-alligatoren](https://github.com/vincent23/lambda-alligatoren).
In detail, those are:
* @texignore tag support
* modify the original "preamble.tex" to become an "\\input"-able "docletcommands.tex" (thanks to [@vincent23](https://github.com/vincent23))
* fix a surplus "}" in HTMLtoTex which broke ordered lists

Build instructions
------------------
* The ant Build.xml file provided does NOT run on its own.
* You will need to open the project in Eclipse
* You will probably have to update the path to "tools.jar" in the project's build path properties if you aren't on linux and/or using an SDK different to openjdk 7
* You can build a self-contained doclet jar file by exporting the project as jar from eclipse

Features
--------

* Commonly used html tags are converted into latex equivalents.
* Layout is defined by a set of macros outside the tool. This allows you to
  easily adapt to your own style/document without the need to touch the tools
  output.
* Classes not intended to be part of the generated tex document can be excluded with a dedicated "texignore" tag.
* Shows generalization and realizations
* Has doclet specific tex commands outsourced to allow easy integration into existing documents.

Commandline Options
-------------------
* -output: Defines the output file path. Default is "docs.tex"
* -silent: If this option is specified, all usually unnecessary messages are muted. Error messages will be printed still.
* -see: Specifies the text to use for references created from inline tags. The default text is "see "
* -See: Specifies the text to use for references created from block tags. The default text is "See also ".
* -help: Prints a help text similar to this

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

Original Author/Contact
--------------

Matthias Braun <matthias.braun@kit.edu>

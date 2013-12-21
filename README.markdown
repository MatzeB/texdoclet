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
* Shows generalization and realizations ("extends" and "implements")
* Shows dimensions of types, like in "String[][]".
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

   This should produce a file docs.tex. See "man javadoc" and the section above for additional command line arguments.

2. You will need to also input "docletcommands.tex" to the preamble of your main document, otherwise
   you will get a lot of errors due to missing commands.

3. \\Input the generated "docs.tex" (or the file with the name you specified via -output) at any place within 
   your document environment.
4. If you like headers, put \\pagestyle{myheadings} in front of the \\input command you just entered.
   You can turn off the headers again by re-executing \\pagestyle{plain} or whichever pagestyle you were using before.

Original Author/Contact
--------------

Matthias Braun <matthias.braun@kit.edu>

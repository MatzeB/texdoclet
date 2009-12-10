package org.wonderly.doclets;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.LanguageVersion;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ParameterizedType;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;

/**
 * Note: This version is heavily modified by Matthias
 * Braun<matthias.braun@kit.edu>
 * 
 * This class provides a Java 2, <code>javadoc</code> Doclet which generates a
 * LaTeX2e document out of the java classes that it is used on.
 * 
 * It converts commonly used html tags to equivalent latex constructs (see
 * HTMLToTex for details) Not working yet: - type parameters for
 * class/interface/methods are not printed yet - only a subset of the javadoc
 * tags are handled (param and return mostly)
 * 
 * {@link #TexDoclet TexDoclet} {@link #start(RootDoc) start}
 * 
 * @author <a href="mailto:gregg.wonderly@pobox.com">Gregg Wonderly</a>
 * @author <a href="mailto:matthias.braun@kit.edu">Matthias Braun</a>
 */
public class TexDoclet extends Doclet {
	/** Writer for writing to output file */
	private static PrintWriter os = null;
	private static String outfile = "docs.tex";

	/**
	 * Returns how many arguments would be consumed if <code>option</code> is a
	 * recognized option.
	 * 
	 * @param option
	 *            the option to check
	 */
	public static int optionLength(String option) {
		if (option.equals("-output"))
			return 2;
		else if (option.equals("-classfilter"))
			return 2;
		else if (option.equals("-help")) {
			System.err.println("TexDoclet Usage:");
			System.err.println("-output <outfile>     Specifies the output file to write to.  If none");
			System.err.println("                      specified, the default is docs.tex in the current");
			System.err.println("                      directory.");

			return 1;
		}

		System.out.println("unknown option " + option);
		return Doclet.optionLength(option);
	}

	/**
	 * Checks the passed options and their arguments for validity.
	 * 
	 * @param args
	 *            the arguments to check
	 * @param err
	 *            the interface to use for reporting errors
	 */
	static public boolean validOptions(String[][] args, DocErrorReporter err) {
		for (int i = 0; i < args.length; ++i) {
			if (args[i][0].equals("-output")) {
				outfile = args[i][1];
			}
		}
		return true;
	}

	/** indicate that we can handle (most) 1.5 language features */
	static public LanguageVersion languageVersion() {
		return LanguageVersion.JAVA_1_5;
	}

	/**
	 * Called by the framework to format the entire document
	 * 
	 * @param root
	 *            the root of the starting document
	 */
	public static boolean start(RootDoc root) {
		System.out.println("TexDoclet 4.0, Copyright 2009 - Matthias Braun");
		System.out.println("based on TexDoclet v3.0, Copyright 2003 - Gregg Wonderly.");
		System.out.println("http://texdoclet.dev.java.net - on the World Wide Web.");

		try {
			/* Open output file and force an UTF-8 encoding */
			FileOutputStream bytestream = new FileOutputStream(outfile);
			OutputStreamWriter charstream = new OutputStreamWriter(bytestream, Charset.forName("UTF-8"));
			os = new PrintWriter(charstream);
		} catch (FileNotFoundException fileNotFound) {
			throw new RuntimeException("Couldn't create output file '" + outfile + "'", fileNotFound);
		}

		ClassDoc[] classes = root.specifiedClasses();
		PackageDoc[] packages = root.specifiedPackages();

		for (PackageDoc pkg : packages) {

			System.out.println("* Package: " + pkg.name());

			os.println("\\begin{texdocpackage}{" + HTMLToTex.convert(pkg.name()) + "}");
			os.println("");

			printClasses(pkg.allClasses());

			os.println("\\end{texdocpackage}");
			os.println("");
			os.println("");
			os.println("");
		}

		printClasses(classes);

		os.close();
		return true;
	}

	private static void printClasses(ClassDoc[] classes) {
		Arrays.sort(classes, new Comparator<ClassDoc>() {
			@Override
			public int compare(ClassDoc o1, ClassDoc o2) {
				return o1.name().compareToIgnoreCase(o2.name());
			}
		});

		for (ClassDoc cd : classes) {
			printClass(cd);
		}
	}

	private static void printClass(ClassDoc cd) {
		String type = cd.isInterface() ? "interface" : "class";

		os.println("\\begin{texdocclass}{" + type + "}{"
				+ HTMLToTex.convert(cd.name()) + "}");

		os.println("\\begin{texdocclassintro}");
		os.println(HTMLToTex.convert(cd.commentText()));
		os.println("\\end{texdocclassintro}");

		FieldDoc[] fields = cd.fields();
		if (fields.length > 0) {
			os.println("\\begin{texdocclassfields}");
			printFields(cd, fields);
			os.println("\\end{texdocclassfields}");
		}

		ConstructorDoc[] constructors = cd.constructors();
		if (constructors.length > 0) {
			os.println("\\begin{texdocclassconstructors}");
			printExecutableMembers(cd, constructors, "constructor");
			os.println("\\end{texdocclassconstructors}");
		}

		MethodDoc[] methods = cd.methods();
		if (methods.length > 0) {
			os.println("\\begin{texdocclassmethods}");
			printExecutableMembers(cd, methods, "method");
			os.println("\\end{texdocclassmethods}");
		}

		os.println("\\end{texdocclass}");
		os.println("");
		os.println("");
	}

	/**
	 * Enumerates the fields passed and formats them using Tex statements.
	 * 
	 * @param fields
	 *            the fields to format
	 */
	private static void printFields(ClassDoc cd, FieldDoc[] fields) {

		/* sort by name */
		Arrays.sort(fields, new Comparator<FieldDoc>() {
			@Override
			public int compare(FieldDoc o1, FieldDoc o2) {
				return o1.name().compareToIgnoreCase(o2.name());
			}
		});

		for (FieldDoc f : fields) {
			os.print("\\texdocfield");
			os.print("{" + HTMLToTex.convert(f.modifiers()) + "}");
			os.print("{" + HTMLToTex.convert(typeToString(f.type())) + "}");
			os.print("{" + HTMLToTex.convert(f.name()) + "}");
			os.print("{" + HTMLToTex.convert(f.commentText()) + "}");
			os.println("");
		}
	}

	/**
	 * Enumerates the members of a section of the document and formats them
	 * using Tex statements.
	 * 
	 * @param mems
	 *            the members of this entity
	 * @see #start
	 */
	private static void printExecutableMembers(ClassDoc cd,
			ExecutableMemberDoc[] members, String type) {

		/* sort by name */
		Arrays.sort(members, new Comparator<ExecutableMemberDoc>() {
			@Override
			public int compare(ExecutableMemberDoc o1, ExecutableMemberDoc o2) {
				return o1.name().compareToIgnoreCase(o2.name());
			}
		});

		for (ExecutableMemberDoc member : members) {
			os.print("\\texdoc" + type);
			os.print("{" + HTMLToTex.convert(member.modifiers()) + "}");
			if (member instanceof MethodDoc) {
				MethodDoc methodDoc = (MethodDoc) member;
				os.print("{" + HTMLToTex.convert(typeToString(methodDoc.returnType())) + "}");
			}
			os.print("{" + HTMLToTex.convert(member.name()) + "}");
			os.print("{" + HTMLToTex.convert(formatParameters(member)) + "}");
			os.print("{" + HTMLToTex.convert(member.commentText()) + "}");
			os.print("{");
			printParameterDocumentation(member);
			os.print("}");
			os.println("");
		}
	}

	private static void printParameterDocumentation(ExecutableMemberDoc member) {
		/* handle @param tags */
		ParamTag[] tags = member.paramTags();
		if (tags.length > 0) {
			os.println("\\begin{texdocparameters}");
			for (ParamTag tag : member.paramTags()) {
				os.print("\\texdocparameter{" + HTMLToTex.convert(tag.parameterName()) + "}");
				os.print("{" + HTMLToTex.convert(tag.parameterComment()) + "}");
				os.println("");
			}
			os.println("\\end{texdocparameters}");
		}

		/* handle @return tag */
		Tag[] returnTags = member.tags("return");
		if (returnTags.length > 0) {
			os.print("\\texdocreturn{");
			for (Tag returnTag : returnTags) {
				os.print(HTMLToTex.convert(returnTag.text()));
			}
			os.print("}");
			os.println("");
		}
	}

	private static String formatParameters(ExecutableMemberDoc member) {
		StringBuilder res = new StringBuilder();

		res.append("(");
		String separator = "";
		for (Parameter param : member.parameters()) {
			res.append(separator);
			res.append(typeToString(param.type()));
			res.append(" ");
			res.append(param.name());
			separator = ", ";
		}
		res.append(")");

		return res.toString();
	}

	/**
	 * Converts a DocLet type back to java syntax
	 */
	private static String typeToString(Type type) {
		String tstring;
		ParameterizedType ptype = type.asParameterizedType();
		if (ptype != null) {
			tstring = ptype.typeName();
			tstring += "<";
			for (Type ta : ptype.typeArguments()) {
				tstring += typeToString(ta);
			}
			tstring += ">";
		} else {
			tstring = type.typeName();
		}
		return tstring;
	}
}

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
import com.sun.javadoc.Doc;
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
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;
import com.sun.javadoc.ThrowsTag;
import com.sun.javadoc.Type;
import com.sun.javadoc.TypeVariable;

/**
 * Note: This version is heavily modified by Matthias Braun<matthias.braun@kit.edu>
 * Note: This version is even heavier modified by Lukas Böhm<suluke93@gmail.com>
 * 
 * This class provides a Java 2, <code>javadoc</code> Doclet which generates a
 * LaTeX2e document out of the java classes that it is used on.
 * 
 * It converts commonly used html tags to equivalent latex constructs (see
 * HTMLToTex for details) Not working yet: - type parameters for
 * class/interface/methods are not printed yet - only a subset of the javadoc
 * tags are handled (param and return mostly)
 * 
 * @author <a href="mailto:gregg.wonderly@pobox.com">Gregg Wonderly</a>
 * @author <a href="mailto:matthias.braun@kit.edu">Matthias Braun</a>
 * @author <a href="mailto:suluke93@gmail.com">Lukas Böhm</a>
 */
public class TexDoclet extends Doclet {
	/** Writer for writing to output file */
	private static PrintWriter os = null;
	private static String outfile = "docs.tex";
	private static String refInlineName = "see ";
	private static String refBlockName = "See also";
	private static boolean silent = false;
	private static String excludeTag = "@texignore";

	/**
	 * Returns how many arguments would be consumed if <code>option</code> is a
	 * recognized option.
	 * 
	 * @param option
	 *            the option to check
	 */
	public static int optionLength(String option) {
		if (option.equals("-output")
			|| option.equals("-classfilter") 
			|| option.equals("-see")) {
			return 2;
		}
		if (option.equals("-help")) {
			System.out.println("TexDoclet Usage:");
			System.out.println("-output <outfile>     Specifies the output file to write to.  If none");
			System.out.println("                      specified, the default is docs.tex in the current");
			System.out.println("                      directory.");
			System.out.println("-see                  Specifies the text to use for references created from inline tags.");
			System.out.println("                      For german javadocs use \"siehe \" for example.");
			System.out.println("                      The default is \"see \".");
			System.out.println("-See                  Specifies the text to use for references created from block tags.");
			System.out.println("                      For german javadocs use \"Siehe auch\" for example.");
			System.out.println("                      The default is \"See also\".");
			System.out.println("-silent               Have the doclet run in silent mode, i.e. all output will be suppressed, except warnings");
			return 1;
		}
		if (option.equals("-silent")) {
			return 1;
		}
		System.err.println("unknown option: " + option);
		return Doclet.optionLength(option);
	}

	/**
	 * Checks the passed options and their arguments for validity.
	 * Used to already configure the doclet according to the options passed.
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
				continue;
			}
			if (args[i][0].equals("-see")) {
				refInlineName = args[i][1];
				continue;
			}
			if (args[i][0].equals("-See")) {
				refBlockName = args[i][1];
				continue;
			}
			if (args[i][0].equals("-silent")) {
				silent = true;
				continue;
			}
		}
		return true;
	}

	/** 
	 * indicate that we can handle (most) Java 1.5 language features
	 */
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
		println("TexDoclet 4.0, Copyright 2009 - Matthias Braun");
		println("based on TexDoclet v3.0, Copyright 2003 - Gregg Wonderly.");
		println("http://texdoclet.dev.java.net - on the World Wide Web.");

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
		Arrays.sort(packages, new Comparator<PackageDoc>() {
			public int compare(PackageDoc o1, PackageDoc o2) {
				return o1.name().compareToIgnoreCase(o2.name());
			}
		});

		for (PackageDoc pkg : packages) {

			println("* Package: " + pkg.name());

			os.println("\\begin{texdocpackage}{" + HTMLToTex.convert(pkg.name()) + "}");
			os.println("\\label{texdoclet:" + pkg.name() + "}");
			os.println("");

			printSees(pkg);

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

	private static void printComment(Doc d) {
		printComment(d.inlineTags(), null);
	}

	private static void printComment(Doc d, MethodDoc md) {
		printComment(d.inlineTags(), md);
	}

	private static void printComment(Tag t) {
		printComment(t.inlineTags(), null);
	}

	private static void printComment(Tag[] tags, MethodDoc md) {
		for (Tag t : tags) {
			if (t instanceof SeeTag) {
				SeeTag st = (SeeTag) t;
				os.print(HTMLToTex.convert(t.text(), md));
				if (st.referencedClassName() != null) {
					os.print(" (" + refInlineName.toLowerCase());
					os.print("\\ref{");
					os.print(getLabel(st));
					os.print("})");
				}
			} else if (t.kind().equals("@inheritDoc")) {
				MethodDoc overridden = findSuperMethod(md);
				if (overridden == null) {
					System.err.println("Warning: No overridden method found for {@inheritDoc} (" + md.name() + ")");
					os.print(HTMLToTex.convert(t.text(), md));
				} else {
					os.print("\\texdocinheritdoc{");
					os.print(overridden.containingClass().qualifiedName().replace(".", "\\-."));
					os.print("}{");
					printComment(overridden.inlineTags(), overridden);
					os.print("}");
				}
			} else {
				if (!t.kind().equals("Text")) {
					System.err.println("Warning: Unknown Tag of kind " + t.kind());
				}
				os.print(HTMLToTex.convert(t.text(), md));
			}
		}
	}

	private static MethodDoc findSuperMethod(MethodDoc md) {
		MethodDoc overrides = md.overriddenMethod();
		if (overrides != null)
			return overrides;

		ClassDoc cls = md.containingClass();
		/* search the method in implemented interfaces */
		for (ClassDoc intf : cls.interfaces()) {
			for (MethodDoc intfmethod : intf.methods()) {
				if (md.overrides(intfmethod))
					return intfmethod;
			}
		}
		return null;
	}

	private static void printClasses(ClassDoc[] classes) {
		Arrays.sort(classes, new Comparator<ClassDoc>() {
			public int compare(ClassDoc o1, ClassDoc o2) {
				return o1.name().compareToIgnoreCase(o2.name());
			}
		});

		for (ClassDoc cd : classes) {
			printClass(cd);
		}
	}

	private static void printClass(ClassDoc cd) {
		if (cd.tags(excludeTag).length > 0) {
			return;
		}
		
		// Determine type: class or interface
		String type;
		if (cd.isInterface()) {
			type = "interface";
		} else if (cd.isEnum()) { 
			type = "enum";
		} else {
			type = "class";
		}
		
		String name = formatClassDoc(cd);
		
		// this string will hold the class' section heading
		String classLine = "\\begin{texdocclass}"
				+ "{" + type + "}"
				+ "{" + texEscape(name) + "}";
		
		// print "extends" fields
		Type s = cd.superclassType();
		if (s != null 
			&& !s.toString().equals("java.lang.Object")
			&& !cd.isEnum()
		) {
			String generalization = s.toString();
			classLine += "[" + HTMLToTex.convert(generalization) + "]";
		} else if (!cd.isInterface()) {
			// HACKY: if cd is an interface, omit the empty brackets for the "extends" field, for it is used for extended interfaces
			classLine += "[]";
		}
		
		// print "implements" fields
		Type[] itypes = cd.interfaceTypes();
		if (itypes.length > 0) {
			String realizations = itypes[0].asClassDoc().toString();
			for (int i = 1; i < itypes.length; i++) {
				ClassDoc ic = itypes[i].asClassDoc();
				realizations +=  ", " + ic.toString();
			}
			classLine += "[" + HTMLToTex.convert(realizations) + "]";
		}

		os.println(classLine);
			
		os.println("\\label{texdoclet:" + cd.containingPackage().name() + "." + cd.name() + "}");
		os.println("\\begin{texdocclassintro}");
		printComment(cd);
		os.println("\\end{texdocclassintro}");

		printSees(cd);

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
		
		FieldDoc[] enums = cd.enumConstants();
		if (enums.length > 0) {
			os.println("\\begin{texdocenums}");
			printEnums(cd, enums);
			os.println("\\end{texdocenums}");
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

	private static String getLabel(SeeTag t) {
		if (t.referencedPackage() != null) {
			return "texdoclet:" + t.referencedPackage().name();
		} else {
			return "texdoclet:" + t.referencedClassName();
		}
	}

	private static void printSees(Doc d) {
		SeeTag[] sts = d.seeTags();
		if (sts.length > 0) {
			os.println("\\begin{texdocsees}{" + refBlockName + "}");
			for (SeeTag st : sts) {
				os.print("\\texdocsee");
				os.print("{" + HTMLToTex.convert(st.text()) + "}");
				os.print("{" + getLabel(st) + "}");
				os.println("");
			}
			os.println("\\end{texdocsees}");
		}
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
			public int compare(FieldDoc o1, FieldDoc o2) {
				return o1.name().compareToIgnoreCase(o2.name());
			}
		});

		for (FieldDoc f : fields) {
			os.print("\\texdocfield");
			os.print("{" + texEscape(f.modifiers()) + "}");
			os.print("{" + texEscape(typeToString(f.type())) + "}");
			os.print("{" + texEscape(f.name()) + "}");
			os.print("{");
			printComment(f);
			os.print("}");
			printSees(f);
			os.println("");
		}
	}
	
	/**
	 * Enumerates the enum constants passed and formats them using Tex statements.
	 * 
	 * @param enums
	 *            the enum constants to format
	 */
	private static void printEnums(ClassDoc cd, FieldDoc[] enums) {

		/* sort by name */
		Arrays.sort(enums, new Comparator<FieldDoc>() {
			public int compare(FieldDoc o1, FieldDoc o2) {
				return o1.name().compareToIgnoreCase(o2.name());
			}
		});

		for (FieldDoc f : enums) {
			os.print("\\texdocenum");
			os.print("{" + HTMLToTex.convert(f.name()) + "}");
			os.print("{");
			printComment(f);
			os.print("}");
			printSees(f);
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
			public int compare(ExecutableMemberDoc o1, ExecutableMemberDoc o2) {
				return o1.name().compareToIgnoreCase(o2.name());
			}
		});

		for (ExecutableMemberDoc member : members) {
			os.print("\\texdoc" + type);
			os.print("{" + HTMLToTex.convert(member.modifiers()) + "}");
			if (member instanceof MethodDoc) {
				MethodDoc methodDoc = (MethodDoc) member;
				os.print("{" + texEscape(typeToString(methodDoc.returnType())) + "}");
			}
			os.print("{" + HTMLToTex.convert(member.name()) + "}");
			os.print("{" + HTMLToTex.convert(formatParameters(member)) + "}");
			if (member instanceof MethodDoc) {
				MethodDoc methodDoc = (MethodDoc) member;
				os.print("{");
				printComment(member, methodDoc);
				os.print("}");
			} else {
				os.print("{");
				printComment(member);
				os.print("}");
			}
			os.print("{");
			printParameterDocumentation(member);
			os.print("}");
			printSees(member);
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
				os.print("{");
				printComment(tag);
				os.println("}");
			}
			os.println("\\end{texdocparameters}");
		}

		/* handle @return tag */
		Tag[] returnTags = member.tags("return");
		if (returnTags.length > 0) {
			os.print("\\texdocreturn{");
			for (Tag returnTag : returnTags) {
				printComment(returnTag);
			}
			os.print("}");
			os.println("");
		}
		ThrowsTag[] throwsTags = member.throwsTags();
		if (throwsTags.length > 0) {
			os.println("\\begin{texdocthrows}");
			for (ThrowsTag tag : member.throwsTags()) {
				os.print("\\texdocthrow{" + HTMLToTex.convert(tag.exceptionName()) + "}");
				os.print("{");
				printComment(tag);
				os.print("}");
				os.println("");
			}
			os.println("\\end{texdocthrows}");
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
	 * Builds a string with the name and the parameter types of a given ClassDoc.
	 * 
	 * @param cd the ClassDoc whoes name is to be formatted
	 * @return a String like "List<String>" in case of a string list
	 */
	private static String formatClassDoc(ClassDoc cd) {
		String name = cd.name();
		TypeVariable[] parameters = cd.typeParameters();
		if (parameters.length > 0) {
			name += "<" + formatTypeVariable(parameters[0]);
			for (int i = 1; i < parameters.length; i++) {
				TypeVariable t = parameters[i];
				name += ", " + formatTypeVariable(t);
			}
			name +=">";
		}
		
		return name;
	}
	
	/**
	 * Formats a given type variable an returns its string representation.
	 * E.g. "? extends String" for an anonymous TypeVariable that is derived from String
	 * 
	 * @param t the type variable to be formatted
	 * @return the formatted string representation
	 */
	private static String formatTypeVariable(TypeVariable t) {
		String result = t.typeName();
		
		Type[] bounds = t.bounds();
		if (bounds.length > 0) {
			result += " extends " + bounds[0].toString();
			for(int i = 1; i < bounds.length; i++) {
				result += ", " + bounds[i].toString();
			}
		}
		
		return result;
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
		tstring += type.dimension();

		return tstring;
	}
	
	private static String texEscape(String s) {
		StringBuilder ret = new StringBuilder();
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '"':
					ret.append("\"'");
					break;
				case '_':
				case '%':
				case '$':
				case '#':
					ret.append('\\');
					ret.append((char) c);
					break;
				case '^': /* { */
					ret.append("$\\wedge$");
					break;
				case '}':
					ret.append("$\\}$");
					break;
				case '{':
					ret.append("$\\{$");
					break;
				case '<':
					ret.append("\\textless{}");
					break;
				case '>':
					ret.append("\\textgreater{}");
					break;
				default:
					ret.append((char) c);
					break;
			}
		}
		return ret.toString();
	}
	
	private static void println(String s) {
		if (!silent) {
			System.out.println(s);
		}
	}
}

package org.wonderly.doclets;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.Doclet;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ParameterizedType;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Type;

/**
 *  Note: This version is heavily modified by Matthias Braun<matthias.braun@kit.edu>
 * 
 *  This class provides a Java 2, <code>javadoc</code> Doclet which generates
 *  a LaTeX2e document out of the java classes that it is used on.  This is
 *  convienent for creating printable documentation complete with cross reference
 *  information.
 *  <p>
 *  Supported HTML tags within comments include the following
 *  <dl>
 *  <dt>&lt;dl&gt;
 *  <dd>with the associated &lt;dt&gt;&lt;dd&gt;&lt;/dl&gt; tags
 *  <dt>&lt;p&gt;
 *  <dd>but not align=center...yet
 *  <dt>&lt;br&gt;
 *  <dd>but not clear=xxx
 *  <dt>&lt;table&gt;
 *  <dd>including all the associcated &lt;td&gt;&lt;th&gt;&lt;tr&gt;&lt;/td&gt;&lt;/th&gt;&lt;/tr&gt;
 *  <dt>&lt;ol&gt;
 *  <dd>ordered lists
 *  <dt>&lt;ul&gt;
 *  <dd>unordered lists
 *  <dt>&lt;font&gt;
 *  <dd>font coloring
 *  <dt>&lt;pre&gt;
 *  <dd>preformatted text
 *  <dt>&lt;code&gt;
 *  <dd>fixed point fonts
 *  <dt>&lt;i&gt;
 *  <dd>italized fonts
 *  <dt>&lt;b&gt;
 *  <dd>bold fonts
 *	</dl>
 *
 * {@link #TexDoclet TexDoclet}
 * {@link #start(RootDoc) start}
 *  @version 1.1
 *  @author <a href="mailto:gregg.wonderly@pobox.com">Gregg Wonderly</a>
 */
public class TexDoclet extends Doclet {
	/** Writer for writing to output file */
	public static PrintWriter os = null;
	private static String outfile = "docs.tex";
	
	/**
	 *  Returns how many arguments would be consumed if <code>option</code>
	 *  is a recognized option.
	 *
	 *  @param option the option to check
	 */
	public static int optionLength(String option) {
		if( option.equals("-output") )
			return 2;
		else if( option.equals("-classfilter") )
			return 2;
		else if( option.equals("-help") ) {
			System.err.println( "TexDoclet Usage:");
			System.err.println( "-output <outfile>     Specifies the output file to write to.  If none");
			System.err.println( "                      specified, the default is docs.tex in the current");
			System.err.println( "                      directory." );

			return 1;
		}
		
		System.out.println( "unknown option "+option);
		return Doclet.optionLength(option);
	}
	
	/**
	 *  Checks the passed options and their arguments for validity.
	 *
	 *  @param args the arguments to check
	 *  @param err the interface to use for reporting errors
	 */
	static public boolean validOptions( String[][] args, DocErrorReporter err ) {
		for( int i = 0; i < args.length; ++i ) {
			if( args[i][0].equals( "-output" ) ) {
				outfile = args[i][1];
			}
		}
		return true;
	}	
	
	/**
	 *  Called by the framework to format the entire document
	 *
	 *  @param root the root of the starting document
	 */
	public static boolean start(RootDoc root) {
		System.out.println("TexDoclet v3.0, Copyright 2003 - Gregg Wonderly.");
		System.out.println("http://texdoclet.dev.java.net - on the World Wide Web.");
		System.out.println("heavily modified by Matthias Braun<matthias.braun@kit.edu>");
		
		try {
			os = new PrintWriter(new FileWriter(outfile));
			if (os == null) {
				System.err.println("Can not create output file, processing aborted");
				System.exit(1);
			}			
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	
		ClassDoc[] classes = root.specifiedClasses();
		PackageDoc[] packages = root.specifiedPackages();
		
		for (PackageDoc pkg : packages) {
		
			System.out.println( "* Package: " + pkg.name() );
			
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
		
		os.println("\\begin{texdocclass}{" + type + "}{" + HTMLToTex.convert(cd.name()) + "}");

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
			printMembers(cd, constructors, "constructor");
			os.println("\\end{texdocclassconstructors}");
		}
		
		MethodDoc[] methods = cd.methods();
		if (methods.length > 0) {
			os.println("\\begin{texdocclassmethods}");
			printMembers(cd, methods, "method");
			os.println("\\end{texdocclassmethods}");
		}
		
		os.println("\\end{texdocclass}");
		os.println("");
		os.println("");			
	}
	
	/**
	 *  Enumerates the fields passed and formats
	 *  them using Tex statements.
	 *
	 *  @param fields the fields to format
	 */
	private static void printFields(ClassDoc cd, FieldDoc[] fields) {
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
	 *  Enumerates the members of a section of the document and formats
	 *  them using Tex statements.
	 *
	 *  @param mems the members of this entity
	 *  @see #start
	 */
	private static void printMembers(ClassDoc cd, ExecutableMemberDoc[] members, String type) {
		
		/* sort by name */
		Arrays.sort(members, new Comparator<ExecutableMemberDoc>() {
			@Override
			public int compare(ExecutableMemberDoc o1, ExecutableMemberDoc o2) {
				return o1.name().compareToIgnoreCase(o2.name());
			}
		});

		for (ExecutableMemberDoc member : members) {
			os.print("\\texdoc" + type);
			os.print("{" + HTMLToTex.convert(member.name()) + "}");
			os.print("{" + HTMLToTex.convert(getFullDeclaration(member)) + "}");
			os.print("{" + HTMLToTex.convert(member.commentText()) + "}");
			os.println("");
		}
	}
	
	/**
	 * reconstructs a java representation of a DocLet Method/Constructor 
	 */
	private static String getFullDeclaration(ExecutableMemberDoc med) {
		StringBuilder res = new StringBuilder();
		if (! med.modifiers().equals("")) {
			res.append(med.modifiers());
			res.append(" ");
		}
		if (med instanceof MethodDoc) {
			MethodDoc method = (MethodDoc) med;
			res.append(typeToString(method.returnType()));
			res.append(" ");
		}
		res.append(med.name());
		res.append("(");
		String separator = "";
		for (Parameter param : med.parameters()) {
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
	 * Converts a DocLet type back to a java syntax
	 */
	private static String typeToString(Type type) {
		String tstring;
		ParameterizedType ptype = type.asParameterizedType();
		if (ptype != null) {
			tstring = ptype.typeName();
			tstring += "<";
			/* Matze: This looks right, but doesn't work for me :-( */
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

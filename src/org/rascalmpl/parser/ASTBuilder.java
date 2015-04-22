/*******************************************************************************
 * Copyright (c) 2009-2013 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:

 *   * Jurgen J. Vinju - Jurgen.Vinju@cwi.nl - CWI
 *   * Tijs van der Storm - Tijs.van.der.Storm@cwi.nl
 *   * Paul Klint - Paul.Klint@cwi.nl - CWI
 *   * Mark Hills - Mark.Hills@cwi.nl (CWI)
 *   * Arnold Lankamp - Arnold.Lankamp@cwi.nl
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI
 *******************************************************************************/
package org.rascalmpl.parser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.IList;
import org.eclipse.imp.pdb.facts.IListWriter;
import org.eclipse.imp.pdb.facts.ISet;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.exceptions.FactTypeUseException;
import org.eclipse.imp.pdb.facts.exceptions.IllegalOperationException;
import org.rascalmpl.ast.AbstractAST;
import org.rascalmpl.ast.Command;
import org.rascalmpl.ast.Commands;
import org.rascalmpl.ast.Expression;
import org.rascalmpl.ast.Module;
import org.rascalmpl.ast.Statement;
import org.rascalmpl.interpreter.asserts.Ambiguous;
import org.rascalmpl.interpreter.asserts.ImplementationError;
import org.rascalmpl.parser.gtd.util.PointerKeyedHashMap;
import org.rascalmpl.semantics.dynamic.Tree;
import org.rascalmpl.values.ValueFactoryFactory;
import org.rascalmpl.values.uptr.Factory;
import org.rascalmpl.values.uptr.ProductionAdapter;
import org.rascalmpl.values.uptr.SymbolAdapter;
import org.rascalmpl.values.uptr.TreeAdapter;

/**
 * Uses reflection to construct an AST hierarchy from a 
 * UPTR parse node of a rascal program.
 */
public class ASTBuilder {
	private static final String MODULE_SORT = "Module";

	private final PointerKeyedHashMap<IConstructor, AbstractAST> lexCache = new PointerKeyedHashMap<IConstructor, AbstractAST>();

	private final PointerKeyedHashMap<IValue, Expression> constructorCache = new PointerKeyedHashMap<IValue, Expression>();

	private final static HashMap<String, Constructor<?>> astConstructors = new HashMap<String,Constructor<?>>();
	private final static ClassLoader classLoader = ASTBuilder.class.getClassLoader();

	public static <T extends AbstractAST> T make(String sort, ISourceLocation src, Object... args) {
		return make(sort, "Default", src, args);
	}

	public static  <T extends Expression> T makeExp(String cons, ISourceLocation src, Object... args) {
		return make("Expression", cons, src, args);
	}

	public static <T extends Statement> T makeStat(String cons, ISourceLocation src, Object... args) {
		return make("Statement", cons, src, args);
	}

	public static <T extends AbstractAST> T makeLex(String sort, ISourceLocation src, Object... args) {
		return make(sort, "Lexical", src, args);
	}

	@SuppressWarnings("unchecked")
	public static <T extends AbstractAST> T make(String sort, String cons, ISourceLocation src, Object... args) {
		Object[] newArgs = new Object[args.length + 1];
		System.arraycopy(args, 0, newArgs, 1, args.length);
		return (T) callMakerMethod(sort, cons, src, null, newArgs, null);
	}

	public Module buildModule(IConstructor parseTree) throws FactTypeUseException {
		IConstructor tree =  parseTree;

		if (TreeAdapter.isAppl(tree)) {
			if (sortName(tree).equals(MODULE_SORT)) {
				// t must be an appl so call buildValue directly
				return (Module) buildValue(tree);
			}
			return buildSort(parseTree, MODULE_SORT);
		}

		if (TreeAdapter.isAmb(tree)) {
			throw new Ambiguous(tree);
		}

		throw new ImplementationError("Parse of module returned invalid tree.");
	}

	public Expression buildExpression(IConstructor parseTree) {
		return buildSort(parseTree, "Expression");
	}

	public Statement buildStatement(IConstructor parseTree) {
		return buildSort(parseTree, "Statement");
	}

	public Command buildCommand(IConstructor parseTree) {
		return buildSort(parseTree, "Command");
	}

	public Command buildSym(IConstructor parseTree) {
		return buildSort(parseTree, "Sym");
	}

	public Commands buildCommands(IConstructor parseTree) {
		return buildSort(parseTree, "Commands");
	}

	@SuppressWarnings("unchecked")
	private <T extends AbstractAST> T buildSort(IConstructor parseTree, String sort) {
		if (TreeAdapter.isAppl(parseTree)) {
			IConstructor tree = TreeAdapter.getStartTop(parseTree);

			if (sortName(tree).equals(sort)) {
				return (T) buildValue(tree);
			}
		} 
		else if (TreeAdapter.isAmb(parseTree)) {
			throw new Ambiguous(parseTree);
		}

		throw new ImplementationError("This is not a " + sort +  ": " + parseTree);
	}

	public AbstractAST buildValue(IValue arg)  {
		IConstructor tree = (IConstructor) arg;

		if (TreeAdapter.isList(tree)) {
			throw new ImplementationError("buildValue should not be called on a list");
		}

		if (TreeAdapter.isAmb(tree)) {
			throw new Ambiguous(tree);
		}

		if (!TreeAdapter.isAppl(tree)) {
			throw new UnsupportedOperationException();
		}	

		if (isLexical(tree)) {
			if (TreeAdapter.isRascalLexical(tree)) {
				return buildLexicalNode(tree);
			}
			return buildLexicalNode((IConstructor) ((IList) ((IConstructor) arg).get("args")).get(0));
		}

		if (sortName(tree).equals("Pattern")) {
			if (isNewEmbedding(tree)) {
				return newLift(tree, true);
			}
		}

		if (sortName(tree).equals("Expression")) {
			if (isNewEmbedding(tree)) {
				return newLift(tree, false);
			}
		}

		return buildContextFreeNode((IConstructor) arg);
	}

	private List<AbstractAST> buildList(IConstructor in)  {
		IList args = TreeAdapter.getListASTArgs(in);
		List<AbstractAST> result = new ArrayList<AbstractAST>(args.length());
		for (IValue arg: args) {
			result.add(buildValue(arg));
		}
		return result;
	}

	private AbstractAST buildContextFreeNode(IConstructor tree)  {
		String constructorName = TreeAdapter.getConstructorName(tree);
		if (constructorName == null) {
			throw new ImplementationError("All Rascal productions should have a constructor name: " + TreeAdapter.getProduction(tree));
		}

		String cons = capitalize(constructorName);
		String sort = sortName(tree);

		if (sort.length() == 0) {
			throw new ImplementationError("Could not retrieve sort name for " + tree);
		}
		sort = sort.equalsIgnoreCase("pattern") ? "Expression" : capitalize(sort); 

		switch(sort){
		case "Mapping":
			sort = "Mapping_Expression"; break;
		case "KeywordArgument":
			sort = "KeywordArgument_Expression"; break;
		case "KeywordArguments":
			sort = "KeywordArguments_Expression"; break;
		}

		IList args = getASTArgs(tree);
		int arity = args.length();
		Object actuals[] = new Object[arity+1];
		actuals[0] = tree;

		int i = 1;
		for (IValue arg : args) {
			IConstructor argTree = (IConstructor) arg;

			if (TreeAdapter.isList(argTree)) {
				actuals[i] = buildList((IConstructor) arg);
			}
			else {
				actuals[i] = buildValue(arg);
			}
			i++;
		}

		return callMakerMethod(sort, cons, tree.asAnnotatable().getAnnotations(), actuals, null);
	}

	private AbstractAST buildLexicalNode(IConstructor tree) {
		String sort = capitalize(sortName(tree));

		if (sort.length() == 0) {
			throw new ImplementationError("could not retrieve sort name for " + tree);
		}
		Object actuals[] = new Object[] { tree, new String(TreeAdapter.yield(tree)) };

		return callMakerMethod(sort, "Lexical", tree.asAnnotatable().getAnnotations(), actuals, null);
	}

	private String getPatternLayout(IConstructor tree) {
		IConstructor prod = TreeAdapter.getProduction(tree);
		String cons = ProductionAdapter.getConstructorName(prod);

		if (cons.equals("concrete")) {
			// TODO
			return "???";
		}

		throw new ImplementationError("Unexpected embedding syntax:" + prod);
	}

	private Expression cache(IConstructor in, Expression out) {
		constructorCache.putUnsafe(in, out);
		return out;
	}

	private int hits1 = 0;

	private Expression liftRec(IConstructor tree, boolean lexicalFather, String layoutOfFather) {
		Expression cached = constructorCache.get(tree);
		if (cached != null) {
			return cached;
		}

		if (layoutOfFather == null) {
			throw new ImplementationError("layout is null");
		}

		if (TreeAdapter.isAppl(tree)) {
			String cons = TreeAdapter.getConstructorName(tree);

			if (cons != null && (cons.equals("hole"))) { // TODO: this is unsafe, what if somebody named their own production "hole"??
				return liftHole(tree);
			}

			boolean lex = lexicalFather ? !TreeAdapter.isSort(tree) : TreeAdapter.isLexical(tree);

			IList args = TreeAdapter.getArgs(tree);
			String layout = layoutOfFather;

			if (cons != null && !lex) { 
				String newLayout = getLayoutName(TreeAdapter.getProduction(tree));

				// this approximation is possibly harmfull. Perhaps there is a chain rule defined in another module, which nevertheless
				// switched the applicable layout. Until we have a proper type-analysis there is nothing we can do here.
				if (newLayout != null) {
					layout = newLayout;
				}
			}

			java.util.List<Expression> kids = new ArrayList<Expression>(args.length());
			for (IValue arg : args) {
				Expression ast = liftRec((IConstructor) arg, lex, layout);
				if (ast == null) {
					return null;
				}
				kids.add(ast);
			}

			if (TreeAdapter.isList(tree)) {
				// TODO: splice element lists (can happen in case of ambiguous lists)
				return cache(tree, new Tree.List(tree, kids));
			}
			else if (TreeAdapter.isOpt(tree)) {
				return cache(tree, new Tree.Optional(tree, kids));
			}
			else { 
				return cache(tree, new Tree.Appl(tree, kids));
			}
		}
		else if (TreeAdapter.isCycle(tree)) {
			return new Tree.Cycle(TreeAdapter.getCycleType(tree), TreeAdapter.getCycleLength(tree));
		}
		else if (TreeAdapter.isAmb(tree)) {
			ISet args = TreeAdapter.getAlternatives(tree);
			java.util.List<Expression> kids = new ArrayList<Expression>(args.size());

			for (IValue arg : args) {
				kids.add(liftRec((IConstructor) arg, lexicalFather, layoutOfFather));
			}

			if (kids.size() == 0) {
				return null;
			}

			if (kids.size() == 1) {
				return kids.get(0);
			}

			return cache(tree, new Tree.Amb(tree, kids));
		}
		else {
			if (!TreeAdapter.isChar(tree)) {
				throw new ImplementationError("unexpected tree type: " + tree);
			}
			return cache(tree, new Tree.Char(tree)); 
		}
	}

	private Expression liftHole(IConstructor tree) {
		assert tree.asAnnotatable().hasAnnotation("holeType");
		IConstructor type = (IConstructor) tree.asAnnotatable().getAnnotation("holeType");
		tree = (IConstructor) TreeAdapter.getArgs(tree).get(0);
		IList args = TreeAdapter.getArgs(tree);
		IConstructor nameTree = (IConstructor) args.get(4);
		ISourceLocation src = TreeAdapter.getLocation(tree);
		Expression result = new Tree.MetaVariable(tree, type, TreeAdapter.yield(nameTree));
		result.setSourceLocation(src);
		return result;
	}

	private String getLayoutName(IConstructor production) {
		if (ProductionAdapter.isDefault(production)) {
			for (IValue sym : ProductionAdapter.getSymbols(production)) {
				if (SymbolAdapter.isLayouts(SymbolAdapter.delabel((IConstructor) sym))) {
					return SymbolAdapter.getName((IConstructor) sym);
				}
			}
		}

		return null;
	}

	private IList getASTArgs(IConstructor tree) {
		IList children = TreeAdapter.getArgs(tree);
		IListWriter writer = ValueFactoryFactory.getValueFactory().listWriter(Factory.Args.getElementType());

		for (int i = 0; i < children.length(); i++) {
			IConstructor kid = (IConstructor) children.get(i);
			if (!TreeAdapter.isLiteral(kid) && !TreeAdapter.isCILiteral(kid) && !TreeAdapter.isEmpty(kid)) {
				writer.append(kid);	
			} 
			// skip layout
			i++;
		}

		return writer.done();
	}

	private String sortName(IConstructor tree) {
		if (TreeAdapter.isAppl(tree)) {
			return TreeAdapter.getSortName(tree);
		}
		if (TreeAdapter.isAmb(tree)) {
			// all alternatives in an amb cluster have the same sort
			return sortName((IConstructor) TreeAdapter.getAlternatives(tree).iterator().next());
		}
		return "";
	}

	private String capitalize(String sort) {
		if (sort.length() == 0) {
			return sort;
		}
		if (sort.length() > 1) {
			return Character.toUpperCase(sort.charAt(0)) + sort.substring(1);
		}

		return sort.toUpperCase();
	}

	private static ImplementationError unexpectedError(Throwable e) {
		return new ImplementationError("Unexpected error in AST construction: " + e, e);
	}

	private boolean isNewEmbedding(IConstructor tree) {
		String name = TreeAdapter.getConstructorName(tree);
		assert name != null;

		if (name.equals("concrete")) {
			tree = (IConstructor) TreeAdapter.getArgs(tree).get(0);
			name = TreeAdapter.getConstructorName(tree);

			if (name.equals("$parsed")) {
				return true;
			}
		}
		return false;
	}

	private boolean isLexical(IConstructor tree) {
		if (TreeAdapter.isRascalLexical(tree)) {
			return true;
		}
		return false;
	}

	private AbstractAST newLift(IConstructor tree, boolean match) {
		IConstructor concrete = (IConstructor) TreeAdapter.getArgs(tree).get(0);
		IConstructor fragment = (IConstructor) TreeAdapter.getArgs(concrete).get(7);
		return liftRec(fragment, false,  getPatternLayout(tree));
	}

	private static AbstractAST callMakerMethod(String sort, String cons, Map<String, IValue> annotations, Object actuals[], Object keywordActuals[]) {
		return callMakerMethod(sort, cons, TreeAdapter.getLocation((IConstructor) actuals[0]), annotations, actuals, keywordActuals);
	}

	private static AbstractAST callMakerMethod(String sort, String cons, ISourceLocation src, Map<String, IValue> annotations, Object actuals[], Object keywordActuals[]) {
		try {
			String name = sort + '$' + cons;
			Constructor<?> constructor = astConstructors.get(name);

			if (constructor == null) {
				Class<?> clazz = null;

				try {
					clazz = classLoader.loadClass("org.rascalmpl.semantics.dynamic." + name);
				}
				catch (ClassNotFoundException e) {
					// it happens
				}

				if (clazz == null) {
					clazz = classLoader.loadClass("org.rascalmpl.ast." + name);
				}

				constructor = clazz.getConstructors()[0];
				constructor.setAccessible(true);
				astConstructors.put(name, constructor);
			}

			AbstractAST result = (AbstractAST) constructor.newInstance(actuals);
			if (src != null) {
				result.setSourceLocation(src);
			}
			if (annotations != null && !annotations.isEmpty()) {
				result.setAnnotations(annotations);
			}
			return result;
		} catch (SecurityException e) {
			throw unexpectedError(e);
		} catch (IllegalArgumentException e) {
			throw unexpectedError(e);
		} catch (IllegalAccessException e) {
			throw unexpectedError(e);
		} catch (InvocationTargetException e) {
			throw unexpectedError(e);
		} catch (ClassNotFoundException e) {
			throw unexpectedError(e);
		} catch (InstantiationException e) {
			throw unexpectedError(e);
		}
	}

}

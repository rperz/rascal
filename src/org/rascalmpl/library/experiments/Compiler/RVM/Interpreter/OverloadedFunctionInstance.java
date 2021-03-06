package org.rascalmpl.library.experiments.Compiler.RVM.Interpreter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.imp.pdb.facts.IAnnotatable;
import org.eclipse.imp.pdb.facts.IExternalValue;
import org.eclipse.imp.pdb.facts.IValue;
import org.eclipse.imp.pdb.facts.IWithKeywordParameters;
import org.eclipse.imp.pdb.facts.exceptions.IllegalOperationException;
import org.eclipse.imp.pdb.facts.type.Type;
import org.eclipse.imp.pdb.facts.type.TypeFactory;
import org.eclipse.imp.pdb.facts.visitors.IValueVisitor;
import org.rascalmpl.interpreter.types.FunctionType;
import org.rascalmpl.interpreter.types.RascalTypeFactory;

public class OverloadedFunctionInstance implements /*ICallableValue,*/ IExternalValue {
	
	private final int[] functions;
	private final int[] constructors;
	final Frame env;
	
	private Type type;
	private List<Function> functionStore;
	private List<Type> constructorStore;
	
	final RVM rvm;
	
	public OverloadedFunctionInstance(final int[] functions, final int[] constructors, final Frame env, 
									  final List<Function> functionStore, final List<Type> constructorStore, final RVM rvm) {
		this.functions = functions;
		this.constructors = constructors;
		this.env = env;
		this.functionStore = functionStore;
		this.constructorStore = constructorStore;
		this.rvm = rvm;
	}
	
	int[] getFunctions() {
		return functions;
	}

	int[] getConstructors() {
		return constructors;
	}

	public String toString(){
		StringBuilder sb = new StringBuilder("OverloadedFunctionInstance[");
		if(getFunctions().length > 0){
			sb.append("functions:");
			for(int i = 0; i < getFunctions().length; i++){
				int fi = getFunctions()[i];
				sb.append(" ").append(functionStore.get(fi).getName()).append("/").append(fi);
			}
		}
		if(getConstructors().length > 0){
			if(getFunctions().length > 0){
				sb.append("; ");
			}
			sb.append("constructors:");
			for(int i = 0; i < getConstructors().length; i++){
				int ci = getConstructors()[i];
				sb.append(" ").append(constructorStore.get(ci).getName()).append("/").append(ci);
			}
		}
		sb.append("]");
		return sb.toString();
	}
	
	/**
	 * Assumption: scopeIn != -1  
	 */
	public static OverloadedFunctionInstance computeOverloadedFunctionInstance(final int[] functions, final int[] constructors, final Frame cf, final int scopeIn,
			                                                                   final List<Function> functionStore, final List<Type> constructorStore, final RVM rvm) {
		for(Frame env = cf; env != null; env = env.previousScope) {
			if (env.scopeId == scopeIn) {
				return new OverloadedFunctionInstance(functions, constructors, env, functionStore, constructorStore, rvm);
			}
		}
		throw new CompilerError("Could not find a matching scope when computing a nested overloaded function instance: " + scopeIn, rvm.getStdErr(), cf);
	}

	@Override
	public Type getType() {
		// TODO: this information should probably be available statically?
		if(this.type != null) {
			return this.type;
		}
		Set<FunctionType> types = new HashSet<FunctionType>();
		for(int fun : this.getFunctions()) {
			types.add((FunctionType) functionStore.get(fun).ftype);
		}
		for(int constr : this.getConstructors()) {
			Type type = constructorStore.get(constr);
			// TODO: void type for the keyword parameters is not right. They should be retrievable from a type store dynamically.
			types.add((FunctionType) RascalTypeFactory.getInstance().functionType(type.getAbstractDataType(), type.getFieldTypes(), TypeFactory.getInstance().voidType()));
		}
		this.type = RascalTypeFactory.getInstance().overloadedFunctionType(types);
		return this.type;
	}

	@Override
	public <T, E extends Throwable> T accept(IValueVisitor<T, E> v) throws E {
		return v.visitExternal((IExternalValue) this);
	}

	@Override
	public boolean isEqual(IValue other) {
		return this == other;
	}

	@Override
	public boolean isAnnotatable() {
		return false;
	}

	@Override
	public IAnnotatable<? extends IValue> asAnnotatable() {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public int getArity() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public boolean hasVarArgs() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public boolean hasKeywordArgs() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public Result<IValue> call(IRascalMonitor monitor, Type[] argTypes, IValue[] argValues, Map<String, IValue> keyArgValues) {
//		// TODO: 
//		return null;
//	}
//
//	@Override
//	public Result<IValue> call(Type[] argTypes, IValue[] argValues, Map<String, IValue> keyArgValues) {
//		return this.call(null, argTypes, argValues, keyArgValues);
//	}
//
//	@Override
//	public ICallableValue cloneInto(Environment env) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public boolean isStatic() {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public IEvaluator<Result<IValue>> getEval() {
//		return rvm.getEvaluatorContext().getEvaluator();
//	}

	@Override
  public boolean mayHaveKeywordParameters() {
    return false;
  }
  
  @Override
  public IWithKeywordParameters<? extends IValue> asWithKeywordParameters() {
    throw new IllegalOperationException(
        "Cannot be viewed as with keyword parameters", getType());
  }
}

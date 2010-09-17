package org.rascalmpl.semantics.concrete;

import org.eclipse.imp.pdb.facts.INode;
import org.eclipse.imp.pdb.facts.IValue;
import org.rascalmpl.ast.IASTVisitor;
import org.rascalmpl.interpreter.BooleanEvaluator;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.IEvaluatorContext;
import org.rascalmpl.interpreter.PatternEvaluator;
import org.rascalmpl.interpreter.debug.DebuggableEvaluator;
import org.rascalmpl.interpreter.debug.DebuggingDecorator;
import org.rascalmpl.interpreter.matching.IBooleanResult;
import org.rascalmpl.interpreter.matching.IMatchingResult;
import org.rascalmpl.interpreter.result.Result;
import org.rascalmpl.interpreter.staticErrors.UnexpectedTypeError;
import org.rascalmpl.interpreter.staticErrors.UnsupportedPatternError;

public class Expression extends org.rascalmpl.ast.Expression {
	static public class Addition extends org.rascalmpl.ast.Expression.Addition {

		public Addition(INode node, Expression lhs, Expression rhs) {
			super(node, lhs, rhs);
		}

		
		@Override
		public IBooleanResult booleanEval(BooleanEvaluator eval) {
			// NOTE: Here we also have private fields from the evaluator: tf
//			throw new UnexpectedTypeError(tf.boolType(), x.accept(
//					ctx.getEvaluator()).getType(), x);
			return super.booleanEval(eval);
		}


		@Override
		public <T> T debugDecorator(DebuggingDecorator<T> ddec) {
			// TODO: The following two lines are from DebuggingDecorator, but 
			// depend on private information
//			suspend(x);
//			return evaluator.visitExpressionAddition(x);
			// TODO Auto-generated method stub
			return super.debugDecorator(ddec);
		}


		@Override
		public Result<IValue> debugEval(DebuggableEvaluator eval) {
			// NOTE: suspend is a private method of the debug evaluator, will need 
			// to make it public before we can implement
			//suspend(this);
			//return this.eval(eval);
			return super.debugEval(eval);
		}


		@Override
		public Result<IValue> eval(Evaluator eval) {
			Result<IValue> left = getLhs().eval(eval);
			Result<IValue> right = getRhs().eval(eval);
			return left.add(right);			}


		@Override
		public IMatchingResult patternEval(PatternEvaluator eval) {
			throw new UnsupportedPatternError(this.toString(), this);
		}
	}

	@Override
	public <T> T accept(IASTVisitor<T> visitor) {
		// TODO Auto-generated method stub
		return null;
	}
}

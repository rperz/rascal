package org.rascalmpl.semantics.dynamic;

import org.eclipse.imp.pdb.facts.INode;
import org.rascalmpl.interpreter.PatternEvaluator;
import org.rascalmpl.interpreter.matching.IMatchingResult;
import org.rascalmpl.interpreter.matching.RegExpPatternValue;

public abstract class RegExp extends org.rascalmpl.ast.RegExp {

	static public class Lexical extends org.rascalmpl.ast.RegExp.Lexical {
		public Lexical(INode __param1, String __param2) {
			super(__param1, __param2);
		}

		@Override
		public IMatchingResult buildMatcher(PatternEvaluator __eval) {
			return new RegExpPatternValue(__eval.__getCtx(), this, this
					.getString(), java.util.Collections.<String> emptyList());
		}
	}

	public RegExp(INode __param1) {
		super(__param1);
	}
}

@doc{
 This module contains the mapping from concrete grammars to abstract grammars.
 
 It also implements a part of the semantics of Rascal's syntax definition formalism,
 i.e. by interspersing symbols with layout nodes and expanding literals.
}  
module rascal::parser::Definition
   
import rascal::syntax::RascalForImportExtraction;
import rascal::parser::Grammar;
import List;
import String;
import ParseTree;
import IO;  
import Integer;
   
@doc{
  Converts the syntax definitions of a module to a grammar.
  Note that this function does not implement the imports of a module
} 
public Grammar module2grammar(Module mod) {
  return syntax2grammar(collect(mod));
}
  
public Grammar imports2grammar(set[Import] imports) {
  return syntax2grammar({ s | (Import) `<SyntaxDefinition s>` <- imports});
}
 
private set[SyntaxDefinition] collect(Module mod) {
  set[SyntaxDefinition] result = {};
  
  top-down-break visit (mod) {
    case SyntaxDefinition s : result += s; 
    case Body b => b
  }
  return result;
}  
   
private Grammar syntax2grammar(set[SyntaxDefinition] defs) {
  set[Production] prods = {};
  set[Symbol] starts = {};
  set[Production] layouts = {};
    
  for (def <- defs) switch (def) {  
    case (SyntaxDefinition) `start syntax <Sym u> = <Prod p>;`  : {
       Symbol top = arg2symbol(u, false);
       starts += start(top);
       prods += prod2prod(top, p);
    }
    case (SyntaxDefinition) `layout <Sym u> = <Prod p>;`  : 
      layouts += prod2prod(arg2symbol(u, false), p);
    case (SyntaxDefinition) `syntax <Sym u> = <Prod p>;`  : {
      prods += prod2prod(arg2symbol(u, false), p);
    }
    default: throw "missed case: <def>";
  }

  return grammar(starts, \layout(prods) 
                       + layouts  
                       + {regular(\iter-star(\layout()),\no-attrs())}
                       + {prod([\iter-star(\layout()), top, \iter-star(\layout())],start(top),\no-attrs()) | start(top) <- starts} 
                       + {prod([rhs],\layout(),\no-attrs()) | /prod(_,Symbol rhs,_) <- layouts }
                       + {prod(str2syms(s),lit(s),attrs([term("literal"())])) | /lit(s) <- prods+layouts}
                       + {prod(cistr2syms(s),lit(s),attrs([term("ciliteral"())])) | /cilit(s) <- prods+layouts}
                );
} 
   
private set[Production] \layout(set[Production] prods) {
  return top-down-break visit (prods) {
    case prod(list[Symbol] lhs,Symbol rhs,attrs(list[Attr] as)) => prod(intermix(lhs),rhs,attrs(as)) 
      when start(_) !:= rhs, term("lex"()) notin as  
    case prod(list[Symbol] lhs,Symbol rhs,\no-attrs()) => prod(intermix(lhs),rhs,\no-attrs()) 
      when start(_) !:= rhs
  }
}  

public list[Symbol] str2syms(str x) {
  // TODO: escaping
  return [\char-class([range(c,c)]) | i <- [0..size(x)-1], int c:= charAt(x,i)]; 
}

private list[Symbol] cistr2syms(str x) {
  return for (i <- [0..size(x)-1], int c:= charAt(x,i)) {
     if (c >= 101 && c <= 132) // A-Z
        append \char-class([range(c,c),range(c+40,c+40)]);
     else if (c >= 141 && c <= 172) // a-z
        append \char-class([range(c,c),range(c-40,c-40)]);
     else 
        append \char-class([range(c,c)]);
  } 
}

private list[Symbol] intermix(list[Symbol] syms) {
  if (syms == []) return syms;
  return tail([\iter-star(\layout()), s | s <- syms]);
}

private Production prod2prod(Symbol nt, Prod p) {
  switch(p) {
    case (Prod) `<ProdModifier* ms> <Name n> : <Sym* args>` :
      return prod(args2symbols(args, hasLex(ms)), nt, mods2attrs(n, ms));
    case (Prod) `<ProdModifier* ms> <Sym* args>` :
      return prod(args2symbols(args, hasLex(ms)), nt, mods2attrs(ms));
    case (Prod) `<Prod l> | <Prod r>` :
      return choice(nt,{prod2prod(nt, l), prod2prod(nt, r)});
    case (Prod) `<Prod l> # <Prod r>` :
      if (prod(list[Symbol] syms, Symbol s, Attributes a) := prod2prod(nt, r))
        return restrict(sort(prod2prod(nt,l)),prod2prod(nt,l), {syms});
    case (Prod) `<Prod l> > <Prod r>` :
      return first(nt,[prod2prod(nt, l), prod2prod(nt, r)]);
    case (Prod) `<Prod l> - <Prod r>` :
      return diff(nt, prod2prod(nt, l), {attribute(prod2prod(nt, r), reject())});
    case (Prod) `left (<Prod p>)` :
      return \assoc(nt, \left(), {attribute(prod2prod(nt, p), \assoc(\left()))});
    case (Prod) `right (<Prod p>)` :
      return \assoc(nt, \right(), {attribute(prod2prod(nt, p), \assoc(\right()))});
    case (Prod) `non-assoc (<Prod p>)` :
      return \assoc(nt, \non-assoc(), {attribute(prod2prod(nt, p), \assoc(\non-assoc()))});
    case (Prod) `assoc(<Prod p>)` :
      return \assoc(nt, \left(), {attribute(prod2prod(nt, p),\assoc(\assoc()))});
    case `...`: return \others(nt);
    case `: <Name n>`: throw "prod referencing is not yet implemented";
    default: throw "missed a case <p>";
  } 
}

@doc{adds an attribute to all productions it can find}
private Production attribute(Production p, Attr a) {
  return visit (p) {
    case prod(lhs,rhs,\no-attrs()) => prod(lhs, rhs, attrs([a]))
    case prod(lhs,rhs,attrs(list[Attributes] l)) => prod(lhs, rhs, attrs([l, a]))
  }
}

private bool hasLex(ProdModifier* ms) {
  return /(ProdModifier) `lex` := ms;
}

private list[Symbol] args2symbols(Sym* args, bool isLex) {
  return [ arg2symbol(s, isLex) | Sym s <- args ];
}

private list[Symbol] separgs2symbols({Sym ","}+ args, bool isLex) {
  return [ arg2symbol(s, isLex) | Sym s <- args ];
}
   
private Symbol arg2symbol(Sym sym, bool isLex) {
  switch (sym) {
    case (Sym) `<Nonterminal n>`          : return sort("<n>");
    case (Sym) `<StringConstant l>` : return lit(unescape(l));
    case (Sym) `<ParameterizedNonterminal n>[<{Sym ","}+ syms>]` : return \parameterized-sort("<n>",separgs2symbols(syms,isLex));
    case (Sym) `<Sym s> <NonterminalLabel n>` : return label("<n>", arg2symbol(s,isLex));
    case (Sym) `<Sym s> ?`  : return opt(arg2symbol(s,isLex));
    case (Sym) `<Sym s> ??` : return opt(arg2symbol(s,isLex));
    case (Sym) `<Class cc>` : return cc2ranges(cc);
    case (Sym) `&<Nonterminal n>` : return \parameter("<n>");
  }  
  
  if (isLex) switch (sym) {
    case (Sym) `<Sym s> *`  : return \iter-star(arg2symbol(s,isLex));
    case (Sym) `<Sym s> +`  : return \iter(arg2symbol(s,isLex));
    case (Sym) `<Sym s> *?` : return \iter-star(arg2symbol(s,isLex));
    case (Sym) `<Sym s> +?` : return \iter(arg2symbol(s,isLex));
    case (Sym) `{<Sym s> <StringConstant sep>} *`  : return \iter-star-seps(arg2symbol(s,isLex), [lit(unescape(sep))]);
    case (Sym) `{<Sym s> <StringConstant sep>} +`  : return \iter-seps(arg2symbol(s,isLex), [lit(unescape(sep))]);
    case (Sym) `{<Sym s> <StringConstant sep>} *?` : return \iter-star-seps(arg2symbol(s,isLex), [lit(unescape(sep))]);
    case (Sym) `{<Sym s> <StringConstant sep>} +?` : return \iter-seps(arg2symbol(s,isLex), [lit(unescape(sep))]);
    default: throw "missed a case <sym>";
  } 
  else switch (sym) {  
    case (Sym) `<Sym s> *`  : return \iter-star-seps(arg2symbol(s,isLex),[\iter-star(\layout())]);
    case (Sym) `<Sym s> +`  : return \iter-seps(arg2symbol(s,isLex),[\iter-star(\layout())]);
    case (Sym) `<Sym s> *?` : return \iter-star-seps(arg2symbol(s,isLex),[\iter-star(\layout())]);
    case (Sym) `<Sym s> +?` : return \iter-seps(arg2symbol(s,isLex),[\iter-star(\layout())]);
    case (Sym) `{<Sym s> <StringConstant sep>} *`  : return \iter-star-seps(arg2symbol(s,isLex), [\iter-star(\layout()),lit(unescape(sep)),\iter-star(\layout())]);
    case (Sym) `{<Sym s> <StringConstant sep>} +`  : return \iter-seps(arg2symbol(s,isLex), [\iter-star(\layout()),lit(unescape(sep)),\iter-star(\layout())]);
    case (Sym) `{<Sym s> <StringConstant sep>} *?` : return \iter-star-seps(arg2symbol(s,isLex), [\iter-star(\layout()),lit(unescape(sep)),\iter-star(\layout())]);
    case (Sym) `{<Sym s> <StringConstant sep>} +?` : return \iter-seps(arg2symbol(s,isLex), [\iter-star(\layout()),lit(unescape(sep)),\iter-star(\layout())]);
    default: throw "missed a case <sym>";  
  }
}
  
private str unescape(StringConstant s) {
   if ([StringConstant] /\"<rest:.*>\"/ := s) {
     return visit (rest) {
       case /\\b/ => "\b"
       case /\\f/ => "\f"
       case /\\n/ => "\n"
       case /\\t/ => "\t"
       case /\\r/ => "\r"  
       case /\\\"/ => "\""  
       case /\\\'/ => "\'"
       case /\\\\/ => "\\"
       case /\\\</ => "\<"   
       case /\\\>/ => "\>"    
     };      
   }
   throw "unexpected string format: <s>";
}

private Symbol cc2ranges(Class cc) {
   switch(cc) {
     case (Class) `[<Range* ranges>]` : return \char-class([range(r) | r <- ranges]);
     case (Class) `(<Class c>)`: return cc2ranges(c);
     case (Class) `! <Class c>`: return complement(cc2ranges(c));
     case (Class) `<Class l> && <Class r>`: return intersection(cc2ranges(l), cc2ranges(r));
     case (Class) `<Class l> || <Class r>`: return union(cc2ranges(l), cc2ranges(r));
     case (Class) `<Class l> - <Class r>`: return difference(cc2ranges(l), cc2ranges(r));
     default: throw "missed a case <cc>";
   }
}
      
private CharRange range(Range r) {
  switch (r) {
    case (Range) `<Char c>` : return range(character(c),character(c));
    case (Range) `<Char l> - <Char r>`: return range(character(l),character(r));
    default: throw "missed a case <r>";
  }
} 
 
private int character(Char c) {
  switch (c) {
    case [Char] /\\n/ : return charAt("\n", 0);
    case [Char] /\\t/ : return charAt("\t", 0);
    case [Char] /\\b/ : return charAt("\b", 0);
    case [Char] /\\r/ : return charAt("\r", 0);
    case [Char] /\\f/ : return charAt("\f", 0);
    case [Char] /\\\>/ : return charAt("\>", 0);
    case [Char] /\\\</ : return charAt("\<", 0);
    case [Char] /<ch:[^"'\-\[\]\\\>\< ]>/        : return charAt(ch, 0); 
    case [Char] /\\<esc:["'\-\[\]\\ ]>/        : return charAt(esc, 0);
    case [Char] /\\[u]+<hex:[0-9a-fA-F][0-9a-fA-F][0-9a-fA-F][0-9a-fA-F]>/ : return toInt("0x<hex>");
    case [Char] /\\<oct:[0-3][0-7][0-7]>/ : return toInt("0<oct>");
    case [Char] /\\<oct:[0-7][0-7]>/      : return toInt("0<oct>");
    case [Char] /\\<oct:[0-7]>/           : return toInt("0<oct>");
    default: throw "missed a case <c>";
  }
}

private Attributes mods2attrs(Name name, ProdModifier* mods) {
  return attrs([term("cons"("<name>"))] + [ mod2attr(m) | m <- mods]);
}

private Attributes mods2attrs(ProdModifier* mods) {
  return attrs([mod2attr(m) | ProdModifier m <- mods]);
}
 
private Attr mod2attr(ProdModifier m) {
  switch (m) {
    case (ProdModifier) `lex`: return term("lex"());
    case (ProdModifier) `left`: return \assoc(\left());
    case (ProdModifier) `right`: return \assoc(\right());
    case (ProdModifier) `non-assoc`: return \assoc(\non-assoc());
    case (ProdModifier) `assoc`: return \assoc(\assoc());
    case (ProdModifier) `bracket`: return \bracket();
    default: throw "missed a case <m>";
  }
}
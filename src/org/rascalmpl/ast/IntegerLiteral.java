/*******************************************************************************
 * Copyright (c) 2009-2011 CWI
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
 *******************************************************************************/
package org.rascalmpl.ast;


import org.eclipse.imp.pdb.facts.IConstructor;
import org.eclipse.imp.pdb.facts.ISourceLocation;
import org.eclipse.imp.pdb.facts.IValue;
import org.rascalmpl.interpreter.Evaluator;
import org.rascalmpl.interpreter.asserts.Ambiguous;
import org.rascalmpl.interpreter.env.Environment;
import org.rascalmpl.interpreter.result.Result;

public abstract class IntegerLiteral extends AbstractAST {
  public IntegerLiteral(IConstructor node) {
    super(node);
  }

  
  public boolean hasOctal() {
    return false;
  }

  public org.rascalmpl.ast.OctalIntegerLiteral getOctal() {
    throw new UnsupportedOperationException();
  }
  public boolean hasHex() {
    return false;
  }

  public org.rascalmpl.ast.HexIntegerLiteral getHex() {
    throw new UnsupportedOperationException();
  }
  public boolean hasDecimal() {
    return false;
  }

  public org.rascalmpl.ast.DecimalIntegerLiteral getDecimal() {
    throw new UnsupportedOperationException();
  }

  static public class Ambiguity extends IntegerLiteral {
    private final java.util.List<org.rascalmpl.ast.IntegerLiteral> alternatives;
    private final IConstructor node;
           
    public Ambiguity(IConstructor node, java.util.List<org.rascalmpl.ast.IntegerLiteral> alternatives) {
      super(node);
      this.node = node;
      this.alternatives = java.util.Collections.unmodifiableList(alternatives);
    }
    
    @Override
    public IConstructor getTree() {
      return node;
    }
  
    @Override
    public AbstractAST findNode(int offset) {
      return null;
    }
  
    @Override
    public Result<IValue> interpret(Evaluator __eval) {
      throw new Ambiguous(node);
    }
      
    @Override
    public org.eclipse.imp.pdb.facts.type.Type typeOf(Environment env) {
      throw new Ambiguous(node);
    }
    
    public java.util.List<org.rascalmpl.ast.IntegerLiteral> getAlternatives() {
      return alternatives;
    }
    
    public <T> T accept(IASTVisitor<T> v) {
    	return v.visitIntegerLiteralAmbiguity(this);
    }
  }

  

  
  public boolean isOctalIntegerLiteral() {
    return false;
  }

  static public class OctalIntegerLiteral extends IntegerLiteral {
    // Production: sig("OctalIntegerLiteral",[arg("org.rascalmpl.ast.OctalIntegerLiteral","octal")])
  
    
    private final org.rascalmpl.ast.OctalIntegerLiteral octal;
  
    public OctalIntegerLiteral(IConstructor node , org.rascalmpl.ast.OctalIntegerLiteral octal) {
      super(node);
      
      this.octal = octal;
    }
  
    @Override
    public boolean isOctalIntegerLiteral() { 
      return true; 
    }
  
    @Override
    public <T> T accept(IASTVisitor<T> visitor) {
      return visitor.visitIntegerLiteralOctalIntegerLiteral(this);
    }
  
    
    @Override
    public org.rascalmpl.ast.OctalIntegerLiteral getOctal() {
      return this.octal;
    }
  
    @Override
    public AbstractAST findNode(int offset) {
      if (src.getOffset() <= offset && offset < src.getOffset() + src.getLength()) {
        return this;
      }
      ISourceLocation loc;
      loc = octal.getLocation();
      if (offset <= loc.getOffset() + loc.getLength()) {
        return octal.findNode(offset);
      } 
      
      return null;
    }
  
    @Override
    public boolean hasOctal() {
      return true;
    }	
  }
  public boolean isHexIntegerLiteral() {
    return false;
  }

  static public class HexIntegerLiteral extends IntegerLiteral {
    // Production: sig("HexIntegerLiteral",[arg("org.rascalmpl.ast.HexIntegerLiteral","hex")])
  
    
    private final org.rascalmpl.ast.HexIntegerLiteral hex;
  
    public HexIntegerLiteral(IConstructor node , org.rascalmpl.ast.HexIntegerLiteral hex) {
      super(node);
      
      this.hex = hex;
    }
  
    @Override
    public boolean isHexIntegerLiteral() { 
      return true; 
    }
  
    @Override
    public <T> T accept(IASTVisitor<T> visitor) {
      return visitor.visitIntegerLiteralHexIntegerLiteral(this);
    }
  
    
    @Override
    public org.rascalmpl.ast.HexIntegerLiteral getHex() {
      return this.hex;
    }
  
    @Override
    public AbstractAST findNode(int offset) {
      if (src.getOffset() <= offset && offset < src.getOffset() + src.getLength()) {
        return this;
      }
      ISourceLocation loc;
      loc = hex.getLocation();
      if (offset <= loc.getOffset() + loc.getLength()) {
        return hex.findNode(offset);
      } 
      
      return null;
    }
  
    @Override
    public boolean hasHex() {
      return true;
    }	
  }
  public boolean isDecimalIntegerLiteral() {
    return false;
  }

  static public class DecimalIntegerLiteral extends IntegerLiteral {
    // Production: sig("DecimalIntegerLiteral",[arg("org.rascalmpl.ast.DecimalIntegerLiteral","decimal")])
  
    
    private final org.rascalmpl.ast.DecimalIntegerLiteral decimal;
  
    public DecimalIntegerLiteral(IConstructor node , org.rascalmpl.ast.DecimalIntegerLiteral decimal) {
      super(node);
      
      this.decimal = decimal;
    }
  
    @Override
    public boolean isDecimalIntegerLiteral() { 
      return true; 
    }
  
    @Override
    public <T> T accept(IASTVisitor<T> visitor) {
      return visitor.visitIntegerLiteralDecimalIntegerLiteral(this);
    }
  
    
    @Override
    public org.rascalmpl.ast.DecimalIntegerLiteral getDecimal() {
      return this.decimal;
    }
  
    @Override
    public AbstractAST findNode(int offset) {
      if (src.getOffset() <= offset && offset < src.getOffset() + src.getLength()) {
        return this;
      }
      ISourceLocation loc;
      loc = decimal.getLocation();
      if (offset <= loc.getOffset() + loc.getLength()) {
        return decimal.findNode(offset);
      } 
      
      return null;
    }
  
    @Override
    public boolean hasDecimal() {
      return true;
    }	
  }
}
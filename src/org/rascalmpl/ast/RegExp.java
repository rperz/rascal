/*******************************************************************************
 * Copyright (c) 2009-2014 CWI
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
package org.rascalmpl.ast;


import org.eclipse.imp.pdb.facts.IConstructor;

public abstract class RegExp extends AbstractAST {
  public RegExp(IConstructor node) {
    super();
  }

  

  static public class Lexical extends RegExp {
  private final java.lang.String string;
  public Lexical(IConstructor node, java.lang.String string) {
    super(node);
    this.string = string;
  }
  public java.lang.String getString() {
    return string;
  }

  @Override
  public int hashCode() {
    return string.hashCode();
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof Lexical && ((Lexical) o).string.equals(string);  
  }

  @Override
  public AbstractAST findNode(int offset) {
    if (src.getOffset() <= offset && offset < src.getOffset() + src.getLength()) {
      return this;
    }
    return null;
  }

  public java.lang.String toString() {
    return string;
  }
  public <T> T accept(IASTVisitor<T> v) {
    return v.visitRegExpLexical(this);
  }
}

  
}
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

public abstract class Variable extends AbstractAST {
  public Variable(IConstructor node) {
    super();
  }

  
  public boolean hasInitial() {
    return false;
  }

  public org.rascalmpl.ast.Expression getInitial() {
    throw new UnsupportedOperationException();
  }
  public boolean hasName() {
    return false;
  }

  public org.rascalmpl.ast.Name getName() {
    throw new UnsupportedOperationException();
  }

  

  
  public boolean isInitialized() {
    return false;
  }

  static public class Initialized extends Variable {
    // Production: sig("Initialized",[arg("org.rascalmpl.ast.Name","name"),arg("org.rascalmpl.ast.Expression","initial")])
  
    
    private final org.rascalmpl.ast.Name name;
    private final org.rascalmpl.ast.Expression initial;
  
    public Initialized(IConstructor node , org.rascalmpl.ast.Name name,  org.rascalmpl.ast.Expression initial) {
      super(node);
      
      this.name = name;
      this.initial = initial;
    }
  
    @Override
    public boolean isInitialized() { 
      return true; 
    }
  
    @Override
    public <T> T accept(IASTVisitor<T> visitor) {
      return visitor.visitVariableInitialized(this);
    }
  
    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Initialized)) {
        return false;
      }        
      Initialized tmp = (Initialized) o;
      return true && tmp.name.equals(this.name) && tmp.initial.equals(this.initial) ; 
    }
   
    @Override
    public int hashCode() {
      return 181 + 463 * name.hashCode() + 569 * initial.hashCode() ; 
    } 
  
    
    @Override
    public org.rascalmpl.ast.Name getName() {
      return this.name;
    }
  
    @Override
    public boolean hasName() {
      return true;
    }
    @Override
    public org.rascalmpl.ast.Expression getInitial() {
      return this.initial;
    }
  
    @Override
    public boolean hasInitial() {
      return true;
    }	
  }
  public boolean isUnInitialized() {
    return false;
  }

  static public class UnInitialized extends Variable {
    // Production: sig("UnInitialized",[arg("org.rascalmpl.ast.Name","name")])
  
    
    private final org.rascalmpl.ast.Name name;
  
    public UnInitialized(IConstructor node , org.rascalmpl.ast.Name name) {
      super(node);
      
      this.name = name;
    }
  
    @Override
    public boolean isUnInitialized() { 
      return true; 
    }
  
    @Override
    public <T> T accept(IASTVisitor<T> visitor) {
      return visitor.visitVariableUnInitialized(this);
    }
  
    @Override
    public boolean equals(Object o) {
      if (!(o instanceof UnInitialized)) {
        return false;
      }        
      UnInitialized tmp = (UnInitialized) o;
      return true && tmp.name.equals(this.name) ; 
    }
   
    @Override
    public int hashCode() {
      return 859 + 463 * name.hashCode() ; 
    } 
  
    
    @Override
    public org.rascalmpl.ast.Name getName() {
      return this.name;
    }
  
    @Override
    public boolean hasName() {
      return true;
    }	
  }
}
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

public abstract class ProtocolTail extends AbstractAST {
  public ProtocolTail(IConstructor node) {
    super();
  }

  
  public boolean hasExpression() {
    return false;
  }

  public org.rascalmpl.ast.Expression getExpression() {
    throw new UnsupportedOperationException();
  }
  public boolean hasMid() {
    return false;
  }

  public org.rascalmpl.ast.MidProtocolChars getMid() {
    throw new UnsupportedOperationException();
  }
  public boolean hasPost() {
    return false;
  }

  public org.rascalmpl.ast.PostProtocolChars getPost() {
    throw new UnsupportedOperationException();
  }
  public boolean hasTail() {
    return false;
  }

  public org.rascalmpl.ast.ProtocolTail getTail() {
    throw new UnsupportedOperationException();
  }

  

  
  public boolean isMid() {
    return false;
  }

  static public class Mid extends ProtocolTail {
    // Production: sig("Mid",[arg("org.rascalmpl.ast.MidProtocolChars","mid"),arg("org.rascalmpl.ast.Expression","expression"),arg("org.rascalmpl.ast.ProtocolTail","tail")])
  
    
    private final org.rascalmpl.ast.MidProtocolChars mid;
    private final org.rascalmpl.ast.Expression expression;
    private final org.rascalmpl.ast.ProtocolTail tail;
  
    public Mid(IConstructor node , org.rascalmpl.ast.MidProtocolChars mid,  org.rascalmpl.ast.Expression expression,  org.rascalmpl.ast.ProtocolTail tail) {
      super(node);
      
      this.mid = mid;
      this.expression = expression;
      this.tail = tail;
    }
  
    @Override
    public boolean isMid() { 
      return true; 
    }
  
    @Override
    public <T> T accept(IASTVisitor<T> visitor) {
      return visitor.visitProtocolTailMid(this);
    }
  
    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Mid)) {
        return false;
      }        
      Mid tmp = (Mid) o;
      return true && tmp.mid.equals(this.mid) && tmp.expression.equals(this.expression) && tmp.tail.equals(this.tail) ; 
    }
   
    @Override
    public int hashCode() {
      return 311 + 853 * mid.hashCode() + 347 * expression.hashCode() + 233 * tail.hashCode() ; 
    } 
  
    
    @Override
    public org.rascalmpl.ast.MidProtocolChars getMid() {
      return this.mid;
    }
  
    @Override
    public boolean hasMid() {
      return true;
    }
    @Override
    public org.rascalmpl.ast.Expression getExpression() {
      return this.expression;
    }
  
    @Override
    public boolean hasExpression() {
      return true;
    }
    @Override
    public org.rascalmpl.ast.ProtocolTail getTail() {
      return this.tail;
    }
  
    @Override
    public boolean hasTail() {
      return true;
    }	
  }
  public boolean isPost() {
    return false;
  }

  static public class Post extends ProtocolTail {
    // Production: sig("Post",[arg("org.rascalmpl.ast.PostProtocolChars","post")])
  
    
    private final org.rascalmpl.ast.PostProtocolChars post;
  
    public Post(IConstructor node , org.rascalmpl.ast.PostProtocolChars post) {
      super(node);
      
      this.post = post;
    }
  
    @Override
    public boolean isPost() { 
      return true; 
    }
  
    @Override
    public <T> T accept(IASTVisitor<T> visitor) {
      return visitor.visitProtocolTailPost(this);
    }
  
    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Post)) {
        return false;
      }        
      Post tmp = (Post) o;
      return true && tmp.post.equals(this.post) ; 
    }
   
    @Override
    public int hashCode() {
      return 563 + 431 * post.hashCode() ; 
    } 
  
    
    @Override
    public org.rascalmpl.ast.PostProtocolChars getPost() {
      return this.post;
    }
  
    @Override
    public boolean hasPost() {
      return true;
    }	
  }
}
 module lang::rascal::tests::functionality::AssignmentTests
  
// testSimple
  
test bool testSimple1() {bool b = true; return b == true;}
test bool testSimple2() {b = true; return b == true;}
  	
// testInteger
  
test bool testInteger1() {int N = 3; N += 2; return N==5;}
test bool testInteger2() {int N = 3; N -= 2; return N==1;}
test bool testInteger3() {int N = 3; N *= 2; return N==6;}
test bool testInteger4() {int N = 6; N /= 2; return N==3;}
test bool testInteger5() {int N = 6; N ?= 2; return N==6;}
  	
// testTuple
  
test bool testTuple1() {int a = 1; int b = 2; <a, b> = <b, a>; return (a == 2) && (b == 1);}
test bool testTuple2() {<a, b> = <1, 2>; return (a == 1) && (b == 2);}
test bool testTuple3() {tuple[str name, int cnt] T = <"abc", 1>; T.name = "def"; return T.name == "def";}
test bool testTuple4() {tuple[str name, int cnt] T = <"abc", 1>; return T[name = "def"] == <"def", 1>;}
  
// testList
  
test bool testList1() {list[int] L = []; return L == [];}
test bool testList2() {list[int] L = [0,1,2]; L[1] = 10; return L == [0,10,2];}
test bool testList3() {L = [0,1,2]; L[1] = 10; return L == [0,10,2];}
test bool testList4() {list[list[int]] L = [[0,1],[2,3]]; L[1][0] = 20; return L == [[0,1],[20,3]];}
test bool testList5() {L = [[0,1],[2,3]]; L[1][0] = 20; return  L == [[0,1],[20,3]];}
  		
test bool testList6() {list[int] L = [1,2,3]; L += [4]; return  L==[1,2,3,4];}
test bool testList7() {list[int] L = [1,2,3]; L -= [2]; return  L==[1,3];}
test bool testList8() {list[int] L = [1,2,3]; L ?= [4]; return  L==[1,2,3];}
  
test bool testList10() {list[list[int]] L = [[1,2,3],[10,20,30]]; L[0] += [4]; return  L==[[1,2,3,4],[10,20,30]];}
test bool testList11() {list[list[int]] L = [[1,2,3],[10,20,30]]; L[0] -= [2]; return  L==[[1,3],[10,20,30]];}
test bool testList12() {list[list[int]] L = [[1,2,3],[10,20,30]]; L[0] ?= [4]; return  L==[[1,2,3],[10,20,30]];}
  	
// testMap
  
test bool testMap1() {map[int,int] M = (); return M == ();}
test bool testMap2() {map[int,int] M = (1:10, 2:20);return  M == (1:10, 2:20);}
  		
test bool testMap3() {map[int,int] M = (1:10, 2:20); M += (3:30); return M==(1:10, 2:20,3:30);}
test bool testMap4() {map[int,int] M = (1:10, 2:20); M -= (2:20); return M==(1:10);}
test bool testMap5() {map[int,int] M = (1:10, 2:20); M ?= (3:30); return M==(1:10, 2:20);}
  
test bool testMap7() {map[int,list[int]] M = (0:[1,2,3],1:[10,20,30]); M[0] += [4]; return M==(0:[1,2,3,4],1:[10,20,30]);}
test bool testMap8() {map[int,list[int]] M = (0:[1,2,3],1:[10,20,30]); M[0] -= [2]; return M==(0:[1,3],1:[10,20,30]);}
test bool testMap9() {map[int,list[int]] M = (0:[1,2,3],1:[10,20,30]); M[0] ?= [4]; return M==(0:[1,2,3],1:[10,20,30]);}
test bool testMap10() {map[int, list[int]] M = (0:[1,2,3],1:[10,20,30]); M[2] ?= [4]; return M==(0:[1,2,3],1:[10,20,30], 2:[4]);}

test bool testMap11() {map[int,int] M = (1:10, 2:20); M[2] ? 0 += 30; return M==(1:10, 2:50);}
test bool testMap11() {map[int,int] M = (1:10, 2:20); M[3] ? 0 += 30; return M==(1:10, 2:20, 3:30);}
  	
// testSet
  
test bool testSet1() {set[int] S = {}; return S == {};}
test bool testSet2() {set[int] S = {0,1,2}; return S == {0, 1, 2};}
  		
test bool testSet3() {set[int] L = {1,2,3}; L += {4}; return  L=={1,2,3,4};}
test bool testSet4() {set[int] L = {1,2,3}; L -= {2}; return L=={1,3};}
test bool testSet5() {set[int] L = {1,2,3}; L ?= {4}; return L=={1,2,3};}

data D = listfield(list[int] ints) | intfield(int i);
  	
// testADT
  
test bool testADT1() {
		    D d = listfield([1,2]); 
    d.ints += [3]; 
    return d == listfield([1,2,3]);
}
   
test bool testADT2() {
    D d = listfield([1,2]); 
    d.ints -= [2]; 
    return d == listfield([1]);
}
            
test bool testADT31() { D d = intfield(2); d.i += 3; return d == intfield(5); }
test bool testADT32() { D d = intfield(5); d.i -= 3; return d == intfield(2); }
test bool testADT33() { D d = intfield(5); d.i *= 3; return d == intfield(15); }
test bool testADT34() { D d = intfield(6); d.i /= 3; return d == intfield(2); }
  	
data F = f() | f(int n) | g(int n) | deep(F f);
anno int F @ pos;
  
// testAnnotations
 
test bool testAnnotations1() { F X = f(); X @ pos = 1; return X @ pos == 1; }
  
test bool testAnnotations2() { X = f(); X @ pos = 2; X @ pos += 3; return X @ pos == 5; }

test bool testAnnotations3() { X = f(); X @ pos = 3; X @ pos -= 2;  return X @ pos == 1; }

test bool testAnnotations4() { X = f(); X @ pos = 2; X @ pos *= 3; return X @ pos == 6; }

test bool testAnnotations5() { X = f(); X @ pos = 6; X @ pos /= 3;  return X @ pos == 2; }

test bool testAnnotations6() { X = f(); X @ pos = 6; X @ pos ?= 3;  return X @ pos == 6; }

test bool testAnnotations7() { X = f(); X @ pos ?= 3; return X @ pos == 3; }
  	
// assigningClosureToVariableBug877
  
test bool assigningClosureToVariableBug8771() {	
    bool() x = bool(){ return true; };
    		return x() == true;
}
  	
data FK(int kw1 = 0) = h(int w = -1);

@ignoreCompiler{Not yet implemented}
test bool testKwParams1() { 
  FK X = h();
  assert X.w == -1;
  X.w *= 2;
  return X.w == -2;
}

@ignoreCompiler{Not yet implemented}
test bool testKwParams2() { 
  FK X = h();
  assert X.kw1 == 0;
  X.kw1 = 2;
  return X.kw1 == 2;
}

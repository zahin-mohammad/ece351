
abstract sig Boolean {}
one sig true extends Boolean {}
abstract sig Var { v : lone Boolean }
one sig  TRUE,FALSE extends Var {}
fact{
	some TRUE.v
	no FALSE.v
}
fun _equal[a,b:Var]:Var{_xnor[a,b]}
fun _xor[a,b:Var]:Var{ _or[_and[a, _not[b]], _and[_not[a], b]] }
fun _or[a,b:Var]:Var{{v':(a+b) | ((some a.v)or(some b.v))<=>(v' in a+b)}}
fun _and[a,b:Var]:Var{{v':(a+b) | ((some a.v)and(some b.v))<=>(v' in a+b)}}
fun _xnor[a,b:Var]:Var{_not[_xor[a,b]]}
fun _nor[a,b:Var]:Var{_not[_or[a,b]]}
fun _nand[a,b:Var]:Var{_not[_and[a,b]]}
fun _not[a:Var]:Var{{v':Var|v'.v!=a.v}}
one sig _XNOR_testa, _XNOR_testb, _XNOR_testx, _XNOR_testy extends Var {}
pred _XNOR_testF1[] {some  _not[ _or[ _and[ _XNOR_testx,  _not[ _XNOR_testy] ] , _and[  _not[ _XNOR_testx] , _XNOR_testy] ] ] .v}
pred _XNOR_testZ1[] {some  _not[ _or[ _and[ _and[ _XNOR_testx, _XNOR_testy] ,  _not[  _not[ _or[ _and[ _XNOR_testa,  _not[ _XNOR_testb] ] , _and[  _not[ _XNOR_testa] , _XNOR_testb] ] ] ] ] , _and[  _not[ _and[ _XNOR_testx, _XNOR_testy] ] ,  _not[ _or[ _and[ _XNOR_testa,  _not[ _XNOR_testb] ] , _and[  _not[ _XNOR_testa] , _XNOR_testb] ] ] ] ] ] .v}
pred _XNOR_testF2[] {some  _not[ _or[ _and[ _XNOR_testx,  _not[ _XNOR_testy] ] , _and[  _not[ _XNOR_testx] , _XNOR_testy] ] ] .v}
pred _XNOR_testZ2[] {some  _not[ _or[ _and[ _and[ _XNOR_testx, _XNOR_testy] ,  _not[  _not[ _or[ _and[ _XNOR_testa,  _not[ _XNOR_testb] ] , _and[  _not[ _XNOR_testa] , _XNOR_testb] ] ] ] ] , _and[  _not[ _and[ _XNOR_testx, _XNOR_testy] ] ,  _not[ _or[ _and[ _XNOR_testa,  _not[ _XNOR_testb] ] , _and[  _not[ _XNOR_testa] , _XNOR_testb] ] ] ] ] ] .v}
assert equivalent {
    _XNOR_testF1 <=> _XNOR_testF2
    _XNOR_testZ1 <=> _XNOR_testZ2
}
check equivalent

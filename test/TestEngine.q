// SECTION A - Global variables

public int globalInt;
public real globalReal;
public boolean globalBoolean;

public IntArray globalIntArray;
public RealArray globalRealArray;
public BooleanArray globalBooleanArray;

public IntMatrix globalIntMatrix;
public RealMatrix globalRealMatrix;
public BooleanMatrix globalBooleanMatrix;

public Pmf globalSimplePmf;
public PmfArray globalSimplePmfArray;
public PmfMatrix globalSimplePmfMatrix;

public Pmf{A,B} globalJointPmf;
public PmfArray{A,B} globalJointPmfArray;
public PmfMatrix{A,B} globalJointPmfMatrix;

public Pmf{(A,B),(C)} globalCompoundPmf;
public PmfArray{(A,B),(C)} globalCompoundPmfArray;
public PmfMatrix{(A,B),(C)} globalCompoundPmfMatrix;

public interface int globalInterface(int i, boolean b);
public InterfaceArray int globalInterfaceArray(int i, boolean b);
public InterfaceMatrix int globalInterfaceMatrix(int i, boolean b);

void f0() {}

int fZ1(int i, boolean b) {
    if (b) {
        return i;
    } else {
        return 0;
    }
}

int fZ2(int i, boolean b) {
    return 42;
}

public InterfaceArray IntArray globalInterfaceArrayFunction(int i, boolean b);

IntArray fZ3(int i, boolean b) {
    if (b) {
        IntArray a = [i];
        return a;
    } else {
        IntArray a = [0];
        return a;
    }
}

token 'ABC' = 42;
public token 'XYZ' = 42;

// SECTION B - Initializers

public IntArray fB1() {
    return [1, 2, 3];
}

public RealArray fB2() {
    return [1.1, 2.2, 3.3];
}

public BooleanArray fB3() {
    return [true, false, true];
}

public IntMatrix fB4() {
    return [[1, 2, 3], [4]];
}

public RealMatrix fB5() {
    return [[1.1, 1.2, 1.3], [2.1, 2.2]];
}

public BooleanMatrix fB6() {
    return [[true, false, true], [false, true]];
}

public Pmf fB7() {
    return {1:0.1, 2:0.2, 3:0.3, 4:0.4};
}

public Pmf{A,B} fB8() {
    return {(0,0):0.1, (1,1):0.2, (2,1):0.3, (2,2):0.4};
}

public Pmf{(A,B),(C)} fB9() {
    return (
        {(0,0):0.1, (1,1):0.2, (2,1):0.3, (2,2):0.4}, 
        {1:0.1, 2:0.2, 3:0.3, 4:0.4}
    );
}

public PmfArray fB10() {
    return [
        {1:0.1, 2:0.2, 3:0.3, 4:0.4},
        {1:0.4, 2:0.3, 3:0.2, 4:0.1}
    ];
}

public PmfArray{A,B} fB11() {
    return [
        {(0,0):0.1, (1,1):0.2, (2,1):0.3, (2,2):0.4},
        {(0,0):0.4, (1,1):0.3, (2,1):0.2, (2,2):0.1}
    ];
}

public PmfArray{(A,B),(C)} fB12() {
    return [
        (
            {(0,0):0.1, (1,1):0.2, (2,1):0.3, (2,2):0.4}, 
            {1:0.1, 2:0.2, 3:0.3, 4:0.4}
        ), (
            {(0,0):0.4, (1,1):0.3, (2,1):0.2, (2,2):0.1}, 
            {1:0.4, 2:0.3, 3:0.2, 4:0.1}
        )
    ];
}

public PmfMatrix fB13() {
    return [
        [
            {1:0.1, 2:0.2, 3:0.3, 4:0.4},
            {1:0.4, 2:0.3, 3:0.2, 4:0.1}
        ], [
            {1:0.4, 2:0.3, 3:0.2, 4:0.1}
        ]
    ];
}

public PmfMatrix{A,B} fB14() {
    return [
        [
            {(0,0):0.1, (1,1):0.2, (2,1):0.3, (2,2):0.4},
            {(0,0):0.4, (1,1):0.3, (2,1):0.2, (2,2):0.1}
        ], [
            {(0,0):0.1, (1,1):0.2, (2,1):0.3, (2,2):0.4}
        ]
    ];
}

public PmfMatrix{(A,B),(C)} fB15() {
    return [
        [
            (
                {(0,0):0.1, (1,1):0.2, (2,1):0.3, (2,2):0.4}, 
                {1:0.1, 2:0.2, 3:0.3, 4:0.4}
            ), (
                {(0,0):0.1, (1,1):0.2, (2,1):0.3, (2,2):0.4},
                {1:0.4, 2:0.3, 3:0.2, 4:0.1}
            )
        ], [
            (
                {(0,0):0.1, (1,1):0.2, (2,1):0.3, (2,2):0.4}, 
                {1:0.1, 2:0.2, 3:0.3, 4:0.4}
            )
        ]
    ];
}

public void fB16() {
    globalInterface = fZ1;
    globalInterfaceArray = [fZ1,fZ2];
    globalInterfaceMatrix = [[fZ1,fZ2],[fZ2]];
}

public void fB17() {
    IntArray intarray = createIntArray(3);
    RealArray realarray = createRealArray(3);
    BooleanArray booleanarray = createBooleanArray(3);
    PmfArray pmfarray = createPmfArray(3);
    PmfArray{?,?} jointpmfarray = createPmfArray(3);
}

// SECTION C - Functions and interfaces

public int fC1(real x) {
    int i = floor(x);
    return i;
}

public int fC2(real x) {
    return ceiling(x);
}

public int fC3(int i) {
    return min(i, 3);
}

public int fC4(int i) {
    int j = max(i, 3);
    return j;
}

public int fC5(int i, boolean b) {
    globalInterface = fZ1;
    return globalInterface(i, b);
} 

public int fC6(int i, boolean b) {
    globalInterfaceArray = createInterfaceArray(2);
    globalInterfaceArray[1] = fZ1;
    return globalInterfaceArray[1](i, b);
} 

public int fC7(int i, boolean b) {
    globalInterfaceMatrix = createInterfaceMatrix(2,1);
    globalInterfaceMatrix[1][0] = fZ1;
    return globalInterfaceMatrix[1][0](i, b);
} 

public int fC8(int i, boolean b) {
    globalInterfaceArrayFunction = createInterfaceArray(2);
    globalInterfaceArrayFunction[1] = fZ3;
    return globalInterfaceArrayFunction[1](i, b)[0];
} 

public real fC9() {
    return exp(1);
}

public real fC10() {
    return log(2.71828);
}

public real fC11() {
    return pow(1.414, 2);
}

public real fC12() {
    return sqrt(2);
}

public real fC15() {
    return randomInt(100);
}

public real fC16() {
    return randomReal();
}

public Pmf fC17() {
    RealArray b = [.1,.2,.3,.4];
    return createPmfFromRealArray(b);
}

public Pmf fC18() {
    return hypergeometric(5,3,2);
}

public Pmf{?,?,?} fC19() {
	IntArray b = [1,2,3];
    return multivariateHypergeometric(6,2,3,b);
}

public Pmf fC20() {
    return bernoulli(0.9);
}

public Pmf fC21() {
    return binomial(2, 0.9);
}

public Pmf{?,?,?,?} fC22() {
    globalSimplePmf = {0:0.1,1:0.2,2:0.3,3:0.4};
    return multinomial(2, 4, globalSimplePmf);
}

// SECTION D - Sampling

public Pmf p;
public Pmf q;
public Pmf{Z,L} mu;

public Pmf fD1(int n, real r) {
    return binomial(n, r);
}

public Pmf fD2() {
    Pmf b = binomial(4, 0.1);
    a ~ b;
    if (a == 1) {
        skip;
    } else {
        return a;
    }
}

public Pmf fD3() {
    a ~ p;
    b ~ q;
    return a+b;
}

public Pmf{Z,L} fD4() {
    a ~ p;
    b ~ q;
    return a,b;
}

public Pmf fD5() {
    z ~ mu;
    return z;
}

public Pmf fD6() {
    z, ell ~ mu;
    return ell;
}

public void fD7(int i, int j) {
    globalReal = mu{L|Z=i}[j];
}


// SECTION E - Pmf configurations

public Pmf{A,B} fE1() {
    Pmf pmf = {1:0.1, 2:0.2, 3:0.3, 4:0.4}; 
    i ~ pmf;
    return i, i+1; 
} 

public Pmf{A,B,C} fE2() {
    Pmf pmf = {1:0.1, 2:0.2, 3:0.3, 4:0.4}; 
    i ~ pmf;
    return i, i+1, i+2; 
} 

public Pmf{(A,B),(C)} fE3() {
    Pmf pmf = {1:0.1, 2:0.2, 3:0.3, 4:0.4}; 
    i ~ pmf;
    return (i, i+1), i+2; 
}

// SECTION F - Tokens and attributes

public int fF1() { return 'ABC'; }

public int fF2() { return globalIntArray.length; }

public int fF3() { return globalSimplePmf.minValue; }

public int fF4() { return globalSimplePmf.maxValue; }

// SECTION G - PMF FUNCTIONS

public int fG1() {
    Pmf pmf = {1:0.1, 2:0.2, 3:0.3, 4:0.4};
    return computeLeftTail(pmf, 0.15);
}

public int fG2() {
    Pmf pmf = {1:0.1, 2:0.2, 3:0.3, 4:0.4};
    return computeRightTail(pmf, 0.15);
}

public boolean fG3() {
    Pmf pmf = {1:0.1, 2:0.2, 3:0.3, 4:0.4};
    Pmf pmf2 = pmf;
    return isSamePmfInstance(pmf, pmf2);
}

// SECTION A - GLOBAL VARIABLES

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

public Pmf{A,B,C} globalJointPmf;
public PmfArray{A,B,C} globalJointPmfArray;
public PmfMatrix{A,B,C} globalJointPmfMatrix;

public Pmf{(A,B,C),(D)} globalCompoundPmf;
public PmfArray{(A,B,C),(D)} globalCompoundPmfArray;
public PmfMatrix{(A,B,C),(D)} globalCompoundPmfMatrix;

public interface int globalInterface(int i, boolean b);
public InterfaceArray int globalInterfaceArray(int i, boolean b);
public InterfaceMatrix int globalInterfaceMatrix(int i, boolean b);

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

// SECTION B - INITIALIZERS

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

public RealArray fB16(int size) {
    return createRealArray(size);
}

public IntMatrix fB17(int rows, int cols) {
    return createIntMatrix(rows, cols);
}

// SECTION C - FUNCTION CALLS AND BUILTINS

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

public int fC13(int a, int b) {
    return a % b;
}

public int fC14(int a, int b, int c) {
    return a * b % c;
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

public Pmf{?,?} fC23() {
    RealMatrix m = [[.05,.1,.15,.2], [.25,.25]];
    return createBivariatePmfFromRealMatrix(m);
}

// SECTION D - INDEXING

public int fD1() {
    IntArray a = [10, 20, 30];
    return a[0] * 100 + a[1] * 10 + a[2];
}

public int fD2() {
    IntMatrix m = [[1, 2], [3, 4]];
    return m[0][0] * 1000 + m[0][1] * 100 + m[1][0] * 10 + m[1][1];
}

public real fD3() {
    RealArray a = [1.5, 2.5, 3.5];
    return a[1];
}

public boolean fD4() {
    BooleanArray a = [true, false, true];
    return a[0];
}

public int fD5() {
    IntArray a = createIntArray(3);
    a[0] = 5;
    a[1] = 6;
    a[2] = 7;
    return a[0] * 100 + a[1] * 10 + a[2];
}

public int fD6() {
    IntMatrix m = createIntMatrix(2, 2);
    m[0][0] = 8;
    m[1][1] = 9;
    return m[0][0] * 10 + m[1][1];
}

public real fD7() {
    RealArray a = createRealArray(3);
    a[0] = 2.5;
    a[1] = a[0];
    return a[1];
}

public boolean fD8() {
    BooleanArray a = createBooleanArray(3);
    a[0] = true;
    a[1] = a[0];
    return a[1];
}

public Pmf fD9() {
    PmfArray a = createPmfArray(2);
    a[0] = {3:0.25, 7:0.75};
    return a[0];
}

public Pmf fD10() {
    PmfMatrix m = createPmfMatrix(2, 2);
    m[0][0] = {3:0.25, 7:0.75};
    return m[0][0];
}

public Pmf{?,?} fD11() {
    PmfArray{?,?} a = createPmfArray(2);
    a[0] = {(0,0):0.5, (1,1):0.5};
    a[1] = a[0];
    return a[1];
}

public int fD12(int i, boolean b) {
    globalInterfaceArray = [fZ1, fZ2];
    return globalInterfaceArray[0](i, b);
}

public int fD13(int i, boolean b) {
    globalInterfaceMatrix = [[fZ1, fZ2], [fZ2]];
    return globalInterfaceMatrix[0][0](i, b);
}

// SECTION E - SAMPLING

public Pmf p;
public Pmf q;
public Pmf{Z,L} mu;

public Pmf fE1(int n, real r) {
    return binomial(n, r);
}

public Pmf fE2() {
    Pmf b = binomial(4, 0.1);
    a ~ b;
    if (a == 1) {
        skip;
    } else {
        return a;
    }
}

public Pmf fE3() {
    a ~ p;
    b ~ q;
    return a+b;
}

public Pmf{Z,L} fE4() {
    a ~ p;
    b ~ q;
    return a,b;
}

public Pmf fE5() {
    z ~ mu{Z};
    return z;
}

public Pmf fE6() {
    z, ell ~ mu;
    return ell;
}

public void fE7(int i, int j) {
    globalReal = mu{L|Z=i}[j];
}


public Pmf fE8(real prob) {
    x ~ bernoulli(prob);
    y ~ bernoulli(prob);
    if (branchProbability() > 0.5) {
        return 1;
    } else {
        return 0;
    }
}

public Pmf fE9(Pmf dist, boolean first) {
    if (first) {
        outcome ~ dist;
        if (outcome == 0) {
            skip;
        } else {
            return outcome;
        }
    } else {
        outcome ~ dist;
        if (outcome == 0) {
            skip;
        } else {
            return outcome;
        }
    }
}

// SECTION F - PMF CONFIGURATIONS

public Pmf{A,B} fF1() {
    Pmf pmf = {1:0.1, 2:0.2, 3:0.3, 4:0.4}; 
    i ~ pmf;
    return i, i+1; 
} 

public Pmf{A,B,C} fF2() {
    Pmf pmf = {1:0.1, 2:0.2, 3:0.3, 4:0.4}; 
    i ~ pmf;
    return i, i+1, i+2; 
} 

public Pmf{(A,B),(C)} fF3() {
    Pmf pmf = {1:0.1, 2:0.2, 3:0.3, 4:0.4}; 
    i ~ pmf;
    return (i, i+1), i+2; 
}

// SECTION G - TOKENS AND ATTRIBUTES

public int fG1() { return 'ABC'; }

public int fG2() {
    Pmf pmf = {3:0.25, 7:0.75};
    return pmf.minValue;
}

public int fG3() { return globalIntArray.length; }

public int fG4() { return globalSimplePmf.minValue; }

public int fG5() { return globalSimplePmf.maxValue; }

// SECTION H - PMF FUNCTIONS

public int fH1() {
    Pmf pmf = {1:0.1, 2:0.2, 3:0.3, 4:0.4};
    return computeLeftTail(pmf, 0.15);
}

public int fH2() {
    Pmf pmf = {1:0.1, 2:0.2, 3:0.3, 4:0.4};
    return computeRightTail(pmf, 0.15);
}

public boolean fH3() {
    Pmf pmf = {1:0.1, 2:0.2, 3:0.3, 4:0.4};
    Pmf pmf2 = pmf;
    return isSamePmfInstance(pmf, pmf2);
}

// SECTION I - PMF EXTRACTION

public Pmf{A,B,C,D} globalQuadPmf;
public Pmf{(A,B,C,D),(E,F)} globalCompoundBigPmf;

public Pmf fI1() {
    Pmf extractedPmf = globalJointPmf{A};
    return extractedPmf;
}

public Pmf{A,B} fI2() {
    Pmf{A,B} extractedPmf = globalJointPmf{A,B};
    return extractedPmf;
}

public Pmf{A,B,C} fI3() {
    Pmf{A,B,C} extractedPmf = globalJointPmf{A,B,C};
    return extractedPmf;
}

public Pmf fI4(int aValue) {
    Pmf extractedPmf = globalJointPmf{B|A=aValue};
    return extractedPmf;
}

public Pmf{B,C} fI5(int aValue) {
    Pmf{B,C} extractedPmf = globalJointPmf{B,C|A=aValue};
    return extractedPmf;
}

public Pmf{A,B} fI6() {
    Pmf{A,B} extractedPmf = globalQuadPmf{A,B};
    return extractedPmf;
}

public Pmf{A,B,C} fI7() {
    Pmf{A,B,C} extractedPmf = globalQuadPmf{A,B,C};
    return extractedPmf;
}

public Pmf{B,C} fI8(int aValue) {
    Pmf{B,C} extractedPmf = globalQuadPmf{B,C|A=aValue};
    return extractedPmf;
}

public Pmf fI9(int aValue, int bValue) {
    Pmf extractedPmf = globalQuadPmf{C|A=aValue,B=bValue};
    return extractedPmf;
}

public Pmf{A,B} fI10() {
    Pmf{A,B} extractedPmf = globalCompoundPmf{A,B};
    return extractedPmf;
}

public Pmf fI11() {
    Pmf extractedPmf = globalCompoundPmf{A};
    return extractedPmf;
}

public Pmf fI12(int aValue) {
    Pmf extractedPmf = globalCompoundPmf{B|A=aValue};
    return extractedPmf;
}

public Pmf{B,C} fI13(int aValue) {
    Pmf{B,C} extractedPmf = globalCompoundPmf{B,C|A=aValue};
    return extractedPmf;
}

public Pmf fI14(int aValue, int bValue) {
    Pmf extractedPmf = globalCompoundPmf{C|A=aValue,B=bValue};
    return extractedPmf;
}

public Pmf{B,C} fI15(int aValue) {
    Pmf{B,C} extractedPmf = globalCompoundBigPmf{B,C|A=aValue};
    return extractedPmf;
}

public Pmf{A,B} fI16() {
    Pmf{A,B} extractedPmf = globalCompoundBigPmf{A,B};
    return extractedPmf;
}

public int fI17() {
    Pmf{A,B,C} dist = {(0,0,0):0.5, (2,0,0):0.5};
    Pmf{A,B} pair = dist{A,B};
    Pmf extractedPmf = pair{A};
    return extractedPmf.maxValue;
}

public int fI18() {
    Pmf{A,B,C} dist = {(0,0,0):0.25, (0,2,0):0.25, (3,1,1):0.5};
    Pmf{A,B} pair = dist{A,B};
    Pmf extractedPmf = pair{A};
    return extractedPmf.minValue;
}

// SECTION J - DATA OBJECTS

public Pmf fJ1(Pmf dist) {
    x ~ dist;
    return x + 1;
}

public Pmf{A,B} fJ2(Pmf dist) {
    x ~ dist;
    return x, x + 1;
}

public IntArray fJ3(IntArray a) {
    return a;
}

public Pmf{(A,B),(B)} globalOverlapPmf;

public void fJ4(Pmf{(A,B),(B)} dist) {
    globalOverlapPmf = dist;
}

// SECTION K - SIDE EFFECTS

int trace;

int record(int value) {
    trace = trace * 10 + value;
    return value;
}

public int fK1() {
    trace = 0;
    boolean ignored = record(1) > 0 && record(2) > 0;
    return trace;
}

public int fK2() {
    trace = 0;
    int remaining = 2;
    while (record(remaining) > 0 && remaining > 0) {
        remaining = remaining - 1;
    }
    return trace;
}

// SECTION L - CONTROL FLOW

public int fL1(int n) {
    int total = 0;
    for (i = 1 to n) {
        total = total + i;
    }
    return total;
}

public int fL2(int n) {
    int result = 1;
    int k = n;
    while (k > 0) {
        result = result * k;
        k = k - 1;
    }
    return result;
}

public int fL3(int i) {
    if (i > 0) {
        return 1;
    } else if (i < 0) {
        return -1;
    } else {
        return 0;
    }
}

public int fL4(int n) {
    int total = 0;
    for (i = 1 to n) {
        for (j = 1 to n) {
            total = total + i * j;
        }
    }
    return total;
}

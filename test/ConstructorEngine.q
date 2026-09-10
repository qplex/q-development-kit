public real p0;
public RealArray ra;
public RealMatrix rm;
public Pmf dist;
public PmfArray pa;
public PmfMatrix pm;

void init(real p) {
    if (p < 0.0 || p > 1.0) {
        fail "probability must be in [0,1]";
    }
    p0 = p;
    ra = [p, 1.0 - p];
    rm = [[p, 1.0 - p], [p]];
    dist = {0: 1.0 - p, 1: p};
    pa = [{0: 1.0 - p, 1: p}, {0: p, 1: 1.0 - p}];
    pm = [[{0: 1.0 - p, 1: p}], [{0: p, 1: 1.0 - p}]];
}

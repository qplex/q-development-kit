import sys

_passes = 0
_failures = 0

def expect(actual, expected):
    global _passes
    if _close(actual, expected):
        _passes += 1
        print("  ok   ", actual)
    else:
        fail("got " + repr(actual) + " want " + repr(expected))

def fail(message):
    global _failures
    _failures += 1
    print("  FAIL ", message)

class expect_error:
    def __enter__(self):
        return self
    def __exit__(self, kind, value, tb):
        global _passes
        if kind is None:
            fail("expected an error")
        else:
            _passes += 1
            print("  ok    raised:", value)
        return True

def summary():
    print()
    print(_passes, "passed,", _failures, "failed")
    if _failures:
        sys.exit(1)

def _close(a, b, tol=1e-9):
    if hasattr(a, "to_plain"):
        a = a.to_plain()
    if hasattr(b, "to_plain"):
        b = b.to_plain()
    if isinstance(a, float) or isinstance(b, float):
        return abs(a - b) <= tol
    if isinstance(a, dict) and isinstance(b, dict):
        return a.keys() == b.keys() and all(_close(a[k], b[k], tol) for k in a)
    if isinstance(a, (list, tuple)) and isinstance(b, (list, tuple)):
        return len(a) == len(b) and all(_close(x, y, tol) for x, y in zip(a, b))
    return a == b

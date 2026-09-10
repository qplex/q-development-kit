import math
import test_module
import constructor_module
from helpers import expect, expect_error, summary

try:
    import numpy
    have_numpy = True
except ImportError:
    have_numpy = False
    print("warning: numpy is not installed, so the numpy tests are skipped")

engine = test_module.TestEngine()

print()
print("SECTION A - GLOBAL VARIABLES")
print()

engine.global_int = 42
expect(engine.global_int, 42)
with expect_error():
    engine.global_int = 1.23

engine.global_real = 4.2
expect(engine.global_real, 4.2)

engine.global_boolean = True
expect(engine.global_boolean, True)

engine.global_int_array = [1, 2, 3]
expect(engine.global_int_array, [1, 2, 3])

engine.global_real_array = [1, 2.2, 3.3]
expect(engine.global_real_array, [1.0, 2.2, 3.3])

engine.global_boolean_array = [True, False, True]
expect(engine.global_boolean_array, [True, False, True])

engine.global_int_matrix = [[1, 2, 3], [11, 12]]
expect(engine.global_int_matrix, [[1, 2, 3], [11, 12]])

engine.global_real_matrix = [[1, 1.2, 3], [2.1, 2.2]]
expect(engine.global_real_matrix, [[1.0, 1.2, 3.0], [2.1, 2.2]])

engine.global_boolean_matrix = [[True, True, False], [False, True]]
expect(engine.global_boolean_matrix, [[True, True, False], [False, True]])

engine.global_simple_pmf = {1:0.1, 2:0.2, 3:0.3, 4:0.4}
expect(engine.global_simple_pmf, {1: 0.1, 2: 0.2, 3: 0.3, 4: 0.4})
with expect_error():
    engine.global_simple_pmf = {-1:1}
with expect_error():
    engine.global_simple_pmf = {}
with expect_error():
    engine.global_simple_pmf = {(1,1):0.1, (2,2):0.2, (3,3):0.3, (4,4):0.4}

engine.global_simple_pmf = {1:1}
expect(engine.global_simple_pmf, {1: 1.0})

engine.global_simple_pmf_array = [{1:0.1, 2:0.2, 3:0.3, 4:0.4},{1:0.4, 2:0.3, 3:0.2, 4:0.1}]
expect(engine.global_simple_pmf_array, [{1: 0.1, 2: 0.2, 3: 0.3, 4: 0.4}, {1: 0.4, 2: 0.3, 3: 0.2, 4: 0.1}])

engine.global_simple_pmf_matrix = [
    [{1:0.1, 2:0.2, 3:0.3, 4:0.4},{1:0.4, 2:0.3, 3:0.2, 4:0.1}],
    [{1:0.1, 2:0.2, 3:0.3, 4:0.4}]
]
expect(engine.global_simple_pmf_matrix, [[{1: 0.1, 2: 0.2, 3: 0.3, 4: 0.4}, {1: 0.4, 2: 0.3, 3: 0.2, 4: 0.1}], [{1: 0.1, 2: 0.2, 3: 0.3, 4: 0.4}]])

engine.global_joint_pmf = {(0,0,0):0.25, (0,0,1):0.125, (0,1,0):0.125, (1,0,0):0.25, (1,1,0):0.125, (1,1,1):0.125}
expect(engine.global_joint_pmf, {(0, 0, 0): 0.25, (0, 0, 1): 0.125, (0, 1, 0): 0.125, (1, 0, 0): 0.25, (1, 1, 0): 0.125, (1, 1, 1): 0.125})

engine.global_joint_pmf_array = [
    	{(0,0,0):0.1, (1,1,1):0.2, (2,1,2):0.3, (2,2,3):0.4},
    	{(0,0,0):0.4, (1,1,1):0.3, (2,1,2):0.2, (2,2,3):0.1}
    ]
expect(engine.global_joint_pmf_array, [{(0, 0, 0): 0.1, (1, 1, 1): 0.2, (2, 1, 2): 0.3, (2, 2, 3): 0.4}, {(0, 0, 0): 0.4, (1, 1, 1): 0.3, (2, 1, 2): 0.2, (2, 2, 3): 0.1}])

engine.global_joint_pmf_matrix = [
    [
      {(0,0,0):0.1, (1,1,1):0.2, (2,1,2):0.3, (2,2,3):0.4},
      {(0,0,0):0.4, (1,1,1):0.3, (2,1,2):0.2, (2,2,3):0.1}
    ], [
      {(0,0,0):0.1, (1,1,1):0.2, (2,1,2):0.3, (2,2,3):0.4}
    ]
]  
expect(engine.global_joint_pmf_matrix, [[{(0, 0, 0): 0.1, (1, 1, 1): 0.2, (2, 1, 2): 0.3, (2, 2, 3): 0.4}, {(0, 0, 0): 0.4, (1, 1, 1): 0.3, (2, 1, 2): 0.2, (2, 2, 3): 0.1}], [{(0, 0, 0): 0.1, (1, 1, 1): 0.2, (2, 1, 2): 0.3, (2, 2, 3): 0.4}]])

engine.global_compound_pmf = (
    {(0,0,0):0.1, (1,1,2):0.2, (2,1,3):0.3, (2,2,4):0.4}, 
    {1:0.1, 2:0.2, 3:0.3, 4:0.4}
  )
expect(engine.global_compound_pmf, ({(0, 0, 0): 0.1, (1, 1, 2): 0.2, (2, 1, 3): 0.3, (2, 2, 4): 0.4}, {1: 0.1, 2: 0.2, 3: 0.3, 4: 0.4}))

engine.global_compound_pmf_array = [
    (
      {(0,0,0):0.1, (1,1,2):0.2, (2,1,3):0.3, (2,2,4):0.4}, 
      {1:0.1, 2:0.2, 3:0.3, 4:0.4}
    ), (
      {(0,0,0):0.4, (1,1,2):0.3, (2,1,3):0.2, (2,2,4):0.1}, 
      {1:0.4, 2:0.3, 3:0.2, 4:0.1}
    )
  ]
expect(engine.global_compound_pmf_array, [({(0, 0, 0): 0.1, (1, 1, 2): 0.2, (2, 1, 3): 0.3, (2, 2, 4): 0.4}, {1: 0.1, 2: 0.2, 3: 0.3, 4: 0.4}), ({(0, 0, 0): 0.4, (1, 1, 2): 0.3, (2, 1, 3): 0.2, (2, 2, 4): 0.1}, {1: 0.4, 2: 0.3, 3: 0.2, 4: 0.1})])

engine.global_compound_pmf_matrix = [
     [
        (
          {(0,0,0):0.1, (1,1,2):0.2, (2,1,3):0.3, (2,2,4):0.4}, 
          {1:0.1, 2:0.2, 3:0.3, 4:0.4}
	), (
	  {(0,0,0):0.4, (1,1,2):0.3, (2,1,3):0.2, (2,2,4):0.1}, 
	  {1:0.4, 2:0.3, 3:0.2, 4:0.1}
	)
    ], [
	(
	  {(0,0,0):0.1, (1,1,2):0.2, (2,1,3):0.3, (2,2,4):0.4}, 
	  {1:0.1, 2:0.2, 3:0.3, 4:0.4}
	)
     ]
  ]
expect(engine.global_compound_pmf_matrix, [[({(0, 0, 0): 0.1, (1, 1, 2): 0.2, (2, 1, 3): 0.3, (2, 2, 4): 0.4}, {1: 0.1, 2: 0.2, 3: 0.3, 4: 0.4}), ({(0, 0, 0): 0.4, (1, 1, 2): 0.3, (2, 1, 3): 0.2, (2, 2, 4): 0.1}, {1: 0.4, 2: 0.3, 3: 0.2, 4: 0.1})], [({(0, 0, 0): 0.1, (1, 1, 2): 0.2, (2, 1, 3): 0.3, (2, 2, 4): 0.4}, {1: 0.1, 2: 0.2, 3: 0.3, 4: 0.4})]])

engine.global_interface = 'f_z1'
expect(engine.global_interface, 'f_z1')
with expect_error():
    engine.global_interface = 'none'

engine.global_interface_array = ['f_z1','f_z2']
expect(engine.global_interface_array, ['f_z1', 'f_z2'])

engine.global_interface_matrix = [['f_z1','f_z2'],['f_z1']]
expect(engine.global_interface_matrix, [['f_z1', 'f_z2'], ['f_z1']])

print()
print("SECTION B - INITIALIZERS")
print()

expect(engine.f_b1(), [1, 2, 3])
expect(engine.f_b2(), [1.1, 2.2, 3.3])
expect(engine.f_b3(), [True, False, True])
expect(engine.f_b4(), [[1, 2, 3], [4]])
expect(engine.f_b5(), [[1.1, 1.2, 1.3], [2.1, 2.2]])
expect(engine.f_b6(), [[True, False, True], [False, True]])
expect(engine.f_b7(), {1: 0.1, 2: 0.2, 3: 0.3, 4: 0.4})
expect(engine.f_b8(), {(0, 0): 0.1, (1, 1): 0.2, (2, 1): 0.3, (2, 2): 0.4})
expect(engine.f_b9(), ({(0, 0): 0.1, (1, 1): 0.2, (2, 1): 0.3, (2, 2): 0.4}, {1: 0.1, 2: 0.2, 3: 0.3, 4: 0.4}))
expect(engine.f_b10(), [{1: 0.1, 2: 0.2, 3: 0.3, 4: 0.4}, {1: 0.4, 2: 0.3, 3: 0.2, 4: 0.1}])
expect(engine.f_b11(), [{(0, 0): 0.1, (1, 1): 0.2, (2, 1): 0.3, (2, 2): 0.4}, {(0, 0): 0.4, (1, 1): 0.3, (2, 1): 0.2, (2, 2): 0.1}])
expect(engine.f_b12(), [({(0, 0): 0.1, (1, 1): 0.2, (2, 1): 0.3, (2, 2): 0.4}, {1: 0.1, 2: 0.2, 3: 0.3, 4: 0.4}), ({(0, 0): 0.4, (1, 1): 0.3, (2, 1): 0.2, (2, 2): 0.1}, {1: 0.4, 2: 0.3, 3: 0.2, 4: 0.1})])
expect(engine.f_b13(), [[{1: 0.1, 2: 0.2, 3: 0.3, 4: 0.4}, {1: 0.4, 2: 0.3, 3: 0.2, 4: 0.1}], [{1: 0.4, 2: 0.3, 3: 0.2, 4: 0.1}]])
expect(engine.f_b14(), [[{(0, 0): 0.1, (1, 1): 0.2, (2, 1): 0.3, (2, 2): 0.4}, {(0, 0): 0.4, (1, 1): 0.3, (2, 1): 0.2, (2, 2): 0.1}], [{(0, 0): 0.1, (1, 1): 0.2, (2, 1): 0.3, (2, 2): 0.4}]])
expect(engine.f_b15(), [[({(0, 0): 0.1, (1, 1): 0.2, (2, 1): 0.3, (2, 2): 0.4}, {1: 0.1, 2: 0.2, 3: 0.3, 4: 0.4}), ({(0, 0): 0.1, (1, 1): 0.2, (2, 1): 0.3, (2, 2): 0.4}, {1: 0.4, 2: 0.3, 3: 0.2, 4: 0.1})], [({(0, 0): 0.1, (1, 1): 0.2, (2, 1): 0.3, (2, 2): 0.4}, {1: 0.1, 2: 0.2, 3: 0.3, 4: 0.4})]])
expect(engine.f_b16(3), [0.0, 0.0, 0.0])
with expect_error():
    engine.f_b16(0)

expect(engine.f_b17(2, 3), [[0, 0, 0], [0, 0, 0]])
with expect_error():
    engine.f_b17(2, 0)

print()
print("SECTION C - FUNCTION CALLS AND BUILTINS")
print()

expect(engine.f_c1(.5), 0)
expect(engine.f_c2(.5), 1)
expect(engine.f_c3(2), 2)
expect(engine.f_c4(2), 3)
      
expect(engine.f_c5(42, True), 42)
expect(engine.f_c5(42, False), 0)

expect(engine.f_c6(42, True), 42)
expect(engine.f_c6(42, False), 0)

expect(engine.f_c7(42, True), 42)
expect(engine.f_c7(42, False), 0)

expect(engine.f_c8(42, True), 42)
expect(engine.f_c8(42, False), 0)

expect(engine.f_c9(), math.exp(1))
expect(engine.f_c10(), math.log(2.71828))
expect(engine.f_c11(), 1.414 ** 2)
expect(engine.f_c12(), math.sqrt(2))
expect(engine.f_c13(7, 3), 1)
expect(engine.f_c13(-7, 3), -1)
with expect_error():
    engine.f_c13(5, 0)
expect(engine.f_c14(5, 4, 3), 2)
expect(0 <= engine.f_c15() < 100, True)
expect(0.0 <= engine.f_c16() < 1.0, True)
expect(engine.f_c17(), {0: 0.1, 1: 0.2, 2: 0.3, 3: 0.4})
expect(engine.f_c18(), {0: 0.1, 1: 0.6, 2: 0.3})
expect(engine.f_c19(), {(1, 1, 0): 2/15, (1, 0, 1): 3/15, (0, 2, 0): 1/15, (0, 1, 1): 6/15, (0, 0, 2): 3/15})
expect(engine.f_c20(), {0: 0.1, 1: 0.9})
expect(engine.f_c21(), {0: 0.01, 1: 0.18, 2: 0.81})
expect(engine.f_c22(), {(2, 0, 0, 0): 0.01, (0, 2, 0, 0): 0.04, (0, 0, 2, 0): 0.09, (0, 0, 0, 2): 0.16, (1, 1, 0, 0): 0.04, (1, 0, 1, 0): 0.06, (1, 0, 0, 1): 0.08, (0, 1, 1, 0): 0.12, (0, 1, 0, 1): 0.16, (0, 0, 1, 1): 0.24})
expect(engine.f_c23(), {(0, 0): 0.05, (0, 1): 0.1, (0, 2): 0.15, (0, 3): 0.2, (1, 0): 0.25, (1, 1): 0.25})

print()
print("SECTION D - INDEXING")
print()

expect(engine.f_d1(), 1230)
expect(engine.f_d2(), 1234)
expect(engine.f_d3(), 2.5)
expect(engine.f_d4(), True)
expect(engine.f_d5(), 567)
expect(engine.f_d6(), 89)
expect(engine.f_d7(), 2.5)
expect(engine.f_d8(), True)
expect(engine.f_d9(), {3: 0.25, 7: 0.75})
expect(engine.f_d10(), {3: 0.25, 7: 0.75})
expect(engine.f_d11(), {(0, 0): 0.5, (1, 1): 0.5})
expect(engine.f_d12(5, True), 5)
expect(engine.f_d12(5, False), 0)
expect(engine.f_d13(5, True), 5)

print()
print("SECTION E - SAMPLING")
print()

engine.p = {1:0.1, 2:0.2, 3:0.3, 4:0.4}
engine.q = {4:0.1, 3:0.2, 2:0.3, 1:0.4}
engine.mu = {(1,1):0.1, (2,1):0.2, (3,1):0.3, (3,2):0.4}

expect(engine.f_e1(3, 0.5), {0: 0.125, 1: 0.375, 2: 0.375, 3: 0.125})
expect(engine.f_e2(), {0: 0.6561/0.7084, 2: 0.0486/0.7084, 3: 0.0036/0.7084, 4: 0.0001/0.7084})
expect(engine.f_e3(), {2: 0.04, 3: 0.11, 4: 0.20, 5: 0.30, 6: 0.20, 7: 0.11, 8: 0.04})
expect(engine.f_e4(), {(1, 1): 0.04, (1, 2): 0.03, (1, 3): 0.02, (1, 4): 0.01, (2, 1): 0.08, (2, 2): 0.06, (2, 3): 0.04, (2, 4): 0.02, (3, 1): 0.12, (3, 2): 0.09, (3, 3): 0.06, (3, 4): 0.03, (4, 1): 0.16, (4, 2): 0.12, (4, 3): 0.08, (4, 4): 0.04})
expect(engine.f_e5(), {1: 0.1, 2: 0.2, 3: 0.7})
expect(engine.f_e6(), {1: 0.6, 2: 0.4})
engine.f_e7(3, 1)
expect(engine.global_real, 0.3 / 0.7)
expect(engine.f_e8(0.9), {0: 0.19, 1: 0.81})
expect(engine.f_e9({0: 0.2, 1: 0.3, 2: 0.5}, True), {1: 0.375, 2: 0.625})
expect(engine.f_e9({0: 0.2, 1: 0.3, 2: 0.5}, False), {1: 0.375, 2: 0.625})

print()
print("SECTION F - PMF CONFIGURATIONS")
print()

expect(engine.f_f1(), {(1, 2): 0.1, (2, 3): 0.2, (3, 4): 0.3, (4, 5): 0.4})
expect(engine.f_f2(), {(1, 2, 3): 0.1, (2, 3, 4): 0.2, (3, 4, 5): 0.3, (4, 5, 6): 0.4})
expect(engine.f_f3(), ({(1, 2): 0.1, (2, 3): 0.2, (3, 4): 0.3, (4, 5): 0.4}, {3: 0.1, 4: 0.2, 5: 0.3, 6: 0.4}))

print()
print("SECTION G - TOKENS AND ATTRIBUTES")
print()

expect(engine.XYZ, 42)
expect(engine.f_g1(), 42)
expect(engine.f_g2(), 3)
expect(engine.f_g3(), 3)
expect(engine.f_g4(), 0)
expect(engine.f_g5(), 3)

print()
print("SECTION H - PMF FUNCTIONS")
print()

expect(engine.f_h1(), 2)
expect(engine.f_h2(), 4)
expect(engine.f_h3(), True)
print()

print()
print("SECTION I - PMF EXTRACTION")
print()

engine.global_joint_pmf = {(0, 0, 0): 0.25, (0, 0, 1): 0.125, (0, 1, 0): 0.125, (1, 0, 0): 0.25, (1, 1, 0): 0.125, (1, 1, 1): 0.125}
engine.global_quad_pmf = {(1, 2, 3, 4): 0.3, (2, 3, 4, 5): 0.7}
engine.global_compound_big_pmf = (
    {(1, 2, 3, 4): 0.3, (2, 3, 4, 5): 0.7},
    {(10, 20): 0.5, (30, 40): 0.5}
)
engine.global_compound_pmf = (
    {(0, 0, 0): 0.1, (1, 1, 2): 0.2, (2, 1, 3): 0.3, (2, 2, 4): 0.4},
    {1: 0.1, 2: 0.2, 3: 0.3, 4: 0.4}
)

expect(engine.f_i1(), {0: 0.5, 1: 0.5})
expect(engine.f_i2(), {(0, 0): 0.375, (0, 1): 0.125, (1, 0): 0.25, (1, 1): 0.25})
expect(engine.f_i3(), {(0, 0, 0): 0.25, (0, 0, 1): 0.125, (0, 1, 0): 0.125, (1, 0, 0): 0.25, (1, 1, 0): 0.125, (1, 1, 1): 0.125})
expect(engine.f_i4(0), {0: 0.75, 1: 0.25})
expect(engine.f_i4(1), {0: 0.5, 1: 0.5})
expect(engine.f_i5(1), {(0, 0): 0.5, (1, 0): 0.25, (1, 1): 0.25})
expect(engine.f_i6(), {(1, 2): 0.3, (2, 3): 0.7})
expect(engine.f_i7(), {(1, 2, 3): 0.3, (2, 3, 4): 0.7})
expect(engine.f_i8(1), {(2, 3): 1.0})
expect(engine.f_i9(1, 2), {3: 1.0})
expect(engine.f_i10(), {(0, 0): 0.1, (1, 1): 0.2, (2, 1): 0.3, (2, 2): 0.4})
expect(engine.f_i11(), {0: 0.1, 1: 0.2, 2: 0.7})
expect(engine.f_i12(1), {1: 1.0})
expect(engine.f_i13(1), {(1, 2): 1.0})
expect(engine.f_i14(1, 1), {2: 1.0})
expect(engine.f_i15(1), {(2, 3): 1.0})
expect(engine.f_i16(), {(1, 2): 0.3, (2, 3): 0.7})
expect(engine.f_i17(), 2)
expect(engine.f_i18(), 0)

print()
print("SECTION J - DATA OBJECTS")
print()

simple_pmf = test_module.pmf({0: 0.5, 2: 0.5})
expect(simple_pmf, {0: 0.5, 2: 0.5})
expect(str(simple_pmf), "{0: 0.5, 2: 0.5}")
expect(repr(simple_pmf), "test_module.pmf({0: 0.5, 2: 0.5})")
expect([o for o in simple_pmf], [0, 2])
expect(simple_pmf[1], 0.0)
expect(simple_pmf.to_plain(), {0: 0.5, 2: 0.5})
expect(simple_pmf.qualifier_shape, '{?}')
expect(engine.f_j1(simple_pmf), {1: 0.5, 3: 0.5})
expect(engine.f_j2(simple_pmf), {(0, 1): 0.5, (2, 3): 0.5})

joint_pmf = test_module.pmf({(0, 0): 0.5, (1, 1): 0.5})
expect(joint_pmf, {(0, 0): 0.5, (1, 1): 0.5})
expect(joint_pmf[(1, 0)], 0.0)
expect(len(joint_pmf), 2)
expect(joint_pmf.qualifier_shape, "{?,?}")

int_array = test_module.int_array([1, 2, 3])
expect(int_array, [1, 2, 3])
expect(str(int_array), "[1, 2, 3]")
expect(repr(int_array), "test_module.int_array([1, 2, 3])")
expect(int_array[-1], 3)
expect(int_array[1:], [2, 3])
expect(type(int_array[1:]) is test_module.int_array, True)
expect(int_array.to_plain(), [1, 2, 3])
expect(engine.f_j3(int_array), [1, 2, 3])

real_matrix = test_module.real_matrix([[1.0, 2.0], [3.0, 4.0]])
expect(real_matrix, [[1.0, 2.0], [3.0, 4.0]])
expect(real_matrix[0], [1.0, 2.0])
expect(real_matrix == [[1.0, 2.0], [3.0, 4.0]], True)
expect(real_matrix == test_module.real_matrix([[1.0, 2.0], [3.0, 4.0]]), True)
expect(real_matrix != [[1.0, 2.0], [3.0, 5.0]], True)
rows = list(real_matrix)
expect([type(r).__name__ for r in rows], ['real_array', 'real_array'])
expect([r == plain for r, plain in zip(rows, [[1.0, 2.0], [3.0, 4.0]])], [True, True])

simple_pmf_array = test_module.pmf_array([{0: 0.5, 1: 0.5}, {2: 1.0}])
expect(simple_pmf_array, [{0: 0.5, 1: 0.5}, {2: 1.0}])
expect(simple_pmf_array[0], {0: 0.5, 1: 0.5})

compound_pmf = test_module.pmf(({(0, 0): 1.0}, {5: 1.0}, {9: 1.0}))
expect(compound_pmf, ({(0, 0): 1.0}, {5: 1.0}, {9: 1.0}))
expect(compound_pmf[0], {(0, 0): 1.0})
expect(compound_pmf[-1], {9: 1.0})
expect(compound_pmf[0:2], ({(0, 0): 1.0}, {5: 1.0}))
expect(type(compound_pmf[0:2]) is test_module.pmf, True)
expect(compound_pmf[0:1], {(0, 0): 1.0})
expect(compound_pmf[::2], ({(0, 0): 1.0}, {9: 1.0}))
expect(len(compound_pmf), 3)
expect(compound_pmf.qualifier_shape, "{(?,?),(?),(?)}")

engine.f_j4(({(0, 0): 0.5, (1, 1): 0.5}, {0: 0.5, 1: 0.5}))
expect(engine.global_overlap_pmf, ({(0, 0): 0.5, (1, 1): 0.5}, {0: 0.5, 1: 0.5}))

foreign_pmf = constructor_module.pmf({0: 0.25, 1: 0.75})
expect(engine.f_j1(foreign_pmf), {1: 0.25, 2: 0.75})
engine.global_simple_pmf = constructor_module.pmf({5: 1.0})
expect(engine.global_simple_pmf, {5: 1.0})

if have_numpy:
    expect(numpy.array(test_module.int_array([1, 2, 3])).tolist(), [1, 2, 3])
    expect(numpy.array(test_module.real_array([1.5, 2.5])).tolist(), [1.5, 2.5])
    expect(numpy.array(test_module.real_matrix([[1.0, 2.0], [3.0, 4.0]])).tolist(), [[1.0, 2.0], [3.0, 4.0]])

with expect_error():
    test_module.pmf({})
with expect_error():
    test_module.pmf({(): 1.0})
with expect_error():
    test_module.pmf(({0: 1.0},))
with expect_error():
    test_module.int_array([1, "x"])
with expect_error():
    simple_pmf[0] = 0.1
with expect_error():
    real_matrix < real_matrix
with expect_error():
    list(compound_pmf)
with expect_error():
    engine.f_j4(({(0, 0): 0.5, (1, 1): 0.5}, {0: 0.9, 1: 0.1}))
with expect_error():
    engine.f_j1(constructor_module.int_array([1, 2, 3]))

print()
print("SECTION K - SIDE EFFECTS")
print()

expect(engine.f_k1(), 12)
expect(engine.f_k2(), 210)

print()
print("SECTION L - CONTROL FLOW")
print()

expect(engine.f_l1(4), 10)
expect(engine.f_l1(0), 0)
expect(engine.f_l2(4), 24)
expect(engine.f_l2(0), 1)
expect(engine.f_l3(5), 1)
expect(engine.f_l3(-3), -1)
expect(engine.f_l3(0), 0)
expect(engine.f_l4(3), 36)

print()
print("SECTION M - CONSTRUCTOR")
print()

constructor_engine = constructor_module.ConstructorEngine(0.25)
expect(constructor_engine.p0, 0.25)
expect(constructor_engine.ra, [0.25, 0.75])
expect(constructor_engine.rm, [[0.25, 0.75], [0.25]])
expect(constructor_engine.dist, {0: 0.75, 1: 0.25})
expect(constructor_engine.pa, [{0: 0.75, 1: 0.25}, {0: 0.25, 1: 0.75}])
expect(constructor_engine.pm, [[{0: 0.75, 1: 0.25}], [{0: 0.25, 1: 0.75}]])

constructor_engine2 = constructor_module.ConstructorEngine(0.75)
expect(constructor_engine2.p0, 0.75)
expect(constructor_engine.p0, 0.25)

with expect_error():
    constructor_module.ConstructorEngine(1.5)
with expect_error():
    constructor_module.ConstructorEngine()

print()
print("MEMORY USE")
print()
print(engine.current_memory_use)
print(engine.peak_memory_use)

print()
print("DONE")


summary()

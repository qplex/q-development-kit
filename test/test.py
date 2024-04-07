import testengine
engine = testengine.TestEngine()

print()
print("SECTION A - Global variables");
print()

engine.global_int = 42
print(engine.global_int)

engine.global_real = 4.2
print(engine.global_real)

engine.global_boolean = True
print(engine.global_boolean)

engine.global_int_array = [1, 2, 3]
print(engine.global_int_array)

engine.global_real_array = [1, 2.2, 3.3]
print(engine.global_real_array)

engine.global_boolean_array = [True, False, True]
print(engine.global_boolean_array)

engine.global_int_matrix = [[1, 2, 3], [11, 12]]
print(engine.global_int_matrix)

engine.global_real_matrix = [[1, 1.2, 3], [2.1, 2.2]]
print(engine.global_real_matrix)

engine.global_boolean_matrix = [[True, True, False], [False, True]]
print(engine.global_boolean_matrix)

engine.global_simple_pmf = {1:0.1, 2:0.2, 3:0.3, 4:0.4}
print(engine.global_simple_pmf)

engine.global_simple_pmf = {1:1}
print(engine.global_simple_pmf)

engine.global_simple_pmf_array = [{1:0.1, 2:0.2, 3:0.3, 4:0.4},{1:0.4, 2:0.3, 3:0.2, 4:0.1}]
print(engine.global_simple_pmf_array)

engine.global_simple_pmf_matrix = [
    [{1:0.1, 2:0.2, 3:0.3, 4:0.4},{1:0.4, 2:0.3, 3:0.2, 4:0.1}],
    [{1:0.1, 2:0.2, 3:0.3, 4:0.4}]
]
print(engine.global_simple_pmf_matrix)

engine.global_joint_pmf = {(1,11):0.1, (2,22):0.2, (3,22):0.3, (4,44):0.4}
print(engine.global_joint_pmf)

engine.global_joint_pmf_array = [
    	{(0,0):0.1, (1,1):0.2, (2,1):0.3, (2,2):0.4},
    	{(0,0):0.4, (1,1):0.3, (2,1):0.2, (2,2):0.1}
    ]
print(engine.global_joint_pmf_array)

engine.global_joint_pmf_matrix = [
    [
      {(0,0):0.1, (1,1):0.2, (2,1):0.3, (2,2):0.4},
      {(0,0):0.4, (1,1):0.3, (2,1):0.2, (2,2):0.1}
    ], [
      {(0,0):0.1, (1,1):0.2, (2,1):0.3, (2,2):0.4}
    ]
]  
print(engine.global_joint_pmf_matrix)

engine.global_compound_pmf = (
    {(0,0):0.1, (1,1):0.2, (2,1):0.3, (2,2):0.4}, 
    {1:0.1, 2:0.2, 3:0.3, 4:0.4}
  )
print(engine.global_compound_pmf)

engine.global_compound_pmf_array = [
    (
      {(0,0):0.1, (1,1):0.2, (2,1):0.3, (2,2):0.4}, 
      {1:0.1, 2:0.2, 3:0.3, 4:0.4}
    ), (
      {(0,0):0.4, (1,1):0.3, (2,1):0.2, (2,2):0.1}, 
      {1:0.4, 2:0.3, 3:0.2, 4:0.1}
    )
  ]
print(engine.global_compound_pmf_array)

engine.global_compound_pmf_matrix = [
     [
        (
          {(0,0):0.1, (1,1):0.2, (2,1):0.3, (2,2):0.4}, 
          {1:0.1, 2:0.2, 3:0.3, 4:0.4}
	), (
	  {(0,0):0.4, (1,1):0.3, (2,1):0.2, (2,2):0.1}, 
	  {1:0.4, 2:0.3, 3:0.2, 4:0.1}
	)
    ], [
	(
	  {(0,0):0.1, (1,1):0.2, (2,1):0.3, (2,2):0.4}, 
	  {1:0.1, 2:0.2, 3:0.3, 4:0.4}
	)
     ]
  ]
print(engine.global_compound_pmf_matrix)

engine.global_interface = 'f_z1'
print(engine.global_interface)

engine.global_interface_array = ['f_z1','f_z2']
print(engine.global_interface_array)

engine.global_interface_matrix = [['f_z1','f_z2'],['f_z1']]
print(engine.global_interface_matrix)

print()
print("Section B - Initializers");
print()

print(engine.f_b1())
print(engine.f_b2())
print(engine.f_b3())
print(engine.f_b4())
print(engine.f_b5())
print(engine.f_b6())
print(engine.f_b7())
print(engine.f_b8())
print(engine.f_b9())
print(engine.f_b10())
print(engine.f_b11())
print(engine.f_b12())
print(engine.f_b13())
print(engine.f_b14())
print(engine.f_b15())
engine.f_b16()
print(engine.global_interface)
print(engine.global_interface_array)
print(engine.global_interface_matrix)

engine.f_b17()

print()
print("SECTION C - Function calls and initializers");
print()

print(engine.f_c1(.5))
print(engine.f_c2(.5))
print(engine.f_c3(2))
print(engine.f_c4(2))
      
print(engine.f_c5(42, True))
print(engine.f_c5(42, False))

print(engine.f_c6(42, True))
print(engine.f_c6(42, False))

print(engine.f_c7(42, True))
print(engine.f_c7(42, False))

print(engine.f_c8(42, True))
print(engine.f_c8(42, False))

print(engine.f_c9())
print(engine.f_c10())
print(engine.f_c11())
print(engine.f_c12())
print(engine.f_c15())
print(engine.f_c16())
print(engine.f_c17())
print(engine.f_c18())
print(engine.f_c19())
print(engine.f_c20())
print(engine.f_c21())
print(engine.f_c22())

print()
print("SECTION D - SAMPLING")
print()

engine.p = {1:0.1, 2:0.2, 3:0.3, 4:0.4}
engine.q = {4:0.1, 3:0.2, 2:0.3, 1:0.4}
engine.mu = {(1,1):0.1, (2,1):0.2, (3,1):0.3, (3,2):0.4}

print(engine.f_d1(3, 0.5))
print(engine.f_d2())
print(engine.f_d3())
print(engine.f_d4())
print(engine.f_d5())
print(engine.f_d6())
engine.f_d7(1,1)

print()
print("SECTION E - PMF CONFIGURATIONS")
print()

print(engine.f_e1())
print(engine.f_e2())
print(engine.f_e3())

print()
print("SECTION F - TOKENS AND ATTRIBUTES")
print()

print(engine.XYZ)
print(engine.f_f1())
print(engine.f_f2())
print(engine.f_f3())
print(engine.f_f4())

print()
print("SECTION G - PMF FUNCTIONS")
print(engine.f_g1())
print(engine.f_g2())
print(engine.f_g3())
print()

print()
print("SECTION H - RUNTIME ERRORS")

try:
    engine.global_int = 1.23
except Exception as e:
    print(e) 

try:
    engine.global_simple_pmf = {-1:1}
except Exception as e:
    print(e) 

try:
    engine.global_simple_pmf = {}
except Exception as e:
    print(e) 

try:
    engine.global_simple_pmf = {(1,1):0.1, (2,2):0.2, (3,3):0.3, (4,4):0.4}
except Exception as e:
    print(e) 

try:
    engine.global_interface = 'none'
except Exception as e:
    print(e) 

print()
print("MEMORY USE")
print()
print(engine.current_memory_use)
print(engine.peak_memory_use)

print()
print("DONE");


# Q Development Kit (QDK)

The Q language is used to specify calculations for
discrete probability distributions and execute them from Python.
[QPLEX](https://qplex.org/) uses such calculations for 
modeling and analysis of a variety of stochastic systems. 

This animation is constructed from probability distributions calculated with Q code:

<p align="center">
<img src="https://qplex.org/assets/images/pmfs_over_time.gif" height="300">
</p>

It represents the distributions of the number of entities
in a system, over time, for a 
multiserver queueing system with arrival distributions
that fluctuate heavily.

The QDK contains a Q-language compiler and documentation.
The compiler produces a C++ extension module for Python from one 
or more Q source files.
We used the QDK to create
the [QPLEX Python Package](https://pypi.org/project/qplex), which
incorporates several standard models. 
You can use it to create your own.

The Q language enables you to write what the [QPLEX book](https://qplex.org/book) calls *distributional programs*.
A distributional program resembles a stochastic simulation, 
but its output is *deterministic* and represents the probability distribution of a sample produced by this simulation.

Here is an example of Q code:

    public Pmf truncationMove() {
        x ~ binomial(2, 0.2);
        int y = min(x, 1);
        return y;
    }

This function produces a distribution that assigns probability 0.64 
to the value 0 and probability 0.36 to the value 1.

## Dependencies

Building a C++ extension module from Q source using the QDK requires Java (JRE 1.8 or later),
a C++ compiler, Python 3, and the Python package setuptools.

Building the QDK itself from source additionally requires Java (JDK 1.8 or later),
Ant, a TeX distribution (e.g., TeX Live, MiKTeX, or MacTeX), and the Python package sphinx.

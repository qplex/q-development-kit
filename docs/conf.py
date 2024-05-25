# Configuration file for the Sphinx documentation builder.
#
# For the full list of built-in configuration values, see the documentation:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

# -- Project information -----------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#project-information

project = 'Q Developer Guide'
copyright = ''
author = 'QPLEX Team'
version = '@VERSION@'
release = '@VERSION@'

root_doc = 'guide'

# -- General configuration ---------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#general-configuration

smartquotes = False

# -- Options for HTML output -------------------------------------------------
# https://www.sphinx-doc.org/en/master/usage/configuration.html#options-for-html-output

html_theme = 'sphinx_rtd_theme'
html_static_path = ['_static']
html_show_sourcelink = False

# -- Options for PDF output -------------------------------------------------

latex_engine = 'xelatex'
latex_toplevel_sectioning = 'section'
latex_elements = {
'makeindex': '',
'printindex': '',
'preamble': r'\counterwithout{section}{chapter} \setcounter{tocdepth}{2} \setcounter{secnumdepth}{3}',
} 


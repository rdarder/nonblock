from setuptools import setup, find_packages
setup(
    name = "gen_election",
    version = "0.2",
    packages = find_packages(),
    #scripts = ['gen_geo.py'],
    entry_points = {
      'console_scripts': [
          'gen_geo = nonblock.gen_election.gen_geo:main'
      ]
    },

    # Project uses reStructuredText, so ensure that the docutils get
    # installed or upgraded on the target machine
    install_requires = ['docutils', 'sqlalchemy'],

    package_data = {
        # If any package contains *.txt or *.rst files, include them:
        'nonblock': ['data/2011-prim-conc-santafe.csv']

        # And include any *.msg files found in the 'hello' package, too:
    },
    include_package_data = True,

    # metadata for upload to PyPI
    author = "rafael.darder",
    author_email = "rafael.darder@globant.com",
    description = "Utilities to generate election base data",
    namespace_packages = ['nonblock']
)

from setuptools import setup, find_packages
setup(
    name = "gen_election",
    version = "0.2",
    packages = find_packages(),
    #scripts = ['gen_geo.py'],
    entry_points = {
      'console_scripts': [
          'random_election = nonblock.gen_election.random_election:main',
          'election_to_db = nonblock.gen_election.db:main',
          #'gen_geo = nonblock.gen_election.gen_geo:main',
      ]
    },

    # Project uses reStructuredText, so ensure that the docutils get
    # installed or upgraded on the target machine
    install_requires = ['docutils', 'sqlalchemy', 'pyyaml', 'oursql'],

    package_data = {
        # If any package contains *.txt or *.rst files, include them:
        'nonblock.gen_election': ['data/people_names.yml',
                                  'data/places_names.yml']

        # And include any *.msg files found in the 'hello' package, too:
    },
    include_package_data = True,

    # metadata for upload to PyPI
    author = "rafael.darder",
    author_email = "rafael.darder@globant.com",
    description = "Utilities to generate election base data",
    namespace_packages = ['nonblock']
)

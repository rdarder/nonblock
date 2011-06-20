from setuptools import setup, find_packages
setup(
    name = "election_utils",
    version = "0.3",
    packages = find_packages(),
    entry_points = {
      'console_scripts': [
          'random_election = nonblock.gen_election.random_election:main',
          'election_to_db = nonblock.gen_election.election_to_db:main',
          'votes_generator= nonblock.vote_poster.generator:main',
      ]
    },

    install_requires = ['docutils', 'sqlalchemy', 'pyyaml', 'oursql',
                        'gevent'],

    package_data = {
        'nonblock.gen_election': ['data/people_names.yml',
                                  'data/places_names.yml',
                                  'data/sample_election_spec.yml',
                                  'data/sample_election.yml']
    },

    include_package_data = True,

    author = "rafael.darder",
    author_email = "rafael.darder@globant.com",
    description = "Election database related utilities",
    namespace_packages = ['nonblock']
)

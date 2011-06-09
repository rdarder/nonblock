from setuptools import setup, find_packages
setup(
    name = "vote_poster",
    version = "0.1",
    packages = find_packages(),
    entry_points = {
      'console_scripts': [
          'vote_sequencer = nonblock.vote_poster.sequencer:main',
          'vote_sequencer = nonblock.vote_poster.poster:main',
      ]
    },

    install_requires = ['docutils', 'sqlalchemy', 'pyyaml', 'oursql'],

    package_data = {},
    include_package_data = True,

    author = "rafael.darder",
    author_email = "rafael.darder@globant.com",
    description = "Utilities to generate fake votes in a fake election",
    namespace_packages = ['nonblock']
)

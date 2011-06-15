from setuptools import setup, find_packages
setup(
    name = "election_webserver",
    version = "0.1",
    packages = find_packages(),
    entry_points = {
      'console_scripts': [
          'gevent_server = nonblock.webserver.server:main',
      ]
    },

    install_requires = ['docutils', 'sqlalchemy', 'pyyaml', 'oursql',
                        'gevent'],#==0.13.6', 'gunicorn'],

    package_data = {
        'nonblock.webserver ': ['static/*']
    },

    include_package_data = True,

    author = "rafael.darder",
    author_email = "rafael.darder@globant.com",
    description = "Election database web server",
    namespace_packages = ['nonblock']
)

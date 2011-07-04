#!/usr/bin/env bash

#wget -O - localhost:8080/submit --post-data '{"Name": "submitvotes", "Id" : "1", "Ref": "", "Data": {"Mesa": "Mesa 10776", "Votos": [ {"Puesto": "gobernador", "Candidato": "Barceló, Covadonga", "Partido": "", "Cantidad": 985} ] } }'
wget -O - localhost:8080/submit --post-data '{"Name": "submitvotes", "Id" : "1", "Ref": "", "Data": {"Mesa": "Mesa 10802", "Votos": [ {"Puesto": "gobernador", "Candidato": "Barceló, Covadonga", "Partido": "", "Cantidad": 985} ] } }'

# vim: set sw=2 sts=2 : #

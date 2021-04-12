# -*- coding: utf-8 -*-
from cx_Freeze import setup, Executable

options = {
    'build_exe': {
        'include_files': ['config.ini', 'script_values.csv'],
    }
}

executables = [
    Executable(
        script="grafana_get_graph.py",
        base="Console",
    )
]

setup(
    name="Grafana Get Graph",
    version="1.0",
    description="Utility for downloading graphs from Grafana Frontend",
    options=options,
    executables=executables,
)

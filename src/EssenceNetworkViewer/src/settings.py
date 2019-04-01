# FileName: settings.py
# Author:   Daniel N. Gisolfi
# Purpose:  To hold the defualt and current settings of the Essence Network Map
# Date:     2018.7.5

# defualt settings
Theme = 'default'
version = '1.00'
app_port = 5000
graphData = 'graphData.json'

# Defualt Peer lives on Port 9090
# TODO: allow the form on the settings page to change these
default_peer = 9090
ipAddress = 'http://127.0.0.1'

themeLinks = {
        'default': 'https://stackpath.bootstrapcdn.com/bootswatch/4.1.1/flatly/bootstrap.min.css',
        'dark' : 'https://stackpath.bootstrapcdn.com/bootswatch/4.1.1/slate/bootstrap.min.css'
}


def getMode(Theme):
    if Theme == 'dark':
        return 'Light Mode'
    else:
        return 'Dark Mode'

def getEditor(Theme):
    if Theme == 'dark':
        return 'terminal'
    else:
        return 'katzenmilch'

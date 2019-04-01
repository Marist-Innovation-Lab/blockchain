#!/usr/bin/python3

# Author:   Daniel N. Gisolfi
# Date:     2018.7.2

map_host = '0.0.0.0'

from flask import Flask, render_template, redirect, url_for, request
import essence, settings
app = Flask(__name__)

@app.route('/', methods=['GET'])
def index():
    return redirect(url_for('EssenceMap'))

@app.route('/EssenceMap', methods=['GET', 'POST'])
def EssenceMap():
    method = ''
    apiRequest = ''
    Graph= ''
    if request.method == 'POST':
        if request.form['requestGET']:
            method = 'GET'
            apiRequest = request.form['getCMD']
        elif request.form['requestPOST']:
            method = 'POST'
            apiRequest = request.form['postCMD']


    return render_template(
        'EssenceMap.html',
        theme=settings.themeLinks[settings.Theme],
        editorTheme=settings.getEditor(settings.Theme),
        peer=settings.default_peer,
        mapStatus=essence.updateMap(settings.default_peer),
        apiCall=essence.callEssenceAPI(method, apiRequest, settings.default_peer),
        graph=Graph
    )


@app.route('/Settings', methods=['GET', 'POST'])
def Settings():
    if request.method == 'POST':
        if request.form['theme']:
            if settings.Theme == 'default':
                settings.Theme = 'dark'
            elif settings.Theme == 'dark':
                settings.Theme = 'default'
    return render_template('Settings.html', theme=settings.themeLinks[settings.Theme], mode=settings.getMode(settings.Theme))

if __name__ == "__main__":
    app.run(host=map_host, port=settings.app_port, debug=True, threaded=True)

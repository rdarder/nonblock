package main

import (
  "template"
  "http"
  "log"
)

var _webclientTemplate *template.Template = nil

/* provide a default lightweight viewer */
func serveWebClient(rw http.ResponseWriter, req *http.Request) {
  if _webclientTemplate == nil {
    _webclientTemplate = template.New(nil)
    _webclientTemplate.SetDelims("<<", ">>")
    if err := _webclientTemplate.Parse(clienthtml); err != nil {
      log.Fatal("Template Parse error: ", err)
      panic("Does this get here?")
    }
  }
  _webclientTemplate.Execute(rw, req.Host)
  log.Println("Served lightweight web client.")
}

/* provide a way to serve local js files */
func serveJSFile(rw http.ResponseWriter, req *http.Request) {
  http.ServeFile(rw, req, req.URL.Path[1:])
  log.Println("Served js file: " + req.URL.Path[1:])
}


/* the lightweight client html template */
const clienthtml = `
<html>
<head>
<title>Json Dump</title>
<script type="text/javascript" src="js/jquery.min.js"></script>
<script type="text/javascript">
    $(function() {

    var conn;
    var puesto = $("#puesto");
    var nivel = $("#nivel");
    var alcance = $("#alcance");
    var lugar = $("#lugar");
    var log = $("#log");

    function appendLog(msg) {
        var d = log[0]
        var doScroll = d.scrollTop == d.scrollHeight - d.clientHeight;
        msg.appendTo(log)
        if (doScroll) {
            d.scrollTop = d.scrollHeight - d.clientHeight;
        }
    }

    $("#form").submit(function() {
        if (!conn) {
            return false;
        }

        var message = {
          "Name": "suscribe",
          "Id": "1",
          "Ref": "0",
          "Data": {
            "Puesto": puesto.val(),
            "Nivel": nivel.val(),
            "Alcance": alcance.val(),
            "Lugar": lugar.val()
          }
        };

        conn.send(JSON.stringify(message));
        puesto.val("");
        nivel.val("");
        alcance.val("");
        lugar.val("");
        return false
    });

    if (window["WebSocket"]) {
        conn = new WebSocket("ws://<<@>>/ws");
        conn.onclose = function(evt) {
            appendLog($("<div><b>Connection closed.</b></div>"))
        }
        conn.onmessage = function(evt) {
            appendLog($("<div/>").text(evt.data))
        }
    } else {
        appendLog($("<div><b>Your browser does not support WebSockets.</b></div>"))
    }
    });
</script>
</head>
<body>
<form id="form">
  <table>
    <tr>
    <td>Puesto</td><td>Nivel</td><td>Alcance</td><td>Lugar</td>
    </tr><tr>
    <td><input type="text" id="puesto" size="16"/></td>
    <td><input type="text" id="nivel" size="16"/></td>
    <td><input type="text" id="alcance" size="16"/></td>
    <td><input type="text" id="lugar" size="16"/></td>
    <td><input type="submit" value="Send"'/></td>
    </tr>
  </table>
</form>
<div id="log"></div>
</body>
</html> `

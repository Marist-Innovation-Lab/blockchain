package edu.marist.jointstudy.essence.api.server;

import com.google.gson.Gson;
import edu.marist.jointstudy.essence.Util;
import fi.iki.elonen.NanoHTTPD;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

/**
 * An improvement on the bad NanoHTTPD framework... introduces easy routing and some defaults.
 */
public abstract class Server extends NanoHTTPD {

    private static final Logger LOG = Logger.getLogger(Server.class.getName());

    /** How to serialize the objects served by the server into JSON. */
    protected Gson gson;

    public Server(int port, Gson gson) {
        super(port);
        this.gson = gson;
        // basic gets
        get("/date", (s, ids) -> newApiResponse(dateJSON()));
        get("/time", (s, ids) -> newApiResponse(timeJSON()));
        get("/datetime", (s, ids) -> newApiResponse(dateTimeJSON()));
        get("/version", (s, ids) -> newApiResponse(versionJSON()));
        get("/ver", (s, ids) -> newApiResponse(versionJSON()));

        // Displays an interactive HTML page for playing with the api
        get("/home", (s, ids) -> newFixedLengthResponse(homeHTML(s, "Home")));
    }

    /**
     * Called every time a request is sent to the server. Deals with routing and whatnot.
     * @param session really should be called request, but whatever.
     * @return an appropriate HTTP response based on the request.
     */
    @Override
    public Response serve(IHTTPSession session) {
        // Grab some session/header/parameter data.
        String fromIP = session.getHeaders().get("remote-addr");

        // We'll begin by parsing the uri into one or more pieces.
        String uri = String.valueOf(session.getUri());

        // Echo the command to the API console.
        System.out.println("API received command: " + uri + " from " + fromIP);

        String[] path = parsePathFrom(uri);
        System.out.println("Resource path: " + Arrays.toString(path));

        // HTTP method, e.g. GET or POST
        Method method = session.getMethod();

        // grabs all the numbers from the path, e.g /blockchain/3/transaction/4 returns [3, 4]
        int[] ids = parseIds(path);

        // e.g. ["blockchain", "3"] -> ["blockchain", ":"] -> "/blockchain/:"
        String generalResourcePath = Arrays.stream(path)
                .map((s) -> Util.isInt(s) ? ":" : s).reduce("", (s1, s2) -> s1 + "/" + s2);
        System.out.println("General resource path: " + generalResourcePath);
        switch(method) {

            // allows clients to request what kind of commands they are allowed to send
            case OPTIONS:
                if(options.containsKey(generalResourcePath)) {
                    return options.get(generalResourcePath).respond(session, ids);
                }
                // Explicitly allow the HTTP verbs GET,POST,PUT,DELETE,OPTIONS.
                // This (hopefully) avoids cross-site scripting security violations in the browser.
                return newFixedLengthResponse(
                        new Gson().toJson("200 OK\nAllow: GET,POST,PUT,DELETE,OPTIONS", String.class));

            // allows clients to view a resource and its information, e.g. "/blockchain/" gets the blockchain
            case GET:
                return respondOrNotFound(gets, generalResourcePath, ids, session);

            // allows clients to create a new resource based on HTTP body data they provide
            // e.g. "/blockchain/transaction" with json in the body of the http creates a new transaction
            case POST:
                return respondOrNotFound(posts, generalResourcePath, ids, session);

            case PUT:
                return respondOrNotFound(puts, generalResourcePath, ids, session);

            // allows clients to request that the server destroy a certain resource, e.g. "/blockchain/buffer"
            case DELETE:
                return respondOrNotFound(deletes, generalResourcePath, ids, session);

            // the server didn't recognize the request, send a 404 not found response
            default:
                return Failure.notFound().response(gson);
        }
    }

    /**
     * Responds with a registered response if the path is valid, otherwise return a 404 response.
     *
     * @param method the collection of ResourceResponses mapped to a path
     * @param path the requested path as a general path (ids replaced with ":")
     * @param ids the ids found from the specific requested path
     * @param session the client's request
     * @return a response to the requested path, or a 404 if it was not found.
     */
    private Response respondOrNotFound(
            Map<String, ResourceResponse> method,
            String path,
            int[] ids,
            IHTTPSession session) {

        ResourceResponse r = method.get(path);
        if(Objects.isNull(r)) {
            LOG.info("Path \"" + path + "\" not found.");
            return Failure.notFound().response(gson);
        }
        return r.respond(session, ids);
    }

    // Maps all the GET requests to a lambda function
    // Allows me to map a resource path, e.g. GET "/blockchain/" to an arbitrary method of my choosing, that conforms
    // to ResourceResponse.
    private Map<String, ResourceResponse> gets = new HashMap<>();

    /** Registers a new GET; if a client requests a concrete version of the general resourcePath, the response lambda
     * is called. */
    protected void get(String resourcePath, ResourceResponse response) {
        gets.put(resourcePath, response);
    }

    private Map<String, ResourceResponse> posts = new HashMap<>();

    protected void post(String resourcePath, ResourceResponse response) {
        posts.put(resourcePath, response);
    }

    private Map<String, ResourceResponse> puts = new HashMap<>();

    protected void put(String resourcePath, ResourceResponse response) {
        puts.put(resourcePath, response);
    }

    private Map<String, ResourceResponse> deletes = new HashMap<>();

    protected void delete(String resourcePath, ResourceResponse response) {
        deletes.put(resourcePath, response);
    }

    private Map<String, ResourceResponse> options = new HashMap<>();

    protected void options(String resourcePath, ResourceResponse response) {
        options.put(resourcePath, response);
    }

    // Parsing

    /**
     * <p>Parses the resource path into a String array.</p>
     *
     * <pre>
     * <code>
     *
     *     input: "path/TO/Resource"
     *     output: ["path", "to", "resource"]
     *
     * </code>
     * </pre>
     *
     * <p><strong>Note:</strong>Paths {@code ""} and {@code "/"} are automatically mapped to {@code "home"}</p>
     *
     * @param wholePath the entire path to the resource/command
     * @return an array of lowercase strings, each being a portion of the path.
     */
    protected String[] parsePathFrom(String wholePath) {
        if (wholePath.equals("") || wholePath.equals("/")) {
            // All we got was / , so alias that as the "home" command.
            return new String[] { "home" };
        }

        // split by "/", filter out the empty strings, make all the strings lowercase, collect into an array
        return Arrays.stream(wholePath.split("/"))
                .filter((s) -> !s.equals(""))
                .map(String::toLowerCase)
                .toArray(String[]::new);
    }

    /**
     * Parses the body of the given HTTP request (session, for whatever reason this framework doesn't call it a request,
     * I dunno why).
     * @param session the client's request, which includes the HTTP body.
     * @return an optional string representing the parsed body, or {@code Optional.empty()} if it could not be parsed.
     */
    protected Optional<String> parseBody(IHTTPSession session) {
        int contentLength = Integer.parseInt(session.getHeaders().get("content-length"));
        byte[] buf = new byte[contentLength];
        try {
            // do not put into try-with-resources (i.e. do not close the input stream)
            // if this input stream is closed the socket connection automatically closes too (before you respond to the
            // client!)
            InputStream in = session.getInputStream();
            in.read(buf, 0, contentLength);
            return Optional.of(new String(buf));
        } catch (IOException e) {
            e.printStackTrace();
            LOG.warning("Could not parse the body of the request.");
            return Optional.empty();
        }
    }

    /**
     * Extracts the ids in a path into an {@code int} array.
     *
     * <code>
     * <pre>
     *      ["blockchain", "3", "transaction", "1"] -> [3, 1]
     * </pre>
     * </code>
     *
     * @param path an array of path elements, e.g. ["blockchain", "3", "transaction"]
     * @return an {@code int} array containing the ids from the given path, in the order in which they appeared.
     */
    private int[] parseIds(String[] path) {
        return Arrays.stream(path)
                .filter(Util::isInt)
                .mapToInt(Integer::valueOf)
                .toArray();
    }

    /**
     * @param responseString the body of the response in JSON format.
     * @return a response with headers denoting that this response is JSON and is part of this api.
     */
    public static Response newApiResponse(Response.IStatus status, String responseString) {
        Response response = newFixedLengthResponse(status, "applications/json", responseString);
        // In general, we want to add the JSON and Access-Control headers to the response object.
        return addApiResponseHeaders(response);
    }

    /**
     * Creates a new API response with a status of 200 OK.
     * @param responseString
     * @return
     */
    public static Response newApiResponse(String responseString) {
        return newApiResponse(Response.Status.OK, responseString);
    }

    /**
     * @param t the body of the response, to be serialized as json.
     * @param <T>
     * @return a response with t in json form as the body.
     */
    public <T> Response newApiResponse(T t) {
        return newApiResponse(this.gson.toJson(t));
    }

    /**
     * @param response a response containing JSON part of this API.
     * @return a response with the headers denoting a JSON payload, and other specifics to do with this API.
     */
    private static Response addApiResponseHeaders(Response response) {
        // Add JSON and Access-Control headers to the response object.
        response.addHeader("Content-Type", "application/json");
        response.addHeader("Access-Control-Allow-Methods", "DELETE, GET, POST, PUT, OPTIONS");
        response.addHeader("Access-Control-Allow-Origin",  "*");
        response.addHeader("Access-Control-Allow-Headers", "X-Requested-With, Content-Type");
        return response;
    }

    /**
     * @param session the given session to which to respond
     * @param method the method of the request
     * @param command the command of the request
     * @return if the method was {@code GET}, a request containing the home HTML page. Otherwise, a response with some
     * information about the unknown request.
     */
    private Response responseForUnknownCommand(IHTTPSession session, Method method, String command) {

        String fromIP    = session.getHeaders().get("remote-addr");
        String userAgent = session.getHeaders().get("user-agent");

        // If someone is trying to run an unknown command, log their it (and their IP address).
        String msg = "Unknown " + method.toString() +
                " from " + fromIP + " ~ " + command + " ~ " + userAgent + " ~ ";

        String responseString;
        if (method == Method.GET) {
            // It was a GET, so send back the home/help/test page with the unknown command as the page header.
            responseString = homeHTML(session, msg);
        } else  {
            // Not being a GET, we don't want to respond with a web page,
            // so we'll respond with an JSON-encoded error message.
            responseString = "{\"message\":\"" + msg + "\"}";
        }

        // Write the mgs to the log file regardless.
        writeLog(msg);
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html", responseString);
    }

    private String homeHTML(IHTTPSession session, String heading) {
        StringBuilder sb;

        // Header
        sb = createApiHomeHeader(heading);

        // HTTP method
        sb.append("<p><strong>Method</strong> = ").append(String.valueOf(session.getMethod())).append("</p>");

        // URI / Command
        sb.append("<p><strong>URI / API command</strong> = ").append(String.valueOf(session.getUri())).append("</p>");

        // API help
        sb.append("<h3>API help</h3><pre>" +   apiHelp() + "</pre>");

        // API tester for GET and POST commands. Note: JavaScript function sendIt()is defined in the header.
        sb.append("<h3>API Testers</h3>");
        sb.append("<input type='button' value='GET'  style='width:64px;' onclick='sendIt(\"GET\");'>&nbsp;/<input type='text' id='txt2get' size='48' onkeydown='javascript:if(event.keyCode === 13) sendIt(\"GET\");'>");
        sb.append("<br>");
        sb.append("<input type='button' value='PUT' style='width:64px;' onclick='sendIt(\"PUT\");'>&nbsp;/<input type='text' id='txt2put' size='48' onkeydown='javascript:if(event.keyCode === 13) sendIt(\"PUT\");'>");
        sb.append("<br>");
        sb.append("<input type='button' value='POST' style='width:64px;' onclick='sendIt(\"POST\");'>&nbsp;/<input type='text' id='txt2post' size='48' onkeydown='javascript:if(event.keyCode === 13) sendIt(\"POST\");'>");
        sb.append("<br>");
        sb.append("<input type='button' value='DELETE' style='width:64px;' onclick='sendIt(\"DELETE\");'>&nbsp;/<input type='text' id='txt2delete' size='48' onkeydown='javascript:if(event.keyCode === 13) sendIt(\"DELETE\");'>");
        sb.append("<br>");
        sb.append("<label>Request Body: <br><textarea id='requestBody' rows='2' cols='64'></textarea></label>");
        sb.append("<br>");
        sb.append("<label>Response: <br><textarea id='taDisplay' rows='2' cols='64'></textarea>");

        // HTTP Headers
        sb.append("<h3>HTTP Headers</h3><blockquote>").append(toString(session.getHeaders())).append("</blockquote>");

        // Parameters
        sb.append("<h3>Parameters</h3><blockquote>").append(toString(session.getParms())).append("</blockquote>");
        Map<String, List<String>> decodedQueryParameters = decodeParameters(session.getQueryParameterString());
        sb.append("<h3>Parameters (multi values?)</h3><blockquote>").append(toString(decodedQueryParameters)).append("</blockquote>");

        // Files
        try {
            Map<String, String> files = new HashMap<>();
            session.parseBody(files);
            sb.append("<h3>Files</h3><blockquote>").
                    append(toString(files)).append("</blockquote>");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Footer
        sb.append(createApiHomeFooter());
        return sb.toString();
    }

    private String versionJSON() {
        return "{ " + "\"version\" : " + "\"" + APIConstants.apiVersion + "\"" + " }";
    }

    private String dateJSON() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        java.util.Date date = new java.util.Date();
        String dateStr = dateFormat.format(date);
        return "{ " + "\"currentDate\" : " + "\"" + dateStr + "\"}";
    }

    private String timeJSON() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss.SSSS");
        java.util.Date date = new java.util.Date();
        String timeStr = dateFormat.format(date);
        return "{ " + "\"currentTime\" : " + "\"" + timeStr + "\"}";
    }

    private String dateTimeJSON() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSSS");
        java.util.Date date = new java.util.Date();
        String dateTimeStr = dateFormat.format(date);
        return "{ " + "\"currentDateTime\" : " + "\"" + dateTimeStr + "\"}";
    }

    /**
     * List the API commands.
     * @return API help string
     */
    private String apiHelp() {
        return "API commands: GET [action], PUT [action], POST [action], DELETE [action]\n\n" +
                "+-- GET  /ver[sion]                                        - API version\n"  +
                "+-- GET  /date                                             - current date\n" +
                "+-- GET  /time                                             - current time\n" +
                "+-- GET  /datetime                                         - current date and time\n" +
                "+-- GET  /details                                          - the server's current mining difficulty and maximum tx per block (for all blockchains)\n" +
                "+-- GET  /blockchains                                      - the blockchain ids that the server has stored\n" + 
                " +- GET  /blockchain/{id}                                  - the entire blockchain with {id}\n" +
                " +- GET  /blockchain/{id}/buffer                           - blockchain with {id}'s transaction buffer\n" +
                " +- GET  /blockchain/{bcId}/transaction/{txId}             - the transaction with {txId} that belongs to blockchain with {bcId}\n" +
                " +- GET  /blockchain/{bcId}/block/{bId}/transaction/{txId} - the transaction with {txId} that belongs to block with {bId} that belongs to blockchain with {bcId}\n" +
                "\n" +
                "+-- POST /blockchain                                       - create a new blockchain, returns the newly created blockchain\n" +
                " +- POST /blockchain/{id}/transaction                      - create a new transaction, slated to be added to blockchain with {id}, and put it on the server's transaction buffer\n" +
                " +- POST /blockchain/{id}                                  - request the server empty its transactions from its tx buffer into a block, mine that block and add it to the blockchain\n" +
                "\n" +
                "+-- PUT \n" +
                "\n" +
                " +- DELETE /blockchain/{id}/buffer                         - request the server clear its transaction buffer\n" +
                "";
    }

    // Yikes
    private StringBuilder createApiHomeHeader(String heading) {
        final String title = APIConstants.apiName;
        final String copyright = "Copyright (c) 2017 Alan G. Labouseur and the Marist NSF-Stars. All Rights Reserved.";
        // TODO: separate .html file... or ... PHP?, perhaps not
        StringBuilder retVal = new StringBuilder();
        retVal.append("<!DOCTYPE html>");
        retVal.append("<html>");
        retVal.append("<head>");
        retVal.append("<title>" + title + "</title>");
        retVal.append("<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />");
        String postTesterJavaScript =
                "<script src='//ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js'></script>"
                        + "<script type='text/javascript'>"
                        + "function sendIt(method) {"
                        + "   addressToUse = window.location.hostname + ':" + this.getListeningPort() + "';"
                        + "   var url = 'http:' + String.fromCharCode(47,47) + addressToUse + '/';"
                        + "   var reqJSON = document.getElementById('requestBody').value;"
                        + "   if (method === 'GET') {"
                        + "      var commandText = document.getElementById('txt2get').value;"
                        + "      url = url + commandText;"
                        + "      window.location = url;"
                        + "   } else if (method === 'POST') {"
                        + "      var commandText = document.getElementById('txt2post').value;"
                        + "      url = url + commandText;"
                        + "      $.ajax(url, {"
                        + "         'data': reqJSON,"
                        + "         'type': 'POST',"
                        + "         'processData': false,"
                        + "         'contentType': 'application/json',"
                        + "         'crossDomain': true"
                        + "      })"
                        + "         .success(function (data, status) {"
                        + "            var msg = 'status: ' + status + '\\n';"
                        + "            if (data.message) {"
                        + "               msg += data.message;"
                        + "            } else {"
                        + "               try {"
                        + "                  msg += JSON.parse(data).message;"
                        + "               } catch (ex) {"
                        + "                  msg += ex.message + ' data = ' + 'data.message = ' + data.message;"
                        + "               }"
                        + "            }"
                        + "            display(msg);"
                        + "         })"
                        + "         .fail(function () {"
                        + "             display('Error: POST failure.');"
                        + "         });"
                        + "   } else if (method === 'PUT') {"
                        + "      var commandText = document.getElementById('txt2put').value;"
                        + "      url = url + commandText;"
                        + "      $.ajax(url, {"
                        + "         'data': reqJSON,"
                        + "         'type': 'PUT',"
                        + "         'processData': false,"
                        + "         'contentType': 'application/json',"
                        + "         'crossDomain': true"
                        + "      })"
                        + "         .success(function (data, status) {"
                        + "            var msg = 'status: ' + status + '\\n';"
                        + "            if (data.message) {"
                        + "               msg += data.message;"
                        + "            } else {"
                        + "               try {"
                        + "                  msg += JSON.parse(data).message;"
                        + "               } catch (ex) {"
                        + "                  msg += ex.message + ' data = ' + 'data.message = ' + data.message;"
                        + "               }"
                        + "            }"
                        + "            display(msg);"
                        + "         })"
                        + "         .fail(function () {"
                        + "             display('Error: PUT failure.');"
                        + "         });"
                        + "   } else if (method === 'DELETE') {"
                        + "      var commandText = document.getElementById('txt2delete').value;"
                        + "      url = url + commandText;"
                        + "      $.ajax(url, {"
                        + "         'data': '',"
                        + "         'type': 'DELETE',"
                        + "         'processData': false,"
                        + "         'contentType': 'application/json',"
                        + "         'crossDomain': true"
                        + "      })"
                        + "         .success(function (data, status) {"
                        + "            var msg = 'status: ' + status + '\\n';"
                        + "            if (data.message) {"
                        + "               msg += data.message;"
                        + "            } else {"
                        + "               try {"
                        + "                  msg += JSON.parse(data).message;"
                        + "               } catch (ex) {"
                        + "                  msg += ex.message + ' data = ' + 'data.message = ' + data.message;"
                        + "               }"
                        + "            }"
                        + "            display(msg);"
                        + "         })"
                        + "         .fail(function () {"
                        + "             display('Error: DELETE failure.');"
                        + "         });"
                        + "   } else {"
                        + "      alert(method + ' is not defined.');"
                        + "   }"
                        + "}"
                        + "function display(msg) {"
                        + "   document.getElementById('taDisplay').value = msg;"
                        + "}"
                        + "</script>";
        retVal.append(postTesterJavaScript);
        retVal.append("</head>");
        retVal.append("<body>");
        retVal.append("<h3>" + title + "</h3>");
        retVal.append("<h4>" + copyright+ "</h4>");
        retVal.append("<h1>" + heading + "</h1>");
        return retVal;
    }

    private StringBuilder createApiHomeFooter() {
        final String legalese  = "Any reproduction, retransmission, redistribution, reeducation, rememorization, "           +
                "reverberation, other re-generalization of this content —- either explicit or implicit -— " +
                "is prohibited without the express written consent of Ted Codd, Ian Fleming, and "          +
                "Stevie Ray Vaughan.";
        StringBuilder retVal = new StringBuilder();
        retVal.append("<blockquote><em>" + legalese + "</em></blockquote>");
        retVal.append("</body>");
        retVal.append("</html>");
        return retVal;
    }

    // FIXME: could be confused with this object's toString() method
    private String toString(Map<String, ? extends Object> map) {
        String retVal = "";
        if (map.size() > 0) {
            retVal = toUnsortedList(map);
        }
        return retVal;
    }

    private String toUnsortedList(Map<String, ? extends Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("<ul>");
        for (Map.Entry<?,?> entry : map.entrySet()) {
            makeListItem(sb, entry);
        }
        sb.append("</ul>");
        return sb.toString();
    }

    private void makeListItem(StringBuilder sb, Map.Entry<?,?> entry) {
        sb.append("<li><code><b>")
                .append(entry.getKey())
                .append("</b> = ")
                .append(entry.getValue())
                .append("</code></li>");
    }

    /**
     * Write to the error log database tables and text stream.
     * @param logMsg the message to log
     */
    private void writeLog(String logMsg) {

        File log = Paths.get("." + File.separator + "API.log").toFile();
        if(!log.exists()) {
            try {
                System.out.println("log doesn't exist");
                log.createNewFile();
            } catch(IOException ioE) {
                ioE.printStackTrace();
                System.err.println("Could not create the log file.");
            }

        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd kk:mm:ssXX");
        String nowString = formatter.format(now);

        try (FileWriter fw = new FileWriter(log.getPath(), true)) {
            fw.write(LocalDateTime.now() + " ~ " + APIConstants.apiName + logMsg + System.lineSeparator());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

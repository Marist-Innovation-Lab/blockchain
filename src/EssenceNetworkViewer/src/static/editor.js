var editor = ace.edit("editor");

function setup() {
    editor.session.setMode("ace/mode/markdown");
    Console_log("Essence Network Map", false);
    editor.setReadOnly(true);
    editor.setShowPrintMargin(false);
    editor.session.setUseWrapMode(true);
    document.getElementById('editor').style.fontSize='14px';
}



function Console_log(output) {

    editor.insert(String(output));

    // var logs = output;
    // logs.forEach(function(entry) {
    //     console.log(entry);
    // });
    //

}

console.log("Chrome ext ready");


function injectScript(file_path) {
    let node = document.body;
    let script = document.createElement("script");
    script.setAttribute('type', 'text/javascript');
    script.setAttribute('src', file_path);
    console.log(script);
    node.appendChild(script);
}

// reference the js file created by the webpack
let script_url = chrome.runtime.getURL("student-inject-script.js");

//event listener that makes sure all DOM elements are loaded prior to running injection script
document.addEventListener('readystatechange', event => {
    if (event.target.readyState === "complete") {
        //onload inject the script
        injectScript(script_url, 'ag-list');
    }
})

//Content script message handler

try {
    window.addEventListener("message", function (msg) {

        if (msg.data.type
            && (msg.data.type === "assignment_evaluate")) {

            console.log("Student Content script sending message to background script");
            console.log("MESSAGE: " + msg)
            //console.log(msg.data.type);
            //console.log(msg.data.output);

            chrome.runtime.sendMessage({type: "evaluation", output: msg.data}, (response) => {
                console.log(response);
            });
        }
        else if ((msg.data.type) && (msg.data.type === "evaluate_error")){
            console.log("Student Content script sending error message to background script");

            chrome.runtime.sendMessage({type: "evaluate_error", output: msg.data}, (response) => {
                console.log(response);
            });
        }
        else if ((msg.data.type) && (msg.data.type === "internal_server_error")){
            console.log("Student Content script sending error message to background script");

            chrome.runtime.sendMessage({type: "internal_server_error", output: msg.data}, (response) => {
                console.log(response);
            });
        }
        else {
            console.log("Student Content script sending error message to background script");
            chrome.runtime.sendMessage({type: "unknown", output: msg.data}, (response) => {
                console.log(response);
            });
        }

    }, false);
} catch (e) {
    console.log(e);
}


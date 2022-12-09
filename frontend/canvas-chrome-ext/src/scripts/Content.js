console.log("Chrome ext ready");

const site = window.location.hostname;
alert("Canvas-Code JS has been injected into: " + site);

if (site.includes("https://seattleu.instructure.com/")) {
    alert("inside SU");
}

if (site.includes("https://canvas.instructure.com/")) {
    alert("inside canvas");
}


function injectScript(file_path, tag){
    let node = document.body;
    let script = document.createElement("script");
    script.setAttribute('type', 'text/javascript');
    script.setAttribute('src', file_path);
    console.log(script);

    node.appendChild(script);
}

let script_url = chrome.runtime.getURL("scripts/inject_script.js");
injectScript(script_url, 'body');



try {
    window.addEventListener("message", function(event) {

        if (event.data.type
            && (event.data.type == "FROM_PAGE")) {

            console.log("sending message to bg")
            chrome.runtime.sendMessage({output: event.data = "waiting"}, (response) => {
                console.log(response);
            });
        }

    }, false);
}catch (e) {

}


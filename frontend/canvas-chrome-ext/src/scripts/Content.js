console.log("Chrome ext ready");

const site = window.location.hostname;
alert("Canvas-Code JS has been injected into: " + site);

function read_only_preview_SU(tag, attr_tag, attr_name, value){
    const code_preview = document.createElement(tag);
    code_preview.setAttribute(attr_tag, attr_name)
    code_preview.innerHTML = value;
    document.body.append(code_preview);
}

if (site.includes("https://seattleu.instructure.com/")) {
    alert("inside SU");

    read_only_preview_SU("div", "id", "js-read-only-preview", "custom element 1" );
}

if(site.includes("google.com")){

    alert("inside google");
    read_only_preview_SU("div", "id", "js-read-only-preview", "custom element 1" );
}

(function () {

    /*
    var fileInput = document.createElement("input");
    fileInput.id = "fileInput";
    fileInput.type = "file";
    fileInput.hidden = "true";

    var btn = document.createElement("input");
    btn.value = "Submit Check";
    btn.id = "submit_compile_code";
    btn.type = "button";
    //btn.onclick = fileInput.click();


    btn.addEventListener("click", function () {
        fileInput.click();

        const endpoint ="http://127.0.0.1:55321/";
        const formData = new FormData();
        formData.append("fileInput", fileInput.files[0]);

        //default multipart form data
        fetch(endpoint, {
            method: "post",
            body: formData
        }).catch(console.error);
    })
    */


    var btn_response = document.createElement("input");
    btn_response.value = "Get response";
    btn_response.id = "response_bth";
    btn_response.type = "button";


    btn_response.addEventListener("click", function () {
        //fileInput.click();

        let endpoint = "http://127.0.0.1:3002/hello"
        //default multipart form data
        fetch(endpoint, {
            method: "GET",
        }).catch(console.error).then(response => {
            console.log(response);
        });
    })


    /*
    btn_response.addEventListener("click", async function() {
        let endpoint = "http://127.0.0.1:3001/hello"
        const config = {
            method: 'get',
            url: endpoint
        }

        let res = await axios(config).catch(console.error).then(response => {
            console.log(response);
        });

        console.log(res.status);
    })
     */



    //find where to add upload button
    const el = document.querySelector(".FPdoLc.lJ9FBc center");
    console.log(el);
    //el.appendChild(fileInput);
    //el.appendChild(btn);

    el.appendChild(btn_response);

})();


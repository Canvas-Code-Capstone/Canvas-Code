
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

        const endpoint ="upload.php";
        const formData = new FormData();
        formData.append("fileInput", fileInput.files[0]);

        //default multipart form data
        fetch(endpoint, {
            method: "post",
            body: formData
        }).catch(console.error);

    })


    //find where to add upload button
    const el = document.querySelector(".FPdoLc.lJ9FBc center");
    console.log(el);
    el.appendChild(fileInput);
    el.appendChild(btn);

})();


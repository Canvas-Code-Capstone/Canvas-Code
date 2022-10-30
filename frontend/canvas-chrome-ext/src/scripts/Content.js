
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
    //tesdt
}


(function () {

    function  addBtn() {
        var btn = document.createElement("input");
        btn.value = "Submit Check";
        btn.id = "submit_compile_code";
        btn.type = "submit";

        //find where to add upload button
        const el = document.querySelector(".FPdoLc.lJ9FBc center");

        console.log(el);
        el.appendChild(btn);
    }

    function defineEvents () {
        document
            .getElementById("submit_compile_code")
            .addEventListener("click", function
                (event){
                upload_compile(event.target.value.split(" ")[1]);
                //needs to grab the file from the field
            });

    }

    function upload_compile(str) {
        console.log("connect to our server and upload assignment")
    }

    addBtn();
    defineEvents();

})();
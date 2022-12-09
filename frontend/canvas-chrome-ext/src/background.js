try {

    chrome.runtime.onMessage.addListener(function (message, sender, sendResponse) {
        console.log(message.output);
        console.log(sender);

        sendResponse("msg recieved");

        if (message.output === 'waiting') {
            chrome.tabs.create({
                url: chrome.runtime.getURL('components/compilation_notification.html'),
                active: false
            }, function(tab) {
                // After the tab has been created, open a window to inject the tab
                chrome.windows.create({
                    tabId: tab.id,
                    type: 'popup',
                    focused: true
                    // incognito, top, left, ...
                }).document.getElementsByClassName("update-text").innerHTML = message.output;
            });
        }


    })
}

catch (e) {
    console.log(e)
}



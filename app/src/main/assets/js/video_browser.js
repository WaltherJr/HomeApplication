if (window.trustedTypes && window.trustedTypes.createPolicy && !window.trustedTypes.defaultPolicy) {
    window.trustedTypes.createPolicy('default', {
        createHTML: string => string
        // Optional, only needed for script (url) tags
        //,createScriptURL: string => string
        //,createScript: string => string,
    });
}

setTimeout(function() {
    var dd = [...document.querySelectorAll('ytd-video-renderer a[href^="/watch?v="]')];

    dd.forEach(anchor => {
        anchor.addEventListener('click', function(event) {
            event.preventDefault();
            event.stopPropagation();
            javaInterface.playMusicVideoByURL(this.href);
        });
    });
}, 2000);

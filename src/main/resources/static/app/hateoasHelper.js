define(function (require) {

    return {
        wrapHref: wrapHref
    };

    // Take an href and serve it via obj.htmlUrl.href
    function wrapHref(href) {
        return {
            htmlUrl: {
                href: href
            }
        }
    }

});